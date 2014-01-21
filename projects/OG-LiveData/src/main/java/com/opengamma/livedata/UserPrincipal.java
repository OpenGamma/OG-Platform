/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * User credentials.
 * <p>
 * These user credentials include the user name and the IP address of the user.
 */
@PublicAPI
public class UserPrincipal implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -9633023788096L;
  /**
   * The test user.
   */
  private static final UserPrincipal TEST_USER = new UserPrincipal("Test user", "127.0.0.1");

  /**
   * The user name.
   */
  private final String _userName;
  /**
   * The IP address.
   */
  private final String _ipAddress;

  /**
   * Gets a local user by user name.
   * <p>
   * This creates a user with the specified name.
   * The IP address is derived from {@code java.net.InetAddress.getLocalHost().toString()}.
   * 
   * @param userName  the user name, not null
   * @return the user, not null
   */
  public static UserPrincipal getLocalUser(String userName) {
    try {
      return new UserPrincipal(userName, InetAddress.getLocalHost().toString());
    } catch (UnknownHostException ex) {
      throw new com.opengamma.OpenGammaRuntimeException("Could not initialize local user", ex);
    }
  }

  /**
   * Gets a local user by system properties.
   * <p>
   * This creates a user based on {@code System.getProperty("user.name")}.
   * The IP address is derived from {@code java.net.InetAddress.getLocalHost().toString()}.
   * 
   * @return the user, not null
   */
  public static UserPrincipal getLocalUser() {
    String userName = System.getProperty("user.name");
    if (userName == null) {
      userName = "Unknown User";
    }
    return UserPrincipal.getLocalUser(userName);
  }

  /**
   * Gets a test user.
   * <p>
   * The name is "Test user" and the IP address is {@code 127.0.0.1}
   * 
   * @return user  the test user, not null
   */
  public static UserPrincipal getTestUser() {
    return TEST_USER;
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new user.
   * 
   * @param userName  the user name, not null
   * @param ipAddress  the IP address, not null
   */
  public UserPrincipal(String userName, String ipAddress) {
    ArgumentChecker.notNull(userName, "userName");
    ArgumentChecker.notNull(ipAddress, "ipAddress");
    _userName = userName;
    _ipAddress = ipAddress;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user name of the user.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Gets the location identifier for a user. This may be an IP address
   * or a session id.
   * 
   * @return the location identifier, not null
   */
  public String getIpAddress() {
    return _ipAddress;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof UserPrincipal) {
      UserPrincipal other = (UserPrincipal) obj;
      return _userName.equals(other._userName) && _ipAddress.equals(other._ipAddress);
    }
    return false;
  }

  public int hashCode() {
    return _userName.hashCode() ^ _ipAddress.hashCode();
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("UserPrincipal[");
    buf.append(_userName);
    buf.append(", ");
    buf.append(_ipAddress);
    buf.append("]");
    return buf.toString();
  }

}
