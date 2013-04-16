/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.equity.EquitySecurityUtils;

/**
 * Constructs volatility surface data objects for equity options (single-name and index) if the target is a Bloomberg
 * ticker or weak ticker.
 */
public class RawEquityFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {

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
  protected boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName())) ||
        targetScheme.equalsIgnoreCase(ExternalSchemes.ACTIVFEED_TICKER.getName());
  }

  /**
   * The postfix (e.g. Index, Equity) is removed from the Bloomberg ticker when constructing the surface name, so the full name of a surface with
   * <ul>
   * <li> definitionName = OPENGAMMA
   * <li> target=BLOOMBERG_TICKER~DJX Index
   * <ul>
   * is OPENGAMMA_DJX_EQUITY_FUTURE_OPTION
   * {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceDefinition<?, ?> getDefinition(final VolatilitySurfaceDefinitionSource definitionSource, final ComputationTarget target, final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + EquitySecurityUtils.getTrimmedTarget(target.getUniqueId());
    final VolatilitySurfaceDefinition<?, ?> definition = definitionSource.getDefinition(fullDefinitionName, InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    }
    return definition;
  }

  /**
   * The postfix (e.g. Index, Equity) is removed from the Bloomberg ticker when constructing the surface name, so the full name of a surface with
   * <ul>
   * <li> specificationName = OPENGAMMA
   * <li> target=BLOOMBERG_TICKER~DJX Index
   * <ul>
   * is OPENGAMMA_DJX_EQUITY_FUTURE_OPTION
   * {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceSpecification getSpecification(final VolatilitySurfaceSpecificationSource specificationSource, final ComputationTarget target, final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + EquitySecurityUtils.getTrimmedTarget(target.getUniqueId());
    final VolatilitySurfaceSpecification specification = specificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + InstrumentTypeProperties.EQUITY_FUTURE_OPTION);
    }
    return specification;
  }

}
