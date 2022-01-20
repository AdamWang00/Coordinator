package edu.brown.cs.student.kmeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that focuses on one main method getGroups() that uses kMeans to generate groups of equal
 * size.
 *
 * @param <T> - The type of objects to be clustered, must have coordinates and can
 *            be treated like a vector.
 */
public class KMeansGroups<T extends VectorData<T>> {
  /**
   * A map that keeps track of every centroid, and the vectors within the cluster defined by that
   * centroid.
   */
  private Map<Centroid, List<T>> centroidClusters;
  /**
   * A list of all vectors that have yet to be grouped.
   */
  private final List<T> vectorList;
  /**
   * The size of each group.
   */
  private final int groupSize;
  /**
   * A static integer that determines how long to run kMeans for.
   */
  private static final int MAX_ITERATIONS = 100;

  /**
   * The constructor for KMeansGroups. Each vector in vector list is assumed to have the same amount
   * of coordinates, and group size is assumed to be a positive integer. Assumes that vectors have
   * coordinates that are between 0 and 1.
   *
   * @param groupSize  - the size of each cluster
   * @param vectorList - the list of vectors to be clustered
   */
  public KMeansGroups(int groupSize, List<T> vectorList) {
    centroidClusters = new HashMap<>();
    this.vectorList = new ArrayList<>(vectorList);
    this.groupSize = groupSize;
    if (vectorList.size() == 0 || groupSize == 0) {
      throw new RuntimeException();
    }
  }

  /**
   * The main method of this class, divides the vectorList into groups of groupSize. Currently,
   * always creates groups larger than groupSize.
   *
   * @return - a list of list of vectors, each inner list is one group. Each group should have
   * relatively similar coordinates.
   */
  public List<List<T>> getGroups() {
    List<List<T>> groupings = new ArrayList<>();
    if (vectorList.size() < groupSize) {
      groupings.add(vectorList);
      return groupings;
    }
    int extra = vectorList.size() % groupSize;
    int extrasPerGroup = extra / (vectorList.size() / groupSize) + 1;
    while (vectorList.size() >= groupSize) {
      // Create a new instance of kmeans, and run it to get clustering
      CosineKMeans<T> kMeans = new CosineKMeans<>(vectorList.size() / groupSize,
          vectorList, MAX_ITERATIONS);
      centroidClusters = kMeans.createClusters();
      double[] center = kMeans.findCentroidCenter();
      // Find the cluster furthest from the rest
      Centroid furthestCentroid = findFurthest(center);
      List<T> finalGrouping;
      // Change its size to groupSize and add it to the final groupings
      if (centroidClusters.get(furthestCentroid).size() > groupSize) {
        finalGrouping = discard(
            centroidClusters.get(furthestCentroid).size() - groupSize
                - Math.min(extra, extrasPerGroup), furthestCentroid);
        extra -= Math.min(extra, extrasPerGroup);
      } else if (centroidClusters.get(furthestCentroid).size() < groupSize) {
        finalGrouping = recruit(
            groupSize - centroidClusters.get(furthestCentroid).size(), furthestCentroid);
      } else {
        finalGrouping = centroidClusters.get(furthestCentroid);
      }
      groupings.add(finalGrouping);
      for (T vector : finalGrouping) {
        vectorList.remove(vector);
      }
    }
    return groupings;
  }

  /**
   * Method to find the furthest centroid from a given point.
   *
   * @param center - an array of doubles, representing a list of coordinates
   * @return - the centroid (currently in centroidClusters) that is furthest from center, using
   * cosine distance
   */
  private Centroid findFurthest(double[] center) {
    double maxDistance = -1;
    Centroid furthestCentroid = null;
    for (Centroid centroid : centroidClusters.keySet()) {
      double cosDistance = CosineKMeans.cosDistance(centroid.getCoords(), center);
      if (maxDistance < cosDistance) {
        maxDistance = cosDistance;
        furthestCentroid = centroid;
      }
    }
    return furthestCentroid;
  }

  /**
   * Removes discardAmt vectors from a cluster. Removes the vectors that can most easily be placed
   * in other clusters. Also, removes the vectors from the vectorList, so that kMeans can be
   * run again on a smaller list of vectors.
   *
   * @param discardAmt - the amount of vectors to remove
   * @param centroid   - the centroid of the cluster that vectors should be removed from
   * @return - A list of vectors, representing the cluster with vectors removed.
   */
  private List<T> discard(int discardAmt, Centroid centroid) {
    List<T> finalGrouping = centroidClusters.get(centroid);
    for (int i = 0; i < discardAmt; i++) {
      T toDiscard = null;
      double discardDistance = Double.POSITIVE_INFINITY;
      for (T vector : finalGrouping) {
        double scd = secondClosestDistance(vector, centroid);
        if (scd <= discardDistance) {
          toDiscard = vector;
          discardDistance = scd;
        }
      }
      if (!finalGrouping.remove(toDiscard)) {
        System.out.println("Bad removal in discard.");
      }
    }
    return finalGrouping;
  }

  /**
   * Given a vector and the closest centroid to it (the centroid of the cluster to which it belongs)
   * return the (cosine) distance to the nearest centroid that is not its own. Assumes at least 2
   * clusters exist.
   *
   * @param vector  - a vector
   * @param closest - the centroid of the cluster that vector belongs to
   * @return - the distance from vector the the centroid that is 2nd closest
   */
  private double secondClosestDistance(T vector, Centroid closest) {
    double minDistance = Double.POSITIVE_INFINITY;
    for (Centroid centroid : centroidClusters.keySet()) {
      if (!closest.equals(centroid)) {
        double distance = CosineKMeans.cosDistance(vector.getVector(), centroid.getCoords());
        if (distance < minDistance) {
          minDistance = distance;
        }
      }
    }
    return minDistance;
  }

  /**
   * Adds recruitAmt vectors to a cluster. Adds the vectors that can most easily be placed
   * into this cluster. Also, removes the vectors from the vectorList, so that kMeans can be
   * run again on a smaller list of vectors.
   *
   * @param recruitAmt - the amount of vectors to add
   * @param centroid   - the centroid of the cluster that vectors should be added to
   * @return - A list of vectors, representing the cluster with vectors added.
   */
  private List<T> recruit(int recruitAmt, Centroid centroid) {
    List<T> finalGrouping = centroidClusters.get(centroid);
    for (int i = 0; i < recruitAmt; i++) {
      T toRecruit = null;
      double recruitDistance = Double.POSITIVE_INFINITY;
      for (T vector : vectorList) {
        if (!finalGrouping.contains(vector)) {
          double distance = CosineKMeans.cosDistance(centroid.getCoords(), vector.getVector());
          if (distance < recruitDistance) {
            toRecruit = vector;
            recruitDistance = distance;
          }
        }
      }
      finalGrouping.add(toRecruit);
    }
    return finalGrouping;
  }

}
