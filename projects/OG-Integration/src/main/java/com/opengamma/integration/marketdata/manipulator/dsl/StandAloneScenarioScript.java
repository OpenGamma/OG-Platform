/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Script;

/**
 * TODO enforce ordering ordering: view, shocks, scenarios
 */
@SuppressWarnings("unused") // it is used reflectively by Groovy
public abstract class StandAloneScenarioScript extends Script {

  private final ViewDelegate _viewDelegate = new ViewDelegate();
  private final List<Map<String, Object>> _scenarioParamList = Lists.newArrayList();
  private final Simulation _simulation = new Simulation("todo - what name for the simulation? does it matter?");

  /** Scenario parameters keyed by scenario name. The parameters are parameter values keyed by parameter name. */
  private final Map<String, Map<String, Object>> _scenarioParameters = Maps.newHashMap();

  public StandAloneScenarioScript() {
    InputStream scriptStream = SimulationScript.class.getResourceAsStream("InitializeScript.groovy");
    try {
      evaluate(IOUtils.toString(scriptStream));
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to initialize DSL script", e);
    }
  }

  public void view(Closure<?> body) {
    body.setDelegate(_viewDelegate);
    body.call();
  }

  public void shockGrid(Closure<?> body) {
    ShocksDelegate delegate = new ShocksDelegate();
    body.setDelegate(delegate);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    _scenarioParamList.addAll(delegate.cartesianProduct());
  }

  public void shockList(Closure<?> body) {
    ShocksDelegate delegate = new ShocksDelegate();
    body.setDelegate(delegate);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
    _scenarioParamList.addAll(delegate.list());
  }

  public void scenarios(Closure<?> body) {
    for (Map<String, Object> params : _scenarioParamList) {
      List<String> varNamesAndValues = Lists.newArrayListWithCapacity(params.size());
      for (Map.Entry<String, Object> entry : params.entrySet()) {
        String varName = entry.getKey();
        Object varValue = entry.getValue();
        getBinding().setVariable(varName, varValue);
        String varStr = (varValue instanceof String) ? "'" + varValue + "'" : varValue.toString();
        varNamesAndValues.add(varName + "=" + varStr);
      }
      String scenarioName = StringUtils.join(varNamesAndValues, " ");
      Scenario scenario = _simulation.scenario(scenarioName);
      _scenarioParameters.put(scenarioName, params);
      body.setDelegate(new ScenarioDelegate(scenario));
      body.call();
    }
  }

  /** visible for testing */
  /* package */ ViewDelegate getViewDelegate() {
    return _viewDelegate;
  }

  /* package */ List<Map<String, Object>> getScenarioParameterList() {
    return Collections.unmodifiableList(_scenarioParamList);
  }

  /* package */ Map<String, Object> getScenarioParameters(String scenarioName) {
    Map<String, Object> parameters = _scenarioParameters.get(scenarioName);
    if (parameters == null) {
      throw new IllegalArgumentException("No scenario found named " + scenarioName);
    }
    return parameters;
  }

  /* package */ Simulation getSimulation() {
    return _simulation;
  }
}

// TODO abstract delegate that catches unexpected method and property calls and logs them

// TODO extend the abstract delegate
// TODO should this contain the logic to validate and convert the fields? or should it just be a dumb container?
@SuppressWarnings("unused")
/* package */ class ViewDelegate {

  //private final DateTimeFormatter _dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private String _name;
  private String _server;
  private final MarketDataDelegate _marketDataDelegate = new MarketDataDelegate();

  /* package */ void name(String name) {
    _name = name;
  }

  /* package */ void server(String server) {
    _server = server;
  }

  /* package */ void marketData(Closure<?> body) {
    body.setDelegate(_marketDataDelegate);
    body.call();
  }

  /* package */ String getName() {
    return _name;
  }

  /* package */ String getServer() {
    return _server;
  }

  /** Visible for testing */
  /* package */ MarketDataDelegate getMarketDataDelegate() {
    return _marketDataDelegate;
  }
}

@SuppressWarnings("unused")
/* package */ class MarketDataDelegate {

  private final List<MarketDataSpec> _specifications = Lists.newArrayList();

  /* package */ void live(String dataSource) {
    _specifications.add(new MarketDataSpec(MarketDataType.LIVE, dataSource));
  }

  /* package */ void snapshot(String snapshotName) {
    _specifications.add(new MarketDataSpec(MarketDataType.SNAPSHOT, snapshotName));
  }

  /* package */ void fixedHistorical(String date) {
    _specifications.add(new MarketDataSpec(MarketDataType.FIXED_HISTORICAL, date));
  }

  /* latestHistorical doesn't have arguments so Groovy views it as a property get */
  /* package */ Object getLatestHistorical() {
    _specifications.add(new MarketDataSpec(MarketDataType.LATEST_HISTORICAL, null));
    return null;
  }

  /* package */ List<MarketDataSpec> getSpecifications() {
    return _specifications;
  }

  /* package */ enum MarketDataType {
    LIVE, SNAPSHOT, FIXED_HISTORICAL, LATEST_HISTORICAL
  }

  /* package */ static class MarketDataSpec {

    private final MarketDataType _type;
    private final String _spec;

    /* package */ MarketDataSpec(MarketDataType type, String spec) {
      _type = type;
      _spec = spec;
    }

    /* package */ MarketDataType getType() {
      return _type;
    }

    /* package */ String getSpec() {
      return _spec;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_type, _spec);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final MarketDataSpec other = (MarketDataSpec) obj;
      return Objects.equals(this._type, other._type) && Objects.equals(this._spec, other._spec);
    }
  }
}

/* package */ class ShocksDelegate extends GroovyObjectSupport {

  private static final Logger s_logger = LoggerFactory.getLogger(ShocksDelegate.class);

  /**
   * Shock variables. Keys are the variable names, values must be lists of values. It's a linked map so the declaration
   * order in the script is reflected in the way the cartesian product is generated.
   */
  private final Map<String, List<?>> _vars = Maps.newLinkedHashMap();

  @Override
  public void setProperty(String property, Object newValue) {
    if (!(newValue instanceof List)) {
      s_logger.warn("Shocks must be a list, type=" + newValue.getClass().getName() + ", value=" + newValue);
      return;
    }
    if (((List) newValue).size() == 0) {
      s_logger.warn("Shocks must have at least one value");
      return;
    }
    _vars.put(property, (List<?>) newValue);
  }

  /* package */ List<Map<String, Object>> cartesianProduct() {
    if (_vars.isEmpty()) {
      return Collections.emptyList();
    }
    if (_vars.size() != 2) {
      throw new IllegalArgumentException("There must be 2 sets of shocks for shockGrid. For 1 set, use shockList.");
    }
    Iterator<Map.Entry<String, List<?>>> itr = _vars.entrySet().iterator();

    Map.Entry<String, List<?>> outer = itr.next();
    String outerVarName = outer.getKey();
    List<?> outerVarValues = outer.getValue();

    Map.Entry<String, List<?>> inner = itr.next();
    String innerVarName = inner.getKey();
    List<?> innerVarValues = inner.getValue();

    // list of parameters, one map for each scenario
    List<Map<String, Object>> params = Lists.newArrayListWithCapacity(outerVarValues.size() * innerVarValues.size());

    for (Object outerVarValue : outerVarValues) {
      for (Object innerVarValue : innerVarValues) {
        // use a linked map so the var names appear in insertion order - this makes for predictable scenario names
        Map<String, Object> paramMap = Maps.newLinkedHashMap();
        paramMap.put(outerVarName, outerVarValue);
        paramMap.put(innerVarName, innerVarValue);
        params.add(paramMap);
      }
    }
    return params;
  }

  /* package */ List<Map<String, Object>> list() {
    // list of parameters, one map for each scenario
    List<Map<String, Object>> params = Lists.newArrayList();

    // calculate number of scenarios and make sure all shock lists have the right number of values
    int nScenarios = 0;
    for (List<?> varValues : _vars.values()) {
      if (nScenarios == 0) {
        nScenarios = varValues.size();
      } else if (nScenarios != varValues.size()) {
        throw new IllegalArgumentException("All scenario parameters must be lists of the same length");
      }
    }
    // create a map for each scenario and populate it with a value from each shock list
    for (int i = 0; i < nScenarios; i++) {
      // use a linked map so the var names appear in insertion order - this makes for predictable scenario names
      Map<String, Object> map = Maps.newLinkedHashMap();
      for (Map.Entry<String, List<?>> entry : _vars.entrySet()) {
        String varName = entry.getKey();
        List<?> varValues = entry.getValue();
        map.put(varName, varValues.get(i));
      }
      params.add(map);
    }
    return params;
  }
}

/**
 * Delegate for the closure passed to the {@code scenarios} block in the script.
 */
/* package */ class ScenarioDelegate {

  private final Scenario _scenario;

  /* package */ ScenarioDelegate(Scenario scenario) {
    _scenario = scenario;
  }

  public void valuationTime(String valuationTime) {
    _scenario.valuationTime(valuationTime);
  }

  public void calculationConfigurations(String... configNames) {
    _scenario.calculationConfigurations(configNames);
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a curve.
   * @param body The block that defines the selection and transformation
   */
  public void curve(Closure<?> body) {
    DslYieldCurveSelectorBuilder selector = new DslYieldCurveSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a curve.
   * @param body The block that defines the selection and transformation
   */
  public void curveData(Closure<?> body) {
    DslYieldCurveDataSelectorBuilder selector = new DslYieldCurveDataSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a market data point.
   * @param body The block that defines the selection and transformation
   */
  public void marketData(Closure<?> body) {
    DslPointSelectorBuilder selector = new DslPointSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform a volatility surface.
   * @param body The block that defines the selection and transformation
   */
  public void surface(Closure<?> body) {
    DslVolatilitySurfaceSelectorBuilder selector = new DslVolatilitySurfaceSelectorBuilder(_scenario);
    body.setDelegate(selector);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }

  /**
   * Defines a method in the DSL that takes a closure which defines how to select and transform spot rates.
   * @param body The block that defines the selection and transformation
   */
  public void spotRate(Closure<?> body) {
    DslSpotRateSelectorBuilder builder = new DslSpotRateSelectorBuilder(_scenario);
    body.setDelegate(builder);
    body.setResolveStrategy(Closure.DELEGATE_FIRST);
    body.call();
  }
}

