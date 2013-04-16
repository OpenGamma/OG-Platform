/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingFudgeBuilder;
import com.opengamma.util.test.TestGroup;

/**
 * Test OpenGammaFudgeContext.
 */
@Test(groups = TestGroup.UNIT)
public class OpenGammaFudgeContextTest {

  @Test
  public void test_context() {
    // simple basic test
    FudgeContext context = OpenGammaFudgeContext.getInstance();
    assertNotNull(context);
    assertNotNull(context.getObjectDictionary());
    assertTrue(context.getObjectDictionary().getObjectBuilder(Paging.class) instanceof PagingFudgeBuilder);
    assertTrue(context.getObjectDictionary().getMessageBuilder(Paging.class) instanceof PagingFudgeBuilder);
  }

  @Test
  public void test_cached() {
    assertSame(OpenGammaFudgeContext.getInstance(), OpenGammaFudgeContext.getInstance());
  }

}
