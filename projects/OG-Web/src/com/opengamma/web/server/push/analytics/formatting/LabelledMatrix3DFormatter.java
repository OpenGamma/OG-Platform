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
/* package */ class LabelledMatrix3DFormatter extends NoHistoryFormatter<LabelledMatrix3D> {

  @Override
  public String formatForDisplay(LabelledMatrix3D value, ValueSpecification valueSpec) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + " x " + value.getZKeys().length + ")";
  }

  @Override
  public Object formatForExpandedDisplay(LabelledMatrix3D value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.LABELLED_MATRIX_3D;
  }
}
