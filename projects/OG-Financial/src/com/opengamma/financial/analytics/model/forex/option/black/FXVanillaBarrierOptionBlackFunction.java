/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.indexoption.EquityIndexVanillaBarrierOptionFunction;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * This function splits a barrier option into a sum of vanilla FXOptionSecurity's,
 * and then calls down to the FXOptionBlackFunction for the paricular requirement
 */
public class FXVanillaBarrierOptionBlackFunction extends FXOptionBlackSingleValuedFunction {

  /**
   * @param valueRequirementName The desired output
   */
  public FXVanillaBarrierOptionBlackFunction(String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXBarrierOptionSecurity;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
    final FXBarrierOptionSecurity barrierSec = (FXBarrierOptionSecurity) target.getSecurity();

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

    // 2. Break the barrier security into it's vanilla analytic DEFINITIONS
    final Set<ForexOptionVanillaDefinition> vanillas = vanillaDecomposition(now, barrierSec, smoothing, overhedge);

    // 3. Build up the market data bundle
    // !!!!!!!!!!!!!!!!!! This will need a considerable rework
    // final StaticReplicationDataBundle market = buildMarketBundle(underlyingId, executionContext, inputs, target, desiredValues);

    // 4. Compute Values
 // !!!!!!!!!!!!!!!!!! This can be abstracted away, at least from here! :)
    final Object results = null; // computeValues(vanillas, market);

    // 5. Properties of what's required of this function
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), desiredValue.getConstraints());
    // 6. Return result
    return Collections.singleton(new ComputedValue(spec, results));

  }
  @Override
  protected Builder getResultProperties(ComputationTarget target) {
    Builder properties = super.getResultProperties(target);
    return properties.withAny(ValuePropertyNames.BINARY_OVERHEDGE)
                     .withAny(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
  }


  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Get requirements common to all EquityIndexOptions's
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

  private Set<ForexOptionVanillaDefinition> vanillaDecomposition(final ZonedDateTime valuation, final FXBarrierOptionSecurity barrierSec,
      final double smoothingFullWidth, final double overhedge) {

    final HashSet<ForexOptionVanillaDefinition> vanillas = new HashSet<ForexOptionVanillaDefinition>();
    // Unpack the barrier security
    final boolean isLong = barrierSec.getLongShort().isLong();
    final ZonedDateTime expiry = barrierSec.getExpiry().getExpiry();
    final double ttm = TimeCalculator.getTimeBetween(valuation, expiry);
    // The barrier has four types
    final BarrierDirection bInOut = barrierSec.getBarrierDirection(); //   KNOCK_IN, KNOCK_OUT,
    final BarrierType bUpDown = barrierSec.getBarrierType(); //   UP, DOWN, DOUBLE
    final double barrier = barrierSec.getBarrierLevel();

    // Put and Call Amounts, along with market convention for quote/base ccy define the strike, notional, and call/put interpretation
    final double callAmt = barrierSec.getCallAmount();
    final Currency callCcy = barrierSec.getCallCurrency();
    final double putAmt = barrierSec.getPutAmount();
    final Currency putCcy = barrierSec.getPutCurrency();

    final boolean inOrder = FXUtils.isInBaseQuoteOrder(putCcy, callCcy);
    final double baseAmt  = inOrder ? putAmt : callAmt; // This is the Notional of the option if interpreted as N*max(w(X-K),0)
    final double quoteAmt = inOrder ? callAmt : putAmt;
    final double strike = quoteAmt / baseAmt;
    final Currency baseCcy = inOrder ? putCcy : callCcy;
    final Currency quoteCcy = inOrder ? callCcy : putCcy; // This is the valuation currency in the (X,K) interpretation

    final boolean isFxCall = !inOrder; // TODO Confirm that we ignore this information!!

    // parameters to model binary as call/put spread
    final double oh = overhedge;
    final double width = strike * smoothingFullWidth;
    final double size; // = (barrier - strike ) / smoothingFullWidth;

    // There are four cases: UP and IN, UP and OUT, DOWN and IN, DOWN and OUT
    // Switch on direction: If UP, use Call Spreads. If DOWN, use Put spreads.
    boolean useCallSpread;
    double nearStrike;
    double farStrike;
    switch (bUpDown) {
      case UP:
        useCallSpread = true;
        if (barrier < strike) {
          throw new OpenGammaRuntimeException("Encountered an UP / CALL type of BarrierOption where barrier, " + barrier + ", is below strike, " + strike);
        }
        size = (barrier - strike) / width;
        nearStrike = barrier + oh - 0.5 * width;
        farStrike = barrier + oh + 0.5 * width;
        break;
      case DOWN:
        useCallSpread = false;
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

    // ForexVanillaOption's are defined in terms of the underlying forward FX transaction, the exchange of two fixed amounts in different currencies.
    // The relative size of the payments implicitly defines the option's strike. So we will build a number of ForexDefinition's below
    final PaymentFixedDefinition quoteCcyPayment = new PaymentFixedDefinition(quoteCcy, expiry, -1 * quoteAmt);
    final PaymentFixedDefinition baseCcyPayment = new PaymentFixedDefinition(baseCcy, expiry, baseAmt);

    // For the binaries, we need to adjust the Forex Payments to match the formula: k = A2/A1.
    // We do this by adjusting A2 = A1 * newStrike as A1 is the Notional in this interpretation
    ForexDefinition fxFwdForNearStrike = new ForexDefinition(baseCcyPayment, new PaymentFixedDefinition(quoteCcy, expiry, -1 * nearStrike * baseAmt));
    ForexDefinition fxFwdForFarStrike = new ForexDefinition(baseCcyPayment, new PaymentFixedDefinition(quoteCcy, expiry, -1 * farStrike * baseAmt));

    // Switch  on type
    switch (bInOut) {
      case KNOCK_OUT: // Long a linear at strike, short a binary at barrier of size (barrier-strike)

        ForexDefinition fxFwd = new ForexDefinition(baseCcyPayment, quoteCcyPayment);
        final ForexOptionVanillaDefinition longLinearK = new ForexOptionVanillaDefinition(fxFwd, expiry, useCallSpread, isLong);
        vanillas.add(longLinearK);
        // Short a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition shortNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, !isLong);
        final ForexOptionVanillaDefinition longFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, isLong);
        vanillas.add(shortNear);
        vanillas.add(longFar);
        break;
      case KNOCK_IN:  // Long a linear at *barrier*, long a binary at barrier of size (barrier - strike)

        ForexDefinition fxFwdForBarrier = new ForexDefinition(baseCcyPayment, new PaymentFixedDefinition(quoteCcy, expiry, -1*barrier*baseAmt));
        final ForexOptionVanillaDefinition longLinearB = new ForexOptionVanillaDefinition(fxFwdForBarrier, expiry, useCallSpread, isLong);
        vanillas.add(longLinearB);
        // Long a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition longNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, isLong);
        final ForexOptionVanillaDefinition shortFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, !isLong);
        vanillas.add(longNear);
        vanillas.add(shortFar);
        break;
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierDirection of: " + bUpDown);
    }
    return vanillas;
  }


  private static final Logger s_logger = LoggerFactory.getLogger(EquityIndexVanillaBarrierOptionFunction.class);

  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative forex, ForexOptionDataBundle<?> data, ComputationTarget target, Set<ValueRequirement> desiredValues, FunctionInputs inputs,
      ValueSpecification spec, FunctionExecutionContext executionContext) {
    throw new OpenGammaRuntimeException("Unexpectedly called FXVanillaBarrierOptionBlackFunction.getResult. Contact Quants.");
  }
}
