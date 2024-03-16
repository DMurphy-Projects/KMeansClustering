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

    public static double calcDistance(double[] p1, double[] p2)
    {
        double d = 0;
        for (int i=0;i<p1.length;i++)
        {
            d += (p1[i] - p2[i]) * (p1[i] - p2[i]);
        }
        return d;
    }

    public static int nearestCentroid(double[][] centroids, double[] point)
    {
        int nearest = -1;
        double best = -1;
        for (int i=0;i<centroids.length;i++)
        {
            double d = calcDistance(centroids[i], point);
            if (best == -1 || best > d)
            {
                best = d;
                nearest = i;
            }
        }
        return nearest;
    }

    public static void assignToCentroidsNoTree(double[][] data, int[] assignedTo, double[][] centroids)
    {
        for (int i=0;i<data.length;i++)
        {
            assignedTo[i] = nearestCentroid(centroids, data[i]);;
        }
    }

    public static void updateCentroidsNoTree(double[][] data, int[] assignedTo, double[][] list)
    {
        int[] count = new int[list.length];

        for (int i=0;i<data.length;i++)
        {
            int centroidIndex = assignedTo[i];
            count[centroidIndex]++;
            for (int j=0;j<list[0].length;j++)
            {
                list[centroidIndex][j] += data[i][j];
            }
        }

        for (int i=0;i<list.length;i++)
        {
            for (int j=0;j<list[0].length;j++)
            {
                if (count[i] > 0) {
                    list[i][j] *= 1d / count[i];
                }
            }
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

    public static void centroidWithTrees(int dataCount, int centroidCount, int dataSeed, int centroidSeed, boolean view)
    {
        KMeansTest test = new KMeansTest();

        int dimensions = 2, iterations = 0;
        double[][] data = test.createRandomData(dataCount, dimensions, new Random(dataSeed));

        int[] centroidAssignment = new int[data.length];
        double[][] centroids = test.createRandomData(centroidCount, dimensions, new Random(centroidSeed));

        double delta, err = 0.1;

        do {
            ClusterTreeHelper treeHelper = new ClusterTreeHelper();
            ClusteringNode centroidTree = treeHelper.createKDTree(cloneData(centroids));

            assignToCentroids(data, centroidAssignment, centroidTree);

            double[][] newCentroids = new double[centroids.length][dimensions];
            updateCentroids(newCentroids, centroidTree);

            delta = sumArray(calculateDelta(centroids, newCentroids));

            centroids = newCentroids;
            iterations++;
        } while(delta > err);

        System.out.println(Arrays.toString(centroidAssignment));
        System.out.println(iterations);

        if (view) {
            try {
                viewPanel(data, centroidAssignment,
                        new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void centroidWithoutTrees(int dataCount, int centroidCount, int dataSeed, int centroidSeed, boolean view)
    {
        KMeansTest test = new KMeansTest();

        int dimensions = 2, iterations = 0;
        double[][] data = test.createRandomData(dataCount, dimensions, new Random(dataSeed));

        int[] centroidAssignment = new int[data.length];
        double[][] centroids = test.createRandomData(centroidCount, dimensions, new Random(centroidSeed));

        double delta, err = 0.1;

        do {
            assignToCentroidsNoTree(data, centroidAssignment, centroids);

            double[][] newCentroids = new double[centroids.length][dimensions];
            updateCentroidsNoTree(data, centroidAssignment, newCentroids);

            delta = sumArray(calculateDelta(centroids, newCentroids));

            centroids = newCentroids;
            iterations++;
        } while(delta > err);

        System.out.println(Arrays.toString(centroidAssignment));
        System.out.println(iterations);

        if (view) {
            try {
                viewPanel(data, centroidAssignment,
                        new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK});
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void benchmark()
    {
        int dataCount = 100000, centroidCount = 1000;

        long t0 = System.currentTimeMillis();
        centroidWithTrees(dataCount, centroidCount, 123, 321, false);
        long t1 = System.currentTimeMillis();
        centroidWithoutTrees(dataCount, centroidCount, 123, 321, false);
        long t2 = System.currentTimeMillis();;

        System.out.println(String.format("With Tree: %s", t1 - t0));
        System.out.println(String.format("Without Tree: %s", t2 - t1));
    }

    //note: due to non-tree method indexes in order, whilst tree method indexes based on a tree heirarchy
    //so point indexes (and therefore coloring) cannot be directly compared
    public static void compareVisualisation()
    {
        final int dataCount = 100, centroidCount = 4;

        new Thread(new Runnable() {
            public void run() {
                centroidWithTrees(dataCount, centroidCount, 123, 123, true);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                centroidWithoutTrees(dataCount, centroidCount, 123, 123, true);
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {

        benchmark();
//        compareVisualisation();
    }
}
