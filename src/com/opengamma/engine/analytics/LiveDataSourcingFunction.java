/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * A meta-function which can be put into any dependency graph to indicate
 * that a particular output is going to be sourced from the live data repository. 
 *
 * @author kirk
 */
public class LiveDataSourcingFunction extends AbstractAnalyticFunction implements AnalyticFunctionInvoker {
  private final AnalyticValueDefinition<?> _specifiedResult;
  private final String _shortName;
  
  public LiveDataSourcingFunction(AnalyticValueDefinition<?> specifiedResult) {
    if(specifiedResult == null) {
      throw new NullPointerException("Must specify the desired live data.");
    }
    _specifiedResult = specifiedResult;
    _shortName = "Live Data Source For " + specifiedResult;
  }

  /**
   * @return the specifiedResult
   */
  public AnalyticValueDefinition<?> getSpecifiedResult() {
    return _specifiedResult;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return Collections.emptySet();
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(_specifiedResult);
  }

  @Override
  public String getShortName() {
    return _shortName;
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    return true;
  }

  @Override
  public boolean isApplicableTo(Position position) {
    return true;
  }

  @Override
  public boolean isPositionSpecific() {
    return false;
  }

  @Override
  public boolean isSecuritySpecific() {
    return false;
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    throw new NotImplementedException("LiveDataSourcingFunction.execute() not yet implemented.");
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Security security) {
    throw new NotImplementedException("LiveDataSourcingFunction.execute() not yet implemented.");
  }

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new UnsupportedOperationException("Does not build own sub graph");
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

}
