package edu.brown.cs.student.routes;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import spark.Request;


// https://github.com/jwtk/jjwt

/**
 * Class to handle json web tokens.
 */
public final class TokenHandler {
  private static final String SECRET = "ypBn3sRtaH8t6UgvjJznHQwd84H4PpHfvj085cjuk74=";

  private TokenHandler() {
  }

  /**
   * Generate new jwt.
   *
   * @param userId to generate jwt for
   * @return jwt
   */
  public static String getNewToken(String userId) {
    return Jwts.builder()
        .setSubject(userId).signWith(SignatureAlgorithm.HS256, SECRET).compact();
  }

  /**
   * Parse token and retrieve username.
   *
   * @param token - jwt attached
   * @return userId
   */
  public static String parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(SECRET).build().parseClaimsJws(token).getBody()
        .getSubject();
  }

  /**
   * Get authorization token from request header and return username.
   *
   * @param req - http request
   * @return userId
   */
  public static String getUserIdFromRequest(Request req) {
    String token = req.headers("Authorization");

    if (token == null) {
      return null;
    }

    return parseToken(token);
  }

}
