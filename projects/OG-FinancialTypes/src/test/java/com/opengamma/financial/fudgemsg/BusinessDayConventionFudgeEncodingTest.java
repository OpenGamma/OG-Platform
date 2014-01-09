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

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BusinessDayConventionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final BusinessDayConvention s_ref = BusinessDayConventions.MODIFIED_FOLLOWING;

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(BusinessDayConvention.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(BusinessDayConvention.class, UnmodifiableFudgeField.of(FudgeWireType.STRING, s_ref.getName())));
  }

}
