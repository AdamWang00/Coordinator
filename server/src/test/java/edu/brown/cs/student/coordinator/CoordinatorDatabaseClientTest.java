package edu.brown.cs.student.coordinator;

import edu.brown.cs.student.ITest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests Coordinator.
 */
public class CoordinatorDatabaseClientTest implements ITest {
  private CoordinatorDatabaseClient db;

  @Override
  public void setUp() {
    this.db = new CoordinatorDatabaseClient("data/test.sqlite3");
    this.db.resetTables();
  }

  @Override
  public void tearDown() {
    this.db.resetTables();
    this.db.close();
    this.db = null;
  }

  /**
   * Tests user-related methods
   */
  @Test
  public void testUser() {
    this.setUp();

    User user = new User("adam", User.getDefaultSchedule(), null);
    assertEquals(User.WEEKLY_SCHEDULE_SIZE, user.getWeeklySchedule().length);
    assertTrue(db.createUser(user, "abc123"));
    assertTrue(db.checkUserExists("adam"));
    assertFalse(db.checkUserExists("Adam"));
    assertFalse(db.createUser(user, "abc123"));
    assertEquals("abc123", db.getHashedPasswordOfUser("adam"));
    assertNull(db.getHashedPasswordOfUser("Adam"));
    assertNull(db.getUser("Adam", false));

    user = db.getUser("adam", false);
    boolean[] schedule = user.getWeeklySchedule();
    assertEquals("adam", user.getUsername());
    assertEquals(User.WEEKLY_SCHEDULE_SIZE, user.getWeeklySchedule().length);
    for (boolean b : schedule) {
      assertFalse(b);
    }

    for (int i = 0; i < User.WEEKLY_SCHEDULE_SIZE; i++) {
      schedule[i] = Math.random() < 0.5;
    }
    assertTrue(db.updateUser(user));
    assertFalse(db.updateUser(new User("Adam", null, null)));
    user = db.getUser("adam", false);
    boolean[] schedule1 = user.getWeeklySchedule();
    assertEquals("adam", user.getUsername());
    assertEquals(User.WEEKLY_SCHEDULE_SIZE, user.getWeeklySchedule().length);
    for (int i = 0; i < User.WEEKLY_SCHEDULE_SIZE; i++) {
      assertEquals(schedule[i], schedule1[i]);
    }

    this.tearDown();
  }

  /**
   * Tests group-related methods
   */
  @Test
  public void testGroup() {
    this.setUp();

    User adam = new User("adam", User.getDefaultSchedule(), null);
    User josh = new User("josh", User.getDefaultSchedule(), null);
    assertTrue(db.createUser(adam, "abc123"));
    assertTrue(db.createUser(josh, "abc123"));

    MeetingGroup group = new MeetingGroup(null, "cs32", "adam", null, null,
        MeetingGroup.MeetingGroupState.OPEN, 60, 0, "#FFFFFF");
    assertEquals(1, db.createMeetingGroup(group));

    group = db.getMeetingGroup(1);
    assertEquals(1, (int) group.getId());
    assertEquals(1, MeetingGroup.joinCodeToId(group.getJoinCode()));
    assertEquals("cs32", group.getName());
    assertEquals("adam", group.getAdminUsername());
    assertNull(group.getParentMeetingGroupId());
    assertNull(group.getHighlightedMeetingTimeId());
    assertEquals(MeetingGroup.MeetingGroupState.OPEN, group.getCurrentState());
    assertEquals(60, (int) group.getMeetingDurationMinutes());
    assertEquals(0, (int) group.getSubgroupSize());
    assertEquals("#FFFFFF", group.getRgb());
    assertEquals(0, group.getMeetingTimes().size());

    assertTrue(db.createMeetingGroupMembership("adam", 1, false));
    assertTrue(db.createMeetingGroupMembership("josh", 1, false));

    // lock group
    group = new MeetingGroup(1, null, "josh", null, null, MeetingGroup.MeetingGroupState.LOCKED,
        null, null, null);
    assertFalse(db.updateMeetingGroup(group)); // josh is not admin
    group = new MeetingGroup(1, null, "adam", null, null, MeetingGroup.MeetingGroupState.LOCKED,
        null, null, null);
    assertTrue(db.updateMeetingGroup(group));
    assertFalse(db.createMeetingGroupMembership("step", 1, false));

    MeetingTime time = new MeetingTime(null, 1, 0, 3, 1, "#123456", null);
    assertTrue(db.createMeetingTimeAndPreferences(time));
    adam = db.getUser("adam", true);
    List<MeetingGroup> adamMeetingGroups = adam.getMeetingGroups();
    assertEquals(1, adamMeetingGroups.size());
    group = adamMeetingGroups.get(0);
    assertEquals(1, (int) group.getId());
    assertEquals(2, group.getMembers().size());
    List<MeetingTime> adamMeetingTimes = group.getMeetingTimes();
    assertEquals(1, adamMeetingTimes.size());
    time = adamMeetingTimes.get(0);
    assertEquals(1, (int) time.getMeetingGroupId());
    assertEquals(4, (int) time.getTotalPreferenceScore());

    MeetingTimePreference preference = new MeetingTimePreference("josh", time.getId(), 3, null,
        null, null, null);
    assertTrue(db.updateMeetingTimePreference(preference));
    List<MeetingTimePreference> preferences = db.getMeetingTimePreferences("josh", 1);
    assertEquals(1, preferences.size());
    assertEquals(3, (int) preferences.get(0).getPreferenceScore());
    josh = db.getUser("josh", true);
    assertEquals(5,
        (int) josh.getMeetingGroups().get(0).getMeetingTimes().get(0).getTotalPreferenceScore());

    assertFalse(db.deleteMeetingGroup(1, "josh"));
    assertTrue(db.deleteMeetingGroupMembership("josh", 1));
    assertEquals(1, db.getMeetingGroupMembers(1).size());
    assertTrue(db.deleteMeetingGroup(1, "adam"));
    assertFalse(db.deleteMeetingGroupMembership("adam", 1));

    this.tearDown();
  }
}