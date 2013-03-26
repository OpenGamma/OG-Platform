/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AvailableOutput} class.
 */
@Test(groups = TestGroup.UNIT)
public class AvailableOutputImplTest {

  private static final String VALUE_NAME = "Foo";
  private static final String SECURITY_TYPE_1 = "Swap";
  private static final String SECURITY_TYPE_2 = "Option";
  private static final String FUNCTION_ID_1 = "F1";
  private static final String FUNCTION_ID_2 = "F2";
  private static final String PROPERTY_1 = "P1";
  private static final String PROPERTY_2 = "P2";
  private static final String VALUE_A = "VA";
  private static final String VALUE_B = "VB";
  private static final String WILDCARD = "*";

  public void testAvailableOnPosition() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    assertFalse(output.isAvailableOnPosition());
    output.setPortfolioNodeProperties(ValueProperties.none());
    assertFalse(output.isAvailableOnPosition());
    output.setPositionProperties(ValueProperties.none(), SECURITY_TYPE_1);
    assertTrue(output.isAvailableOnPosition());
  }

  public void testAvailableOn() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    assertFalse(output.isAvailableOn(SECURITY_TYPE_1));
    assertFalse(output.isAvailableOn(SECURITY_TYPE_2));
    output.setPortfolioNodeProperties(ValueProperties.none());
    assertFalse(output.isAvailableOn(SECURITY_TYPE_1));
    assertFalse(output.isAvailableOn(SECURITY_TYPE_2));
    output.setPositionProperties(ValueProperties.none(), SECURITY_TYPE_1);
    assertTrue(output.isAvailableOn(SECURITY_TYPE_1));
    assertFalse(output.isAvailableOn(SECURITY_TYPE_2));
    output.setPositionProperties(ValueProperties.none(), SECURITY_TYPE_2);
    assertTrue(output.isAvailableOn(SECURITY_TYPE_1));
    assertTrue(output.isAvailableOn(SECURITY_TYPE_2));
  }

  public void testAvailableOnPortfolioNode() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    assertFalse(output.isAvailableOnPortfolioNode());
    output.setPositionProperties(ValueProperties.none(), SECURITY_TYPE_1);
    assertFalse(output.isAvailableOnPortfolioNode());
    output.setPortfolioNodeProperties(ValueProperties.none());
    assertTrue(output.isAvailableOnPortfolioNode());
  }

  public void testPropertiesNone() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.none());
    output.setPositionProperties(ValueProperties.none(), SECURITY_TYPE_1);
    assertEquals(output.getProperties(), ValueProperties.none());
    assertEquals(output.getPortfolioNodeProperties(), ValueProperties.none());
    assertEquals(output.getPositionProperties(SECURITY_TYPE_1), ValueProperties.none());
  }

  public void testPropertiesAll() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).get());
    output.setPositionProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).get(), SECURITY_TYPE_1);
    output.setPositionProperties(ValueProperties.all(), SECURITY_TYPE_2);
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).get());
    assertEquals(output.getPortfolioNodeProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).get());
    assertEquals(output.getPositionProperties(SECURITY_TYPE_1), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).get());
    assertEquals(output.getPositionProperties(SECURITY_TYPE_2), ValueProperties.all());
  }

  public void testPropertiesWildCardNoIndicator() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(PROPERTY_1, VALUE_A, VALUE_B).withAny(PROPERTY_2).get());
    output.setPortfolioNodeProperties(ValueProperties.withAny(PROPERTY_1).withAny(PROPERTY_2).get());
    assertEquals(output.getProperties(), ValueProperties.with(PROPERTY_1, VALUE_A, VALUE_B).withAny(PROPERTY_2).get());
  }

  public void testPropertiesWildCardIndicator() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, WILDCARD);
    output.setPortfolioNodeProperties(ValueProperties.with(PROPERTY_1, VALUE_A, VALUE_B).withAny(PROPERTY_2).get());
    output.setPortfolioNodeProperties(ValueProperties.withAny(PROPERTY_1).withAny(PROPERTY_2).get());
    assertEquals(output.getProperties(), ValueProperties.with(PROPERTY_1, VALUE_A, VALUE_B, WILDCARD).withAny(PROPERTY_2).get());
  }

  public void testPropertiesMergeSimple() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).with(PROPERTY_1, VALUE_A).get());
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).with(PROPERTY_1, VALUE_B).get());
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).with(PROPERTY_1, VALUE_A, VALUE_B).get());
  }

  public void testPropertiesMergeOptional() {
    AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).with(PROPERTY_1, VALUE_A).get());
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).withOptional(PROPERTY_1).with(PROPERTY_1, VALUE_B).get());
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).withOptional(PROPERTY_1).with(PROPERTY_1, VALUE_A, VALUE_B).get());
    output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).withOptional(PROPERTY_1).with(PROPERTY_1, VALUE_A).get());
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).with(PROPERTY_1, VALUE_B).get());
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).withOptional(PROPERTY_1).with(PROPERTY_1, VALUE_A, VALUE_B).get());
  }

  public void testPropertiesMergeMissing() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).with(PROPERTY_1, VALUE_A).get());
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).with(PROPERTY_2, VALUE_A).get());
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).withOptional(PROPERTY_1).with(PROPERTY_1, VALUE_A).withOptional(PROPERTY_2)
        .with(PROPERTY_2, VALUE_A).get());
  }

  public void testPropertiesMergeAny() {
    final AvailableOutputImpl output = new AvailableOutputImpl(VALUE_NAME, null);
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1).with(PROPERTY_1, VALUE_A).withAny(PROPERTY_2).get());
    output.setPortfolioNodeProperties(ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_2).withAny(PROPERTY_1).with(PROPERTY_2, VALUE_A).get());
    assertEquals(output.getProperties(), ValueProperties.with(ValuePropertyNames.FUNCTION, FUNCTION_ID_1, FUNCTION_ID_2).with(PROPERTY_1, VALUE_A).with(PROPERTY_2, VALUE_A).get());
  }

}
