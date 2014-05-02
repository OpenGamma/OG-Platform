/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static com.opengamma.util.money.Currency.EUR;
import static com.opengamma.util.money.Currency.USD;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests related to ParameterSensitivity manipulations.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyParameterSensitivityTest {

  private static final OGMatrixAlgebra MATRIX = MatrixAlgebraFactory.OG_ALGEBRA;
  private static final DoubleMatrix1D SENSITIVITY_1_1 = new DoubleMatrix1D(4.0, 2.0, 5.0, 1.5);
  private static final DoubleMatrix1D SENSITIVITY_1_2 = new DoubleMatrix1D(4.0, 3.0, 5.0, 2.5);
  private static final DoubleMatrix1D SENSITIVITY_2_1 = new DoubleMatrix1D(5.0, 1.0, 2.0, 5.0, 1.5);
  private static final String NAME_1 = "Name1";
  private static final String NAME_2 = "Name2";
  private static final Pair<String, Currency> NAME_1_USD = Pairs.of(NAME_1, USD);
  private static final Pair<String, Currency> NAME_1_EUR = Pairs.of(NAME_1, EUR);
  private static final Pair<String, Currency> NAME_2_USD = Pairs.of(NAME_2, USD);
  private static final Pair<String, Currency> NAME_2_EUR = Pairs.of(NAME_2, EUR);

  private static final double TOLERANCE = 1.0E-5;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap1() {
    new MultipleCurrencyParameterSensitivity(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap2() {
    MultipleCurrencyParameterSensitivity.of(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullPair() {
    new MultipleCurrencyParameterSensitivity().plus(null, SENSITIVITY_1_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullMatrix() {
    new MultipleCurrencyParameterSensitivity().plus(NAME_1_EUR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullSensitivities() {
    new MultipleCurrencyParameterSensitivity().plus(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSensitivityNullNameCurrencyPair() {
    new MultipleCurrencyParameterSensitivity().getSensitivity(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSensitivityNullName() {
    new MultipleCurrencyParameterSensitivity().getSensitivity(null, Currency.EUR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSensitivityNullCurrency() {
    new MultipleCurrencyParameterSensitivity().getSensitivity(NAME_1, null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAddToUnmodifiableMap() {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map = new LinkedHashMap<>();
    map.put(NAME_1_EUR, SENSITIVITY_1_1);
    final MultipleCurrencyParameterSensitivity sensitivities = MultipleCurrencyParameterSensitivity.of(map);
    final Map<Pair<String, Currency>, DoubleMatrix1D> unmodifiable = sensitivities.getSensitivities();
    unmodifiable.put(NAME_1_USD, SENSITIVITY_1_2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConvertNullFXMatrix() {
    new MultipleCurrencyParameterSensitivity().converted(null, EUR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConvertNullCurrency() {
    new MultipleCurrencyParameterSensitivity().converted(new FXMatrix(), null);
  }

  @Test
  public void testObject() {
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map = new LinkedHashMap<>();
    map.put(NAME_1_EUR, SENSITIVITY_1_1);
    final MultipleCurrencyParameterSensitivity sensitivity = new MultipleCurrencyParameterSensitivity(map);
    assertFalse(sensitivity.getSensitivities() == map);
    assertEquals(map, sensitivity.getSensitivities());
    assertEquals(map.keySet(), sensitivity.getAllNamesCurrency());
    assertEquals(SENSITIVITY_1_1, sensitivity.getSensitivity(NAME_1_EUR));
    assertEquals(SENSITIVITY_1_1, sensitivity.getSensitivity(NAME_1, EUR));
    MultipleCurrencyParameterSensitivity other = new MultipleCurrencyParameterSensitivity(map);
    assertEquals(sensitivity, other);
    assertEquals(sensitivity.hashCode(), other.hashCode());
    map = new LinkedHashMap<>();
    assertFalse(sensitivity.getSensitivities().equals(map));
    map.put(NAME_1_EUR, SENSITIVITY_1_1);
    other = new MultipleCurrencyParameterSensitivity(map);
    assertEquals(sensitivity, other);
    map.put(NAME_1_USD, SENSITIVITY_1_2);
    map.put(NAME_2_USD, SENSITIVITY_2_1);
    other = MultipleCurrencyParameterSensitivity.of(map);
    assertFalse(sensitivity.equals(other));
    other = new MultipleCurrencyParameterSensitivity();
    assertTrue(other.getSensitivities().isEmpty());
  }

  @Test
  public void add() {
    MultipleCurrencyParameterSensitivity sensitivity1 = new MultipleCurrencyParameterSensitivity();
    sensitivity1 = sensitivity1.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity1 = sensitivity1.plus(NAME_1_USD, SENSITIVITY_1_2);
    assertEquals("Add same currency, different sensitivities: ",
                 MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2),
                 sensitivity1.getSensitivity(NAME_1_USD));
    MultipleCurrencyParameterSensitivity sensitivity2 = new MultipleCurrencyParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1_EUR, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(sensitivity1);
    assertEquals("Add different currency, test size: ", 2, sensitivity2.getSensitivities().size());
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> expected = Maps.newLinkedHashMap();
    expected.put(NAME_1_EUR, SENSITIVITY_1_1);
    expected.put(NAME_1_USD, (DoubleMatrix1D) MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2));
    assertEquals("Add different currency, test map: ", expected, sensitivity2.getSensitivities());
    sensitivity2 = new MultipleCurrencyParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(sensitivity1);
    assertEquals("Add same currency, test sensitivities: ", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1_USD));
    sensitivity2 = sensitivity2.plus(NAME_2_EUR, SENSITIVITY_2_1);
    assertEquals("Add different currency, test sensitivities of first currency: ",
                 SENSITIVITY_2_1,
                 sensitivity2.getSensitivity(NAME_2_EUR));
    assertEquals("Add different currency, test sensitivities of second currency: ", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1_USD));
    sensitivity2 = sensitivity2.plus(NAME_2_EUR, SENSITIVITY_2_1);
    assertEquals("Test add same sensitivities: ",
                 MATRIX.scale(SENSITIVITY_2_1, 2.0),
                 sensitivity2.getSensitivity(NAME_2_EUR));
    assertEquals("Test other currency: ",
                 MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)),
                 sensitivity2.getSensitivity(NAME_1_USD));
    sensitivity2 = sensitivity2.plus(NAME_1_EUR, SENSITIVITY_1_1);
    assertEquals("ParameterSensitivity: add",
                 MATRIX.scale(SENSITIVITY_2_1, 2.0),
                 sensitivity2.getSensitivity(NAME_2_EUR));
    assertEquals("ParameterSensitivity: add", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1_USD));
    assertEquals("ParameterSensitivity: add", SENSITIVITY_1_1, sensitivity2.getSensitivity(NAME_1_EUR));
  }

  @Test
  public void multiplyBy() {
    final double factor = 5.8;
    MultipleCurrencyParameterSensitivity sensitivity = new MultipleCurrencyParameterSensitivity();
    sensitivity = sensitivity.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity = sensitivity.multipliedBy(factor);
    assertEquals("Test multiplyBy, single name / currency pair: ", MATRIX.scale(SENSITIVITY_1_1, factor), sensitivity.getSensitivity(NAME_1_USD));
    MultipleCurrencyParameterSensitivity sensi2 = new MultipleCurrencyParameterSensitivity();
    sensi2 = sensi2.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensi2 = sensi2.plus(NAME_2_EUR, SENSITIVITY_2_1);
    sensi2 = sensi2.multipliedBy(factor);
    assertEquals("Test multiplyBy, first name / currency pair: ", MATRIX.scale(SENSITIVITY_1_1, factor), sensi2.getSensitivity(NAME_1_USD));
    assertEquals("Test multiplyBy, second name / currency pair: ", MATRIX.scale(SENSITIVITY_2_1, factor), sensi2.getSensitivity(NAME_2_EUR));
  }

  @Test
  public void converted() {
    final FXMatrix fxMatrix = new FXMatrix(EUR, USD, 1.25);
    MultipleCurrencyParameterSensitivity sensitivity = new MultipleCurrencyParameterSensitivity();
    sensitivity = sensitivity.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity = sensitivity.plus(NAME_1_EUR, SENSITIVITY_1_2);
    sensitivity = sensitivity.plus(NAME_2_EUR, SENSITIVITY_2_1);
    final MultipleCurrencyParameterSensitivity sensitivityUSDConverted = sensitivity.converted(fxMatrix, USD);
    MultipleCurrencyParameterSensitivity sensitivityUSDExpected = new MultipleCurrencyParameterSensitivity();
    sensitivityUSDExpected = sensitivityUSDExpected.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivityUSDExpected = sensitivityUSDExpected.plus(NAME_1_USD,
                                                         (DoubleMatrix1D) MATRIX.scale(SENSITIVITY_1_2,
                                                                                       fxMatrix.getFxRate(EUR, USD)));
    sensitivityUSDExpected = sensitivityUSDExpected.plus(NAME_2_USD, (DoubleMatrix1D) MATRIX.scale(SENSITIVITY_2_1, fxMatrix.getFxRate(EUR, USD)));
    assertTrue("Test convert: ",
               AssertSensitivityObjects.assertEquals("ParameterSensitivity: convert",
                                                   sensitivityUSDExpected,
                                                   sensitivityUSDConverted,
                                                   TOLERANCE));
  }

  @Test
  public void compare() {
    MultipleCurrencyParameterSensitivity sensitivity1 = new MultipleCurrencyParameterSensitivity();
    sensitivity1 = sensitivity1.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity1 = sensitivity1.plus(NAME_2_EUR, SENSITIVITY_2_1);
    MultipleCurrencyParameterSensitivity sensitivity2 = new MultipleCurrencyParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(NAME_2_EUR, SENSITIVITY_2_1);
    AssertSensitivityObjects.assertEquals("ParameterSensitivity: compare same data", sensitivity1, sensitivity2, TOLERANCE);
    AssertSensitivityObjects.assertDoesNotEqual("ParameterSensitivity: compare different data outside tolerance", sensitivity1.multipliedBy(2.0), sensitivity2, TOLERANCE);
    AssertSensitivityObjects.assertEquals("ParameterSensitivity: compare different data inside tolerance", sensitivity1.multipliedBy(1 + TOLERANCE / 10), sensitivity2, TOLERANCE);
    MultipleCurrencyParameterSensitivity sensitivity3 = new MultipleCurrencyParameterSensitivity();
    sensitivity3 = sensitivity3.plus(NAME_1_USD, SENSITIVITY_1_1);
    AssertSensitivityObjects.assertDoesNotEqual("ParameterSensitivity: compare data with different name / currency pairs",
                                              sensitivity1,
                                              sensitivity3,
                                              TOLERANCE);
  }

  @Test
  public void getAllNamesCurrency() {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map1 = Maps.newLinkedHashMap();
    map1.put(NAME_1_EUR, SENSITIVITY_1_1);
    map1.put(NAME_2_EUR, SENSITIVITY_1_2);
    map1.put(NAME_1_USD, SENSITIVITY_2_1);
    final MultipleCurrencyParameterSensitivity sensitivity1 = MultipleCurrencyParameterSensitivity.of(map1);
    assertEquals("ParameterSensitivity: getAllNamesCurrency", sensitivity1.getAllNamesCurrency(), sensitivity1.getSensitivities().keySet());
    assertEquals("ParameterSensitivity: getAllNamesCurrency", sensitivity1.getSensitivity(NAME_1, EUR), sensitivity1.getSensitivity(NAME_1_EUR));
    assertEquals("ParameterSensitivity: getAllNamesCurrency",
                 sensitivity1.getSensitivity(NAME_2, EUR),
                 sensitivity1.getSensitivity(NAME_2_EUR));
  }

  @Test
  public void getByName() {
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> map1 = Maps.newLinkedHashMap();
    map1.put(NAME_1_EUR, SENSITIVITY_1_1);
    map1.put(NAME_2_EUR, SENSITIVITY_1_2);
    map1.put(NAME_1_USD, SENSITIVITY_2_1);
    final MultipleCurrencyParameterSensitivity sensitivity1 = MultipleCurrencyParameterSensitivity.of(map1);
    assertEquals("ParameterSensitivity: getSensitivityByName",
                 ImmutableMap.of(EUR, SENSITIVITY_1_1, USD, SENSITIVITY_2_1),
                 sensitivity1.getSensitivityByName(NAME_1));
    assertEquals("ParameterSensitivity: getSensitivityByName",
                 ImmutableMap.of(EUR, SENSITIVITY_1_2),
                 sensitivity1.getSensitivityByName(NAME_2));
  }

  @Test
  public void equalHash() {
    MultipleCurrencyParameterSensitivity sensitivity = new MultipleCurrencyParameterSensitivity();
    sensitivity = sensitivity.plus(NAME_1_USD, SENSITIVITY_1_1);
    sensitivity = sensitivity.plus(NAME_2_EUR, SENSITIVITY_2_1);
    MultipleCurrencyParameterSensitivity modified = new MultipleCurrencyParameterSensitivity();
    modified = modified.plus(NAME_2_USD, SENSITIVITY_2_1);
    modified = modified.plus(NAME_2_EUR, SENSITIVITY_2_1);
    assertEquals("ParameterSensitivity: equalHash", sensitivity, sensitivity);
    assertEquals("ParameterSensitivity: equalHash", sensitivity.hashCode(), sensitivity.hashCode());
    assertFalse("ParameterSensitivity: equalHash", sensitivity.equals(SENSITIVITY_1_1));
    assertFalse("ParameterSensitivity: equalHash", sensitivity.equals(modified));
  }

}
