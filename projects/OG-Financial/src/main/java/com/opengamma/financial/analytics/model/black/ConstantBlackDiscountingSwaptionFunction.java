/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.CONSTANT_BLACK;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.InterestRateSwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;

/**
 * Base function for all swaption pricing and risk functions that use a constant Black surface and curves constructed using the discounting method.
 */
public abstract class ConstantBlackDiscountingSwaptionFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public ConstantBlackDiscountingSwaptionFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(securitySource, holidaySource, conventionSource, regionSource);
    final InterestRateSwapSecurityConverter irsConverter = new InterestRateSwapSecurityConverter(holidaySource, conventionSource);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(swapConverter, irsConverter);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().swaptionVisitor(swaptionConverter)
        .create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter();
    return new DefaultTradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use a Black surface and curves constructed using the discounting method.
   */
  protected abstract class BlackDiscountingCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected BlackDiscountingCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof SwaptionSecurity;
    }

    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(PROPERTY_VOLATILITY_MODEL, CONSTANT_BLACK).withAny(SURFACE)
          .withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final Security security = target.getTrade().getSecurity();
        final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
        properties.with(CURRENCY, currency);
        return Collections.singleton(properties);
      }
      return Collections.singleton(properties);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.add(new ValueRequirement(MarketDataRequirementNames.IMPLIED_VOLATILITY, target.toSpecification(), ValueProperties.builder().get()));
      return requirements;
    }

    /**
     * Gets the Black surface and curve data.
     * 
     * @param executionContext The execution context, not null
     * @param inputs The function inputs, not null
     * @param target The computation target, not null
     * @param fxMatrix The FX matrix, not null
     * @return The Black surface and curve data
     */
    protected BlackSwaptionFlatProvider getSwaptionBlackSurface(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final FXMatrix fxMatrix) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
      final SwaptionSecurity security = (SwaptionSecurity) target.getTrade().getSecurity();
      final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
      final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
      final double volatility = (Double) inputs.getValue(MarketDataRequirementNames.IMPLIED_VOLATILITY);
      final BlackFlatSwaptionParameters parameters = new BlackFlatSwaptionParameters(ConstantDoublesSurface.from(volatility), SwaptionUtils.getSwapGenerator(security, definition,
          securitySource));
      final BlackSwaptionFlatProvider blackData = new BlackSwaptionFlatProvider(data, parameters);
      return blackData;
    }
  }
}
