import Helper.ClusterTreeHelper;
import Model.BestNode;
import Model.ClusteringNode;

import java.util.Arrays;
import java.util.Random;

public class KMeansTest {

    public double[] createRandomPoint(int d, Random r)
    {
        double[] point = new double[d];
        for (int i=0;i<d;i++)
        {
            point[i] = r.nextInt(100);
        }
        return point;
    }

    public double[][] createRandomData(int n, int d, Random r)
    {
        double[][] centroids = new double[n][];
        for (int i=0;i<n;i++)
        {
            centroids[i] = createRandomPoint(d, r);
        }
        return centroids;
    }

    public static double[][] cloneData(double[][] source)
    {
        double [][] copy = new double[source.length][];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    public static void assignToCentroids(double[][] data, ClusteringNode centroidTree)
    {
        for (double[] d: data)
        {
            BestNode<ClusteringNode> bestNode = new BestNode<ClusteringNode>();
            centroidTree.nearest(d, bestNode);

            bestNode.node.addPoint(d);
        }
    }

    public static void updateCentroids(double[][] list, ClusteringNode node)
    {
        if (node == null) return;
        list[node.getIndex()] = node.calculateAveragePoint();

        updateCentroids(list, node.left());
        updateCentroids(list, node.right());
    }

    public static double[] calculateDelta(double[][] oldCentroids, double[][] newCentroids)
    {
        double[] delta = new double[oldCentroids.length];

        for (int i=0;i<oldCentroids.length;i++)
        {
            for (int j=0;j<oldCentroids[0].length;j++)
            {
                delta[i] += (oldCentroids[i][j] - newCentroids[i][j]) * (oldCentroids[i][j] - newCentroids[i][j]);
            }
        }

        return delta;
    }

    public static double sumArray(double[] data)
    {
        double total = 0;
        for (double d: data)
        {
            total += d;
        }
        return total;
    }

    public static void main(String[] args)
    {
        KMeansTest test = new KMeansTest();

        int dimensions = 2, iterations = 0;
        double[][] data = test.createRandomData(100, dimensions, new Random(123));
        double[][] centroids = test.createRandomData(2, dimensions, new Random());
        double delta = 0, err = 0.1;

        do {
            ClusterTreeHelper treeHelper = new ClusterTreeHelper();
            ClusteringNode centroidTree = treeHelper.createKDTree(cloneData(centroids));

            assignToCentroids(data, centroidTree);

            double[][] newCentroids = new double[centroids.length][];
            updateCentroids(newCentroids, centroidTree);

            delta = sumArray(calculateDelta(centroids, newCentroids));

            centroids = newCentroids;
            iterations++;
        } while(delta > err);

        System.out.println(Arrays.deepToString(centroids));
        System.out.println(iterations);
    }
}
