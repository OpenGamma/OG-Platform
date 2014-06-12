/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.util.Locale;
import java.util.Map;

import org.threeten.bp.ZoneId;

import com.opengamma.util.PublicAPI;

/**
 * A user profile, containing settings specific to how the user uses the system.
 * <p> 
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicAPI
public interface UserProfile {

  /**
   * The session attribute key for the user profile.
   * This is used to store the profile in the Apache Shiro session.
   */
  String ATTRIBUTE_KEY = UserProfile.class.getName();

  /**
   * Gets the display name, such as the user's real name.
   * This is typically used in a GUI and is not guaranteed to be unique.
   * 
   * @return the display user name, may be null
   */
  String getDisplayName();

  /**
   * Gets the locale that the user prefers.
   * 
   * @return the locale, not null
   */
  Locale getLocale();

  /**
   * Gets the time-zone used to display local times.
   * 
   * @return the time-zone, not null
   */
  ZoneId getZone();

  /**
   * Gets the date format style that the user prefers.
   * 
   * @return the date format style, null
   */
  DateStyle getDateStyle();

  /**
   * Gets the time format style that the user prefers.
   * 
   * @return the time format style, null
   */
  TimeStyle getTimeStyle();

  /**
   * Gets the extended map of profile data.
   * 
   * @return the extended map of profile data, may be null
   */
  Map<String, String> getExtensions();

}
