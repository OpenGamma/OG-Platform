/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Test NumberDeltaComparer.
 *
 * @author jonathan
 */
public class NumberDeltaComparerTest {
  
  @Test
  public void bothNulls() {
    NumberDeltaComparer ndc = new NumberDeltaComparer(4);
    assertFalse(ndc.isDelta(null, null));
  }
  
  @Test
  public void oneNulls() {
    NumberDeltaComparer ndc = new NumberDeltaComparer(4);
    assertTrue(ndc.isDelta(null, 1.5));
    assertTrue(ndc.isDelta(1.5, null));
  }
  
  @Test
  public void testDoubleFourDecimalPlaces() {
    NumberDeltaComparer ndc = new NumberDeltaComparer(4);
    
    assertFalse(ndc.isDelta(1.0000456789, 1.000056789));
    assertTrue(ndc.isDelta(1.000456789, 1.00056789));
    assertTrue(ndc.isDelta(1.000456789, 1.100456789));
    
    assertFalse(ndc.isDelta(99.99999999, 99.99998888));
    assertTrue(ndc.isDelta(99.999999999, 99.999899999));
    
    assertFalse(ndc.isDelta(0.0, 0.0000123456));
    assertTrue(ndc.isDelta(0.0, 0.000123456));
    
    assertFalse(ndc.isDelta(0.0000123456, -0.0000123456));
  }
  
  @Test
  public void testDoubleNoDecimalPlaces() {
    NumberDeltaComparer ndc = new NumberDeltaComparer(0);
    
    assertFalse(ndc.isDelta(1.456, 1.567));
    assertTrue(ndc.isDelta(1.123, 2.123));
    
    assertFalse(ndc.isDelta(99.0, 99.9));
    assertTrue(ndc.isDelta(99.9, 100.0));
  }
  
  @Test
  public void testDoubleMinusTwoDecimalPlaces() {
    // Means we're only interested in hundreds or more changing
    NumberDeltaComparer ndc = new NumberDeltaComparer(-2);
    
    assertFalse(ndc.isDelta(1000.123, 1099.999));
    assertTrue(ndc.isDelta(1000.123, 1100.123));
  }
  
  @Test
  public void testBigDecimalTenDecimalPlaces() {
    NumberDeltaComparer ndc = new NumberDeltaComparer(10);
    
    assertFalse(ndc.isDelta(new BigDecimal(25.1234567890123456), new BigDecimal(25.1234567890223456)));
    assertTrue(ndc.isDelta(new BigDecimal(25.1234567890123456), new BigDecimal(25.1234567891123456)));
  }
}
