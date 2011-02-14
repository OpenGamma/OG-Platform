/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * A lookup of MAC address.
 */
final class MacAddressLookUp {

  /**
   * Array of hex digits.
   */
  private static final char[] HEX_DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  /**
   * The MAC address.
   */
  static final String ADDRESS;
  static {
    String address = null;
    try {
      Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
      if (ifs != null) {
        while (ifs.hasMoreElements()) {
          NetworkInterface iface = ifs.nextElement();
          byte[] hardware = iface.getHardwareAddress();
          if (hardware != null && hardware.length == 6
              && hardware[1] != (byte) 0xff) {
            address = convertToHexString(hardware);
            break;
          }
        }
      }
    } catch (SocketException ex) {
      // Ignore it.
    }
    ADDRESS = address;
  }

  /**
   * Restricted constructor.
   */
  private MacAddressLookUp() {
  }

  /**
   * Converts a byte array to a string.
   * @param bytes  the bytes, not null
   * @return the string, not null
   */
  private static String convertToHexString(byte[] bytes) {
    StringBuilder buf = new StringBuilder(36);
    for (byte b : bytes) {
      buf.append(HEX_DIGITS[(byte) ((b & 0xF0) >> 4)]);
      buf.append(HEX_DIGITS[(byte) (b & 0x0F)]);
    }
    return buf.toString();
  }

}
