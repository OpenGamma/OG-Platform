/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.fourier.FourierPricer;
import com.opengamma.analytics.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverterDeprecated;
import com.opengamma.financial.analytics.ircurve.calcconfig.ConfigDBCurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.analytics.volatility.fittedresults.HestonFittedSurfaces;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.money.Currency;

/**
 */
public class InterestRateFutureOptionHestonPresentValueFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionHestonPresentValueFunction.class);
  private FixedIncomeConverterDataProvider _dataConverter;
  private ConfigDBCurveCalculationConfigSource _curveCalculationConfigSource;

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  private InterestRateFutureOptionTradeConverterDeprecated getConverter(final FunctionExecutionContext context) {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(context);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(context);
    return new InterestRateFutureOptionTradeConverterDeprecated(new InterestRateFutureOptionSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, securitySource,
        context.getComputationTargetResolver().getVersionCorrection()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    _dataConverter = new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
    _curveCalculationConfigSource = ConfigDBCurveCalculationConfigSource.init(context, this);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
    final InstrumentDefinition<InstrumentDerivative> irFutureOptionDefinition = (InstrumentDefinition<InstrumentDerivative>) getConverter(executionContext).convert(target.getTrade());
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String curveCalculationConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      throw new OpenGammaRuntimeException("Could not find curve calculation configuration named " + curveCalculationConfigName);
    }
    final String[] curveNames = curveCalculationConfig.getYieldCurveNames();
    final YieldCurveBundle curves = YieldCurveFunctionUtils.getYieldCurves(inputs, curveCalculationConfig);
    final InstrumentDerivative irFutureOption = _dataConverter.convert(target.getTrade().getSecurity(), irFutureOptionDefinition, now, curveNames, timeSeries);
    final double price = irFutureOption.accept(new MyDerivativeVisitor(target, inputs, curves));
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), createValueProperties()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName).with(ValuePropertyNames.SURFACE, surfaceName).with(ValuePropertyNames.SMILE_FITTING_METHOD, "Heston")
        .with(ValuePropertyNames.CALCULATION_METHOD, "Fourier").get());
    return Sets.newHashSet(new ComputedValue(valueSpecification, price));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.TRADE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof IRFutureOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final Trade trade = target.getTrade();
    final Currency currency = FinancialSecurityUtils.getCurrency(trade.getSecurity());
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = Iterables.getOnlyElement(surfaceNames) + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
    final Set<String> curveCalculationConfigNames = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigNames == null || curveCalculationConfigNames.size() != 1) {
      return null;
    }
    final String curveCalculationConfigName = curveCalculationConfigNames.iterator().next();
    final MultiCurveCalculationConfig curveCalculationConfig = _curveCalculationConfigSource.getConfig(curveCalculationConfigName);
    if (curveCalculationConfig == null) {
      s_logger.error("Could not find curve calculation configuration named " + curveCalculationConfigName);
      return null;
    }
    final ComputationTargetSpecification curveCalculationTarget = curveCalculationConfig.getTarget();
    if (!ComputationTargetSpecification.of(currency).equals(curveCalculationTarget)) {
      return null;
    }
    requirements.addAll(YieldCurveFunctionUtils.getCurveRequirements(curveCalculationConfig, _curveCalculationConfigSource));
    requirements.add(getSurfaceRequirement(target, surfaceName));
    final Set<ValueRequirement> timeSeriesRequirements = _dataConverter.getConversionTimeSeriesRequirements(trade.getSecurity(), getConverter(context).convert(trade));
    if (timeSeriesRequirements == null) {
      return null;
    }
    requirements.addAll(timeSeriesRequirements);
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties resultProperties = createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode())
        .withAny(ValuePropertyNames.SURFACE).withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).with(ValuePropertyNames.SMILE_FITTING_METHOD, "Heston")
        .with(ValuePropertyNames.CALCULATION_METHOD, "Fourier").get();
    final ValueSpecification resultSpecification = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), resultProperties);
    return Sets.newHashSet(resultSpecification);
  }

  private ValueRequirement getSurfaceRequirement(final ComputationTarget target, final String surfaceName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CURRENCY, currency.getCode()).with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.IR_FUTURE_OPTION).get();
    return new ValueRequirement(ValueRequirementNames.HESTON_SURFACES, ComputationTargetSpecification.of(currency), properties);
  }

  private class MyDerivativeVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {
    private final double _alpha = -0.5;
    private final double _tolerance = 0.001;
    private final InterestRateFutureSecurityDiscountingMethod _futurePricer = InterestRateFutureSecurityDiscountingMethod.getInstance();
    private final FourierPricer _fourierPricer = new FourierPricer(new RungeKuttaIntegrator1D());
    private final ComputationTarget _target;
    private final FunctionInputs _inputs;
    private final YieldCurveBundle _curves;

    public MyDerivativeVisitor(final ComputationTarget target, final FunctionInputs inputs, final YieldCurveBundle curves) {
      _target = target;
      _inputs = inputs;
      _curves = curves;
    }

    @Override
    public Double visitInterestRateFutureOptionPremiumSecurity(final InterestRateFutureOptionPremiumSecurity option) {
      final double t = option.getExpirationTime();
      final double k = option.getStrike();
      final boolean isCall = option.isCall();
      final InterestRateFutureSecurity irFuture = option.getUnderlyingFuture();
      final double f = 1 - _futurePricer.price(irFuture, _curves);
      final BlackFunctionData blackData = new BlackFunctionData(f, 1, 0);
      final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(k, t, isCall);
      final HestonCharacteristicExponent ce = getModelParameters(_target, _inputs, t, k);
      return _fourierPricer.price(blackData, vanillaOption, ce, _alpha, _tolerance, true);
    }

    @Override
    public Double visitInterestRateFutureOptionPremiumTransaction(final InterestRateFutureOptionPremiumTransaction option) {
      return visitInterestRateFutureOptionPremiumSecurity(option.getUnderlyingSecurity());
    }

    @Override
    public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option) {
      final double t = option.getExpirationTime();
      final double k = option.getStrike();
      final boolean isCall = option.isCall();
      final InterestRateFutureSecurity irFuture = option.getUnderlyingFuture();
      final double f = 1 - _futurePricer.price(irFuture, _curves);
      final BlackFunctionData blackData = new BlackFunctionData(f, 1, 1e-6);
      final EuropeanVanillaOption vanillaOption = new EuropeanVanillaOption(k, t, isCall);
      final HestonCharacteristicExponent ce = getModelParameters(_target, _inputs, t, k);
      return _fourierPricer.price(blackData, vanillaOption, ce, _alpha, _tolerance, true);
    }

    @Override
    public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option) {
      return visitInterestRateFutureOptionMarginSecurity(option.getUnderlyingSecurity());
    }

    private HestonCharacteristicExponent getModelParameters(final ComputationTarget target, final FunctionInputs inputs, final double t, final double k) {
      final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
      @SuppressWarnings("synthetic-access")
      final Object surfacesObject = inputs.getValue(ValueRequirementNames.HESTON_SURFACES);
      if (surfacesObject == null) {
        throw new OpenGammaRuntimeException("Could not get heston surface");
      }
      final HestonFittedSurfaces surfaces = (HestonFittedSurfaces) surfacesObject;
      if (!surfaces.getCurrency().equals(currency)) {
        throw new OpenGammaRuntimeException("Currency mismatch between heston curves and trade");
      }
      final InterpolatedDoublesSurface kappaSurface = surfaces.getKappaSurface();
      final InterpolatedDoublesSurface thetaSurface = surfaces.getThetaSurface();
      final InterpolatedDoublesSurface vol0Surface = surfaces.getVol0Surface();
      final InterpolatedDoublesSurface omegaSurface = surfaces.getOmegaSurface();
      final InterpolatedDoublesSurface rhoSurface = surfaces.getRhoSurface();
      return new HestonCharacteristicExponent(kappaSurface.getZValue(t, k), thetaSurface.getZValue(t, k), vol0Surface.getZValue(t, k), omegaSurface.getZValue(t, k), rhoSurface.getZValue(t,
          k));
    }
  }
}
