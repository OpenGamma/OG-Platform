/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.method;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * Class used to compute interest rate instrument sensitivity by finite difference.
 */
public class SensitivityFiniteDifference {

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference.
   * @param instrument The instrument.
   * @param curves The yield curve bundle.
   * @param pv The initial instrument present value.
   * @param curveToBumpName The name of the curve to bump.
   * @param curveBumpedName The name of the curve after bumped. The name should be used in the instrument construction.
   * @param nodeTimes The node times to be bumped.
   * @param deltaShift The bump size.
   * @param method The method to compute the present value sensitivity.
   * @param isCentered Flag indicating if the computation should be symmetrical (centered, shift above and below the initial value) or non-symmetrical (shift on one side).
   * @return The array of sensitivity with respect the to the given node times.
   */
  public static double[] curveSensitivity(final InterestRateDerivative instrument, final YieldCurveBundle curves, double pv, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method, final boolean isCentered) {
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
    if (!isCentered) {
      for (int loopnode = 0; loopnode < nbNode; loopnode++) {
        final YieldAndDiscountCurve curveBumped = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
        final YieldCurveBundle curvesBumped = new YieldCurveBundle();
        curvesBumped.addAll(curves);
        curvesBumped.setCurve(curveBumpedName, curveBumped);
        final double bumpedpv = method.presentValue(instrument, curvesBumped);
        result[loopnode] = (bumpedpv - pv) / deltaShift;
      }
    } else {
      for (int loopnode = 0; loopnode < nbNode; loopnode++) {
        final YieldAndDiscountCurve curveBumpedPlus = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], deltaShift);
        final YieldAndDiscountCurve curveBumpedMinus = curveNode.withSingleShift(nodeTimesExtended[loopnode + 1], -deltaShift);
        final YieldCurveBundle curvesBumpedPlus = new YieldCurveBundle();
        curvesBumpedPlus.addAll(curves);
        curvesBumpedPlus.setCurve(curveBumpedName, curveBumpedPlus);
        final YieldCurveBundle curvesBumpedMinus = new YieldCurveBundle();
        curvesBumpedMinus.addAll(curves);
        curvesBumpedMinus.setCurve(curveBumpedName, curveBumpedMinus);
        final double bumpedpvPlus = method.presentValue(instrument, curvesBumpedPlus);
        final double bumpedpvMinus = method.presentValue(instrument, curvesBumpedMinus);
        result[loopnode] = (bumpedpvPlus - bumpedpvMinus) / (2 * deltaShift);
      }
    }
    return result;
  }

  /**
   * Compute the present value rate sensitivity for a set of node time by finite difference. The computation is done in an non-symmetrical way (non-centered).
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
  public static double[] curveSensitivity(final InterestRateDerivative instrument, final YieldCurveBundle curves, double pv, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method) {
    return curveSensitivity(instrument, curves, pv, curveToBumpName, curveBumpedName, nodeTimes, deltaShift, method, false);
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
  public static double[] curveSensitivity(final InterestRateDerivative instrument, final YieldCurveBundle curves, String curveToBumpName, String curveBumpedName, double[] nodeTimes,
      double deltaShift, PricingMethod method) {
    return curveSensitivity(instrument, curves, 0.0, curveToBumpName, curveBumpedName, nodeTimes, deltaShift, method, true);
  }

}
