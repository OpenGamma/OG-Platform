/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.model.option.definition.CappedPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.CappedPowerOptionModel;
import com.opengamma.financial.security.option.CappedPoweredPayoffStyle;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;

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
  protected OptionDefinition getOptionDefinition(final OptionSecurity option) {
    final CappedPoweredPayoffStyle payoff = (CappedPoweredPayoffStyle) option.getPayoffStyle();
    return new CappedPowerOptionDefinition(option.getStrike(), option.getExpiry(), payoff.getPower(), payoff.getCap(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof CappedPoweredPayoffStyle) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "CappedPowerOptionModel";
  }

}
