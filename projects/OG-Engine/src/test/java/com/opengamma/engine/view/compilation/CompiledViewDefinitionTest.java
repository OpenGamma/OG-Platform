/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
import com.opengamma.engine.target.ComputationTargetReference;
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

  private void createDependencyNode(final TestDependencyGraphBuilder builder, final Instant functionStart, final Instant functionEnd) {
    final String function;
    if (functionStart != null) {
      assert functionEnd == null;
      function = "start" + functionStart.toString();
    } else if (functionEnd != null) {
      function = "end" + functionEnd.toString();
    } else {
      function = "foo";
    }
    builder.addNode(function, ComputationTargetSpecification.NULL);
  }

  private DependencyGraph graphNoStartEndTimes() {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("no start/end");
    createDependencyNode(gb, null, null);
    createDependencyNode(gb, null, null);
    return gb.buildGraph();
  }

  private DependencyGraph graphOneEndTime(final Instant end) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("one end");
    createDependencyNode(gb, null, null);
    createDependencyNode(gb, null, end);
    return gb.buildGraph();
  }

  private DependencyGraph graphTwoEndTimes(final Instant end1, final Instant end2) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("two ends");
    createDependencyNode(gb, null, null);
    createDependencyNode(gb, null, end1);
    createDependencyNode(gb, null, end2);
    return gb.buildGraph();
  }

  private DependencyGraph graphOneStartTime(final Instant start) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("one start");
    createDependencyNode(gb, null, null);
    createDependencyNode(gb, start, null);
    return gb.buildGraph();
  }

  private DependencyGraph graphTwoStartTimes(final Instant start1, final Instant start2) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("two starts");
    createDependencyNode(gb, null, null);
    createDependencyNode(gb, start1, null);
    createDependencyNode(gb, start2, null);
    return gb.buildGraph();
  }

  private CompiledViewDefinitionWithGraphsImpl buildCompiledViewDefinition(final DependencyGraph... graphs) {
    final CompiledFunctionResolver compiledResolver = mock(CompiledFunctionResolver.class);
    when(compiledResolver.getFunction(Mockito.<String>any())).thenAnswer(new Answer<CompiledFunctionDefinition>() {
      @Override
      public CompiledFunctionDefinition answer(final InvocationOnMock invocation) throws Throwable {
        final CompiledFunctionDefinition cfd = mock(CompiledFunctionDefinition.class);
        final String function = (String) invocation.getArguments()[0];
        if (function.startsWith("start")) {
          final Instant validFrom = Instant.parse(function.substring(5));
          when(cfd.getEarliestInvocationTime()).thenReturn(validFrom);
        } else if (function.startsWith("end")) {
          final Instant validTo = Instant.parse(function.substring(3));
          when(cfd.getLatestInvocationTime()).thenReturn(validTo);
        }
        return cfd;
      }
    });
    final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setFunctionInitId(0);
    final ViewCompilationServices vcs = mock(ViewCompilationServices.class);
    when(vcs.getFunctionCompilationContext()).thenReturn(compilationContext);
    final ViewCompilationContext context = mock(ViewCompilationContext.class);
    when(context.getActiveResolutions()).thenReturn(new ConcurrentHashMap<ComputationTargetReference, UniqueId>());
    when(context.getCompiledFunctionResolver()).thenReturn(compiledResolver);
    when(context.getResolverVersionCorrection()).thenReturn(VersionCorrection.of(_time0, _time0));
    when(context.getServices()).thenReturn(vcs);
    when(context.getViewDefinition()).thenReturn(mock(ViewDefinition.class));
    return CompiledViewDefinitionWithGraphsImpl.of(context, "", Arrays.asList(graphs), null);
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
    final CompiledViewDefinitionWithGraphsImpl model = buildCompiledViewDefinition(graphNoStartEndTimes(), graphOneStartTime(_time0), graphTwoStartTimes(_time1, _time2),
        graphOneEndTime(_time3), graphTwoEndTimes(_time4, _time5));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time1));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time2));
    assertTrue(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time3));
    assertFalse(CompiledViewDefinitionWithGraphsImpl.isValidFor(model, _time4));
  }

}
