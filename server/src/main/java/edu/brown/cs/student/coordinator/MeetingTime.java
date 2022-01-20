package edu.brown.cs.student.coordinator;

/**
 * Class representing a meeting time.
 */
public class MeetingTime {
  private Integer id;
  private Integer meetingGroupId;
  private Integer startIndex;
  private Integer endIndex;
  private Integer availabilityRank;
  private String rgb;
  private Integer totalPreferenceScore;

  /**
   * Constructor.
   *
   * @param id                   id
   * @param meetingGroupId       id of meetingGroup
   * @param startIndex           start DT
   * @param endIndex             end DT
   * @param availabilityRank     rank of availability (e.g. 1, 2, 3, ..., where 1 is the "best")
   * @param totalPreferenceScore sum of all group members' preference scores for this meeting time
   * @param rgb                  display color
   */

  public MeetingTime(Integer id, Integer meetingGroupId, Integer startIndex, Integer endIndex,
                     Integer availabilityRank, String rgb, Integer totalPreferenceScore) {
    this.id = id;
    this.meetingGroupId = meetingGroupId;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.availabilityRank = availabilityRank;
    this.rgb = rgb;
    this.totalPreferenceScore = totalPreferenceScore;
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
   * Gets meetingGroupId.
   *
   * @return meetingGroupId
   */
  public Integer getMeetingGroupId() {
    return meetingGroupId;
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

  /**
   * Gets totalPreferenceScore.
   *
   * @return totalPreferenceScore
   */
  public Integer getTotalPreferenceScore() {
    return totalPreferenceScore;
  }

  /**
   * Gets rgb.
   *
   * @return rgb
   */
  public String getRgb() {
    return rgb;
  }
}
