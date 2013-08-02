/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GICSCodeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final GICSCode GICS = GICSCode.of("10203040");

  @Test
  public void testCycle() {
    assertEquals(GICS, cycleObject(GICSCode.class, GICS));
  }

  @Test
  public void testFromInteger() {
    assertEquals(GICS, getFudgeContext().getFieldValue(GICSCode.class,
        UnmodifiableFudgeField.of(FudgeWireType.INT, GICS.getCodeInt())));
  }

}
