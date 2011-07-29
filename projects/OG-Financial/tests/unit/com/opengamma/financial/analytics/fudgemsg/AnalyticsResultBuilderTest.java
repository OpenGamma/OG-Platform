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
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class AnalyticsResultBuilderTest extends AnalyticsTestBase {

  @Test
  public void testCycle() {
    final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
    data.put("A", Lists.newArrayList(new DoublesPair(1, 2), new DoublesPair(3, 4), new DoublesPair(5, 6)));
    data.put("B", Lists.newArrayList(new DoublesPair(10, 20), new DoublesPair(30, 40), new DoublesPair(50, 60)));
    final PresentValueSensitivity sensitivity = new PresentValueSensitivity(data);
    assertEquals(sensitivity, cycleObject(PresentValueSensitivity.class, sensitivity));
  }
}
