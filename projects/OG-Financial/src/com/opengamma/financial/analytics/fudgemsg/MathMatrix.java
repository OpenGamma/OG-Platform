/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
/* package */final class MathMatrix {
  
  private MathMatrix() {
  }
  
  /**
   * Fudge builder for {@code DoubleMatrix1D}
   */
  @FudgeBuilderFor(DoubleMatrix1D.class)
  public static final class DoubleMatrix1DBuilder extends AbstractFudgeBuilder<DoubleMatrix1D> {
    private static final String DATA_FIELD_NAME = "data";

    @Override
    public DoubleMatrix1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      double[] data = message.getFieldValue(double[].class, message.getByName(DATA_FIELD_NAME));
      return new DoubleMatrix1D(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final DoubleMatrix1D object) {
      message.add(DATA_FIELD_NAME, null, object.getData());
    }

  }

  /**
   * Fudge builder for {@code DoubleMatrix2D}
   */
  @FudgeBuilderFor(DoubleMatrix2D.class)
  public static final class DoubleMatrix2DBuilder extends AbstractFudgeBuilder<DoubleMatrix2D> {
    private static final String DATA_FIELD_NAME = "data";

    @Override
    public DoubleMatrix2D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[][] data = deserializer.fieldValueToObject(double[][].class, message.getByName(DATA_FIELD_NAME));
      return new DoubleMatrix2D(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final DoubleMatrix2D object) {
      serializer.addToMessage(message, DATA_FIELD_NAME, null, object.getData());
    }
  }

}
