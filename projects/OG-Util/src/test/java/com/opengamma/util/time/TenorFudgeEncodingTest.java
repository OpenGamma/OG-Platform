/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Tenor Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class TenorFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Tenor PERIOD_TENOR = Tenor.EIGHT_MONTHS;
  private static final Tenor BUSINESS_DAY_TENOR = Tenor.SN;

  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Tenor.class, PERIOD_TENOR);
    assertEncodeDecodeCycle(Tenor.class, BUSINESS_DAY_TENOR);
  }

  @Test
  public void testFromString() {
    assertEquals(PERIOD_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, PERIOD_TENOR.getPeriod().toString())));
    assertEquals(BUSINESS_DAY_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, BUSINESS_DAY_TENOR.getBusinessDayTenor().toString())));
    assertEquals(PERIOD_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, PERIOD_TENOR.toFormattedString())));
    assertEquals(BUSINESS_DAY_TENOR, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, BUSINESS_DAY_TENOR.toFormattedString())));
  }

}
