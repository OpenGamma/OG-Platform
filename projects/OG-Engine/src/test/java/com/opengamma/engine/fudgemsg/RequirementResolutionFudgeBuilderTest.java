/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.RequirementResolution;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link RequirementResolutionFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class RequirementResolutionFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  private ValueSpecification valueSpecification() {
    return new ValueSpecification("Foo", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
  }

  private DependencyNodeFunction functionNoParameters() {
    return DependencyNodeFunctionImpl.of("Test", EmptyFunctionParameters.INSTANCE);
  }

  private DependencyNodeFunction functionWithParameters() {
    return DependencyNodeFunctionImpl.of("Test", new SimpleFunctionParameters());
  }

  private ValueRequirement requirement(final String name) {
    return new ValueRequirement(name, ComputationTargetSpecification.NULL);
  }

  public void testNoInputs() {
    final RequirementResolution resolution = new RequirementResolution(valueSpecification(), functionNoParameters(), Collections.<FullRequirementResolution>emptySet());
    assertEncodeDecodeCycle(RequirementResolution.class, resolution);
  }

  public void testInputs() {
    final Collection<FullRequirementResolution> inputs = new ArrayList<FullRequirementResolution>();
    inputs.add(new FullRequirementResolution(requirement("A")));
    inputs.add(new FullRequirementResolution(requirement("B")));
    final RequirementResolution resolution = new RequirementResolution(valueSpecification(), functionNoParameters(), inputs);
    assertEncodeDecodeCycle(RequirementResolution.class, resolution);
  }

  public void testFunctionParameters() {
    final RequirementResolution resolution = new RequirementResolution(valueSpecification(), functionWithParameters(), Collections.<FullRequirementResolution>emptySet());
    assertEncodeDecodeCycle(RequirementResolution.class, resolution);
  }

}
