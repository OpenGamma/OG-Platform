/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.definition.FixedInterestRateInstrumentDefinition;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DModel;

/**
 * 
 */
public class ParBondBootstrapZeroDiscountCurveModel implements DiscountCurveModel<FixedInterestRateInstrumentDefinition> {
  private final Map<Double, Interpolator1D<? extends Interpolator1DModel>> _interpolators;

  public ParBondBootstrapZeroDiscountCurveModel(final Interpolator1D<? extends Interpolator1DModel> interpolator) {
    this(Collections.<Double, Interpolator1D<? extends Interpolator1DModel>>singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  public ParBondBootstrapZeroDiscountCurveModel(final Map<Double, Interpolator1D<? extends Interpolator1DModel>> interpolators) {
    _interpolators = interpolators;
  }

  @Override
  public InterpolatedYieldAndDiscountCurve getCurve(final Set<FixedInterestRateInstrumentDefinition> data, final ZonedDateTime date) {
    final Map<Double, Double> zeroRates = new HashMap<Double, Double>();
    final Comparator<FixedInterestRateInstrumentDefinition> comparator = new FixedIncomeDefinitionComparator(date);
    final TreeSet<FixedInterestRateInstrumentDefinition> sorted = new TreeSet<FixedInterestRateInstrumentDefinition>(comparator);
    sorted.addAll(data);
    final Iterator<FixedInterestRateInstrumentDefinition> iterator = sorted.iterator();
    FixedInterestRateInstrumentDefinition definition = iterator.next();
    double r = Math.log(1 + definition.getRate());
    double t = definition.getTenor(date);
    zeroRates.put(t, r);
    double sum = Math.exp(-r * t);
    double c;
    while (iterator.hasNext()) {
      definition = iterator.next();
      t = definition.getTenor(date);
      c = definition.getRate();
      r = -Math.log((1 - c * sum) / (1 + c)) / t;
      zeroRates.put(t, r);
      sum += Math.exp(-r * t);
    }
    return new InterpolatedYieldCurve(zeroRates, _interpolators);
  }

  class FixedIncomeDefinitionComparator implements Comparator<FixedInterestRateInstrumentDefinition> {
    private final ZonedDateTime _date;

    public FixedIncomeDefinitionComparator(final ZonedDateTime date) {
      _date = date;
    }

    @Override
    public int compare(final FixedInterestRateInstrumentDefinition i1, final FixedInterestRateInstrumentDefinition i2) {
      return i1.getTenor(_date).compareTo(i2.getTenor(_date));
    }
  }
}
