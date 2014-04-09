/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURRENCY_PAIRS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveNodeCurrencyVisitor;
import com.opengamma.financial.analytics.curve.CurveUtils;
import com.opengamma.financial.analytics.curve.credit.ConfigDBCurveDefinitionSource;
import com.opengamma.financial.analytics.curve.credit.CurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * Function that returns a {@link FXMatrix} for a curve construction configuration.
 */
public class FXMatrixFunction extends AbstractFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FXMatrixFunction.class);
  /** The configuration name */
  private final String _configurationName;
  /** A curve construction configuration source */
  private CurveConstructionConfigurationSource _curveConstructionConfigurationSource;
  /** A curve definition source */
  private CurveDefinitionSource _curveDefinitionSource;

  /**
   * @param configurationName The configuration name, not null
   */
  public FXMatrixFunction(final String configurationName) {
    ArgumentChecker.notNull(configurationName, "configuration name");
    _configurationName = configurationName;
  }

  /**
   * Gets the curve configuration name.
   *
   * @return The curve configuration names
   */
  public String getConfigurationName() {
    return _configurationName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _curveConstructionConfigurationSource = ConfigDBCurveConstructionConfigurationSource.init(context, this);
    _curveDefinitionSource = ConfigDBCurveDefinitionSource.init(context, this);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final ZonedDateTime atZDT = ZonedDateTime.ofInstant(atInstant, ZoneOffset.UTC);
    //TODO work out a way to use dependency graph to get curve information for this config
    final CurveConstructionConfiguration curveConstructionConfiguration = _curveConstructionConfigurationSource.getCurveConstructionConfiguration(_configurationName);
    if (curveConstructionConfiguration == null) {
      throw new OpenGammaRuntimeException("Could not get curve construction configuration called " + _configurationName);
    }
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    try {
      final CurveNodeVisitor<Set<Currency>> visitor = new CurveNodeCurrencyVisitor(conventionSource, securitySource, configSource);
      final Set<Currency> currencies = CurveUtils.getCurrencies(curveConstructionConfiguration, _curveDefinitionSource, _curveConstructionConfigurationSource, visitor);
      final ValueProperties properties = createValueProperties().with(CURVE_CONSTRUCTION_CONFIG, _configurationName).get();
      final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_MATRIX, ComputationTargetSpecification.NULL, properties);
      return new MyCompiledFunction(atZDT.with(LocalTime.MIDNIGHT), atZDT.plusDays(1).with(LocalTime.MIDNIGHT).minusNanos(1000000), spec, currencies);
    } catch (final Throwable e) {
      s_logger.error("{}: problem in CurveConstructionConfiguration called {}", e.getMessage(), _configurationName);
      s_logger.error("Full stack trace", e);
      throw new OpenGammaRuntimeException(e.getMessage() + ": problem in CurveConstructionConfiguration called " + _configurationName);
    }
  }

  /**
   * Function that creates an {@link FXMatrix}
   */
  protected class MyCompiledFunction extends AbstractInvokingCompiledFunction {
    /** The result specification */
    private final ValueSpecification _spec;
    /** The set of relevant currencies */
    private final Set<Currency> _currencies;

    /**
     * @param earliestInvocation The earliest time that this function is valid, not null
     * @param latestInvocation The latest time that this function is valid, not null
     * @param spec The result specification for the FX matrix, not null
     * @param currencies The currencies contained in the matrix, not null
     */
    public MyCompiledFunction(final ZonedDateTime earliestInvocation, final ZonedDateTime latestInvocation, final ValueSpecification spec, final Set<Currency> currencies) {
      super(earliestInvocation, latestInvocation);
      ArgumentChecker.notNull(spec, "specification");
      ArgumentChecker.notNull(currencies, "currencies");
      _spec = spec;
      _currencies = currencies;
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      if (inputs.getAllValues().size() == 0) {
        return Collections.singleton(new ComputedValue(_spec, new FXMatrix()));
      }
      final FXMatrix matrix = new FXMatrix();
      final Iterator<Currency> iter = _currencies.iterator();
      final Currency initialCurrency = iter.next();
      final CurrencyPairs pairs = (CurrencyPairs) inputs.getValue(CURRENCY_PAIRS);
      while (iter.hasNext()) {
        final Currency otherCurrency = iter.next();
        final CurrencyPair pair = pairs.getCurrencyPair(initialCurrency, otherCurrency);
        if (pair == null) {
          throw new OpenGammaRuntimeException("CurrencyPairs for currencies " + initialCurrency + " and " + otherCurrency + " not available");
        }
        final double spotRate = (Double) inputs.getValue(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(otherCurrency, initialCurrency))));
        matrix.addCurrency(otherCurrency, initialCurrency, spotRate);
      }
      return Collections.singleton(new ComputedValue(_spec, matrix));
    }

    @Override
    public ComputationTargetType getTargetType() {
      return ComputationTargetType.NULL;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      return Collections.singleton(_spec);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
      if (_currencies == null || _currencies.isEmpty() || _currencies.size() == 1) {
        return Collections.emptySet();
      }
      final Set<ValueRequirement> requirements = new HashSet<>();
      final Iterator<Currency> iter = _currencies.iterator();
      final Currency initialCurrency = iter.next();
      while (iter.hasNext()) {
        requirements.add(new ValueRequirement(ValueRequirementNames.SPOT_RATE, CurrencyPair.TYPE.specification(CurrencyPair.of(iter.next(), initialCurrency))));
      }
      requirements.add(new ValueRequirement(CURRENCY_PAIRS, ComputationTargetSpecification.NULL, ValueProperties.none()));
      return requirements;
    }
  };
}
