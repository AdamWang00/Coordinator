package edu.brown.cs.student.coordinator;

import org.hashids.Hashids;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a meeting group.
 */
public class MeetingGroup {
  /**
   * Enumeration of meeting group's possible states.
   */
  public enum MeetingGroupState {
    OPEN,
    LOCKED,
    ENDED
  }

  private static Hashids hashids = new Hashids("a good salt", 6, "0123456789abcdef");

  private Integer id;
  private String joinCode; // this is determined by id.
  private String name;
  private String adminUsername;
  private Integer parentMeetingGroupId;
  private Integer highlightedMeetingTimeId;
  private MeetingGroupState currentState;
  private Integer meetingDurationMinutes;
  private Integer subgroupSize;
  private String rgb;
  private List<MeetingTime> meetingTimes;
  private boolean isMember;
  private List<String> members;

  /**
   * Constructor.
   *
   * @param id                       id
   * @param name                     name
   * @param adminUsername            username of admin
   * @param parentMeetingGroupId     id of parent meeting group (if this is a subgroup)
   * @param highlightedMeetingTimeId id of highlighted meeting time
   * @param currentState             current state
   * @param meetingDurationMinutes   duration of meetings in minutes
   * @param subgroupSize             size of subgroups to be created from this group
   * @param rgb                      display color
   */
  public MeetingGroup(Integer id, String name, String adminUsername, Integer parentMeetingGroupId,
                      Integer highlightedMeetingTimeId, MeetingGroupState currentState,
                      Integer meetingDurationMinutes, Integer subgroupSize, String rgb) {
    this.id = id;
    if (id == null) {
      this.joinCode = null;
    } else {
      this.joinCode = idToJoinCode(id);
    }
    this.name = name;
    this.adminUsername = adminUsername;
    this.parentMeetingGroupId = parentMeetingGroupId;
    this.highlightedMeetingTimeId = highlightedMeetingTimeId;
    this.currentState = currentState;
    this.meetingDurationMinutes = meetingDurationMinutes;
    this.subgroupSize = subgroupSize;
    this.rgb = rgb;
    this.meetingTimes = new ArrayList<>();
    this.members = new ArrayList<>();
  }

  /**
   * Gets id.
   *
   * @return id
   */
  public Integer getId() {
    return id;
  }

  /**
   * Gets joinCode.
   *
   * @return joinCode
   */
  public String getJoinCode() {
    return joinCode;
  }

  /**
   * Gets name.
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets adminUsername.
   *
   * @return adminUsername
   */
  public String getAdminUsername() {
    return adminUsername;
  }

  /**
   * Gets parentMeetingGroupId.
   *
   * @return parentMeetingGroupId
   */
  public Integer getParentMeetingGroupId() {
    return parentMeetingGroupId;
  }

  /**
   * Gets highlightedMeetingTimeId.
   *
   * @return highlightedMeetingTimeId
   */
  public Integer getHighlightedMeetingTimeId() {
    return highlightedMeetingTimeId;
  }

  /**
   * Gets currentState.
   *
   * @return currentState
   */
  public MeetingGroupState getCurrentState() {
    return currentState;
  }

  /**
   * Gets meetingDurationMinutes.
   *
   * @return meetingDurationMinutes
   */
  public Integer getMeetingDurationMinutes() {
    return meetingDurationMinutes;
  }

  /**
   * Gets subgroupSize.
   *
   * @return subgroupSize
   */
  public Integer getSubgroupSize() {
    return subgroupSize;
  }

  /**
   * Gets meetingTimes.
   *
   * @return meetingTimes
   */
  public List<MeetingTime> getMeetingTimes() {
    return meetingTimes;
  }

  /**
   * Gets rgb.
   *
   * @return rgb
   */
  public String getRgb() {
    return rgb;
  }

  /**
   * Getter for isMember.
   *
   * @return isMember
   */
  public boolean getIsMember() {
    return isMember;
  }

  /**
   * Getter for members.
   *
   * @return members
   */
  public List<String> getMembers() {
    return members;
  }

  /**
   * Setter for isMember.
   *
   * @param b bool indicating if user is member
   */
  public void setIsMember(boolean b) {
    isMember = b;
  }

  /**
   * Adds a member to members.
   *
   * @param m member to add
   */
  public void addMember(String m) {
    this.members.add(m);
  }

  /**
   * Setter for state.
   *
   * @param newState to change to
   */
  public void setState(MeetingGroupState newState) {
    this.currentState = newState;
  }

  /**
   * Adds a MeetingTime to meetingTimes.
   *
   * @param newMeetingTime MeetingTime to add
   */
  public void addMeetingTime(MeetingTime newMeetingTime) {
    this.meetingTimes.add(newMeetingTime);
  }

  /**
   * Converts int to group state.
   *
   * @param currentStateInt int (1, 2, or 3)
   * @return corresponding MeetingGroupState
   */
  public static MeetingGroupState intToMeetingGroupState(int currentStateInt) {
    if (currentStateInt == 1) {
      return MeetingGroup.MeetingGroupState.OPEN;
    } else if (currentStateInt == 2) {
      return MeetingGroup.MeetingGroupState.LOCKED;
    } else if (currentStateInt == 3) {
      return MeetingGroup.MeetingGroupState.ENDED;
    } else {
      throw new RuntimeException("invalid input state int");
    }
  }

  /**
   * Converts group state to int.
   *
   * @param currentState a MeetingGroupState
   * @return corresponding int (1, 2, or 3)
   */
  public static int meetingGroupStateToInt(MeetingGroupState currentState) {
    if (currentState == MeetingGroup.MeetingGroupState.OPEN) {
      return 1;
    } else if (currentState == MeetingGroup.MeetingGroupState.LOCKED) {
      return 2;
    } else if (currentState == MeetingGroup.MeetingGroupState.ENDED) {
      return 3;
    } else {
      throw new RuntimeException("invalid input state");
    }
  }

  /**
   * Encodes an id as a join code.
   *
   * @param id meetingGroup id
   * @return join code
   */
  public static String idToJoinCode(int id) {
    return hashids.encode(id);
  }

  /**
   * Decodes a join code (not case-sensitive).
   *
   * @param joinCode join code
   * @return meetingGroup id
   */
  public static int joinCodeToId(String joinCode) {
    joinCode = joinCode.toLowerCase();
    return (int) hashids.decode(joinCode)[0];
  }
}
