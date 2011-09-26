/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;

/**
 * Fudge message builder for {@link SecondaryCurveRiskFactorsConfigurationProvider}.
 */
@FudgeBuilderFor(SecondaryCurveRiskFactorsConfigurationProvider.class)
public class SecondaryCurveRiskFactorsConfigurationProviderBuilder extends DefaultRiskFactorsConfigurationProviderBuilder {

  @Override
  public SecondaryCurveRiskFactorsConfigurationProvider buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return new SecondaryCurveRiskFactorsConfigurationProvider(getCurrencyOverride(deserializer, msg));
  }

}
