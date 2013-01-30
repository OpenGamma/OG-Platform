/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatters;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class ZonedDateTimeFormatter extends AbstractFormatter<ZonedDateTime> {

  private static final DateTimeFormatter s_formatter = DateTimeFormatters.isoLocalDate();

  /* package */ ZonedDateTimeFormatter() {
    super(ZonedDateTime.class);
  }

  @Override
  public Object formatCell(ZonedDateTime value, ValueSpecification valueSpec) {
    return s_formatter.print(value);
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
