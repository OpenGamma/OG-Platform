/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.model.option.definition.GapOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.GapOptionModel;
import com.opengamma.financial.security.option.GapPayoffStyle;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;

/**
 * 
 */
public class GapOptionModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<GapOptionDefinition, StandardOptionDataBundle> _model = new GapOptionModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<GapOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final OptionSecurity option) {
    final GapPayoffStyle payoff = (GapPayoffStyle) option.getPayoffStyle();
    return new GapOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL, payoff.getPayment());
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof GapPayoffStyle) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "GapOptionModel";
  }

}
