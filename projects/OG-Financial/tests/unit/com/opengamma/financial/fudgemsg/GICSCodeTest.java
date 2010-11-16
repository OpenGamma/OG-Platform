/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.junit.Test;

import com.opengamma.master.security.financial.GICSCode;

public class GICSCodeTest extends FinancialTestBase {

  private static final GICSCode s_ref = GICSCode.getInstance(10203040);

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(GICSCode.class, s_ref));
  }

  @Test
  public void testFromInteger () {
    assertEquals(s_ref, getFudgeContext().getFieldValue(GICSCode.class,
        new FudgeMsgField(PrimitiveFieldTypes.INT_TYPE, s_ref.getCode(), null, null)));
  }

}
