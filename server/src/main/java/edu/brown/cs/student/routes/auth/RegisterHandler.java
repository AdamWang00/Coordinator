package edu.brown.cs.student.routes.auth;

import com.google.common.collect.ImmutableMap;
import edu.brown.cs.student.coordinator.Coordinator;
import edu.brown.cs.student.routes.BcryptHandler;
import edu.brown.cs.student.routes.TokenHandler;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;

import com.google.gson.Gson;
import org.json.JSONObject;

/**
 * Class for handling signups.
 */
public class RegisterHandler implements Route {
  private static final Gson GSON = new Gson();
  private static final int I201 = 201;
  private static final int I409 = 409;

  @Override
  public Object handle(Request request, Response response) throws Exception {
    try {
      // get params
      JSONObject data = new JSONObject(request.body());
      String username = data.getString("username");
      String password = data.getString("password");

      // hash password
      String hashedPassword = BcryptHandler.encrypt(password);

      // store in db
      boolean result = Coordinator.createUser(username, hashedPassword);

      // not unique username
      if (!result) {
        response.status(I409); // 409 conflict
        return GSON.toJson(ImmutableMap.of("message", "Choose another username"));
      }

      // generate token
      String token = TokenHandler.getNewToken(username);

      // Create an immutable map for the response
      Map<String, Object> responseMap = ImmutableMap.of("token", token, "username", username);
      // return JSON object
      response.status(I201);
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
