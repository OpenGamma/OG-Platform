/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.CappedPowerOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.CappedPowerOptionModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * 
 */
public class CappedPowerOptionModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> _model = new CappedPowerOptionModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    final CappedPoweredPayoffStyle payoff = (CappedPoweredPayoffStyle) option.getPayoffStyle();
    return new CappedPowerOptionDefinition(option.getStrike(), option.getExpiry(), payoff.getPower(), payoff.getCap(), option.getOptionType() == OptionType.CALL);
    */
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    if (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof CappedPoweredPayoffStyle) {
      return true;
    }
    */
    if (target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "CappedPowerOptionModel";
  }

}
