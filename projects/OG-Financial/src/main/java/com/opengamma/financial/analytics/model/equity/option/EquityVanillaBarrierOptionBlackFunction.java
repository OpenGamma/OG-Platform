/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.ExerciseTypeAnalyticsVisitorAdapter;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * This function splits a barrier option into a sum of vanilla calls or puts,
 * and then calls down to the EquityIndexOptionFunction as its requirements
 */
public abstract class EquityVanillaBarrierOptionBlackFunction extends EquityOptionBlackFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityVanillaBarrierOptionBlackFunction.class);

  /**
   * @param requirementName The desired output
   */
  public EquityVanillaBarrierOptionBlackFunction(final String requirementName) {
    super(requirementName);
  }

  /**
   * This method is defined by extending Functions
   * @param vanillaOptions Set of EquityIndexOptions that European Barrier is composed of. Binaries are modelled as spreads
   * @param market EquityOptionDataBundle
   * @param inputs The market data inputs
   * @param desiredValues The desired values
   * @param targetSpec The target specification of the result
   * @param resultProperties The result properties
   * @return the result
   */
  protected abstract Set<ComputedValue> computeValues(Set<EquityIndexOption> vanillaOptions, StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties);

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    throw new OpenGammaRuntimeException("Execution wasn't intended to go here. Please review.");
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues)
    throws AsynchronousExecution {

    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final EquityBarrierOptionSecurity barrierSec = (EquityBarrierOptionSecurity) target.getSecurity();
    final ExternalId underlyingId = barrierSec.getUnderlyingId();
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    // 1. Get parameters for the smoothing of binary payoffs into put spreads
    final String strOH = desiredValue.getConstraint(ValuePropertyNames.BINARY_OVERHEDGE);
    if (strOH == null) {
      throw new OpenGammaRuntimeException("Could not find: " + ValuePropertyNames.BINARY_OVERHEDGE);
    }
    final Double overhedge = Double.parseDouble(strOH);

    final String strSmooth = desiredValue.getConstraint(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    if (strSmooth == null) {
      throw new OpenGammaRuntimeException("Could not find: " + ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    }
    final Double smoothing = Double.parseDouble(strSmooth);

    // 2. Break the barrier security into it's vanilla analytic derivatives
    final Set<EquityIndexOption> vanillas = vanillaDecomposition(now, barrierSec, smoothing, overhedge);
    if (vanillas.iterator().next().getTimeToSettlement() < 0.0) {
      throw new OpenGammaRuntimeException("EquityBarrierOptionSecurity with expiry, " + barrierSec.getExpiry().getExpiry().toString() + ", has already settled.");
    }
    // 3. Build up the market data bundle
    final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // 4. Properties of what's required of this function
    final ValueProperties resultProperties = desiredValue.getConstraints().copy().get();
    // 5. Compute Values and return
    return computeValues(vanillas, market, inputs, desiredValues, target.toSpecification(), resultProperties);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final ExerciseType exerciseType = ((EquityBarrierOptionSecurity) security).getExerciseType();
    if (!(exerciseType instanceof EuropeanExerciseType)) {
      return false;
    }
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Get requirements common to all EquityIndexOptionss
    final Set<ValueRequirement> commonReqs = super.getRequirements(context, target, desiredValue);
    if (commonReqs == null) {
      return null;
    }
    // Barriers additionally have parameters for the smoothing of binary payoffs into put spreads
    // Return null if they haven't been set so that EquityIndexVanillaBarrierOptionDefaultPropertiesFunction can set them
    final Set<String> overhedgeSet = desiredValue.getConstraints().getValues(ValuePropertyNames.BINARY_OVERHEDGE);
    if (overhedgeSet == null || overhedgeSet.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.BINARY_OVERHEDGE);
      return null;
    }
    final Set<String> smoothingSet = desiredValue.getConstraints().getValues(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    if (smoothingSet == null || smoothingSet.size() != 1) {
      s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
      return null;
    }
    return commonReqs;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Set<ValueSpecification> results = super.getResults(context, target, inputs);
    final Set<ValueSpecification> resultsWithExtraProperties = Sets.newHashSetWithExpectedSize(results.size());
    for (final ValueSpecification spec : results) {
      final String name = spec.getValueName();
      final ComputationTargetSpecification targetSpec = spec.getTargetSpecification();
      final ValueProperties properties = spec.getProperties().copy()
          .withAny(ValuePropertyNames.BINARY_OVERHEDGE)
          .withAny(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH)
          .get();
      resultsWithExtraProperties.add(new ValueSpecification(name, targetSpec, properties));
    }
    return results;
  }

  //TODO does all of this work need to be done in financial?
  private Set<EquityIndexOption> vanillaDecomposition(final ZonedDateTime valuation, final EquityBarrierOptionSecurity barrierOption,
      final double smoothingFullWidth, final double overhedge) {

    final Set<EquityIndexOption> vanillas = new HashSet<>();
    // Unpack the barrier security
    final BarrierDirection bInOut = barrierOption.getBarrierDirection(); //   KNOCK_IN, KNOCK_OUT,
    final BarrierType bUpDown = barrierOption.getBarrierType(); //   UP, DOWN, DOUBLE
    final double strike = barrierOption.getStrike();
    final double barrier = barrierOption.getBarrierLevel();
    final ZonedDateTime expiry = barrierOption.getExpiry().getExpiry();
    final double ttm = TimeCalculator.getTimeBetween(valuation, expiry);
    final Currency ccy = barrierOption.getCurrency();
    final double ptVal = barrierOption.getPointValue();

    // parameters to model binary as call/put spread
    final double oh = overhedge;
    final double width = barrier * smoothingFullWidth; // we specify smoothing as relative value
    final double size; // = (barrier - strike ) / smoothingFullWidth;

    // There are four cases: UP and IN, UP and OUT, DOWN and IN, DOWN and OUT
    // Switch on direction: If UP, use Call Spreads. If DOWN, use Put spreads.
    boolean isCall;
    double nearStrike;
    double farStrike;
    switch (bUpDown) {
      case UP:
        isCall = true;
        if (barrierOption.getOptionType().equals(OptionType.PUT)) {
          throw new OpenGammaRuntimeException("ONE_LOOK / Vanilla Barriers do not apply to an UP type of Barrier with OptionType.CALL. Confirm that the intended samplingFrequency is ONE_LOOK");
        }
        if (barrier < strike) {
          throw new OpenGammaRuntimeException("Encountered an UP / CALL type of BarrierOption where barrier, " + barrier + ", is below strike, " + strike);
        }
        size = (barrier - strike) / width;
        nearStrike = barrier + oh - 0.5 * width;
        farStrike = barrier + oh + 0.5 * width;
        break;
      case DOWN:
        isCall = false;
        if (barrierOption.getOptionType().equals(OptionType.CALL)) {
          throw new OpenGammaRuntimeException("ONE_LOOK / Vanilla Barriers do not apply to a DOWN type of Barrier with OptionType.PUT. Confirm that the intended samplingFrequency is ONE_LOOK");
        }
        if (barrier > strike) {
          throw new OpenGammaRuntimeException("Encountered a DOWN / PUT type of BarrierOption where barrier, " + barrier + ", is above strike, " + strike);
        }
        size = (strike - barrier) / width;
        nearStrike = barrier + oh + 0.5 * width;
        farStrike = barrier + oh - 0.5 * width;
        break;
      case DOUBLE:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption where barrierType is DOUBLE. This isn't yet handled.");
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierType of: " + bUpDown);
    }

    // Switch  on type
    final ExerciseDecisionType exerciseType = barrierOption.getExerciseType().accept(ExerciseTypeAnalyticsVisitorAdapter.getInstance());
    switch (bInOut) {
      case KNOCK_OUT:
        // Long a linear at strike, short a linear at barrier
        final EquityIndexOption longlinearK = new EquityIndexOption(ttm, ttm, strike, isCall, ccy, ptVal, exerciseType, SettlementType.PHYSICAL);
        final EquityIndexOption shortLinearB = new EquityIndexOption(ttm, ttm, barrier, isCall, ccy, -ptVal, exerciseType, SettlementType.PHYSICAL);
        vanillas.add(longlinearK);
        vanillas.add(shortLinearB);
        // Short a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final EquityIndexOption shortNear = new EquityIndexOption(ttm, ttm, nearStrike, isCall, ccy, -1 * ptVal * size, exerciseType, SettlementType.PHYSICAL);
        final EquityIndexOption longFar = new EquityIndexOption(ttm, ttm, farStrike, isCall, ccy, ptVal * size, exerciseType, SettlementType.PHYSICAL);
        vanillas.add(shortNear);
        vanillas.add(longFar);
        break;
      case KNOCK_IN:
        // Long a linear at barrier
        final EquityIndexOption longLinearB = new EquityIndexOption(ttm, ttm, barrier, isCall, ccy, ptVal, exerciseType, SettlementType.PHYSICAL);
        vanillas.add(longLinearB);
        // Long a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final EquityIndexOption longNear = new EquityIndexOption(ttm, ttm, nearStrike, isCall, ccy, ptVal * size, exerciseType, SettlementType.PHYSICAL);
        final EquityIndexOption shortFar = new EquityIndexOption(ttm, ttm, farStrike, isCall, ccy, -1 * ptVal * size, exerciseType, SettlementType.PHYSICAL);
        vanillas.add(longNear);
        vanillas.add(shortFar);
        break;
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierDirection of: " + bUpDown);
    }
    return vanillas;
  }

}
