/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.AddCurveSpreadFunction;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.SpreadDoublesCurve;
import com.opengamma.math.curve.SubtractCurveSpreadFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class FDCurveSensitivityCalculator {

  /**
   * Gives the sensitivity of the some metric of an IRD to a points on a one of the family of curves by finite difference
     * @param ird The Interest Rate Derivative
   * @param calculator This calculates the metric 
   * @param curves The family of yield curves
   * @param curveName The name of the curve of interest
   * @param times The times along the curve. <b>Note</b> These should be in ascending order and  be known sensitivity points 
   * (or the result will be zero) 
   * @param absTol If the absolute value of a sensitivities is below this value it is ignored 
   * @return Sensitivities at a given points 
   */
  public static final List<DoublesPair> curveSensitvityFDCalculator(final InstrumentDerivative ird, AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final YieldCurveBundle curves, final String curveName, final double[] times, final double absTol) {

    Validate.notNull(times, "null times");
    Validate.notNull(ird, "null ird");
    Validate.notNull(calculator, "null calculator");
    Validate.notNull(curves, "null curves");
    Validate.isTrue(times[0] >= 0.0, "t less than 0");
    Validate.isTrue(curves.containsName(curveName), "curveName not in curves");

    List<DoublesPair> res = new ArrayList<DoublesPair>();
    double oldT = times[0];
    boolean first = true;
    for (double t : times) {
      if (!first) {
        Validate.isTrue(t > oldT, "times not strictly assending");
      } else {
        first = false;
      }
      double sense = impFDCalculator(ird, calculator, curves, curveName, t);
      if (Math.abs(sense) > absTol) {
        res.add(new DoublesPair(t, sense));
      }
      oldT = t;
    }
    return res;
  }

  /**
   * Gives the sensitivity of the some metric of an IRD to a points on a one of the family of curves by finite difference
     * @param ird The Interest Rate Derivative
   * @param method This calculates the metric 
   * @param curves The family of yield curves
   * @param curveName The name of the curve of interest
   * @param times The times along the curve. <b>Note</b> These should be in ascending order and  be known sensitivity points 
   * (or the result will be zero) 
   * @param absTol If the absolute value of a sensitivities is below this value it is ignored 
   * @return Sensitivities at a given points 
   */
  public static final List<DoublesPair> curveSensitvityFDCalculator(final InstrumentDerivative ird, PricingMethod method, final YieldCurveBundle curves, final String curveName,
      final double[] times, final double absTol) {

    Validate.notNull(times, "null times");
    Validate.notNull(ird, "null ird");
    Validate.notNull(method, "null method");
    Validate.notNull(curves, "null curves");
    Validate.isTrue(times[0] >= 0.0, "t less than 0");
    Validate.isTrue(curves.containsName(curveName), "curveName not in curves");

    List<DoublesPair> res = new ArrayList<DoublesPair>();
    double oldT = times[0];
    boolean first = true;
    for (double t : times) {
      if (!first) {
        Validate.isTrue(t > oldT, "times not strictly assending");
      } else {
        first = false;
      }
      double sense = impFDCalculator(ird, method, curves, curveName, t);
      if (Math.abs(sense) > absTol) {
        res.add(new DoublesPair(t, sense));
      }
      oldT = t;
    }
    return res;
  }

  /**
   * Gives the sensitivity of the some metric of an IRD to a point on a one of the family of curves by finite difference
   * @param ird The Interest Rate Derivative
   * @param calculator This calculates the metric 
   * @param curves The family of yield curves
   * @param curveName The name of the curve of interest
   * @param t The time along the curve. <b>Note</b> This should be a known sensitivity point or the result will be zero 
   * @return Sensitivity at a given point 
   */
  public static final double curveSensitvityFDCalculator(final InstrumentDerivative ird, AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, final YieldCurveBundle curves,
      final String curveName, final double t) {
    Validate.notNull(ird, "null ird");
    Validate.notNull(calculator, "null calculator");
    Validate.notNull(curves, "null curves");
    Validate.isTrue(t >= 0.0, "t less than 0");
    Validate.isTrue(curves.containsName(curveName), "curveName not in curves");

    return impFDCalculator(ird, calculator, curves, curveName, t);
  }

  private static double impFDCalculator(final InstrumentDerivative ird, AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, final YieldCurveBundle curves,
      final String curveName, final double t) {

    final double eps = 1e-6;

    Function1D<Double, Double> blip = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return (Math.abs(x - t) < 3.0e-6 ? eps : 0.0); //100 second tolerance 
      }
    };

    FunctionalDoublesCurve blipCurve = FunctionalDoublesCurve.from(blip);
    YieldAndDiscountCurve originalCurve = curves.getCurve(curveName);

    @SuppressWarnings("rawtypes")
    Curve[] curveSet = new Curve[] {originalCurve.getCurve(), blipCurve};
    @SuppressWarnings("unchecked")
    YieldAndDiscountCurve upCurve = new YieldCurve(SpreadDoublesCurve.from(new AddCurveSpreadFunction(), curveSet));
    @SuppressWarnings("unchecked")
    YieldAndDiscountCurve downCurve = new YieldCurve(SpreadDoublesCurve.from(new SubtractCurveSpreadFunction(), curveSet));

    curves.replaceCurve(curveName, upCurve);
    double up = calculator.visit(ird, curves);
    curves.replaceCurve(curveName, downCurve);
    double down = calculator.visit(ird, curves);
    curves.replaceCurve(curveName, originalCurve);

    return (up - down) / 2 / eps;
  }

  private static double impFDCalculator(final InstrumentDerivative ird, PricingMethod method, final YieldCurveBundle curves, final String curveName, final double t) {

    final double eps = 1e-6;

    Function1D<Double, Double> blip = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return (Math.abs(x - t) < 3.0e-6 ? eps : 0.0); //100 second tolerance 
      }
    };

    FunctionalDoublesCurve blipCurve = FunctionalDoublesCurve.from(blip);
    YieldAndDiscountCurve originalCurve = curves.getCurve(curveName);

    @SuppressWarnings("rawtypes")
    Curve[] curveSet = new Curve[] {originalCurve.getCurve(), blipCurve};
    @SuppressWarnings("unchecked")
    YieldAndDiscountCurve upCurve = new YieldCurve(SpreadDoublesCurve.from(new AddCurveSpreadFunction(), curveSet));
    @SuppressWarnings("unchecked")
    YieldAndDiscountCurve downCurve = new YieldCurve(SpreadDoublesCurve.from(new SubtractCurveSpreadFunction(), curveSet));

    curves.replaceCurve(curveName, upCurve);
    double up = method.presentValue(ird, curves).getAmount();
    curves.replaceCurve(curveName, downCurve);
    double down = method.presentValue(ird, curves).getAmount();
    curves.replaceCurve(curveName, originalCurve);

    return (up - down) / 2 / eps;
  }

}
