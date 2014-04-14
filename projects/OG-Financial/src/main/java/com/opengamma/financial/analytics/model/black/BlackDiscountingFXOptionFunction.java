/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.FOREX;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.InterpolatedDataProperties.X_INTERPOLATOR_NAME;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.BLACK;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FXVanillaOptionConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyPairsSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Base function for all FX option pricing and risk functions that use a Black surface and curves constructed using the discounting method.
 */
public abstract class BlackDiscountingFXOptionFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public BlackDiscountingFXOptionFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Gets the currency pairs configuration called {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS} from a {@link CurrencyPairsSource}.
   * 
   * @param context The compilation context
   * @return The currency pairs
   */
  protected CurrencyPairs getCurrencyPairs(final FunctionCompilationContext context) {
    @SuppressWarnings("deprecation")
    final CurrencyPairsSource currencyPairsSource = OpenGammaCompilationContext.getCurrencyPairsSource(context);
    return currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final FXVanillaOptionConverter fxOptionConverter = new FXVanillaOptionConverter(getCurrencyPairs(context));
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().fxOptionVisitor(fxOptionConverter)
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
      return security instanceof FXOptionSecurity;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(PROPERTY_VOLATILITY_MODEL, BLACK).withAny(X_INTERPOLATOR_NAME)
          .withAny(LEFT_X_EXTRAPOLATOR_NAME).withAny(RIGHT_X_EXTRAPOLATOR_NAME).withAny(SURFACE).withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
        final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
        final CurrencyPairs baseQuotePairs = getCurrencyPairs(context);
        final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
        final String currency = getResultCurrency(target, baseQuotePair);
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
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> interpolatorNames = constraints.getValues(X_INTERPOLATOR_NAME);
      final Set<String> leftExtrapolatorNames = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
      final Set<String> rightExtrapolatorNames = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
      final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
      final Set<String> surface = constraints.getValues(SURFACE);
      final String interpolatorName = Iterables.getOnlyElement(interpolatorNames);
      final String leftExtrapolatorName = Iterables.getOnlyElement(leftExtrapolatorNames);
      final String rightExtrapolatorName = Iterables.getOnlyElement(rightExtrapolatorNames);
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      final ValueProperties surfaceProperties = ValueProperties.builder().with(SURFACE, surface).with(PROPERTY_SURFACE_INSTRUMENT_TYPE, FOREX).with(X_INTERPOLATOR_NAME, interpolatorName)
          .with(LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName).with(RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName).get();
      final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
      final ValueRequirement surfaceRequirement = new ValueRequirement(STANDARD_VOLATILITY_SURFACE_DATA, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currenciesTarget),
          surfaceProperties);
      requirements.add(surfaceRequirement);
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> surfaceNames = constraints.getValues(SURFACE);
      if (surfaceNames == null) {
        return false;
      }
      final Set<String> interpolatorNames = constraints.getValues(X_INTERPOLATOR_NAME);
      if (interpolatorNames == null || interpolatorNames.size() != 1) {
        return false;
      }
      final Set<String> leftExtrapolatorNames = constraints.getValues(LEFT_X_EXTRAPOLATOR_NAME);
      if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
        return false;
      }
      final Set<String> rightExtrapolatorNames = constraints.getValues(RIGHT_X_EXTRAPOLATOR_NAME);
      if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
        return false;
      }
      return super.requirementsSet(constraints);
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
    protected BlackForexSmileProvider getBlackSurface(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final FXMatrix fxMatrix) {
      final FXOptionSecurity security = (FXOptionSecurity) target.getTrade().getSecurity();
      final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
      final SmileDeltaTermStructureParametersStrikeInterpolation volatilitySurface = (SmileDeltaTermStructureParametersStrikeInterpolation) inputs.getValue(STANDARD_VOLATILITY_SURFACE_DATA);
      final Pair<Currency, Currency> currencyPair = Pairs.of(security.getPutCurrency(), security.getCallCurrency());
      final BlackForexSmileProvider blackData = new BlackForexSmileProvider(data, volatilitySurface, currencyPair);
      return blackData;
    }

    /**
     * Gets the currency code of the result.
     * 
     * @param target The computation target
     * @param baseQuotePair The base/quote pair for the currencies in the security
     * @return The result currency code.
     */
    protected String getResultCurrency(final ComputationTarget target, final CurrencyPair baseQuotePair) {
      final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
      if (security instanceof FXDigitalOptionSecurity) {
        return ((FXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
      } else if (security instanceof NonDeliverableFXDigitalOptionSecurity) {
        return ((NonDeliverableFXDigitalOptionSecurity) target.getSecurity()).getPaymentCurrency().getCode();
      }
      final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
      final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
      if (baseQuotePair.getBase().equals(putCurrency)) {
        return callCurrency.getCode();
      }
      return putCurrency.getCode();
    }
  }
}
