/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
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
  public void newlyIssuedSwapTest() {

    final double spot = 1.5;
    final double timeToExpiry = 0.5;
    final double timeFromInception = 0.;
    final double dr = 0.01;
    final double fr = 0.005;

    final double bump = 1.e-5;
    final double bumpVol = 1.e-7;

    final CarrLeeFXVolatilitySwapCalculator baseCal = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapFiniteDifferenceGreeksCalculator cal = new VolatilitySwapFiniteDifferenceGreeksCalculator();

    final CarrLeeFXVolatilitySwapCalculator baseCalRange = new CarrLeeFXVolatilitySwapCalculator(40, new double[] {0.8 * spot, 1.2 * spot });
    final VolatilitySwapFiniteDifferenceGreeksCalculator calRange = new VolatilitySwapFiniteDifferenceGreeksCalculator(bump, baseCalRange);

    final double[] timeSet = new double[] {timeToExpiry * 0.5, timeToExpiry };
    final int nTime = timeSet.length;
    final double[] delta = new double[] {0.10, 0.25 };
    final int nVols = 2 * delta.length + 1;
    final double[][] volatility = new double[nTime][nVols];
    final double[] volSmile = new double[] {9. / 100., 8. / 100., 7.5 / 100., 7.2 / 100., 7.85 / 100. };
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(volSmile, 0, volatility[i], 0, nVols);
    }
    final Interpolator1D interp = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeSet, delta, volatility, interp);
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
    final double[] greeksRange = calRange.getFXVolatilitySwapGreeks(swap, data);

    /**
     * Tests with bumped data
     */
    final VolatilitySwapCalculatorResultWithStrikes baseResult = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(baseCal, data);
    final double baseFV = baseResult.getFairValue();
    final VolatilitySwapCalculatorResultWithStrikes baseResultRange = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(baseCalRange, data);
    final double baseFVRange = baseResultRange.getFairValue();

    final double bumpedSpot = spot + bump;
    final FXMatrix bumpedFxMatrix = new FXMatrix(base, counter, bumpedSpot);
    final MulticurveProviderDiscount spotBumpedCurves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), bumpedFxMatrix);
    final CarrLeeFXData spotBumpedData = new CarrLeeFXData(Pairs.of(base, counter), smile, spotBumpedCurves);
    final double spotBumpedFV = swap.accept(baseCal, spotBumpedData).getFairValue();
    final double spotBumpedFVRange = swap.accept(baseCalRange, spotBumpedData).getFairValue();

    final double[][] bumpedVolatility = new double[nTime][nVols];
    final double[] bumpedVolSmile = new double[nVols];
    for (int i = 0; i < nVols; ++i) {
      /*
       * Note interpolation is linear, but strike range affected
       */
      bumpedVolSmile[i] = volSmile[i] + bumpVol;
    }
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(bumpedVolSmile, 0, bumpedVolatility[i], 0, nVols);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation volBumpedSmile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeSet, delta, bumpedVolatility, interp);
    final CarrLeeFXData volBumpedData = new CarrLeeFXData(Pairs.of(base, counter), volBumpedSmile, curves);
    final double volBumpedFV = swap.accept(baseCal, volBumpedData).getFairValue();
    final double volBumpedFVRange = swap.accept(baseCalRange, volBumpedData).getFairValue();

    final double bumpedTimeToExpiry = timeToExpiry - 1.0 / 252.0;
    final FXVolatilitySwap timeBumpedSwap = new FXVolatilitySwap(-timeFromInception, bumpedTimeToExpiry, PeriodFrequency.DAILY, bumpedTimeToExpiry, spot, 1, base, base, counter, 252);
    final double timeBumpedFV = timeBumpedSwap.accept(baseCal, data).getFairValue();
    final double timeBumpedFVRange = timeBumpedSwap.accept(baseCalRange, data).getFairValue();

    assertEquals((spotBumpedFV - baseFV) / bump, greeks[0], 1.e-12);
    assertEquals((volBumpedFV - baseFV) / bumpVol / 100., greeks[1], 1.e-2);//Approximation
    assertEquals(timeBumpedFV - baseFV, greeks[2], 1.e-12);

    assertEquals((spotBumpedFVRange - baseFVRange) / bump, greeksRange[0], 1.e-12);
    assertEquals((volBumpedFVRange - baseFVRange) / bumpVol / 100., greeksRange[1], 1.e-2);//Approximation
    assertEquals(timeBumpedFVRange - baseFVRange, greeksRange[2], 1.e-12);

    /**
     * Consistency with separate methods
     */
    final CarrLeeFXVolatilitySwapDeltaCalculator del = new CarrLeeFXVolatilitySwapDeltaCalculator();
    final CarrLeeFXVolatilitySwapVegaCalculator veg = new CarrLeeFXVolatilitySwapVegaCalculator();
    final CarrLeeFXVolatilitySwapThetaCalculator the = new CarrLeeFXVolatilitySwapThetaCalculator();
    assertEquals(swap.accept(del, data), greeks[0], 1.e-12);
    assertEquals(swap.accept(veg, data), greeks[1], 1.e-12);
    assertEquals(swap.accept(the, data), greeks[2], 1.e-12);

    final CarrLeeFXVolatilitySwapDeltaCalculator delRange = new CarrLeeFXVolatilitySwapDeltaCalculator(bump, baseCalRange);
    final CarrLeeFXVolatilitySwapVegaCalculator vegRange = new CarrLeeFXVolatilitySwapVegaCalculator(bumpVol, baseCalRange);
    final CarrLeeFXVolatilitySwapThetaCalculator theRange = new CarrLeeFXVolatilitySwapThetaCalculator(baseCalRange);
    assertEquals(swap.accept(delRange, data), greeksRange[0], 1.e-12);
    assertEquals(swap.accept(vegRange, data), greeksRange[1], 1.e-12);
    assertEquals(swap.accept(theRange, data), greeksRange[2], 1.e-12);

  }

  /**
   * 
   */
  public void SeasonedSwapTest() {

    final double spot = 1.5;
    final double timeToExpiry = 0.5;
    final double timeFromInception = 0.25;
    final double dr = 0.01;
    final double fr = 0.005;
    final double rv = 8.1 * 8.1;

    final double bump = 1.e-5;
    final double bumpVol = 1.e-7;

    final CarrLeeFXVolatilitySwapCalculator baseCal = new CarrLeeFXVolatilitySwapCalculator();
    final VolatilitySwapFiniteDifferenceGreeksCalculator cal = new VolatilitySwapFiniteDifferenceGreeksCalculator();

    final CarrLeeFXVolatilitySwapCalculator baseCalRange = new CarrLeeFXVolatilitySwapCalculator(40, new double[] {0.8 * spot, 1.2 * spot });
    final VolatilitySwapFiniteDifferenceGreeksCalculator calRange = new VolatilitySwapFiniteDifferenceGreeksCalculator(bump, baseCalRange);

    final double[] timeSet = new double[] {timeToExpiry * 0.5, timeToExpiry };
    final int nTime = timeSet.length;
    final double[] delta = new double[] {0.10, 0.25 };
    final int nVols = 2 * delta.length + 1;
    final double[][] volatility = new double[nTime][nVols];
    final double[] volSmile = new double[] {9. / 100., 8. / 100., 7.5 / 100., 7.2 / 100., 7.85 / 100. };
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(volSmile, 0, volatility[i], 0, nVols);
    }
    final Interpolator1D interp = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final SmileDeltaTermStructureParametersStrikeInterpolation smile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeSet, delta, volatility, interp);
    final Currency base = Currency.EUR;
    final Currency counter = Currency.USD;
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    discountingCurves.put(Currency.EUR, new YieldCurve("domestic", ConstantDoublesCurve.from(dr)));
    discountingCurves.put(Currency.USD, new YieldCurve("foreign", ConstantDoublesCurve.from(fr)));
    final FXMatrix fxMatrix = new FXMatrix(base, counter, spot);
    final MulticurveProviderDiscount curves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), fxMatrix);
    final CarrLeeFXData data = new CarrLeeFXData(Pairs.of(base, counter), smile, curves, rv);
    final FXVolatilitySwap swap = new FXVolatilitySwap(-timeFromInception, timeToExpiry, PeriodFrequency.DAILY, timeToExpiry, spot, 1, base, base, counter, 252);

    final double[] greeks = cal.getFXVolatilitySwapGreeks(swap, data);
    final double[] greeksRange = calRange.getFXVolatilitySwapGreeks(swap, data);

    /**
     * Tests with bumped data
     */
    final VolatilitySwapCalculatorResultWithStrikes baseResult = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(baseCal, data);
    final double baseFV = baseResult.getFairValue();
    final VolatilitySwapCalculatorResultWithStrikes baseResultRange = (VolatilitySwapCalculatorResultWithStrikes) swap.accept(baseCalRange, data);
    final double baseFVRange = baseResultRange.getFairValue();

    final double bumpedSpot = spot + bump;
    final FXMatrix bumpedFxMatrix = new FXMatrix(base, counter, bumpedSpot);
    final MulticurveProviderDiscount spotBumpedCurves = new MulticurveProviderDiscount(discountingCurves, new LinkedHashMap<IborIndex, YieldAndDiscountCurve>(),
        new LinkedHashMap<IndexON, YieldAndDiscountCurve>(), bumpedFxMatrix);
    final CarrLeeFXData spotBumpedData = new CarrLeeFXData(Pairs.of(base, counter), smile, spotBumpedCurves, rv);
    final double spotBumpedFV = swap.accept(baseCal, spotBumpedData).getFairValue();
    final double spotBumpedFVRange = swap.accept(baseCalRange, spotBumpedData).getFairValue();

    final double[][] bumpedVolatility = new double[nTime][nVols];
    final double[] bumpedVolSmile = new double[nVols];
    for (int i = 0; i < nVols; ++i) {
      /*
       * Note interpolation is linear, but strike range affected
       */
      bumpedVolSmile[i] = volSmile[i] + bumpVol;
    }
    for (int i = 0; i < nTime; ++i) {
      System.arraycopy(bumpedVolSmile, 0, bumpedVolatility[i], 0, nVols);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation volBumpedSmile = new SmileDeltaTermStructureParametersStrikeInterpolation(timeSet, delta, bumpedVolatility, interp);
    final CarrLeeFXData volBumpedData = new CarrLeeFXData(Pairs.of(base, counter), volBumpedSmile, curves, rv);
    final double volBumpedFV = swap.accept(baseCal, volBumpedData).getFairValue();
    final double volBumpedFVRange = swap.accept(baseCalRange, volBumpedData).getFairValue();

    final double bumpedTimeToExpiry = timeToExpiry - 1.0 / 252.0;
    final FXVolatilitySwap timeBumpedSwap = new FXVolatilitySwap(-timeFromInception - 1.0 / 252.0, bumpedTimeToExpiry, PeriodFrequency.DAILY, bumpedTimeToExpiry, spot, 1, base, base, counter, 252);
    final double timeBumpedFV = timeBumpedSwap.accept(baseCal, data).getFairValue();
    final double timeBumpedFVRange = timeBumpedSwap.accept(baseCalRange, data).getFairValue();

    assertEquals((spotBumpedFV - baseFV) / bump, greeks[0], 1.e-12);
    assertEquals((volBumpedFV - baseFV) / bumpVol / 100., greeks[1], 1.e-1);//Approximation
    assertEquals(timeBumpedFV - baseFV, greeks[2], 1.e-12);

    assertEquals((spotBumpedFVRange - baseFVRange) / bump, greeksRange[0], 1.e-12);
    assertEquals((volBumpedFVRange - baseFVRange) / bumpVol / 100., greeksRange[1], 1.e-1);//Approximation
    assertEquals(timeBumpedFVRange - baseFVRange, greeksRange[2], 1.e-12);

    /**
     * Consistency with separate methods
     */
    final CarrLeeFXVolatilitySwapDeltaCalculator del = new CarrLeeFXVolatilitySwapDeltaCalculator();
    final CarrLeeFXVolatilitySwapVegaCalculator veg = new CarrLeeFXVolatilitySwapVegaCalculator();
    final CarrLeeFXVolatilitySwapThetaCalculator the = new CarrLeeFXVolatilitySwapThetaCalculator();
    assertEquals(swap.accept(del, data), greeks[0], 1.e-12);
    assertEquals(swap.accept(veg, data), greeks[1], 1.e-12);
    assertEquals(swap.accept(the, data), greeks[2], 1.e-12);

    final CarrLeeFXVolatilitySwapDeltaCalculator delRange = new CarrLeeFXVolatilitySwapDeltaCalculator(bump, baseCalRange);
    final CarrLeeFXVolatilitySwapVegaCalculator vegRange = new CarrLeeFXVolatilitySwapVegaCalculator(bumpVol, baseCalRange);
    final CarrLeeFXVolatilitySwapThetaCalculator theRange = new CarrLeeFXVolatilitySwapThetaCalculator(baseCalRange);
    assertEquals(swap.accept(delRange, data), greeksRange[0], 1.e-12);
    assertEquals(swap.accept(vegRange, data), greeksRange[1], 1.e-12);
    assertEquals(swap.accept(theRange, data), greeksRange[2], 1.e-12);
  }

  public void hashCodeAndEqualsTest() {

    final CarrLeeFXVolatilitySwapCalculator baseCal = new CarrLeeFXVolatilitySwapCalculator();
    final CarrLeeFXVolatilitySwapCalculator baseCalRange = new CarrLeeFXVolatilitySwapCalculator(40, new double[] {0.8, 1.2 });

    final VolatilitySwapFiniteDifferenceGreeksCalculator greeksDef = new VolatilitySwapFiniteDifferenceGreeksCalculator();
    final VolatilitySwapFiniteDifferenceGreeksCalculator greeksBase = new VolatilitySwapFiniteDifferenceGreeksCalculator(1.e-5);
    final VolatilitySwapFiniteDifferenceGreeksCalculator greeksBaseCal = new VolatilitySwapFiniteDifferenceGreeksCalculator(1.e-5, baseCal);
    final VolatilitySwapFiniteDifferenceGreeksCalculator greeksRangeCal = new VolatilitySwapFiniteDifferenceGreeksCalculator(1.e-5, baseCalRange);
    final VolatilitySwapFiniteDifferenceGreeksCalculator greeksBump = new VolatilitySwapFiniteDifferenceGreeksCalculator(1.e-7);

    assertTrue(greeksDef.equals(greeksDef));

    assertEquals(greeksDef.hashCode(), greeksBase.hashCode());
    assertTrue(greeksDef.equals(greeksBase));
    assertTrue(greeksBase.equals(greeksDef));

    assertEquals(greeksDef.hashCode(), greeksBaseCal.hashCode());
    assertTrue(greeksDef.equals(greeksBaseCal));
    assertTrue(greeksBaseCal.equals(greeksDef));

    assertFalse(greeksDef.equals(greeksRangeCal));
    assertFalse(greeksDef.equals(greeksBump));
    assertFalse(greeksDef.equals(new double[] {}));
    assertFalse(greeksDef.equals(null));

    final CarrLeeFXVolatilitySwapDeltaCalculator delDef = new CarrLeeFXVolatilitySwapDeltaCalculator();
    final CarrLeeFXVolatilitySwapDeltaCalculator delBase = new CarrLeeFXVolatilitySwapDeltaCalculator(1.e-5);
    final CarrLeeFXVolatilitySwapDeltaCalculator delBaseCal = new CarrLeeFXVolatilitySwapDeltaCalculator(1.e-5, baseCal);
    final CarrLeeFXVolatilitySwapDeltaCalculator delRangeCal = new CarrLeeFXVolatilitySwapDeltaCalculator(1.e-5, baseCalRange);
    final CarrLeeFXVolatilitySwapDeltaCalculator delBump = new CarrLeeFXVolatilitySwapDeltaCalculator(1.e-7);

    assertTrue(delDef.equals(delDef));

    assertEquals(delDef.hashCode(), delBase.hashCode());
    assertTrue(delDef.equals(delBase));
    assertTrue(delBase.equals(delDef));

    assertEquals(delDef.hashCode(), delBaseCal.hashCode());
    assertTrue(delDef.equals(delBaseCal));
    assertTrue(delBaseCal.equals(delDef));

    assertFalse(delDef.equals(delRangeCal));
    assertFalse(delDef.equals(delBump));
    assertFalse(delDef.equals(new double[] {}));
    assertFalse(delDef.equals(null));

    final CarrLeeFXVolatilitySwapVegaCalculator vegDef = new CarrLeeFXVolatilitySwapVegaCalculator();
    final CarrLeeFXVolatilitySwapVegaCalculator vegBase = new CarrLeeFXVolatilitySwapVegaCalculator(1.e-7);
    final CarrLeeFXVolatilitySwapVegaCalculator vegBaseCal = new CarrLeeFXVolatilitySwapVegaCalculator(1.e-7, baseCal);
    final CarrLeeFXVolatilitySwapVegaCalculator vegRangeCal = new CarrLeeFXVolatilitySwapVegaCalculator(1.e-7, baseCalRange);
    final CarrLeeFXVolatilitySwapVegaCalculator vegBump = new CarrLeeFXVolatilitySwapVegaCalculator(1.e-5);

    assertTrue(vegDef.equals(vegDef));

    assertEquals(vegDef.hashCode(), vegBase.hashCode());
    assertTrue(vegDef.equals(vegBase));
    assertTrue(vegBase.equals(vegDef));

    assertEquals(vegDef.hashCode(), vegBaseCal.hashCode());
    assertTrue(vegDef.equals(vegBaseCal));
    assertTrue(vegBaseCal.equals(vegDef));

    assertFalse(vegDef.equals(vegRangeCal));
    assertFalse(vegDef.equals(vegBump));
    assertFalse(vegDef.equals(new double[] {}));
    assertFalse(vegDef.equals(null));

    final CarrLeeFXVolatilitySwapThetaCalculator theDef = new CarrLeeFXVolatilitySwapThetaCalculator();
    final CarrLeeFXVolatilitySwapThetaCalculator theBaseCal = new CarrLeeFXVolatilitySwapThetaCalculator(baseCal);
    final CarrLeeFXVolatilitySwapThetaCalculator theRangeCal = new CarrLeeFXVolatilitySwapThetaCalculator(baseCalRange);

    assertTrue(theDef.equals(theDef));

    assertEquals(theDef.hashCode(), theBaseCal.hashCode());
    assertTrue(theDef.equals(theBaseCal));
    assertTrue(theBaseCal.equals(theDef));

    assertFalse(theDef.equals(theRangeCal));
    assertFalse(theDef.equals(new double[] {}));
    assertFalse(theDef.equals(null));
  }
}
