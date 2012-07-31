/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SmileDeltaTermStructureVannaVolgaDataBundleTest {
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.01, 5.00};
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20};
  private static final Pair<Currency, Currency> CCYS = Pair.of(Currency.USD, Currency.EUR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILES = TestsDataSetsForex.smile3points(REFERENCE_DATE, Interpolator1DFactory.LINEAR_INSTANCE);
  private static final SmileDeltaTermStructureVannaVolgaDataBundle FX_DATA = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, CCYS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(null, SMILES, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSmiles() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, null, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCcys() {
    new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, null);
  }

  //  @Test
  public void testObject() {
    assertEquals(FX_DATA.getVolatilityModel(), SMILES);
    SmileDeltaTermStructureVannaVolgaDataBundle other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(TestsDataSetsForex.createCurvesForex(), SMILES, CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, TestsDataSetsForex.smile3points(REFERENCE_DATE, Interpolator1DFactory.LOG_LINEAR_INSTANCE), CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureVannaVolgaDataBundle(CURVES, SMILES, Pair.of(Currency.USD, Currency.GBP));
    assertFalse(FX_DATA.equals(other));
  }

  @Test
  public void testCopy() {
    assertFalse(FX_DATA == FX_DATA.copy());
    assertEquals(FX_DATA, FX_DATA.copy());
  }
}
