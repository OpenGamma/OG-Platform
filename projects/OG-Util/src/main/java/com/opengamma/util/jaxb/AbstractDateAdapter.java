/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * Abstract date XML adapter.
 * 
 * @param <DT>  date-time type
 */
public abstract class AbstractDateAdapter<DT extends TemporalAccessor> extends XmlAdapter<String, DT> {

  /**
   * The formatter.
   */
  private final DateTimeFormatter _formatter;

  /**
   * Creates an instance.
   * 
   * @param datePattern  the date pattern to wrap, not null
   */
  public AbstractDateAdapter(String datePattern) {
    // Use a case insensitive pattern so a string of the
    // form MAR15 can be parsed to March 2015
    _formatter = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ofPattern(datePattern))
        .toFormatter();
  }

  /**
   * Gets the formatter.
   * 
   * @return the formatter, not null
   */
  public DateTimeFormatter getFormatter() {
    return _formatter;
  }

  @Override
  public String marshal(DT v) throws Exception {
    return _formatter.format(v);
  }

}
