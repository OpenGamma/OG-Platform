/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
