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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.MapComputationTargetResolver;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the dependency graph building with functions that implement conversion strategies (e.g. for currency)
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphConversionTest extends AbstractDependencyGraphBuilderTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DepGraphConversionTest.class);

  public void functionWithStaticConversion() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
      final MockFunction fnConv = new MockFunction(helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req2bar = helper.getRequirement2Bar();
          return Collections.singleton(new ValueSpecification(req2bar.getValueName(), target.toSpecification(), req2bar.getConstraints().copy()
              .with(ValuePropertyNames.FUNCTION, getUniqueId()).get()));
        }

      };
      fnConv.addRequirement(helper.getRequirement2Foo());
      helper.getFunctionRepository().addFunction(fnConv);
      final MockFunction fn2 = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertGraphContains(graph, fn1, fn2);
      builder.addTarget(helper.getRequirement2Bar());
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertGraphContains(graph, fn1, fn2, fnConv);
    } finally {
      TestLifecycle.end();
    }
  }

  public void functionWithDynamicConversionSingle() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
      final MockFunction fn2 = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
      final AtomicBoolean getResultsCalled = new AtomicBoolean();
      final MockFunction fnConv = new MockFunction(helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement2Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          s_logger.debug("fnConv late resolving with inputs {}");
          getResultsCalled.set(true);
          return super.getResults(context, target, inputs);
        }

      };
      fnConv.addRequirement(helper.getRequirement2Any());
      helper.getFunctionRepository().addFunction(fnConv);
      DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(CompiledFunctionDefinition function) {
          if (function == fnConv) {
            return -1;
          }
          return 0;
        }
      });
      builder.addTarget(helper.getRequirement2Bar());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      assertGraphContains(graph, fn1, fn2, fnConv);
      assertTrue(getResultsCalled.get());
    } finally {
      TestLifecycle.end();
    }
  }

  public void functionWithDynamicConversionTwoLevel() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
      // This converter will manipulate a value name but preserve a property; requiring late-stage property/constraint composition
      final MockFunction fnConv1 = new MockFunction("conv1", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement2Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          s_logger.debug("fnConv1 late resolving with inputs {}", inputs);
          assertEquals(1, inputs.size());
          final ValueSpecification input = inputs.keySet().iterator().next();
          return Collections.singleton(new ValueSpecification(helper.getRequirement2().getValueName(), target.toSpecification(), ValueProperties
              .with(ValuePropertyNames.FUNCTION, getUniqueId()).with("TEST", input.getProperties().getValues("TEST")).get()));
        }

      };
      fnConv1.addRequirement(helper.getRequirement1Any());
      helper.getFunctionRepository().addFunction(fnConv1);
      // This converter will preserve the value name but manipulate a property; and be selected if a converter is needed on top
      // of fnConv1 after late-stage composition
      final MockFunction fnConv2 = new MockFunction("conv2", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement2Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          s_logger.debug("fnConv2 late resolving with inputs {}", inputs);
          assertEquals(1, inputs.size());
          return super.getResults(context, target, inputs);
        }

      };
      fnConv2.addRequirement(helper.getRequirement2Any());
      helper.getFunctionRepository().addFunction(fnConv2);
      DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(CompiledFunctionDefinition function) {
          if (function == fnConv2) {
            return -1;
          }
          return 0;
        }
      });
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      Map<MockFunction, DependencyNode> nodes = assertGraphContains(graph, fn1, fnConv1);
      s_logger.debug("fnConv1 - inputs = {}", DependencyNodeImpl.getInputValues(nodes.get(fnConv1)));
      s_logger.debug("fnConv1 - outputs = {}", DependencyNodeImpl.getOutputValues(nodes.get(fnConv1)));
      assertTrue(nodes.get(fnConv1).getOutputValue(0).getProperties().getValues("TEST").contains("Foo"));
      builder.addTarget(helper.getRequirement2Bar());
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      nodes = assertGraphContains(graph, fn1, fnConv1, fnConv2);
      s_logger.debug("fnConv1 - inputs = {}", DependencyNodeImpl.getInputValues(nodes.get(fnConv1)));
      s_logger.debug("fnConv1 - outputs = {}", DependencyNodeImpl.getOutputValues(nodes.get(fnConv1)));
      assertTrue(nodes.get(fnConv1).getOutputValue(0).getProperties().getValues("TEST").contains("Foo"));
      s_logger.debug("fnConv2 - inputs = {}", DependencyNodeImpl.getInputValues(nodes.get(fnConv2)));
      s_logger.debug("fnConv2 - outputs = {}", DependencyNodeImpl.getOutputValues(nodes.get(fnConv2)));
      assertTrue(nodes.get(fnConv2).getOutputValue(0).getProperties().getValues("TEST").contains("Bar"));
    } finally {
      TestLifecycle.end();
    }
  }

  public void functionWithDynamicConversionDouble() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
      final AtomicInteger getResultsInvoked = new AtomicInteger();
      final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          final ValueRequirement req = helper.getRequirement2Any();
          return Collections.singleton(new ValueSpecification(req.getValueName(), target.toSpecification(), req.getConstraints().copy().with(ValuePropertyNames.FUNCTION, getUniqueId())
              .get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          s_logger.debug("fnConv late resolving with inputs {}", inputs);
          assertEquals(1, inputs.size());
          getResultsInvoked.incrementAndGet();
          return super.getResults(context, target, inputs);
        }

      };
      fnConv.addRequirement(helper.getRequirement1Any());
      helper.getFunctionRepository().addFunction(fnConv);
      DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement2Foo());
      builder.addTarget(helper.getRequirement2Bar());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      Map<MockFunction, DependencyNode> nodes = assertGraphContains(graph, fn1, fnConv, fnConv);
      s_logger.debug("fnConv - inputs = {}", DependencyNodeImpl.getInputValues(nodes.get(fnConv)));
      s_logger.debug("fnConv - outputs = {}", DependencyNodeImpl.getOutputValues(nodes.get(fnConv)));
      assertEquals(2, getResultsInvoked.get());
    } finally {
      TestLifecycle.end();
    }
  }

  public void twoLevelConversion() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final ComputationTarget target1 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Target", "1"));
      final ComputationTarget target2 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Target", "2"));
      final ComputationTarget target3 = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Target", "3"));
      final String property = "Constraint";
      MockFunction source = new MockFunction("source1", target1);
      source.addResult(new ComputedValue(new ValueSpecification("A", target1.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "1").with(property, "Foo").get()), 1.0));
      helper.getFunctionRepository().addFunction(source);
      source = new MockFunction("source2", target2);
      source.addResult(new ComputedValue(new ValueSpecification("A", target2.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "1").with(property, "Bar").get()), 2.0));
      helper.getFunctionRepository().addFunction(source);
      // Constraint preserving A->B
      helper.getFunctionRepository().addFunction(new TestFunction() {

        @Override
        public String getShortName() {
          return "AtoB";
        }

        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
          return Collections.singleton(new ValueRequirement("A", target.toSpecification(), ValueProperties.withAny(property).get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return Collections.singleton(new ValueSpecification("B", target.toSpecification(), createValueProperties().withAny(property).get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          return Collections.singleton(new ValueSpecification("B", target.toSpecification(), createValueProperties().with(property, inputs.keySet().iterator().next().getProperty(property))
              .get()));
        }

        @Override
        public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
          return null;
        }

      });
      // Constraint converting B->B
      helper.getFunctionRepository().addFunction(new TestFunction() {

        @Override
        public String getShortName() {
          return "BConv";
        }

        @Override
        public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
          return null;
        }

        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
          return Collections.singleton(new ValueRequirement("B", target.toSpecification(), ValueProperties.withAny(property).get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return Collections.singleton(new ValueSpecification("B", target.toSpecification(), ValueProperties.all()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          final Set<ValueSpecification> result = Sets.newHashSetWithExpectedSize(inputs.size());
          for (ValueSpecification input : inputs.keySet()) {
            result.add(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), input.getProperties().copy().withAny(property).get()));
          }
          return result;
        }

        @Override
        public int getPriority() {
          return -1;
        }

      });
      // Combining B->C; any constraint but must be the same
      helper.getFunctionRepository().addFunction(new TestFunction() {

        @Override
        public String getShortName() {
          return "BtoC";
        }

        @Override
        public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
          return null;
        }

        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
          final Set<ValueRequirement> req = new HashSet<ValueRequirement>();
          Set<String> props = desiredValue.getConstraints().getValues(property);
          if (props == null) {
            if (target.equals(target3)) {
              req.add(new ValueRequirement("B", target1.toSpecification(), ValueProperties.withAny(property).get()));
              req.add(new ValueRequirement("B", target2.toSpecification(), ValueProperties.withAny(property).get()));
            } else {
              req.add(new ValueRequirement("B", target.toSpecification(), ValueProperties.withAny(property).get()));
            }
          } else {
            if (target.equals(target3)) {
              req.add(new ValueRequirement("B", target1.toSpecification(), ValueProperties.with(property, props).get()));
              req.add(new ValueRequirement("B", target2.toSpecification(), ValueProperties.with(property, props).get()));
            } else {
              req.add(new ValueRequirement("B", target.toSpecification(), ValueProperties.with(property, props).get()));
            }
          }
          return req;
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return Collections.singleton(new ValueSpecification("C", target.toSpecification(), createValueProperties().withAny(property).get()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          String propValue = null;
          for (ValueSpecification input : inputs.keySet()) {
            if (propValue == null) {
              propValue = input.getProperty(property);
            } else {
              if (!propValue.equals(input.getProperty(property))) {
                throw new IllegalArgumentException("property mismatch - " + propValue + " vs " + input.getProperty(property));
              }
            }
          }
          return Collections.singleton(new ValueSpecification("C", target.toSpecification(), createValueProperties().with(property, propValue).get()));
        }

      });
      // Converting C->C; constraint omitted implies default
      helper.getFunctionRepository().addFunction(new TestFunction() {

        @Override
        public String getShortName() {
          return "CConv";
        }

        @Override
        public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
          return null;
        }

        @Override
        public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
          final Set<String> props = desiredValue.getConstraints().getValues(property);
          if (props == null) {
            return Collections.singleton(new ValueRequirement("C", target.toSpecification(), ValueProperties.with(property, "Default").get()));
          } else {
            return Collections.singleton(new ValueRequirement("C", target.toSpecification(), ValueProperties.withAny(property).get()));
          }
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
          return Collections.singleton(new ValueSpecification("C", target.toSpecification(), ValueProperties.all()));
        }

        @Override
        public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
          final Set<ValueSpecification> result = Sets.newHashSetWithExpectedSize(inputs.size());
          for (ValueSpecification input : inputs.keySet()) {
            result.add(new ValueSpecification(input.getValueName(), input.getTargetSpecification(), input.getProperties().copy().withAny(property).get()));
          }
          return result;
        }

        @Override
        public int getPriority() {
          return -1;
        }

      });
      final DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(CompiledFunctionDefinition function) {
          if (function instanceof TestFunction) {
            return ((TestFunction) function).getPriority();
          }
          return 0;
        }
      });
      ((MapComputationTargetResolver) builder.getCompilationContext().getRawComputationTargetResolver()).addTarget(target1);
      ((MapComputationTargetResolver) builder.getCompilationContext().getRawComputationTargetResolver()).addTarget(target2);
      ((MapComputationTargetResolver) builder.getCompilationContext().getRawComputationTargetResolver()).addTarget(target3);
      builder.addTarget(new ValueRequirement("C", target3.toSpecification()));
      builder.addTarget(new ValueRequirement("C", target2.toSpecification()));
      builder.addTarget(new ValueRequirement("C", target1.toSpecification()));
      builder.addTarget(new ValueRequirement("B", target1.toSpecification()));
      builder.addTarget(new ValueRequirement("B", target2.toSpecification()));
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      graph = DependencyGraphImpl.removeUnnecessaryValues(graph);
      s_logger.debug("After removeUnnecessaryValues");
      //graph.dumpStructureASCII(System.out);
    } finally {
      TestLifecycle.end();
    }
  }

}
