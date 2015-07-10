/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Returns the change in PV01 of an instrument due to a parallel move of the yield curve, scaled so that the move is 1bp.
 * @deprecated Use the calculators that reference {@link ParameterProviderInterface}
 */
@Deprecated
public final class GammaPV01Calculator extends InstrumentDerivativeVisitorSameMethodAdapter<YieldCurveBundle, Double> {
  /**
   * The size of the scaling: 1 basis point.
   */
  private static final double BP1 = 1.0E-4;
  /**
  * The unique instance of the gamma PV01 calculator.
  */
  private static final GammaPV01Calculator INSTANCE = new GammaPV01Calculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static GammaPV01Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * The present value curve sensitivity calculator.
   */
  private final PV01Calculator _pv01Calculator;

  /**
   * Private standard constructor. Using the standard curve sensitivity calculator: PresentValueCurveSensitivityCalculator.
   */
  private GammaPV01Calculator() {
    _pv01Calculator = PV01Calculator.getInstance();
  }

  /**
   * Constructor with a specific present value curve sensitivity calculator.
   * @param presentValueCurveSensitivityCalculator The calculator.
   */
  public GammaPV01Calculator(final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> presentValueCurveSensitivityCalculator) {
    _pv01Calculator = new PV01Calculator(presentValueCurveSensitivityCalculator);
  }

  /**
   * Calculates the change in PV01 of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird The instrument.
   * @param curves The bundle of relevant yield curves.
   * @return a Map between curve name and Gamma PV01 for that curve
   */
  @Override
  public Double visit(final InstrumentDerivative ird, final YieldCurveBundle curves) {
    final Set<String> names = curves.getAllNames();
    final YieldCurveBundle newCurves = curves.copy();
    for (final String name : names) {
      final YieldAndDiscountCurve original = curves.getCurve(name);
      final YieldAndDiscountCurve bumped = original.withParallelShift(BP1);
      newCurves.replaceCurve(name, bumped);
    }
    final Map<String, Double> up = ird.accept(_pv01Calculator, newCurves);
    final Map<String, Double> pv01 = ird.accept(_pv01Calculator, curves);
    double gammaPV01 = 0;
    for (final Map.Entry<String, Double> entry : up.entrySet()) {
      if (!(pv01.containsKey(entry.getKey()))) {
        throw new IllegalStateException("Have bumped PV01 for curve called " + entry.getKey() + " but no PV01");
      }
      gammaPV01 += (entry.getValue() - pv01.get(entry.getKey())) / BP1;
    }
    return gammaPV01;
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curve data");
  }

}
