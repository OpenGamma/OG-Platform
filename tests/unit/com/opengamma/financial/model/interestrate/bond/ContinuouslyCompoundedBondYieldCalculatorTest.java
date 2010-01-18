/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.bond;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class ContinuouslyCompoundedBondYieldCalculatorTest {
  private static final ContinuouslyCompoundedBondYieldCalculator CALCULATOR = new ContinuouslyCompoundedBondYieldCalculator();
  private static final DoubleTimeSeries TS;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);

  static {
    final List<ZonedDateTime> times = Arrays.asList(DateUtil.getDateOffsetWithYearFraction(DATE, 1.), DateUtil.getDateOffsetWithYearFraction(DATE, 2.), DateUtil
        .getDateOffsetWithYearFraction(DATE, 3.), DateUtil.getDateOffsetWithYearFraction(DATE, 4.), DateUtil.getDateOffsetWithYearFraction(DATE, 5.));
    final List<Double> cf = Arrays.asList(.1, .1, .1, .1, 1.1);
    TS = new ArrayDoubleTimeSeries(times, cf);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(TS, 1.2559, DATE), 0.0413, 1e-4);
  }

}
