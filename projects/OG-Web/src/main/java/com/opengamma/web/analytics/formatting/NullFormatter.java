/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats null values for display in the client.
 */
/* package */ class NullFormatter extends AbstractFormatter<Object> {

  /* package */ NullFormatter() {
    super(Object.class);
    addFormatter(new Formatter<Object>(Format.HISTORY) {
      @Override
      Object format(Object value, ValueSpecification valueSpec, Object inlineKey) {
        return null;
      }
    });
    addFormatter(new Formatter<Object>(Format.EXPANDED) {
      @Override
      Object format(Object value, ValueSpecification valueSpec, Object inlineKey) {
        return "";
      }
    });
  }

  @Override
  public Object formatCell(Object value, ValueSpecification valueSpec, Object inlineKey) {
    return "";
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
