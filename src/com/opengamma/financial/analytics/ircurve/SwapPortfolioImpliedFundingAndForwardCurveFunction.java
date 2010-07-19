/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.analytics.model.swap.SwapScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.interestrate.DoubleCurveFinder;
import com.opengamma.financial.interestrate.DoubleCurveJacobian;
import com.opengamma.financial.interestrate.InterestRateCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

/**
 * 
 */
public class SwapPortfolioImpliedFundingAndForwardCurveFunction extends AbstractFunction implements FunctionInvoker {
  private final Currency _currency;
  //TODO all interpolators should be passed in
  private final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> _interpolator; // TODO this should not be hard-coded
  // TODO this should depend on the type of _fundingInterpolator
  private final Interpolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> _interpolatorWithSensitivity;
  private final InterestRateCalculator _swapRateCalculator = new InterestRateCalculator();
  private final String _fundingCurveName;
  private final String _forwardCurveName;

  public SwapPortfolioImpliedFundingAndForwardCurveFunction(final Currency currency) {
    Validate.notNull(currency);
    _currency = currency;
    // TODO these should depend on what will be parameters: rootfinder type and jacobian calculation method
    _fundingCurveName = ValueRequirementNames.YIELD_CURVE + "_" + _currency + "_Broyden_AnalyticJacobian_FundingCurve";
    _forwardCurveName = ValueRequirementNames.YIELD_CURVE + "_" + _currency + "_Broyden_AnalyticJacobian_ForwardCurve";
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getSnapshotClock().zonedDateTime();
    final PortfolioNode node = target.getPortfolioNode();
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) inputs.getValue(getFundingCurveRequirement());
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) inputs.getValue(getFundingCurveRequirement());
    final Calendar calendar = null; // TODO where does this live?
    SwapSecurity swapSecurity;
    SwapLeg payLeg, receiveLeg;
    ZonedDateTime effectiveDate, maturityDate;
    boolean payFixed;
    final int numberOfSwaps = node.getPositions().size();
    final List<InterestRateDerivative> swaps = new ArrayList<InterestRateDerivative>();
    final double[] marketRates = new double[numberOfSwaps];
    final double[] fundingNodeTimes = new double[fundingCurve.getMaturities().size()];
    final double[] forwardNodeTimes = new double[forwardCurve.getMaturities().size()];
    final double[] initialRatesGuess = new double[numberOfSwaps]; // TODO is this where the fixed rate should live?
    int i = 0, nFix, nFloat;
    // final ZonedDateTime[] referenceRatePaymentDates; TODO use this when the offsets are actually calculated
    for (final Position position : node.getPositions()) {
      swapSecurity = (SwapSecurity) position;
      payLeg = swapSecurity.getPayLeg();
      receiveLeg = swapSecurity.getReceiveLeg();
      effectiveDate = swapSecurity.getEffectiveDate();
      maturityDate = swapSecurity.getMaturityDate();
      final double[] payLegPaymentTimes = SwapScheduleCalculator.getPaymentTimes(effectiveDate, maturityDate, payLeg, calendar, now);
      final double[] receiveLegPaymentTimes = SwapScheduleCalculator.getPaymentTimes(effectiveDate, maturityDate, receiveLeg, calendar, now);
      payFixed = payLeg instanceof FixedInterestRateLeg;
      Swap swap;
      double[] fixedPaymentTimes;
      double[] floatPaymentTimes;
      double[] forwardStartOffsets;
      double[] forwardEndOffsets;
      if (payFixed) {
        nFix = payLegPaymentTimes.length;
        nFloat = receiveLegPaymentTimes.length;
        fixedPaymentTimes = payLegPaymentTimes;
        floatPaymentTimes = receiveLegPaymentTimes;
        forwardStartOffsets = new double[nFloat];
        forwardEndOffsets = new double[nFloat];
      } else {
        nFix = receiveLegPaymentTimes.length;
        nFloat = payLegPaymentTimes.length;
        fixedPaymentTimes = receiveLegPaymentTimes;
        floatPaymentTimes = payLegPaymentTimes;
        forwardStartOffsets = new double[nFloat];
        forwardEndOffsets = new double[nFloat];
      }
      swap = new Swap(fixedPaymentTimes, floatPaymentTimes, forwardStartOffsets, forwardEndOffsets);
      // swap rates from bbg
      marketRates[i] = _swapRateCalculator.getRate(fundingCurve, fundingCurve, swap);
      fundingNodeTimes[i] = Math.max(fixedPaymentTimes[nFix - 1], floatPaymentTimes[nFloat - 1] + forwardEndOffsets[nFloat - 1]);
      // forwardNodeTimes[i] = somethign
      initialRatesGuess[i] = 0.05;
      swaps.add(swap);
      i++;
    }
    final DoubleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle> jacobian = new DoubleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle>(swaps, _spotRate,
        forwardNodeTimes, fundingNodeTimes, _fundingInterpolatorWithSensitivity, _forwardInterpolatorWithSensitivity);
    final DoubleCurveFinder curveFinder = new DoubleCurveFinder(swaps, marketRates, _spotRate, forwardNodeTimes, fundingNodeTimes, forwardCurve, fundingCurve, _fundingInterpolator,
        _forwardInterpolator);
    // TODO this should not be hard-coded
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
    final double[] yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    final YieldAndDiscountCurve fundingResult = new InterpolatedYieldCurve(fundingNodeTimes, yields, _fundingInterpolator);
    final YieldAndDiscountCurve forwardResult = new InterpolatedYieldCurve(forwardNodeTimes, yields, _fundingInterpolator);
    final ValueSpecification fundingCurveSpecification = new ValueSpecification(new ValueRequirement(_fundingCurveName, target));
    final ValueSpecification forwardCurveSpecification = new ValueSpecification(new ValueRequirement(_forwardCurveName, target));
    return Sets.newHashSet(new ComputedValue(fundingCurveSpecification, fundingResult), new ComputedValue(forwardCurveSpecification, forwardResult));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PORTFOLIO_NODE) {
      return false;
    }
    final PortfolioNode node = target.getPortfolioNode();
    final List<Position> positions = node.getPositions();
    SwapSecurity swap;
    for (final Position position : positions) {
      if (!(position.getSecurity() instanceof SwapSecurity)) {
        return false;
      }
      swap = (SwapSecurity) position.getSecurity();
      if (!(swap.getPayLeg() instanceof InterestRateLeg) || !(swap.getReceiveLeg() instanceof InterestRateLeg)) {
        return false;
      }
      if ((swap.getPayLeg() instanceof FixedInterestRateLeg && !(swap.getReceiveLeg() instanceof FloatingInterestRateLeg))
          || (swap.getReceiveLeg() instanceof FixedInterestRateLeg && !(swap.getPayLeg() instanceof FloatingInterestRateLeg))) {
        return false;
      }
    }
    return false;
  }

  private ValueRequirement getFundingCurveRequirement() {
    // TODO make sure that the definition is actually called a funding curve
    return new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, ComputationTargetType.PRIMITIVE, _currency.getUniqueIdentifier());
  }

  private ValueRequirement getForwardCurveRequirement() {
    // TODO make sure that the definition is actually called a forward curve
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, ComputationTargetType.PRIMITIVE, _currency.getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getFundingCurveRequirement(), getForwardCurveRequirement());
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(_fundingCurveName, target)), new ValueSpecification(new ValueRequirement(_forwardCurveName, target)));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SwapImpliedFundingAndForwardCurveFunction" + _currency;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
