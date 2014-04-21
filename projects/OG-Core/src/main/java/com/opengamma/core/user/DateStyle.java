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
 * The style of date selected in the user profile.
 */
@PublicAPI
public enum DateStyle {

  /**
   * The standard ISO-8601 format
   */
  ISO(DateTimeFormatter.ISO_LOCAL_DATE),
  /**
   * A standard US ordered format.
   */
  STANDARD_US(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
  /**
   * A standard EU ordered format.
   */
  STANDARD_EU(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
  /**
   * A simple format with abbreviated textual month.
   */
  TEXTUAL_MONTH(DateTimeFormatter.ofPattern("d MMM yyyy")),
  /**
   * The short localized format.
   */
  LOCALIZED_SHORT(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
  /**
   * The medium localized format.
   */
  LOCALIZED_MEDIUM(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
  /**
   * The long localized format.
   */
  LOCALIZED_LONG(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
  /**
   * The full localized format.
   */
  LOCALIZED_FULL(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));

  private final DateTimeFormatter _formatter;

  private DateStyle(DateTimeFormatter formatter) {
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
