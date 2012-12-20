/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.general.types.BidiagonalisationDecomposition;

/**
 * Struct containing full bidiagonalisation decomposition results. To get this decomposition call non-fastpath methods in DGEBRD().
 * U^T * A * V = D 
 */
public class BidiagonalisationDecompositionFullUTDV {
  private double[] _matrixUT;
  private double[] _vectorD;
  private double[] _vectorDP1;
  private double[] _matrixV;

  /**
   * Empty BidiagonalisationDecompositionFullUTDV
   */
  public BidiagonalisationDecompositionFullUTDV() {
  }

  /**
   * Creates a BidiagonalisationDecompositionFullUTDV without memcpying the passed in data
   * @param matrixUT the matrix UT
   * @param vectorD the vector D
   * @param vectorDP1 the vector DP1 
   * @param matrixV the matrix V
   * @return a BidiagonalisationDecompositionFullUTDV struct which members point to the pre-alloc'd data passed in  
   */
  public BidiagonalisationDecompositionFullUTDV constructNoMemcpy(double[] matrixUT, double[] vectorD, double[] vectorDP1, double[] matrixV) {
    _matrixUT = matrixUT;
    _vectorD = vectorD;
    _vectorDP1 = vectorDP1;
    _matrixV = matrixV;
    return this;
  }

  /**
   * Creates a BidiagonalisationDecompositionFullUTDV by memcpying the passed in data
   * @param matrixUT the matrix UT
   * @param vectorD the vector D
   * @param vectorDP1 the vector DP1 
   * @param matrixV the matrix V 
   */  
  public BidiagonalisationDecompositionFullUTDV(double[] matrixUT, double[] vectorD, double[] vectorDP1, double[] matrixV) {
    final int lenUT = matrixUT.length;
    _matrixUT = new double[lenUT];
    System.arraycopy(matrixUT, 0, _matrixUT, 0, lenUT);

    final int lenD = vectorD.length;
    _vectorD = new double[lenD];
    System.arraycopy(vectorD, 0, _vectorD, 0, lenD);

    final int lenDP1 = vectorDP1.length;
    _vectorDP1 = new double[lenDP1];
    System.arraycopy(vectorDP1, 0, _vectorDP1, 0, lenDP1);
    
    final int lenV = matrixUT.length;
    _matrixV = new double[lenV];
    System.arraycopy(matrixV, 0, _matrixV, 0, lenV);    
      
  }

  /**
   * Gets the matrixUT.
   * @return the matrixUT
   */
  public double[] getMatrixUT() {
    return _matrixUT;
  }

  /**
   * Gets the vectorD.
   * @return the vectorD
   */
  public double[] getVectorD() {
    return _vectorD;
  }

  /**
   * Gets the vectorDP1.
   * @return the vectorDP1
   */
  public double[] getVectorDP1() {
    return _vectorDP1;
  }

  /**
   * Gets the matrixV.
   * @return the matrixV
   */
  public double[] getMatrixV() {
    return _matrixV;
  }

}
