/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeResolver;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */


public class HardCodedCostOfCarryAnalyticFunction<T extends OptionDefinition>
extends AbstractAnalyticFunction
implements AnalyticFunctionInvoker {
  
  private final double _costOfCarry;
  
  @SuppressWarnings("unchecked")
  private static final AnalyticValueDefinition<Double> s_resultDefinition = 
    new AnalyticValueDefinitionImpl<Double>(new KeyValuePair<String, Object>("TYPE", "COST_OF_CARRY"));

  public HardCodedCostOfCarryAnalyticFunction(double costOfCarry) {
    _costOfCarry = costOfCarry;
  }
  
  public HardCodedCostOfCarryAnalyticFunction() {
    _costOfCarry = 0.01;
  }
  
  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Position position) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<AnalyticValue<?>> execute(AnalyticFunctionInputs inputs,
      Security security) {
    return Collections.<AnalyticValue<?>>singleton(
        new DoubleAnalyticValue(
            s_resultDefinition, _costOfCarry)
    );
  }
  
  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs(Security security) {
    return Collections.<AnalyticValueDefinition<?>>emptySet();
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getPossibleResults() {
    return Collections.<AnalyticValueDefinition<?>>singleton(s_resultDefinition);
  }

  @Override
  public String getShortName() {
    return "HardCodedCostOfCarry";
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
  public DependencyNode buildSubGraph(Security security,
      AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    // TODO Auto-generated method stub
    return false;
  }

}
