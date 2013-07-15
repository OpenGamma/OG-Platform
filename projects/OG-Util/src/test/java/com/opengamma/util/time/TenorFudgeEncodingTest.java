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

  private static final Tenor s_ref = Tenor.EIGHT_MONTHS;

  @Test
  public void testCycle() {
    assertEncodeDecodeCycle(Tenor.class, s_ref);
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Tenor.class,
        UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getPeriod().toString())));
  }

}
