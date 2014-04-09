/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import java.util.Locale;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import com.opengamma.util.PublicAPI;

/**
 * The style of time selected in the user profile.
 */
@PublicAPI
public enum TimeStyle {

  /**
   * The standard ISO-8601 format
   */
  ISO(DateTimeFormatter.ISO_LOCAL_TIME),
  /**
   * The short localized format.
   */
  LOCALIZED_SHORT(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
  /**
   * The medium localized format.
   */
  LOCALIZED_MEDIUM(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)),
  /**
   * The long localized format.
   */
  LOCALIZED_LONG(DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG)),
  /**
   * The full localized format.
   */
  LOCALIZED_FULL(DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL));

  private final DateTimeFormatter _formatter;

  private TimeStyle(DateTimeFormatter formatter) {
    _formatter = formatter;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the formatter object, ready to use.
   * 
   * @param locale  the locale to use, not null
   * @return the formatter, not null
   */
  public DateTimeFormatter formatter(Locale locale) {
    return _formatter.withLocale(locale);
  }

}
