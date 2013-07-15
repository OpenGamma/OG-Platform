/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the function exclusion group class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionExclusionGroupTest {

  private static class Function implements FunctionDefinition {

    private final String _shortName;

    private Function(String shortName) {
      _shortName = shortName;
    }

    @Override
    public void init(FunctionCompilationContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getUniqueId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getShortName() {
      return _shortName;
    }

    @Override
    public FunctionParameters getDefaultParameters() {
      throw new UnsupportedOperationException();
    }

  }

  public void testAbstract() {
    final FunctionExclusionGroups groups = new AbstractFunctionExclusionGroups() {
      @Override
      protected String getKey(final FunctionDefinition function) {
        if (function.getShortName().startsWith("A_")) {
          return "A";
        } else if (function.getShortName().startsWith("B_")) {
          return "B";
        } else {
          return null;
        }
      }
    };
    final FunctionDefinition a_foo = new Function("A_foo");
    final FunctionDefinition a_bar = new Function("A_bar");
    final FunctionDefinition b_foo = new Function("B_foo");
    final FunctionDefinition b_bar = new Function("B_bar");
    final FunctionDefinition foo = new Function("foo");
    final FunctionDefinition bar = new Function("bar");
    assertNull(groups.getExclusionGroup(foo));
    assertNull(groups.getExclusionGroup(bar));
    final FunctionExclusionGroup afoo1 = groups.getExclusionGroup(a_foo);
    final FunctionExclusionGroup afoo2 = groups.getExclusionGroup(a_foo);
    assertEquals(afoo1, afoo2);
    final FunctionExclusionGroup abar = groups.getExclusionGroup(a_bar);
    assertEquals(abar, afoo1);
    final FunctionExclusionGroup bfoo = groups.getExclusionGroup(b_foo);
    assertNotEquals(abar, bfoo);
    final FunctionExclusionGroup bbar = groups.getExclusionGroup(b_bar);
    assertEquals(bbar, bfoo);
  }

}
