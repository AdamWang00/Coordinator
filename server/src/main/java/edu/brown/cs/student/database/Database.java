package edu.brown.cs.student.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class representing a database. The database is able to make connection to a SQL database and
 * make prepared statements with the connected database.
 */
public class Database {
  private Connection conn;

  /**
   * Constructor. Make connection to database.
   *
   * @param databasePath path to database file
   * @throws RuntimeException when an error with SQL is encountered
   * @throws RuntimeException when the path does not point to a database file
   */
  public Database(String databasePath) throws RuntimeException {
    try {
      // Check if file exists
      if (!(new File(databasePath)).exists()) {
        throw new RuntimeException("no file found at " + databasePath);
      }
      Class.forName("org.sqlite.JDBC");
      this.conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
      Statement stat = conn.createStatement();
      stat.executeUpdate("PRAGMA foreign_keys=ON;");
    } catch (SQLException | ClassNotFoundException e) {
      throw new RuntimeException("could not connect to sqlite database at " + databasePath);
    }
  }

  /**
   * Creates a PreparedStatement with the database connection.
   *
   * @param sql content of the statement
   * @return the statement with the provided content
   * @throws RuntimeException when a statement cannot be created (e.g. cannot obtain database lock)
   */
  public PreparedStatement createStatement(String sql) throws RuntimeException {
    try {
      return this.conn.prepareStatement(sql);
    } catch (SQLException e) {
      throw new RuntimeException("could not prepare a new SQL statement");
    }
  }

  /**
   * Closes connection to database (call on cleanup).
   *
   * @throws RuntimeException when database connection cannot be closed
   */
  public void close() throws RuntimeException {
    try {
      this.conn.close();
    } catch (SQLException e) {
      throw new RuntimeException("could not close database connection");
    }
  }
}
