/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.riskfactor.ValueGreekDataBundle.DataType;

/**
 * @author emcleod
 * 
 */
public class GreekToValueGreekConversionVisitorTest {
  private static final Map<DataType, Double> DATA = new HashMap<DataType, Double>();
  private static final double S = 140;
  private static final double PV = 10;
  private static final double N = 30;
  private static final double DELTA = 0.3;
  private static final double GAMMA = 0.07;
  private static final double THETA = 0.12;
  private static final double RHO = 0.8;
  private static final GreekVisitor<Double> VISITOR = new GreekToValueGreekConversionVisitor(new ValueGreekDataBundle(DATA));
  private static final double EPS = 1e-12;

  static {
    DATA.put(DataType.UNDERLYING_PRICE, S);
    DATA.put(DataType.OPTION_POINT_VALUE, PV);
    DATA.put(DataType.NUMBER_OF_CONTRACTS, N);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new GreekToValueGreekConversionVisitor(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueGreekForPrice() {
    VISITOR.visitPrice();
  }

  @Test
  public void test() {
    assertNull(VISITOR.visitVega());
    assertEquals(DELTA * S * PV * N, VISITOR.visitDelta(), EPS);
    assertEquals(GAMMA * S * S * PV * N, VISITOR.visitGamma(), EPS);
  }
}
