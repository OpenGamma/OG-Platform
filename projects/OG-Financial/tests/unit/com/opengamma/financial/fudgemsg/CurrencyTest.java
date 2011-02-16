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

import com.opengamma.core.common.CurrencyUnit;

public class CurrencyTest extends FinancialTestBase {

  private static final CurrencyUnit s_ref = CurrencyUnit.USD;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(CurrencyUnit.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(CurrencyUnit.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getCode(), null, null)));
  }

  @Test
  public void testFromUniqueIdentifier() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(CurrencyUnit.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getUniqueId().toString(), null, null)));
  }

}
