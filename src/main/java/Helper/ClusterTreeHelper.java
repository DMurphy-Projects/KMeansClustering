package Helper;

import Model.ClusteringNode;

public class ClusterTreeHelper extends TreeHelper<ClusteringNode> {
    protected ClusteringNode createNode(int index, double[] p, int axis, ClusteringNode left, ClusteringNode right) {
        return new ClusteringNode(index, p, axis, left, right);
    }
}
