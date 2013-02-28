/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;


import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
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
}
