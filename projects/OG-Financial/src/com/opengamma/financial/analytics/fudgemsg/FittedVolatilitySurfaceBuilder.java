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
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.MoneynessPiecewiseSABRSurfaceFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
/* package */ final class FittedVolatilitySurfaceBuilder {

  private FittedVolatilitySurfaceBuilder() {
  }

  @FudgeBuilderFor(MoneynessPiecewiseSABRSurfaceFitter.class)
  public static final class MoneynessPiecewiseSABRSurfaceFitterBuilder extends AbstractFudgeBuilder<MoneynessPiecewiseSABRSurfaceFitter> {
    private static final String LOG_TIME_FIELD_NAME = "logTimeField";
    private static final String INTEGRATED_VARIANCE_FIELD_NAME = "integratedVarianceField";
    private static final String LOG_VALUE_FIELD_NAME = "logValueField";

    @Override
    public MoneynessPiecewiseSABRSurfaceFitter buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final boolean useLogTime = message.getBoolean(LOG_TIME_FIELD_NAME);
      final boolean useIntegratedVariance = message.getBoolean(INTEGRATED_VARIANCE_FIELD_NAME);
      final boolean useLogValue = message.getBoolean(LOG_VALUE_FIELD_NAME);
      return new MoneynessPiecewiseSABRSurfaceFitter(useLogTime, useIntegratedVariance, useLogValue);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MoneynessPiecewiseSABRSurfaceFitter object) {
      message.add(LOG_TIME_FIELD_NAME, null, object.useLogTime());
      message.add(INTEGRATED_VARIANCE_FIELD_NAME, null, object.useIntegratedVariance());
      message.add(LOG_VALUE_FIELD_NAME, null, object.useLogValue());
    }
  }

  @FudgeBuilderFor(StandardSmileSurfaceDataBundle.class)
  public static final class StandardSmileSurfaceDataBundleBuilder extends AbstractFudgeBuilder<StandardSmileSurfaceDataBundle> {
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurveField";
    private static final String EXPIRIES_FIELD_NAME = "expiriesField";
    private static final String STRIKES_FIELD_NAME = "strikesField";
    private static final String VOLS_FIELD_NAME = "volsField";
    private static final String IS_CALL_FIELD_NAME = "isCallField";

    @Override
    public StandardSmileSurfaceDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
      final double[] expiries = deserializer.fieldValueToObject(double[].class, message.getByName(EXPIRIES_FIELD_NAME));
      final double[][] strikes = deserializer.fieldValueToObject(double[][].class, message.getByName(STRIKES_FIELD_NAME));
      final double[][] vols = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLS_FIELD_NAME));
      final boolean isCallData = message.getBoolean(IS_CALL_FIELD_NAME);
      return new StandardSmileSurfaceDataBundle(forwardCurve, expiries, strikes, vols, isCallData);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final StandardSmileSurfaceDataBundle object) {
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
      serializer.addToMessage(message, EXPIRIES_FIELD_NAME, null, object.getExpiries());
      serializer.addToMessage(message, STRIKES_FIELD_NAME, null, object.getStrikes());
      serializer.addToMessage(message, VOLS_FIELD_NAME, null, object.getVolatilities());
      serializer.addToMessage(message, IS_CALL_FIELD_NAME, null, object.isCallData());
    }

  }

  @FudgeBuilderFor(ForexSmileDeltaSurfaceDataBundle.class)
  public static final class ForexSmileDeltaSurfaceDataBundleBuilder extends AbstractFudgeBuilder<ForexSmileDeltaSurfaceDataBundle> {
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurveField";
    private static final String EXPIRIES_FIELD_NAME = "expiriesField";
    private static final String STRIKES_FIELD_NAME = "strikesField";
    private static final String VOLS_FIELD_NAME = "volsField";
    private static final String IS_CALL_FIELD_NAME = "isCallField";

    @Override
    public ForexSmileDeltaSurfaceDataBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByName(FORWARD_CURVE_FIELD_NAME));
      final double[] expiries = deserializer.fieldValueToObject(double[].class, message.getByName(EXPIRIES_FIELD_NAME));
      final double[][] strikes = deserializer.fieldValueToObject(double[][].class, message.getByName(STRIKES_FIELD_NAME));
      final double[][] vols = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLS_FIELD_NAME));
      final boolean isCallData = message.getBoolean(IS_CALL_FIELD_NAME);
      return new ForexSmileDeltaSurfaceDataBundle(forwardCurve, expiries, strikes, vols, isCallData);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForexSmileDeltaSurfaceDataBundle object) {
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, object.getForwardCurve());
      serializer.addToMessage(message, EXPIRIES_FIELD_NAME, null, object.getExpiries());
      serializer.addToMessage(message, STRIKES_FIELD_NAME, null, object.getStrikes());
      serializer.addToMessage(message, VOLS_FIELD_NAME, null, object.getVolatilities());
      serializer.addToMessage(message, IS_CALL_FIELD_NAME, null, object.isCallData());
    }

  }
}
