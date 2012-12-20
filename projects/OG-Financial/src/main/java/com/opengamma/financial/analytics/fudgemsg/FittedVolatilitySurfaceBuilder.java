/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
/* package */final class FittedVolatilitySurfaceBuilder {

  private FittedVolatilitySurfaceBuilder() {
  }

  @FudgeBuilderFor(StandardSmileSurfaceDataBundle.class)
  public static final class StandardSmileSurfaceDataBundleBuilder extends AbstractFudgeBuilder<StandardSmileSurfaceDataBundle> {
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurveField";
    private static final String EXPIRIES_FIELD_NAME = "expiriesField";
    private static final String STRIKES_FIELD_NAME = "strikesField";
    private static final String VOLS_FIELD_NAME = "volsField";

    @Override
    public StandardSmileSurfaceDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
      final double[] expiries = deserializer.fieldValueToObject(double[].class, message.getByName(EXPIRIES_FIELD_NAME));
      final double[][] strikes = deserializer.fieldValueToObject(double[][].class, message.getByName(STRIKES_FIELD_NAME));
      final double[][] vols = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLS_FIELD_NAME));
      return new StandardSmileSurfaceDataBundle(forwardCurve, expiries, strikes, vols);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final StandardSmileSurfaceDataBundle object) {
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
      serializer.addToMessage(message, EXPIRIES_FIELD_NAME, null, object.getExpiries());
      serializer.addToMessage(message, STRIKES_FIELD_NAME, null, object.getStrikes());
      serializer.addToMessage(message, VOLS_FIELD_NAME, null, object.getVolatilities());
    }

  }

  @FudgeBuilderFor(ForexSmileDeltaSurfaceDataBundle.class)
  public static final class ForexSmileDeltaSurfaceDataBundleBuilder extends AbstractFudgeBuilder<ForexSmileDeltaSurfaceDataBundle> {
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurveField";
    private static final String EXPIRIES_FIELD_NAME = "expiriesField";
    private static final String STRIKES_FIELD_NAME = "strikesField";
    private static final String VOLS_FIELD_NAME = "volsField";

    @Override
    public ForexSmileDeltaSurfaceDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
      final double[] expiries = deserializer.fieldValueToObject(double[].class, message.getByName(EXPIRIES_FIELD_NAME));
      final double[][] strikes = deserializer.fieldValueToObject(double[][].class, message.getByName(STRIKES_FIELD_NAME));
      final double[][] vols = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLS_FIELD_NAME));
      return new ForexSmileDeltaSurfaceDataBundle(forwardCurve, expiries, strikes, vols, true);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForexSmileDeltaSurfaceDataBundle object) {
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
      serializer.addToMessage(message, EXPIRIES_FIELD_NAME, null, object.getExpiries());
      serializer.addToMessage(message, STRIKES_FIELD_NAME, null, object.getStrikes());
      serializer.addToMessage(message, VOLS_FIELD_NAME, null, object.getVolatilities());
    }

  }
}
