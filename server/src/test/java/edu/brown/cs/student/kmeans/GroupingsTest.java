package edu.brown.cs.student.kmeans;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

public class GroupingsTest {

  public static class TestVector implements VectorData<TestVector> {
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


  @Test
  public void testKMeansEqual() {
    double [] coords1 = {1,0};
    double [] coords2 = {0,1};
    List<TestVector> vectors = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      vectors.add(new TestVector(coords1));
      vectors.add(new TestVector(coords2));
    }
    KMeansGroups<TestVector> kGroups = new KMeansGroups<>(5, vectors);
    List<List<TestVector>> groupings = kGroups.getGroups();
    assertEquals(groupings.size(), 2);
    for (List<TestVector> group: groupings) {
      assertEquals(group.size(), 5);
      boolean xClusterP = (group.get(0).getVector()[0] == 0.0);
      for (TestVector vector: group) {
        if (xClusterP) {
          assertEquals(0.0, vector.getVector()[0], 0.001);
          assertEquals(1.0, vector.getVector()[1], 0.001);
        } else {
          assertEquals(1.0, vector.getVector()[0], 0.001);
          assertEquals(0.0, vector.getVector()[1], 0.001);
        }
      }
    }
  }
  @Test
  public void testKMeansUnequal() {
    for (int i = 0; i < 20; i++) {
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
      KMeansGroups<TestVector> kGroups = new KMeansGroups<>(4, vectors);
      List<List<TestVector>> groupings = kGroups.getGroups();
      assertEquals(groupings.size(), 2);
      for (List<TestVector> group: groupings) {
        assertEquals(group.size(), 4);
        boolean topClusterP = (group.get(0).getVector()[1] > 0.5);
        for (TestVector vector: group) {
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
  public void testKMeans4Clusters() {
    for (int j = 0; j < 20; j++) {
      List<TestVector> vectors = new ArrayList<>();
      // 16 Coords, in four distinct groups
      vectors.add(new TestVector(new double[]{0.9,0.1}));
      vectors.add(new TestVector(new double[]{0.91,0.11}));
      vectors.add(new TestVector(new double[]{0.92,0.08}));
      vectors.add(new TestVector(new double[]{0.93,0.11}));
      vectors.add(new TestVector(new double[]{0.1,0.92}));
      vectors.add(new TestVector(new double[]{0.11,0.93}));
      vectors.add(new TestVector(new double[]{0.09,0.915}));
      vectors.add(new TestVector(new double[]{0.12,0.92}));
      vectors.add(new TestVector(new double[]{0.3,0.7}));
      vectors.add(new TestVector(new double[]{0.32,0.68}));
      vectors.add(new TestVector(new double[]{0.31,0.69}));
      vectors.add(new TestVector(new double[]{0.29,0.71}));
      vectors.add(new TestVector(new double[]{0.6,0.82}));
      vectors.add(new TestVector(new double[]{0.6,0.78}));
      vectors.add(new TestVector(new double[]{0.59,0.79}));
      vectors.add(new TestVector(new double[]{0.61,0.81}));
      KMeansGroups<TestVector> kGroups = new KMeansGroups<>(4, vectors);
      List<List<TestVector>> groupings = kGroups.getGroups();
      assertEquals(groupings.size(), 4);
      for (List<TestVector> group: groupings) {
        assertEquals(group.size(), 4);
        for (TestVector vector: group) {
          for (TestVector otherVector: group) {
            assertTrue(CosineKMeans.cosDistance(vector.getVector(), otherVector.getVector()) < 0.05);
          }
        }
      }
    }
  }

  @Test
  public void testDiscardRecruit() {
    for (int j = 0; j < 20; j++) {
      List<TestVector> vectors = new ArrayList<>();
      // 8 coords, 2 groups of 3 and 5 each.
      // The furthest in the large group should be moved to the smaller group
      vectors.add(new TestVector(new double[]{0.9,0.1}));
      vectors.add(new TestVector(new double[]{0.91,0.11}));
      vectors.add(new TestVector(new double[]{0.92,0.08}));
      vectors.add(new TestVector(new double[]{0.93,0.11}));
      vectors.add(new TestVector(new double[]{0.7,0.3}));
      vectors.add(new TestVector(new double[]{0.11,0.93}));
      vectors.add(new TestVector(new double[]{0.09,0.915}));
      vectors.add(new TestVector(new double[]{0.12,0.92}));
      KMeansGroups<TestVector> kGroups = new KMeansGroups<>(4, vectors);
      List<List<TestVector>> groupings = kGroups.getGroups();
      assertEquals(groupings.size(), 2);
      for (List<TestVector> group: groupings) {
        assertEquals(group.size(), 4);
        boolean lowXCoords = false;
        if (group.get(1).getVector()[0] < 0.8) {
          lowXCoords = true;
        }
        for (TestVector vector: group) {
         if (lowXCoords) {
           assertTrue(vector.getVector()[0] < 0.8);
         } else {
           assertTrue(vector.getVector()[0] > 0.8);
         }
        }
      }
    }
  }

  @Test
  public void testUnevenGroups() {
    for (int j = 0; j < 20; j++) {
      List<TestVector> vectors = new ArrayList<>();
      // 9 coords, 2 groups of 4 and 5 each.
      // The furthest in the large group should be moved to the smaller group
      vectors.add(new TestVector(new double[]{0.9,0.1}));
      vectors.add(new TestVector(new double[]{0.91,0.11}));
      vectors.add(new TestVector(new double[]{0.92,0.08}));
      vectors.add(new TestVector(new double[]{0.93,0.11}));
      vectors.add(new TestVector(new double[]{0.11,0.93}));
      vectors.add(new TestVector(new double[]{0.09,0.915}));
      vectors.add(new TestVector(new double[]{0.12,0.92}));
      vectors.add(new TestVector(new double[]{0.09,0.93}));
      vectors.add(new TestVector(new double[]{0.115,0.91}));
      KMeansGroups<TestVector> kGroups = new KMeansGroups<>(4, vectors);
      List<List<TestVector>> groupings = kGroups.getGroups();
      assertEquals(groupings.size(), 2);
      for (List<TestVector> group: groupings) {
        boolean lowXCoords;
        if (group.get(1).getVector()[0] < 0.8) {
          lowXCoords = true;
          assertEquals(group.size(),5);
        } else {
          lowXCoords = false;
          assertEquals(group.size(),4);
        }
        for (TestVector vector: group) {
          if (lowXCoords) {
            assertTrue(vector.getVector()[0] < 0.8);
          } else {
            assertTrue(vector.getVector()[0] > 0.8);
          }
        }
      }
    }
  }

  @Test
  public void testAgainstRandom(){
    // Number of iterations currently set to 2, has been tested at up to 5000
    for (int i = 0; i < 2; i++) {
      List<TestVector> vectors = generateVectors();
      Random rand = new Random();
      int groupSize = rand.nextInt(15) + 1;
      KMeansGroups<TestVector> kGroups = new KMeansGroups<>(groupSize, vectors);
      List<List<TestVector>> kGroupings = kGroups.getGroups();
      List<List<TestVector>> randomGroupings = generateRandomGroupings(groupSize, vectors);
      compareGroupings(kGroupings, randomGroupings);
    }
  }

  public List<TestVector> generateVectors() {
    Random rand = new Random();
    List<TestVector> vectors = new ArrayList<>();
    int vectorAmt = rand.nextInt(1000) + 1;
    int vectorLength = rand.nextInt(100) + 1;
    for (int i = 0; i < vectorAmt; i++) {
      double[] coords = new double[vectorLength];
      for (int j = 0; j < vectorLength; j++) {
        coords[j] = rand.nextDouble();
      }
      vectors.add(new TestVector(coords));
    }
    return vectors;
  }

  public List<List<TestVector>> generateRandomGroupings(int groupSize, List<TestVector> vectors) {
    Random rand = new Random();
    List<List<TestVector>> groupings = new ArrayList<>();
    if (groupSize > vectors.size()) {
      groupings.add(vectors);
      return groupings;
    }
    int extras = vectors.size() % groupSize;
    int extrasPerGroup = extras / (vectors.size() / groupSize) + 1;

    while (vectors.size() > 0) {
      List<TestVector> group = new ArrayList<>();
      for (int i = 0; i < groupSize; i++) {
        group.add(vectors.remove(rand.nextInt(vectors.size())));
      }
      if (extras > 0) {
        for (int i = 0; i < extrasPerGroup; i++){
          if (extras > 0) {
            group.add(vectors.remove(rand.nextInt(vectors.size())));
            extras--;
          }
        }
      }
      groupings.add(group);
    }
    return groupings;
  }

  public void compareGroupings(List<List<TestVector>> betterGroup,
                               List<List<TestVector>> worseGroup) {
  assertEquals(betterGroup.size(), worseGroup.size());
  assertTrue(clusteringLoss(betterGroup) <= clusteringLoss(worseGroup)+0.0000001);
  }

  public double clusteringLoss(List<List<TestVector>> groupings) {
    double totalLoss = 0;
    for (List<TestVector> group: groupings) {
      double[] averageCoords = new double[group.get(0).getLength()];
      for (TestVector vector : group) {
        double[] vectorCoords = CosineKMeans.normalize(vector.getVector());
        for (int i = 0; i < vector.getLength(); i++) {
          averageCoords[i] += vectorCoords[i] / group.size();
        }
      }
      for (TestVector vector : group) {
        totalLoss += CosineKMeans.cosDistance(vector.getVector(), averageCoords);
      }
    }
    return totalLoss;
  }
}
