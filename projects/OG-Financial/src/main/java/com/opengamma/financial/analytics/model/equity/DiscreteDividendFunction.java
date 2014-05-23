/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.DateUtils;

/**
 * Dividend payments (per share) at discrete times $\tau_i$ of the form $\alpha_i + \beta_iS_{\tau_{i^-}}$  where $S_{\tau_{i^-}}$ is the stock price immediately before the
 * dividend payment.<p>
 * 
 * This simple version takes three typically available inputs (eg from Activ): the next dividend date, the annual amount, and the payment frequency.
 * From these, we construct a model which pays fixed amounts for the first year, and amounts proportional to the share price thereafter  
 */
public class DiscreteDividendFunction extends AbstractFunction.NonCompiledInvoker {
  
  private final double _dividendHorizon;
  private final double _timeThatProportionalDividendsBegin;
  
  public DiscreteDividendFunction(final double dividendHorizon, final double timeThatProportionalDividendsBegin) {
    ArgumentChecker.notNull(dividendHorizon, "dividendHorizon is null");
    ArgumentChecker.notNull(timeThatProportionalDividendsBegin, "timeThatProportionalDividendsBegin is null");
    _dividendHorizon = dividendHorizon;
    _timeThatProportionalDividendsBegin = timeThatProportionalDividendsBegin;
  }
  /** Default constructor */
  public DiscreteDividendFunction() {
    _dividendHorizon = 2.0;
    _timeThatProportionalDividendsBegin = 2.0;    
  }
  private static final Logger s_logger = LoggerFactory.getLogger(DiscreteDividendFunction.class);
  private static final Set<ExternalScheme> s_validSchemes = ImmutableSet.of(ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.ACTIVFEED_TICKER);
  
  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    
    // The frequency sets up an interval 
    Double nDividendsPerYear = (Double) inputs.getValue(MarketDataRequirementNames.DIVIDEND_FREQUENCY);
    if (nDividendsPerYear == null) {
      s_logger.debug("No dividend frequency - defaulting to 4 per year");
      nDividendsPerYear = 4.0;
    }
    final double dividendInterval = 1.0 / nDividendsPerYear;
    final int nDividends = (int) Math.ceil(getDividendHorizon() * nDividendsPerYear);
    
    // The next dividend date anchors the vector of dividend times
    double firstDivTime;
    final Object nextDividendInput = inputs.getValue(MarketDataRequirementNames.NEXT_DIVIDEND_DATE);
    if (nextDividendInput != null) {
      final LocalDate nextDividendDate = DateUtils.toLocalDate(nextDividendInput);
      final LocalDate valuationDate = ZonedDateTime.now(executionContext.getValuationClock()).toLocalDate();
      firstDivTime = TimeCalculator.getTimeBetween(valuationDate, nextDividendDate);
      if (firstDivTime < 0.0) {
        s_logger.warn("Next_Dividend Date is in the past. We will estimate next future date and continue. See [ACTIV-62]");
        firstDivTime = dividendInterval; // TODO: Review [ACTIV-62]
      }
    } else {
      firstDivTime = dividendInterval;
    }
        
    // The annual amount defines what we model to be known future amounts 
    // and the spot share price is used to define the proportional amounts
    final double annualAmount;
    Object annualDividendInput = inputs.getValue(MarketDataRequirementNames.ANNUAL_DIVIDEND);
    if (annualDividendInput != null) {
      annualAmount = (double) annualDividendInput;
    } else {
      annualAmount = 0.0;
    }
    final double fixedAmt = annualAmount / nDividendsPerYear;
    final double proportionalAmt;
    final Object sharePriceInput = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (sharePriceInput != null) {
      final double sharePrice = (double) sharePriceInput;
      proportionalAmt = fixedAmt / sharePrice;
    } else {
      proportionalAmt = 0.0;
    }
    
    // Now we can define vectors of dividends d_i = alpha_i + beta_i * share_price(t_i)
    final double[] divTimes = new double[nDividends];
    final double[] fixedAmounts = new double[nDividends];
    final double[] proportionalAmounts = new double[nDividends];
    final double crossover = getTimeThatProportionalDividendsBegin();
    for (int i = 0; i < nDividends; i++) {
      divTimes[i] = firstDivTime + i * dividendInterval;
      if (divTimes[i] < crossover) {
        fixedAmounts[i] = fixedAmt;
        proportionalAmounts[i] = 0.0; 
      } else {
        fixedAmounts[i] = 0.0;
        proportionalAmounts[i] = proportionalAmt;
      }
    }
    
    final AffineDividends dividends = new AffineDividends(divTimes, fixedAmounts, proportionalAmounts);
    final ValueProperties properties = getValuePropertiesBuilder().get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.AFFINE_DIVIDENDS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, dividends));
  }

  @Override // REVIEW Andrew 2012-01-17 -- Can we make the target type of this SECURITY, or even EQUITY_SECURITY ?
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getValue() instanceof ExternalIdentifiable) {
      final ExternalId identifier = ((ExternalIdentifiable) target.getValue()).getExternalId();
      return s_validSchemes.contains(identifier.getScheme());
    }
    return false;
  }
  
  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }
  
  @Override
  /** Any or all of the requirements may not be available. */
  public boolean canHandleMissingInputs() {
    return true;
  }
  
  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.AFFINE_DIVIDENDS, target.toSpecification(), getValuePropertiesBuilder().get()));
  }

  private ValueProperties.Builder getValuePropertiesBuilder() {
    return createValueProperties()
        .with("DividendHorizon", String.valueOf(getDividendHorizon())) // TODO: Add "DividendHorizon" to ValuePropertyNames
        .with("TimeThatProportionalDividendsBegin", String.valueOf(getTimeThatProportionalDividendsBegin())); // TODO: Add "TimeThatProportionalDividendsBegin" to ValuePropertyNames
  }
  
  public double getDividendHorizon() {
    return _dividendHorizon;
  }
  
  public double getTimeThatProportionalDividendsBegin() {
    return _timeThatProportionalDividendsBegin;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.ANNUAL_DIVIDEND, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.NEXT_DIVIDEND_DATE, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.DIVIDEND_FREQUENCY, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, target.getUniqueId()));
    return requirements;
  }
}
