/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.exec.plan.CachingExecutionPlanner.CacheKey;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class CachingExecutionPlannerTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  private TestDependencyGraphBuilder testGraphBuilder(final String config) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder(config);
    for (int i = 0; i < 10; i++) {
      final NodeBuilder nb = gb.addNode(new MockFunction("Foo" + i, new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "X"))));
      if (i > 0) {
        nb.addOutput(Integer.toString(i));
        nb.addInput(new ValueSpecification(Integer.toString(i - 1), ComputationTargetSpecification.of(UniqueId.of("Test", "X")), ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo" + (i - 1))
            .get()));
      } else {
        nb.addTerminalOutput("0");
      }
    }
    return gb;
  }

  public void testCacheKey_same() {
    final CacheKey ak = new CacheKey(testGraphBuilder("Default").buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(testGraphBuilder("Default").buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    assertTrue(ak.equals(bk));
    assertTrue(bk.equals(ak));
    assertEquals(ak.hashCode(), bk.hashCode());
  }

  public void testCacheKey_graph1() {
    final DependencyGraph a = testGraphBuilder("A").buildGraph();
    final DependencyGraph b = testGraphBuilder("B").buildGraph();
    assertFalse(a.equals(b));
    assertFalse(b.equals(a));
    final CacheKey ak = new CacheKey(a, 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(b, 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    assertFalse(ak.equals(bk));
    assertFalse(bk.equals(ak));
  }

  public void testCacheKey_initId() {
    final DependencyGraph graph = testGraphBuilder("Default").buildGraph();
    final CacheKey ak = new CacheKey(graph, 1, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(graph, 2, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    assertFalse(ak.equals(bk));
    assertFalse(bk.equals(ak));
  }

  public void testCacheKey_graph2() {
    final TestDependencyGraphBuilder a = testGraphBuilder("Default");
    a.addTerminalOutput(new ValueSpecification("1", ComputationTargetSpecification.of(UniqueId.of("Test", "X")), ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo1").get()),
        Collections.singleton(new ValueRequirement("1", ComputationTargetSpecification.of(UniqueId.of("Test", "X")))));
    final TestDependencyGraphBuilder b = testGraphBuilder("Default");
    b.addTerminalOutput(new ValueSpecification("2", ComputationTargetSpecification.of(UniqueId.of("Test", "X")), ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo2").get()),
        Collections.singleton(new ValueRequirement("2", ComputationTargetSpecification.of(UniqueId.of("Test", "X")))));
    final CacheKey ak = new CacheKey(a.buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(b.buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    assertFalse(ak.equals(bk));
    assertFalse(bk.equals(ak));
  }

  public void testCacheKey_shared() {
    final TestDependencyGraphBuilder graph = testGraphBuilder("Default");
    final ValueSpecification value = graph.addNode("Foo", ComputationTargetSpecification.NULL).addTerminalOutput("Bar");
    final CacheKey ak = new CacheKey(graph.buildGraph(), 0, Collections.singleton(value), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(graph.buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    assertFalse(ak.equals(bk));
    assertFalse(bk.equals(ak));
  }

  public void testCacheKey_parameters() {
    final TestDependencyGraphBuilder graph = testGraphBuilder("Default");
    final ValueSpecification value = graph.addNode("Foo", ComputationTargetSpecification.NULL).addTerminalOutput("Bar");
    final CacheKey ak = new CacheKey(graph.buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final CacheKey bk = new CacheKey(graph.buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>singletonMap(value,
        EmptyFunctionParameters.INSTANCE));
    assertFalse(ak.equals(bk));
    assertFalse(bk.equals(ak));
  }

  public void testCacheKey_serialization() throws Exception {
    final CacheKey a = new CacheKey(testGraphBuilder("Default").buildGraph(), 0, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(a);
    final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    final CacheKey b = (CacheKey) ois.readObject();
    assertEquals(b, a);
  }

  private GraphExecutionPlanner createExecutionPlanner() {
    return new GraphExecutionPlanner() {
      @Override
      public GraphExecutionPlan createPlan(DependencyGraph graph, ExecutionLogModeSource logModeSource, long functionInitialisationId, Set<ValueSpecification> sharedValues,
          Map<ValueSpecification, FunctionParameters> parameters) {
        return new GraphExecutionPlan(graph.getCalculationConfigurationName(), 0L, Collections.<PlannedJob>emptySet(), 0, 0d, 0d, 0d);
      }
    };
  }

  public void testCache_match() {
    final CachingExecutionPlanner cache = new CachingExecutionPlanner(createExecutionPlanner(), _cacheManager);
    try {
      final GraphExecutionPlan plan1 = cache.createPlan(testGraphBuilder("Default").buildGraph(), Mockito.mock(ExecutionLogModeSource.class), 0, Collections.<ValueSpecification>emptySet(),
          Collections.<ValueSpecification, FunctionParameters>emptyMap());
      final GraphExecutionPlan plan2 = cache.createPlan(testGraphBuilder("Default").buildGraph(), Mockito.mock(ExecutionLogModeSource.class), 0, Collections.<ValueSpecification>emptySet(),
          Collections.<ValueSpecification, FunctionParameters>emptyMap());
      assertSame(plan2, plan1);
      assertNotNull(plan1);
      assertNotNull(plan2);
    } finally {
      cache.shutdown();
    }
  }

  public void testCache_mismatch() {
    final CachingExecutionPlanner cache = new CachingExecutionPlanner(createExecutionPlanner(), _cacheManager);
    try {
      final GraphExecutionPlan plan1 = cache.createPlan(testGraphBuilder("Default").buildGraph(), Mockito.mock(ExecutionLogModeSource.class), 0, Collections.<ValueSpecification>emptySet(),
          Collections.<ValueSpecification, FunctionParameters>emptyMap());
      final TestDependencyGraphBuilder gb = testGraphBuilder("Default");
      gb.addTerminalOutput(new ValueSpecification("1", ComputationTargetSpecification.of(UniqueId.of("Test", "X")), ValueProperties.with(ValuePropertyNames.FUNCTION, "Foo1").get()),
          Collections.singleton(new ValueRequirement("1", ComputationTargetSpecification.of(UniqueId.of("Test", "X")))));
      final GraphExecutionPlan plan2 = cache.createPlan(gb.buildGraph(), Mockito.mock(ExecutionLogModeSource.class), 0, Collections.<ValueSpecification>emptySet(),
          Collections.<ValueSpecification, FunctionParameters>emptyMap());
      assertNotSame(plan2, plan1);
      assertNotNull(plan1);
      assertNotNull(plan2);
    } finally {
      cache.shutdown();
    }
  }

}
