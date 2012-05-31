/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParameter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public final class ConstantSpreadFXBlackRolldown implements RolldownFunction<SmileDeltaTermStructureDataBundle> {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  private static final ConstantSpreadFXBlackRolldown INSTANCE = new ConstantSpreadFXBlackRolldown();

  public static ConstantSpreadFXBlackRolldown getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadFXBlackRolldown() {
  }

  @Override
  public SmileDeltaTermStructureDataBundle rollDown(final SmileDeltaTermStructureDataBundle data, final double shiftTime) {
    final YieldCurveBundle shiftedCurves = CURVES_ROLLDOWN.rollDown(data, shiftTime);
    final Pair<Currency, Currency> currencyPair = data.getCurrencyPair();
    final SmileDeltaTermStructureParameter smile = data.getSmile();
    return new SmileDeltaTermStructureDataBundle(data.getFxRates(), data.getCcyMap(), shiftedCurves, smile, currencyPair) {

      @Override
      public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
        if ((ccy1 == currencyPair.getFirst()) && (ccy2 == currencyPair.getSecond())) {
          return smile.getVolatility(time + shiftTime, strike, forward);
        }
        if ((ccy2 == currencyPair.getFirst()) && (ccy1 == currencyPair.getSecond())) {
          return smile.getVolatility(time + shiftTime, 1.0 / strike, 1.0 / forward);
        }
        Validate.isTrue(false, "Currencies not compatible with smile data");
        return 0.0;
      }

      @Override
      public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward, final double[][] bucketSensitivity) {
        if ((ccy1 == currencyPair.getFirst()) && (ccy2 == currencyPair.getSecond())) {
          return smile.getVolatility(time + shiftTime, strike, forward, bucketSensitivity);
        }
        if ((ccy2 == currencyPair.getFirst()) && (ccy1 == currencyPair.getSecond())) {
          return smile.getVolatility(time + shiftTime, 1.0 / strike, 1.0 / forward, bucketSensitivity);
        }
        Validate.isTrue(false, "Currencies not compatible with smile data");
        return 0.0;
      }
    };
  }

}
