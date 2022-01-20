package edu.brown.cs.student.routes.group;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.coordinator.MeetingTimePreference;
import edu.brown.cs.student.routes.TokenHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.Map;

/**
 * Class to get time preferences.
 */
public class GetTimePreferences implements Route {
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

      // get params
      int groupId = Integer.parseInt(request.params(":groupId"));

      // retrieve from db
      List<MeetingTimePreference> preferences = Coordinator.getPreferences(username, groupId);

      //Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("preferences", preferences);

      //return JSON object
      response.status(200);
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
