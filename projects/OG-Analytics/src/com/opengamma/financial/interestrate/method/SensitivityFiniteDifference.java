/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.FiniteDifferenceType;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * Class used to compute interest rate instrument sensitivity by finite difference.
 */
public class SensitivityFiniteDifference {

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference.
   * @param instrument The instrument.
   * @param curves The yield curve bundle. The bumped curve will be added to the curve bundle.
   * @param pv The initial instrument present value.
   * @param curveToBumpName The name of the curve to bump.
   * @param curveBumpedName The name of the curve after bumped. The name should be used in the instrument construction.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @param differenceType {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. 
   * Indicates how the finite difference is computed. Not null
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InstrumentDerivative instrument, YieldCurveBundle curves, double pv, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method, final FiniteDifferenceType differenceType) {
    Validate.notNull(instrument, "Instrument");
    Validate.notNull(curves, "Curves");
    Validate.notNull(method, "Method");
    Validate.notNull(differenceType, "Difference type");
    int nbNode = nodeTimes.length;
    double[] result = new double[nbNode];
    final YieldAndDiscountCurve curveToBump = curves.getCurve(curveToBumpName);
    final double[] yields = new double[nbNode + 1];
    final double[] nodeTimesExtended = new double[nbNode + 1];
    System.arraycopy(nodeTimes, 0, nodeTimesExtended, 1, nbNode);
    yields[0] = curveToBump.getInterestRate(0.0);
    for (int loopnode = 0; loopnode < nbNode; loopnode++) {
      yields[loopnode + 1] = curveToBump.getInterestRate(nodeTimesExtended[loopnode + 1]);
    }
    final YieldAndDiscountCurve curveNode = new YieldCurve(InterpolatedDoublesCurve.fromSorted(nodeTimesExtended, yields, new LinearInterpolator1D()));
    if (!curves.containsName(curveBumpedName)) {
      curves.setCurve(curveBumpedName, curveToBump);
    }
    switch (differenceType) {
      case FORWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
          curves.replaceCurve(curveBumpedName, curveBumped);
          final double bumpedpv = method.presentValue(instrument, curves).getAmount();
          result[loopnode] = (bumpedpv - pv) / deltaShift;
        }
        return result;
      case CENTRAL:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          final YieldAndDiscountCurve curveBumpedPlus = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
          final YieldAndDiscountCurve curveBumpedMinus = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], -deltaShift);
          curves.replaceCurve(curveBumpedName, curveBumpedPlus);
          final double bumpedpvPlus = method.presentValue(instrument, curves).getAmount();
          curves.replaceCurve(curveBumpedName, curveBumpedMinus);
          final double bumpedpvMinus = method.presentValue(instrument, curves).getAmount();
          result[loopnode] = (bumpedpvPlus - bumpedpvMinus) / (2 * deltaShift);
        }
        return result;
      case BACKWARD:
        for (int loopnode = 0; loopnode < nbNode; loopnode++) {
          final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], -deltaShift);
          curves.replaceCurve(curveBumpedName, curveBumped);
          final double bumpedpv = method.presentValue(instrument, curves).getAmount();
          result[loopnode] = (pv - bumpedpv) / deltaShift;
        }
        return result;
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");

  }

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference. The computation is done in an non-symmetrical forward way.
   * @param instrument The instrument.
   * @param curves The yield curve bundle.
   * @param pv The initial instrument present value.
   * @param curveToBumpName The name of the curve to bump.
   * @param curveBumpedName The name of the curve after bumped. The name should be used in the instrument construction.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves, double pv, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method) {
    return curveSensitivity(instrument, curves, pv, curveToBumpName, curveBumpedName, nodeTimes, deltaShift, method, FiniteDifferenceType.FORWARD);
  }

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference. The computation is done in a symmetrical way (centered).
   * @param instrument The instrument.
   * @param curves The yield curve bundle.
   * @param curveToBumpName The name of the curve to bump.
   * @param curveBumpedName The name of the curve after bumped. The name should be used in the instrument construction.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InstrumentDerivative instrument, final YieldCurveBundle curves, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method) {
    return curveSensitivity(instrument, curves, 0.0, curveToBumpName, curveBumpedName, nodeTimes, deltaShift, method, FiniteDifferenceType.CENTRAL);
  }

}
