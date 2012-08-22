/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility classes key for performing authentication-related duties
 * with instances of {@link OGUser}.
 */
public final class AuthenticationUtils {
  private AuthenticationUtils() {
  }
  
  /**
   * Check to see whether the password provided by an end-user as part of an authentication
   * pass matches the hashed version stored in the {@link OGUser}.
   * 
   * @param user The user to check.
   * @param passwordProvided The password provided by the user.
   * @return true iff the password matches the hash.
   */
  public static boolean passwordsMatch(OGUser user, String passwordProvided) {
    return BCrypt.checkpw(passwordProvided, user.getPasswordHash());
  }
  
  /**
   * Generate a new hash for the raw password provided.
   * @param rawPassword The password to hash.
   * @return the hashed password for storage.
   */
  public static String generatePasswordHash(String rawPassword) {
    String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    return passwordHash;
  }

}
