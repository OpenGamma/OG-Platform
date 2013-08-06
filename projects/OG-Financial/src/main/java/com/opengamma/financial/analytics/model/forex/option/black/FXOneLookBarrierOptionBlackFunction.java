/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.equity.option.EquityVanillaBarrierOptionBlackFunction;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.util.money.Currency;

/**
 * This function splits a European ONE-LOOK Barrier Option into a sum of vanilla FXOptionSecurity's,
 * and then calls down to the FXOptionBlackFunction for the particular requirement. <p>
 * See FXBarrierOptionBlackFunction for Functions on TRUE Barriers. That is, options that knock in or out contingent on hitting a barrier,
 * at ANY time before expiry. The one-look case here only checks the barrier at expiry. <p>
 * The payoffs are thus restricted, on cannot have a Down-and-Out nor Down-and-In Calls, nor Up-and-In and Up-and-Out Puts <p>
 */
public abstract class FXOneLookBarrierOptionBlackFunction extends FXOptionBlackSingleValuedFunction {

  /**
   * @param valueRequirementName The desired output
   */
  public FXOneLookBarrierOptionBlackFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof FXBarrierOptionSecurity) {
      return ((FXBarrierOptionSecurity) security).getSamplingFrequency().equals(SamplingFrequency.ONE_LOOK);
    }
    return false;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
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
    final Set<ForexOptionVanilla> vanillas = vanillaDecomposition(barrierSec, smoothing, overhedge, now, desiredValues);


    // 3. Build up the market data bundle
    final ForexOptionDataBundle<?> market = FXOptionFunctionUtils.buildMarketBundle(now, inputs, target, desiredValues);

    // TODO Confirm whether we need to support both types of dataBundles: SmileDeltaTermStructureParametersStrikeInterpolation AND BlackForexTermStructureParameters

    // 4. Compute Values - in base class
    final Object results = computeValues(vanillas, market);

    // 5. Properties of what's required of this function
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final Currency putCurrency = barrierSec.getPutCurrency();
    final Currency callCurrency = barrierSec.getCallCurrency();
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), getResultProperties(target, desiredValue, baseQuotePair).get());
    // 6. Return result
    return Collections.singleton(new ComputedValue(spec, results));

  }

  @Override
  protected Builder getResultProperties(final ComputationTarget target) {
    final Builder properties = super.getResultProperties(target);
    return properties.withAny(ValuePropertyNames.BINARY_OVERHEDGE)
        .withAny(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String putCurve, final String putCurveCalculationConfig, final String callCurve,
      final String callCurveCalculationConfig, final CurrencyPair baseQuotePair, final ValueProperties optionalProperties) {
    final Builder properties = super.getResultProperties(target, putCurve, putCurveCalculationConfig, callCurve, callCurveCalculationConfig, baseQuotePair, optionalProperties);
    return properties.withAny(ValuePropertyNames.BINARY_OVERHEDGE)
        .withAny(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair);
    final String binaryOverhead = desiredValue.getConstraint(ValuePropertyNames.BINARY_OVERHEDGE);
    final String binarySmoothing = desiredValue.getConstraint(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH);
    return properties.with(ValuePropertyNames.BINARY_OVERHEDGE, binaryOverhead)
        .with(ValuePropertyNames.BINARY_SMOOTHING_FULLWIDTH, binarySmoothing);
  }

  /**
   * This method is defined by extending Functions
   * @param vanillas Set of ForexOptionVanilla that European Barrier is composed of. Binaries are modelled as spreads
   * @param market ForexOptionDataBundle, typically SmileDeltaTermStructureParametersStrikeInterpolation
   * @return  ComputedValue what the function promises to deliver
   */
  protected abstract Object computeValues(Set<ForexOptionVanilla> vanillas, ForexOptionDataBundle<?> market);

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // Get requirements common to all FXOptionBlackFunctions
    final Set<ValueRequirement> commonReqs = super.getRequirements(context, target, desiredValue);
    if (commonReqs == null) {
      return null;
    }
    // Barriers additionally have parameters for the smoothing of binary payoffs into put spreads
    // Return null if they haven't been set so that FXOneLookBarrierOptionDefaultPropertiesFunction can set them
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

  // TODO: Consider whether to interpret overhedge so that a positive value always reduces the price, i.e. incorporate isLong
  private static Set<ForexOptionVanilla> vanillaDecomposition(final FXBarrierOptionSecurity barrierSec,
      final double smoothingFullWidth, final double overhedge, final ZonedDateTime valTime, final Set<ValueRequirement> desiredValues) {

    final HashSet<ForexOptionVanilla> vanillas = new HashSet<>();
    // Unpack the barrier security
    final boolean isLong = barrierSec.getLongShort().isLong();
    final ZonedDateTime expiry = barrierSec.getExpiry().getExpiry();
    final ZonedDateTime settlement = barrierSec.getSettlementDate();

    // The barrier has four types
    final BarrierDirection bInOut = barrierSec.getBarrierDirection(); //   KNOCK_IN, KNOCK_OUT,
    final BarrierType bUpDown = barrierSec.getBarrierType(); //   UP, DOWN, DOUBLE
    final double barrier = barrierSec.getBarrierLevel();

    // Put and Call Amounts, along with market convention for quote/base ccy define the strike, notional, and call/put interpretation
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final double callAmt = barrierSec.getCallAmount();
    final Currency callCcy = barrierSec.getCallCurrency();
    final double putAmt = barrierSec.getPutAmount();
    final Currency putCcy = barrierSec.getPutCurrency();

    final boolean inOrder = FXUtils.isInBaseQuoteOrder(putCcy, callCcy);
    double baseAmt; // This is the Notional of the option if interpreted as N*max(w(X-K),0)
    double quoteAmt;
    Currency baseCcy;
    Currency quoteCcy; // This is the valuation currency in the (X,K) interpretation
    String baseCurveName;
    String quoteCurveName;
    boolean linearIsCall; //
    if (inOrder) {
      linearIsCall = false; // putCcy == baseCcy => Put
      baseAmt = putAmt;
      baseCcy = putCcy;
      baseCurveName = putCurveName + "_" + putCcy.getCode();
      quoteAmt = callAmt;
      quoteCcy = callCcy;
      quoteCurveName = callCurveName + "_" + callCcy.getCode();
    } else {
      linearIsCall = true; // callCcy == baseCcy => Call
      baseAmt = callAmt;
      baseCcy = callCcy;
      baseCurveName = callCurveName + "_" + callCcy.getCode();
      quoteAmt = putAmt;
      quoteCcy = putCcy;
      quoteCurveName = putCurveName + "_" + putCcy.getCode();
    }
    final double strike = quoteAmt / baseAmt;
    final String[] baseQuoteCurveNames = new String[] {baseCurveName, quoteCurveName};

    // parameters to model binary as call/put spread
    final double oh = overhedge;
    final double width = barrier * smoothingFullWidth;
    final double size;

    // There are four cases: UP and IN, UP and OUT, DOWN and IN, DOWN and OUT
    // Switch on direction: If UP, use Call Spreads. If DOWN, use Put spreads.
    boolean useCallSpread;
    double nearStrike;
    double farStrike;
    switch (bUpDown) {
      case UP:
        useCallSpread = true;
        if (!linearIsCall) {
          throw new OpenGammaRuntimeException("ONE_LOOK Barriers do not apply to an UP type of Barrier unless the option itself is a Call. Check Call/Put currencies.");
        }
        if (barrier < strike) {
          throw new OpenGammaRuntimeException("Encountered an UP type of BarrierOption where barrier, " + barrier + ", is below strike, " + strike);
        }
        size = (barrier - strike) / width;
        nearStrike = barrier + oh - 0.5 * width;
        farStrike = barrier + oh + 0.5 * width;
        if (nearStrike < 0.0 || farStrike < 0.0) {
          throw new OpenGammaRuntimeException("A strike in the put binary approximation is negative. Look at the BinaryOverhedge and BinarySmoothingFullWidth properties.");
        }
        break;
      case DOWN:
        useCallSpread = false;
        if (linearIsCall) {
          throw new OpenGammaRuntimeException("ONE_LOOK Barriers do not apply to a DOWN type of Barrier unless the option itself is a Put. Check Call/Put currencies.");
        }
        if (barrier > strike) {
          throw new OpenGammaRuntimeException("Encountered a DOWN type of BarrierOption where barrier, " + barrier + ", is above strike, " + strike);
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
    final PaymentFixedDefinition quoteCcyPayment = new PaymentFixedDefinition(quoteCcy, settlement, -1 * quoteAmt);
    final PaymentFixedDefinition baseCcyPayment = new PaymentFixedDefinition(baseCcy, settlement, baseAmt);
    final ForexDefinition fxFwd = new ForexDefinition(baseCcyPayment, quoteCcyPayment); // This is what defines the strike, K = quoteAmt / baseAmt
    // We restrike an option by changing the underlying Forex, adjusting the Payments to match the formulae: k = A2/A1, N = A1.
    final ForexDefinition fxFwdForBarrier = new ForexDefinition(baseCcyPayment, new PaymentFixedDefinition(quoteCcy, settlement, -1 * barrier * baseAmt));

    // For the binaries, we do this by adjusting A1' = size * A1; A2' = A1' * newStrike as A1 is the Notional in this interpretation
    final double baseAmtForSpread = size * baseAmt;
    final PaymentFixedDefinition baseCcyPmtForSpread = new PaymentFixedDefinition(baseCcy, settlement, baseAmtForSpread);
    final ForexDefinition fxFwdForNearStrike = new ForexDefinition(baseCcyPmtForSpread, new PaymentFixedDefinition(quoteCcy, settlement, -1 * nearStrike * baseAmtForSpread));
    final ForexDefinition fxFwdForFarStrike = new ForexDefinition(baseCcyPmtForSpread, new PaymentFixedDefinition(quoteCcy, settlement, -1 * farStrike * baseAmtForSpread));


    // Switch  on type
    switch (bInOut) {
      case KNOCK_OUT: // Long a linear at strike, short a linear at barrier, short a binary at barrier of size (barrier-strike)

        final ForexOptionVanillaDefinition longLinearK = new ForexOptionVanillaDefinition(fxFwd, expiry, useCallSpread, isLong);
        final ForexOptionVanillaDefinition shortLinearB = new ForexOptionVanillaDefinition(fxFwdForBarrier, expiry, useCallSpread, !isLong);
        vanillas.add(longLinearK.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(shortLinearB.toDerivative(valTime, baseQuoteCurveNames));
        // Short a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition shortNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, !isLong);
        final ForexOptionVanillaDefinition longFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, isLong);
        vanillas.add(shortNear.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(longFar.toDerivative(valTime, baseQuoteCurveNames));
        break;

      case KNOCK_IN:  // Long a linear at barrier, long a binary at barrier of size (barrier - strike)

        final ForexOptionVanillaDefinition longLinearB = new ForexOptionVanillaDefinition(fxFwdForBarrier, expiry, useCallSpread, isLong);
        vanillas.add(longLinearB.toDerivative(valTime, baseQuoteCurveNames));
        // Long a binary of size, barrier - strike. Modelled as call spread struck around strike + oh, with spread of 2*eps
        final ForexOptionVanillaDefinition longNear = new ForexOptionVanillaDefinition(fxFwdForNearStrike, expiry, useCallSpread, isLong);
        final ForexOptionVanillaDefinition shortFar = new ForexOptionVanillaDefinition(fxFwdForFarStrike, expiry, useCallSpread, !isLong);
        vanillas.add(longNear.toDerivative(valTime, baseQuoteCurveNames));
        vanillas.add(shortFar.toDerivative(valTime, baseQuoteCurveNames));
        break;
      default:
        throw new OpenGammaRuntimeException("Encountered an EquityBarrierOption with unexpected BarrierDirection of: " + bUpDown);
    }
    return vanillas;
  }


  private static final Logger s_logger = LoggerFactory.getLogger(EquityVanillaBarrierOptionBlackFunction.class);

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    throw new OpenGammaRuntimeException("Unexpectedly called FXVanillaBarrierOptionBlackFunction.getResult");
  }
}
