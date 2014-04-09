/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import org.mindrot.jbcrypt.BCrypt;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities for managing authentication.
 * <p>
 * Authentication is the process of ensuring that the user is who they say they are.
 * This is supported via a password.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class AuthenticationUtils {

  /**
   * Restricted constructor.
   */
  private AuthenticationUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Check to see whether the password provided by an end-user as part of an authentication
   * pass matches the hashed version stored in the {@link UserAccount}.
   * 
   * @param user  the user to check, not null
   * @param passwordProvided  the password provided by the user, not null
   * @return true if the password matches the hash
   */
  public static boolean passwordsMatch(UserAccount user, String passwordProvided) {
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(passwordProvided, "passwordProvided");
    return BCrypt.checkpw(passwordProvided, user.getPasswordHash());
  }

  /**
   * Generate a new hash for the raw password provided.
   * 
   * @param rawPassword  the password to hash, not null
   * @return the hashed password for storage, not null
   */
  public static String generatePasswordHash(String rawPassword) {
    ArgumentChecker.notNull(rawPassword, "rawPassword");
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

}
