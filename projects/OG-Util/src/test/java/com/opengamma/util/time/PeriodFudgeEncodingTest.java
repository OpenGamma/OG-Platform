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
import org.threeten.bp.Period;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Period Fudge support.
 */
@Test(groups = TestGroup.UNIT)
public class PeriodFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Period s_ref = Period.ofDays(2);

  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Period.class, s_ref);
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Period.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.toString())));
  }

}
