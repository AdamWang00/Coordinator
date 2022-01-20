package edu.brown.cs.student.coordinator;

import edu.brown.cs.student.kmeans.KMeansGroups;
import edu.brown.cs.student.kmeans.TimeSelection;

import java.util.List;

/**
 * Class containing all core Coordinator functionalities.
 */
public final class Coordinator {
  private static final int MINUTES_PER_SCHEDULE_BLOCK = 15;
  private static CoordinatorDatabaseClient dbClient = null;
  private static String currentDatabasePath = null;

  /**
   * Constructor.
   */
  private Coordinator() {
  }

  /**
   * Connects to a database through a new dbClient.
   *
   * @param databasePath the path to the coordinator database file
   */
  public static void connectCoordinatorDatabase(String databasePath) {
    if (databasePath.equals(currentDatabasePath)) {
      // To prevent trying to access self-locked database
      return;
    }
    // use temporary variables so that if an exception is thrown, we can roll back
    CoordinatorDatabaseClient tempDbClient = new CoordinatorDatabaseClient(databasePath);

    if (dbClient != null) {
      dbClient.close(); // close previous database connection
    }
    dbClient = tempDbClient;
    currentDatabasePath = databasePath;
  }

  /**
   * Get hashed password of user.
   *
   * @param username of user
   * @return hashed password
   * @throws RuntimeException if error occurred during db interaction
   */
  public static String getHashedPassword(String username) throws RuntimeException {
    return dbClient.getHashedPasswordOfUser(username);
  }

  /**
   * Insert user into db.
   *
   * @param username       of user
   * @param hashedPassword of user
   * @return boolean indicating if user was created. False if not unique username.
   * @throws RuntimeException if error occurred during db interaction
   */
  public static boolean createUser(String username, String hashedPassword)
      throws RuntimeException {
    User user = new User(username, User.getDefaultSchedule(), null);
    return dbClient.createUser(user, hashedPassword);
  }

  /**
   * Retrieve weekly schedule of user.
   *
   * @param username of user
   * @return weeklySchedule
   */
  public static boolean[] getWeeklySchedule(String username) {
    return dbClient.getUser(username, false).getWeeklySchedule();
  }

  /**
   * Get groups of a user.
   *
   * @param username of user
   * @return user object with user groups in it
   */
  public static User getUserGroups(String username) {
    return dbClient.getUser(username, true);
  }

  /**
   * Update weekly schedule.
   *
   * @param username    of user
   * @param newSchedule to update
   * @return boolean indicating whether user has been updated successfully
   */
  public static boolean updateWeeklySchedule(String username, boolean[] newSchedule) {
    User u = dbClient.getUser(username, false);
    u.updateWeeklySchedule(newSchedule);
    return dbClient.updateUser(u);
  }

  /**
   * Create group.
   *
   * @param name            of group
   * @param adminUsername   of group
   * @param meetingDuration of group
   * @param subgroupSize    of group
   * @param rgb             of group
   * @return join code of group
   */
  public static String createGroup(String name, String adminUsername, int meetingDuration,
                                   int subgroupSize, String rgb) {
    MeetingGroup mg =
        new MeetingGroup(null, name, adminUsername, null, null, MeetingGroup.MeetingGroupState.OPEN,
            meetingDuration, subgroupSize, rgb);

    int id = dbClient.createMeetingGroup(mg);
    return MeetingGroup.idToJoinCode(id);
  }

  /**
   * Add user to group, if it is open.
   *
   * @param username of user
   * @param groupId  of group to join
   * @return boolean indicating success
   */
  public static boolean joinGroup(String username, int groupId) {
    return dbClient.createMeetingGroupMembership(username, groupId, false);
  }

  /**
   * Removes a user from a given group, its subgroups (if any), and its parent group (if exists).
   *
   * @param username of user
   * @param groupId  of group to leave
   * @return boolean indicating success
   */
  public static boolean leaveGroup(String username, int groupId) {
    return dbClient.deleteMeetingGroupMembership(username, groupId);
  }

  /**
   * Get time preferences of a user.
   *
   * @param username of user
   * @param groupId  of group to join
   * @return preferences
   */
  public static List<MeetingTimePreference> getPreferences(String username, int groupId) {
    List<MeetingTimePreference> prefs = dbClient.getMeetingTimePreferences(username, groupId);
    return prefs;
  }

  /**
   * Lock group.
   *
   * @param groupId of group to lock
   * @param adminUsername username of admin (i.e. user attempting to lock the group)
   * @return boolean indicating success
   */
  public static boolean lockGroup(int groupId, String adminUsername) {
    // fetch group settings
    MeetingGroup mg = dbClient.getMeetingGroup(groupId);
    if (!adminUsername.equals(mg.getAdminUsername())) { // user is not the admin
      return false;
    }
    Integer subgroupSize = mg.getSubgroupSize();
    Integer meetingDuration = mg.getMeetingDurationMinutes();

    // get users
    List<User> users = dbClient.getMeetingGroupMembers(groupId);

    // change state of grp
    mg.setState(MeetingGroup.MeetingGroupState.LOCKED);
    dbClient.updateMeetingGroup(mg);

    if (subgroupSize == null || subgroupSize == 0) {
      // find common times
      System.out.println("Finding common times...");
      TimeSelection<User> ts = new TimeSelection<>(users);
      int blockSize = meetingDuration / MINUTES_PER_SCHEDULE_BLOCK;
      List<Integer> bestTimes = ts.getBestTimes(5, blockSize);

      // convert times into strings
      for (int i = 0; i < bestTimes.size(); i++) {
        // Both start and end are inclusive
        Integer start = bestTimes.get(i);
        Integer end = start + blockSize - 1;
        String rgb = "#FFFFFF";

        MeetingTime mt = new MeetingTime(null,
            mg.getId(),
            start,
            end,
            i,
            rgb,
            null
        );

        // add meeting time to db
        dbClient.createMeetingTimeAndPreferences(mt);
      }
    } else {
      // split groups
      System.out.println("Splitting group...");
      KMeansGroups<User> kmg = new KMeansGroups<>(subgroupSize, users);
      List<List<User>> groups = kmg.getGroups();

      // insert each grp into db
      for (int i = 0; i < groups.size(); i++) {
        List<User> g = groups.get(i);

        // create group
        String newGroupName = mg.getName() + " (subgroup " + (i + 1) + ")";
        int newGroupId = dbClient.createMeetingGroup(
            new MeetingGroup(null, newGroupName, mg.getAdminUsername(), groupId, null,
                MeetingGroup.MeetingGroupState.LOCKED, mg.getMeetingDurationMinutes(), 0,
                mg.getRgb()));

        // insert users into group
        for (User u : g) {
          dbClient.createMeetingGroupMembership(u.getUsername(), newGroupId, true);
        }

        // generate timings for grp
        TimeSelection<User> ts = new TimeSelection<>(g);
        int blockSize = meetingDuration / MINUTES_PER_SCHEDULE_BLOCK;
        List<Integer> bestTimes = ts.getBestTimes(5, blockSize);
        // convert times into strings
        for (int j = 0; j < bestTimes.size(); j++) {
          // Both start and end are inclusive
          Integer start = bestTimes.get(j);
          Integer end = start + blockSize - 1;
          String rgb = "#FFFFFF";

          MeetingTime mt = new MeetingTime(null,
              newGroupId,
              start,
              end,
              j,
              rgb,
              null
          );

          // add meeting time to db
          dbClient.createMeetingTimeAndPreferences(mt);
        }
      }
    }
    return true;
  }

  /**
   * Update group.
   *
   * @param id              of group
   * @param name            of group
   * @param adminUsername   of group (note: this cannot be null and must match the admin's username)
   * @param meetingDuration of group
   * @param subGroupSize    of group
   * @param rgb             of group
   * @return boolean indicating success
   */
  public static boolean updateGroup(int id, String name, String adminUsername, int meetingDuration,
                                    int subGroupSize, String rgb) {
    MeetingGroup mg = new MeetingGroup(id, name, adminUsername, null, null, null, meetingDuration,
        subGroupSize, rgb);

    return dbClient.updateMeetingGroup(mg);
  }

  /**
   * Delete group.
   *
   * @param groupId       id of group
   * @param adminUsername username of admin (i.e. user attempting to make this deletion)
   * @return boolean indicating success
   */
  public static boolean deleteGroup(int groupId, String adminUsername) {
    return dbClient.deleteMeetingGroup(groupId, adminUsername);
  }

  /**
   * Update preference score.
   *
   * @param username      of user
   * @param meetingTimeId of meeting time to update
   * @param updatedScore  of meeting time
   * @return boolean indicating success
   */
  public static boolean updatePreferenceScore(String username, int meetingTimeId,
                                              int updatedScore) {
    return dbClient.updateMeetingTimePreference(
        new MeetingTimePreference(username, meetingTimeId, updatedScore, null, null, null, null));
  }

  /**
   * Update preference pinned status.
   *
   * @param username      of user
   * @param meetingTimeId of meeting time to update
   * @param isPinned      of meeting time
   * @return boolean indicating success
   */
  public static boolean updatePreferenceIsPinned(String username, int meetingTimeId,
                                              boolean isPinned) {
    return dbClient.updateMeetingTimePreference(
        new MeetingTimePreference(username, meetingTimeId, null, isPinned, null, null, null));
  }

  /**
   * Resets all tables of the database.
   */
  public static void resetDb() {
    dbClient.resetTables();
  }

  /**
   * Closes the database connection and resets Coordinator (call on cleanup).
   */
  public static void closeAndReset() {
    if (dbClient != null) {
      dbClient.close();
    }
    dbClient = null;
    currentDatabasePath = null;
  }
}
