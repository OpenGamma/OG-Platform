/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * 
 */
public class DoubleConverterTest {
  
  private final DoubleConverter _converter = new DoubleConverter();
  
  @Test
  public void convert() {
    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Foo", 5.5);
    
    Map<String, Double> actual = _converter.convert("Foo", 5.5);
    
    assertEquals(expected, actual);
  }

  @Test
  public void getConvertedClass() {
    assertEquals(Double.class, _converter.getConvertedClass());  
  }

}
