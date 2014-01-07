/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.exclusion.AbstractFunctionExclusionGroups;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the function exclusion group mechansim.
 */
@Test(groups = TestGroup.UNIT)
public class DepGraphExclusionTest extends AbstractDependencyGraphBuilderTest {

  private static abstract class Group extends AbstractFunctionExclusionGroups {

    protected abstract String getKey(int functionId);

    @Override
    protected String getKey(final FunctionDefinition function) {
      return getKey(Integer.parseInt(function.getUniqueId()));
    }

  }

  private DependencyGraph test(final FunctionExclusionGroups exclusions) {
    TestLifecycle.begin();
    try {
      final DepGraphTestHelper helper = helper();
      final Map<CompiledFunctionDefinition, Integer> priority = new HashMap<CompiledFunctionDefinition, Integer>();
      priority.put(helper.addFunctionRequiringProducing(helper.getRequirement1Bar(), helper.getValue1Foo()), 5); // 0
      priority.put(helper.addFunctionRequiringProducing(helper.getRequirement2Bar(), helper.getValue1Bar()), 5); // 1
      priority.put(helper.addFunctionRequiringProducing(helper.getRequirement2Foo(), helper.getValue2Bar()), 5); // 2
      priority.put(helper.addFunctionProducing(helper.getValue1Foo()), 1); // 3
      priority.put(helper.addFunctionProducing(helper.getValue1Bar()), 1); // 4
      priority.put(helper.addFunctionProducing(helper.getValue2Bar()), 1); // 5
      priority.put(helper.addFunctionProducing(helper.getValue2Foo()), 1); // 6
      final DependencyGraphBuilder builder = helper.createBuilder(new FunctionPriority() {
        @Override
        public int getPriority(final CompiledFunctionDefinition function) {
          return priority.get(function);
        }
      });
      builder.setFunctionExclusionGroups(exclusions);
      builder.addTarget(helper.getRequirement1Foo());
      return builder.getDependencyGraph();
    } finally {
      TestLifecycle.end();
    }
  }

  public void noGroups() {
    final DependencyGraph graph = test(new FunctionExclusionGroups() {

      @Override
      public FunctionExclusionGroup getExclusionGroup(final FunctionDefinition function) {
        return null;
      }

      @Override
      public boolean isExcluded(final FunctionExclusionGroup current, final Collection<FunctionExclusionGroup> existing) {
        return false;
      }

      @Override
      public Collection<FunctionExclusionGroup> withExclusion(final Collection<FunctionExclusionGroup> existing, final FunctionExclusionGroup newGroup) {
        // Should never be called
        fail();
        throw new UnsupportedOperationException();
      }

    });
    assertEquals(4, graph.getSize()); // 6 -> 2 -> 1 -> 0
  }

  public void singleGroups() {
    final DependencyGraph graph = test(new Group() {
      @Override
      protected String getKey(final int function) {
        return Integer.toString(function);
      }
    });
    assertEquals(4, graph.getSize()); // 6 -> 2 -> 1 -> 0
  }

  public void group01group2() {
    final DependencyGraph graph = test(new Group() {
      @Override
      protected String getKey(final int function) {
        switch (function) {
          case 0:
          case 1:
            return "A";
          case 2:
            return "B";
          default:
            return null;
        }
      }
    });
    assertEquals(2, graph.getSize()); // 4 -> 0
  }

  public void group0group26() {
    final DependencyGraph graph = test(new Group() {
      @Override
      protected String getKey(final int function) {
        switch (function) {
          case 0:
            return "A";
          case 2:
          case 6:
            return "B";
          default:
            return null;
        }
      }
    });
    assertEquals(3, graph.getSize()); // 5 -> 1 -> 0
  }

  public void group02() {
    final DependencyGraph graph = test(new Group() {
      @Override
      protected String getKey(final int function) {
        switch (function) {
          case 0:
          case 2:
            return "A";
          default:
            return null;
        }
      }
    });
    assertEquals(4, graph.getSize()); // 6 -> 2 -> 1 -> 0
  }

  public void group014() {
    final DependencyGraph graph = test(new Group() {
      @Override
      protected String getKey(final int function) {
        switch (function) {
          case 0:
          case 1:
          case 4:
            return "A";
          default:
            return null;
        }
      }
    });
    assertEquals(1, graph.getSize()); // 3
  }

}
