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

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleMatrix2DConverterTest {
  
  private final DoubleMatrix2DConverter _converter = new DoubleMatrix2DConverter();
  
  @Test
  public void convertEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    
    Map<String, Double> actual = _converter.convert("Foo", new DoubleMatrix2D(new double[0][0]));
    
    assertEquals(expected, actual);
  }
  
  @Test
  public void convertNonEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Foo[0][0]", 5.5);
    expected.put("Foo[0][1]", 6.6);
    expected.put("Foo[1][0]", 7.7);
    expected.put("Foo[1][1]", 8.8);
    
    Map<String, Double> actual = _converter.convert("Foo", 
        new DoubleMatrix2D(new double[][] { new double[] { 5.5, 6.6 }, new double[] { 7.7, 8.8 } }));
    
    assertEquals(expected, actual);
  }

  @Test
  public void getConvertedClass() {
    assertEquals(DoubleMatrix2D.class, _converter.getConvertedClass());  
  }

}
