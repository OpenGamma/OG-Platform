/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.MatrixAlgebraFactory;
import com.opengamma.math.minimization.QuasiNewtonVectorMinimizer.DataBundle;

/**
 * 
 */
public class BroydenFletcherGoldfarbShannoInverseHessianUpdate implements QuasiNewtonInverseHessianUpdate {
  private static final MatrixAlgebra MA = MatrixAlgebraFactory.getMatrixAlgebra("OG");

  @Override
  public void update(DataBundle data) {
    DoubleMatrix1D hDeltaGrad = (DoubleMatrix1D) MA.multiply(data.getInverseHessianEsimate(), data.getDeltaGrad());
    double deltaXdeltaGrad = MA.getInnerProduct(data.getDeltaX(), data.getDeltaGrad());
    double deltaGradHdeltaGrad = MA.getInnerProduct(data.getDeltaGrad(), hDeltaGrad);
    if (deltaXdeltaGrad == 0.0) {
      throw new ArithmeticException("The dot product of the change in position and the change in gradiant is zero");
    }
    if (deltaGradHdeltaGrad == 0.0) {
      throw new ArithmeticException("Most likely the change in gradiant is zero - should have exited");
    }

    DoubleMatrix2D tempMat1 = MA.getOuterProduct(MA.scale(data.getDeltaX(), 1.0 / deltaXdeltaGrad), data.getDeltaX());
    DoubleMatrix2D tempMat2 = MA.getOuterProduct(MA.scale(hDeltaGrad, -1.0 / deltaGradHdeltaGrad), hDeltaGrad);

    DoubleMatrix1D u = (DoubleMatrix1D) MA.subtract(MA.scale(data.getDeltaX(), 1.0 / deltaXdeltaGrad), MA.scale(hDeltaGrad, deltaGradHdeltaGrad));
    DoubleMatrix2D tempMat3 = MA.getOuterProduct(MA.scale(u, deltaGradHdeltaGrad), u);

    DoubleMatrix2D res = (DoubleMatrix2D) MA.add(data.getInverseHessianEsimate(), MA.add(tempMat1, MA.add(tempMat2, tempMat3)));
    data.setInverseHessianEsimate(res);
  }

}
