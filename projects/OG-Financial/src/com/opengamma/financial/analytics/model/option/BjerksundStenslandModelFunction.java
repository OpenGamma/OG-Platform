/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;

/**
 * 
 *
 */
public class BjerksundStenslandModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> _model = new BjerksundStenslandModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected AmericanVanillaOptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    return new AmericanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof EquityOptionSecurity)) {
      return false;
    }
    final EquityOptionSecurity optionSecurity = (EquityOptionSecurity) target.getSecurity();
    //REVIEW yomi 03-06-2011 Elaine needs to confirm what this test should be
    /*
    if ((optionSecurity.getExerciseType() instanceof AmericanExerciseType) && (optionSecurity.getPayoffStyle() instanceof VanillaPayoffStyle)) {
      return true;
    }
    */
    if (optionSecurity.getExerciseType() instanceof AmericanExerciseType) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "BjerksundStenslandModel";
  }

}
