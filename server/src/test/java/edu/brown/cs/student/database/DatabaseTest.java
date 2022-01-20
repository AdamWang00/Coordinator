package edu.brown.cs.student.database;

import edu.brown.cs.student.ITest;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

/**
 * Tests Database.
 */
public class DatabaseTest implements ITest {
  private Database db;

  @Override
  public void setUp() {
    try {
      this.db = new Database("data/test.sqlite3");
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Override
  public void tearDown() {
    try {
      this.db.close();
    } catch (RuntimeException e) {
      fail();
    }
  }

  /**
   * Tests constructor.
   */
  @Test
  public void testConstructor() {
    assertThrows(RuntimeException.class, () -> {
      new Database("hello");
    });
  }

  /**
   * Tests createStatement.
   */
  @Test
  public void testCreateStatement() {
    this.setUp();
    try {
      this.db.createStatement("SELECT 1+1;");
    } catch (RuntimeException e) {
      fail();
    }
    this.tearDown();
    assertThrows(RuntimeException.class, () -> {
      this.db.createStatement("SELECT 1+1;");
    });
  }
}