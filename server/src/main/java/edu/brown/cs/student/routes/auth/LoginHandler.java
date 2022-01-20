package edu.brown.cs.student.routes.auth;

import com.google.common.collect.ImmutableMap;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.BcryptHandler;
import edu.brown.cs.student.routes.TokenHandler;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Class for handling logins.
 */
public class LoginHandler implements Route {
  private static final Gson GSON = new Gson();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      // get params
      JSONObject data = new JSONObject(request.body());
      String username = data.getString("username");
      String password = data.getString("password");

      // get hashed password
      String hashedPassword = Coordinator.getHashedPassword(username);

      // check hash
      if (hashedPassword == null || !BcryptHandler.compare(password, hashedPassword)) {
        response.status(401);
        return GSON.toJson(ImmutableMap.of("message", "Unauthenticated"));
      }

      // authenticated - generate token
      String token = TokenHandler.getNewToken(username);

      // Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("token", token, "username", username);

      // return JSON object
      response.status(200);
      return GSON.toJson(responseMap);
    } catch (Exception e) {
      // return 400 HTTP code if malformed request
      response.status(400);

      // Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("message", e.getMessage());
      return GSON.toJson(responseMap);
    }
  }
}
