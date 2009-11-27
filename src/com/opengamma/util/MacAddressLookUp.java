/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 
 * 
 * @author yomi
 */
public final class MacAddressLookUp {
  private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
  public static String ADDRESS = null;

  static {
    try {
      Enumeration<NetworkInterface> ifs = NetworkInterface
          .getNetworkInterfaces();
      if (ifs != null) {
        while (ifs.hasMoreElements()) {
          NetworkInterface iface = ifs.nextElement();
          byte[] hardware = iface.getHardwareAddress();
          if (hardware != null && hardware.length == 6
              && hardware[1] != (byte) 0xff) {
            ADDRESS = append(hardware);
            break;
          }
        }
      }
    } catch (SocketException ex) {
      // Ignore it.
    }
  }

  private MacAddressLookUp() {
  }

  public static String append(byte[] bytes) {
    StringBuilder buf = new StringBuilder(36);
    for (byte b : bytes) {
      buf.append(HEX_DIGITS[(byte) ((b & 0xF0) >> 4)]);
      buf.append(HEX_DIGITS[(byte) (b & 0x0F)]);
    }
    return buf.toString();
  }

}
