/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class DoubleCurrencyLabelledMatrix2DTest {

  public void testAddUsingDoubleLabels() {
    DoubleCurrencyLabelledMatrix2D m1 = new DoubleCurrencyLabelledMatrix2D(
        new Double[] { 1d, 3d }, new Object[] { "1", "3" },
        new Currency[] { Currency.USD }, new Object[] { "USD" },
        new double[][] { new double[] { 0.3, 0.5 } });
    
    DoubleCurrencyLabelledMatrix2D m1Doubled = m1.addUsingDoubleLabels(m1);
    assertArrayEquals(m1.getXKeys(), m1Doubled.getXKeys());
    assertArrayEquals(m1.getXLabels(), m1Doubled.getXLabels());
    assertArrayEquals(m1.getYKeys(), m1Doubled.getYKeys());
    assertArrayEquals(m1.getYLabels(), m1Doubled.getYLabels());
    assertArrayEquals(new double[][] { new double[] { 0.6, 1.0 } }, m1Doubled.getValues());
    
    DoubleCurrencyLabelledMatrix2D m2 = new DoubleCurrencyLabelledMatrix2D(
        new Double[] { 1d, 2d }, new Object[] { "1", "2" },
        new Currency[] { Currency.USD }, new Object[] { "USD" },
        new double[][] { new double[] { 0.1, 0.7 } });
    
    DoubleCurrencyLabelledMatrix2D m1PlusM2 = m1.addUsingDoubleLabels(m2);
    assertArrayEquals(new Double[] { 1d, 2d, 3d }, m1PlusM2.getXKeys());
    assertArrayEquals(new Object[] { "1", "2", "3" }, m1PlusM2.getXLabels());
    assertArrayEquals(new Currency[] { Currency.USD }, m1PlusM2.getYKeys());
    assertArrayEquals(new Object[] { "USD" }, m1PlusM2.getYLabels());
    assertArrayEquals(new double[][] { new double[] { 0.4, 0.7, 0.5 } }, m1PlusM2.getValues());
    
    DoubleCurrencyLabelledMatrix2D m3 = new DoubleCurrencyLabelledMatrix2D(
        new Double[] { 1d, 2d }, new Object[] { "1", "2" },
        new Currency[] { Currency.GBP }, new Object[] { "GBP" },
        new double[][] { new double[] { 0.1, 0.7 } });
    DoubleCurrencyLabelledMatrix2D m1PlusM3 = m1.addUsingDoubleLabels(m3);
    assertArrayEquals(new Double[] { 1d, 2d, 3d }, m1PlusM3.getXKeys());
    assertArrayEquals(new Object[] { "1", "2", "3" }, m1PlusM3.getXLabels());
    assertArrayEquals(new Currency[] { Currency.GBP, Currency.USD }, m1PlusM3.getYKeys());
    assertArrayEquals(new Object[] { "GBP", "USD" }, m1PlusM3.getYLabels());
    assertArrayEquals(new double[][] { new double[] { 0.1, 0.7, 0.0 }, new double[] { 0.3, 0.0, 0.5 } }, m1PlusM3.getValues());
  }
  
}
