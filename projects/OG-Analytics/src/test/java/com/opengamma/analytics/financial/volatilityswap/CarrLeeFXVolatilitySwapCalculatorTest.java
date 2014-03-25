/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
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
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CarrLeeFXVolatilitySwapCalculatorTest {

  /**
   *
   */
  @Test
  void sampleDataTest() {
    final CarrLeeFXVolatilitySwapCalculator cal = new CarrLeeFXVolatilitySwapCalculator();

    final double spot = 0.8;
    final double timeToExpiry = 0.49;
    final double timeFromInception = 0.12;
    final double dr = 0.05;
    final double fr = 0.02;
    final double realizedVar = 6.7 * 6.7;
    final double[] timeToExpiration = new double[] {0.01, 0.1, 0.3 };
    final int nTime = timeToExpiration.length;
    final double[] delta = new double[] {0.10, 0.25 };
    final int nVols = 2 * delta.length + 1;
    final double[][] volatility = new double[nTime][nVols];
    final double[] volSmile = new double[] {9. / 100., 8.1 / 100., 6.9 / 100., 6.45 / 100., 7.22 / 100. };
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(volSmile, 0, volatility[i], 0, nVols);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiration, delta, volatility);
    final Currency base = Currency.EUR;
    final Currency counter = Currency.USD;
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    discountingCurves.put(Currency.EUR, new YieldCurve("domestic", ConstantDoublesCurve.from(dr)));
    discountingCurves.put(Currency.USD, new YieldCurve("foreign", ConstantDoublesCurve.from(fr)));
    final FXMatrix fxMatrix = new FXMatrix(base, counter, spot);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), fxMatrix);
    final CarrLeeFXData data = new CarrLeeFXData(Pairs.of(base, counter), smile, curves, realizedVar);
    final FXVolatilitySwap swap = new FXVolatilitySwap(-timeFromInception, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    assertEquals(6.861525317073218, swap.accept(cal, data).getFairValue(), 1e-10);

    final FXVolatilitySwap swap1 = new FXVolatilitySwap(0., timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    final FXVolatilitySwap swap2 = new FXVolatilitySwap(1.e-4, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    assertEquals(swap1.accept(cal, data).getFairValue(), swap2.accept(cal, data).getFairValue(), 1.e-6);
  }

  /**
   *
   */
  @SuppressWarnings("unused")
  @Test
  public void flatVolSmileTest() {
    final CarrLeeFXVolatilitySwapCalculator cal = new CarrLeeFXVolatilitySwapCalculator();
    final double eps = 1.e-3;

    final double spot = 1.3;
    final double timeToExpiry = 0.24;
    final double timeFromInception = 0.12;
    final double dr = 0.;
    final double fr = 0.;
    final double realizedVar = 6. * 6.;

    final double[] timeToExpiration = new double[] {0.01, 0.1, 0.3 };
    final int nTime = timeToExpiration.length;
    final double[] delta = new double[] {0.10, 0.25 };
    final int nVols = 2 * delta.length + 1;
    final double[][] volatility = new double[nTime][nVols];
    for (int i = 0; i < nTime; ++i) {
      Arrays.fill(volatility[i], 0.06);
    }
    final Currency base = Currency.EUR;
    final Currency counter = Currency.USD;
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeToExpiration, delta, volatility);
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    discountingCurves.put(Currency.EUR, new YieldCurve("domestic", ConstantDoublesCurve.from(dr)));
    discountingCurves.put(Currency.USD, new YieldCurve("foreign", ConstantDoublesCurve.from(fr)));
    final FXMatrix fxMatrix = new FXMatrix(base, counter, spot);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), fxMatrix);
    final CarrLeeFXData data = new CarrLeeFXData(Pairs.of(base, counter), smile, curves, realizedVar);
    final FXVolatilitySwap swap = new FXVolatilitySwap(-timeFromInception, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    assertEquals(6., swap.accept(cal, data).getFairValue(), eps);

    final FXVolatilitySwap swap1 = new FXVolatilitySwap(0., timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    final FXVolatilitySwap swap2 = new FXVolatilitySwap(1.e-4, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    assertEquals(swap1.accept(cal, data).getFairValue(), swap2.accept(cal, data).getFairValue(), 1.e-6);

    /*
     * Error test
     */
    try {
      new CarrLeeFXVolatilitySwapCalculator(0.1, 0.1, 10);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      new CarrLeeFXVolatilitySwapCalculator(-2.1, 0.1, 10);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      new CarrLeeFXVolatilitySwapCalculator(-0.1, -0.1, 10);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      new CarrLeeFXVolatilitySwapCalculator(-0.1, 3.1, 10);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      new CarrLeeFXVolatilitySwapCalculator(-0.1, 0.1, 1);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    //    try {
    //      cal.fairValueSeasoned(spot, -timeToExpiry, timeFromInception, dr, fr, realizedVar, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    //    try {
    //      cal.fairValueSeasoned(spot, timeToExpiry, -timeFromInception, dr, fr, realizedVar, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    //    try {
    //      cal.fairValueSeasoned(-spot, timeToExpiry, timeFromInception, dr, fr, realizedVar, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    //    try {
    //      cal.fairValueSeasoned(spot, timeToExpiry, timeFromInception, dr, fr, -realizedVar, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }

    //    try {
    //      cal.fairValueNew(spot, -timeToExpiry, dr, fr, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
    //    try {
    //      cal.fairValueNew(-spot, timeToExpiry, dr, fr, smile);
    //      throw new RuntimeException();
    //    } catch (final Exception e) {
    //      assertTrue(e instanceof IllegalArgumentException);
    //    }
  }

}
