/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.PoweredOptionModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * 
 */
public class PoweredOptionModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> _model = new PoweredOptionModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    final PoweredPayoffStyle payoff = (PoweredPayoffStyle) option.getPayoffStyle();
    return new PoweredOptionDefinition(option.getStrike(), option.getExpiry(), payoff.getPower(), option.getOptionType() == OptionType.CALL);
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
    if (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof PoweredPayoffStyle) {
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
    return null;
  }

}
