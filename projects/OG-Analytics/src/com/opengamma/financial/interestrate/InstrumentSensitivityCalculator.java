/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.LinkedHashMap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class InstrumentSensitivityCalculator {

  private final MatrixAlgebra _ma = new CommonsMatrixAlgebra();

  /**
   *Calculates the sensitivity of the present value (PV) of an instrument to changes in the par-rates of the instruments used to build the curve (i.e. bucked delta)
   * @param ird The instrument of interest
   * @param fixedCurves any fixed curves (can be null)
   * @param interpolatedCurves The set of interpolatedCurves 
   * @param dollarDurations The sensitivity of the PV of the instruments used to build the curve(s) to their par-rates
   * @param pvJacobian Matrix of sensitivity of the PV of the instruments used to build the curve(s) to the yields - 
   * the i,j element is dx_i/dy_j where x_i is the PV of the ith instrument and y_j is the jth yield 
   * @return bucked delta
   */
  public DoubleMatrix1D calculateFromPresentValue(final InterestRateDerivative ird, final YieldCurveBundle fixedCurves, final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves,
      final DoubleMatrix1D dollarDurations, final DoubleMatrix2D pvJacobian) {
    final NodeSensitivityCalculator nsc = new NodeSensitivityCalculator();
    final DoubleMatrix1D nodeSense = nsc.presentValueCalculate(ird, fixedCurves, interpolatedCurves);
    final int n = nodeSense.getNumberOfElements();
    Validate.isTrue(n == dollarDurations.getNumberOfElements());
    Validate.isTrue(n == pvJacobian.getNumberOfColumns());
    Validate.isTrue(n == pvJacobian.getNumberOfRows());

    final DoubleMatrix2D invJac = _ma.getInverse(pvJacobian);

    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      double sum = 0;
      for (int j = 0; j < n; j++) {
        sum += dollarDurations.getEntry(i) * invJac.getEntry(j, i) * nodeSense.getEntry(j);
      }
      res[i] = sum;
    }
    return new DoubleMatrix1D(res);
  }

  /**
   *Calculates the sensitivity of the present value (PV) of an instrument to changes in the par-rates of the instruments used to build the curve (i.e. bucked delta)
   * @param ird The instrument of interest
   * @param fixedCurves any fixed curves (can be null)
   * @param interpolatedCurves The set of interpolatedCurves
   * @param parRateJacobian Matrix of sensitivity of the par-rate of the instruments used to build the curve(s) to the yields - 
   * the i,j element is dr_i/dy_j where r_i is the par-rate of the ith instrument and y_j is the jth yield 
   * @return bucked delta
   */
  public DoubleMatrix1D calculateFromParRate(final InterestRateDerivative ird, final YieldCurveBundle fixedCurves, final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves,
      final DoubleMatrix2D parRateJacobian) {
    final NodeSensitivityCalculator nsc = new NodeSensitivityCalculator();
    final DoubleMatrix1D nodeSense = nsc.presentValueCalculate(ird, fixedCurves, interpolatedCurves);
    final int n = nodeSense.getNumberOfElements();

    Validate.isTrue(n == parRateJacobian.getNumberOfColumns());
    Validate.isTrue(n == parRateJacobian.getNumberOfRows());

    final DoubleMatrix2D invJac = _ma.getInverse(parRateJacobian);

    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      double sum = 0;
      for (int j = 0; j < n; j++) {
        sum += invJac.getEntry(j, i) * nodeSense.getEntry(j);
      }
      res[i] = sum;
    }
    return new DoubleMatrix1D(res);
  }

}
