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

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.curve.Curve;

/**
 * Holds Fudge builders for the interest rate curve model.
 */
/* package */final class ModelForwardCurve {

  /**
   * Restricted constructor.
   */
  private ModelForwardCurve() {
  }

  /**
   * Fudge builder for {@code ForwardCurve}
   */
  @FudgeBuilderFor(ForwardCurve.class)
  public static final class ForwardCurveBuilder extends AbstractFudgeBuilder<ForwardCurve> {
    //TODO need to propagate the drift curve as well
    private static final String CURVE_FIELD_NAME = "forwardCurve";

    @SuppressWarnings("unchecked")
    @Override
    public ForwardCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Curve<Double, Double> curve = deserializer.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new ForwardCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForwardCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getForwardCurve(), Curve.class);
    }
  }

}
