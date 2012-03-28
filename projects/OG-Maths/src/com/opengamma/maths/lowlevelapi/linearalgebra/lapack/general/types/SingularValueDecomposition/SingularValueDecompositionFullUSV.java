/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.types.SingularValueDecomposition;

/**
 * Type safe SVD full result, does not reflect LAPACK naming as there are two drivers from which the SVD can be obtained (DGESVD and DGESDD).
 */
public class SingularValueDecompositionFullUSV {
 
  private double[] _matrixU;
  private double[] _matrixS;
  private double[] _matrixVT;

  /**
   * Constructs for full result from pointer
   * @param matrixU the matrix U
   * @param matrixS the matrix S
   * @param matrixVT the matrix VT
   */
  public SingularValueDecompositionFullUSV(double[] matrixU, double[] matrixS, double[] matrixVT) {
    int lenU = matrixU.length;
    _matrixU = new double[lenU];
    System.arraycopy(matrixU, 0, _matrixU, 0, lenU);

    int lenS = matrixS.length;
    _matrixS = new double[lenS];
    System.arraycopy(matrixS, 0, _matrixS, 0, lenS);
    
    int lenV = matrixVT.length;
    _matrixVT = new double[lenV];
    System.arraycopy(matrixVT, 0, _matrixVT, 0, lenV);      
  };
  
  
  public SingularValueDecompositionFullUSV() {
  }
  
  public SingularValueDecompositionFullUSV constructNoMemcpy(double[] matrixU, double[] matrixS, double[] matrixVT) {
    _matrixU = matrixU;
    _matrixS = matrixS;
    _matrixVT = matrixVT;
    return new SingularValueDecompositionFullUSV();
  };  
  
  
  /**
   * Gets U.
   * @return the matrixU
   */
  public double[] getMatrixU() {
    return _matrixU;
  }

  /**
   * Gets S.
   * @return the matrixS
   */
  public double[] getMatrixS() {
    return _matrixS;
  }

  /**
   * Gets VT.
   * @return the matrixVT
   */
  public double[] getMatrixV() {
    return _matrixVT;
  }  
  
}
