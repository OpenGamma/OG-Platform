/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.decompositions.svd;

import org.testng.annotations.Test;

import cern.colt.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;

/**
 * 
 */
public class SingularValueDecompositionTest {
  
  double [][] _Athinmat = {{1,2,3},{4,5,6},{7,8,9},{10,11,12}};
  double [][] _Alargerthinmat = {{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15},{16,17,18,19,20},{21,22,23,24,25},{26,27,28,29,30}};   
  double [][] _Arowvector = {{1,2,3,4,5}};
  double [][] _Acolvector = {{1},{2},{3},{4},{5}};  
  
  DenseMatrix _DMArowvector = new DenseMatrix(_Arowvector);
  DenseMatrix _DMAcolvector = new DenseMatrix(_Acolvector);
  DenseMatrix _DMAthinmat = new DenseMatrix(_Athinmat);
  DenseMatrix _DMAlargerthinmat = new DenseMatrix(_Alargerthinmat);
  
  @Test
  public void svdTestRowVect(){
    SingularValueDecompositionFullUSV tmp = SingularValueDecomposition.full(_DMArowvector);
    System.out.println("U="+Arrays.toString(tmp.getMatrixU()));
    System.out.println("S="+Arrays.toString(tmp.getMatrixS()));
    System.out.println("V=");
    printToMatrix(tmp.getMatrixV(), 5, 5);  
  }

  @Test
  public void svdTestColVect(){
    SingularValueDecompositionFullUSV tmp = SingularValueDecomposition.full(_DMAcolvector);
    System.out.println("U="+Arrays.toString(tmp.getMatrixU()));
    System.out.println("S="+Arrays.toString(tmp.getMatrixS()));
    System.out.println("V=");
    printToMatrix(tmp.getMatrixV(), 5, 5);  
  }
  
  @Test
  public void svdTestThinMat(){
    SingularValueDecompositionFullUSV tmp = SingularValueDecomposition.full(_DMAthinmat);
    System.out.println("U="+Arrays.toString(tmp.getMatrixU()));
    System.out.println("S="+Arrays.toString(tmp.getMatrixS()));
    System.out.println("V=");
    printToMatrix(tmp.getMatrixV(), 4, 4);  
  }

  @Test
  public void svdTestLargerThinMat(){
    SingularValueDecompositionFullUSV tmp = SingularValueDecomposition.full(_DMAlargerthinmat);
    System.out.println("U="+Arrays.toString(tmp.getMatrixU()));
    System.out.println("S="+Arrays.toString(tmp.getMatrixS()));
    System.out.println("V=");
    printToMatrix(tmp.getMatrixV(), 4, 4);  
  }  
  
  
  private void printToMatrix(double [] x, int m, int n) {
    int in;
    for (int i = 0; i < m; i++){
      in = i*n;
      for (int j = 0; j < n ; j++){
        System.out.print(x[in+j]+" ");
      }
      System.out.println("\n");
    }
  }
  
}
