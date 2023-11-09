---
title: aws-eks
date: 2023-9-18 21:40:00
categories: [ k8s, eks ]
---

The process of creating an eks instance

<!-- more -->

### install kubectl cli and eksctl

Because Aws-eks haven't `Visual dashboard for K8s`,
we have to config kubectl cli to create resource.

#### kubectl

{% link 'install kubectl' https://docs.aws.amazon.com/zh_cn/eks/latest/userguide/install-kubectl.html [title] %}

```shell
curl -O https://s3.us-west-2.amazonaws.com/amazon-eks/1.27.1/2023-04-19/bin/linux/amd64/kubectl
chmod +x ./kubectl
mkdir -p $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$HOME/bin:$PATH
echo 'export PATH=$HOME/bin:$PATH' >>~/.bashrc
kubectl version --short --client
```

#### eksctl

{% link 'install eksctl' https://github.com/eksctl-io/eksctl/blob/main/README.md#installation [title] %}

```shell
# for ARM systems, set ARCH to: `arm64`, `armv6` or `armv7`
ARCH=amd64
PLATFORM=$(uname -s)_$ARCH

curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_$PLATFORM.tar.gz"

# (Optional) Verify checksum
curl -sL "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_checksums.txt" | grep $PLATFORM | sha256sum --check

tar -xzf eksctl_$PLATFORM.tar.gz -C /tmp && rm eksctl_$PLATFORM.tar.gz

sudo mv /tmp/eksctl /usr/local/bin

```

actually:

```shell
curl -sLO "https://github.com/eksctl-io/eksctl/releases/latest/download/eksctl_Linux_amd64.tar.gz"
```

if too slow, we can just find the release in github.

#### AWS Cli

{% link 'AWS Cli' https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html [title] %}

```shell
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

```

### aws configure

config your local IAM security info

```shell
aws configure
```

{% img /images/pic_aws_iam.png %}

update eks info:

```shell
aws eks update-kubeconfig --region region-code --name my-cluster

```

### check connection

```shell
kubectl get svc

## console info:
[root@xxxxxxxxxxxxx aws]# kubectl get svc
NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.100.0.1   <none>        443/TCP   7d
```

### CREATE ELB

use helm to create aws-load-balancer-controller

if command `eksctl` exist error, Check ERROR Stack in `CloudFormation` in aws web

```shell
curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.5.4/docs/install/iam_policy.json
curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.5.4/docs/install/iam_policy_us-gov.json

echo 'download ok'

## use iam_policy.json or iam_policy_us-gov.json depend on your region
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json

echo 'create prolicy finished'

## create associate-iam-oidc-oidc if not exist
eksctl utils associate-iam-oidc-provider --cluster $cluster_name --approve

## use aws cli and kubectl to create role
oidc_id=$(aws eks describe-cluster --name my-cluster --query "cluster.identity.oidc.issuer" --output text | cut -d '/' -f 5)
aws iam list-open-id-connect-providers | grep $oidc_id | cut -d "/" -f4

aws iam create-role \
  --role-name AmazonEKSLoadBalancerControllerRole \
  --assume-role-policy-document file://"load-balancer-role-trust-policy.json"

### attach-role-policy
aws iam attach-role-policy \
  --policy-arn arn:aws:iam::${account_id}:policy/AWSLoadBalancerControllerIAMPolicy \
  --role-name AmazonEKSLoadBalancerControllerRole

kubectl apply -f aws-load-balancer-controller-service-account.yaml

echo 'create iam finished'

helm repo add eks https://aws.github.io/eks-charts
helm repo update eks

## if install tips : Error: INSTALLATION FAILED: cannot re-use a name that is still in use
helm delete aws-alb-ingress-controller -n kube-system
helm delete aws-load-balancer-controller -n kube-system

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set region=${regionCode} \
  --set vpcId=${vpcId} \
  --set clusterName=${clusterName} \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller

echo 'create elb finished'

kubectl get deployment -n kube-system aws-load-balancer-controller

```

#### load-balancer-role-trust-policy.json

replace: ${account_id},${region-code},${oidc_id} below

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::${account_id}:oidc-provider/oidc.eks.${region-code}.amazonaws.com/id/${oidc_id}"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "oidc.eks.${region-code}.amazonaws.com/id/${oidc_id}:aud": "sts.amazonaws.com",
          "oidc.eks.${region-code}.amazonaws.com/id/${oidc_id}:sub": "system:serviceaccount:kube-system:aws-load-balancer-controller"
        }
      }
    }
  ]
}
```

#### aws-load-balancer-controller-service-account.yaml

replace: ${account_id}

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    app.kubernetes.io/component: controller
    app.kubernetes.io/name: aws-load-balancer-controller
  name: aws-load-balancer-controller
  namespace: kube-system
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::${account_id}:role/AmazonEKSLoadBalancerControllerRole
```

### 子网标签配置

```yaml
kubernetes.io/cluster/my-cluster: owned
### 公网 (通常是这个)
kubernetes.io/role/elb: 1
### 私网
kubernetes.io/role/internal-elb: 1
```

### 多端口监听

部署MQTT服务时，需要对外暴漏 TCP:1883 端口

与集群内服务使用的ALB不一样，需要额外部署`NLB负载均衡器`
{% link 'nlb aws official
doc' https://docs.aws.amazon.com/zh_cn/eks/latest/userguide/network-load-balancing.html [title] %}

{% img /images/pic_aws_eks_lb.png %}

nlb与alb不同, 通过在 service 中指定 annotations, alb controller就会帮我们自动创建好负载均衡器