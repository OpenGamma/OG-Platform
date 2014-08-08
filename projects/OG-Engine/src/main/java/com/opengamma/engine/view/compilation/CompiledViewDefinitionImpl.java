/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

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
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link CompiledViewDefinition}.
 */
public class CompiledViewDefinitionImpl implements CompiledViewDefinition {

  private final VersionCorrection _versionCorrection;
  private final String _identifier;
  private final ViewDefinition _viewDefinition;
  private final Portfolio _portfolio;
  private final Map<String, CompiledViewCalculationConfiguration> _compiledCalculationConfigurations;
  private final Instant _earliestValidity;
  private final Instant _latestValidity;
  private volatile Set<ValueSpecification> _marketDataRequirements;

  public CompiledViewDefinitionImpl(final VersionCorrection versionCorrection, final String identifier, final ViewDefinition viewDefinition, final Portfolio portfolio,
      final Collection<CompiledViewCalculationConfiguration> compiledCalculationConfigurations, final Instant earliestValidity, final Instant latestValidity) {
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    assert !versionCorrection.containsLatest();
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    ArgumentChecker.notNull(compiledCalculationConfigurations, "compiledCalculationConfigurations");
    _versionCorrection = versionCorrection;
    _identifier = identifier;
    _viewDefinition = viewDefinition;
    _portfolio = portfolio;
    _compiledCalculationConfigurations = new HashMap<>();
    for (final CompiledViewCalculationConfiguration compiledCalculationConfiguration : compiledCalculationConfigurations) {
      ArgumentChecker.notNull(compiledCalculationConfiguration, "compiledCalculationConfiguration");
      _compiledCalculationConfigurations.put(compiledCalculationConfiguration.getName(), compiledCalculationConfiguration);
    }
    _earliestValidity = earliestValidity;
    _latestValidity = latestValidity;
  }

  protected CompiledViewDefinitionImpl(final CompiledViewDefinitionImpl copyFrom, final VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
    _identifier = copyFrom.getCompilationIdentifier();
    _viewDefinition = copyFrom.getViewDefinition();
    _portfolio = copyFrom.getPortfolio();
    _compiledCalculationConfigurations = copyFrom._compiledCalculationConfigurations;
    _earliestValidity = copyFrom.getValidFrom();
    _latestValidity = copyFrom.getValidTo();
  }

  @Override
  public VersionCorrection getResolverVersionCorrection() {
    return _versionCorrection;
  }

  @Override
  public String getCompilationIdentifier() {
    return _identifier;
  }

  @Override
  public CompiledViewDefinition withResolverVersionCorrection(final VersionCorrection versionCorrection) {
    return new CompiledViewDefinitionImpl(this, versionCorrection);
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
  public Map<String, CompiledViewCalculationConfiguration> getCompiledCalculationConfigurationsMap() {
    return Collections.unmodifiableMap(_compiledCalculationConfigurations);
  }

  @Override
  public Set<ValueSpecification> getMarketDataRequirements() {
    if (_marketDataRequirements == null) {
      final Set<ValueSpecification> allRequirements = new HashSet<>();
      for (final CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
        allRequirements.addAll(compiledCalcConfig.getMarketDataRequirements());
      }
      _marketDataRequirements = Collections.unmodifiableSet(allRequirements);
    }
    return _marketDataRequirements;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalValuesRequirements() {
    final Map<ValueSpecification, Set<ValueRequirement>> allRequirements = new HashMap<>();
    for (final CompiledViewCalculationConfiguration compiledCalcConfig : getCompiledCalculationConfigurations()) {
      allRequirements.putAll(compiledCalcConfig.getTerminalOutputSpecifications());
    }
    return Collections.unmodifiableMap(allRequirements);
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    final Set<ComputationTargetSpecification> allTargets = new HashSet<>();
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
