/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.ModifiedCorradoSuSkewnessKurtosisModel;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.option.OptionType;

/**
 * 
 *
 */
public class ModifiedCorradoSuSkewnessKurtosisModelFunction extends SkewKurtosisDataOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> _model = new ModifiedCorradoSuSkewnessKurtosisModel();

  @SuppressWarnings("unchecked")
  @Override
  protected AnalyticOptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final OptionSecurity option) {
    return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof OptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "ModifiedCorradoSuSkewnessKurtosisModel";
  }
}
