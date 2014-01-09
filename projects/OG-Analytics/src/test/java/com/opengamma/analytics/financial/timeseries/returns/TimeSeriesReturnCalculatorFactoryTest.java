/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import static com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory.getReturnCalculator;
import static com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory.getReturnCalculatorName;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TimeSeriesReturnCalculatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCalculatorName1() {
    getReturnCalculator("x");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadCalculatorName2() {
    getReturnCalculator("x", CalculationMode.STRICT);
  }

  @Test
  public void testNullCalculator1() {
    assertNull(getReturnCalculatorName(null));
  }

  @Test
  public void testNullCalculator2() {
    assertNull(getReturnCalculatorName(null, CalculationMode.STRICT));
  }

  @Test
  public void test() {
    assertEquals(TimeSeriesReturnCalculatorFactory.CONTINUOUS_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.CONTINUOUS_RELATIVE_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_RELATIVE_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.CONTINUOUS_RELATIVE_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_RELATIVE_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.EXCESS_SIMPLE_NET_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_SIMPLE_NET_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.EXCESS_SIMPLE_NET_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_SIMPLE_NET_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_GROSS_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_GROSS_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_GROSS_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_GROSS_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_LENIENT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_LENIENT)));
    assertEquals(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_STRICT, getReturnCalculatorName(getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_STRICT)));
  }
}
