/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.SingleCurveFinder;
import com.opengamma.financial.interestrate.SingleCurveJacobian;
import com.opengamma.financial.interestrate.SwapRateCalculator;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

/**
 * 
 */
public class SwapPortfolioImpliedYieldCurveFunction extends AbstractFunction implements FunctionInvoker {
  private final Currency _currency;
  private final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> _interpolator = new NaturalCubicSplineInterpolator1D(); // TODO this should not be hard-coded
  // TODO this should depend on the type of _interpolator
  private final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> _interpolatorWithSensitivity = new CubicSplineInterpolatorWithSensitivities1D();
  private final double _spotRate = 0.01; // TODO this needs to be changed - it is the "instantaneous" interest rate. Possibly the O/N rate for the currency is the best proxy
  private final SwapRateCalculator _swapRateCalculator = new SwapRateCalculator();
  private final String _curveName;

  public SwapPortfolioImpliedYieldCurveFunction(final Currency currency) {
    Validate.notNull(currency);
    _currency = currency;
    // TODO this should depend on what will be parameters: rootfinder type and jacobian calculation method
    _curveName = ValueRequirementNames.YIELD_CURVE + "_" + _currency + "_Broyden_AnalyticJacobian_SingleCurveCalculation";
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getSnapshotClock().zonedDateTime();
    final PortfolioNode node = target.getPortfolioNode();
    final YieldAndDiscountCurve inputCurve = (YieldAndDiscountCurve) inputs.getValue(getRequirement());
    final Calendar calendar = null; // TODO where does this live?
    SwapSecurity swapSecurity;
    SwapLeg payLeg, receiveLeg;
    ZonedDateTime effectiveDate, maturityDate;
    boolean payFixed;
    final int numberOfSwaps = node.getPositions().size();
    final List<InterestRateDerivative> swaps = new ArrayList<InterestRateDerivative>();
    final double[] marketRates = new double[numberOfSwaps];
    final double[] nodeTimes = new double[numberOfSwaps];
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
      // marketRates = swap rate from bloomberg
      // marketRates[i] = _swapRateCalculator.getRate(inputCurve, inputCurve, swap);
      nodeTimes[i] = Math.max(fixedPaymentTimes[nFix - 1], floatPaymentTimes[nFloat - 1] + forwardEndOffsets[nFloat - 1]);
      initialRatesGuess[i] = 0.05;
      swaps.add(swap);
      i++;
    }
    final SingleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle> jacobian = new SingleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle>(swaps, _spotRate,
        nodeTimes, _interpolatorWithSensitivity);
    final SingleCurveFinder curveFinder = new SingleCurveFinder(swaps, marketRates, _spotRate, nodeTimes, _interpolator);
    // TODO this should not be hard-coded
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
    final double[] yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(nodeTimes, yields, _interpolator);
    final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(_curveName, target));
    final ComputedValue result = new ComputedValue(resultSpecification, curve);
    return Sets.newHashSet(result);
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
    return true;
  }

  public static Set<ValueRequirement> buildRequirements(final YieldCurveDefinition definition) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStrip strip : definition.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      result.add(requirement);
    }
    return result;
  }

  private ValueRequirement getRequirement() {
    // TODO make sure that the definition is actually called a funding curve
    return new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, ComputationTargetType.PRIMITIVE, _currency.getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getRequirement());
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(_curveName, target)));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SwapImpliedYieldCurveFunction" + _currency;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
