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
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class AnalyticsResultBuilderTest extends AnalyticsTestBase {

  @Test
  public void testInterestRateCurveSensitivityCycle() {
    final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
    data.put("A", Lists.newArrayList(new DoublesPair(1, 2), new DoublesPair(3, 4), new DoublesPair(5, 6)));
    data.put("B", Lists.newArrayList(new DoublesPair(10, 20), new DoublesPair(30, 40), new DoublesPair(50, 60)));
    final InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(data);
    assertEquals(sensitivity, cycleObject(InterestRateCurveSensitivity.class, sensitivity));
  }
  
  @Test
  public void testMultipleCurrencyInterestRateCurveSensitivityCycle() {
    final Map<String, List<DoublesPair>> usdData = new HashMap<String, List<DoublesPair>>();
    usdData.put("A", Lists.newArrayList(new DoublesPair(1, 2), new DoublesPair(3, 4), new DoublesPair(5, 6)));
    usdData.put("B", Lists.newArrayList(new DoublesPair(10, 20), new DoublesPair(30, 40), new DoublesPair(50, 60)));
    final Map<String, List<DoublesPair>> eurData = new HashMap<String, List<DoublesPair>>();
    eurData.put("C", Lists.newArrayList(new DoublesPair(11, 12), new DoublesPair(13, 14), new DoublesPair(15, 16)));
    eurData.put("D", Lists.newArrayList(new DoublesPair(110, 120), new DoublesPair(130, 140), new DoublesPair(150, 160)));
    final MultipleCurrencyInterestRateCurveSensitivity sensitivity = MultipleCurrencyInterestRateCurveSensitivity.of(Currency.USD, new InterestRateCurveSensitivity(usdData));
    sensitivity.plus(Currency.EUR, new InterestRateCurveSensitivity(eurData));
    assertEquals(sensitivity, cycleObject(MultipleCurrencyInterestRateCurveSensitivity.class, sensitivity));
  }
}
