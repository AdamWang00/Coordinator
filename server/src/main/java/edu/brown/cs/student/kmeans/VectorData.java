package edu.brown.cs.student.kmeans;

/**
 * A interface to represent a vector.
 *
 * @param <T> The datatype to be represented as a vector
 */
public interface VectorData<T extends VectorData> {
  /**
   * Getter for the coordinates of a vector.
   *
   * @return - the coordinates of a vector represented as an array of doubles
   */
  double[] getVector();

  /**
   * Getter for the dimension of a vector.
   *
   * @return - the number of attributes in the vector
   */
  int getLength();
}
