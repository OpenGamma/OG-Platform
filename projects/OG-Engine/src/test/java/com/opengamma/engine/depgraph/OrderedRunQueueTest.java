/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link OrderedRunQueue} implementations. Basic add/remove will have been tested as part of {@link RunQueueTest} - this is specifically targeting the sorting algorithm.
 */
@Test(groups = TestGroup.UNIT)
public class OrderedRunQueueTest {

  private ContextRunnable runnable() {
    return new ContextRunnable() {
      @Override
      public boolean tryRun(final GraphBuildingContext context) {
        // No-op
        return true;
      }
    };
  }

  private ContextRunnable resolveTask(final ComputationTargetReference target) {
    return new ResolveTask(new ValueRequirement("Value", target), null, null);
  }

  /**
   * Creates a set of tasks in the expected ordering (lowest to highest priority). They will get added in a haphazard order to test the sorting.
   */
  private ContextRunnable[] createTasks() {
    return new ContextRunnable[] {
        /*  0 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Position", "1"))),
        /*  1 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Position", "0"))),
        /*  2 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.TRADE, UniqueId.of("Trade", "1"))),
        /*  3 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.TRADE, UniqueId.of("Trade", "0"))),
        /*  4 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "1"))),
        /*  5 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Security", "0"))),
        /*  6 */resolveTask(new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Test", "Foo"))),
        /*  7 */resolveTask(new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Test", "Bar"))),
        /*  8 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Primitive", "1"))),
        /*  9 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Primitive", "0"))),
        /* 10 */resolveTask(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "Foo"))),
        /* 11 */resolveTask(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "Bar"))),
        /* 12 */resolveTask(ComputationTargetSpecification.NULL),
        /* 13 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "1"))),
        /* 14 */resolveTask(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", "0"))),
        /* 15 */runnable(),
        /* 16 */runnable(),
        /* 17 */runnable()
    };
  }

  private void assertOrder(final OrderedRunQueue queue, final ContextRunnable[] tasks, final int... expected) {
    final Iterator<ContextRunnable> itr = queue.iterator();
    for (int i = 0; i < expected.length; i++) {
      assertTrue(itr.hasNext());
      final ContextRunnable actual = itr.next();
      final ContextRunnable expect = tasks[expected[i]];
      assertSame(actual, expect);
    }
    assertFalse(itr.hasNext());
  }

  public void testAddAndSort() {
    final ContextRunnable[] tasks = createTasks();
    final OrderedRunQueue queue = new OrderedRunQueue(10, 8);
    // Fill the queue to its "unsorted" limit (8); expect that order to be kept
    queue.add(tasks[17]);
    queue.add(tasks[15]);
    queue.add(tasks[13]);
    queue.add(tasks[11]);
    queue.add(tasks[9]);
    queue.add(tasks[7]);
    queue.add(tasks[5]);
    queue.add(tasks[3]);
    assertOrder(queue, tasks, 17, 15, 13, 11, 9, 7, 5, 3);
    // Add one more; the start of the buffer will be sorted
    queue.add(tasks[1]);
    assertOrder(queue, tasks, 11, 13, 17, 15, 9, 7, 5, 3, 1);
    // Fill the unsorted portion
    queue.add(tasks[16]);
    queue.add(tasks[14]);
    queue.add(tasks[12]);
    assertOrder(queue, tasks, 11, 13, 17, 15, 9, 7, 5, 3, 1, 16, 14, 12);
    // Add one more; the front of the buffer will be sorted
    queue.add(tasks[10]);
    assertOrder(queue, tasks, 3, 5, 7, 9, 11, 13, 17, 15, 1, 16, 14, 12, 10);
    // Add remainder
    queue.add(tasks[8]);
    queue.add(tasks[6]);
    queue.add(tasks[4]);
    assertOrder(queue, tasks, 3, 5, 7, 9, 11, 13, 17, 15, 1, 16, 14, 12, 10, 8, 6, 4);
    queue.add(tasks[2]);
    assertOrder(queue, tasks, 1, 3, 5, 7, 9, 11, 12, 13, 14, 17, 16, 15, 10, 8, 6, 4, 2);
    queue.add(tasks[0]);
    assertOrder(queue, tasks, 1, 3, 5, 7, 9, 11, 12, 13, 14, 17, 16, 15, 10, 8, 6, 4, 2, 0);
  }

  // TODO: Test the "take" operation, including reading into the "sorted" section of the array

}
