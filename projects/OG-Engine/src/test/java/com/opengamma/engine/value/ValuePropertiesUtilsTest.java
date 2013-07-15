/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ValuePropertiesUtilsTest {
  private static final ValueProperties CURVE_PROPERTIES = ValueProperties.builder()
      .with(ValuePropertyNames.CURVE, "CurveName")
      .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
      .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
      .get();
  private static final ValueProperties PAY_CURVE_PROPERTIES = ValueProperties.builder()
      .with(ValuePropertyNames.PAY_CURVE, "PayCurveName")
      .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName")
      .get();
  
  @Test
  public void testAddOptional() {
    final ValueProperties properties = ValuePropertiesUtils.addOptional(CURVE_PROPERTIES, ValuePropertyNames.PAY_CURVE, "PayCurveName").get();
    assertFalse(CURVE_PROPERTIES == properties);
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName")
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
        .with(ValuePropertyNames.PAY_CURVE, "PayCurveName").withOptional(ValuePropertyNames.PAY_CURVE)
        .get();
    assertEquals(expectedProperties, properties);
  }
  
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAddOptionalDifferentValue() {
    ValuePropertiesUtils.addOptional(CURVE_PROPERTIES, ValuePropertyNames.CURVE, "OtherCurveName").get();
  }

  @Test
  public void testAddOptionalSameProperty() {
    final ValueProperties properties = ValuePropertiesUtils.addOptional(CURVE_PROPERTIES, ValuePropertyNames.CURVE, "CurveName").get();
    assertFalse(CURVE_PROPERTIES == properties);
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName").withOptional(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
        .get();
    assertEquals(expectedProperties, properties);
  }

  @Test
  public void testAddAllOptional() {
    final ValueProperties properties = ValuePropertiesUtils.addAllOptional(CURVE_PROPERTIES, PAY_CURVE_PROPERTIES).get();
    assertFalse(CURVE_PROPERTIES == properties);
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName")
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
        .with(ValuePropertyNames.PAY_CURVE, "PayCurveName").withOptional(ValuePropertyNames.PAY_CURVE)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName").withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .get();
    assertEquals(expectedProperties, properties);
  }
  
  @Test(expectedExceptions = IllegalStateException.class)
  public void testAddAllOptionalDifferentValue() {
    final ValueProperties payCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "PayCurveName")
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName")
        .get();
    ValuePropertiesUtils.addAllOptional(CURVE_PROPERTIES, payCurveProperties).get();
  }

  @Test
  public void testAddAllOptionalSameProperty() {
    final ValueProperties payCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName")
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName")
        .get();
    final ValueProperties properties = ValuePropertiesUtils.addAllOptional(CURVE_PROPERTIES, payCurveProperties).get();
    assertFalse(CURVE_PROPERTIES == properties);
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName").withOptional(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName").withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .get();
    assertEquals(expectedProperties, properties);
  }
  
  @Test
  public void testRemoveAllNullSupplied() {
    final ValueProperties properties = ValuePropertiesUtils.removeAll(CURVE_PROPERTIES, (String[]) null).get();
    assertFalse(CURVE_PROPERTIES == properties);
    assertEquals(CURVE_PROPERTIES, properties);
  }
  
  @Test
  public void testRemoveAllEmptySupplied() {
    final ValueProperties properties = ValuePropertiesUtils.removeAll(CURVE_PROPERTIES, new String[0]).get();
    assertFalse(CURVE_PROPERTIES == properties);
    assertEquals(CURVE_PROPERTIES, properties);
  }
  
  @Test
  public void testRemoveAllNoOverlap() {
    final ValueProperties properties = ValuePropertiesUtils.removeAll(CURVE_PROPERTIES, ValuePropertyNames.RECEIVE_CURVE, ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG).get();
    assertFalse(CURVE_PROPERTIES == properties);
    assertEquals(CURVE_PROPERTIES, properties);    
  }
  
  @Test
  public void testRemoveAll() {
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .get();
    final ValueProperties remainingProperties = ValuePropertiesUtils.removeAll(CURVE_PROPERTIES, ValuePropertyNames.CURVE, 
        ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, ValuePropertyNames.CURVE_CALCULATION_METHOD).get();
    assertFalse(CURVE_PROPERTIES == remainingProperties);
    assertEquals(expectedProperties, remainingProperties);
  }
  
  @Test
  public void testGetAllOptional() {
    assertEquals(ValueProperties.builder().get(), ValuePropertiesUtils.getAllOptional(CURVE_PROPERTIES).get());
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName").withOptional(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, "CurveCalculationConfigName")
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "CurveCalculationMethodName")
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName").withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE).withOptional(ValuePropertyNames.RECEIVE_CURVE)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, "ReceiveCurveName1", "ReceiveCurveName2").withOptional(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .get();
    final ValueProperties expectedProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, "CurveName").withOptional(ValuePropertyNames.CURVE)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, "PayCurveCalculationConfigName").withOptional(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE).withOptional(ValuePropertyNames.RECEIVE_CURVE)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, "ReceiveCurveName1", "ReceiveCurveName2").withOptional(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .get();
    final ValueProperties optionalProperties = ValuePropertiesUtils.getAllOptional(properties).get();
    assertFalse(properties == optionalProperties);
    assertEquals(expectedProperties, optionalProperties);
  }
}
