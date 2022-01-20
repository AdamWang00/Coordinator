package edu.brown.cs.student.kmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to select the best times given schedules represented as doubles of weights.
 *
 * @param <T> - A type of object that contains a schedule represented as a double array
 */
public class TimeSelection<T extends VectorData<T>> {
  /**
   * List of data with schedules.
   */
  private final List<T> vectorList;
  /**
   * A single data whose weights will multiply all other weights. Can be used to enforce that
   * the leader must be available at all times.
   */
  private T multiplier;
  /**
   * Length of the schedules.
   */
  private final int vectorLength;

  /**
   * A constructor for a TimeSelection without a multiplier.
   *
   * @param vectorList - a list of data, each with their own schedule
   */
  public TimeSelection(List<T> vectorList) {
    this.vectorList = new ArrayList<>(vectorList);
    if (vectorList.size() == 0) {
      throw new RuntimeException();
    } else {
      this.vectorLength = vectorList.get(0).getLength();
    }
  }

  /**
   * A constructor for a TimeSelection with a multiplier.
   *
   * @param vectorList - a list of data, each with their own schedule
   * @param multiplier - a multiplier by which to multiply every schedule by
   */
  public TimeSelection(List<T> vectorList, T multiplier) {
    this.vectorList = new ArrayList<>(vectorList);
    this.multiplier = multiplier;
    if (vectorList.size() == 0) {
      throw new RuntimeException();
    } else {
      this.vectorLength = vectorList.get(0).getLength();
    }
  }

  /**
   * A method to find the best times for everyone's schedule.
   *
   * @param timeAmt   - the amount of times to find
   * @param blockSize - the length of the meeting (in 15 minute chunks)
   * @return - a list of integers, representing the indices of an array where the sums of blocks of
   * blockSize size among all schedules are highest. This represents the optimal starting times
   * for events. As a time, this should be interpreted as w/e time the 0th index represents +
   * 15 min * index.
   */
  public List<Integer> getBestTimes(int timeAmt, int blockSize) {
    if (timeAmt > vectorLength - blockSize + 1 || timeAmt < 0 || blockSize <= 0) {
      throw new RuntimeException();
    }
    List<Integer> finalStartTimes = new ArrayList<>();
    double[] timeWeights = new double[vectorList.get(0).getLength()];
    for (int i = 0; i < timeWeights.length; i++) {
      double total = 0;
      for (T vector : vectorList) {
        total += vector.getVector()[i];
      }
      if (multiplier != null) {
        total = total * multiplier.getVector()[i];
      }
      timeWeights[i] = total;
    }
    double[] blockWeights = new double[vectorList.get(0).getLength() - blockSize + 1];
    blockWeights[0] = 0;
    for (int i = 0; i < blockSize; i++) {
      blockWeights[0] += timeWeights[i];
    }
    for (int i = 1; i < blockWeights.length; i++) {
      blockWeights[i] = blockWeights[i - 1] - timeWeights[i - 1] + timeWeights[i + blockSize - 1];
    }
    for (int i = 0; i < timeAmt; i++) {
      List<Integer> maxIndices = new ArrayList<>();
      double maxValue = Double.NEGATIVE_INFINITY;
      for (int j = 0; j < blockWeights.length; j++) {
        if (blockWeights[j] > maxValue && !finalStartTimes.contains(j)) {
          maxIndices = new ArrayList<>();
          maxIndices.add(j);
          maxValue = blockWeights[j];
        } else if (blockWeights[j] == maxValue) {
          maxIndices.add(j);
        }
      }
      if (maxIndices.size() > timeAmt - finalStartTimes.size()) {
        int timesToAdd = timeAmt - finalStartTimes.size();
        if (timesToAdd == 1) {
          finalStartTimes.add(maxIndices.get(0));
        } else {
          for (int k = 0; k < timesToAdd; k++) {
            finalStartTimes.add(maxIndices.get((maxIndices.size() - 1) / (timesToAdd - 1) * k));
          }
        }
      } else {
        finalStartTimes.addAll(maxIndices);
      }
    }
    return finalStartTimes;
  }
}
