/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.rest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureObjectFactory;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTraceFudgeBuilder.ThrowableWithClass;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests fudge builder for DependencyGraphBuildTrace, serialize and deserialize behaviour.
 */
@Test(groups = TestGroup.UNIT)
public class DependencyGraphBuildTraceFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void cycleObjectTest() {
    DependencyGraphBuildTrace object = createDependencyGraphBuildTrace();
    DependencyGraphBuildTrace cycleObject = cycleObject(DependencyGraphBuildTrace.class, object);
    System.out.println(object.getDependencyGraph());
    System.out.println();
    System.out.println(cycleObject.getDependencyGraph());
    DependencyGraph objectDepGraph = object.getDependencyGraph();
    DependencyGraph cycleObjectDepGraph = cycleObject.getDependencyGraph();
    assertEquals(objectDepGraph, cycleObjectDepGraph);
    assertEquals(object.getExceptionsWithCounts(), cycleObject.getExceptionsWithCounts());
    assertEquals(object.getFailures(), cycleObject.getFailures());
    assertEquals(object.getMappings(), cycleObject.getMappings());
  }

  /**
   * @return a basic dep graph build trace object
   */
  private DependencyGraphBuildTrace createDependencyGraphBuildTrace() {
    DependencyGraph dependencyGraph = createGraph();
    Map<Throwable, Integer> exceptions = new HashMap<>();
    //somewhat contrived...
    exceptions.put(new ThrowableWithClass("a null pointer", ThrowableWithClass.class), 1);
    exceptions.put(new ThrowableWithClass("out of memory error", ThrowableWithClass.class), 4);
    ValueRequirement valueRequirement = new ValueRequirement(ValueRequirementNames.FAIR_VALUE, ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Fair Value Scheme", "Fair Value Id"));
    List<ResolutionFailure> failures = new ArrayList<>();
    failures.add(ResolutionFailureObjectFactory.unsatisfiedResolutionFailure(valueRequirement));
    Map<ValueRequirement, ValueSpecification> mappings = new HashMap<>();
    ValueSpecification valueSpecification = ValueSpecification.of("Foo", ComputationTargetType.PRIMITIVE, UniqueId.of("Scheme", "Value2"),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    mappings.put(valueRequirement, valueSpecification);
    return DependencyGraphBuildTrace.of(dependencyGraph, exceptions, failures, mappings);
  }

  /**
   * A very simple graph. Testing of (de)serializing more complicated graphs done in {@link DependencyGraphTraceBuilderTest}.
   * 
   * @return
   */
  private DependencyGraph createGraph() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("testGraph");
    final ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(ComputationTargetType.CURRENCY, Currency.GBP.getUniqueId());
    final NodeBuilder yieldCurveNode = gb.addNode("MockYieldCurve", targetSpecification);
    yieldCurveNode.addOutput(new ValueSpecification("YieldCurveMarketData", targetSpecification, ValueProperties.builder().with("Curve", "Forward3M").with("Function", "someFunction").get()));
    return gb.buildGraph();
  }

}
