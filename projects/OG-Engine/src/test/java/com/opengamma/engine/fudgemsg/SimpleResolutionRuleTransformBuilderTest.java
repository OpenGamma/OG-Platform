/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.resolver.ApplyToAllTargets;
import com.opengamma.engine.function.resolver.ResolutionRuleTransform;
import com.opengamma.engine.function.resolver.SimpleResolutionRuleTransform;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link SimpleResolutionRuleTransformFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleResolutionRuleTransformBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testEmpty() {
    final SimpleResolutionRuleTransform transform = new SimpleResolutionRuleTransform();
    assertEncodeDecodeCycle(ResolutionRuleTransform.class, transform);
  }

  public void testPopulated () {
    final SimpleResolutionRuleTransform transform = new SimpleResolutionRuleTransform ();
    transform.suppressRule("Foo");
    transform.adjustRule("Bar", new EmptyFunctionParameters(), null, null);
    transform.adjustRule("Bar", null, ApplyToAllTargets.INSTANCE, null);
    transform.adjustRule("Bar", null, null, 42);
    transform.adjustRule("Cow", new EmptyFunctionParameters(), ApplyToAllTargets.INSTANCE, -42);
    assertEncodeDecodeCycle(ResolutionRuleTransform.class, transform);
  }

}
