/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.bond;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;

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

}
