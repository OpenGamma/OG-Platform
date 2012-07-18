/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import com.bloomberglp.blpapi.SessionOptions;

/**
 * Utility to provide proper toString() for SessionOptions to aid with debugging.
 */
public class SessionOptionsUtils {
  
  public static String toString(SessionOptions session) {
    StringBuilder sb = new StringBuilder();
    sb.append("SessionOptions[host=");
    sb.append(session.getServerHost());
    sb.append(",port=");
    sb.append(session.getServerPort());
    sb.append(",clientMode");
    sb.append(session.getClientMode());
    sb.append("]");
    return sb.toString();
  }
}
