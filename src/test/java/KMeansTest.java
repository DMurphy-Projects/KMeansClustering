import Helper.ClusterTreeHelper;
import Model.BestNode;
import Model.ClusteringNode;
import View.PanZoomPanel;
import ViewController.PanZoomController;

import javax.swing.*;
import java.awt.*;
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

    public static void assignToCentroids(double[][] data, int[] assignedTo, ClusteringNode centroidTree)
    {
        for (int i=0;i<data.length;i++)
        {
            BestNode<ClusteringNode> bestNode = new BestNode<ClusteringNode>();
            centroidTree.nearest(data[i], bestNode);

            bestNode.node.addPoint(data[i]);
            assignedTo[i] = bestNode.node.getIndex();
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

    public static void viewPanel(final double[][] data,
                                 final int[] centroidAssignment, final Color[] centroidColors) throws InterruptedException {
        JFrame window = new JFrame("Test Window");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setSize(600,600);

        PanZoomController panZoomController = new PanZoomController();
        PanZoomPanel view = new PanZoomPanel(panZoomController) {

            @Override
            protected void renderImplementation(Graphics g) {

                for (int i=0;i<data.length;i++)
                {
                    g.setColor(centroidColors[centroidAssignment[i] % centroidColors.length]);
                    g.fillOval((int)data[i][0], (int)data[i][1], 1, 1);
                }
            }
        };

        window.add(view);
        window.setVisible(true);

//        panZoomController.pan(new double[]{view.getWidth() / 2, view.getHeight() / 2});
        panZoomController.zoomOnWorldPosition(new double[]{0, 0}, 50);

        while(true)
        {
            view.repaint();

            Thread.sleep(100);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        KMeansTest test = new KMeansTest();

        int dimensions = 2, iterations = 0;
        double[][] data = test.createRandomData(100, dimensions, new Random(123));

        int[] centroidAssignment = new int[data.length];
        double[][] centroids = test.createRandomData(3, dimensions, new Random());

        double delta, err = 0.1;

        do {
            ClusterTreeHelper treeHelper = new ClusterTreeHelper();
            ClusteringNode centroidTree = treeHelper.createKDTree(cloneData(centroids));

            assignToCentroids(data, centroidAssignment, centroidTree);

            double[][] newCentroids = new double[centroids.length][];
            updateCentroids(newCentroids, centroidTree);

            delta = sumArray(calculateDelta(centroids, newCentroids));

            centroids = newCentroids;
            iterations++;
        } while(delta > err);

        System.out.println(Arrays.deepToString(centroids));
        System.out.println(iterations);

        viewPanel(data, centroidAssignment,
                new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK});
    }
}
