/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;


import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
public class CurveResultBuildersTest extends AnalyticsTestBase {

  @Test
  public void testForwardSensitivity() {
    final ForwardSensitivity forwardSensitivity = new ForwardSensitivity(12., 13., 17., 25.);
    assertEquals(forwardSensitivity, cycleObject(ForwardSensitivity.class, forwardSensitivity));
  }

  @Test
  public void testMulticurveSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities = new HashMap<>();
    yieldCurveSensitivities.put(name1, Arrays.asList(new DoublesPair(1., 2.), new DoublesPair(3.5, 6.8)));
    yieldCurveSensitivities.put(name2, Arrays.asList(new DoublesPair(11., 12.), new DoublesPair(13.5, 16.8), new DoublesPair(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities = new HashMap<>();
    forwardCurveSensitivities.put(name1, Arrays.asList(new ForwardSensitivity(1, 5, 0.25, 10), new ForwardSensitivity(2, 3, 0.215, 20), new ForwardSensitivity(3, 9, 0.225, 30)));
    forwardCurveSensitivities.put(name2, Arrays.asList(new ForwardSensitivity(0.1, 4, 0.5, 10)));
    final MulticurveSensitivity sensitivities = MulticurveSensitivity.of(yieldCurveSensitivities, forwardCurveSensitivities);
    assertEquals(sensitivities, cycleObject(MulticurveSensitivity.class, sensitivities));
  }

  @Test
  public void testMultipleCurrencyMulticurveSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities1 = new HashMap<>();
    yieldCurveSensitivities1.put(name1, Arrays.asList(new DoublesPair(1., 2.), new DoublesPair(3.5, 6.8)));
    yieldCurveSensitivities1.put(name2, Arrays.asList(new DoublesPair(11., 12.), new DoublesPair(13.5, 16.8), new DoublesPair(45., 12.)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities1 = new HashMap<>();
    forwardCurveSensitivities1.put(name1, Arrays.asList(new ForwardSensitivity(1, 5, 0.25, 10), new ForwardSensitivity(2, 3, 0.215, 20), new ForwardSensitivity(3, 9, 0.225, 30)));
    forwardCurveSensitivities1.put(name2, Arrays.asList(new ForwardSensitivity(0.1, 4, 0.5, 10)));
    final MulticurveSensitivity sensitivities1 = MulticurveSensitivity.of(yieldCurveSensitivities1, forwardCurveSensitivities1);
    final String name3 = "YC3";
    final String name4 = "YC4";
    final Map<String, List<DoublesPair>> yieldCurveSensitivities2 = new HashMap<>();
    yieldCurveSensitivities2.put(name3, Arrays.asList(new DoublesPair(10., 20.)));
    yieldCurveSensitivities2.put(name4, Arrays.asList(new DoublesPair(110., 120.), new DoublesPair(13.51, 16.81), new DoublesPair(45.3, 12.3)));
    final Map<String, List<ForwardSensitivity>> forwardCurveSensitivities2 = new HashMap<>();
    forwardCurveSensitivities2.put(name3, Arrays.asList(new ForwardSensitivity(18, 58, 0.258, 108), new ForwardSensitivity(32, 92, 0.2252, 302)));
    forwardCurveSensitivities2.put(name4, Arrays.asList(new ForwardSensitivity(0.18, 48, 0.58, 18)));
    final MulticurveSensitivity sensitivities2 = MulticurveSensitivity.of(yieldCurveSensitivities2, forwardCurveSensitivities2);
    MultipleCurrencyMulticurveSensitivity sensitivities = new MultipleCurrencyMulticurveSensitivity();
    sensitivities = sensitivities.plus(Currency.AUD, sensitivities1);
    sensitivities = sensitivities.plus(Currency.CAD, sensitivities2);
    assertEquals(sensitivities, cycleObject(MultipleCurrencyMulticurveSensitivity.class, sensitivities));
  }

  @Test
  public void testSimpleParameterSensitivity() {
    final String name1 = "YC1";
    final String name2 = "YC2";
    final DoubleMatrix1D sensitivities1 = new DoubleMatrix1D(new double[] {1, 2, 4, 6, 7, 9, 12});
    final DoubleMatrix1D sensitivities2 = new DoubleMatrix1D(new double[] {89, 456, 234, 12});
    final LinkedHashMap<String, DoubleMatrix1D> sensitivities = new LinkedHashMap<>();
    sensitivities.put(name1, sensitivities1);
    sensitivities.put(name2, sensitivities2);
    final SimpleParameterSensitivity sps = new SimpleParameterSensitivity(sensitivities);
    assertEquals(sps, cycleObject(SimpleParameterSensitivity.class, sps));
  }
}
