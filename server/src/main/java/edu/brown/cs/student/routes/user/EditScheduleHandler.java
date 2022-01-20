package edu.brown.cs.student.routes.user;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.TokenHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

/**
 * Class that handles updates to user schedule.
 */
public class EditScheduleHandler implements Route {
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

      // get schedule from body
      // get params
      JSONObject data = new JSONObject(request.body());
      JSONArray schedule = data.getJSONArray("schedule");

      // convert to array
      boolean[] scheduleArray = new boolean[schedule.length()];
      for (int i = 0; i < schedule.length(); i++) {
        scheduleArray[i] = schedule.getBoolean(i);
      }

      // insert into database
      Coordinator.updateWeeklySchedule(username, scheduleArray);

      //Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("message", "success");

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
