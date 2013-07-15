/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for columns whose type is unknown.
 */
/* package */ class UnknownTypeFormatter extends DefaultFormatter {

  private static final Logger s_logger = LoggerFactory.getLogger(UnknownTypeFormatter.class);

  @Override
  public String formatCell(Object value, ValueSpecification valueSpec, Object inlineKey) {
    logType(value, valueSpec);
    return super.formatCell(value, valueSpec, inlineKey);
  }

  @Override
  public Object format(Object value, ValueSpecification valueSpec, Format format, Object inlineKey) {
    logType(value, valueSpec);
    return super.format(value, valueSpec, format, inlineKey);
  }

  private static void logType(Object value, ValueSpecification valueSpec) {
    String typeName = value == null ? null : value.getClass().getName();
    s_logger.info("Value received for unknown type, value name: {}, type: {}", valueSpec.getValueName(), typeName);
  }

  @Override
  public DataType getDataType() {
    return DataType.UNKNOWN;
  }
}
