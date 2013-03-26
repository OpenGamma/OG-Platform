/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SecurityInfoTest extends AbstractTest {

  public void testRawSecurity() {
    final AbstractComparator comparator = new AbstractComparator(OpenGammaFudgeContext.getInstance()) {
    };
    final ComparisonContext context = comparator.createContext();
    final SecurityInfo raw1 = new SecurityInfo(context, createRawSecurity("Foo", 42));
    final SecurityInfo raw2 = new SecurityInfo(context, createRawSecurity("Foo", 0));
    final SecurityInfo raw3 = new SecurityInfo(context, createRawSecurity("Bar", 42));
    final SecurityInfo raw4 = new SecurityInfo(context, createRawSecurity("Foo", 42));
    assertTrue(raw1.equals(raw4));
    assertTrue(raw4.equals(raw1));
    assertFalse(raw1.equals(raw2));
    assertFalse(raw1.equals(raw3));
    assertFalse(raw2.equals(raw1));
    assertFalse(raw2.equals(raw3));
    assertFalse(raw3.equals(raw1));
    assertFalse(raw3.equals(raw2));
  }

}
