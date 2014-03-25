/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
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
public class VolatilitySwapGreeksCalculatorTest {

  /**
  *
  */
  @Test
  void sampleDataTest() {
    final CarrLeeFXVolatilitySwapCalculator cal = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapGreeksCalculator calGreeks = new VolatilitySwapGreeksCalculator();

    final double spot = 0.8;
    final double timeToExpiry = 0.49;
    final double timeFromInception = 0.12;
    final double dr = 0.12;
    final double fr = 0.04;
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
    final VolatilitySwapCalculatorResultWithStrikes result = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(cal, data);

    final double[] greeks = calGreeks.getFXVolatilitySwapGreeks(result, swap, data);
    final double[] greeksRe = calGreeks.getFXVolatilitySwapGreeks(swap, data);
    assertEquals(greeksRe[0], greeks[0], 1.e-14);
    assertEquals(greeksRe[1], greeks[1], 1.e-14);
    assertEquals(greeksRe[2], greeks[2], 1.e-14);
    assertEquals(greeksRe[3], greeks[3], 1.e-14);

    final double forward = spot * Math.exp((dr - fr) * timeToExpiry);
    final double[] expGreeks = new double[4];
    for (int i = 0; i < result.getPutWeights().length; ++i) {
      expGreeks[0] += result.getPutWeights()[i] *
          BlackScholesFormulaRepository.delta(spot, result.getPutStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getPutStrikes()[i], forward), dr, dr - fr, false);
      expGreeks[1] += result.getPutWeights()[i] *
          BlackScholesFormulaRepository.gamma(spot, result.getPutStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getPutStrikes()[i], forward), dr, dr - fr);
      expGreeks[2] += result.getPutWeights()[i] *
          BlackScholesFormulaRepository.vega(spot, result.getPutStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getPutStrikes()[i], forward), dr, dr - fr);
      expGreeks[3] += -result.getPutWeights()[i] *
          BlackScholesFormulaRepository.theta(spot, result.getPutStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getPutStrikes()[i], forward), dr, dr - fr, false);
    }
    for (int i = 0; i < result.getCallWeights().length; ++i) {
      expGreeks[0] += result.getCallWeights()[i] *
          BlackScholesFormulaRepository.delta(spot, result.getCallStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getCallStrikes()[i], forward), dr, dr - fr, true);
      expGreeks[1] += result.getCallWeights()[i] *
          BlackScholesFormulaRepository.gamma(spot, result.getCallStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getCallStrikes()[i], forward), dr, dr - fr);
      expGreeks[2] += result.getCallWeights()[i] *
          BlackScholesFormulaRepository.vega(spot, result.getCallStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getCallStrikes()[i], forward), dr, dr - fr);
      expGreeks[3] += -result.getCallWeights()[i] *
          BlackScholesFormulaRepository.theta(spot, result.getCallStrikes()[i], timeToExpiry, smile.getVolatility(timeToExpiry, result.getCallStrikes()[i], forward), dr, dr - fr, true);
    }
    assertEquals(expGreeks[0], greeks[0], Math.abs(greeks[0]) * 1.e-13);
    assertEquals(expGreeks[1], greeks[1], Math.abs(greeks[1]) * 1.e-13);
    assertEquals(expGreeks[2], greeks[2], Math.abs(greeks[2]) * 1.e-13);
    assertEquals(expGreeks[3], greeks[3], Math.abs(greeks[3]) * 1.e-13);

    final FXVolatilitySwap swapNew = new FXVolatilitySwap(0., timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    final FXVolatilitySwap swapSmall = new FXVolatilitySwap(1.e-5, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);
    final VolatilitySwapCalculatorResultWithStrikes resultNew = (VolatilitySwapCalculatorResultWithStrikes) swapNew.accept(cal, data);
    final VolatilitySwapCalculatorResultWithStrikes resultSmall = (VolatilitySwapCalculatorResultWithStrikes) swapSmall.accept(cal, data);
    final double[] greeksNew = calGreeks.getFXVolatilitySwapGreeks(resultNew, swapNew, data);
    final double[] greeksSmall = calGreeks.getFXVolatilitySwapGreeks(resultSmall, swapSmall, data);
    assertEquals(greeksNew[0], greeksSmall[0], 1.e-6);
    assertEquals(greeksNew[1], greeksSmall[1], 1.e-6);
    assertEquals(greeksNew[2], greeksSmall[2], 1.e-6);
    assertEquals(greeksNew[3], greeksSmall[3], 1.e-6);
  }

  /**
  *
  */
  @Test
  public void flatVolSmileTest() {
    final CarrLeeFXVolatilitySwapCalculator cal = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapGreeksCalculator calGreeks = new VolatilitySwapGreeksCalculator();

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

    final VolatilitySwapCalculatorResultWithStrikes result = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(cal, data);

    final double[] greeks = calGreeks.getFXVolatilitySwapGreeks(result, swap, data);
    final double[] greeksRe = calGreeks.getFXVolatilitySwapGreeks(swap, data);
    assertEquals(greeksRe[0], greeks[0], 1.e-14);
    assertEquals(greeksRe[1], greeks[1], 1.e-14);
    assertEquals(greeksRe[2], greeks[2], 1.e-14);
    assertEquals(greeksRe[3], greeks[3], 1.e-14);

    final double[] expGreeks = new double[4];
    for (int i = 0; i < result.getPutWeights().length; ++i) {
      expGreeks[0] += result.getPutWeights()[i] * BlackScholesFormulaRepository.delta(spot, result.getPutStrikes()[i], timeToExpiry, volatility[0][0], 0., 0., false);
      expGreeks[1] += result.getPutWeights()[i] * BlackScholesFormulaRepository.gamma(spot, result.getPutStrikes()[i], timeToExpiry, volatility[0][0], 0., 0.);
      expGreeks[2] += result.getPutWeights()[i] * BlackScholesFormulaRepository.vega(spot, result.getPutStrikes()[i], timeToExpiry, volatility[0][0], 0., 0.);
      expGreeks[3] += -result.getPutWeights()[i] * BlackScholesFormulaRepository.theta(spot, result.getPutStrikes()[i], timeToExpiry, volatility[0][0], 0., 0., false);
    }
    for (int i = 0; i < result.getCallWeights().length; ++i) {
      expGreeks[0] += result.getCallWeights()[i] * BlackScholesFormulaRepository.delta(spot, result.getCallStrikes()[i], timeToExpiry, volatility[0][0], 0., 0., true);
      expGreeks[1] += result.getCallWeights()[i] * BlackScholesFormulaRepository.gamma(spot, result.getCallStrikes()[i], timeToExpiry, volatility[0][0], 0., 0.);
      expGreeks[2] += result.getCallWeights()[i] * BlackScholesFormulaRepository.vega(spot, result.getCallStrikes()[i], timeToExpiry, volatility[0][0], 0., 0.);
      expGreeks[3] += -result.getCallWeights()[i] * BlackScholesFormulaRepository.theta(spot, result.getCallStrikes()[i], timeToExpiry, volatility[0][0], 0., 0., true);
    }
    assertEquals(expGreeks[0], greeks[0], 1.e-14);
    assertEquals(expGreeks[1], greeks[1], 1.e-14);
    assertEquals(expGreeks[2], greeks[2], 1.e-14);
    assertEquals(expGreeks[3], greeks[3], 1.e-14);
  }
}
