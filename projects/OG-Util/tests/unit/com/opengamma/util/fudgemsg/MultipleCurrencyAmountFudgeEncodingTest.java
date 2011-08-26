/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class MultipleCurrencyAmountFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    MultipleCurrencyAmount object = MultipleCurrencyAmount.of(Currency.AUD, 101);
    object = object.plus(Currency.GBP, 300);
    object = object.plus(Currency.USD, 400);
    assertEncodeDecodeCycle(MultipleCurrencyAmount.class, object);
  }

}
