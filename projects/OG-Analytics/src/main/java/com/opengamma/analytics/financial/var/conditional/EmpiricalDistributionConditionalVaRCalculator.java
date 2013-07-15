/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.var.conditional;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.var.EmpiricalDistributionVaRCalculator;
import com.opengamma.analytics.financial.var.EmpiricalDistributionVaRParameters;
import com.opengamma.analytics.financial.var.VaRCalculationResult;
import com.opengamma.analytics.financial.var.VaRCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EmpiricalDistributionConditionalVaRCalculator implements VaRCalculator<EmpiricalDistributionVaRParameters, DoubleTimeSeries<?>> {
  private final Function1D<double[], Double> _meanCalculator;
  private final EmpiricalDistributionVaRCalculator _varCalculator;

  public EmpiricalDistributionConditionalVaRCalculator(final Function1D<double[], Double> meanCalculator) {
    ArgumentChecker.notNull(meanCalculator, "mean calculator");
    _meanCalculator = meanCalculator;
    _varCalculator = new EmpiricalDistributionVaRCalculator();
  }

  @Override
  public VaRCalculationResult evaluate(final EmpiricalDistributionVaRParameters parameters, final DoubleTimeSeries<?>... data) {
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(data, "data");
    final double var = _varCalculator.evaluate(parameters, data).getVaRValue();
    final DoubleArrayList excesses = new DoubleArrayList();
    for (final double portfolioReturn : data[0].valuesArrayFast()) {
      if (portfolioReturn < -var) {
        excesses.add(portfolioReturn);
      }
    }
    if (excesses.isEmpty()) {
      return new VaRCalculationResult(var, null);
    }
    return new VaRCalculationResult(-_meanCalculator.evaluate(excesses.toDoubleArray()), null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _meanCalculator.hashCode();
    result = prime * result + _varCalculator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EmpiricalDistributionConditionalVaRCalculator other = (EmpiricalDistributionConditionalVaRCalculator) obj;
    if (!ObjectUtils.equals(_meanCalculator, other._meanCalculator)) {
      return false;
    }
    if (!ObjectUtils.equals(_varCalculator, other._varCalculator)) {
      return false;
    }
    return true;
  }

}
