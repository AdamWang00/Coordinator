package edu.brown.cs.student.coordinator;

import edu.brown.cs.student.kmeans.VectorData;

import java.util.List;

/**
 * Class representing a user.
 */
public class User implements VectorData<User> {
  private String username;
  private boolean[] weeklySchedule;
  private List<MeetingGroup> meetingGroups;
  public static final int WEEKLY_SCHEDULE_SIZE = 7 * 24 * 4;

  /**
   * Constructor.
   *
   * @param username       of user
   * @param weeklySchedule of user
   * @param meetingGroups  of user
   */
  public User(String username, boolean[] weeklySchedule, List<MeetingGroup> meetingGroups) {
    this.username = username;
    this.weeklySchedule = weeklySchedule;
    this.meetingGroups = meetingGroups;
  }

  /**
   * Username getter.
   *
   * @return username
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Weekly schedule getter.
   *
   * @return weekly schedule
   */
  public boolean[] getWeeklySchedule() {
    return this.weeklySchedule;
  }

  /**
   * Meeting groups getter.
   *
   * @return meeting groups
   */
  public List<MeetingGroup> getMeetingGroups() {
    return this.meetingGroups;
  }

  /**
   * Update weekly schedule.
   *
   * @param newWeeklySchedule to update to
   */
  public void updateWeeklySchedule(boolean[] newWeeklySchedule) {
    this.weeklySchedule = newWeeklySchedule;
  }

  /**
   * Gets the default weekly schedule.
   *
   * @return an array of booleans, with each entry set to false.
   */
  public static boolean[] getDefaultSchedule() {
    return new boolean[WEEKLY_SCHEDULE_SIZE];
  }

  @Override
  public double[] getVector() {
    // convert boolean to 0s and 1s
    double[] converted = new double[WEEKLY_SCHEDULE_SIZE];
    for (int i = 0; i < WEEKLY_SCHEDULE_SIZE; i++) {
      if (weeklySchedule[i]) {
        converted[i] = 1;
      } else {
        converted[i] = 0;
      }
    }

    return converted;
  }

  @Override
  public int getLength() {
    return WEEKLY_SCHEDULE_SIZE;
  }
}
