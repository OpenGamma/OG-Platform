/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.ISOChronology;
import javax.time.calendar.Period;

import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.StringFieldType;

/**
 * Test Period Fudge support.
 */
public class PeriodTest extends FinancialTestBase {

  private static final Period s_ref = Period.of(2, ISOChronology.periodDays());

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(Period.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Period.class,
        FudgeMsgField.of(StringFieldType.INSTANCE, s_ref.toString())));
  }

}
