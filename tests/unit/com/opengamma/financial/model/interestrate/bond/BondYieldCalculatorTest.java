/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.bond;

import java.util.Arrays;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class BondYieldCalculatorTest {
  private static final BondYieldCalculator CALCULATOR = new BondYieldCalculator() {

    @Override
    Function1D<Double, Double> getFunction(final DoubleTimeSeries cashFlows, final double price, final ZonedDateTime date) {
      return new Function1D<Double, Double>() {

        @Override
        public Double evaluate(final Double x) {
          return 0.;
        }

      };
    }
  };
  private static final DoubleTimeSeries TS = new ArrayDoubleTimeSeries(Arrays.asList(DateUtil.getUTCDate(2010, 1, 1)), Arrays.asList(1.));
  private static final double PRICE = 1.;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);

  @Test(expected = IllegalArgumentException.class)
  public void testCF() {
    CALCULATOR.calculate(null, PRICE, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrice() {
    CALCULATOR.calculate(TS, 0., DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDate() {
    CALCULATOR.calculate(TS, PRICE, null);
  }
}
