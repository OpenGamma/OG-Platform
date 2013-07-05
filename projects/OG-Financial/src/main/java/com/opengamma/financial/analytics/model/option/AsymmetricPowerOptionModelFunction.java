/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.AsymmetricPowerOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AsymmetricPowerOptionModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 *
 */
@Deprecated
public class AsymmetricPowerOptionModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> _model = new AsymmetricPowerOptionModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    final AsymmetricPoweredPayoffStyle payoff = (AsymmetricPoweredPayoffStyle) option.getPayoffStyle();
    return new AsymmetricPowerOptionDefinition(option.getStrike(), option.getExpiry(), payoff.getPower(), option.getOptionType() == OptionType.CALL);
    */
    throw new UnsupportedOperationException();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    //REVIEW yomi 09-06-2011 OptionSecurity is no more..
    /*
    return (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof AsymmetricPoweredPayoffStyle);
    */
    return true;
  }

  @Override
  public String getShortName() {
    return "AsymmetricPowerOptionModel";
  }

}
