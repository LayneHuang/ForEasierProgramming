---
title: 树目录构建方法
date: 2022-9-25 21:00:00
categories: Business
---

树节点, 用于返回信息继承
```java
@Data
public class TreeNode {
    private static final long serialVersionUID = 7395241565634729984L;

    public TreeNode(Long id) {
        this.id = id;
        this.children = new ArrayList<>();
    }

    private Long id;

    private List<TreeNode> children;
}

```

边构建与转换
```java
public interface TreeNodeTransfer {
    /**
     * 节点ID
     */
    Long getId();

    /**
     * @return 父节点ID
     */
    Long getParentId();

    /**
     * 普通节点信息转换成树节点信息
     */
    TreeNode transfer();
}
```

建树工具
```java
public class TreeUtil<T extends TreeNodeTransfer> {

    /**
     * 建树
     *
     * @param root 根节点
     * @param list 节点信息列表
     */
    public void buildTree(TreeNode root, List<T> list) {
        doBuildTree(root, buildEdges(list), new HashSet<>());
    }

    /**
     * 构建边
     */
    private Map<Long, List<T>> buildEdges(List<T> list) {
        Map<Long, List<T>> edges = new HashMap<>();
        list.forEach(info -> {
            if (!edges.containsKey(info.getParentId())) {
                edges.put(info.getParentId(), new ArrayList<>());
            }
            List<T> children = edges.get(info.getParentId());
            children.add(info);
        });
        return edges;
    }

    /**
     * 递归建树
     */
    private void doBuildTree(TreeNode node, Map<Long, List<T>> edges, Set<Long> visited) {
        if (node == null || node.getId() == null) return;
        long id = node.getId();
        if (visited.contains(id)) return;
        visited.add(id);
        List<TreeNode> children = new ArrayList<>();
        node.setchildren(children);
        List<T> childrenInfo = edges.get(id);
        if (CollectionUtils.isEmpty(childrenInfo)) {
            return;
        }
        childrenInfo.forEach(info -> {
            TreeNode child = info.transfer();
            children.add(child);
            doBuildTree(child, edges, visited);
        });
    }
}
```