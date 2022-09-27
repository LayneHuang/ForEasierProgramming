---
title: 树目录构建方法
date: 2022-9-25 21:00:00
categories: Business
---
很多时候业务需要用到树形目录结构，归纳模板，方便构建

<!-- more -->

树节点, 用于返回普通信息进行继承。
普通信息列表获取后转成列表

```java

@Data
public class TreeNode implements Serializable {
    private static final long serialVersionUID = -1;

    public TreeNode(Long id) {
        this.id = id;
        this.children = new ArrayList<>();
    }

    public TreeNode(Long id, Long parentId) {
        this.id = id;
        this.parentId = parentId;
        this.children = new ArrayList<>();
    }

    private Long id;

    private Long parentId;

    private List<TreeNode> children;
}

```

建树工具

```java
public class TreeUtil<T extends TreeNode> {

    public TreeNode buildTree(long rootId, List<T> list) {
        TreeNode root = getRootInfo(rootId, list);
        doBuildTree(root, buildEdges(list), new HashSet<>());
        return root;
    }

    private TreeNode getRootInfo(long rootId, List<T> list) {
        for (T info : list) {
            if (Objects.equals(info.getId(), rootId)) {
                return info;
            }
        }
        return new TreeNode(rootId);
    }

    private Map<Long, List<T>> buildEdges(List<T> list) {
        Map<Long, List<T>> edges = new HashMap<>();
        for (T info : list) {
            if (info.getParentId() == null) continue;
            if (!edges.containsKey(info.getParentId())) {
                edges.put(info.getParentId(), new ArrayList<>());
            }
            List<T> children = edges.get(info.getParentId());
            children.add(info);
        }
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
        List<T> children = edges.getOrDefault(id, new ArrayList<>());
        node.setChildren(children);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (T child : children) {
            doBuildTree(child, edges, visited);
        }
        // 树节点升序排序
        children.sort(Comparator.comparingInt(TreeNode::getSeq));
    }
}
```