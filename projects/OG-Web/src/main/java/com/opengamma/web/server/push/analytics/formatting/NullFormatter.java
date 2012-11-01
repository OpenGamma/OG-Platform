/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats null values for display in the client.
 */
/* package */ class NullFormatter extends AbstractFormatter<Object> {

  NullFormatter() {
    super(Object.class);
  }

  @Override
  public Object formatCell(Object value, ValueSpecification valueSpec) {
    return "";
  }

  @Override
  public DataType getDataType() {
    return DataType.PRIMITIVE;
  }
}
