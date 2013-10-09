/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabr;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.CUBE;
import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.SABR_SURFACES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.SABR;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwaptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.model.swaption.SwaptionUtils;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * Base function for all pricing and risk functions that use SABR parameter surfaces
 * and curves constructed using the discounting method.
 */
public abstract class SABRDiscountingFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public SABRDiscountingFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected TradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource);
    final SwaptionSecurityConverter swaptionConverter = new SwaptionSecurityConverter(securitySource, swapConverter);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .swaptionVisitor(swaptionConverter)
        .create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter(securitySource, holidaySource, conventionSource, conventionBundleSource,
        regionSource);
    return new TradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use SABR parameter surfaces
   * and curves constructed using the discounting method.
   */
  protected abstract class SABRDiscountingCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected SABRDiscountingCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof SwaptionSecurity;
    }

    @Override
    protected ValueProperties.Builder getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .with(PROPERTY_VOLATILITY_MODEL, SABR)
          .withAny(CUBE)
          .with(CALCULATION_METHOD, getCalculationMethod())
          .withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final Security security = target.getTrade().getSecurity();
        final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
        properties.with(CURRENCY, currency);
        return properties;
      }
      return properties;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueProperties constraints = desiredValue.getConstraints();
      final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
      final Set<String> cube = constraints.getValues(CUBE);
      final ValueProperties properties = ValueProperties.builder()
          .with(CUBE, cube)
          .with(CURRENCY, currency.getCode())
          .with(PROPERTY_VOLATILITY_MODEL, SABR)
          .get();
      final ValueRequirement surfacesRequirement = new ValueRequirement(ValueRequirementNames.SABR_SURFACES,
          ComputationTargetSpecification.of(currency), properties);
      requirements.add(surfacesRequirement);
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> cubeNames = constraints.getValues(CUBE);
      if (cubeNames == null) {
        return false;
      }
      return super.requirementsSet(constraints);
    }

    /**
     * Gets the calculation method.
     * @return The calculation method
     */
    protected abstract String getCalculationMethod();

    protected SABRSwaptionProvider getSABRSurfaces(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final FXMatrix fxMatrix, final DayCount dayCount) {
      final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
      final SwaptionSecurity security = (SwaptionSecurity) target.getTrade().getSecurity();
      final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
      final SABRFittedSurfaces surfaces = (SABRFittedSurfaces) inputs.getValue(SABR_SURFACES);
      final InterpolatedDoublesSurface alphaSurface = surfaces.getAlphaSurface();
      final InterpolatedDoublesSurface betaSurface = surfaces.getBetaSurface();
      final InterpolatedDoublesSurface nuSurface = surfaces.getNuSurface();
      final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
      final SABRInterestRateParameters modelParameters = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, dayCount,
          VolatilityFunctionFactory.HAGAN_FORMULA);
      final MulticurveProviderDiscount curves = getMergedProviders(inputs, fxMatrix);
      final GeneratorInstrument<GeneratorAttributeIR> generatorSwap = SwaptionUtils.getSwapGenerator(security, definition, securitySource);
      if (!(generatorSwap instanceof GeneratorSwapFixedIbor)) {
        throw new OpenGammaRuntimeException("Cannot handle swap generators of type " + generatorSwap.getClass());
      }
      return new SABRSwaptionProviderDiscount(curves, modelParameters, (GeneratorSwapFixedIbor) generatorSwap);
    }

  }
}
