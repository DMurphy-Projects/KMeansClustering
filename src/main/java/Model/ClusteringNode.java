package Model;

import java.util.ArrayList;
import java.util.List;

public class ClusteringNode extends KDTreeNode<ClusteringNode> {

    List<double[]> points = new ArrayList<double[]>();

    public ClusteringNode(int index, double[] p, int a, KDTreeNode l, KDTreeNode r) {
        super(index, p, a, l, r);
    }

    public void addPoint(double[] p)
    {
        points.add(p);
    }

    public double[] calculateAveragePoint()
    {
        double[] avg = new double[point.length];
        double denom = 1d / points.size();

        for (double[] p: points)
        {
            for (int i=0;i<point.length;i++)
            {
                avg[i] += p[i] * denom;
            }
        }

        return avg;
    }
}
