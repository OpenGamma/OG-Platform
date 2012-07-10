/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix1D;

/**
 *
 */
/* package */ class LabelledMatrix1DFormatter implements Formatter<LabelledMatrix1D> {

  private static final String NAME = "LABELLED_MATRIX_1D";

  @Override
  public String formatForDisplay(LabelledMatrix1D value, ValueSpecification valueSpec) {
    return "Vector (" + value.getKeys().length + ")";
  }

  @Override
  public Object formatForExpandedDisplay(LabelledMatrix1D value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(LabelledMatrix1D history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
