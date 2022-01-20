package edu.brown.cs.student.kmeans;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class TimeSelectionTest {
  @Test
  public void testNoMult2D() {
    List<TestVector> vectorList = new ArrayList<>();
    vectorList.add(new TestVector(new double[]{1,0}));
    TimeSelection<TestVector> time = new TimeSelection<>(vectorList);
    List<Integer> expectedTimes = new ArrayList<>();
    expectedTimes.add(0);
    assertEquals(time.getBestTimes(1,1), expectedTimes);
    expectedTimes.add(1);
    assertEquals(time.getBestTimes(2,1), expectedTimes);
    for (int i = 0; i < 4; i++) {
      vectorList.add(new TestVector(new double[]{1,0}));
      vectorList.add(new TestVector(new double[]{0,1}));
    };
    assertEquals(time.getBestTimes(2,1), expectedTimes);
    expectedTimes.remove(1);
    assertEquals(time.getBestTimes(1,1), expectedTimes);
  }
  @Test
  public void testNoMult() {
    List<TestVector> vectorList = new ArrayList<>();
    vectorList.add(new TestVector(new double[]{0.5,0.8,0}));
    vectorList.add(new TestVector(new double[]{0.3,0.2,0.6}));
    vectorList.add(new TestVector(new double[]{0.3,0.7,0.6}));
    vectorList.add(new TestVector(new double[]{0.9,1,0.6}));
    TimeSelection<TestVector> time = new TimeSelection<>(vectorList);
    List<Integer> expectedTimes = new ArrayList<>();
    expectedTimes.add(1);
    assertEquals(time.getBestTimes(1,1), expectedTimes);
    expectedTimes.add(0);
    assertEquals(time.getBestTimes(2,1), expectedTimes);
    expectedTimes.add(2);
    assertEquals(time.getBestTimes(3,1), expectedTimes);
    List<Integer> expectedTimes2 = new ArrayList<>();
    expectedTimes2.add(0);
    assertEquals(time.getBestTimes(1,2), expectedTimes2);
    expectedTimes2.add(1);
    assertEquals(time.getBestTimes(2,2), expectedTimes2);
  }
  @Test
  public void testMult() {
    List<TestVector> vectorList = new ArrayList<>();
    TestVector multiplier = new TestVector(new double[]{1,0,0.5});
    vectorList.add(new TestVector(new double[]{0.5,0.8,0}));
    vectorList.add(new TestVector(new double[]{0.3,0.2,0.6}));
    vectorList.add(new TestVector(new double[]{0.3,0.7,0.6}));
    vectorList.add(new TestVector(new double[]{0.9,1,0.6}));
    TimeSelection<TestVector> time = new TimeSelection<>(vectorList, multiplier);
    List<Integer> expectedTimes = new ArrayList<>();
    expectedTimes.add(0);
    assertEquals(time.getBestTimes(1,1), expectedTimes);
    expectedTimes.add(2);
    assertEquals(time.getBestTimes(2,1), expectedTimes);
    expectedTimes.add(1);
    assertEquals(time.getBestTimes(3,1), expectedTimes);
    List<Integer> expectedTimes2 = new ArrayList<>();
    expectedTimes2.add(0);
    assertEquals(time.getBestTimes(1,2), expectedTimes2);
    expectedTimes2.add(1);
    assertEquals(time.getBestTimes(2,2), expectedTimes2);
  }
  @Test
  public void testTie() {
    List<TestVector> vectorList = new ArrayList<>();
    for (int i = 0; i < 5; i ++) {
      vectorList.add(new TestVector(new double[]{1,1,1,1,1,1,1,1,1,1}));
    }
    TimeSelection<TestVector> time = new TimeSelection<>(vectorList);
    List<Integer> expectedTimes = new ArrayList<>();
    expectedTimes.add(0);
    assertEquals(time.getBestTimes(1,1), expectedTimes);
    expectedTimes.add(9);
    assertEquals(time.getBestTimes(2,1), expectedTimes);
    expectedTimes.remove(1);
    expectedTimes.add(4);
    expectedTimes.add(8);
    assertEquals(time.getBestTimes(3,1), expectedTimes);
  }
}
