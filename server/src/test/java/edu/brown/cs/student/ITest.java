package edu.brown.cs.student;

import org.junit.After;
import org.junit.Before;

/**
 * a template for all tests.
 */
public interface ITest {

  /**
   * sets up before testing.
   */
  @Before
  void setUp();

  /**
   * tears down everything after testing.
   */
  @After
  void tearDown();

}