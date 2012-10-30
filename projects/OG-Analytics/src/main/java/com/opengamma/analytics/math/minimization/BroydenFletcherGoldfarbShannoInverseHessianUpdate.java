/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.minimization.QuasiNewtonVectorMinimizer.DataBundle;

/**
 * 
 */
public class BroydenFletcherGoldfarbShannoInverseHessianUpdate implements QuasiNewtonInverseHessianUpdate {
  private static final MatrixAlgebra MA = MatrixAlgebraFactory.getMatrixAlgebra("OG");

  @Override
  public void update(final DataBundle data) {
    final DoubleMatrix1D hDeltaGrad = (DoubleMatrix1D) MA.multiply(data.getInverseHessianEsimate(), data.getDeltaGrad());
    final double deltaXdeltaGrad = MA.getInnerProduct(data.getDeltaX(), data.getDeltaGrad());
    final double deltaGradHdeltaGrad = MA.getInnerProduct(data.getDeltaGrad(), hDeltaGrad);
    if (deltaXdeltaGrad == 0.0) {
      throw new MathException("The dot product of the change in position and the change in gradient is zero");
    }
    if (deltaGradHdeltaGrad == 0.0) {
      throw new MathException("Most likely the change in gradient is zero - should have exited");
    }

    final DoubleMatrix2D tempMat1 = MA.getOuterProduct(MA.scale(data.getDeltaX(), 1.0 / deltaXdeltaGrad), data.getDeltaX());
    final DoubleMatrix2D tempMat2 = MA.getOuterProduct(MA.scale(hDeltaGrad, -1.0 / deltaGradHdeltaGrad), hDeltaGrad);

    final DoubleMatrix1D u = (DoubleMatrix1D) MA.subtract(MA.scale(data.getDeltaX(), 1.0 / deltaXdeltaGrad), MA.scale(hDeltaGrad, deltaGradHdeltaGrad));
    final DoubleMatrix2D tempMat3 = MA.getOuterProduct(MA.scale(u, deltaGradHdeltaGrad), u);

    final DoubleMatrix2D res = (DoubleMatrix2D) MA.add(data.getInverseHessianEsimate(), MA.add(tempMat1, MA.add(tempMat2, tempMat3)));
    data.setInverseHessianEsimate(res);
  }

}
