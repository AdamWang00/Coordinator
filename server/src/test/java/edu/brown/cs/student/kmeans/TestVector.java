package edu.brown.cs.student.kmeans;

public class TestVector implements VectorData<TestVector> {
  private final double[] coords;

  public TestVector(double[] coords) {
    this.coords = coords;
  }

  @Override
  public double[] getVector() {
    return coords;
  }

  @Override
  public int getLength() {
    return coords.length;
  }
}
