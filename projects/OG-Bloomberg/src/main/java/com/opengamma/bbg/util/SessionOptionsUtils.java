/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import com.bloomberglp.blpapi.SessionOptions;

/**
 * Utilities for working with {@code SessionOptions}.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class SessionOptionsUtils {

  /**
   * Restricted constructor.
   */
  private SessionOptionsUtils() {
  }

  /**
   * Converts a {@code SessionOptions} to a string for debugging.
   * 
   * @param options  the session options, not null
   * @return the string for debugging, not null
   */
  public static String toString(SessionOptions options) {
    StringBuilder sb = new StringBuilder();
    sb.append("SessionOptions[host=");
    sb.append(options.getServerHost());
    sb.append(",port=");
    sb.append(options.getServerPort());
    sb.append(",clientMode=");
    sb.append(options.getClientMode());
    sb.append("]");
    return sb.toString();
  }

}
