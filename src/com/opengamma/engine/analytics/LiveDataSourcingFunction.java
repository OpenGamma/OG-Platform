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
import com.opengamma.engine.viewer.ValueDefinitionRenderingVisitor;
import com.opengamma.engine.viewer.VisitableValueDefinition;

/**
 * A meta-function which can be put into any dependency graph to indicate
 * that a particular output is going to be sourced from the live data repository. 
 *
 * @author kirk
 */
public class LiveDataSourcingFunction extends AbstractPrimitiveFunction
implements PrimitiveFunctionDefinition, PrimitiveFunctionInvoker {
  private final AnalyticValueDefinition<?> _specifiedResult;
  private final String _shortName;
  private final ValueDefinitionRenderingVisitor _renderingVisitor = new ValueDefinitionRenderingVisitor();
  public LiveDataSourcingFunction(AnalyticValueDefinition<?> specifiedResult) {
    if(specifiedResult == null) {
      throw new NullPointerException("Must specify the desired live data.");
    }
    _specifiedResult = specifiedResult;
    if (_specifiedResult instanceof VisitableValueDefinition) {
      _shortName = "Live Data for " + ((VisitableValueDefinition)specifiedResult).accept(_renderingVisitor);
    } else {
      _shortName = "Live Data For " + specifiedResult;
    }
  }

  /**
   * @return the specifiedResult
   */
  public AnalyticValueDefinition<?> getSpecifiedResult() {
    return _specifiedResult;
  }

  @Override
  public Collection<AnalyticValue<?>> execute(
      FunctionExecutionContext executionContext, FunctionInputs inputs) {
    throw new NotImplementedException("LiveDataSourcingFunction should never be executed.");
  }

  @Override
  public Collection<AnalyticValueDefinition<?>> getInputs() {
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
  public DependencyNode buildSubGraph(AnalyticFunctionResolver functionResolver,
      DependencyNodeResolver dependencyNodeResolver) {
    throw new UnsupportedOperationException("Does not build own sub graph");
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

}
