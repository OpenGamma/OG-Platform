/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix2D;

/**
 *
 */
/* package */ class LabelledMatrix2DFormatter implements Formatter<LabelledMatrix2D> {

  private static final String NAME = "LABELLED_MATRIX_2D";

  @Override
  public String formatForDisplay(LabelledMatrix2D value, ValueSpecification valueSpec) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + ")";
  }

  @Override
  public Object formatForExpandedDisplay(LabelledMatrix2D value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(LabelledMatrix2D history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
