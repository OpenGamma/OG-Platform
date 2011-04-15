/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CompiledViewDefinitionImpl implements CompiledViewDefinition {

  private final ViewDefinition _viewDefinition;
  private final Portfolio _portfolio;
  private final Map<ValueRequirement, ValueSpecification> _liveDataRequirements;
  private final Set<String> _outputValueNames;
  private final Set<ComputationTarget> _computationTargets;
  private final Set<String> _securityTypes;
  private final Instant _earliestValidity;
  private final Instant _latestValidity;

  public CompiledViewDefinitionImpl(ViewDefinition viewDefinition, Portfolio portfolio,
      Map<ValueRequirement, ValueSpecification> liveDataRequirements, Set<String> outputValueNames,
      Set<ComputationTarget> computationTargets, Set<String> securityTypes, Instant earliestValidity,
      Instant latestValidity) {
    _viewDefinition = viewDefinition;
    _portfolio = portfolio;
    _liveDataRequirements = liveDataRequirements;
    _outputValueNames = outputValueNames;
    _computationTargets = computationTargets;
    _securityTypes = securityTypes;
    _earliestValidity = earliestValidity;
    _latestValidity = latestValidity;
  }
  
  @Override
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  @Override
  public Portfolio getPortfolio() {
    return _portfolio;
  }

  @Override
  public Map<ValueRequirement, ValueSpecification> getLiveDataRequirements() {
    return _liveDataRequirements;
  }

  @Override
  public Set<String> getOutputValueNames() {
    return _outputValueNames;
  }

  @Override
  public Set<ComputationTarget> getComputationTargets() {
    return _computationTargets;
  }

  @Override
  public Set<String> getSecurityTypes() {
    return _securityTypes;
  }

  @Override
  public Instant getValidFrom() {
    return _earliestValidity;
  }

  @Override
  public Instant getValidTo() {
    return _latestValidity;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Checks whether the compilation results encapsulated in this instance are valid for a specific valuation time.
   * Note that this does not ensure that the view definition used for compilation is still up-to-date.
   * 
   * @param valuationTimeProvider  the valuation time
   * @return  {@code true} if the compilation results are valid for the valuation time, {@code false} otherwise
   */
  public boolean isValidFor(final InstantProvider valuationTimeProvider) {
    ArgumentChecker.notNull(valuationTimeProvider, "valuationTimeProvider");
    Instant valuationTime = valuationTimeProvider.toInstant();
    return (_earliestValidity == null || !valuationTime.isBefore(_earliestValidity))
        && (_latestValidity == null || !valuationTime.isAfter(_latestValidity));
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "CompiledViewDefinitionWithGraphs[" + getViewDefinition().getName() + ", " + getValidityString() + "]";
  }
  
  protected String getValidityString() {
    if (_earliestValidity == null && _latestValidity == null) {
      return "unrestricted validity";
    } else if (_earliestValidity == null) {
      return "valid until " + _latestValidity.toString();
    } else if (_latestValidity == null) {
      return "valid from " + _earliestValidity.toString();
    } else {
      return "valid from " + _earliestValidity.toString() + " to " + _latestValidity.toString();
    }
  }

}
