/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Builds an ISDA curve for the passed currency. The function provides a mapping from a Yield Curve contained in
 * the market data environment to the Isda compliant Yield curve. The returned {@link ISDACompliantYieldCurve}
 * is the yield curve used in ISDA compliant analytic credit computations.
 *
 * The Isda compliant yield curve has a requirement to be log-linear interpolated. Thus the original interpolation used
 * to create the curve in the market data environment will be converted to log-linear interpolation.
 *
 */
public class MappingIsdaCompliantYieldCurveFn implements IsdaCompliantYieldCurveFn {


  private final String _multicurveName;

  /**
   * Builds a new instance.
   * @param multicurveName the name of the multicurve
   */
  public MappingIsdaCompliantYieldCurveFn(String multicurveName) {
    _multicurveName = ArgumentChecker.notNull(multicurveName, "multicurveName");
  }

  @Override
  public Result<IsdaYieldCurve> buildIsdaCompliantCurve(Environment env, Currency ccy) {

    Result<MulticurveBundle> bundleResult = env.getMarketDataBundle().get(MulticurveId.of(_multicurveName),
                                                                          MulticurveBundle.class);
    if (bundleResult.isSuccess()) {
      MulticurveBundle multicurve = bundleResult.getValue();
      YieldAndDiscountCurve curve = multicurve.getMulticurveProvider().getCurve(ccy);
      ISDACompliantYieldCurve isdaCompliantYieldCurve = buildCurve(curve);
      IsdaYieldCurve yieldCurve = IsdaYieldCurve.builder()
          .calibratedCurve(isdaCompliantYieldCurve)
          .build();
      return Result.success(yieldCurve);
    } else {
      return Result.failure(bundleResult);
    }
  }

  /**
   * Creates the ISDACompliantYieldCurve from the YieldAndDiscountCurve
   */
  private ISDACompliantYieldCurve buildCurve(YieldAndDiscountCurve curve) {

    Double[] tenors;
    Double[] rates;

    if (curve instanceof YieldCurve) {
      tenors = ((YieldCurve) curve).getCurve().getXData();
      rates = ((YieldCurve) curve).getCurve().getYData();
    } else if (curve instanceof DiscountCurve) {
      tenors = ((DiscountCurve) curve).getCurve().getXData();
      Double[] rawRates = ((DiscountCurve) curve).getCurve().getYData();

      // Convert discount factors
      Double[] zeroRates = new Double[rawRates.length];
      for (int i = 0; i < rawRates.length; ++i) {
        if (tenors[i] <= 0) {
          throw new IllegalArgumentException("Mapping to the ISDA compliant yield curve does not handle time <= 0");
        }
        zeroRates[i] = -Math.log(rawRates[i]) / tenors[i];
      }
      rates = zeroRates;

    } else {
      throw new IllegalArgumentException("Mapping to the ISDA compliant yield curve is supported for the YieldCurve" +
                                             " and DiscountCurve instances of YieldAndDiscountCurve");
    }

    return new ISDACompliantYieldCurve(ArrayUtils.toPrimitive(tenors), ArrayUtils.toPrimitive(rates));
  }

}
