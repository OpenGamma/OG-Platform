/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;

/**
 * Provides a shared singleton {@code Clock} for use throughout OpenGamma.
 * <p>
 * The {@code Clock} is a JSR-310 concept that provides access to the current
 * date and time in a time-zone. Managing a separate clock via dependencies is
 * hard work, so this class provides a singleton.
 */
public final class OpenGammaClock {

  /**
   * Singleton instance.
   */
  private static volatile Clock s_instance = Clock.system(TimeZone.UTC);

  /**
   * Restricted constructor.
   */
  private OpenGammaClock() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance of the clock.
   * 
   * @return the singleton instance, not null
   */
  public static Clock getInstance() {
    return s_instance;
  }

  /**
   * Sets the Clock.
   * 
   * @param clock  the clock, not null
   */
  public static void setInstance(Clock clock) {
    s_instance = clock;
  }

  /**
   * Sets the time-zone.
   * 
   * @param zone  the zone, not null
   */
  public static void setZone(TimeZone zone) {
    s_instance = s_instance.withZone(zone);
  }

}
