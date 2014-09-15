/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.function.scenarios;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class ScenarioDefinitionTest {

  private final Arg1 _arg11 = new Arg1();
  private final Arg1 _arg12 = new Arg1();
  private final Arg2 _arg21 = new Arg2();
  private final Arg2 _arg22 = new Arg2();

  @Test
  public void withArgumentsList() {
    List<ScenarioArgument<?, ?>> argList = ImmutableList.<ScenarioArgument<?, ?>>of(_arg11, _arg21);
    ScenarioDefinition args = new ScenarioDefinition(_arg12, _arg22).with(argList);
    FilteredScenarioDefinition filteredArgs = args.filter("foo");

    List<Arg1> args1 = filteredArgs.getArguments(new Fn1());
    assertEquals(2, args1.size());
    assertTrue(args1.contains(_arg11));
    assertTrue(args1.contains(_arg12));

    List<Arg2> args2 = filteredArgs.getArguments(new Fn2());
    assertEquals(2, args2.size());
    assertTrue(args2.contains(_arg21));
    assertTrue(args2.contains(_arg22));
  }

  @Test
  public void withArgumentsArray() {
    ScenarioDefinition args = new ScenarioDefinition(_arg11, _arg12).with(_arg21, _arg22);
    FilteredScenarioDefinition filteredArgs = args.filter("foo");

    List<Arg1> args1 = filteredArgs.getArguments(new Fn1());
    assertEquals(2, args1.size());
    assertTrue(args1.contains(_arg11));
    assertTrue(args1.contains(_arg12));

    List<Arg2> args2 = filteredArgs.getArguments(new Fn2());
    assertEquals(2, args2.size());
    assertTrue(args2.contains(_arg21));
    assertTrue(args2.contains(_arg22));
  }

  @Test
  public void withArgumentsForColumn() {
    List<ScenarioArgument<?, ?>> argList = ImmutableList.<ScenarioArgument<?, ?>>of(_arg11);
    Map<String, List<ScenarioArgument<?, ?>>> argMap =
        ImmutableMap.<String, List<ScenarioArgument<?, ?>>>of("foo", ImmutableList.<ScenarioArgument<?, ?>>of(_arg21));
    ScenarioDefinition baseArgs = new ScenarioDefinition(argList, argMap);

    List<ScenarioArgument<?, ?>> extraArgs = ImmutableList.<ScenarioArgument<?, ?>>of(_arg12, _arg22);
    ScenarioDefinition scenarioDefinition = baseArgs.with(extraArgs, "foo");
    FilteredScenarioDefinition filteredArgs = scenarioDefinition.filter("foo");

    List<Arg1> args1 = filteredArgs.getArguments(new Fn1());
    assertEquals(2, args1.size());
    assertTrue(args1.contains(_arg11));
    assertTrue(args1.contains(_arg12));

    List<Arg2> args2 = filteredArgs.getArguments(new Fn2());
    assertEquals(2, args2.size());
    assertTrue(args2.contains(_arg21));
    assertTrue(args2.contains(_arg22));
  }

  @Test
  public void filter() {
    List<ScenarioArgument<?, ?>> argList = ImmutableList.<ScenarioArgument<?, ?>>of(_arg11);
    Map<String, List<ScenarioArgument<?, ?>>> argMap = new HashMap<>();
    argMap.put("foo", ImmutableList.<ScenarioArgument<?, ?>>of(_arg21));
    ScenarioDefinition scenarioDefinition = new ScenarioDefinition(argList, argMap);

    FilteredScenarioDefinition barArgs = scenarioDefinition.filter("bar");

    List<Arg1> barArgs1 = barArgs.getArguments(new Fn1());
    assertEquals(1, barArgs1.size());
    assertTrue(barArgs1.contains(_arg11));

    List<Arg2> barArgs2 = barArgs.getArguments(new Fn2());
    assertTrue(barArgs2.isEmpty());

    FilteredScenarioDefinition fooArgs = scenarioDefinition.filter("foo");

    List<Arg1> fooArgs1 = fooArgs.getArguments(new Fn1());
    assertEquals(1, fooArgs1.size());
    assertTrue(fooArgs1.contains(_arg11));

    List<Arg2> fooArgs2 = fooArgs.getArguments(new Fn2());
    assertEquals(1, fooArgs2.size());
    assertTrue(fooArgs2.contains(_arg21));
  }

  @Test
  public void mergedWith() {
    Arg1 arg13 = new Arg1();
    Arg2 arg23 = new Arg2();

    ScenarioDefinition args1 = new ScenarioDefinition(_arg11).with(_arg12, "foo").with(arg13, "bar");
    ScenarioDefinition args2 = new ScenarioDefinition(_arg21).with(_arg22, "foo").with(arg23, "bar");
    ScenarioDefinition merged = args1.mergedWith(args2);
    FilteredScenarioDefinition fooArgs = merged.filter("foo");
    FilteredScenarioDefinition barArgs = merged.filter("bar");

    List<Arg1> fooArgs1 = fooArgs.getArguments(new Fn1());
    assertEquals(2, fooArgs1.size());
    assertTrue(fooArgs1.contains(_arg11));
    assertTrue(fooArgs1.contains(_arg12));

    List<Arg1> barArgs1 = barArgs.getArguments(new Fn1());
    assertEquals(2, barArgs1.size());
    assertTrue(barArgs1.contains(_arg11));
    assertTrue(barArgs1.contains(arg13));

    List<Arg2> fooArgs2 = fooArgs.getArguments(new Fn2());
    assertEquals(2, fooArgs2.size());
    assertTrue(fooArgs2.contains(_arg21));
    assertTrue(fooArgs2.contains(_arg22));

    List<Arg2> barArgs2 = barArgs.getArguments(new Fn2());
    assertEquals(2, barArgs2.size());
    assertTrue(barArgs2.contains(_arg21));
    assertTrue(barArgs2.contains(arg23));
  }

  @Test
  public void getFunctionTypes() {
    List<ScenarioArgument<?, ?>> argList = ImmutableList.<ScenarioArgument<?, ?>>of(_arg11);
    Map<String, List<ScenarioArgument<?, ?>>> argMap = new HashMap<>();
    argMap.put("foo", ImmutableList.<ScenarioArgument<?, ?>>of(_arg21));
    ScenarioDefinition scenarioDefinition = new ScenarioDefinition(argList, argMap);

    Set<Class<? extends ScenarioFunction<?, ?>>> functionTypes = scenarioDefinition.getFunctionTypes();
    assertTrue(functionTypes.contains(Fn1.class));
    assertTrue(functionTypes.contains(Fn2.class));
  }

  public static class Arg1 implements ScenarioArgument<Arg1, Fn1> {

    @Override
    public Class<Fn1> getFunctionType() {
      return Fn1.class;
    }
  }

  public static class Fn1 implements ScenarioFunction<Arg1, Fn1> {

    @Override
    public Class<Arg1> getArgumentType() {
      return Arg1.class;
    }
  }

  public static class Arg2 implements ScenarioArgument<Arg2, Fn2> {

    @Override
    public Class<Fn2> getFunctionType() {
      return Fn2.class;
    }
  }

  public static class Fn2 implements ScenarioFunction<Arg2, Fn2> {

    @Override
    public Class<Arg2> getArgumentType() {
      return Arg2.class;
    }
  }
}
