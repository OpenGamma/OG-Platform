/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;
import com.opengamma.financial.security.option.OptionType;

/**
 * 
 *
 */
public class BlackScholesMertonModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _model = new BlackScholesMertonModel();

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof EquityOptionSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public String getShortName() {
    return "BlackScholesMertonModel";
  }

  @Override
  protected AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> getModel() {
    return _model;
  }

  @Override
  protected OptionDefinition getOptionDefinition(final EquityOptionSecurity option) {
    return option.getExerciseType().accept(
      new ExerciseTypeVisitor<OptionDefinition>() {
        @Override
        public OptionDefinition visitAmericanExerciseType(AmericanExerciseType exerciseType) {
          return new AmericanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
        }
  
        @Override
        public OptionDefinition visitAsianExerciseType(AsianExerciseType exerciseType) {
          throw new OpenGammaRuntimeException("Unsupported option type: Asian");
        }
  
        @Override
        public OptionDefinition visitBermudanExerciseType(BermudanExerciseType exerciseType) {
          throw new OpenGammaRuntimeException("Unsupported option type: Bermudan");
        }
  
        @Override
        public OptionDefinition visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
          return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
        }
      }
    );
  }
}
