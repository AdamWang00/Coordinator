package edu.brown.cs.student.kmeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Class to run k-means on a given set of vectors.
 *
 * @param <T> The type of vector to be clustered
 */
public class CosineKMeans<T extends VectorData<T>> {
  /**
   * A list of all centroids.
   */
  private List<Centroid> centroids;
  /**
   * A map from the centroids to a list of vectors in their cluster.
   */
  private Map<Centroid, List<T>> centroidClusters;
  /**
   * A list of all vectors.
   */
  private final List<T> vectorList;
  /**
   * The number of clusters to create.
   */
  private final int k;
  /**
   * The length of a vector in the vector list.
   */
  private final int attrCount;
  /**
   * The maximum number of iterations to execute kmeans.
   */
  private final int maxIterations;

  /**
   * A constructor for k-means.
   *
   * @param k             - the amount of clusters to create
   * @param vectorList    - the list of vectors to be clustered, each vector must have same amount
   *                      of coordinates. Coordinates must all be between 0 and 1.
   * @param maxIterations - the amount of times to run k-means (must be positive)
   */
  public CosineKMeans(int k, List<T> vectorList, int maxIterations) {
    centroidClusters = new HashMap<>();
    this.vectorList = vectorList;
    this.k = k;
    if (!vectorList.isEmpty()) {
      this.attrCount = vectorList.get(0).getLength();
    } else {
      this.attrCount = 0;
    }
    this.maxIterations = maxIterations;
    initializeCentroids();
  }

  /**
   * Initializes the centroids randomly.
   */
  private void initializeCentroids() {
    Random rand = new Random();
    this.centroids = new ArrayList<>();
    for (int i = 0; i < k; i++) {
      double[] centroidCoords = new double[attrCount];
      for (int j = 0; j < attrCount; j++) {
        centroidCoords[j] = rand.nextDouble();
      }
      Centroid toAdd = new Centroid(centroidCoords);
      centroidClusters.put(toAdd, new ArrayList<>());
      centroids.add(toAdd);
    }
  }

  /**
   * Key method of CosineKMeans class. Runs kmeans iteratively to group similar vectors together.
   *
   * @return - A map of centroids to a list of vectors within their cluster
   */
  public Map<Centroid, List<T>> createClusters() {
    for (int i = 0; i < maxIterations; i++) {
      List<Centroid> oldCentroids = this.centroids;
      // Put vectors into new clusters
      findClosestCentroids();
      // Create a list of new centroids
      List<Centroid> newCentroids = findAverages();
      this.centroids = newCentroids;
      // If no change has been made, we have reached convergence and break
      if (oldCentroids.equals(newCentroids)) {
        break;
      } else {
        // Otherwise we update our clusters and keep iterating
        this.centroidClusters = new HashMap<>();
        for (Centroid centroid : newCentroids) {
          centroidClusters.put(centroid, new ArrayList<>());
        }
      }
    }
    return centroidClusters;
  }

  /**
   * Puts each vector into the cluster of the closest centroid.
   */
  private void findClosestCentroids() {
    for (T vector : vectorList) {
      double minDistance = Double.POSITIVE_INFINITY;
      Centroid closestCentroid = null;
      for (Centroid centroid : centroids) {
        double distance = CosineKMeans.cosDistance(centroid.getCoords(), vector.getVector());
        if (distance < minDistance) {
          minDistance = distance;
          closestCentroid = centroid;
        }
      }
      centroidClusters.get(closestCentroid).add(vector);
    }
  }


  /**
   * Creates a new list/map of centroids, with the new centroids being the centroids of the current
   * clusters.
   *
   * @return - the new centroids of each cluster
   */
  private List<Centroid> findAverages() {
    List<Centroid> newCentroids = new ArrayList<>();
    for (Centroid centroid : centroids) {
      List<T> cluster = centroidClusters.get(centroid);
      if (cluster.isEmpty()) {
        newCentroids.add(centroid);
      } else {
        double[] averageCoords = new double[attrCount];
        Arrays.fill(averageCoords, 0);
        for (T vector : cluster) {
          double[] vectorCoords = normalize(vector.getVector());
          for (int i = 0; i < vector.getLength(); i++) {
            averageCoords[i] += vectorCoords[i] / cluster.size();
          }
        }
        newCentroids.add(new Centroid(normalize(averageCoords)));
      }
    }
    return newCentroids;
  }

  /**
   * Finds the center of all centroids.
   *
   * @return - the coordinates of the center of all centroids
   */
  protected double[] findCentroidCenter() {
    double[] centroidCenter = new double[attrCount];
    Arrays.fill(centroidCenter, 0);
    for (Centroid centroid : centroids) {
      double[] normalizedCentroid = normalize(centroid.getCoords());
      for (int i = 0; i < attrCount; i++) {
        centroidCenter[i] += normalizedCentroid[i] / k;
      }
    }
    return centroidCenter;
  }

  /**
   * Calculates the cosine distance between two list of coordinates. Assumes the cosine distance
   * between the 0 vector and any other vector is 1.
   *
   * @param coords      - a list of coordinates
   * @param otherCoords - a list of coordinates of the same size
   * @return - the cosine distance between them
   */
  public static double cosDistance(double[] coords, double[] otherCoords) {
    double centroidMagnitude = 0;
    double otherMagnitude = 0;
    double dotProduct = 0;
    for (int i = 0; i < coords.length; i++) {
      centroidMagnitude += coords[i] * coords[i];
      otherMagnitude += otherCoords[i] * otherCoords[i];
      dotProduct += coords[i] * otherCoords[i];
    }
    if (centroidMagnitude == 0 || otherMagnitude == 0) {
      return 1;
    } else {
      return 1 - dotProduct / Math.sqrt(centroidMagnitude) / Math.sqrt(otherMagnitude);
    }
  }
  /**
   * Given the coordinates of a vector in vectorList, find the normalized coordinates.
   *
   * @param coords - the coordinates of a vector in vectorList
   * @return - the normalzied coordinates
   */
  public static double[] normalize(double[] coords) {
    double magnitudeSquared = 0;
    for (double coord : coords) {
      magnitudeSquared += coord * coord;
    }
    double[] normalized = new double[coords.length];
    if (magnitudeSquared == 0) {
      Arrays.fill(normalized, 0);
      normalized[0] = 1;
      return normalized;
    }
    for (int i = 0; i < coords.length; i++) {
      normalized[i] += coords[i] / Math.sqrt(magnitudeSquared);
    }
    return normalized;
  }
}
