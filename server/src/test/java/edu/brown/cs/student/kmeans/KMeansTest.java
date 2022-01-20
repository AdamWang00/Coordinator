package edu.brown.cs.student.kmeans;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class KMeansTest {

  @Test
  public void testNormalize() {
    double[] coords1 = {1,0};
    double[] coords2 = {0.5,0};
    double[] coords3 = {0.5,0.5};
    double[] coords4 = {0.75,0.75};
    double[] coords5 = {-0.5,0.5};
    double[] coords6 = {3,4};
    double[] coords7 = {1,2,2,4};
    assertArrayEquals(CosineKMeans.normalize(coords1), coords1, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords2), coords1, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords3), new double[]{0.707,0.707}, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords4), new double[]{0.707,0.707}, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords5), new double[]{-0.707,0.707}, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords6), new double[]{0.6,0.8}, 0.001);
    assertArrayEquals(CosineKMeans.normalize(coords7), new double[]{0.2,0.4,0.4,0.8}, 0.001);
  }

  @Test
  public void testCosDistance() {
    double[] coords1 = {1,0};
    double[] coords2 = {0.5,0};
    double[] coords3 = {0.5,0.5};
    double[] coords4 = {0.75,0.75};
    double[] coords5 = {0.2,0.3,0.4};
    double[] coords6 = {0.5,0.6,0.7};
    assertEquals(CosineKMeans.cosDistance(coords1,coords1), 0, 0);
    assertEquals(CosineKMeans.cosDistance(coords1,coords2), 0, 0);
    assertEquals(CosineKMeans.cosDistance(coords2,coords3), 1-0.707, 0.01);
    assertEquals(CosineKMeans.cosDistance(coords4,coords3), 0, 0.0001);
    assertEquals(CosineKMeans.cosDistance(new double[]{0.1}, new double[]{0.2}), 0,0.0001);
    assertEquals(CosineKMeans.cosDistance(coords5,coords6), 1-0.9915, 0.0001);
  }
  @Test
  public void testKMeansEqual() {
    double [] coords1 = {1,0};
    double [] coords2 = {0,1};
    List<TestVector> vectors = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      vectors.add(new TestVector(coords1));
      vectors.add(new TestVector(coords2));
    }
   CosineKMeans<TestVector> kmeans = new CosineKMeans<>(2, vectors, 5);
   Map<Centroid, List<TestVector>> clusters = kmeans.createClusters();
   Set<Centroid> keySet = clusters.keySet();
   assertEquals(keySet.size(),2);
   assertTrue(keySet.contains(new Centroid(new double[]{1, 0})));
   assertTrue(keySet.contains(new Centroid(new double[]{0, 1})));
   assertEquals(clusters.get(new Centroid(new double[]{1, 0})).size(), 5);
   for (TestVector vector: clusters.get(new Centroid(new double[]{1, 0}))) {
     assertArrayEquals(vector.getVector(), new double[] {1,0}, 0.001);
   }
   assertEquals(clusters.get(new Centroid(new double[]{0, 1})).size(), 5);
   for (TestVector vector: clusters.get(new Centroid(new double[]{0, 1}))) {
     assertArrayEquals(vector.getVector(), new double[] {0,1}, 0.001);
   }
  }

  @Test
  public void testKMeansSpread() {
    for (int j = 0; j < 100; j++) {
      List<TestVector> vectors = new ArrayList<>();
      // 8 Coords, in two distinct groups
      vectors.add(new TestVector(new double[]{0.8,0.1}));
      vectors.add(new TestVector(new double[]{0.8,0.2}));
      vectors.add(new TestVector(new double[]{0.7,0.1}));
      vectors.add(new TestVector(new double[]{0.7,0.2}));
      vectors.add(new TestVector(new double[]{0.1,0.8}));
      vectors.add(new TestVector(new double[]{0.2,0.8}));
      vectors.add(new TestVector(new double[]{0.1,0.7}));
      vectors.add(new TestVector(new double[]{0.1,0.8}));
      CosineKMeans<TestVector> kmeans = new CosineKMeans<>(2, vectors, 20);
      Map<Centroid, List<TestVector>> clusters = kmeans.createClusters();
      Set<Centroid> keySet = clusters.keySet();
      assertEquals(keySet.size(),2);
      for (Centroid key: keySet) {
        boolean topClusterP = (clusters.get(key).get(0).getVector()[1] > 0.5);
        for (TestVector vector: clusters.get(key)) {
          if (topClusterP) {
            assertTrue(vector.getVector()[0] < 0.5);
            assertTrue(vector.getVector()[1] > 0.5);
          } else {
            assertTrue(vector.getVector()[0] > 0.5);
            assertTrue(vector.getVector()[1] < 0.5);
          }
        }
      }
    }
  }

  @Test
  public void testUnevenClusters() {
    for (int j = 0; j < 20; j++) {
      List<TestVector> vectors = new ArrayList<>();
      // 8 coords, 2 groups of 3 and 5 each.
      vectors.add(new TestVector(new double[]{0.9,0.1}));
      vectors.add(new TestVector(new double[]{0.91,0.11}));
      vectors.add(new TestVector(new double[]{0.92,0.08}));
      vectors.add(new TestVector(new double[]{0.93,0.11}));
      vectors.add(new TestVector(new double[]{0.7,0.3}));
      vectors.add(new TestVector(new double[]{0.11,0.93}));
      vectors.add(new TestVector(new double[]{0.09,0.915}));
      vectors.add(new TestVector(new double[]{0.12,0.92}));
      CosineKMeans<TestVector> kmeans = new CosineKMeans<>(2, vectors, 5);
      Map<Centroid, List<TestVector>> clusters = kmeans.createClusters();
      Set<Centroid> keySet = clusters.keySet();
      assertEquals(keySet.size(), 2);
      for (Centroid key: keySet) {
        boolean topClusterP = (clusters.get(key).get(0).getVector()[1] > 0.5);
        assertTrue(clusters.get(key).size() == 3 || clusters.get(key).size() == 5);
        for (TestVector vector: clusters.get(key)) {
          if (topClusterP) {
            assertTrue(vector.getVector()[0] < 0.5);
            assertTrue(vector.getVector()[1] > 0.5);
          } else {
            assertTrue(vector.getVector()[0] > 0.5);
            assertTrue(vector.getVector()[1] < 0.5);
          }
        }
      }
    }
  }
}
