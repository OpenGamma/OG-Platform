/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

import static com.opengamma.engine.value.ValueRequirementNames.PRESENT_VALUE;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_MU;
import static com.opengamma.financial.analytics.model.sabr.SABRPropertyValues.PROPERTY_STRIKE_CUTOFF;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionRightExtrapolationCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of instruments using SABR parameter surfaces and
 * curves constructed using the discounting method where
 */
public class RightExtrapolationSABRDiscountingPVFunction extends RightExtrapolationSABRDiscountingFunction {

  /**
   * Sets the value requirement to {@link ValueRequirementNames#PRESENT_VALUE}
   */
  public RightExtrapolationSABRDiscountingPVFunction() {
    super(PRESENT_VALUE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new RightExtrapolationSABRDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final DayCount dayCount = DayCounts.ACT_360; //TODO
        final SABRSwaptionProvider sabrData = getSABRSurfaces(executionContext, inputs, target, fxMatrix, dayCount);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final double strikeCutoff = Double.parseDouble(desiredValue.getConstraint(PROPERTY_STRIKE_CUTOFF));
        final double mu = Double.parseDouble(desiredValue.getConstraint(PROPERTY_MU));
        final InstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyAmount> calculator =
            new PresentValueSABRSwaptionRightExtrapolationCalculator(strikeCutoff, mu);
        final MultipleCurrencyAmount mca = derivative.accept(calculator, sabrData);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
        final ValueSpecification spec = new ValueSpecification(PRESENT_VALUE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, mca.getAmount(currency)));
      }

    };
  }

}
