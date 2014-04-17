/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Set;

import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;

/**
 * Constructs volatility surface data objects for equity options (single-name and index) if the target is a Bloomberg ticker or weak ticker.
 */
public class RawEquityFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  /** The supported schemes */
  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);

  /**
   * Default constructor
   */
  public RawEquityFutureOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // Bloomberg ticker or weak ticker
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }

  /**
   * The postfix (e.g. Index, Equity) is removed from the Bloomberg ticker when constructing the surface name, so the full name of a surface with
   * <ul>
   * <li>definitionName = OPENGAMMA
   * <li>target=BLOOMBERG_TICKER~DJX Index
   * <ul>
   * is OPENGAMMA_DJX_EQUITY_FUTURE_OPTION {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceDefinition<?, ?> getDefinition(final VolatilitySurfaceDefinitionSource definitionSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + EquitySecurityUtils.getTrimmedTarget(((ExternalIdentifiable) target.getValue()).getExternalId());
    final VolatilitySurfaceDefinition<?, ?> definition = definitionSource.getDefinition(fullDefinitionName, InstrumentTypeProperties.EQUITY_FUTURE_OPTION, versionCorrection);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    }
    return definition;
  }

  /**
   * The postfix (e.g. Index, Equity) is removed from the Bloomberg ticker when constructing the surface name, so the full name of a surface with
   * <ul>
   * <li>specificationName = OPENGAMMA
   * <li>target=BLOOMBERG_TICKER~DJX Index
   * <ul>
   * is OPENGAMMA_DJX_EQUITY_FUTURE_OPTION {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceSpecification getSpecification(final VolatilitySurfaceSpecificationSource specificationSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + EquitySecurityUtils.getTrimmedTarget(((ExternalIdentifiable) target.getValue()).getExternalId());
    final VolatilitySurfaceSpecification specification = specificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.EQUITY_FUTURE_OPTION, versionCorrection);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    }
    return specification;
  }

}
