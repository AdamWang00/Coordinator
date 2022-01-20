package edu.brown.cs.student.routes.group;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.TokenHandler;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Class to get update preference score.
 */
public class UpdatePreferenceScoreHandler implements Route {
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

      // get body
      JSONObject data = new JSONObject(request.body());
      int meetingTimeId = data.getInt("meetingTimeId");
      int updatedScore = data.getInt("updatedScore");

      // update in db
      Coordinator.updatePreferenceScore(username, meetingTimeId, updatedScore);

      //Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("message", "preferences updated");

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
