/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 *
 */
public class EmpiricalDistributionVaRCalculator implements VaRCalculator<EmpiricalDistributionVaRParameters, DoubleTimeSeries<?>> {

  @Override
  public Double evaluate(final EmpiricalDistributionVaRParameters parameters, final DoubleTimeSeries<?>... returns) {
    Validate.notNull(parameters, "parameters");
    Validate.notNull(returns, "time series");
    Validate.notNull(returns, "returns");
    Validate.isTrue(returns.length > 0);
    final double[] data = returns[0].valuesArrayFast();
    Arrays.sort(data);
    return parameters.getMult() * parameters.getPercentileCalculator().evaluate(data);
  }

}
