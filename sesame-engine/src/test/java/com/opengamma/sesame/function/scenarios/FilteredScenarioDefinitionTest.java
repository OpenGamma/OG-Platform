/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.annotation.Nullable;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FilteredScenarioDefinitionTest {

  @Test
  public void forFunction() {
    Arg1 arg1 = new Arg1();
    Arg2 arg2 = new Arg2();
    FilteredScenarioDefinition args = new FilteredScenarioDefinition(arg1, arg2);

    List<Arg1> args1 = args.getArguments(new Function1());
    assertEquals(1, args1.size());
    assertSame(arg1, args1.get(0));

    List<Arg2> args2 = args.getArguments(new Function2());
    assertEquals(1, args2.size());
    assertSame(arg2, args2.get(0));

    FilteredScenarioDefinition filteredArgs = args.forFunctions(ImmutableSet.<Class<?>>of(Function1.class));

    List<Arg1> filteredArgs1 = filteredArgs.getArguments(new Function1());
    assertEquals(1, filteredArgs1.size());
    assertSame(arg1, filteredArgs1.get(0));

    List<Arg2> filteredArgs2 = filteredArgs.getArguments(new Function2());
    assertTrue(filteredArgs2.isEmpty());
  }

  public class Function1 implements ScenarioFunction<Arg1, Function1> {

    @Nullable
    @Override
    public Class<Arg1> getArgumentType() {
      return Arg1.class;
    }
  }

  public class Function2 implements ScenarioFunction<Arg2, Function2> {

    @Nullable
    @Override
    public Class<Arg2> getArgumentType() {
      return Arg2.class;
    }
  }

  public static final class Arg1 implements ScenarioArgument<Arg1, Function1> {

    @Override
    public Class<Function1> getFunctionType() {
      return Function1.class;
    }
  }

  public static final class Arg2 implements ScenarioArgument<Arg2, Function2> {

    @Override
    public Class<Function2> getFunctionType() {
      return Function2.class;
    }
  }
}
