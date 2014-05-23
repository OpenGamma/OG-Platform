/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the dependency graph building with optional inputs to functions.
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphOptionalInputsTest extends AbstractDependencyGraphBuilderTest {

  private static class OptionalInputsFunction extends TestFunction {

    private final boolean _expect1;
    private final boolean _expect2;
    private final ValueRequirement _req1;
    private final ValueRequirement _req2;

    public OptionalInputsFunction(final DepGraphTestHelper helper, final boolean expect1, final boolean expect2) {
      _expect1 = expect1;
      _expect2 = expect2;
      _req1 = helper.getRequirement2();
      _req2 = helper.getRequirement1();
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return null;
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      final Set<ValueRequirement> req = Sets.newHashSetWithExpectedSize(4);
      req.add(_req1);
      req.add(_req2);
      return req;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      if (_expect1) {
        assertTrue(inputs.values().contains(_req1));
      } else {
        assertFalse(inputs.values().contains(_req1));
      }
      if (_expect2) {
        assertTrue(inputs.values().contains(_req2));
      } else {
        assertFalse(inputs.values().contains(_req2));
      }
      return getResults(context, target);
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return Collections.singleton(new ValueSpecification("OUT", target.toSpecification(), ValueProperties.all()));
    }

    @Override
    public boolean canHandleMissingRequirements() {
      return true;
    }

  }

  public void optionalInputsPresent() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing1and2();
      helper.getFunctionRepository().addFunction(new OptionalInputsFunction(helper, true, true));
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      expectCompletion(builder, builder.getContext().resolveRequirement(new ValueRequirement("OUT", helper.getTarget().toSpecification()), null, null));
    } finally {
      TestLifecycle.end();
    }
  }

  public void optionalInputsBothMissing() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.getFunctionRepository().addFunction(new OptionalInputsFunction(helper, false, false));
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      expectCompletion(builder, builder.getContext().resolveRequirement(new ValueRequirement("OUT", helper.getTarget().toSpecification()), null, null));
    } finally {
      TestLifecycle.end();
    }
  }

  public void optionalInputsOneMissing() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing2();
      helper.getFunctionRepository().addFunction(new OptionalInputsFunction(helper, true, false));
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      expectCompletion(builder, builder.getContext().resolveRequirement(new ValueRequirement("OUT", helper.getTarget().toSpecification()), null, null));
    } finally {
      TestLifecycle.end();
    }
  }

}
