/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the dependency graph building under late resolution conditions
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphLateResolutionTest extends AbstractDependencyGraphBuilderTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DepGraphLateResolutionTest.class);

  public void backtrackCleanup() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      helper.addFunctionProducing(helper.getValue2Foo());
      final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
      final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement1Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          s_logger.debug("fnConv late resolving with inputs {}", inputs);
          assertEquals(1, inputs.size());
          final ValueSpecification input = inputs.keySet().iterator().next();
          if (!input.getProperties().getValues("TEST").contains("Bar")) {
            return Collections.emptySet();
          }
          return super.getResults(context, target, inputs);
        }

      };
      fnConv.addRequirement(helper.getRequirement2Any());
      helper.getFunctionRepository().addFunction(fnConv);
      DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(CompiledFunctionDefinition function) {
          if (function == fnConv) {
            return 1;
          }
          if (function == fn2Bar) {
            return -1;
          }
          return 0;
        }
      });
      builder.addTarget(helper.getRequirement1Bar());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertGraphContains(graph, fn2Bar, fnConv);
    } finally {
      TestLifecycle.end();
    }
  }

  public void additionalRequirements() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1Foo = helper.addFunctionProducing(helper.getValue1Foo());
      final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
      final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement1Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueRequirement> getAdditionalRequirements(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
          assertEquals(1, inputs.size());
          assertTrue(inputs.contains(helper.getSpec2Bar()));
          assertEquals(1, outputs.size());
          //final ValueSpecification expected = _result.compose(helper.getRequirement1Bar());
          //s_logger.debug("Outputs={}, expected={}", outputs, expected);
          //assertTrue(outputs.contains(expected));
          return Collections.singleton(helper.getRequirement1Foo());
        }

      };
      fnConv.addRequirement(helper.getRequirement2Any());
      helper.getFunctionRepository().addFunction(fnConv);
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1Bar());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertGraphContains(graph, fn2Bar, fnConv, fn1Foo);
    } finally {
      TestLifecycle.end();
    }
  }

  public void additionalRequirementBacktracking() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1Foo = helper.addFunctionProducing(helper.getValue1Foo());
      final MockFunction fn2Foo = helper.addFunctionProducing(helper.getValue2Foo());
      final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
      final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement1Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueRequirement> getAdditionalRequirements(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
          if (inputs.contains(helper.getSpec2Foo())) {
            return Collections.singleton(helper.getRequirement2Beta());
          } else {
            return Collections.singleton(helper.getRequirement1Foo());
          }
        }

      };
      fnConv.addRequirement(helper.getRequirement2Any());
      helper.getFunctionRepository().addFunction(fnConv);
      DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(CompiledFunctionDefinition function) {
          if (function == fn2Foo) {
            return 1;
          }
          return 0;
        }
      });
      builder.addTarget(helper.getRequirement1Bar());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertGraphContains(graph, fn2Bar, fnConv, fn1Foo);
    } finally {
      TestLifecycle.end();
    }
  }

}
