/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

/**
 * Test class for all of the Greek calculators based on finite difference method
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySwapFiniteDifferenceGreeksTest {

  /**
  *
  */
  @Test
  public void sampleDataTest() {
    final VolatilitySwapFiniteDifferenceGreeksCalculator cal = new VolatilitySwapFiniteDifferenceGreeksCalculator();

    final double spot = 1.3680000038304;
    final double timeToExpiry = 129. / 252.;
    final double timeFromInception = 0.0;
    final double dr = 0.0;
    final double fr = 0.0;
    final double[] timeToExpiration = new double[] {timeToExpiry * 0.5, timeToExpiry };
    final int nTime = timeToExpiration.length;
    final double[] delta = new double[] {0.10, 0.25 };
    final int nVols = 2 * delta.length + 1;
    final double[][] volatility = new double[nTime][nVols];
    final double[] volSmile = new double[] {8.7 / 100., 7.75 / 100., 7.0 / 100., 6.8 / 100., 6.95 / 100. };
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(volSmile, 0, volatility[i], 0, nVols);
    }
    final Interpolator1D interp = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiration, delta, volatility, interp);
    //    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiration, delta, volatility);
    final Currency base = Currency.EUR;
    final Currency counter = Currency.USD;
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    discountingCurves.put(Currency.EUR, new YieldCurve("domestic", ConstantDoublesCurve.from(dr)));
    discountingCurves.put(Currency.USD, new YieldCurve("foreign", ConstantDoublesCurve.from(fr)));
    final FXMatrix fxMatrix = new FXMatrix(base, counter, spot);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), fxMatrix);
    final CarrLeeFXData data = new CarrLeeFXData(Pairs.of(base, counter), smile, curves);
    final FXVolatilitySwap swap = new FXVolatilitySwap(-timeFromInception, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);

    final double[] greeks = cal.getFXVolatilitySwapGreeks(swap, data);
    final double expDelta = 0.0000000000030515;
    final double expTheta = -2.794370E-07;
    final double expVega = 0.01001;

    assertEquals(expDelta, greeks[0], 1.e-4);
    assertEquals(expVega, greeks[1] * 1.e-2, 1.e-4);
    assertEquals(expTheta, greeks[2], 1.e-3);
    assertTrue(expTheta * greeks[2] > 0.);

    //    final CarrLeeFXVolatilitySwapCalculator calbr = new CarrLeeFXVolatilitySwapCalculator();
    //    System.out.println(swap.accept(calbr, data).getFairValue());

    /**
     * Consistency checked
     */
    final CarrLeeFXVolatilitySwapDeltaCalculator calDelta = new CarrLeeFXVolatilitySwapDeltaCalculator();
    final CarrLeeFXVolatilitySwapVegaCalculator calVega = new CarrLeeFXVolatilitySwapVegaCalculator();
    final CarrLeeFXVolatilitySwapThetaCalculator calTheta = new CarrLeeFXVolatilitySwapThetaCalculator();

    assertEquals(swap.accept(calDelta, data), greeks[0], 1.e-12);
    assertEquals(swap.accept(calVega, data), greeks[1], 1.e-12);
    assertEquals(swap.accept(calTheta, data), greeks[2], 1.e-12);

    //    System.out.println(greeks[0]);
    //    System.out.println(greeks[1]);
    //    System.out.println(greeks[2]);
  }
}
