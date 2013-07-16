/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix3D;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class LabelledMatrix3DFormatter extends AbstractFormatter<LabelledMatrix3D> {

  /* package */ LabelledMatrix3DFormatter() {
    super(LabelledMatrix3D.class);
  }

  @Override
  public String formatCell(LabelledMatrix3D value, ValueSpecification valueSpec, Object inlineKey) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + " x " + value.getZKeys().length + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_3D;
  }
}
