package com.opengamma.util.array;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * Tests methods on {@link ArrayUtils}.
 */
public class ArrayUtilsTest {

  private static List<Double> DOUBLE_LIST = ImmutableList.of(1.0, 2.0, 3.0, 4.0, 5.0);
  
  
  @Test
  public void toDoubleArray() {
    
    double[] result = ArrayUtils.toDoubleArray(DOUBLE_LIST);
    
    for (int i = 0; i < DOUBLE_LIST.size(); i++) {
      assertEquals(DOUBLE_LIST.get(i), result[i]);
    }
    
  }
}
