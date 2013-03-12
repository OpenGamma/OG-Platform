/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.TemporalAccessor;

/**
 * 
 * @param <DT>  date-time type
 */
public abstract class AbstractDateAdapter<DT extends TemporalAccessor> extends XmlAdapter<String, DT> {

  protected final DateTimeFormatter _formatter = buildFormatter();

  private DateTimeFormatter buildFormatter() {

    // Use a case insensitive pattern so a string of the
    // form MAR15 can be parsed to March 2015
    return new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ofPattern(getDatePattern()))
        .toFormatter();
  }

  @Override
  public String marshal(DT v) throws Exception {
    return _formatter.format(v);
  }

  protected abstract String getDatePattern();
}
