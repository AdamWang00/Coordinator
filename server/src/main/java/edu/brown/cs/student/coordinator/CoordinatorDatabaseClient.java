package edu.brown.cs.student.coordinator;

import edu.brown.cs.student.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for a Coordinator database.
 */
public class CoordinatorDatabaseClient {
  private final Database db;
  private static final int BITS_PER_BYTE = 8;
  private static final int BYTE_SIZE = 256;

  /**
   * Constructor.
   * Creates a database connection.
   *
   * @param databasePath path to the database file.
   */
  public CoordinatorDatabaseClient(String databasePath) {
    this.db = new Database(databasePath);
  }

  /**
   * Checks whether a user exists.
   *
   * @param username username of user
   * @return true if user with given username exists, false otherwise
   */
  public boolean checkUserExists(String username) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT COUNT(username) FROM user WHERE username = ?;"
      );
      stat.setString(1, username);
      ResultSet rs = stat.executeQuery();
      rs.next();
      int numUsers = rs.getInt(1);
      stat.close();
      rs.close();
      if (numUsers == 0) {
        return false;
      } else if (numUsers == 1) {
        return true;
      } else {
        throw new RuntimeException("duplicate users found");
      }
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while checking existence of user");
    }
  }

  /**
   * Checks whether a meetingGroup exists.
   *
   * @param id id of meetingGroup
   * @return true if user with given username exists, false otherwise
   */
  public boolean checkMeetingGroupExists(int id) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT COUNT(id) FROM meetingGroup WHERE id = ?;"
      );
      stat.setInt(1, id);
      ResultSet rs = stat.executeQuery();
      rs.next();
      int numGroups = rs.getInt(1);
      stat.close();
      rs.close();
      if (numGroups == 0) {
        return false;
      } else if (numGroups == 1) {
        return true;
      } else {
        throw new RuntimeException("duplicate meetingGroups found");
      }
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while checking existence of meeting group");
    }
  }

  /**
   * Checks whether a user is the admin of a meetingGroup.
   *
   * @param meetingGroupId id of meetingGroup
   * @param username       username of user
   * @return true if the username is equal to the group adminUsername
   */
  public boolean checkMeetingGroupAdmin(int meetingGroupId, String username) {
    if (username == null || !checkUserExists(username)
        || !checkMeetingGroupExists(meetingGroupId)) {
      return false;
    }
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT adminUsername FROM meetingGroup WHERE id = ?;"
      );
      stat.setInt(1, meetingGroupId);
      ResultSet rs = stat.executeQuery();
      rs.next();
      String adminUsername = rs.getString(1);
      stat.close();
      rs.close();
      return username.equals(adminUsername);
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while checking existence of meeting group");
    }
  }

  /**
   * Finds user's hashed password using username.
   *
   * @param username username of user
   * @return hashedPassword for that user, null if user not found
   */
  public String getHashedPasswordOfUser(String username) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT hashedPassword FROM user WHERE username = ?;"
      );
      stat.setString(1, username);
      ResultSet rs = stat.executeQuery();
      if (!rs.next()) {
        return null;
      }
      String hashedPassword = rs.getString(1);
      stat.close();
      rs.close();
      return hashedPassword;
    } catch (SQLException e) {
      throw new RuntimeException("Bad SQL operation while getting user's hashed password");
    }
  }

  /**
   * Creates a new user using username, hashedPassword, and weeklySchedule.
   *
   * @param newUser        User representation of new user
   * @param hashedPassword hashed password of new user
   * @return true if new user has been created, false otherwise (e.g. if username already exists)
   */
  public boolean createUser(User newUser, String hashedPassword) {
    String username = newUser.getUsername();
    if (checkUserExists(username)) {
      return false;
    } else {
      try {
        PreparedStatement stat = this.db.createStatement(
            "INSERT INTO user (username, hashedPassword, weeklySchedule) VALUES (?, ?, ?);"
        );
        stat.setString(1, username);
        stat.setString(2, hashedPassword);
        stat.setBytes(3, toByteArray(newUser.getWeeklySchedule()));
        stat.executeUpdate();
        stat.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while creating new user");
      }
    }
  }

  /**
   * Gets a user.
   *
   * @param username         username of user
   * @param getMeetingGroups true if getting user's meetingGroups (admin or member), false
   *                         otherwise (User's meetingGroups field will be null)
   * @return User representation of user if user exists, null otherwise
   */
  public User getUser(String username, boolean getMeetingGroups) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT weeklySchedule FROM user WHERE username = ?;"
      );
      stat.setString(1, username);
      ResultSet rs = stat.executeQuery();
      if (!rs.next()) { // User does not exist
        stat.close();
        rs.close();
        return null;
      } else { // User exists
        byte[] weeklyScheduleBytes = rs.getBytes(1);
        stat.close();
        rs.close();

        List<MeetingGroup> meetingGroups = null;
        if (getMeetingGroups) {
          // get User's MeetingGroups (admin or member)
          meetingGroups = new ArrayList<>();
          stat = this.db.createStatement(
              "SELECT *, CASE WHEN meetingGroup.id IN "
                  + "(SELECT meetingGroupId FROM meetingGroupMembership WHERE username = ?) "
                  + "THEN TRUE ELSE FALSE END AS isMember "
                  + "FROM meetingGroup WHERE meetingGroup.id IN "
                  + "(SELECT meetingGroupId FROM meetingGroupMembership WHERE username = ?) "
                  + "OR meetingGroup.adminUsername = ?;"
          );
          stat.setString(1, username);
          stat.setString(2, username);
          stat.setString(3, username);
          rs = stat.executeQuery();
          while (rs.next()) {
            int id = rs.getInt(1);
            String groupName = rs.getString(2);
            String adminUsername = rs.getString(3);
            Integer parentMeetingGroupId = rs.getInt(4);
            if (parentMeetingGroupId.equals(0)) {
              parentMeetingGroupId = null;
            }
            Integer highlightedMeetingTimeId = rs.getInt(5);
            if (highlightedMeetingTimeId.equals(0)) {
              highlightedMeetingTimeId = null;
            }
            int currentStateEnum = rs.getInt(6);
            int meetingDurationMinutes = rs.getInt(6 + 1);
            int subgroupSize = rs.getInt(6 + 2);
            String rgb = rs.getString(6 + 3);
            boolean isMember = rs.getBoolean(6 + 4);

            MeetingGroup.MeetingGroupState currentState = MeetingGroup.intToMeetingGroupState(
                currentStateEnum);

            MeetingGroup meetingGroup = new MeetingGroup(id, groupName, adminUsername,
                parentMeetingGroupId, highlightedMeetingTimeId, currentState,
                meetingDurationMinutes, subgroupSize, rgb);

            // set whether user is member
            meetingGroup.setIsMember(isMember);

            if (currentState != MeetingGroup.MeetingGroupState.OPEN) {
              // get MeetingGroup's MeetingTimes
              PreparedStatement stat1 = this.db.createStatement(
                  "SELECT meetingTime.*, SUM(meetingTimePreference.preferenceScore) "
                      + "FROM meetingTime "
                      + "LEFT JOIN meetingTimePreference "
                      + "ON meetingTime.id = meetingTimePreference.meetingTimeId "
                      + "WHERE meetingTime.meetingGroupId = ? "
                      + "GROUP BY meetingTime.id "
                      + "ORDER BY meetingTime.availabilityRank "
                      + "LIMIT 5;"
              );
              stat1.setInt(1, id);
              ResultSet rs1 = stat1.executeQuery();
              while (rs1.next()) {
                meetingGroup.addMeetingTime(new MeetingTime(
                    rs1.getInt(1),
                    rs1.getInt(2),
                    rs1.getInt(3),
                    rs1.getInt(4),
                    rs1.getInt(5),
                    rs1.getString(6),
                    rs1.getInt(6 + 1)
                ));
              }
              stat1.close();
              rs1.close();
            }

            // fetch meetingGroup members
            PreparedStatement stat2 = this.db.createStatement(
                "SELECT username FROM meetingGroupMembership WHERE meetingGroupId = ?;");
            stat2.setInt(1, meetingGroup.getId());
            ResultSet rs2 = stat2.executeQuery();
            while (rs2.next()) {
              meetingGroup.addMember(rs2.getString(1));
            }
            stat2.close();
            rs2.close();

            meetingGroups.add(meetingGroup);
          }
          stat.close();
          rs.close();
        }
        return new User(username, toBoolArray(weeklyScheduleBytes), meetingGroups);
      }
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while getting user");
    }
  }

  /**
   * Updates an existing user's weeklySchedule. To avoid updating a property, set it to null.
   *
   * @param updatedUser User representation of existing user with updated properties
   * @return true if existing user has been updated, false otherwise (e.g. if user doesn't exist)
   */
  public boolean updateUser(User updatedUser) {
    String username = updatedUser.getUsername();
    if (!checkUserExists(username)) {
      return false;
    } else {
      try {
        PreparedStatement stat = this.db.createStatement(
            "UPDATE user SET weeklySchedule = COALESCE(?, weeklySchedule) "
                + "WHERE username = ?;"
        );
        if (updatedUser.getWeeklySchedule() == null) {
          stat.setNull(1, Types.BLOB);
        } else {
          stat.setBytes(1, toByteArray(updatedUser.getWeeklySchedule()));
        }
        stat.setString(2, username);
        stat.executeUpdate();
        stat.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while updating user");
      }
    }
  }

  /**
   * Creates a new meetingGroup using all properties except id (which is automatically determined).
   *
   * @param newGroup MeetingGroup representation of new meetingGroup
   * @return id of new meetingGroup
   */
  public int createMeetingGroup(MeetingGroup newGroup) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "INSERT INTO meetingGroup (name, adminUsername, parentMeetingGroupId, "
              + "highlightedMeetingTimeId, currentStateEnum, meetingDurationMinutes, "
              + "subgroupSize, rgb) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
      );
      stat.setString(1, newGroup.getName());
      if (newGroup.getAdminUsername() == null) {
        stat.setNull(2, Types.VARCHAR);
      } else {
        stat.setString(2, newGroup.getAdminUsername());
      }
      if (newGroup.getParentMeetingGroupId() == null) {
        stat.setNull(3, Types.INTEGER);
      } else {
        stat.setInt(3, newGroup.getParentMeetingGroupId());
      }
      if (newGroup.getHighlightedMeetingTimeId() == null) {
        stat.setNull(4, Types.INTEGER);
      } else {
        stat.setInt(4, newGroup.getHighlightedMeetingTimeId());
      }
      stat.setInt(5, MeetingGroup.meetingGroupStateToInt(newGroup.getCurrentState()));
      stat.setInt(6, newGroup.getMeetingDurationMinutes());
      stat.setInt(6 + 1, newGroup.getSubgroupSize());
      stat.setString(6 + 2, newGroup.getRgb());
      stat.executeUpdate();
      stat.close();

      stat = this.db.createStatement("SELECT last_insert_rowid();");
      ResultSet rs = stat.executeQuery();
      rs.next();
      int id = rs.getInt(1);
      stat.close();
      rs.close();
      return id;
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while creating new meeting group");
    }
  }

  /**
   * Gets a meetingGroup, not including the meetingTimes field in MeetingGroup.
   *
   * @param meetingGroupId id of meetingGroup
   * @return a MeetingGroup, or null if no group with id exists
   */
  public MeetingGroup getMeetingGroup(int meetingGroupId) {
    try {
      PreparedStatement stat = this.db.createStatement(
          "SELECT * FROM meetingGroup WHERE id = ?;"
      );
      stat.setInt(1, meetingGroupId);
      ResultSet rs = stat.executeQuery();
      if (!rs.next()) {
        stat.close();
        rs.close();
        return null;
      }
      int id = rs.getInt(1);
      String groupName = rs.getString(2);
      String adminUsername = rs.getString(3);
      Integer parentMeetingGroupId = rs.getInt(4);
      if (parentMeetingGroupId.equals(0)) {
        parentMeetingGroupId = null;
      }
      Integer highlightedMeetingTimeId = rs.getInt(5);
      if (highlightedMeetingTimeId.equals(0)) {
        highlightedMeetingTimeId = null;
      }
      int currentStateEnum = rs.getInt(6);
      int meetingDurationMinutes = rs.getInt(6 + 1);
      int subgroupSize = rs.getInt(6 + 2);
      String rgb = rs.getString(6 + 3);
      MeetingGroup.MeetingGroupState currentState = MeetingGroup.intToMeetingGroupState(
          currentStateEnum);
      MeetingGroup meetingGroup = new MeetingGroup(id, groupName, adminUsername,
          parentMeetingGroupId, highlightedMeetingTimeId, currentState, meetingDurationMinutes,
          subgroupSize, rgb);
      stat.close();
      rs.close();
      return meetingGroup;
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while getting meeting group");
    }
  }

  /**
   * Update existing meetingGroup using all properties except id, adminUsername, and
   * parentMeetingGroupId. To avoid updating a property, set it to null. Note that adminUsername
   * cannot be null and must match the admin's username.
   *
   * @param updatedGroup MeetingGroup representation of updated meetingGroup
   * @return true if meetingGroup has been updated, false otherwise (e.g. if MeetingGroup's id DNE)
   */
  public boolean updateMeetingGroup(MeetingGroup updatedGroup) {
    int id = updatedGroup.getId();
    if (!checkMeetingGroupAdmin(id, updatedGroup.getAdminUsername())) {
      return false;
    } else {
      try {
        PreparedStatement stat = this.db.createStatement(
            "UPDATE meetingGroup "
                + "SET name = COALESCE(?, name), "
                + "highlightedMeetingTimeId = COALESCE(?, highlightedMeetingTimeId), "
                + "currentStateEnum = COALESCE(?, currentStateEnum), "
                + "meetingDurationMinutes = COALESCE(?, meetingDurationMinutes), "
                + "subgroupSize = COALESCE(?, subgroupSize), "
                + "rgb = COALESCE(?, rgb) "
                + "WHERE id = ?;"
        );
        MeetingGroup.MeetingGroupState currentState = updatedGroup.getCurrentState();

        if (updatedGroup.getName() == null) {
          stat.setNull(1, Types.VARCHAR);
        } else {
          stat.setString(1, updatedGroup.getName());
        }
        if (updatedGroup.getHighlightedMeetingTimeId() == null) {
          stat.setNull(2, Types.INTEGER);
        } else {
          stat.setInt(2, updatedGroup.getHighlightedMeetingTimeId());
        }
        if (currentState == null) {
          stat.setNull(3, Types.INTEGER);
        } else {
          stat.setInt(3, MeetingGroup.meetingGroupStateToInt(currentState));
        }
        if (updatedGroup.getMeetingDurationMinutes() == null) {
          stat.setNull(4, Types.INTEGER);
        } else {
          stat.setInt(4, updatedGroup.getMeetingDurationMinutes());
        }
        if (updatedGroup.getSubgroupSize() == null) {
          stat.setNull(5, Types.INTEGER);
        } else {
          stat.setInt(5, updatedGroup.getSubgroupSize());
        }
        if (updatedGroup.getRgb() == null) {
          stat.setNull(6, Types.VARCHAR);
        } else {
          stat.setString(6, updatedGroup.getRgb());
        }
        stat.setInt(6 + 1, id);
        stat.executeUpdate();
        stat.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while updating meeting group");
      }
    }
  }

  /**
   * Deletes a meetingGroup.
   *
   * @param meetingGroupId id of meetingGroup to delete
   * @param adminUsername  username of admin (i.e. user attempting to make this deletion)
   * @return true if deletion query is executed, false otherwise (e.g. if user is not admin)
   */
  public boolean deleteMeetingGroup(int meetingGroupId, String adminUsername) {
    try {
      if (!checkMeetingGroupAdmin(meetingGroupId, adminUsername)) {
        return false;
      }
      PreparedStatement stat = this.db.createStatement(
          "DELETE FROM meetingGroup WHERE id = ?;"
      );
      stat.setInt(1, meetingGroupId);
      stat.executeUpdate();
      stat.close();
      return true;
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while getting meeting group");
    }
  }

  /**
   * Creates a new meeting group membership.
   *
   * @param username       username of user
   * @param meetingGroupId id of meetingGroup
   * @param ignoreState    if true, add to meetingGroup, regardless of its state (e.g. open)
   * @return true if membership is created or already exists, false otherwise (e.g. group is locked)
   */
  public boolean createMeetingGroupMembership(String username, int meetingGroupId,
                                              boolean ignoreState) {
    if (checkUserExists(username) && checkMeetingGroupExists(meetingGroupId)) {
      try {
        PreparedStatement stat = this.db.createStatement(
            "SELECT currentStateEnum FROM meetingGroup WHERE id = ?;"
        );
        stat.setInt(1, meetingGroupId);
        ResultSet rs = stat.executeQuery();
        rs.next();
        int currentStateEnum = rs.getInt(1);
        stat.close();
        rs.close();
        if (!ignoreState && currentStateEnum != 1) { // meetingGroup state is not OPEN.
          return false;
        }

        stat = this.db.createStatement(
            "INSERT OR IGNORE INTO meetingGroupMembership (username, meetingGroupId) "
                + "VALUES (?, ?);"
        );
        stat.setString(1, username);
        stat.setInt(2, meetingGroupId);
        stat.executeUpdate();
        stat.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while creating meeting group membership");
      }
    } else {
      return false;
    }
  }

  /**
   * Deletes meeting group memberships of a user for a given group, its subgroups (if any), and
   * its parent group (if exists).
   *
   * @param username       username of user
   * @param meetingGroupId id of meetingGroup
   * @return true if deletion query is executed, false otherwise (e.g. user or group does not exist)
   */
  public boolean deleteMeetingGroupMembership(String username, int meetingGroupId) {
    if (checkUserExists(username) && checkMeetingGroupExists(meetingGroupId)) {
      try {
        PreparedStatement stat = this.db.createStatement(
            "DELETE FROM meetingGroupMembership WHERE username = ? "
                + "AND (meetingGroupId = ? OR meetingGroupId IN "
                + "(SELECT id FROM meetingGroup WHERE parentMeetingGroupId = ? "
                + "UNION SELECT parentMeetingGroupId FROM meetingGroup WHERE id = ?));"
        );
        stat.setString(1, username);
        stat.setInt(2, meetingGroupId);
        stat.setInt(3, meetingGroupId);
        stat.setInt(4, meetingGroupId);
        stat.executeUpdate();
        stat.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while deleting meeting group membership");
      }
    } else {
      return false;
    }
  }

  /**
   * Gets all (User) members of a meeting group.
   *
   * @param meetingGroupId id of meetingGroup
   * @return List of Users in meeting group, without the meetingGroups field, or null if the
   * meetingGroup does not exist
   */
  public List<User> getMeetingGroupMembers(int meetingGroupId) {
    if (!checkMeetingGroupExists(meetingGroupId)) {
      return null;
    }
    try {
      List<User> users = new ArrayList<>();
      PreparedStatement stat = this.db.createStatement(
          "SELECT user.username, user.weeklySchedule FROM meetingGroupMembership "
              + "INNER JOIN user ON user.username = meetingGroupMembership.username "
              + "WHERE meetingGroupMembership.meetingGroupId = ?;"
      );
      stat.setInt(1, meetingGroupId);
      ResultSet rs = stat.executeQuery();
      while (rs.next()) {
        users.add(new User(rs.getString(1), toBoolArray(rs.getBytes(2)), null));
      }
      stat.close();
      rs.close();
      return users;
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while getting meeting group members");
    }
  }

  /**
   * Creates a meeting time and its associated meeting time preferences.
   *
   * @param meetingTime MeetingTime representation (ignores id and totalPreferenceScore)
   * @return true if meeting time is created, false otherwise (e.g. meetingGroup is not locked)
   */
  public boolean createMeetingTimeAndPreferences(MeetingTime meetingTime) {
    int meetingGroupId = meetingTime.getMeetingGroupId();
    if (checkMeetingGroupExists(meetingGroupId)) {
      try {
        PreparedStatement stat = this.db.createStatement(
            "SELECT currentStateEnum FROM meetingGroup WHERE id = ?;"
        );
        stat.setInt(1, meetingGroupId);
        ResultSet rs = stat.executeQuery();
        rs.next();
        int currentStateEnum = rs.getInt(1);
        stat.close();
        rs.close();
        if (currentStateEnum != 2) { // meetingGroup state is not LOCKED.
          return false;
        }

        // Create meetingTime.
        stat = this.db.createStatement(
            "INSERT INTO meetingTime (meetingGroupId, startIndex, endIndex, "
                + "availabilityRank, rgb) VALUES (?, ?, ?, ?, ?);"
        );
        stat.setInt(1, meetingGroupId);
        stat.setInt(2, meetingTime.getStartIndex());
        stat.setInt(3, meetingTime.getEndIndex());
        stat.setInt(4, meetingTime.getAvailabilityRank());
        stat.setString(5, meetingTime.getRgb());
        stat.executeUpdate();
        stat.close();

        stat = this.db.createStatement("SELECT last_insert_rowid();");
        rs = stat.executeQuery();
        rs.next();
        int meetingTimeId = rs.getInt(1);
        stat.close();
        rs.close();

        // Create meetingTimePreference for each user in meetingGroup.
        stat = this.db.createStatement(
            "SELECT username FROM meetingGroupMembership WHERE meetingGroupId = ?;"
        );
        stat.setInt(1, meetingGroupId);
        rs = stat.executeQuery();
        while (rs.next()) {
          PreparedStatement stat1 = this.db.createStatement(
              "INSERT OR IGNORE INTO meetingTimePreference (username, meetingTimeId) "
                  + "VALUES (?, ?);"
          );
          stat1.setString(1, rs.getString(1));
          stat1.setInt(2, meetingTimeId);
          stat1.executeUpdate();
          stat1.close();
        }
        stat.close();
        rs.close();
        return true;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while creating meeting time/preferences");
      }
    } else {
      return false;
    }
  }

  /**
   * Gets the meeting time preferences for the meeting times of a group that the user is in.
   *
   * @param username       username of user
   * @param meetingGroupId id of meetingGroup
   * @return List of meeting time preferences, ordered by availabilityRank
   */
  public List<MeetingTimePreference> getMeetingTimePreferences(String username,
                                                               int meetingGroupId) {
    if (checkUserExists(username) && checkMeetingGroupExists(meetingGroupId)) {
      try {
        List<MeetingTimePreference> preferences = new ArrayList<>();
        PreparedStatement stat = this.db.createStatement(
            "SELECT meetingTimePreference.*, meetingTime.* "
                + "FROM meetingTimePreference "
                + "INNER JOIN meetingTime "
                + "ON meetingTimePreference.meetingTimeId = meetingTime.id "
                + "WHERE meetingTimePreference.username = ? "
                + "AND meetingTime.meetingGroupId = ? "
                + "ORDER BY meetingTime.availabilityRank "
                + "LIMIT 5;"
        );
        stat.setString(1, username);
        stat.setInt(2, meetingGroupId);
        ResultSet rs = stat.executeQuery();
        while (rs.next()) {
          preferences.add(new MeetingTimePreference(rs.getString(1), rs.getInt(2), rs.getInt(3),
              rs.getInt(4) > 0, rs.getInt(6 + 1), rs.getInt(6 + 2), rs.getInt(6 + 3)));
        }
        stat.close();
        rs.close();
        return preferences;
      } catch (SQLException e) {
        throw new RuntimeException("bad SQL operation while getting meeting time preference");
      }
    } else {
      return null;
    }
  }

  /**
   * Updates the meeting time preference of a user. username and meetingTimeId fields are required.
   * To avoid updating a property, set it to null.
   *
   * @param updatedPreference MeetingTimePreference representation of updated preference
   * @return true if meeting time preference is updated, false otherwise (e.g. if preference DNE)
   */
  public boolean updateMeetingTimePreference(MeetingTimePreference updatedPreference) {
    try {
      String username = updatedPreference.getUsername();
      Integer meetingTimeId = updatedPreference.getMeetingTimeId();
      if (username == null || meetingTimeId == null) {
        return false;
      }
      PreparedStatement stat = this.db.createStatement(
          "SELECT * FROM meetingTimePreference WHERE username = ? AND meetingTimeId = ?;"
      );
      stat.setString(1, username);
      stat.setInt(2, meetingTimeId);
      ResultSet rs = stat.executeQuery();
      boolean preferenceExists = rs.next();
      stat.close();
      rs.close();
      if (!preferenceExists) {
        return false;
      }

      stat = this.db.createStatement(
          "UPDATE meetingTimePreference SET preferenceScore = COALESCE(?, preferenceScore), "
              + "isPinned = COALESCE(?, isPinned) WHERE username = ? AND meetingTimeId = ?;"
      );
      if (updatedPreference.getPreferenceScore() == null) {
        stat.setNull(1, Types.INTEGER);
      } else {
        stat.setInt(1, updatedPreference.getPreferenceScore());
      }
      if (updatedPreference.getIsPinned() == null) {
        stat.setNull(2, Types.INTEGER);
      } else {
        stat.setInt(2, updatedPreference.getIsPinned() ? 1 : 0);
      }
      stat.setString(3, username);
      stat.setInt(4, meetingTimeId);
      stat.executeUpdate();
      stat.close();
      return true;
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while updating meeting time preference");
    }
  }

  /**
   * Resets tables by deleting all rows.
   */
  public void resetTables() {
    try {
      PreparedStatement stat = this.db.createStatement("DELETE FROM meetingTimePreference;");
      stat.executeUpdate();
      stat.close();
      stat = this.db.createStatement("DELETE FROM meetingTime;");
      stat.executeUpdate();
      stat.close();
      stat = this.db.createStatement("DELETE FROM meetingGroupMembership;");
      stat.executeUpdate();
      stat.close();
      stat = this.db.createStatement("DELETE FROM meetingGroup;");
      stat.executeUpdate();
      stat.close();
      stat = this.db.createStatement("DELETE FROM scheduleEvent;");
      stat.executeUpdate();
      stat.close();
      stat = this.db.createStatement("DELETE FROM user;");
      stat.executeUpdate();
      stat.close();
    } catch (SQLException e) {
      throw new RuntimeException("bad SQL operation while resetting tables");
    }
  }

  /**
   * Closes the database connection (call on cleanup).
   */
  public void close() {
    this.db.close();
  }

  /**
   * Converts byte array to boolean array, where bytes are converted to bits, and then to booleans.
   *
   * @param byteArray byte array to convert
   * @return boolean array that is 8 times the length of byteArray, or null if input is null
   */
  private static boolean[] toBoolArray(byte[] byteArray) {
    if (byteArray == null) {
      return null;
    }
    boolean[] boolArray = new boolean[byteArray.length * BITS_PER_BYTE];
    for (int i = 0; i < byteArray.length; i++) {
      for (int bit = 0; bit < BITS_PER_BYTE; bit++) {
        boolArray[i * BITS_PER_BYTE + bit] = (byteArray[i] & (BYTE_SIZE >> (bit + 1))) != 0;
      }
    }
    return boolArray;
  }

  /**
   * Converts boolean array to byte array, where booleans are converted to bits, and then to bytes.
   *
   * @param boolArray boolean array to convert
   * @return byte array that is 1/8 times the length of boolArray (undefined behavior for
   * boolArrays with lengths that are not a multiple of 8), or null if input is null
   */
  private static byte[] toByteArray(boolean[] boolArray) {
    if (boolArray == null) {
      return null;
    }
    byte[] byteArray = new byte[boolArray.length / BITS_PER_BYTE];
    for (int i = 0; i < byteArray.length; i++) {
      for (int bit = 0; bit < BITS_PER_BYTE; bit++) {
        if (boolArray[i * BITS_PER_BYTE + bit]) {
          byteArray[i] |= BYTE_SIZE >> (bit + 1);
        }
      }
    }
    return byteArray;
  }
}
