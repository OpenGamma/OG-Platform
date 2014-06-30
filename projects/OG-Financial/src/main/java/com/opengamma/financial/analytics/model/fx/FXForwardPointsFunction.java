/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fx;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.FORWARD_CURVE_NAME;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_DEFINITION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_MARKET_DATA;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_SPECIFICATION;
import static com.opengamma.engine.value.ValueRequirementNames.FX_MATRIX;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues.FORWARD_POINTS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.FXForwardSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.NonDeliverableFXForwardSecurityConverter;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.exposure.ConfigDBInstrumentExposuresProvider;
import com.opengamma.financial.analytics.curve.exposure.InstrumentExposuresProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public abstract class FXForwardPointsFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardPointsFunction.class);
  /** The value requirements */
  private final String[] _valueRequirements;

  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  private InstrumentExposuresProvider _instrumentExposuresProvider;

  /**
   * @param valueRequirements The value requirement names, not null
   */
  public FXForwardPointsFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _instrumentExposuresProvider = ConfigDBInstrumentExposuresProvider.init(context, this);
  }

  /**
   * Constructs an object capable of converting from {@link ComputationTarget} to {@link InstrumentDefinition}.
   * 
   * @param context The compilation context, not null
   * @return The converter
   */
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final FXForwardSecurityConverter fxForwardSecurityConverter = new FXForwardSecurityConverter();
    final NonDeliverableFXForwardSecurityConverter nonDeliverableFXForwardSecurityConverter = new NonDeliverableFXForwardSecurityConverter();
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .fxForwardVisitor(fxForwardSecurityConverter).nonDeliverableFxForwardVisitor(nonDeliverableFXForwardSecurityConverter).create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter();
    return new DefaultTradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Constructs an object capable of converting from {@link InstrumentDefinition} to {@link InstrumentDerivative}.
   * 
   * @param context The compilation context, not null
   * @return The converter
   */
  protected FixedIncomeConverterDataProvider getDefinitionToDerivativeConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    return new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver);
  }

  /**
   * Base compiled function for all FX forward points pricing and risk functions.
   */
  public abstract class FXForwardPointsCompiledFunction extends AbstractInvokingCompiledFunction {
    /** Converts targets to definitions */
    private final DefaultTradeConverter _tradeToDefinitionConverter;
    /** Converts definitions to derivatives */
    private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;
    /** Indicates whether the results set {@link ValuePropertyNames#CURRENCY} */
    private final boolean _withCurrency;

    /**
     * @param tradeToDefinitionConverter Converts trades to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if this function sets {@link ValuePropertyNames#CURRENCY}
     */
    protected FXForwardPointsCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      ArgumentChecker.notNull(tradeToDefinitionConverter, "target to definition converter");
      ArgumentChecker.notNull(definitionToDerivativeConverter, "definition to derivative converter");
      _tradeToDefinitionConverter = tradeToDefinitionConverter;
      _definitionToDerivativeConverter = definitionToDerivativeConverter;
      _withCurrency = withCurrency;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
        throws AsynchronousExecution {
      final Clock snapshotClock = executionContext.getValuationClock();
      final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
      final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
      final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
      final Forex forex = getForex(target, now, timeSeries, definition);
      final FXMatrix fxMatrix = new FXMatrix();
      final CurrencyPairs pairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
      final Currency currency1 = forex.getCurrency1();
      final Currency currency2 = forex.getCurrency2();
      if (pairs.getCurrencyPair(currency1, currency2).getBase().equals(currency1)) {
        final double spotRate = (Double) inputs.getValue(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(currency2, currency1))));
        fxMatrix.addCurrency(currency1, currency2, spotRate);
      } else {
        final double spotRate = (Double) inputs.getValue(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(currency2, currency1))));
        fxMatrix.addCurrency(currency2, currency1, 1 / spotRate);
      }
      return getValues(inputs, target, desiredValues, forex, fxMatrix, now);
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.TRADE;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties properties = getResultProperties(target).get();
      final Set<ValueSpecification> results = new HashSet<>();
      for (final String valueRequirement : _valueRequirements) {
        results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
      }
      return results;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposureConfigs == null) {
        return null;
      }
      final Set<String> fxForwardCurveNames = constraints.getValues(FORWARD_CURVE_NAME);
      if (fxForwardCurveNames == null || fxForwardCurveNames.size() != 1) {
        return null;
      }
      try {
        final FinancialSecurity security = (FinancialSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
        final Set<ValueRequirement> requirements = new HashSet<>();
        for (final String curveExposureConfig : curveExposureConfigs) {
          final Set<String> curveConstructionConfigurationNames = _instrumentExposuresProvider.getCurveConstructionConfigurationsForConfig(curveExposureConfig, target.getTrade());
          for (final String curveConstructionConfigurationName : curveConstructionConfigurationNames) {
            final ValueProperties properties = ValueProperties.with(CURVE_CONSTRUCTION_CONFIG, curveConstructionConfigurationName).get();
            requirements.add(new ValueRequirement(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties));
            requirements.add(new ValueRequirement(JACOBIAN_BUNDLE, ComputationTargetSpecification.NULL, properties));
            final CurveConstructionConfiguration curveConstructionConfiguration = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(curveConstructionConfigurationName);
            final String[] curveNames = CurveUtils.getCurveNamesForConstructionConfiguration(curveConstructionConfiguration);
            for (final String curveName : curveNames) {
              final ValueProperties curveProperties = ValueProperties.builder().with(CURVE, curveName).get();
              requirements.add(new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties));
              requirements.add(new ValueRequirement(FX_MATRIX, ComputationTargetSpecification.NULL, properties));
            }
          }
        }
        final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, securitySource);
        if (currencies.size() > 1) {
          final Iterator<Currency> iter = currencies.iterator();
          final Currency initialCurrency = iter.next();
          while (iter.hasNext()) {
            requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(iter.next(), initialCurrency))));
          }
        }
        final InstrumentDefinition<?> definition = getDefinitionFromTarget(target);
        final Set<ValueRequirement> timeSeriesRequirements = getConversionTimeSeriesRequirements(context, target, definition);
        if (timeSeriesRequirements == null) {
          return null;
        }
        requirements.addAll(timeSeriesRequirements);
        final String fxForwardCurveName = Iterables.getOnlyElement(fxForwardCurveNames);
        final ValueProperties fxForwardCurveProperties = ValueProperties.builder().with(CURVE, fxForwardCurveName).get();
        final ValueRequirement fxForwardCurveDefinition = new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, fxForwardCurveProperties);
        final ValueRequirement fxForwardCurveSpecification = new ValueRequirement(CURVE_SPECIFICATION, ComputationTargetSpecification.NULL, fxForwardCurveProperties);
        final ValueRequirement fxForwardCurveDataRequirement = new ValueRequirement(CURVE_MARKET_DATA, ComputationTargetSpecification.NULL, fxForwardCurveProperties);
        final ValueRequirement currencyPairsRequirement = new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, ValueProperties.none());
        requirements.add(fxForwardCurveDataRequirement);
        requirements.add(fxForwardCurveDefinition);
        requirements.add(fxForwardCurveSpecification);
        requirements.add(currencyPairsRequirement);
        return requirements;
      } catch (final Exception e) {
        s_logger.error(e.getMessage());
        return null;
      }
    }

    protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().withAny(CURVE_EXPOSURES).withAny(FORWARD_CURVE_NAME).with(PROPERTY_CURVE_TYPE, FORWARD_POINTS);
      if (_withCurrency) {
        properties.with(CURRENCY, ((FinancialSecurity) target.getTrade().getSecurity()).accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode());
      }
      return properties;
    }

    /**
     * Gets an {@link InstrumentDefinition} given a target.
     * 
     * @param target The target, not null
     * @return An instrument definition
     */
    protected InstrumentDefinition<?> getDefinitionFromTarget(final ComputationTarget target) {
      return _tradeToDefinitionConverter.convert(target.getTrade());
    }

    /**
     * Gets a conversion time-series for an instrument definition. If no time-series are required, returns an empty set.
     * 
     * @param context The compilation context, not null
     * @param target The target, not null
     * @param definition The definition, not null
     * @return A set of time-series requirements
     */
    protected Set<ValueRequirement> getConversionTimeSeriesRequirements(final FunctionCompilationContext context, final ComputationTarget target, final InstrumentDefinition<?> definition) {
      return _definitionToDerivativeConverter.getConversionTimeSeriesRequirements(target.getTrade().getSecurity(), definition);
    }

    /**
     * Gets an {@link InstrumentDerivative}.
     * 
     * @param target The target, not null
     * @param now The valuation time, not null
     * @param timeSeries The conversion time series bundle, not null but may be empty
     * @param definition The definition, not null
     * @return The instrument derivative
     */
    protected Forex getForex(final ComputationTarget target, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries, final InstrumentDefinition<?> definition) {
      return (Forex) _definitionToDerivativeConverter.convert(target.getTrade().getSecurity(), definition, now, timeSeries);
    }

    /**
     * Calculates the result.
     * 
     * @param inputs The inputs, not null
     * @param target The target, not null
     * @param desiredValues The desired values for this function, not null
     * @param forex The forex trade, not null
     * @param fxMatrix The FX matrix, not null
     * @param now The valuation time, not null
     * @return The results
     */
    protected abstract Set<ComputedValue> getValues(FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues, Forex forex, FXMatrix fxMatrix, ZonedDateTime now);

    protected MulticurveProviderDiscount getMergedProviders(final FunctionInputs inputs, final FXMatrix matrix) {
      final Collection<MulticurveProviderDiscount> providers = new HashSet<>();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (CURVE_BUNDLE.equals(valueName)) {
          providers.add((MulticurveProviderDiscount) input.getValue());
        }
      }
      final MulticurveProviderDiscount result = ProviderUtils.mergeDiscountingProviders(providers);
      return ProviderUtils.mergeDiscountingProviders(result, matrix);
    }

    protected CurveBuildingBlockBundle getMergedCurveBuildingBlocks(final FunctionInputs inputs) {
      final CurveBuildingBlockBundle result = new CurveBuildingBlockBundle();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (valueName.equals(JACOBIAN_BUNDLE)) {
          result.addAll((CurveBuildingBlockBundle) input.getValue());
        }
      }
      return result;
    }

    protected DoublesCurve getForwardPoints(final FunctionInputs inputs, final String fxForwardCurveName, final ZonedDateTime now) {
      final ValueProperties curveProperties = ValueProperties.with(CURVE, fxForwardCurveName).get();
      final ValueRequirement definitionRequirement = new ValueRequirement(CURVE_DEFINITION, ComputationTargetSpecification.NULL, curveProperties);
      final CurveDefinition definition = (CurveDefinition) inputs.getValue(definitionRequirement);
      if (definition == null) {
        throw new OpenGammaRuntimeException("Could not get definition for " + fxForwardCurveName);
      }
      final String interpolatorName, leftExtrapolatorName, rightExtrapolatorName;
      if (definition instanceof InterpolatedCurveDefinition) {
        final InterpolatedCurveDefinition interpolatedDefinition = (InterpolatedCurveDefinition) definition;
        interpolatorName = interpolatedDefinition.getInterpolatorName();
        if (interpolatedDefinition.getLeftExtrapolatorName() != null) {
          leftExtrapolatorName = interpolatedDefinition.getLeftExtrapolatorName();
          rightExtrapolatorName = interpolatedDefinition.getRightExtrapolatorName();
        } else {
          leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
          rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
        }
      } else {
        interpolatorName = Interpolator1DFactory.LINEAR;
        leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
        rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
      }
      final CurveSpecification specification = (CurveSpecification) inputs.getValue(CURVE_SPECIFICATION);
      final SnapshotDataBundle data = (SnapshotDataBundle) inputs.getValue(CURVE_MARKET_DATA);
      final DoubleArrayList tList = new DoubleArrayList();
      final DoubleArrayList fxList = new DoubleArrayList();
      for (final CurveNodeWithIdentifier nodeWithId : specification.getNodes()) {
        final CurveNode node = nodeWithId.getCurveNode();
        if (!(node instanceof FXForwardNode)) {
          throw new OpenGammaRuntimeException("Unexpected node " + nodeWithId + " found");
        }
        final Double fxForward = data.getDataPoint(nodeWithId.getIdentifier());
        if (fxForward == null) {
          throw new OpenGammaRuntimeException("Could not get FX forward rate for " + node);
        }
        final Tenor tenor = node.getResolvedMaturity();
        tList.add(DateUtils.getDifferenceInYears(now, now.plus(tenor.getPeriod())));
        fxList.add(fxForward);
      }
      final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
      return InterpolatedDoublesCurve.from(tList.toDoubleArray(), fxList.toDoubleArray(), interpolator);
    }
  }
}
