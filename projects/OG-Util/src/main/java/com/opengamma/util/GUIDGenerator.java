/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * A generator of GUID.
 */
public final class GUIDGenerator {

  /**
   * The last time value. Used to remove duplicate UUIDs.
   */
  private static long s_lastTime = Long.MIN_VALUE;
  /**
   * The current clock and node value.
   */
  private static long s_clockSeqAndNode = 0x8000000000000000L;

  /**
   * Restricted constructor.
   */
  private GUIDGenerator() {
  }

  //-------------------------------------------------------------------------
  /**
   * Generate a UUID.
   * 
   * @return the UUID, not null
   */
  public static UUID generate() {
    return new UUID(newTime(), generateClockSeqAndNode(MacAddressLookUp.ADDRESS));
  }

  /**
   * Generates the time part of the GUID.
   * 
   * @return the time
   */
  public static synchronized long newTime() {
    long time;
    // UTC time
    long timeMillis = (System.currentTimeMillis() * 10000) + 0x01B21DD213814000L;
    if (timeMillis > s_lastTime) {
      s_lastTime = timeMillis;
    } else {
      timeMillis = ++s_lastTime;
    }
    // time low
    time = timeMillis << 32;
    // time mid
    time |= (timeMillis & 0xFFFF00000000L) >> 16;
    // time hi and version
    time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // version 1
    return time;
  }

  /**
   * Generates part of the GUID.
   * @param macAddress  the MAC address, null uses the local host address
   * @return the clock sequence and node
   */
  private static synchronized long generateClockSeqAndNode(String macAddress) {
    if (macAddress != null) {
      s_clockSeqAndNode |= parseLong(macAddress);
    } else {
      try {
        byte[] local = InetAddress.getLocalHost().getAddress();
        s_clockSeqAndNode |= (local[0] << 24) & 0xFF000000L;
        s_clockSeqAndNode |= (local[1] << 16) & 0xFF0000;
        s_clockSeqAndNode |= (local[2] << 8) & 0xFF00;
        s_clockSeqAndNode |= local[3] & 0xFF;
      } catch (UnknownHostException ex) {
        s_clockSeqAndNode |= (long) (Math.random() * 0x7FFFFFFF);
      }
    }
    // Skip the clock sequence generation process and use random instead.
    //REVIEW yomi -- 20091127 not sure if this correct
//    s_clockSeqAndNode |= (long) (Math.random() * 0x3FFF) << 48;
    return s_clockSeqAndNode;
  }

  /**
   * Parses a string to a long as required by GUID.
   * @param s  the string, not null
   * @return the long
   */
  public static long parseLong(CharSequence s) {
    long out = 0;
    byte shifts = 0;
    char c;
    for (int i = 0; i < s.length() && shifts < 16; i++) {
      c = s.charAt(i);
      if ((c > 47) && (c < 58)) {
        ++shifts;
        out <<= 4;
        out |= c - 48;
      } else if ((c > 64) && (c < 71)) {
        ++shifts;
        out <<= 4;
        out |= c - 55;
      } else if ((c > 96) && (c < 103)) {
        ++shifts;
        out <<= 4;
        out |= c - 87;
      }
    }
    return out;
  }

}
