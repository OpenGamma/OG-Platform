/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDALegacyVanillaCDSDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ISDALegacyVanillaCDSDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.CLEAN_PRICE,
    ValueRequirementNames.DIRTY_PRICE
  };
  private final String _nIntegrationPoints;

  public ISDALegacyVanillaCDSDefaults(final String nIntegrationPoints) {
    super(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY, true);
    ArgumentChecker.notNull(nIntegrationPoints, "number of integration points");
    _nIntegrationPoints = nIntegrationPoints;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_N_INTEGRATION_POINTS);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_N_INTEGRATION_POINTS.equals(propertyName)) {
      return Collections.singleton(_nIntegrationPoints);
    }
    s_logger.warn("Did not have default value for property called {}", propertyName);
    return null;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.ISDA_LEGACY_CDS_PRICING;
  }

}
