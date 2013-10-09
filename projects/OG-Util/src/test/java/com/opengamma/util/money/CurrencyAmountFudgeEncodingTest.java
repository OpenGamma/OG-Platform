/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyAmountFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test() {
    CurrencyAmount object = CurrencyAmount.of(Currency.AUD, 101);
    assertEncodeDecodeCycle(CurrencyAmount.class, object);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromFudgeMsg_badMessage1() {
    MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyAmountFudgeBuilder.AMOUNT_FIELD_NAME, "100");
    CurrencyAmountFudgeBuilder bld = new CurrencyAmountFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromFudgeMsg_badMessage2() {
    MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyAmountFudgeBuilder.CURRENCY_FIELD_NAME, "USD");
    CurrencyAmountFudgeBuilder bld = new CurrencyAmountFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

}
