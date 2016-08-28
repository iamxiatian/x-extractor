package ruc.irm.xextractor.algorithm.kmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class for clustering a set of points.
 *
 * @author Tian Xia
 * @date Aug 28, 2016 23:44
 */
public class Clusterer {

    /** 向量的维度 */
    protected int dimension;

    protected List<Point> instances = new ArrayList<Point>();

    public Point instance(int index) {
        return instances.get(index);
    }

    public int getDimension() {
        return dimension;
    }
}
