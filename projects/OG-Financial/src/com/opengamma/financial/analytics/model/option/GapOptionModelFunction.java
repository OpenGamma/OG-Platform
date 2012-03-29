/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.GapOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.GapOptionModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.EquityOptionSecurity;

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
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    final GapPayoffStyle payoff = (GapPayoffStyle) option.getPayoffStyle();
    return new GapOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL, payoff.getPayment());
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
    if (target.getSecurity() instanceof OptionSecurity && ((OptionSecurity) target.getSecurity()).getPayoffStyle() instanceof GapPayoffStyle) {
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
    return "GapOptionModel";
  }

}
