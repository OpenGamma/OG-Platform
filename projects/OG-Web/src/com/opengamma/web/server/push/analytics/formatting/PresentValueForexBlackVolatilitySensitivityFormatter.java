/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class PresentValueForexBlackVolatilitySensitivityFormatter
    extends NoHistoryFormatter<PresentValueForexBlackVolatilitySensitivity> {

  @Override
  public Object formatForDisplay(PresentValueForexBlackVolatilitySensitivity value, ValueSpecification valueSpec) {
    return "Vector (" + value.getVega().getMap().size() + ")";
  }

  @Override
  public Object formatForExpandedDisplay(PresentValueForexBlackVolatilitySensitivity value,
                                         ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.LABELLED_MATRIX_1D;
  }
}
