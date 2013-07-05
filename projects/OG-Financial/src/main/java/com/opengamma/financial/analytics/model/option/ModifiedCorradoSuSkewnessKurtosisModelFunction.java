/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.ModifiedCorradoSuSkewnessKurtosisModel;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;

/**
 *
 *
 */
@Deprecated
public class ModifiedCorradoSuSkewnessKurtosisModelFunction extends SkewKurtosisDataOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> _model = new ModifiedCorradoSuSkewnessKurtosisModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

  @Override
  public String getShortName() {
    return "ModifiedCorradoSuSkewnessKurtosisModel";
  }
}
