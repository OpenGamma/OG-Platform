/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.financial.security.equity.GICSCode;

/**
 * Test GICSCode Fudge support.
 */
public class GICSCodeTest extends FinancialTestBase {

  private static final GICSCode s_ref = GICSCode.getInstance(10203040);

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(GICSCode.class, s_ref));
  }

  @Test
  public void testFromInteger () {
    assertEquals(s_ref, getFudgeContext().getFieldValue(GICSCode.class,
        UnmodifiableFudgeField.of(FudgeWireType.INT, s_ref.getCode())));
  }

}
