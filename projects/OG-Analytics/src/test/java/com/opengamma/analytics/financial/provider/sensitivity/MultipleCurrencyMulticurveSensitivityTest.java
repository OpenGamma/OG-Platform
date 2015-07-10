/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the MultipleCurrencyCurveSensitivityMarket class.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyMulticurveSensitivityTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 10d), DoublesPair.of(2d, 20d), DoublesPair.of(3d, 30d), DoublesPair.of(4d, 40d) });
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {DoublesPair.of(1d, 40d), DoublesPair.of(2d, 30d), DoublesPair.of(3d, 20d), DoublesPair.of(4d, 10d) });
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {DoublesPair.of(11d, 40d), DoublesPair.of(12d, 30d), DoublesPair.of(13d, 20d), DoublesPair.of(14d, 10d) });
  private static final List<ForwardSensitivity> SENSI_FWD_1 = new ArrayList<>();
  static {
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(0.5, 0.75, 0.26, 11));
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(0.75, 1.00, 0.26, 12));
    SENSI_FWD_1.add(new SimplyCompoundedForwardSensitivity(1.00, 1.25, 0.24, 13));
  }
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";

  private static final Map<String, List<DoublesPair>> SENSI_11 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSI_12 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSI_22 = new HashMap<>();
  private static final Map<String, List<DoublesPair>> SENSI_33 = new HashMap<>();
  private static final Map<String, List<ForwardSensitivity>> SENSI_FWD_11 = new HashMap<>();
  static {
    SENSI_11.put(CURVE_NAME_1, SENSI_DATA_1);
    SENSI_22.put(CURVE_NAME_2, SENSI_DATA_2);
    SENSI_12.put(CURVE_NAME_1, SENSI_DATA_2);
    SENSI_33.put(CURVE_NAME_3, SENSI_DATA_3);
    SENSI_FWD_11.put(CURVE_NAME_2, SENSI_FWD_1);
  }

  private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCcy() {
    MultipleCurrencyMulticurveSensitivity.of(null, new MulticurveSensitivity());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSensi() {
    MultipleCurrencyMulticurveSensitivity.of(Currency.AUD, null);
  }

  @Test
  public void of() {
    final MulticurveSensitivity cs = MulticurveSensitivity.of(SENSI_11, SENSI_FWD_11);
    final Currency ccy1 = Currency.AUD;
    final MultipleCurrencyMulticurveSensitivity mcs = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: of", cs, mcs.getSensitivity(ccy1));
    MultipleCurrencyMulticurveSensitivity constructor = new MultipleCurrencyMulticurveSensitivity();
    constructor = constructor.plus(ccy1, cs);
    AssertSensitivityObjects.assertEquals("MultipleCurrencyCurveSensitivityMarket: of", mcs.cleaned(), constructor.cleaned(), TOLERANCE);
    AssertSensitivityObjects.assertEquals("MultipleCurrencyCurveSensitivityMarket: getSensitivity", new MulticurveSensitivity(), mcs.getSensitivity(Currency.CAD), TOLERANCE);
  }

  @Test
  public void plusMultipliedBy() {
    final Currency ccy1 = Currency.AUD;
    final Currency ccy2 = Currency.CAD;
    final MulticurveSensitivity cs = MulticurveSensitivity.of(SENSI_11, SENSI_FWD_11);
    MultipleCurrencyMulticurveSensitivity mcs = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs);
    final MulticurveSensitivity cs2 = MulticurveSensitivity.ofYieldDiscounting(SENSI_22);
    final MultipleCurrencyMulticurveSensitivity mcs2 = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs2);
    final MultipleCurrencyMulticurveSensitivity mcs3 = mcs.plus(mcs2);
    final Map<String, List<DoublesPair>> sum = InterestRateCurveSensitivityUtils.addSensitivity(SENSI_11, SENSI_22);
    final MultipleCurrencyMulticurveSensitivity mcs3Expected = MultipleCurrencyMulticurveSensitivity.of(ccy1, MulticurveSensitivity.of(sum, SENSI_FWD_11));
    AssertSensitivityObjects.assertEquals("", mcs3Expected.cleaned(), mcs3.cleaned(), TOLERANCE);
    mcs = mcs.plus(ccy2, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: plusMultipliedBy", cs, mcs.getSensitivity(ccy1));
    assertEquals("MultipleCurrencyCurveSensitivityMarket: plusMultipliedBy", cs, mcs.getSensitivity(ccy2));
    AssertSensitivityObjects.assertEquals("", mcs.plus(mcs).cleaned(), mcs.multipliedBy(2.0).cleaned(), TOLERANCE);
  }

  @Test
  public void cleaned() {
    final Currency ccy1 = Currency.AUD;
    final Currency ccy2 = Currency.CAD;
    final MulticurveSensitivity cs1 = MulticurveSensitivity.of(SENSI_11, SENSI_FWD_11);
    final MulticurveSensitivity cs2 = MulticurveSensitivity.of(SENSI_22, SENSI_FWD_11);
    MultipleCurrencyMulticurveSensitivity mcs1 = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs1);
    mcs1 = mcs1.plus(ccy2, cs2);
    MultipleCurrencyMulticurveSensitivity mcs2 = MultipleCurrencyMulticurveSensitivity.of(ccy2, cs2);
    mcs2 = mcs2.plus(ccy1, cs1);
    AssertSensitivityObjects.assertEquals("MultipleCurrencyCurveSensitivityMarket: cleaned", mcs1.cleaned(), mcs2.cleaned(), TOLERANCE);
  }

  @Test
  public void converted() {
    final Currency ccy1 = Currency.EUR;
    final Currency ccy2 = Currency.USD;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, 1.25);
    final MulticurveSensitivity cs = MulticurveSensitivity.of(SENSI_11, SENSI_FWD_11);
    final MultipleCurrencyMulticurveSensitivity mcs = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs);
    final MultipleCurrencyMulticurveSensitivity mcsConverted = mcs.converted(ccy2, fxMatrix);
    final MultipleCurrencyMulticurveSensitivity mcsExpected = MultipleCurrencyMulticurveSensitivity.of(ccy2, cs.multipliedBy(fxMatrix.getFxRate(ccy1, ccy2)));
    AssertSensitivityObjects.assertEquals("MultipleCurrencyCurveSensitivityMarket: converted", mcsExpected.cleaned(), mcsConverted.cleaned(), TOLERANCE);
  }

  @Test
  public void equalHash() {
    final Currency ccy1 = Currency.EUR;
    final Currency ccy2 = Currency.USD;
    final MulticurveSensitivity cs = MulticurveSensitivity.of(SENSI_11, SENSI_FWD_11);
    final MultipleCurrencyMulticurveSensitivity mcs = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs, mcs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs.hashCode(), mcs.hashCode());
    assertFalse("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs.equals(null));
    assertFalse("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs.equals(SENSI_11));
    final MultipleCurrencyMulticurveSensitivity other = MultipleCurrencyMulticurveSensitivity.of(ccy1, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs, other);
    MultipleCurrencyMulticurveSensitivity modified;
    modified = MultipleCurrencyMulticurveSensitivity.of(ccy2, cs);
    assertFalse("MultipleCurrencyCurveSensitivityMarket: equalHash", mcs.equals(modified));
  }

}
