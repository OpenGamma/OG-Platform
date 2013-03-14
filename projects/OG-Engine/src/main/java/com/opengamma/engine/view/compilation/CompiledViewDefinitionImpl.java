/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static com.opengamma.util.functional.Functional.merge;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link CompiledViewDefinition}.
 */
public class CompiledViewDefinitionImpl implements CompiledViewDefinition {

  private final ViewDefinition _viewDefinition;
  private final Portfolio _portfolio;
  private final Map<String, CompiledViewCalculationConfiguration> _compiledCalculationConfigurations;
  private final Instant _earliestValidity;
  private final Instant _latestValidity;
  private volatile Set<ValueSpecification> _marketDataRequirements;

  public CompiledViewDefinitionImpl(final ViewDefinition viewDefinition, final Portfolio portfolio,
      final Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations,
      final Instant earliestValidity, final Instant latestValidity) {
    _viewDefinition = viewDefinition;
    _portfolio = portfolio;
    _compiledCalculationConfigurations = new HashMap<String, CompiledViewCalculationConfiguration>();
    for (final CompiledViewCalculationConfiguration compiledCalculationConfiguration : compiledCalculationConfigurations) {
      _compiledCalculationConfigurations.put(compiledCalculationConfiguration.getName(), compiledCalculationConfiguration);
    }
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
  public CompiledViewCalculationConfiguration getCompiledCalculationConfiguration(final String viewCalculationConfiguration) {
    ArgumentChecker.notNull(viewCalculationConfiguration, "viewCalculationConfiguration");
    return _compiledCalculationConfigurations.get(viewCalculationConfiguration);
  }

  @Override
  public Collection<CompiledViewCalculationConfiguration> getCompiledCalculationConfigurations() {
    return Collections.unmodifiableCollection(_compiledCalculationConfigurations.values());
  }

  @Override
  public Set<ValueSpecification> getMarketDataRequirements() {
    if (_marketDataRequirements == null) {
      final Set<ValueSpecification> allRequirements = new HashSet<ValueSpecification>();
      for (final CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
        allRequirements.addAll(compiledCalcConfig.getMarketDataRequirements());
      }
      _marketDataRequirements = Collections.unmodifiableSet(allRequirements);
    }
    return _marketDataRequirements;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements() {
    final Map<ValueSpecification, Set<ValueRequirement>> allRequirements = new HashMap<ValueSpecification, Set<ValueRequirement>>();
    for (final CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
      merge(allRequirements, compiledCalcConfig.getTerminalOutputSpecifications());
    }
    return Collections.unmodifiableMap(allRequirements);
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    final Set<ComputationTargetSpecification> allTargets = new HashSet<ComputationTargetSpecification>();
    for (final CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
      allTargets.addAll(compiledCalcConfig.getComputationTargets());
    }
    return Collections.unmodifiableSet(allTargets);
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
   * Checks whether the compilation results encapsulated in an instance are valid for a specific cycle. Note that this does not ensure that the view definition used for compilation is still
   * up-to-date.
   * 
   * @param viewDefinition the compiled view definition instance, not null
   * @param valuationTime the valuation time, not null
   * @return true if the compilation results are valid for the valuation time
   */
  public static boolean isValidFor(final CompiledViewDefinition viewDefinition, final Instant valuationTime) {
    final Instant validFrom = viewDefinition.getValidFrom();
    if ((validFrom != null) && valuationTime.isBefore(validFrom)) {
      return false;
    }
    final Instant validTo = viewDefinition.getValidTo();
    return (validTo == null) || !valuationTime.isAfter(validTo);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "CompiledViewDefinition[" + getViewDefinition().getName() + ", " + getValidityString() + "]";
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
