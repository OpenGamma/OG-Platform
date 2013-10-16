/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.Assert;

import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Base class of dependency graph building algorithm tests.
 */
/* package */class AbstractDependencyGraphBuilderTest {

  protected DepGraphTestHelper helper() {
    return new DepGraphTestHelper();
  }

  private void blockOnTask(final DependencyGraphBuilder builder, final ResolvedValueProducer task, final String expected) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<String> result = new AtomicReference<String>();
    task.addCallback(builder.getContext(), new ResolvedValueCallback() {

      @Override
      public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
        result.set("FAILED");
        latch.countDown();
      }

      @Override
      public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
        result.set("COMPLETE");
        latch.countDown();
        if (pump != null) {
          context.close(pump);
        }
      }

      @Override
      public void recursionDetected() {
        // No-op
      }

    });
    assertTrue(builder.startBackgroundConstructionJob());
    try {
      latch.await(com.opengamma.util.test.Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Assert.fail("Interrupted", e);
    }
    assertEquals(expected, result.get());
  }

  protected void expectFailure(final DependencyGraphBuilder builder, final ResolvedValueProducer task) {
    blockOnTask(builder, task, "FAILED");
  }

  protected void expectCompletion(final DependencyGraphBuilder builder, final ResolvedValueProducer task) {
    blockOnTask(builder, task, "COMPLETE");
  }

  protected Map<MockFunction, DependencyNode> assertGraphContains(final DependencyGraph graph, final MockFunction... functions) {
    final Collection<DependencyNode> nodes = DependencyGraphImpl.getDependencyNodes(graph);
    final LinkedList<MockFunction> functionList = new LinkedList<MockFunction>(Arrays.asList(functions));
    final Map<MockFunction, DependencyNode> result = new HashMap<MockFunction, DependencyNode>();
    for (DependencyNode node : nodes) {
      MockFunction function = null;
      final Iterator<MockFunction> itr = functionList.iterator();
      while (itr.hasNext()) {
        final MockFunction f = itr.next();
        if (node.getFunction().getFunctionId().equals(f.getUniqueId())) {
          function = f;
          itr.remove();
          break;
        }
      }
      if (function == null) {
        Assert.fail(node.toString() + " not in expected functions");
      }
      result.put(function, node);
    }
    if (!functionList.isEmpty()) {
      Assert.fail(functionList.toString() + " not in graph");
    }
    return result;
  }

  protected static abstract class TestFunction extends AbstractFunction.NonCompiledInvoker {

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.ANYTHING;
    }

    public int getPriority() {
      return 0;
    }

  };

}
