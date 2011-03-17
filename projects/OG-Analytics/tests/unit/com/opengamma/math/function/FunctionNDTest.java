package com.opengamma.math.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class FunctionNDTest {
  private static final FunctionND<Double, Double> F = new MyFunction(3);
  
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDimension() {
    new MyFunction(-4);
  }
 
  @Test(expected = IllegalArgumentException.class)
  public void testTooFewArguments() {
    F.evaluate(1., 2.);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testTooManyArguments() {
    F.evaluate(1., 2., 3., 4., 5., 6.);
  }
  
  @Test
  public void test() {
    assertEquals(3, F.getDimension());
    MyFunction other = new MyFunction(3);
    assertEquals(F, other);
    assertEquals(F.hashCode(), other.hashCode());
    other = new MyFunction(4);
    assertFalse(other.equals(F));
  }
  
  private static class MyFunction extends FunctionND<Double, Double> {
    public MyFunction(int dimension) {
      super(dimension);
    }

    @Override
    protected Double evaluateFunction(Double[] x) {
      return x[0] + x[1] * x[1] + x[2] * x[2] * x[3];
    }
  }
}
