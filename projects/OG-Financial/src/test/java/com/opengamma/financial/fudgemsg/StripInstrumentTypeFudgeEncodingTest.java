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

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StripInstrumentTypeFudgeEncodingTest extends FinancialTestBase {

  private static final StripInstrumentType s_ref = StripInstrumentType.FUTURE;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(StripInstrumentType.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(StripInstrumentType.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.name())));
  }

}
