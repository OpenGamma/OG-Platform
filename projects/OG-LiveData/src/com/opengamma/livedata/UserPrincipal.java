/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import com.opengamma.util.PublicAPI;

/**
 * User credentials.
 * <p>
 * In LiveData, user credentials include user name and the IP address of the user.
 */
@PublicAPI
public class UserPrincipal implements java.io.Serializable {

  /**
   * Gets a local user by user name.
   * 
   * @param userName user name, not null
   * @return user with the specified user name and IP address {@code java.net.InetAddress.getLocalHost().toString()}
   */
  public static UserPrincipal getLocalUser(String userName) {
    try {
      UserPrincipal user = new UserPrincipal(userName, java.net.InetAddress.getLocalHost().toString());
      return user;
    } catch (java.net.UnknownHostException e) {
      throw new com.opengamma.OpenGammaRuntimeException("Could not initialize local user", e);
    }
  }

  /**
   * Gets a local user.
   * 
   * @return user with user name {@code System.getProperty("user.name")} and
   * IP address {@code java.net.InetAddress.getLocalHost().toString()}
   */
  public static UserPrincipal getLocalUser() {
    String userName = System.getProperty("user.name");
    if (userName == null) {
      userName = "Unknown User";
    }
    return UserPrincipal.getLocalUser(userName);
  }

  private static final UserPrincipal TEST_USER = new UserPrincipal("Test user", "127.0.0.1");

  /**
   * Gets a test user.
   * 
   * @return user with user name {@code Test user} and IP address {@code 127.0.0.1}
   */
  public static UserPrincipal getTestUser() {
    return TEST_USER;
  }

  private static final long serialVersionUID = -9633023788096L;
  
  /**
   * User name
   */
  private final String _userName;
  
  /**
   * IP address
   */
  private final String _ipAddress;
  
  private static final String USER_NAME_KEY = "userName";
  private static final String IP_ADDRESS_KEY = "ipAddress";

  /**
   * Constructs a new user.
   * 
   * @param userName user name, not null
   * @param ipAddress IP address, not null
   */
  public UserPrincipal(String userName, String ipAddress) {
    if (userName == null) {
      throw new NullPointerException("userName' cannot be null");
    }
    _userName = userName;
    if (ipAddress == null) {
      throw new NullPointerException("ipAddress' cannot be null");
    }
    _ipAddress = ipAddress;
  }

  protected UserPrincipal(final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName(USER_NAME_KEY);
    if (fudgeField == null) {
      throw new IllegalArgumentException("Fudge message is not a UserPrincipal - field 'userName' is not present");
    }
    try {
      _userName = fudgeField.getValue().toString();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Fudge message is not a UserPrincipal - field 'userName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName(IP_ADDRESS_KEY);
    if (fudgeField == null) {
      throw new IllegalArgumentException("Fudge message is not a UserPrincipal - field 'ipAddress' is not present");
    }
    try {
      _ipAddress = fudgeField.getValue().toString();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Fudge message is not a UserPrincipal - field 'ipAddress' is not string", e);
    }
  }

  protected UserPrincipal(final UserPrincipal source) {
    if (source == null) {
      throw new NullPointerException("'source' must not be null");
    }
    _userName = source._userName;
    _ipAddress = source._ipAddress;
  }

  public UserPrincipal clone() {
    return new UserPrincipal(this);
  }

  public org.fudgemsg.FudgeFieldContainer toFudgeMsg(final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) {
      throw new NullPointerException("fudgeContext must not be null");
    }
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    toFudgeMsg(fudgeContext, msg);
    return msg;
  }

  public void toFudgeMsg(final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_userName != null) {
      msg.add(USER_NAME_KEY, null, _userName);
    }
    if (_ipAddress != null) {
      msg.add(IP_ADDRESS_KEY, null, _ipAddress);
    }
  }

  public static UserPrincipal fromFudgeMsg(final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal(0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String) field.getValue();
      if ("com.opengamma.livedata.msg.UserPrincipal".equals(className)) {
        break;
      }
      try {
        return (com.opengamma.livedata.UserPrincipal) Class.forName(className).getDeclaredMethod("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke(null, fudgeMsg);
      } catch (Throwable t) {
        // no-action
      }
    }
    return new UserPrincipal(fudgeMsg);
  }

  /**
   * Gets the user name of the user.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Gets the IP address of the user
   * 
   * @return the IP address, not null
   */
  public String getIpAddress() {
    return _ipAddress;
  }

  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof UserPrincipal)) {
      return false;
    }
    UserPrincipal msg = (UserPrincipal) o;
    if (_userName != null) {
      if (msg._userName != null) {
        if (!_userName.equals(msg._userName)) {
          return false;
        }
      } else {
        return false;
      }
    } else if (msg._userName != null) {
      return false;
    }
    if (_ipAddress != null) {
      if (msg._ipAddress != null) {
        if (!_ipAddress.equals(msg._ipAddress)) {
          return false;
        }
      } else {
        return false;
      }
    } else if (msg._ipAddress != null) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    int hc = 1;
    hc *= 31;
    if (_userName != null) {
      hc += _userName.hashCode();
    }
    hc *= 31;
    if (_ipAddress != null) {
      hc += _ipAddress.hashCode();
    }
    return hc;
  }

  public String toString() {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
