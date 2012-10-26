/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class CurrencyAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    CurrencyAmount object = CurrencyAmount.of(Currency.AUD, 101);
    assertEncodeDecodeCycle(CurrencyAmount.class, object);
  }

}
