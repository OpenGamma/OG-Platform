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

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.Curve;

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
    private static final String FORWARD_CURVE_FIELD_NAME = "forwardCurve";
    private static final String DRIFT_CURVE_FIELD_NAME = "driftCurve";

    @SuppressWarnings("unchecked")
    @Override
    public ForwardCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Object forwardCurve = deserializer.fieldValueToObject(message.getByName(FORWARD_CURVE_FIELD_NAME));
      final Object driftCurve = deserializer.fieldValueToObject(message.getByName(DRIFT_CURVE_FIELD_NAME));
      return new ForwardCurve((Curve<Double, Double>) forwardCurve, (Curve<Double, Double>) driftCurve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForwardCurve object) {
      final Curve<Double, Double> driftCurve = object.getDriftCurve();
      final Curve<Double, Double> forwardCurve = object.getForwardCurve();
      serializer.addToMessage(message, FORWARD_CURVE_FIELD_NAME, null, substituteObject(forwardCurve));
      serializer.addToMessage(message, DRIFT_CURVE_FIELD_NAME, null, substituteObject(driftCurve));
    }
  }
  
  /**
   * Fudge builder for {@code ForwardCurveYieldImplied}
   */
  @FudgeBuilderFor(ForwardCurveYieldImplied.class)
  public static final class ForwardCurveYieldImpliedBuilder extends AbstractFudgeBuilder<ForwardCurveYieldImplied> {
    private static final String SPOT_FIELD_NAME = "spot";
    private static final String RISK_FREE_FIELD_NAME = "riskFreeCurve";
    private static final String COST_OF_CARRY_FIELD_NAME = "costOfCarryCurve";

    @Override
    public ForwardCurveYieldImplied buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double spot = message.getDouble(SPOT_FIELD_NAME);
      final YieldAndDiscountCurve riskFreeCurve = deserializer.fieldValueToObject(YieldAndDiscountCurve.class, message.getByName(RISK_FREE_FIELD_NAME));
      final YieldAndDiscountCurve costOfCarryCurve = deserializer.fieldValueToObject(YieldAndDiscountCurve.class, message.getByName(COST_OF_CARRY_FIELD_NAME));
      return new ForwardCurveYieldImplied(spot, riskFreeCurve, costOfCarryCurve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ForwardCurveYieldImplied object) {
      serializer.addToMessage(message, SPOT_FIELD_NAME, null, object.getSpot());
      serializer.addToMessage(message, RISK_FREE_FIELD_NAME, null, object.getRiskFreeCurve());
      serializer.addToMessage(message, COST_OF_CARRY_FIELD_NAME, null, object.getCostOfCarryCurve());
    }
  }
  
  /**
   * Fudge builder for {@code ForwardCurveAffineDividends}
   */
  @FudgeBuilderFor(ForwardCurveAffineDividends.class)
  public static final class ForwardCurveAffineDividendsBuilder extends AbstractFudgeBuilder<ForwardCurveAffineDividends> {
    private static final String SPOT_FIELD_NAME = "spot";
    private static final String RISK_FREE_FIELD_NAME = "riskFreeCurve";
    private static final String DIVIDENDS_FIELD_NAME = "dividends";
    
    @Override
    public ForwardCurveAffineDividends buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      final double spot = message.getDouble(SPOT_FIELD_NAME);
      final YieldAndDiscountCurve riskFreeCurve = deserializer.fieldValueToObject(YieldAndDiscountCurve.class, message.getByName(RISK_FREE_FIELD_NAME));
      final AffineDividends dividends = deserializer.fieldValueToObject(AffineDividends.class, message.getByName(DIVIDENDS_FIELD_NAME));
      return new ForwardCurveAffineDividends(spot, riskFreeCurve, dividends);
    }

    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, ForwardCurveAffineDividends object) {
      serializer.addToMessage(message, SPOT_FIELD_NAME, null, object.getSpot());
      serializer.addToMessage(message, RISK_FREE_FIELD_NAME, null, object.getRiskFreeCurve());
      final AffineDividends dividends = object.getDividends();
      serializer.addToMessage(message, DIVIDENDS_FIELD_NAME, null, substituteObject(dividends));
    }
    
  }

}
