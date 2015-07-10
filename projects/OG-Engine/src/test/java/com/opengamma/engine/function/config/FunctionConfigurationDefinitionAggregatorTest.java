/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link FunctionConfigurationDefinitionAggregator}
 */
@Test(groups = TestGroup.UNIT)
public class FunctionConfigurationDefinitionAggregatorTest {

  private static final StaticFunctionConfiguration SF1 = new StaticFunctionConfiguration("SF1");
  private static final StaticFunctionConfiguration SF2 = new StaticFunctionConfiguration("SF2");
  private static final StaticFunctionConfiguration SF3 = new StaticFunctionConfiguration("SF3");
  private static final StaticFunctionConfiguration SF4 = new StaticFunctionConfiguration("SF4");
  private static final StaticFunctionConfiguration SF5 = new StaticFunctionConfiguration("SF5");
  private static final StaticFunctionConfiguration SF6 = new StaticFunctionConfiguration("SF6");
  private static final StaticFunctionConfiguration SF7 = new StaticFunctionConfiguration("SF7");
  private static final StaticFunctionConfiguration SF8 = new StaticFunctionConfiguration("SF8");

  private static final ParameterizedFunctionConfiguration PF1 = new ParameterizedFunctionConfiguration("PF1", ImmutableList.of("P1"));
  private static final ParameterizedFunctionConfiguration PF2 = new ParameterizedFunctionConfiguration("PF2", ImmutableList.of("P1", "P2"));
  private static final ParameterizedFunctionConfiguration PF3 = new ParameterizedFunctionConfiguration("PF3", ImmutableList.of("P1", "P2", "P3"));
  private static final ParameterizedFunctionConfiguration PF4 = new ParameterizedFunctionConfiguration("PF4", ImmutableList.of("P4"));
  private static final ParameterizedFunctionConfiguration PF5 = new ParameterizedFunctionConfiguration("PF5", ImmutableList.of("P4", "P5"));
  private static final ParameterizedFunctionConfiguration PF6 = new ParameterizedFunctionConfiguration("PF6", ImmutableList.of("P4", "P5", "P6"));
  private static final ParameterizedFunctionConfiguration PF7 = new ParameterizedFunctionConfiguration("PF7", ImmutableList.of("P7"));
  private static final ParameterizedFunctionConfiguration PF8 = new ParameterizedFunctionConfiguration("PF8", ImmutableList.of("P7", "P8"));

  private static final FunctionConfigurationDefinition ROOT_DEFINITION = makeRootDefinition();
  private static final FunctionConfigurationDefinition CF1 = makeDefinition1();
  private static final FunctionConfigurationDefinition CF2 = makeDefinition2();
  private static final FunctionConfigurationDefinition CF3 = makeDefinition3();

  private ConfigSource _cfgSource;

  private FunctionConfigurationDefinitionAggregator _functionConfigAggregator;

  @BeforeMethod
  public void setUp() {
    _cfgSource = mock(ConfigSource.class);
    _functionConfigAggregator = new FunctionConfigurationDefinitionAggregator(_cfgSource);
  }

  @Test
  public void resolveFunctionDefinition() {

    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "ROOT", VersionCorrection.LATEST)).thenReturn(ROOT_DEFINITION);
    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "CF1", VersionCorrection.LATEST)).thenReturn(CF1);
    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "CF2", VersionCorrection.LATEST)).thenReturn(CF2);
    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "CF3", VersionCorrection.LATEST)).thenReturn(CF3);

    final FunctionConfigurationSource configurationSource = _functionConfigAggregator.aggregate("ROOT");
    assertEquals(getExpectedFunctions(), ImmutableSet.copyOf(configurationSource.getFunctionConfiguration(Instant.now()).getFunctions()));
  }

  @Test
  public void cyclicReference() {

    final FunctionConfigurationDefinition defA = new FunctionConfigurationDefinition("DFA", ImmutableList.<String>of("DFB"), ImmutableList.of(SF1, SF2), ImmutableList.of(PF1, PF2));
    final FunctionConfigurationDefinition defB = new FunctionConfigurationDefinition("DFB", ImmutableList.<String>of("DFA"), ImmutableList.of(SF3, SF4), ImmutableList.of(PF3, PF4));

    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "DFA", VersionCorrection.LATEST)).thenReturn(defA);
    when(_cfgSource.getSingle(FunctionConfigurationDefinition.class, "DFB", VersionCorrection.LATEST)).thenReturn(defB);

    Set<FunctionConfiguration> expectedFunc = new HashSet<FunctionConfiguration>();
    expectedFunc.add(SF1);
    expectedFunc.add(SF2);
    expectedFunc.add(SF3);
    expectedFunc.add(SF4);

    expectedFunc.add(PF1);
    expectedFunc.add(PF2);
    expectedFunc.add(PF3);
    expectedFunc.add(PF4);

    final FunctionConfigurationSource configurationSource = _functionConfigAggregator.aggregate("DFA");
    assertEquals(expectedFunc, ImmutableSet.copyOf(configurationSource.getFunctionConfiguration(Instant.now()).getFunctions()));

  }

  private Set<FunctionConfiguration> getExpectedFunctions() {
    Set<FunctionConfiguration> func = new HashSet<FunctionConfiguration>();
    func.add(SF1);
    func.add(SF2);
    func.add(SF3);
    func.add(SF4);
    func.add(SF5);
    func.add(SF6);
    func.add(SF7);
    func.add(SF8);
    func.add(PF1);
    func.add(PF2);
    func.add(PF3);
    func.add(PF4);
    func.add(PF5);
    func.add(PF6);
    func.add(PF7);
    func.add(PF8);
    return func;
  }

  private static FunctionConfigurationDefinition makeRootDefinition() {
    return new FunctionConfigurationDefinition("ROOT", ImmutableList.of("CF1", "CF2"), ImmutableList.of(SF8, SF7), ImmutableList.of(PF7, PF8));
  }

  private static FunctionConfigurationDefinition makeDefinition1() {
    return new FunctionConfigurationDefinition("CF1", ImmutableList.<String>of(), ImmutableList.of(SF6, SF5), ImmutableList.of(PF6, PF5));
  }

  private static FunctionConfigurationDefinition makeDefinition2() {
    return new FunctionConfigurationDefinition("CF2", ImmutableList.of("CF3"), ImmutableList.of(SF4, SF3), ImmutableList.of(PF4, PF3));
  }

  private static FunctionConfigurationDefinition makeDefinition3() {
    return new FunctionConfigurationDefinition("CF3", ImmutableList.<String>of(), ImmutableList.of(SF2, SF1), ImmutableList.of(PF2, PF1));
  }

}
