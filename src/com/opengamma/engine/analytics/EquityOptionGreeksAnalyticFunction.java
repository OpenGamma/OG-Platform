/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * 
 *
 * @author jim
 */
public class EquityOptionGreeksAnalyticFunction implements AnalyticFunction {

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Security security) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return null;
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getShortName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isApplicableTo(String securityType) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isApplicableTo(Position position) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isPositionSpecific() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isSecuritySpecific() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new UnsupportedOperationException("Doesn't build own sub-graph.");
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

}
