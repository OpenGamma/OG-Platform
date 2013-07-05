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
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.FinancialSecurityTypes;
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
@Deprecated
public class BlackScholesMertonModelFunction extends StandardOptionDataAnalyticOptionModelFunction {
  private final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> _model = new BlackScholesMertonModel();

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_OPTION_SECURITY;
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
        public OptionDefinition visitAmericanExerciseType(final AmericanExerciseType exerciseType) {
          return new AmericanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
        }

        @Override
        public OptionDefinition visitAsianExerciseType(final AsianExerciseType exerciseType) {
          throw new OpenGammaRuntimeException("Unsupported option type: Asian");
        }

        @Override
        public OptionDefinition visitBermudanExerciseType(final BermudanExerciseType exerciseType) {
          throw new OpenGammaRuntimeException("Unsupported option type: Bermudan");
        }

        @Override
        public OptionDefinition visitEuropeanExerciseType(final EuropeanExerciseType exerciseType) {
          return new EuropeanVanillaOptionDefinition(option.getStrike(), option.getExpiry(), option.getOptionType() == OptionType.CALL);
        }
      }
    );
  }
}
