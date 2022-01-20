package edu.brown.cs.student.coordinator;

/**
 * Class representing a user's preference for a meeting time.
 */
public class MeetingTimePreference {
  private String username;
  private Integer meetingTimeId;
  private Integer preferenceScore;
  private Boolean isPinned;
  // From meetingTime:
  private Integer startIndex;
  private Integer endIndex;
  private Integer availabilityRank;

  /**
   * Constructor.
   *
   * @param username         username
   * @param meetingTimeId    id of meeting time
   * @param preferenceScore  user's preference for this meeting time (higher is better)
   * @param isPinned         whether the user has pinned this meeting time
   * @param startIndex       startIndex of meeting time
   * @param endIndex         endIndex of meeting time
   * @param availabilityRank availabilityRank of meeting time
   */
  public MeetingTimePreference(String username, Integer meetingTimeId, Integer preferenceScore,
                               Boolean isPinned, Integer startIndex, Integer endIndex,
                               Integer availabilityRank) {
    this.username = username;
    this.meetingTimeId = meetingTimeId;
    this.preferenceScore = preferenceScore;
    this.isPinned = isPinned;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.availabilityRank = availabilityRank;
  }

  /**
   * Gets username.
   *
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets meetingTimeId.
   *
   * @return meetingTimeId
   */
  public Integer getMeetingTimeId() {
    return meetingTimeId;
  }

  /**
   * Gets preferenceScore.
   *
   * @return preferenceScore
   */
  public Integer getPreferenceScore() {
    return preferenceScore;
  }

  /**
   * Gets isPinned.
   *
   * @return isPinned
   */
  public Boolean getIsPinned() {
    return isPinned;
  }

  /**
   * Gets startIndex.
   *
   * @return startIndex
   */
  public Integer getStartIndex() {
    return startIndex;
  }

  /**
   * Gets endIndex.
   *
   * @return endIndex
   */
  public Integer getEndIndex() {
    return endIndex;
  }

  /**
   * Gets availabilityRank.
   *
   * @return availabilityRank
   */
  public Integer getAvailabilityRank() {
    return availabilityRank;
  }
}
