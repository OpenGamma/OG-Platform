/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix3D;

/**
 *
 */
/* package */ class LabelledMatrix3DFormatter implements Formatter<LabelledMatrix3D> {

  private static final String NAME = "LABELLED_MATRIX_3D";

  @Override
  public Object formatForDisplay(LabelledMatrix3D value, ValueSpecification valueSpec) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + " x " + value.getZKeys().length + ")";
  }

  @Override
  public Object formatForExpandedDisplay(LabelledMatrix3D value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(LabelledMatrix3D history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public String getName() {
    return NAME;
  }
}
