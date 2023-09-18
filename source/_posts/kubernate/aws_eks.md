---
title: aws-eks
date: 2023-9-18 21:40:00
categories: [ k8s, eks ]
---

The process of creating an eks instance

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

