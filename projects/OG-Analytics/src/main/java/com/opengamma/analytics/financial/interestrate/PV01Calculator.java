/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Returns the change in present value of an instrument due to a parallel move of the yield curve, scaled so that the move is 1bp.
 * @deprecated Use the calculators that reference {@link ParameterProviderInterface}
 */
@Deprecated
public final class PV01Calculator extends InstrumentDerivativeVisitorSameMethodAdapter<YieldCurveBundle, Map<String, Double>> {

  /**
  * The unique instance of the sensitivity calculator.
  */
  private static final PV01Calculator INSTANCE = new PV01Calculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PV01Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private standard constructor. Using the standard curve sensitivity calculator: PresentValueCurveSensitivityCalculator.
   */
  private PV01Calculator() {
    _presentValueCurveSensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
  }

  /**
   * Constructor with a specific present value curve sensitivity calculator.
   * @param presentValueCurveSensitivityCalculator The calculator.
   */
  public PV01Calculator(final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> presentValueCurveSensitivityCalculator) {
    _presentValueCurveSensitivityCalculator = presentValueCurveSensitivityCalculator;
  }

  /**
   * The size of the scaling: 1 basis point.
   */
  private static final double BP1 = 1.0E-4;
  /**
   * The present value curve sensitivity calculator.
   */
  private final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> _presentValueCurveSensitivityCalculator;

  /**
   * Calculates the change in present value of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird The instrument.
   * @param curves The bundle of relevant yield curves.
   * @return a Map between curve name and PV01 for that curve
   */
  @Override
  public Map<String, Double> visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Map<String, List<DoublesPair>> sense = ird.accept(_presentValueCurveSensitivityCalculator, curves);
    final Map<String, Double> res = new HashMap<>();
    final Iterator<Entry<String, List<DoublesPair>>> iterator = sense.entrySet().iterator();
    while (iterator.hasNext()) {
      final Entry<String, List<DoublesPair>> entry = iterator.next();
      final String name = entry.getKey();
      final double pv01 = sumListPair(entry.getValue()) * BP1;
      res.put(name, pv01);
    }
    return res;
  }

  private static double sumListPair(final List<DoublesPair> list) {
    double sum = 0.0;
    for (final DoublesPair pair : list) {
      sum += pair.getSecond();
    }
    return sum;
  }

  @Override
  public Map<String, Double> visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curve data");
  }

}
