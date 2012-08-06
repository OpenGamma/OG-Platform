/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to ParameterSensitivity manipulations.
 */
public class ParameterSensitivityTest {

  private static final OGMatrixAlgebra MATRIX = new OGMatrixAlgebra();

  private static final DoubleMatrix1D SENSI_1_1 = new DoubleMatrix1D(4.0, 2.0, 5.0, 1.5);
  private static final DoubleMatrix1D SENSI_1_2 = new DoubleMatrix1D(4.0, 3.0, 5.0, 2.5);
  private static final DoubleMatrix1D SENSI_2_1 = new DoubleMatrix1D(5.0, 1.0, 2.0, 5.0, 1.5);
  private static final String NAME_1 = "Name1";
  private static final String NAME_2 = "Name2";
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final Pair<String, Currency> NAME_1_USD = new ObjectsPair<String, Currency>(NAME_1, USD);
  private static final Pair<String, Currency> NAME_1_EUR = new ObjectsPair<String, Currency>(NAME_1, EUR);
  private static final Pair<String, Currency> NAME_2_EUR = new ObjectsPair<String, Currency>(NAME_2, EUR);

  @Test
  public void add() {
    ParameterSensitivity sensi = new ParameterSensitivity();
    sensi = sensi.plus(NAME_1_USD, SENSI_1_1);
    sensi = sensi.plus(NAME_1_USD, SENSI_1_2);
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSI_1_1, SENSI_1_2), sensi.getSensitivity(NAME_1_USD));
    ParameterSensitivity sensi2 = new ParameterSensitivity();
    sensi2 = sensi2.plus(NAME_1_USD, SENSI_1_1);
    sensi2 = sensi2.plus(sensi);
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSI_1_1, MATRIX.add(SENSI_1_1, SENSI_1_2)), sensi2.getSensitivity(NAME_1_USD));
    sensi2 = sensi2.plus(NAME_2_EUR, SENSI_2_1);
    assertEquals("ParameterSensitivity: add", SENSI_2_1, sensi2.getSensitivity(NAME_2_EUR));
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSI_1_1, MATRIX.add(SENSI_1_1, SENSI_1_2)), sensi2.getSensitivity(NAME_1_USD));
    sensi2 = sensi2.plus(NAME_2_EUR, SENSI_2_1);
    assertEquals("ParameterSensitivity: add", MATRIX.scale(SENSI_2_1, 2.0), sensi2.getSensitivity(NAME_2_EUR));
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSI_1_1, MATRIX.add(SENSI_1_1, SENSI_1_2)), sensi2.getSensitivity(NAME_1_USD));
    sensi2 = sensi2.plus(NAME_1_EUR, SENSI_1_1);
    assertEquals("ParameterSensitivity: add", MATRIX.scale(SENSI_2_1, 2.0), sensi2.getSensitivity(NAME_2_EUR));
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSI_1_1, MATRIX.add(SENSI_1_1, SENSI_1_2)), sensi2.getSensitivity(NAME_1_USD));
    assertEquals("ParameterSensitivity: add", SENSI_1_1, sensi2.getSensitivity(NAME_1_EUR));
  }

  @Test
  public void multiplyBy() {
    double factor = 5.8;
    ParameterSensitivity sensi = new ParameterSensitivity();
    sensi = sensi.plus(NAME_1_USD, SENSI_1_1);
    sensi = sensi.multiplyBy(factor);
    assertEquals("ParameterSensitivity: multiplyBy", MATRIX.scale(SENSI_1_1, factor), sensi.getSensitivity(NAME_1_USD));
    ParameterSensitivity sensi2 = new ParameterSensitivity();
    sensi2 = sensi2.plus(NAME_1_USD, SENSI_1_1);
    sensi2 = sensi2.plus(NAME_2_EUR, SENSI_2_1);
    sensi2 = sensi2.multiplyBy(factor);
    assertEquals("ParameterSensitivity: multiplyBy", MATRIX.scale(SENSI_1_1, factor), sensi2.getSensitivity(NAME_1_USD));
    assertEquals("ParameterSensitivity: multiplyBy", MATRIX.scale(SENSI_2_1, factor), sensi2.getSensitivity(NAME_2_EUR));
  }

  @Test
  public void compare() {
    double tolerance = 1.0E-2;
    ParameterSensitivity sensi1 = new ParameterSensitivity();
    sensi1 = sensi1.plus(NAME_1_USD, SENSI_1_1);
    sensi1 = sensi1.plus(NAME_2_EUR, SENSI_2_1);
    ParameterSensitivity sensi2 = new ParameterSensitivity();
    sensi2 = sensi2.plus(NAME_1_USD, SENSI_1_1);
    sensi2 = sensi2.plus(NAME_2_EUR, SENSI_2_1);
    assertTrue("ParameterSensitivity: multiplyBy", ParameterSensitivity.compare(sensi1, sensi2, tolerance));
    assertFalse("ParameterSensitivity: multiplyBy", ParameterSensitivity.compare(sensi1.multiplyBy(2.0), sensi2, tolerance));
    assertTrue("ParameterSensitivity: multiplyBy", ParameterSensitivity.compare(sensi1.multiplyBy(1.000001), sensi2, tolerance));
    ParameterSensitivity sensi3 = new ParameterSensitivity();
    sensi3 = sensi3.plus(NAME_1_USD, SENSI_1_1);
    assertFalse("ParameterSensitivity: multiplyBy", ParameterSensitivity.compare(sensi1, sensi3, tolerance));
  }

}
