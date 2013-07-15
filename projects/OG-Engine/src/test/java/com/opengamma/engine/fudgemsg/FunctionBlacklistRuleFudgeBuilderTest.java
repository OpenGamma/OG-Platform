/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.blacklist.FunctionBlacklistRule;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link FunctionBlacklistRuleFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionBlacklistRuleFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    assertEncodeDecodeCycle(FunctionBlacklistRule.class, rule);
  }

  public void testPopulated() {
    final FunctionBlacklistRule rule = new FunctionBlacklistRule();
    rule.setFunctionIdentifier("Foo");
    rule.setFunctionParameters(new EmptyFunctionParameters());
    rule.setTarget(ComputationTargetSpecification.of(UniqueId.of("Test", "Bar")));
    rule.setInputs(Arrays.asList(new ValueSpecification("Foo", rule.getTarget(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Fn").get())));
    rule.setOutputs(Arrays.asList(new ValueSpecification("Bar", rule.getTarget(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Fn").get())));
    assertEncodeDecodeCycle(FunctionBlacklistRule.class, rule);
  }

}
