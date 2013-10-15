/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AnalyticsResultBuilderTest extends AnalyticsTestBase {

  @Test
  public void testInterestRateCurveSensitivityCycle() {
    final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
    data.put("A", Lists.newArrayList(DoublesPair.of(1d, 2d), DoublesPair.of(3d, 4d), DoublesPair.of(5d, 6d)));
    data.put("B", Lists.newArrayList(DoublesPair.of(10d, 20d), DoublesPair.of(30d, 40d), DoublesPair.of(50d, 60d)));
    final InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(data);
    assertEquals(sensitivity, cycleObject(InterestRateCurveSensitivity.class, sensitivity));
  }
  
  @Test
  public void testMultipleCurrencyInterestRateCurveSensitivityCycle() {
    final Map<String, List<DoublesPair>> usdData = new HashMap<String, List<DoublesPair>>();
    usdData.put("A", Lists.newArrayList(DoublesPair.of(1d, 2d), DoublesPair.of(3d, 4d), DoublesPair.of(5d, 6d)));
    usdData.put("B", Lists.newArrayList(DoublesPair.of(10d, 20d), DoublesPair.of(30d, 40d), DoublesPair.of(50d, 60d)));
    final Map<String, List<DoublesPair>> eurData = new HashMap<String, List<DoublesPair>>();
    eurData.put("C", Lists.newArrayList(DoublesPair.of(11d, 12d), DoublesPair.of(13d, 14d), DoublesPair.of(15d, 16d)));
    eurData.put("D", Lists.newArrayList(DoublesPair.of(110d, 120d), DoublesPair.of(130d, 140d), DoublesPair.of(150d, 160d)));
    final MultipleCurrencyInterestRateCurveSensitivity sensitivity = MultipleCurrencyInterestRateCurveSensitivity.of(Currency.USD, new InterestRateCurveSensitivity(usdData));
    sensitivity.plus(Currency.EUR, new InterestRateCurveSensitivity(eurData));
    assertEquals(sensitivity, cycleObject(MultipleCurrencyInterestRateCurveSensitivity.class, sensitivity));
  }
}
