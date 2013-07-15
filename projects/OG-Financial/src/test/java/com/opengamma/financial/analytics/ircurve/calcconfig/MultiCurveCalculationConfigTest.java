/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Collections;
import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MultiCurveCalculationConfigTest extends FinancialTestBase {

  private static final String DEFAULT_USD_CONFIG_NAME = "Default";
  private static final String[] DEFAULT_USD_YIELD_CURVE_NAMES = new String[] {"FUNDING", "FORWARD_3M" };
  private static final ComputationTargetSpecification DEFAULT_USD_ID = ComputationTargetSpecification.of(Currency.USD);
  private static final String DEFAULT_USD_CALCULATION_METHOD = "Present Value";
  private static final String EXTRA_USD_CONFIG_NAME = "Extra";
  private static final String[] EXTRA_USD_YIELD_CURVE_NAMES = new String[] {"FORWARD_6M", "FORWARD_12M", "FORWARD_1M" };
  private static final ComputationTargetSpecification EXTRA_USD_ID = ComputationTargetSpecification.of(Currency.USD);
  private static final String EXTRA_USD_CALCULATION_METHOD = "Par Rate";
  private static final String DEFAULT_INR_CONFIG_NAME = "Default";
  private static final String[] DEFAULT_INR_YIELD_CURVE_NAMES = new String[] {"FUNDING" };
  private static final ComputationTargetSpecification DEFAULT_INR_ID = ComputationTargetSpecification.of(Currency.of("INR"));
  private static final String DEFAULT_INR_CALCULATION_METHOD = "FXImplied";
  private static final MultiCurveCalculationConfig DEFAULT_USD_CONFIG;
  private static final MultiCurveCalculationConfig EXTRA_USD_CONFIG;
  private static final MultiCurveCalculationConfig DEFAULT_INR_CONFIG;
  private static final LinkedHashMap<String, CurveInstrumentConfig> CURVE_EXPOSURES;
  private static final LinkedHashMap<String, String[]> EXOGENOUS_CURVES;

  static {
    EXOGENOUS_CURVES = new LinkedHashMap<String, String[]>();
    EXOGENOUS_CURVES.put(DEFAULT_USD_CONFIG_NAME, new String[] {"FUNDING" });
    CURVE_EXPOSURES = new LinkedHashMap<String, CurveInstrumentConfig>();
    CURVE_EXPOSURES.put("FUNDING", new CurveInstrumentConfig(Collections.singletonMap(StripInstrumentType.CASH, new String[] {"FUNDING" })));
    DEFAULT_USD_CONFIG = new MultiCurveCalculationConfig(DEFAULT_USD_CONFIG_NAME, DEFAULT_USD_YIELD_CURVE_NAMES, DEFAULT_USD_ID,
        DEFAULT_USD_CALCULATION_METHOD, CURVE_EXPOSURES);
    EXTRA_USD_CONFIG = new MultiCurveCalculationConfig(EXTRA_USD_CONFIG_NAME, EXTRA_USD_YIELD_CURVE_NAMES, EXTRA_USD_ID, EXTRA_USD_CALCULATION_METHOD,
        CURVE_EXPOSURES, EXOGENOUS_CURVES);
    DEFAULT_INR_CONFIG = new MultiCurveCalculationConfig(DEFAULT_INR_CONFIG_NAME, DEFAULT_INR_YIELD_CURVE_NAMES, DEFAULT_INR_ID, DEFAULT_INR_CALCULATION_METHOD,
        CURVE_EXPOSURES, EXOGENOUS_CURVES);
  }

  @Test
  public void test() {
    assertFalse(DEFAULT_USD_CONFIG.equals(EXTRA_USD_CONFIG));
    assertFalse(DEFAULT_USD_CONFIG.equals(DEFAULT_INR_CONFIG));
    final String name = "Extra";
    final String[] curveNames = new String[] {"FORWARD_6M", "FORWARD_12M", "FORWARD_1M" };
    final ComputationTargetSpecification target = ComputationTargetSpecification.of(Currency.USD);
    final LinkedHashMap<String, String[]> exogenousCurves = new LinkedHashMap<String, String[]>();
    exogenousCurves.put(DEFAULT_USD_CONFIG_NAME, new String[] {"FUNDING" });
    final MultiCurveCalculationConfig config = new MultiCurveCalculationConfig(name, curveNames, target, "Par Rate", CURVE_EXPOSURES, exogenousCurves);
    assertEquals(EXTRA_USD_CONFIG, config);
    assertEquals(config.getCalculationMethod(), "Par Rate");
    assertEquals(config.getCalculationConfigName(), name);
    assertEquals(config.getTarget(), target);
    ArrayAsserts.assertArrayEquals(config.getYieldCurveNames(), curveNames);
    assertEquals(config.getExogenousConfigData(), exogenousCurves);
  }

  @Test
  public void testCycle() {
    assertEquals(DEFAULT_USD_CONFIG, cycleObject(MultiCurveCalculationConfig.class, DEFAULT_USD_CONFIG));
    assertEquals(EXTRA_USD_CONFIG, cycleObject(MultiCurveCalculationConfig.class, EXTRA_USD_CONFIG));
    assertEquals(DEFAULT_INR_CONFIG, cycleObject(MultiCurveCalculationConfig.class, DEFAULT_INR_CONFIG));
  }
}
