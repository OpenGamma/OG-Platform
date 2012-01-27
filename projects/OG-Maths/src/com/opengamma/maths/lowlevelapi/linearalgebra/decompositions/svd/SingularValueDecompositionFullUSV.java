/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.decompositions.svd;

/**
 * 
 */
public class SingularValueDecompositionFullUSV {

  private double[] _matrixU;
  private double[] _matrixS;
  private double[] _matrixV;

  /**
   * Constructs for full result from pointer
   * @param U
   * @param S
   * @param V
   */
  SingularValueDecompositionFullUSV(double[] matrixU, double[] matrixS, double[] matrixV) {
    int lenU = matrixU.length;
    _matrixU = new double[lenU];
    System.arraycopy(matrixU, 0, _matrixU, 0, lenU);

    int lenS = matrixS.length;
    _matrixS = new double[lenS];
    System.arraycopy(matrixS, 0, _matrixS, 0, lenS);
    
    int lenV = matrixV.length;
    _matrixV = new double[lenV];
    System.arraycopy(matrixV, 0, _matrixV, 0, lenV);      
  };
  
  /**
   * Gets the matrixU.
   * @return the matrixU
   */
  public double[] getMatrixU() {
    return _matrixU;
  }

  /**
   * Gets the matrixS.
   * @return the matrixS
   */
  public double[] getMatrixS() {
    return _matrixS;
  }

  /**
   * Gets the matrixV.
   * @return the matrixV
   */
  public double[] getMatrixV() {
    return _matrixV;
  }  
  
}
