/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebraFactory;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to ParameterSensitivity manipulations.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleParameterSensitivityTest {

  private static final OGMatrixAlgebra MATRIX = MatrixAlgebraFactory.OG_ALGEBRA;
  private static final DoubleMatrix1D SENSITIVITY_1_1 = new DoubleMatrix1D(4.0, 2.0, 5.0, 1.5);
  private static final DoubleMatrix1D SENSITIVITY_1_2 = new DoubleMatrix1D(4.0, 3.0, 5.0, 2.5);
  private static final DoubleMatrix1D SENSITIVITY_2_1 = new DoubleMatrix1D(5.0, 1.0, 2.0, 5.0, 1.5);
  private static final String NAME_1 = "Name1";
  private static final String NAME_2 = "Name2";

  private static final double TOLERANCE = 1.0E-5;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap1() {
    new SimpleParameterSensitivity(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullPair() {
    new SimpleParameterSensitivity().plus(null, SENSITIVITY_1_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullMatrix() {
    new SimpleParameterSensitivity().plus(NAME_1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullSensitivities() {
    new SimpleParameterSensitivity().plus(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetSensitivityNullName() {
    new SimpleParameterSensitivity().getSensitivity(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void addWrongSize() {
    SimpleParameterSensitivity sensitivity1 = new SimpleParameterSensitivity();
    sensitivity1 = sensitivity1.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity1.plus(NAME_1, SENSITIVITY_2_1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testAddToUnmodifiableMap() {
    final LinkedHashMap<String, DoubleMatrix1D> map = new LinkedHashMap<>();
    map.put(NAME_1, SENSITIVITY_1_1);
    final SimpleParameterSensitivity sensitivities = new SimpleParameterSensitivity(map);
    final Map<String, DoubleMatrix1D> unmodifiable = sensitivities.getSensitivities();
    unmodifiable.put(NAME_1, SENSITIVITY_1_2);
  }

  @Test
  public void testObject() {
    LinkedHashMap<String, DoubleMatrix1D> map = new LinkedHashMap<>();
    map.put(NAME_1, SENSITIVITY_1_1);
    final SimpleParameterSensitivity sensitivity = new SimpleParameterSensitivity(map);
    assertFalse(sensitivity.getSensitivities() == map);
    assertEquals(map, sensitivity.getSensitivities());
    assertEquals(map.keySet(), sensitivity.getAllNames());
    assertEquals(SENSITIVITY_1_1, sensitivity.getSensitivity(NAME_1));
    SimpleParameterSensitivity other = new SimpleParameterSensitivity(map);
    assertEquals(sensitivity, other);
    assertEquals(sensitivity.hashCode(), other.hashCode());
    map = new LinkedHashMap<>();
    assertFalse(sensitivity.getSensitivities().equals(map));
    map.put(NAME_1, SENSITIVITY_1_1);
    other = new SimpleParameterSensitivity(map);
    assertEquals(sensitivity, other);
    map.put(NAME_1, SENSITIVITY_1_2);
    map.put(NAME_2, SENSITIVITY_2_1);
    other = new SimpleParameterSensitivity(map);
    assertFalse(sensitivity.equals(other));
    other = new SimpleParameterSensitivity();
    assertTrue(other.getSensitivities().isEmpty());
  }

  @Test
  public void plus() {
    SimpleParameterSensitivity sensitivity1 = new SimpleParameterSensitivity();
    sensitivity1 = sensitivity1.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity1 = sensitivity1.plus(NAME_1, SENSITIVITY_1_2);
    assertEquals("Add same currency, different sensitivities: ", MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2), sensitivity1.getSensitivity(NAME_1));
    SimpleParameterSensitivity sensitivity2 = new SimpleParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(sensitivity1);
    sensitivity2 = new SimpleParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(sensitivity1);
    assertEquals("Add same currency, test sensitivities: ", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1));
    sensitivity2 = sensitivity2.plus(NAME_2, SENSITIVITY_2_1);
    assertEquals("Add different currency, test sensitivities of first name: ", SENSITIVITY_2_1, sensitivity2.getSensitivity(NAME_2));
    assertEquals("Add different currency, test sensitivities of second name: ", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1));
    sensitivity2 = sensitivity2.plus(NAME_2, SENSITIVITY_2_1);
    assertEquals("Test add same sensitivities: ", MATRIX.scale(SENSITIVITY_2_1, 2.0), sensitivity2.getSensitivity(NAME_2));
    assertEquals("Test other currency: ", MATRIX.add(SENSITIVITY_1_1, MATRIX.add(SENSITIVITY_1_1, SENSITIVITY_1_2)), sensitivity2.getSensitivity(NAME_1));
    sensitivity2 = sensitivity2.plus(NAME_1, SENSITIVITY_1_1);
    assertEquals("SimpleParameterSensitivity: plus", MATRIX.scale(SENSITIVITY_2_1, 2.0), sensitivity2.getSensitivity(NAME_2));
    SimpleParameterSensitivity sensitivity3 = new SimpleParameterSensitivity();
    sensitivity3 = sensitivity3.plus("New name", SENSITIVITY_1_1);
    sensitivity3 = sensitivity3.plus(NAME_1, SENSITIVITY_1_1);
    SimpleParameterSensitivity sensitivity4 = new SimpleParameterSensitivity();
    sensitivity4 = sensitivity4.plus("New name", SENSITIVITY_1_1);
    SimpleParameterSensitivity sensitivity5 = new SimpleParameterSensitivity();
    sensitivity5 = sensitivity5.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity4 = sensitivity4.plus(sensitivity5);
    AssertSensitivityObjects.assertEquals("SimpleParameterSensitivity: plus", sensitivity3, sensitivity4, TOLERANCE);
  }

  @Test
  public void multiplyBy() {
    final double factor = 5.8;
    SimpleParameterSensitivity sensitivity = new SimpleParameterSensitivity();
    sensitivity = sensitivity.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity = sensitivity.multipliedBy(factor);
    assertEquals("Test multiplyBy, single name / currency pair: ", MATRIX.scale(SENSITIVITY_1_1, factor), sensitivity.getSensitivity(NAME_1));
    SimpleParameterSensitivity sensi2 = new SimpleParameterSensitivity();
    sensi2 = sensi2.plus(NAME_1, SENSITIVITY_1_1);
    sensi2 = sensi2.plus(NAME_2, SENSITIVITY_2_1);
    sensi2 = sensi2.multipliedBy(factor);
    assertEquals("Test multiplyBy, first name / currency pair: ", MATRIX.scale(SENSITIVITY_1_1, factor), sensi2.getSensitivity(NAME_1));
    assertEquals("Test multiplyBy, second name / currency pair: ", MATRIX.scale(SENSITIVITY_2_1, factor), sensi2.getSensitivity(NAME_2));
  }

  @Test
  public void compare() {
    SimpleParameterSensitivity sensitivity1 = new SimpleParameterSensitivity();
    sensitivity1 = sensitivity1.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity1 = sensitivity1.plus(NAME_2, SENSITIVITY_2_1);
    SimpleParameterSensitivity sensitivity2 = new SimpleParameterSensitivity();
    sensitivity2 = sensitivity2.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity2 = sensitivity2.plus(NAME_2, SENSITIVITY_2_1);
    AssertSensitivityObjects.assertEquals("ParameterSensitivity: compare same data", sensitivity1, sensitivity2, TOLERANCE);
    AssertSensitivityObjects.assertDoesNotEqual("ParameterSensitivity: compare different data outside tolerance", sensitivity1.multipliedBy(2.0), sensitivity2, TOLERANCE);
    AssertSensitivityObjects.assertEquals("ParameterSensitivity: compare different data inside tolerance", sensitivity1.multipliedBy(1 + TOLERANCE / 10), sensitivity2, TOLERANCE);
    SimpleParameterSensitivity sensitivity3 = new SimpleParameterSensitivity();
    sensitivity3 = sensitivity3.plus(NAME_1, SENSITIVITY_1_1);
    AssertSensitivityObjects.assertDoesNotEqual("ParameterSensitivity: compare data with different name / currency pairs", sensitivity1, sensitivity3, TOLERANCE);
  }

  @Test
  public void getAllNamesCurrency() {
    final LinkedHashMap<String, DoubleMatrix1D> map1 = Maps.newLinkedHashMap();
    map1.put(NAME_1, SENSITIVITY_1_1);
    map1.put(NAME_2, SENSITIVITY_1_2);
    final SimpleParameterSensitivity sensitivity1 = new SimpleParameterSensitivity(map1);
    assertEquals("ParameterSensitivity: getAllNamesCurrency", sensitivity1.getAllNames(), sensitivity1.getSensitivities().keySet());
    assertEquals("ParameterSensitivity: getAllNamesCurrency", sensitivity1.getSensitivity(NAME_1), sensitivity1.getSensitivity(NAME_1));
    assertEquals("ParameterSensitivity: getAllNamesCurrency", sensitivity1.getSensitivity(NAME_2), sensitivity1.getSensitivity(NAME_2));
  }

  @Test
  public void equalHash() {
    SimpleParameterSensitivity sensitivity = new SimpleParameterSensitivity();
    sensitivity = sensitivity.plus(NAME_1, SENSITIVITY_1_1);
    sensitivity = sensitivity.plus(NAME_2, SENSITIVITY_2_1);
    SimpleParameterSensitivity modified = new SimpleParameterSensitivity();
    modified = modified.plus(NAME_2, SENSITIVITY_2_1);
    modified = modified.plus(NAME_2, SENSITIVITY_2_1);
    assertEquals("ParameterSensitivity: equalHash", sensitivity, sensitivity);
    assertEquals("ParameterSensitivity: equalHash", sensitivity.hashCode(), sensitivity.hashCode());
    assertFalse("ParameterSensitivity: equalHash", sensitivity.equals(SENSITIVITY_1_1));
    assertFalse("ParameterSensitivity: equalHash", sensitivity.equals(modified));
  }

}
