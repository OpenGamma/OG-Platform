/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.StringFieldType;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;

/**
 * Test StripInstrumentType Fudge support.
 */
public class StripInstrumentTypeTest extends FinancialTestBase {

  private static final StripInstrumentType s_ref = StripInstrumentType.FUTURE;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(StripInstrumentType.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(StripInstrumentType.class,
        FudgeMsgField.of(StringFieldType.INSTANCE, s_ref.name())));
  }

}
