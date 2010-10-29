/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver.FunctionPriority;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class DependencyGraphBuilderTest {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilderTest.class);

  @Test
  public void singleOutputSingleFunctionNode() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction function = helper.addFunctionProducing1and2();

    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(Sets.newHashSet(helper.getRequirement1()));

    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));
    assertTrue(graph.getOutputValues().contains(helper.getSpec2()));

    Collection<DependencyNode> nodes = graph.getDependencyNodes();
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
    assertEquals(helper.getTarget(), node.getComputationTarget());

    graph.removeUnnecessaryValues();

    nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertFalse(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }

  /**
   * When you have multiple requirements eminating from the same function,
   * should only have a single node using that function. 
   */
  @Test
  public void multipleOutputsSingleFunctionNode() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction function = helper.addFunctionProducing1and2();

    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
    builder.addTarget(Sets.newHashSet(helper.getRequirement2()));

    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(1, nodes.size());
    DependencyNode node = nodes.iterator().next();
    assertEquals(function, node.getFunction().getFunction());
    assertEquals(function.getDefaultParameters(), node.getFunction().getParameters());
    assertTrue(node.getOutputValues().contains(helper.getSpec1()));
    assertTrue(node.getOutputValues().contains(helper.getSpec2()));
    assertTrue(node.getInputNodes().isEmpty());
  }

  @Test(expected = UnsatisfiableDependencyGraphException.class)
  public void unsatisfiableDependency() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing1and2();
    ValueRequirement anotherReq = new ValueRequirement("Req-3", helper.getTarget());

    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(Sets.newHashSet(helper.getRequirement1()));
    builder.addTarget(Collections.singleton(anotherReq));
  }

  @Test
  public void doubleLevelNoLiveData() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    MockFunction fn2 = helper.addFunctionProducing2();

    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement1());

    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);

    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));
    assertTrue(graph.getOutputValues().contains(helper.getSpec2()));

    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if (ObjectUtils.equals(node.getFunction().getFunction(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertFalse(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputValues().contains(helper.getSpec2()));
        assertEquals(1, node.getInputNodes().size());
        assertEquals(helper.getTarget(), node.getComputationTarget());
      } else if (ObjectUtils.equals(node.getFunction().getFunction(), fn2)) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertTrue(node.getOutputValues().contains(helper.getSpec2()));
        assertTrue(node.getInputValues().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }

  @Test
  public void doubleLevelLiveData() {
    DepGraphTestHelper helper = new DepGraphTestHelper();
    MockFunction fn1 = helper.addFunctionRequiring2Producing1();
    helper.make2AvailableFromLiveData();

    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement1());

    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);

    graph.removeUnnecessaryValues();

    assertTrue(graph.getOutputValues().contains(helper.getSpec1()));

    Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    assertNotNull(nodes);
    assertEquals(2, nodes.size());
    for (DependencyNode node : nodes) {
      if (ObjectUtils.equals(node.getFunction().getFunction(), fn1)) {
        assertTrue(node.getOutputValues().contains(helper.getSpec1()));
        assertEquals(1, node.getInputNodes().size());
        ValueSpecification inputSpec = node.getInputValues().iterator().next();
        assertEquals(helper.getSpec2().getValueName(), inputSpec.getValueName());
        assertEquals(helper.getSpec2().getTargetSpecification(), inputSpec.getTargetSpecification());
        assertEquals(helper.getTarget(), node.getComputationTarget());
      } else if (node.getFunction().getFunction() instanceof LiveDataSourcingFunction) {
        assertFalse(node.getOutputValues().contains(helper.getSpec1()));
        assertEquals(1, node.getOutputValues().size());
        ValueSpecification outputSpec = node.getOutputValues().iterator().next();
        assertEquals(helper.getSpec2().getValueName(), outputSpec.getValueName());
        assertEquals(helper.getSpec2().getTargetSpecification(), outputSpec.getTargetSpecification());
        assertTrue(node.getInputValues().isEmpty());
        assertTrue(node.getInputNodes().isEmpty());
      } else {
        fail("Unexpected function definition");
      }
    }
  }

  private Map<MockFunction, DependencyNode> assertGraphContains(final DependencyGraph graph, final MockFunction... functions) {
    final Collection<DependencyNode> nodes = graph.getDependencyNodes(ComputationTargetType.PRIMITIVE);
    final List<MockFunction> functionList = new LinkedList<MockFunction>(Arrays.asList(functions));
    final Map<MockFunction, DependencyNode> result = new HashMap<MockFunction, DependencyNode>();
    for (DependencyNode node : nodes) {
      if (!functionList.remove(node.getFunction().getFunction())) {
        fail(node.toString() + " not in expected functions");
      }
      result.put((MockFunction) node.getFunction().getFunction(), node);
    }
    if (!functionList.isEmpty()) {
      fail(functionList.toString());
    }
    return result;
  }

  @Test
  public void testFunctionByName1() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn = helper.addFunctionProducing2();
    final MockFunction fnBeta = helper.addFunctionProducing2Beta();
    final DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
      @Override
      public int getPriority(CompiledFunctionDefinition function) {
        if (function.getFunctionDefinition().getUniqueIdentifier().equals(fnBeta.getUniqueIdentifier())) {
          return -1;
        }
        return 0;
      }
    });
    builder.addTarget(helper.getRequirement2());
    builder.addTarget(helper.getRequirement2Beta());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn, fnBeta);
  }

  @Test
  public void testFunctionByName2() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing2();
    final MockFunction fnBeta = helper.addFunctionProducing2Beta();
    final DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
      @Override
      public int getPriority(CompiledFunctionDefinition function) {
        if (function.getFunctionDefinition().getUniqueIdentifier().equals(fnBeta.getUniqueIdentifier())) {
          return 1;
        }
        return 0;
      }
    });
    builder.addTarget(helper.getRequirement2Beta());
    builder.addTarget(helper.getRequirement2Beta());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fnBeta);
  }

  @Test(expected = UnsatisfiableDependencyGraphException.class)
  public void testFunctionByNameMissing() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing2();
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2());
    builder.addTarget(helper.getRequirement2Beta());
  }

  @Test
  public void testFunctionWithProperty() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    helper.addFunctionRequiringProducing(helper.getRequirement1Bar(), helper.getValue2Bar());
    final MockFunction fn2b = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2Foo());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn1, fn2b);
  }

  @Test(expected = UnsatisfiableDependencyGraphException.class)
  public void testFunctionWithPropertyMissing() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing(helper.getValue1Foo());
    helper.addFunctionRequiringProducing(helper.getRequirement1Bar(), helper.getValue2Bar());
    helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2Bar());
  }

  @Test
  public void testFunctionWithStaticConversion() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    final MockFunction fnConv = new MockFunction(helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement2Bar(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv late resolving with inputs {}", inputs);
        fail("getResults shouldn't be called on function without wildcard inputs");
        return null;
      }

    };
    fnConv.addRequirement(helper.getRequirement2Foo());
    helper.getFunctionRepository().addFunction(fnConv);
    final MockFunction fn2 = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2Foo());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn1, fn2);
    builder.addTarget(helper.getRequirement2Bar());
    graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn1, fn2, fnConv);
  }

  @Test
  public void testFunctionWithDynamicConversionSingle() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    final MockFunction fn2 = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    final AtomicBoolean getResultsCalled = new AtomicBoolean();
    final MockFunction fnConv = new MockFunction(helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement2Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv late resolving with inputs {}");
        getResultsCalled.set(true);
        return super.getResults(context, target, inputs);
      }

    };
    fnConv.addRequirement(helper.getRequirement2Any());
    helper.getFunctionRepository().addFunction(fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
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
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn1, fn2, fnConv);
    assertTrue(getResultsCalled.get());
  }

  @Test
  public void testFunctionWithDynamicConversionTwoLevel() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    // This converter will manipulate a value name but preserve a property; requiring late-stage property/constraint composition
    final MockFunction fnConv1 = new MockFunction("conv1", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement2Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv1 late resolving with inputs {}", inputs);
        assertEquals(1, inputs.size());
        final ValueSpecification input = inputs.iterator().next();
        return Collections.singleton(new ValueSpecification(helper.getRequirement2().getValueName(), helper.getRequirement2().getTargetSpecification(), ValueProperties.with(
            ValuePropertyNames.FUNCTION, getUniqueIdentifier()).with("TEST", input.getProperties().getValues("TEST")).get()));
      }

    };
    fnConv1.addRequirement(helper.getRequirement1Any());
    helper.getFunctionRepository().addFunction(fnConv1);
    // This converter will preserve the value name but manipulate a property; and be selected if a converter is needed on top
    // of fnConv1 after late-stage composition
    final MockFunction fnConv2 = new MockFunction("conv2", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement2Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv2 late resolving with inputs {}", inputs);
        assertEquals(1, inputs.size());
        return super.getResults(context, target, inputs);
      }

    };
    fnConv2.addRequirement(helper.getRequirement2Any());
    helper.getFunctionRepository().addFunction(fnConv2);
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
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
    graph.removeUnnecessaryValues();
    Map<MockFunction, DependencyNode> nodes = assertGraphContains(graph, fn1, fnConv1);
    s_logger.debug("fnConv1 - inputs = {}", nodes.get(fnConv1).getInputValues());
    s_logger.debug("fnConv1 - outputs = {}", nodes.get(fnConv1).getOutputRequirements());
    assertTrue(nodes.get(fnConv1).getOutputRequirements().iterator().next().getConstraints().getValues("TEST").contains("Foo"));
    builder.addTarget(helper.getRequirement2Bar());
    graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    nodes = assertGraphContains(graph, fn1, fnConv1, fnConv2);
    s_logger.debug("fnConv1 - inputs = {}", nodes.get(fnConv1).getInputValues());
    s_logger.debug("fnConv1 - outputs = {}", nodes.get(fnConv1).getOutputRequirements());
    assertTrue(nodes.get(fnConv1).getOutputRequirements().iterator().next().getConstraints().getValues("TEST").contains("Foo"));
    s_logger.debug("fnConv2 - inputs = {}", nodes.get(fnConv2).getInputValues());
    s_logger.debug("fnConv2 - outputs = {}", nodes.get(fnConv2).getOutputRequirements());
    assertTrue(nodes.get(fnConv2).getOutputRequirements().iterator().next().getConstraints().getValues("TEST").contains("Bar"));
  }

  @Test
  public void testFunctionWithDynamicConversionDouble() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    final AtomicInteger getResultsInvoked = new AtomicInteger();
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement2Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv late resolving with inputs {}", inputs);
        assertEquals(1, inputs.size());
        getResultsInvoked.incrementAndGet();
        return super.getResults(context, target, inputs);
      }

    };
    fnConv.addRequirement(helper.getRequirement1Any());
    helper.getFunctionRepository().addFunction(fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2Foo());
    builder.addTarget(helper.getRequirement2Bar());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    Map<MockFunction, DependencyNode> nodes = assertGraphContains(graph, fn1, fnConv, fnConv);
    s_logger.debug("fnConv - inputs = {}", nodes.get(fnConv).getInputValues());
    s_logger.debug("fnConv - outputs = {}", nodes.get(fnConv).getOutputRequirements());
    assertEquals(2, getResultsInvoked.get());
  }

  @Test
  public void testBacktrackCleanup() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    final MockFunction fn2Foo = helper.addFunctionProducing(helper.getValue2Foo());
    final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement1Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs) {
        s_logger.debug("fnConv late resolving with inputs {}", inputs);
        assertEquals(1, inputs.size());
        final ValueSpecification input = inputs.iterator().next();
        if (!input.getProperties().getValues("TEST").contains("Bar")) {
          return Collections.emptySet();
        }
        return super.getResults(context, target, inputs);
      }

    };
    fnConv.addRequirement(helper.getRequirement2Any());
    helper.getFunctionRepository().addFunction(fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
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
    assertGraphContains(graph, fn2Foo, fn2Bar, fnConv);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn2Bar, fnConv);
  }

  @Test
  public void testOutputBasedRequirements() {
    final DepGraphTestHelper helper = new DepGraphTestHelper();
    helper.addFunctionProducing(helper.getValue2Foo());
    final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement1Any(), getUniqueIdentifier()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
        return Collections.singleton(new ValueRequirement(helper.getRequirement2Any().getValueName(), desiredValue.getTargetSpecification(), ValueProperties.with("TEST",
            desiredValue.getConstraints().getValues("TEST")).get()));
      }

    };
    helper.getFunctionRepository().addFunction(fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement1Bar());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    assertGraphContains(graph, fn2Bar, fnConv);
  }
  
  @Test
  public void testAdditionalRequirements () {
    final DepGraphTestHelper helper = new DepGraphTestHelper ();
    final MockFunction fn1Foo = helper.addFunctionProducing(helper.getValue1Foo());
    final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget ()) {
      
      private final ValueSpecification _result = new ValueSpecification(helper.getRequirement1Any(), getUniqueIdentifier());
      
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(_result);
      }
      
      @Override
      public Set<ValueRequirement> getAdditionalRequirements (FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
        assertEquals (1, inputs.size ());
        assertTrue (inputs.contains (helper.getSpec2Bar ()));
        assertEquals (1, outputs.size ());
        assertTrue (outputs.contains (_result.compose(helper.getRequirement1Bar ())));
        return Collections.singleton (helper.getRequirement1Foo ());
      }
      
    };
    fnConv.addRequirement(helper.getRequirement2Any ());
    helper.getFunctionRepository().addFunction (fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement1Bar());
    DependencyGraph graph = builder.getDependencyGraph ();
    assertNotNull (graph);
    assertGraphContains (graph, fn2Bar, fnConv, fn1Foo);
  }
  
  @Test
  public void testAdditionalRequirementBacktracking () {
    final DepGraphTestHelper helper = new DepGraphTestHelper ();
    final MockFunction fn1Foo = helper.addFunctionProducing(helper.getValue1Foo());
    final MockFunction fn2Foo = helper.addFunctionProducing(helper.getValue2Foo());
    final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget ()) {
      
      private final ValueSpecification _result = new ValueSpecification(helper.getRequirement1Any(), getUniqueIdentifier());
      
      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(_result);
      }
      
      @Override
      public Set<ValueRequirement> getAdditionalRequirements (FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
        if (inputs.contains (helper.getSpec2Foo ())) {
          return Collections.singleton (helper.getRequirement2Beta());
        } else {
          return Collections.singleton (helper.getRequirement1Foo ());
        }
      }
      
    };
    fnConv.addRequirement(helper.getRequirement2Any ());
    helper.getFunctionRepository().addFunction (fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
      @Override
      public int getPriority(CompiledFunctionDefinition function) {
        if (function == fn2Foo) {
          return 1;
        }
        return 0;
      }
    });
    builder.addTarget(helper.getRequirement1Bar());
    DependencyGraph graph = builder.getDependencyGraph ();
    assertNotNull (graph);
    assertGraphContains (graph, fn2Foo, fn2Bar, fnConv, fn1Foo);
    graph.removeUnnecessaryValues();
    assertGraphContains (graph, fn2Bar, fnConv, fn1Foo);
  }

}
