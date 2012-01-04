/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.highlevelapi;

import static com.opengamma.math.highlevelapi.OGFunctions.abs;
import static com.opengamma.math.highlevelapi.OGFunctions.fliplr;
import static com.opengamma.math.highlevelapi.OGFunctions.flipud;
import static com.opengamma.math.highlevelapi.OGFunctions.horzcat;
import static com.opengamma.math.highlevelapi.OGFunctions.multiply;
import static com.opengamma.math.highlevelapi.OGFunctions.print;
import static com.opengamma.math.highlevelapi.OGFunctions.reshape;
import static com.opengamma.math.highlevelapi.OGFunctions.transpose;
import static com.opengamma.math.highlevelapi.OGFunctions.unique;
import static com.opengamma.math.highlevelapi.OGFunctions.vertcat;

import org.testng.annotations.Test;

/**
 * tests functions on OGArray
 */
public class OGFunctionsTest {

  final double[][] doubleMatrixData = { {1, -2, 3, -4, 5 }, {-6, 7, -8, 9, -10 }, {11, -12, 13, -14, 15 } };
  final double[][] doubleRepeatMatrixData = { {1, 1, 3, -4, 5 }, {7, 7, -8, 9, -10 }, {11, -12, 13, -14, 15 } };
  final double[][] doubleRepeatMatrixUniqueData = {{-14, -12, -10, -8, -4, 1, 3, 5, 7, 9, 11, 13, 15 } };
  final double[][] doubleAbsMatrixData = { {1, 2, 3, 4, 5 }, {6, 7, 8, 9, 10 }, {11, 12, 13, 14, 15 } };
  final double[][] doubleMatrixTranposeData = { {1, -6, 11 }, {-2, 7, -12 }, {3, -8, 13 }, {-4, 9, -14 }, {5, -10, 15 } };
  final double[][] doubleMatrixDataReshape = { {1, -2, 3 }, {-4, 5, -6 }, {7, -8, 9 }, {-10, 11, -12 }, {13, -14, 15 } };

  final double[][] doubleVectorData = { {1 }, {2 }, {3 }, {4 }, {5 } };
  final double[][] doubleShortVectorData = {{1, 2, 3 } };
  final double[][] doubleShortVectorTransposeData = { {1 }, {2 }, {3 } };
  final double[][] doubleVectorTransposeData = {{1, 2, 3, 4, 5 } };
  final OGArray ogDoubleMatrixData = new OGArray(doubleMatrixData);
  final OGArray ogDoubleRepeatMatrixData = new OGArray(doubleRepeatMatrixData);
  final OGArray ogDoubleRepeatMatrixUniqueData = new OGArray(doubleRepeatMatrixUniqueData);
  final OGArray ogDoubleAbsMatrixData = new OGArray(doubleAbsMatrixData);
  final OGArray ogDoubleMatrixTranposeData = new OGArray(doubleMatrixTranposeData);
  final OGArray ogDoubleMatrixDataReshape = new OGArray(doubleMatrixDataReshape);

  final OGArray ogDoubleVectorData = new OGArray(doubleVectorData);
  final OGArray ogDoubleShortVectorData = new OGArray(doubleShortVectorData);
  final OGArray ogDoubleVectorTranposeData = new OGArray(doubleVectorTransposeData);
  final OGArray ogDoubleShortTransposeVectorData = new OGArray(doubleShortVectorTransposeData);

  final int[][] intMatrixData = { {1, -2, 3, -4, 5 }, {-6, 7, -8, 9, -10 }, {11, -12, 13, -14, 15 } };
  final int[][] intRepeatMatrixData = { {1, 1, 3, -4, 5 }, {7, 7, -8, 9, -10 }, {11, -12, 13, -14, 15 } };
  final int[][] intRepeatMatrixUniqueData = {{-14, -12, -10, -8, -4, 1, 3, 5, 7, 9, 11, 13, 15 } };
  final OGIndex ogIntMatrixData = new OGIndex(intMatrixData);
  final OGIndex ogIntRepeatMatrixData = new OGIndex(intRepeatMatrixData);
  final OGIndex ogIntRepeatMatrixUniqueData = new OGIndex(intRepeatMatrixUniqueData);

  /* TEST OGArray */
  @Test
  public void testAbsMatrix() {
    assert (abs(ogDoubleMatrixData).equals(ogDoubleAbsMatrixData));
  }

  @Test
  public void testMultiply() {
    multiply(ogDoubleMatrixData, ogDoubleVectorData); // mat * vec
    multiply(ogDoubleShortVectorData, ogDoubleMatrixData); // vec * mat
    multiply(ogDoubleMatrixTranposeData, ogDoubleShortTransposeVectorData); // mat^T * vec
    multiply(ogDoubleVectorTranposeData, ogDoubleMatrixTranposeData); // vec * mat^T
  }

  @Test
  public void testGetNumberOfRows() {
    assert (ogDoubleVectorData.getNumberOfRows() == 5);
  }

  @Test
  public void testGetNumberOfColumns() {
    assert (ogDoubleVectorData.getNumberOfColumns() == 1);
  }

  @Test
  public void testOGArrayUnique() {
    assert (unique(ogDoubleRepeatMatrixData).equals(ogDoubleRepeatMatrixUniqueData));
  }

  @Test
  public void testOGArrayReshape() {
    assert (reshape(ogDoubleMatrixData, 5, 3).equals(ogDoubleMatrixDataReshape));
  }

  @Test
  public void testOGArrayfliplr() {
    print(fliplr(ogDoubleMatrixData));
  }

  @Test
  public void testOGArrayflipud() {
    print(flipud(ogDoubleMatrixData));
  }

  @Test
  public void testOGArrayhorzcat() {
    print(horzcat(ogDoubleMatrixData, ogDoubleMatrixData, ogDoubleAbsMatrixData));
  }

  @Test
  public void testOGArrayvertcat() {
    print(vertcat(ogDoubleMatrixData, ogDoubleMatrixData, ogDoubleAbsMatrixData));
  }

  @Test
  public void testOGArraytranspose() {
    System.out.println("transpose"+transpose(ogDoubleMatrixData).toString());
  }

  /* TEST OGIndex */
  @Test
  public void testOGIndexUnique() {
    assert (unique(ogIntRepeatMatrixData).equals(ogIntRepeatMatrixUniqueData));
  }

  @Test
  public void testOGIndexfliplr() {
    print(fliplr(ogIntMatrixData));
  }

  @Test
  public void testOGIndexflipud() {
    print(flipud(ogIntMatrixData));
  }

  @Test
  public void testOGIndexhorzcat() {
    print(horzcat(ogIntMatrixData, ogIntMatrixData, ogIntRepeatMatrixData));
  }

  @Test
  public void testOGIndexvertcat() {
    print(vertcat(ogIntMatrixData, ogIntMatrixData, ogIntRepeatMatrixData));
  }

  @Test
  public void testOGIndextranspose() {
    System.out.println("transpose"+transpose(horzcat(ogIntMatrixData, ogIntMatrixData, ogIntRepeatMatrixData)).toString());
  }

}
