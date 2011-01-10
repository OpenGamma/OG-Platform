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

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;

public class BusinessDayConventionTest extends FinancialTestBase {

  private static final BusinessDayConvention s_ref = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(BusinessDayConvention.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(BusinessDayConvention.class, new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getConventionName(), null, null)));
  }

}
