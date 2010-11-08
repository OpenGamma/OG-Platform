/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class DoubleMatrix1DConverterTest {
  
  private final DoubleMatrix1DConverter _converter = new DoubleMatrix1DConverter();
  
  @Test
  public void convertEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    
    Map<String, Double> actual = _converter.convert("Foo", new DoubleMatrix1D(new double[0]));
    
    assertEquals(expected, actual);
  }
  
  @Test
  public void convertNonEmpty() {
    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Foo[0]", 5.5);
    expected.put("Foo[1]", 6.6);
    
    Map<String, Double> actual = _converter.convert("Foo", 
        new DoubleMatrix1D(new double[] { 5.5, 6.6 }));
    
    assertEquals(expected, actual);
  }

  @Test
  public void getConvertedClass() {
    assertEquals(DoubleMatrix1D.class, _converter.getConvertedClass());  
  }

}
