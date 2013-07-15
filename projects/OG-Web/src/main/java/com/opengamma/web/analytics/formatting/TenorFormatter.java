/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.threeten.bp.Period;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.time.Tenor;

/**
 *
 */
/* package */ class TenorFormatter extends AbstractFormatter<Tenor> {

  protected TenorFormatter() {
    super(Tenor.class);
  }

  @Override
  public Period formatCell(Tenor tenor, ValueSpecification valueSpec, Object inlineKey) {
    return tenor.getPeriod();
  }

  @Override
  public DataType getDataType() {
    return DataType.TENOR;
  }
}
