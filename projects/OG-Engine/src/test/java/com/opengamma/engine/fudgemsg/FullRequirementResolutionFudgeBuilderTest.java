/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.RequirementResolution;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FullRequirementResolutionFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class FullRequirementResolutionFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  private ValueRequirement requirement() {
    return new ValueRequirement("Foo", ComputationTargetSpecification.NULL);
  }

  private ValueSpecification valueSpecification(final String function) {
    return new ValueSpecification("Foo", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, function).get());
  }

  private DependencyNodeFunction function(final String name) {
    return DependencyNodeFunctionImpl.of(name, EmptyFunctionParameters.INSTANCE);
  }

  public void testUnresolved() {
    final FullRequirementResolution resolution = new FullRequirementResolution(requirement());
    assertEncodeDecodeCycle(FullRequirementResolution.class, resolution);
  }

  public void testSingleResolution() {
    final FullRequirementResolution resolution = new FullRequirementResolution(requirement());
    resolution.addResolutions(Collections.singleton(new RequirementResolution(valueSpecification("Test"), function("Test"), Collections.<FullRequirementResolution>emptySet())));
    assertEncodeDecodeCycle(FullRequirementResolution.class, resolution);
  }

  public void testAmbiguousResolution() {
    final FullRequirementResolution resolution = new FullRequirementResolution(requirement());
    resolution.addResolutions(Arrays.asList(new RequirementResolution(valueSpecification("A"), function("A"), Collections.<FullRequirementResolution>emptySet()), new RequirementResolution(
        valueSpecification("B"), function("B"), Collections.<FullRequirementResolution>emptySet())));
    assertEncodeDecodeCycle(FullRequirementResolution.class, resolution);
  }

  public void testMultipleResolutions() {
    final FullRequirementResolution resolution = new FullRequirementResolution(requirement());
    resolution.addResolutions(Collections.singleton(new RequirementResolution(valueSpecification("A"), function("A"), Collections.<FullRequirementResolution>emptySet())));
    resolution.addResolutions(Collections.singleton(new RequirementResolution(valueSpecification("B"), function("B"), Collections.<FullRequirementResolution>emptySet())));
    assertEncodeDecodeCycle(FullRequirementResolution.class, resolution);
  }

  public void testMultipleAmbiguousResolutions() {
    final FullRequirementResolution resolution = new FullRequirementResolution(requirement());
    resolution.addResolutions(Arrays.asList(new RequirementResolution(valueSpecification("A"), function("A"), Collections.<FullRequirementResolution>emptySet()), new RequirementResolution(
        valueSpecification("B"), function("B"), Collections.<FullRequirementResolution>emptySet())));
    resolution.addResolutions(Arrays.asList(new RequirementResolution(valueSpecification("C"), function("C"), Collections.<FullRequirementResolution>emptySet()), null));
    assertEncodeDecodeCycle(FullRequirementResolution.class, resolution);
  }

}
