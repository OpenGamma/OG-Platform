/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 * 
 */
/* package */ class ForexOptionDataBundleBuilders {

  @FudgeBuilderFor(SmileDeltaTermStructureParametersStrikeInterpolation.class)
  public static class SmileDeltaTermStructureParameterStrikeInterpolationBuilder extends AbstractFudgeBuilder<SmileDeltaTermStructureParametersStrikeInterpolation> {
    private static final String T_DATA_FIELD_NAME = "Time data";
    private static final String DELTA_DATA_FIELD_NAME = "Delta data";
    private static final String VOLATILITY_DATA_FIELD_NAME = "Volatility data";
    private static final String TIME_INTERPOLATOR_NAME = "Time Interpolator";
    private static final String STRIKE_INTERPOLATOR_NAME = "Strike Interpolator";

    @Override
    public SmileDeltaTermStructureParametersStrikeInterpolation buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_DATA_FIELD_NAME));
      final double[][] delta = deserializer.fieldValueToObject(double[][].class, message.getByName(DELTA_DATA_FIELD_NAME));
      final double[][] volatility = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLATILITY_DATA_FIELD_NAME));
      final int n = t.length;
      final SmileDeltaParameters[] smiles = new SmileDeltaParameters[n];
      for (int i = 0; i < n; i++) {
        smiles[i] = new SmileDeltaParameters(t[i], delta[i], volatility[i]);
      }
      final Interpolator1D strikeInterpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(STRIKE_INTERPOLATOR_NAME));
      final Interpolator1D timeInterpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(TIME_INTERPOLATOR_NAME));
      return new SmileDeltaTermStructureParametersStrikeInterpolation(smiles, strikeInterpolator, timeInterpolator);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileDeltaTermStructureParametersStrikeInterpolation object) {
      final SmileDeltaParameters[] smiles = object.getVolatilityTerm();
      final int n = smiles.length;
      final double[] t = new double[n];
      final double[][] delta = new double[n][];
      final double[][] volatility = new double[n][];
      for (int i = 0; i < n; i++) {
        t[i] = smiles[i].getTimeToExpiry();
        delta[i] = smiles[i].getDelta();
        volatility[i] = smiles[i].getVolatility();
      }
      serializer.addToMessage(message, T_DATA_FIELD_NAME, null, t);
      serializer.addToMessage(message, DELTA_DATA_FIELD_NAME, null, delta);
      serializer.addToMessage(message, VOLATILITY_DATA_FIELD_NAME, null, volatility);
      serializer.addToMessage(message, STRIKE_INTERPOLATOR_NAME, null, object.getStrikeInterpolator());
      serializer.addToMessage(message, TIME_INTERPOLATOR_NAME, null, object.getTimeInterpolator());
    }
  }

  @FudgeBuilderFor(SmileDeltaTermStructureParameters.class)
  public static class SmileDeltaTermStructureParameterBuilder extends AbstractFudgeBuilder<SmileDeltaTermStructureParameters> {
    private static final String T_DATA_FIELD_NAME = "Time data";
    private static final String DELTA_DATA_FIELD_NAME = "Delta data";
    private static final String VOLATILITY_DATA_FIELD_NAME = "Volatility data";
    private static final String TIME_INTERPOLATOR_NAME = "Time Interpolator";

    @Override
    public SmileDeltaTermStructureParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_DATA_FIELD_NAME));
      final double[][] delta = deserializer.fieldValueToObject(double[][].class, message.getByName(DELTA_DATA_FIELD_NAME));
      final double[][] volatility = deserializer.fieldValueToObject(double[][].class, message.getByName(VOLATILITY_DATA_FIELD_NAME));
      final int n = t.length;
      final SmileDeltaParameters[] smiles = new SmileDeltaParameters[n];
      for (int i = 0; i < n; i++) {
        smiles[i] = new SmileDeltaParameters(t[i], delta[i], volatility[i]);
      }
      final Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(TIME_INTERPOLATOR_NAME));
      return new SmileDeltaTermStructureParameters(smiles, interpolator);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final SmileDeltaTermStructureParameters object) {
      final SmileDeltaParameters[] smiles = object.getVolatilityTerm();
      final int n = smiles.length;
      final double[] t = new double[n];
      final double[][] delta = new double[n][];
      final double[][] volatility = new double[n][];
      for (int i = 0; i < n; i++) {
        t[i] = smiles[i].getTimeToExpiry();
        delta[i] = smiles[i].getDelta();
        volatility[i] = smiles[i].getVolatility();
      }
      serializer.addToMessage(message, T_DATA_FIELD_NAME, null, t);
      serializer.addToMessage(message, DELTA_DATA_FIELD_NAME, null, delta);
      serializer.addToMessage(message, VOLATILITY_DATA_FIELD_NAME, null, volatility);
      serializer.addToMessage(message, TIME_INTERPOLATOR_NAME, null, object.getTimeInterpolator());
    }
  }

  @FudgeBuilderFor(BlackForexTermStructureParameters.class)
  public static class BlackForexTermStructureParametersBuilder extends AbstractFudgeBuilder<BlackForexTermStructureParameters> {
    private static final String VOLATILITY_DATA_FIELD_NAME = "Volatility data";

    @Override
    public BlackForexTermStructureParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve volatility = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(VOLATILITY_DATA_FIELD_NAME));
      return new BlackForexTermStructureParameters(volatility);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final BlackForexTermStructureParameters object) {
      serializer.addToMessage(message, VOLATILITY_DATA_FIELD_NAME, null, object.getVolatilityCurve());
    }
  }
}
