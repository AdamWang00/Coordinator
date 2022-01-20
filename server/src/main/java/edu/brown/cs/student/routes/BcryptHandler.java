package edu.brown.cs.student.routes;


import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Class to hash passwords.
 */
public final class BcryptHandler {
  private static final int COST = 12;

  private BcryptHandler() {
  }

  /**
   * Encrypt a string.
   *
   * @param toEncrypt String to encrypt
   * @return encrypted string
   */
  public static String encrypt(String toEncrypt) {

    return BCrypt.withDefaults().hashToString(COST, toEncrypt.toCharArray());
  }

  /**
   * Compare string with encrypted string.
   *
   * @param s      string
   * @param hashed encrypted string
   * @return boolean indicating if they match
   */
  public static Boolean compare(String s, String hashed) {
    BCrypt.Result result = BCrypt.verifyer().verify(s.toCharArray(), hashed);
    return result.verified;
  }


}
