/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the construction of multiple currency interest rate sensitivity.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyInterestRateCurveSensitivityTest {

  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final String DSC_USD = "USD Discounting";
  private static final InterestRateCurveSensitivity IRCS_EMPTY = new InterestRateCurveSensitivity();
  private static final Map<String, List<DoublesPair>> SENSI_MAP = new HashMap<>();
  private static final List<DoublesPair> SENSI_LIST_1 = new ArrayList<>();
  static {
    SENSI_LIST_1.add(DoublesPair.of(1.0, 10000.0));
    SENSI_LIST_1.add(DoublesPair.of(2.0, -20000.0));
    SENSI_MAP.put(DSC_USD, SENSI_LIST_1);
  }
  private static final InterestRateCurveSensitivity IRCS = new InterestRateCurveSensitivity(SENSI_MAP);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    MultipleCurrencyInterestRateCurveSensitivity.of(null, IRCS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIRCS() {
    MultipleCurrencyInterestRateCurveSensitivity.of(USD, null);
  }

  @Test
  public void of() {
    final MultipleCurrencyInterestRateCurveSensitivity mcIRCS = MultipleCurrencyInterestRateCurveSensitivity.of(USD, IRCS);
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS, mcIRCS.getSensitivity(USD));
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS_EMPTY, mcIRCS.getSensitivity(EUR));
  }

  @Test
  public void plus() {
    final double tolerance = 1.0E-2;
    MultipleCurrencyInterestRateCurveSensitivity mcIRCS = MultipleCurrencyInterestRateCurveSensitivity.of(USD, IRCS);
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS, mcIRCS.getSensitivity(USD));
    mcIRCS = mcIRCS.plus(EUR, IRCS_EMPTY);
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS, mcIRCS.getSensitivity(USD));
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS_EMPTY, mcIRCS.getSensitivity(EUR));
    mcIRCS = mcIRCS.plus(USD, IRCS);
    AssertSensitivityObjects.assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS.multipliedBy(2.0), mcIRCS.getSensitivity(USD).cleaned(), tolerance);
    assertEquals("MultipleCurrencyInterestRateCurveSensitivity", IRCS_EMPTY, mcIRCS.getSensitivity(EUR));
  }

}
