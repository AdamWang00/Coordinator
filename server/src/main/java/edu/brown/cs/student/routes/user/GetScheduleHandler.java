package edu.brown.cs.student.routes.user;

import com.google.common.collect.ImmutableMap;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.TokenHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

import com.google.gson.Gson;

/**
 * Class that represents endpoint for getting user schedule.
 */
public class GetScheduleHandler implements Route {
  private static final Gson GSON = new Gson();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      // get username
      String username = TokenHandler.getUserIdFromRequest(request);

      if (username == null) {
        // not authorized
        response.status(401);
        return "";
      }

      boolean[] schedule = Coordinator.getWeeklySchedule(username);

      //Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("schedule", schedule);

      //return JSON object
      return GSON.toJson(responseMap);
    } catch (Exception e) {
      // return 400 HTTP code if malformed request
      response.status(400);

      //Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("message", e.getMessage());
      return GSON.toJson(responseMap);
    }
  }
}
