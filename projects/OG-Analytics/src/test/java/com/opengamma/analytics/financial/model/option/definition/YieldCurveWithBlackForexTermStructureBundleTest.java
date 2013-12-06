/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class YieldCurveWithBlackForexTermStructureBundleTest {
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.01, 5.00 };
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20 };
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT);
  private static final BlackForexTermStructureParameters VOLS = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL);
  private static final Pair<Currency, Currency> CCYS = Pairs.of(Currency.EUR, Currency.EUR);
  private static final YieldCurveWithBlackForexTermStructureBundle FX_DATA = new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, CCYS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new YieldCurveWithBlackForexTermStructureBundle(null, VOLS, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols() {
    new YieldCurveWithBlackForexTermStructureBundle(CURVES, null, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencies() {
    new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCurrencyPair() {
    new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, Pairs.of(Currency.AUD, Currency.SEK));
  }

  @Test
  public void testObject() {
    assertEquals(VOLS, FX_DATA.getVolatilityModel());
    YieldCurveWithBlackForexTermStructureBundle other = new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, CCYS);
    assertEquals(other, FX_DATA);
    assertEquals(other.hashCode(), FX_DATA.hashCode());
    other = YieldCurveWithBlackForexTermStructureBundle.from(CURVES, VOLS, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    final Map<String, YieldAndDiscountCurve> otherCurves = new HashMap<>();
    final YieldAndDiscountCurve curve = CURVES.getCurve(CURVES.getAllNames().iterator().next());
    for (final String name : CURVES.getAllNames()) {
      otherCurves.put(name, curve);
    }
    other = new YieldCurveWithBlackForexTermStructureBundle(new YieldCurveBundle(CURVES.getFxRates(), CURVES.getCurrencyMap(), otherCurves), VOLS, CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new YieldCurveWithBlackForexTermStructureBundle(CURVES, new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT)), CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, Pairs.of(Currency.EUR, Currency.GBP));
    assertFalse(FX_DATA.equals(other));
  }

  @Test
  public void testCopy() {
    assertFalse(FX_DATA == FX_DATA.copy());
    assertEquals(FX_DATA, FX_DATA.copy());
  }

  @Test
  public void testBuilders() {
    final YieldCurveWithBlackForexTermStructureBundle fxData = new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, CCYS);
    assertEquals(FX_DATA, fxData);
    YieldCurveWithBlackForexTermStructureBundle other = fxData.with(TestsDataSetsForex.createCurvesForex2());
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
    other = FX_DATA.with(new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT)));
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
  }
}
