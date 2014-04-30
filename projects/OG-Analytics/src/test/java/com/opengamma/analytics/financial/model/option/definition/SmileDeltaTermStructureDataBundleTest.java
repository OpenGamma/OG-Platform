/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SmileDeltaTermStructureDataBundleTest {
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Pair<Currency, Currency> CCYS = Pairs.of(Currency.EUR, Currency.EUR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 13);
  private static final SmileDeltaTermStructureParametersStrikeInterpolation SMILES = TestsDataSetsForex.smile5points(REFERENCE_DATE, 0);
  private static final SmileDeltaTermStructureDataBundle FX_DATA = new SmileDeltaTermStructureDataBundle(CURVES, SMILES, CCYS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new SmileDeltaTermStructureDataBundle(null, SMILES, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSmiles() {
    new SmileDeltaTermStructureDataBundle(CURVES, null, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCcys() {
    new SmileDeltaTermStructureDataBundle(CURVES, SMILES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCurrencyPair() {
    new SmileDeltaTermStructureDataBundle(CURVES, SMILES, Pairs.of(Currency.AUD, Currency.SEK));
  }

  @Test
  public void testObject() {
    assertEquals(FX_DATA.getVolatilityModel(), SMILES);
    assertEquals(FX_DATA.getCurrencyPair(), CCYS);
    SmileDeltaTermStructureDataBundle other = new SmileDeltaTermStructureDataBundle(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    other = SmileDeltaTermStructureDataBundle.from(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, other);
    assertEquals(FX_DATA.hashCode(), other.hashCode());
    other = new SmileDeltaTermStructureDataBundle(TestsDataSetsForex.createCurvesForex2(), SMILES, CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureDataBundle(CURVES, TestsDataSetsForex.smile5points(REFERENCE_DATE, 1), CCYS);
    assertFalse(FX_DATA.equals(other));
    other = new SmileDeltaTermStructureDataBundle(CURVES, SMILES, Pairs.of(Currency.EUR, Currency.GBP));
    assertFalse(FX_DATA.equals(other));
  }

  @Test
  public void testCopy() {
    assertFalse(FX_DATA == FX_DATA.copy());
    assertEquals(FX_DATA, FX_DATA.copy());
  }

  @Test
  public void testBuilders() {
    final SmileDeltaTermStructureDataBundle fxData = new SmileDeltaTermStructureDataBundle(CURVES, SMILES, CCYS);
    assertEquals(FX_DATA, fxData);
    SmileDeltaTermStructureDataBundle other = fxData.with(TestsDataSetsForex.createCurvesForex2());
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
    other = FX_DATA.with(TestsDataSetsForex.smile5points(REFERENCE_DATE, 0.1));
    assertEquals(FX_DATA, fxData);
    assertFalse(other.equals(fxData));
  }
}
