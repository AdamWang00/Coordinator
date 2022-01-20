package edu.brown.cs.student.main;

import java.io.PrintWriter;
import java.io.StringWriter;

import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.auth.LoginHandler;
import edu.brown.cs.student.routes.auth.RegisterHandler;
import edu.brown.cs.student.routes.group.CreateGroupHandler;
import edu.brown.cs.student.routes.group.DeleteGroupHandler;
import edu.brown.cs.student.routes.group.EditGroupHandler;
import edu.brown.cs.student.routes.group.GetTimePreferences;
import edu.brown.cs.student.routes.group.JoinGroupHandler;
import edu.brown.cs.student.routes.group.LeaveGroupHandler;
import edu.brown.cs.student.routes.group.LockGroupHandler;
import edu.brown.cs.student.routes.group.UpdatePreferenceScoreHandler;
import edu.brown.cs.student.routes.group.UpdatePreferencePinHandler;
import edu.brown.cs.student.routes.user.EditScheduleHandler;
import edu.brown.cs.student.routes.user.GetScheduleHandler;
import edu.brown.cs.student.routes.user.GetUserGroupsHandler;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.json.JSONObject;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  private static final int DEFAULT_PORT = 80;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(DEFAULT_PORT);
    parser.accepts("db").withRequiredArg().ofType(String.class).defaultsTo("data/test.sqlite3");

    OptionSet options = parser.parse(args);

    // connect db
    Coordinator.connectCoordinatorDatabase((String) options.valueOf("db"));

    runSparkServer((int) options.valueOf("port"));
  }

  private void runSparkServer(int port) {
    Spark.port(port);

    Spark.staticFiles.location("/public");

    Spark.options("/*", (request, response) -> {
      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");

      if (accessControlRequestMethod != null) {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

    Spark.exception(Exception.class, new ExceptionPrinter());

    // Setup Spark Routes

    Spark.post("/hello", (req, res) -> {
      JSONObject j = new JSONObject(req.body());
      return j.optString("hey", "12345");
    });

    // auth routes
    Spark.post("/auth/register", new RegisterHandler());
    Spark.post("/auth/login", new LoginHandler());

    // user routes
    Spark.get("/user/schedule", new GetScheduleHandler());
    Spark.get("/user/groups", new GetUserGroupsHandler());
    Spark.post("/user/schedule", new EditScheduleHandler());

    // group routes
    Spark.post("/group", new CreateGroupHandler());
    Spark.put("/group/:groupId", new EditGroupHandler());
    Spark.post("/group/:groupId/lock", new LockGroupHandler());
    Spark.delete("/group/:groupId", new DeleteGroupHandler());
    Spark.post("/group/:joinCode/join", new JoinGroupHandler());
    Spark.post("/group/:joinCode/leave", new LeaveGroupHandler());
    Spark.get("/group/:groupId/time", new GetTimePreferences());
    Spark.post("/group/time/score", new UpdatePreferenceScoreHandler());
    Spark.post("/group/time/pin", new UpdatePreferencePinHandler());
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }
}
