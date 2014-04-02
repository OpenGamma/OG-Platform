/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
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
 * Tests the dependency graph building with a multiple output function whose inputs vary with its output set
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphInputMergingTest extends AbstractDependencyGraphBuilderTest {

  private static class VariantInputFunction extends TestFunction {

    private final ValueSpecification _spec1;
    private final ValueSpecification _spec2;
    private final ValueRequirement _req1;
    private final ValueRequirement _req2;

    public VariantInputFunction(final DepGraphTestHelper helper, final String in, final String out) {
      _spec1 = new ValueSpecification(helper.getSpec1().getValueName(), helper.getSpec1().getTargetSpecification(), helper.getSpec1().getProperties().copy().with("AUX", "X", "Y")
          .with("TEST", out).get());
      _req1 = new ValueRequirement(helper.getRequirement1().getValueName(), helper.getRequirement1().getTargetReference(), ValueProperties.with("TEST", in).get());
      _spec2 = new ValueSpecification(helper.getSpec2().getValueName(), helper.getSpec2().getTargetSpecification(), helper.getSpec2().getProperties().copy().with("AUX", "X", "Y")
          .with("TEST", out).get());
      _req2 = new ValueRequirement(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetReference(), ValueProperties.with("TEST", in).get());
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return null;
    }

    @Override
    public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
      if (desiredValue.getValueName() == _spec1.getValueName()) {
        return Collections.singleton(_req1);
      } else {
        return Collections.singleton(_req2);
      }
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
      for (ValueRequirement input : inputs.values()) {
        if (input.getValueName() == _req1.getValueName()) {
          result.add(_spec1);
        } else {
          result.add(_spec2);
        }
      }
      return result;
    }

    @Override
    public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
      return ImmutableSet.of(_spec1, _spec2);
    }

  }

  public void req1() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v = new VariantInputFunction(helper, "Bar", "Foo");
      helper.getFunctionRepository().addFunction(v);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 2); // VariantFunction & NodeProducing1
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec1, helper.getSpecification1Bar()));
      final ValueRequirement req = new ValueRequirement(helper.getRequirement1().getValueName(), helper.getRequirement1().getTargetReference(), ValueProperties.with("TEST", "Foo")
          .with("AUX", "X").get());
      builder.addTarget(req);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 2); // VariantFunction & NodeProducing1
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec1.compose(req), helper.getSpecification1Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void req1_twoLevel() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v1 = new VariantInputFunction(helper, "Bar", "Cow");
      final VariantInputFunction v2 = new VariantInputFunction(helper, "Cow", "Foo");
      helper.getFunctionRepository().addFunction(v1);
      helper.getFunctionRepository().addFunction(v2);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction1, VariantFunction2 & NodeProducing1
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v1._spec1, v2._spec1, helper.getSpecification1Bar()));
      final ValueRequirement req = new ValueRequirement(helper.getRequirement1().getValueName(), helper.getRequirement1().getTargetReference(), ValueProperties.with("TEST", "Cow")
          .with("AUX", "X").get());
      builder.addTarget(req);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction1, VariantFunction2 & NodeProducing1
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v1._spec1.compose(req), v2._spec1, helper.getSpecification1Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void req2() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v = new VariantInputFunction(helper, "Bar", "Foo");
      helper.getFunctionRepository().addFunction(v);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 2); // VariantFunction & NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec2, helper.getSpec2Bar()));
      final ValueRequirement req = new ValueRequirement(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetReference(), ValueProperties.with("TEST", "Foo")
          .with("AUX", "X").get());
      builder.addTarget(req);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 2); // VariantFunction & NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec2.compose(req), helper.getSpec2Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void req2_twoLevel() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v1 = new VariantInputFunction(helper, "Bar", "Cow");
      final VariantInputFunction v2 = new VariantInputFunction(helper, "Cow", "Foo");
      helper.getFunctionRepository().addFunction(v1);
      helper.getFunctionRepository().addFunction(v2);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction1, VariantFunction2 & NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v1._spec2, v2._spec2, helper.getSpec2Bar()));
      final ValueRequirement req = new ValueRequirement(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetReference(), ValueProperties.with("TEST", "Cow")
          .with("AUX", "X").get());
      builder.addTarget(req);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction1, VariantFunction2 & NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v1._spec2.compose(req), v2._spec2, helper.getSpec2Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void req1And2() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v = new VariantInputFunction(helper, "Bar", "Foo");
      helper.getFunctionRepository().addFunction(v);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1Foo());
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction (with two outputs), NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec1, v._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
      final ValueRequirement req = new ValueRequirement(helper.getRequirement1().getValueName(), helper.getRequirement1().getTargetReference(), ValueProperties.with("TEST", "Foo")
          .with("AUX", "X").get());
      builder.addTarget(req);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 3); // VariantFunction (with two outputs), NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph), ImmutableSet.of(v._spec1.compose(req), v._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

  public void req1And2_threeLevel() {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = new DepGraphTestHelper();
      helper.addFunctionProducing(new ComputedValue(helper.getSpecification1Bar(), null));
      helper.addFunctionProducing(new ComputedValue(helper.getSpec2Bar(), null));
      final VariantInputFunction v1 = new VariantInputFunction(helper, "Bar", "Cow");
      final VariantInputFunction v2 = new VariantInputFunction(helper, "Cow", "Dog");
      final VariantInputFunction v3 = new VariantInputFunction(helper, "Dog", "Foo");
      helper.getFunctionRepository().addFunction(v1);
      helper.getFunctionRepository().addFunction(v2);
      helper.getFunctionRepository().addFunction(v3);
      final DependencyGraphBuilder builder = helper.createBuilder(null);
      builder.addTarget(helper.getRequirement1Foo());
      builder.addTarget(helper.getRequirement2Foo());
      DependencyGraph graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 5); // v1, v2, v3, NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph),
          ImmutableSet.of(v1._spec1, v1._spec2, v2._spec1, v2._spec2, v3._spec1, v3._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
      final ValueRequirement req1 = new ValueRequirement(helper.getRequirement1().getValueName(), helper.getRequirement1().getTargetReference(), ValueProperties.with("TEST", "Cow")
          .with("AUX", "X").get());
      builder.addTarget(req1);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 5); // v1, v2, v3, NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph),
          ImmutableSet.of(v1._spec1.compose(req1), v1._spec2, v2._spec1, v2._spec2, v3._spec1, v3._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
      final ValueRequirement req2 = new ValueRequirement(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetReference(), ValueProperties.with("TEST", "Cow")
          .with("AUX", "X").get());
      builder.addTarget(req2);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 5); // v1, v2, v3, NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph),
          ImmutableSet.of(v1._spec1.compose(req1), v1._spec2.compose(req2), v2._spec1, v2._spec2, v3._spec1, v3._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
      final ValueRequirement req3 = new ValueRequirement(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetReference(), ValueProperties.with("TEST", "Dog")
          .with("AUX", "Y").get());
      builder.addTarget(req3);
      graph = builder.getDependencyGraph();
      assertNotNull(graph);
      assertEquals(graph.getSize(), 5); // v1, v2, v3, NodeProducing1 and NodeProducing2
      assertEquals(DependencyGraphImpl.getAllOutputSpecifications(graph),
          ImmutableSet.of(v1._spec1.compose(req1), v1._spec2.compose(req2), v2._spec1, v2._spec2.compose(req3), v3._spec1, v3._spec2, helper.getSpecification1Bar(), helper.getSpec2Bar()));
    } finally {
      TestLifecycle.end();
    }
  }

}
