/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.VersionCorrection;

/**
 * Constructs volatility surface data objects for bond future options if the target is the currency of the option.
 */
public class RawBondFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawBondFutureOptionVolatilitySurfaceDataFunction.class);

  /**
   * Default constructor
   */
  public RawBondFutureOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.BOND_FUTURE_OPTION);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  protected VolatilitySurfaceDefinition<?, ?> getDefinition(final VolatilitySurfaceDefinitionSource definitionSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String definitionName) {
    final String fullDefinitionName = definitionName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceDefinition<?, ?> definition = definitionSource.getDefinition(fullDefinitionName, InstrumentTypeProperties.BOND_FUTURE_OPTION, versionCorrection);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + InstrumentTypeProperties.BOND_FUTURE_OPTION);
    }
    return definition;
  }

  @Override
  protected VolatilitySurfaceSpecification getSpecification(final VolatilitySurfaceSpecificationSource specificationSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String specificationName) {
    final String fullSpecificationName = specificationName + "_" + target.getUniqueId().getValue();
    final VolatilitySurfaceSpecification specification = specificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.BOND_FUTURE_OPTION, versionCorrection);
    if (specification == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + InstrumentTypeProperties.BOND_FUTURE_OPTION);
    }
    return specification;
  }

}
