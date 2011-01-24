/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.StringFieldType;
import org.junit.Test;

import com.opengamma.core.common.Currency;

public class CurrencyTest extends FinancialTestBase {

  private static final Currency s_ref = Currency.getInstance("USD");

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(Currency.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getISOCode(), null, null)));
  }

  @Test
  public void testFromUniqueIdentifier() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getUniqueId().toString(), null, null)));
  }

}
