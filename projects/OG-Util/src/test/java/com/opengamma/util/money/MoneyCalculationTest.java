/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MoneyCalculationTest {

  public void test_add() {
    assertEquals(BigDecimal.valueOf(300, 2), MoneyCalculationUtils.add(BigDecimal.valueOf(2, 0), BigDecimal.valueOf(1, 0)));
    assertEquals(BigDecimal.valueOf(100, 2), MoneyCalculationUtils.add(BigDecimal.valueOf(3, 0), BigDecimal.valueOf(-2, 0)));
  }

  public void test_subtract() {
    assertEquals(BigDecimal.valueOf(100, 2), MoneyCalculationUtils.subtract(BigDecimal.valueOf(2, 0), BigDecimal.valueOf(1, 0)));
    assertEquals(BigDecimal.valueOf(500, 2), MoneyCalculationUtils.subtract(BigDecimal.valueOf(3, 0), BigDecimal.valueOf(-2, 0)));
  }

  public void test_rounded() {
    assertEquals(BigDecimal.valueOf(200, 2), MoneyCalculationUtils.rounded(BigDecimal.valueOf(2, 0)));
  }

}
