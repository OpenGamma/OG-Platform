/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link CompiledViewDefinitionWithGraphsImpl}.
 */
@Test(groups = TestGroup.UNIT)
public class CompiledViewDefinitionTest {

  private final Instant _time0 = Instant.now();
  private final Instant _time1 = _time0.plusMillis(1);
  private final Instant _time2 = _time0.plusMillis(2);
  private final Instant _time3 = _time0.plusMillis(3);
  private final Instant _time4 = _time0.plusMillis(4);
  private final Instant _time5 = _time0.plusMillis(5);

  private DependencyNode createDependencyNode(final Instant functionStart, final Instant functionEnd) {
    final DependencyNode node = new DependencyNode(new ComputationTarget(ComputationTargetType.NULL, null));
    node.setFunction(new CompiledFunctionDefinition() {

      @Override
      public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
        return false;
      }

      @Override
      public Instant getEarliestInvocationTime() {
        return functionStart;
      }

      @Override
      public FunctionDefinition getFunctionDefinition() {
        return new FunctionDefinition() {

          @Override
          public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
            return null;
          }

          @Override
          public FunctionParameters getDefaultParameters() {
            return new EmptyFunctionParameters();
          }

          @Override
          public String getShortName() {
            return null;
          }

          @Override
          public String getUniqueId() {
            return null;
          }

          @Override
          public void init(FunctionCompilationContext context) {
          }

        };
      }

      @Override
      public FunctionInvoker getFunctionInvoker() {
        return null;
      }

      @Override
      public Instant getLatestInvocationTime() {
        return functionEnd;
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
        return null;
      }

      @Override
      public Set<ValueRequirement> getAdditionalRequirements(FunctionCompilationContext context, ComputationTarget target, Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
        return null;
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return null;
      }

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
        return null;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.NULL;
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return false;
      }

    });
    return node;
  }

  private DependencyGraph graphNoStartEndTimes() {
    final DependencyGraph graph = new DependencyGraph("no start/end");
    graph.addDependencyNode(createDependencyNode(null, null));
    graph.addDependencyNode(createDependencyNode(null, null));
    return graph;
  }

  private DependencyGraph graphOneEndTime(final Instant end) {
    final DependencyGraph graph = new DependencyGraph("one end");
    graph.addDependencyNode(createDependencyNode(null, null));
    graph.addDependencyNode(createDependencyNode(null, end));
    return graph;
  }

  private DependencyGraph graphTwoEndTimes(final Instant end1, final Instant end2) {
    final DependencyGraph graph = new DependencyGraph("two ends");
    graph.addDependencyNode(createDependencyNode(null, null));
    graph.addDependencyNode(createDependencyNode(null, end1));
    graph.addDependencyNode(createDependencyNode(null, end2));
    return graph;
  }

  private DependencyGraph graphOneStartTime(final Instant start) {
    final DependencyGraph graph = new DependencyGraph("one start");
    graph.addDependencyNode(createDependencyNode(null, null));
    graph.addDependencyNode(createDependencyNode(start, null));
    return graph;
  }

  private DependencyGraph graphTwoStartTimes(final Instant start1, final Instant start2) {
    final DependencyGraph graph = new DependencyGraph("two starts");
    graph.addDependencyNode(createDependencyNode(null, null));
    graph.addDependencyNode(createDependencyNode(start1, null));
    graph.addDependencyNode(createDependencyNode(start2, null));
    return graph;
  }

  private CompiledViewDefinitionWithGraphsImpl buildCompiledViewDefinition(final DependencyGraph... graphs) {
    return new CompiledViewDefinitionWithGraphsImpl(VersionCorrection.LATEST, "", mock(ViewDefinition.class), Arrays.asList(graphs), Collections.<ComputationTargetReference, UniqueId>emptyMap(),
        null, 0);
  }

  @Test
  public void testNoValidityTimes() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes());
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MIN_VALUE)));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MAX_VALUE)));
  }

  @Test
  public void testNoStartTime1() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneEndTime(_time0), graphTwoEndTimes(_time1, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MIN_VALUE)));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time0));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
  }

  @Test
  public void testNoStartTime2() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneEndTime(_time1), graphTwoEndTimes(_time0, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MIN_VALUE)));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time0));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
  }

  @Test
  public void testNoEndTime1() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneStartTime(_time2), graphTwoStartTimes(_time0, _time1));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MAX_VALUE)));
  }

  @Test
  public void testNoEndTime2() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneStartTime(_time1), graphTwoStartTimes(_time0, _time2));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, Instant.ofEpochMilli(Long.MAX_VALUE)));
  }

  @Test
  public void testStartEndTime() {
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneStartTime(_time0), graphTwoStartTimes(_time1, _time2), graphOneEndTime(_time3),
        graphTwoEndTimes(
            _time4, _time5));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time3));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time4));
  }

}
