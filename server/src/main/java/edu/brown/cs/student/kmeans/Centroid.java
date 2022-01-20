package edu.brown.cs.student.kmeans;

import java.util.Arrays;

/**
 * A class to represent a Centroid in space.
 */
public class Centroid {
  /**
   * The coordinates of the centroid.
   */
  private final double[] coords;

  /**
   * Constructor for centroid.
   *
   * @param coords - the coordinates of the centroid to be created
   */
  public Centroid(double[] coords) {
    this.coords = coords;
  }

  /**
   * Getter for coordinates.
   *
   * @return - the coordinates of the centroid.
   */
  public double[] getCoords() {
    return coords;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Centroid centroid = (Centroid) o;
    return Arrays.equals(coords, centroid.coords);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(coords);
  }
}
