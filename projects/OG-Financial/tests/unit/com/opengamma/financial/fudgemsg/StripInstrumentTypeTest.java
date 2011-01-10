/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.StringFieldType;
import org.junit.Test;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;

public class StripInstrumentTypeTest extends FinancialTestBase {

  private static final StripInstrumentType s_ref = StripInstrumentType.FUTURE;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(StripInstrumentType.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(StripInstrumentType.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.name(), null, null)));
  }

}
