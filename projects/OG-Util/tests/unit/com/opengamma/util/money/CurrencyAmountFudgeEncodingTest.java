/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.AbstractBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class CurrencyAmountFudgeEncodingTest extends AbstractBuilderTestCase {

  public void test() {
    CurrencyAmount object = CurrencyAmount.of(Currency.AUD, 101);
    assertEncodeDecodeCycle(CurrencyAmount.class, object);
  }

}
