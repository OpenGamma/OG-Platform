/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.integration.marketdata.manipulator.dsl.MarketDataDelegate.MarketDataType.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

import groovy.lang.GroovyShell;

@Test(groups = TestGroup.UNIT)
public class StandAloneScenarioScriptTest {

  private static final String FOO = "foo";
  private static final String BAR = "bar";

  @Test
  public void view() {
    String scriptText =
        "view {\n" +
        "  name 'an example view'\n" +
        "  server 'svr:8080'\n" +
        "  valuationTime '2014-01-28 12:00'\n" +
        "  aggregators 'agg1', 'agg2', 'agg3'\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);
    ViewDelegate viewDelegate = script.getViewDelegate();
    assertEquals("an example view", viewDelegate.getName());
    assertEquals("svr:8080", viewDelegate.getServer());
    assertEquals("2014-01-28 12:00", viewDelegate.getValuationTime());
    assertEquals(ImmutableList.of("agg1", "agg2", "agg3"), viewDelegate.getAggregators());
  }

  @Test
  public void marketData() {
    String scriptText =
        "view {\n" +
        "  marketData {\n" +
        "    live 'Bloomberg'\n" +
        "    live 'Activ'\n" +
        "    snapshot 'the snapshot name'\n" +
        "    fixedHistorical '2011-03-08 11:30'\n" +
        "    latestHistorical\n" +
        "    fixedHistorical '2012-03-10 12:30'\n" +
        "  }\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);
    ImmutableList<MarketDataDelegate.MarketDataSpec> expectedSpecs =
        ImmutableList.of(spec(LIVE, "Bloomberg"),
                         spec(LIVE, "Activ"),
                         spec(SNAPSHOT, "the snapshot name"),
                         spec(FIXED_HISTORICAL, "2011-03-08 11:30"),
                         spec(LATEST_HISTORICAL, null),
                         spec(FIXED_HISTORICAL, "2012-03-10 12:30"));
    MarketDataDelegate marketDataDelegate = script.getViewDelegate().getMarketDataDelegate();
    assertEquals(expectedSpecs, marketDataDelegate.getSpecifications());
  }

  @Test
  public void shockList() {
    String scriptText =
        "shockList {\n" +
        "  foo = [1, 2, 3]\n" +
        "  bar = ['a', 'b', 'c']\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);
    List<Map<String,Object>> params = script.getScenarioParameters();
    assertEquals(3, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(2, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(3, params.get(2).get(FOO));
    assertEquals("c", params.get(2).get(BAR));
  }

  @Test
  public void shockGrid() {
    String scriptText =
        "shockGrid {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);
    List<Map<String,Object>> params = script.getScenarioParameters();
    assertEquals(4, params.size());

    assertEquals(1, params.get(0).get(FOO));
    assertEquals("a", params.get(0).get(BAR));

    assertEquals(1, params.get(1).get(FOO));
    assertEquals("b", params.get(1).get(BAR));

    assertEquals(2, params.get(2).get(FOO));
    assertEquals("a", params.get(2).get(BAR));

    assertEquals(2, params.get(3).get(FOO));
    assertEquals("b", params.get(3).get(BAR));
  }

  @Test
  public void scenarioList() {
    String scriptText =
        "shockList {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "}\n" +
        "scenarios {\n" +
        "  marketData {\n" +
        "    id 'SCHEME', bar\n" +
        "    apply {\n" +
        "      shift foo\n" +
        "    }\n" +
        "  }\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);

    Simulation simulation = script.getSimulation();
    Map<String, Scenario> scenarios = simulation.getScenarios();
    assertEquals(2, scenarios.size());

    checkScenario(scenarios, "a", 1);
    checkScenario(scenarios, "b", 2);
  }

  @Test
  public void scenarioGrid() {
    String scriptText =
        "shockGrid {\n" +
        "  foo = [1, 2]\n" +
        "  bar = ['a', 'b']\n" +
        "}\n" +
        "scenarios {\n" +
        "  marketData {\n" +
        "    id 'SCHEME', bar\n" +
        "    apply {\n" +
        "      shift foo\n" +
        "    }\n" +
        "  }\n" +
        "}";
    StandAloneScenarioScript script = createScript(scriptText);

    Simulation simulation = script.getSimulation();
    Map<String, Scenario> scenarios = simulation.getScenarios();
    assertEquals(4, scenarios.size());
    checkScenario(scenarios, "a", 1);
    checkScenario(scenarios, "a", 2);
    checkScenario(scenarios, "b", 1);
    checkScenario(scenarios, "b", 2);
  }

  private static void checkScenario(Map<String, Scenario> scenarios , String id, double shiftAmount) {
    Scenario scenario = scenarios.get("bar='" + id + "', foo=" + (int) shiftAmount + "");
    assertNotNull(scenario);
    ScenarioDefinition definition = scenario.createDefinition();
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = definition.getDefinitionMap();
    PointSelector selector =
        new PointSelector(null, ImmutableSet.of(ExternalId.of("SCHEME", id)), null, null, null, null, null);
    FunctionParameters parameters = definitionMap.get(selector);
    CompositeStructureManipulator compositeManipulator =
        ((SimpleFunctionParameters) parameters).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    MarketDataShift shift = (MarketDataShift) compositeManipulator.getManipulators().get(0);
    assertEquals(1 + shiftAmount, shift.execute(1.0, null, null));
  }

  private static StandAloneScenarioScript createScript(String scriptText) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(StandAloneScenarioScript.class.getName());
    GroovyShell shell = new GroovyShell(config);
    StandAloneScenarioScript script = (StandAloneScenarioScript) shell.parse(scriptText);
    script.run();
    return script;
  }

  private static MarketDataDelegate.MarketDataSpec spec(MarketDataDelegate.MarketDataType type, String spec) {
    return new MarketDataDelegate.MarketDataSpec(type, spec);
  }
}
