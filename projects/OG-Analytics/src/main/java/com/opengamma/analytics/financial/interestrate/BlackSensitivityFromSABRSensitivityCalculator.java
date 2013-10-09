/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes sensitivity to Black volatilities from SABR sensitivities and the SABR parameters sensitivities to calibration volatilities.
 */
public class BlackSensitivityFromSABRSensitivityCalculator {

  /**
   * The implementation of the matrix algebra used.
   */
  private static final MatrixAlgebra ALGEBRA = MatrixAlgebraFactory.OG_ALGEBRA;

  /**
   * Computes the sensitivities to Black volatilities from the sensitivities to SABR parameters.
   * The SABR sensitivities are the node sensitivities to the same grid points as the Black volatilities and the inverseJacobian provided.
   * @param sabrSensitivity The node SABR sensitivities.
   * @param inverseJacobianMap The inverse Jacobian for each node point.
   * @return The Black sensitivity.
   */
  public static Map<DoublesPair, DoubleMatrix1D> blackSensitivity(final PresentValueSABRSensitivityDataBundle sabrSensitivity, final Map<DoublesPair, DoubleMatrix2D> inverseJacobianMap) {
    final Set<DoublesPair> alphaSet = sabrSensitivity.getAlpha().getMap().keySet();
    final Set<DoublesPair> ijSet = inverseJacobianMap.keySet();
    ArgumentChecker.isTrue(alphaSet.equals(ijSet), "Point of SABR sensitivity and inverse Jacobian are not the same.");
    final HashMap<DoublesPair, DoubleMatrix1D> result = new HashMap<>();
    for (final DoublesPair expiryMaturity : sabrSensitivity.getAlpha().getMap().keySet()) {
      final double alphaSensitivity = sabrSensitivity.getAlpha().getMap().get(expiryMaturity);
      final double betaSensitivity = sabrSensitivity.getBeta().getMap().get(expiryMaturity);
      final double rhoSensitivity = sabrSensitivity.getRho().getMap().get(expiryMaturity);
      final double nuSensitivity = sabrSensitivity.getNu().getMap().get(expiryMaturity);
      final DoubleMatrix1D sabrPointSensitivity = new DoubleMatrix1D(alphaSensitivity, betaSensitivity, rhoSensitivity, nuSensitivity);
      final DoubleMatrix2D inverseJacobian = inverseJacobianMap.get(expiryMaturity);
      final DoubleMatrix1D blackPointSensitivity = (DoubleMatrix1D) ALGEBRA.multiply(sabrPointSensitivity, inverseJacobian);
      result.put(expiryMaturity, blackPointSensitivity);
    }
    return result;
  }

}
