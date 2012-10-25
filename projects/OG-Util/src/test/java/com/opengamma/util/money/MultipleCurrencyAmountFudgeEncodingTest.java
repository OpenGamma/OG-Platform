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
public class MultipleCurrencyAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    MultipleCurrencyAmount object = MultipleCurrencyAmount.of(Currency.AUD, 101);
    object = object.plus(Currency.GBP, 300);
    object = object.plus(Currency.USD, 400);
    assertEncodeDecodeCycle(MultipleCurrencyAmount.class, object);
  }

}
