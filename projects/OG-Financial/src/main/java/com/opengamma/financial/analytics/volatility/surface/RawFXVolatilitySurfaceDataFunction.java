/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Constructs volatility surface data objects for FX options if the target is an unordered currency pair.
 */
public class RawFXVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(RawFXVolatilitySurfaceDataFunction.class);

  /**
   * Default constructor
   */
  public RawFXVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.FOREX);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  /**
   * Tries the unordered currency pair both ways. If the target is UnorderedCurrencyPair~EURUSD, and the surface name is OPENGAMMA, will look for OPENGAMMA_EURUSD_FX_VANILLA_OPTION and
   * OPENGAMMA_USDEUR_FX_VANILLA_OPTION. {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceDefinition<?, ?> getDefinition(final VolatilitySurfaceDefinitionSource definitionSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String definitionName) {
    final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
    String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
    String fullDefinitionName = definitionName + "_" + name;
    VolatilitySurfaceDefinition<?, ?> definition = definitionSource.getDefinition(fullDefinitionName, InstrumentTypeProperties.FOREX, versionCorrection);
    if (definition == null) {
      name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
      fullDefinitionName = definitionName + "_" + name;
      definition = definitionSource.getDefinition(fullDefinitionName, InstrumentTypeProperties.FOREX);
      if (definition == null) {
        s_logger.error("Could not get volatility surface definition named " + fullDefinitionName + " for instrument type " + InstrumentTypeProperties.FOREX);
        return null;
      }
    }
    return definition;
  }

  /**
   * Tries the unordered currency pair both ways. If the target is UnorderedCurrencyPair~EURUSD, and the surface name is OPENGAMMA, will look for OPENGAMMA_EURUSD_FX_VANILLA_OPTION and
   * OPENGAMMA_USDEUR_FX_VANILLA_OPTION. {@inheritDoc}
   */
  @Override
  protected VolatilitySurfaceSpecification getSpecification(final VolatilitySurfaceSpecificationSource specificationSource, final VersionCorrection versionCorrection, final ComputationTarget target,
      final String specificationName) {
    final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(target.getUniqueId());
    String name = pair.getFirstCurrency().getCode() + pair.getSecondCurrency().getCode();
    String fullSpecificationName = specificationName + "_" + name;
    VolatilitySurfaceSpecification specification = specificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.FOREX, versionCorrection);
    if (specification == null) {
      name = pair.getSecondCurrency().getCode() + pair.getFirstCurrency().getCode();
      fullSpecificationName = specificationName + "_" + name;
      specification = specificationSource.getSpecification(fullSpecificationName, InstrumentTypeProperties.FOREX);
      if (specification == null) {
        s_logger.error("Could not get volatility surface specification named " + fullSpecificationName + " for instrument type " + InstrumentTypeProperties.FOREX);
        return null;
      }
    }
    return specification;
  }
}
