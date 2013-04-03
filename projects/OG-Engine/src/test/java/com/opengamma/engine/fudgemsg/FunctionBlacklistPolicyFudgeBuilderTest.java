/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.EmptyFunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link FunctionBlacklistPolicyFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionBlacklistPolicyFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    final FunctionBlacklistPolicy policy = new EmptyFunctionBlacklistPolicy();
    assertEncodeDecodeCycle(FunctionBlacklistPolicy.class, policy);
  }

  public void testPopulated() {
    final FunctionBlacklistPolicy policy = new DefaultFunctionBlacklistPolicy(UniqueId.of("Test", "Test"), 500, Arrays.asList(FunctionBlacklistPolicy.Entry.EXECUTION_NODE,
        FunctionBlacklistPolicy.Entry.FUNCTION, FunctionBlacklistPolicy.Entry.PARAMETERIZED_FUNCTION, FunctionBlacklistPolicy.Entry.WILDCARD.activationPeriod(1)));
    assertEncodeDecodeCycle(FunctionBlacklistPolicy.class, policy);
  }

}
