/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

public class MultipleCurrencyCurveSensitivityMarketTest {

  private static final List<DoublesPair> SENSI_DATA_1 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 10), new DoublesPair(2, 20), new DoublesPair(3, 30), new DoublesPair(4, 40)});
  private static final List<DoublesPair> SENSI_DATA_2 = Arrays.asList(new DoublesPair[] {new DoublesPair(1, 40), new DoublesPair(2, 30), new DoublesPair(3, 20), new DoublesPair(4, 10)});
  private static final List<DoublesPair> SENSI_DATA_3 = Arrays.asList(new DoublesPair[] {new DoublesPair(11, 40), new DoublesPair(12, 30), new DoublesPair(13, 20), new DoublesPair(14, 10)});
  private static final List<MarketForwardSensitivity> SENSI_FWD_1 = new ArrayList<MarketForwardSensitivity>();
  static {
    SENSI_FWD_1.add(new MarketForwardSensitivity(0.5, 0.75, 0.26, 11));
    SENSI_FWD_1.add(new MarketForwardSensitivity(0.75, 1.00, 0.26, 12));
    SENSI_FWD_1.add(new MarketForwardSensitivity(1.00, 1.25, 0.24, 13));
  }
  private static final String CURVE_NAME_1 = "A";
  private static final String CURVE_NAME_2 = "B";
  private static final String CURVE_NAME_3 = "C";

  private static final Map<String, List<DoublesPair>> SENSI_11 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_12 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_22 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<DoublesPair>> SENSI_33 = new HashMap<String, List<DoublesPair>>();
  private static final Map<String, List<MarketForwardSensitivity>> SENSI_FWD_11 = new HashMap<String, List<MarketForwardSensitivity>>();
  static {
    SENSI_11.put(CURVE_NAME_1, SENSI_DATA_1);
    SENSI_22.put(CURVE_NAME_2, SENSI_DATA_2);
    SENSI_12.put(CURVE_NAME_1, SENSI_DATA_2);
    SENSI_33.put(CURVE_NAME_3, SENSI_DATA_3);
    SENSI_FWD_11.put(CURVE_NAME_2, SENSI_FWD_1);
  }

  // private static final double TOLERANCE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCcy() {
    MultipleCurrencyCurveSensitivityMarket.of(null, new CurveSensitivityMarket());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSensi() {
    MultipleCurrencyCurveSensitivityMarket.of(Currency.AUD, null);
  }

  @Test
  public void of() {
    CurveSensitivityMarket cs = CurveSensitivityMarket.of(SENSI_11, SENSI_FWD_11, SENSI_33);
    Currency ccy1 = Currency.AUD;
    MultipleCurrencyCurveSensitivityMarket mcs = MultipleCurrencyCurveSensitivityMarket.of(ccy1, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: of", cs, mcs.getSensitivity(ccy1));
    Currency ccy2 = Currency.CAD;
    mcs = mcs.plus(ccy2, cs);
    assertEquals("MultipleCurrencyCurveSensitivityMarket: of", cs, mcs.getSensitivity(ccy1));
    assertEquals("MultipleCurrencyCurveSensitivityMarket: of", cs, mcs.getSensitivity(ccy2));
  }

  // TODO: tests on plus, multipliedBy and cleaned.

}
