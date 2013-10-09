/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Currency s_ref = Currency.USD;

  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Currency.class, s_ref);
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getCode())));
  }

  @Test
  public void testFromUniqueId() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getUniqueId().toString())));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueId_bad1() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, "Rubbish~ID"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testFromUniqueId_bad2() {
    getFudgeContext().getFieldValue(Currency.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, Currency.OBJECT_SCHEME + "~Rubbish"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toFudgeMsg() {
    CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    MutableFudgeMsg msg = bld.buildMessage(getFudgeSerializer(), s_ref);
    assertEquals(ImmutableSet.of(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME), msg.getAllFieldNames());
    assertEquals("USD", msg.getString(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_fromFudgeMsg() {
    MutableFudgeMsg msg = getFudgeContext().newMessage();
    msg.add(CurrencyFudgeBuilder.CURRENCY_FIELD_NAME, "USD");
    CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_fromFudgeMsg_badMessage1() {
    MutableFudgeMsg msg = getFudgeContext().newMessage();
    CurrencyFudgeBuilder bld = new CurrencyFudgeBuilder();
    bld.buildObject(getFudgeDeserializer(), msg);
  }

}
