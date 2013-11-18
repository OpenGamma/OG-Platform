/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.convention.frequency.Frequency;

/**
 *
 */
/* package */ class FrequencyFormatter extends AbstractFormatter<Frequency> {

  /* package */ FrequencyFormatter() {
    super(Frequency.class);
  }

  @Override
  public Object formatCell(Frequency frequency, ValueSpecification valueSpec, Object inlineKey) {
    return frequency.getName();
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
