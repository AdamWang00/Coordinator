package edu.brown.cs.student.coordinator;

import edu.brown.cs.student.ITest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests Coordinator.
 */
public class CoordinatorTest implements ITest {

  @Override
  public void setUp() {
    Coordinator.connectCoordinatorDatabase("data/test.sqlite3");
  }

  @Override
  public void tearDown() {
    Coordinator.resetDb();
    Coordinator.closeAndReset();
  }

  /**
   * Tests coordinator
   */
  @Test
  public void testCoordinator() {
    setUp();

    assertTrue(Coordinator.createUser("adam", ""));
    assertEquals(MeetingGroup.idToJoinCode(1), Coordinator.createGroup("32", "adam", 30, 0,
        "#123456"));
    assertTrue(Coordinator.joinGroup("adam", 1));
    assertTrue(Coordinator.lockGroup(1, "adam"));

    assertEquals(MeetingGroup.idToJoinCode(2), Coordinator.createGroup("33", "adam", 60, 1,
        "#123456"));
    assertTrue(Coordinator.joinGroup("adam", 2));
    assertTrue(Coordinator.lockGroup(2, "adam"));

    tearDown();
  }
}