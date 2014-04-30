/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;

/**
 * Calculates the PV01 of a bond with information about the country added to the result properties.
 * @deprecated The parent class of this function is deprecated.
 */
@Deprecated
public class BondPV01CountryCurveFunction extends BondPV01Function {

  @Override
  protected ValueProperties.Builder getResultProperties() {
    return super.getResultProperties()
        .withAny(ValuePropertyNames.COUNTRY);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String riskFreeCurveConfig,
      final String creditCurveConfig, final ComputationTarget target) {
    return super.getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig, target)
        .with(ValuePropertyNames.COUNTRY, BondFunctionUtils.getCountryName(target));
  }
}
