/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Utility for managing IP addresses.
 */
public final class InetAddressUtils {

  /**
   * Restricted constructor.
   */
  private InetAddressUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the local host name.
   * 
   * @return the local host name, not null
   * @throws OpenGammaRuntimeException if unable to obtain the local host
   */
  public static String getLocalHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Could not obtain local host", e);
    }
  }

}
