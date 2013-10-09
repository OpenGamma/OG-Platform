/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValueRequirementNames.G2PP_PARAMETERS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_G2PP_PARAMETERS;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.parameters.G2ppParameters;
import com.opengamma.financial.analytics.parameters.HullWhiteOneFactorParameters;
import com.opengamma.financial.view.ConfigDocumentWatchSetProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Function that supplies G2++ parameters.
 */
public class G2ppParametersFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(G2ppParametersFunction.class);
  /** The configuration name */
  private final String _name;
  /** The currency for which these parameters are valid */
  private final Currency _currency;

  /**
   * @param name The name of the G2++ parameter set, not null
   * @param currency The currency for which the parameters are valid, not null
   */
  public G2ppParametersFunction(final String name, final String currency) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currency, "currency");
    _name = name;
    _currency = Currency.of(currency);
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    ConfigDocumentWatchSetProvider.reinitOnChanges(context, this, HullWhiteOneFactorParameters.class);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_G2PP_PARAMETERS, _name)
        .get();
    final ValueSpecification result = new ValueSpecification(G2PP_PARAMETERS, ComputationTargetSpecification.of(_currency), properties);
    final Set<ValueRequirement> requirements = new HashSet<>();
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final Collection<ConfigItem<G2ppParameters>> configs = configSource.get(G2ppParameters.class, _name, VersionCorrection.LATEST);
    if (configs == null) {
      throw new OpenGammaRuntimeException("G2ppParameter configuration called " + _name + " was null");
    }
    final G2ppParameters parameters = configs.iterator().next().getValue();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, parameters.getFirstMeanReversionId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, parameters.getSecondMeanReversionId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, parameters.getFirstInitialVolatilityId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, parameters.getSecondInitialVolatilityId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, parameters.getCorrelationId()));
    final Map<Tenor, Pair<ExternalId, ExternalId>> volatilityTermStructure = parameters.getVolatilityTermStructure();
    for (final Map.Entry<Tenor, Pair<ExternalId, ExternalId>> entry : volatilityTermStructure.entrySet()) {
      final ExternalScheme firstScheme = entry.getValue().getFirst().getScheme();
      final ExternalScheme secondScheme = entry.getValue().getSecond().getScheme();
      final String firstId = entry.getValue().getFirst().getValue();
      final String secondId = entry.getValue().getSecond().getValue();
      final ExternalId firstTenorAppendedId = ExternalId.of(firstScheme, createId(entry.getKey(), firstId));
      final ExternalId secondTenorAppendedId = ExternalId.of(secondScheme, createId(entry.getKey(), secondId));
      requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, firstTenorAppendedId));
      requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, secondTenorAppendedId));
    }
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    return new AbstractInvokingCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final Object firstMeanReversionObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.PRIMITIVE, parameters.getFirstMeanReversionId()));
        if (firstMeanReversionObject == null) {
          throw new OpenGammaRuntimeException("Could not get first mean reversion value");
        }
        final Object secondMeanReversionObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.PRIMITIVE, parameters.getSecondMeanReversionId()));
        if (secondMeanReversionObject == null) {
          throw new OpenGammaRuntimeException("Could not get second mean reversion value");
        }
        final Object firstInitialVolatilityObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.PRIMITIVE, parameters.getFirstInitialVolatilityId()));
        if (firstInitialVolatilityObject == null) {
          throw new OpenGammaRuntimeException("Could not get first initial volatility value");
        }
        final Object secondInitialVolatilityObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.PRIMITIVE, parameters.getSecondInitialVolatilityId()));
        if (secondInitialVolatilityObject == null) {
          throw new OpenGammaRuntimeException("Could not get second initial volatility value");
        }
        final Object correlationObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
            ComputationTargetType.PRIMITIVE, parameters.getCorrelationId()));
        if (correlationObject == null) {
          throw new OpenGammaRuntimeException("Could not get correlation value");
        }
        final Double firstMeanReversion = (Double) firstMeanReversionObject;
        final Double secondMeanReversion = (Double) secondMeanReversionObject;
        final Double firstInitialVolatility = (Double) firstInitialVolatilityObject;
        final Double secondInitialVolatility = (Double) secondInitialVolatilityObject;
        final Double correlation = (Double) correlationObject;
        final DoubleArrayList firstVolatility = new DoubleArrayList();
        firstVolatility.add(firstInitialVolatility);
        final DoubleArrayList secondVolatility = new DoubleArrayList();
        secondVolatility.add(secondInitialVolatility);
        final DoubleArrayList volatilityTime = new DoubleArrayList();
        for (final Map.Entry<Tenor, Pair<ExternalId, ExternalId>> entry : volatilityTermStructure.entrySet()) {
          final ExternalScheme firstScheme = entry.getValue().getFirst().getScheme();
          final ExternalScheme secondScheme = entry.getValue().getSecond().getScheme();
          final String firstId = entry.getValue().getFirst().getValue();
          final String secondId = entry.getValue().getSecond().getValue();
          final ExternalId firstTenorAppendedId = ExternalId.of(firstScheme, createId(entry.getKey(), firstId));
          final ExternalId secondTenorAppendedId = ExternalId.of(secondScheme, createId(entry.getKey(), secondId));
          final Object firstVolatilityObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
              ComputationTargetType.PRIMITIVE, firstTenorAppendedId));
          final Object secondVolatilityObject = inputs.getValue(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE,
              ComputationTargetType.PRIMITIVE, secondTenorAppendedId));
          if (firstVolatilityObject == null) {
            s_logger.error("Could not get value for " + firstTenorAppendedId);
            continue;
          }
          if (secondVolatilityObject == null) {
            s_logger.error("Could not get value for " + secondTenorAppendedId);
          } else {
            final double t = TimeCalculator.getTimeBetween(now, now.plus(entry.getKey().getPeriod()));
            firstVolatility.add((Double) firstVolatilityObject);
            secondVolatility.add((Double) secondVolatilityObject);
            volatilityTime.add(t);
          }
        }
        final G2ppPiecewiseConstantParameters g2ppParameters = new G2ppPiecewiseConstantParameters(new double[] {firstMeanReversion, secondMeanReversion},
            new double[][] {firstVolatility.toDoubleArray(), secondVolatility.toDoubleArray()}, volatilityTime.toDoubleArray(), correlation);
        return Collections.singleton(new ComputedValue(result, g2ppParameters));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.CURRENCY;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return _currency.equals(target.getValue());
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return Collections.singleton(result);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> names = constraints.getValues(PROPERTY_G2PP_PARAMETERS);
        if (names == null || names.size() != 1) {
          return null;
        }
        return requirements;
      }

      @Override
      public boolean canHandleMissingRequirements() {
        return true;
      }

      @Override
      public boolean canHandleMissingInputs() {
        return true;
      }

    };
  }

  /**
   * Appends the tenor to an id to create the market data identifier.
   * @param tenor The tenor
   * @param id The id
   * @return The market data id
   */
  static String createId(final Tenor tenor, final String id) {
    final StringBuilder newId = new StringBuilder(id);
    newId.append("_");
    newId.append(tenor.getPeriod().toString());
    return newId.toString();
  }

}
