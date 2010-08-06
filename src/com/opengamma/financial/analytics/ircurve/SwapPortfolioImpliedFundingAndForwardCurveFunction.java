/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.ParRateCalculatorTest;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.ParRateDifferanceCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.Extrapolator1D;
import com.opengamma.math.interpolation.ExtrapolatorMethod;
import com.opengamma.math.interpolation.FixedNodeInterpolator1D;
import com.opengamma.math.interpolation.FlatExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolatorWithSensitivities;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineDataBundle;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.LinearExtrapolator;
import com.opengamma.math.interpolation.LinearExtrapolatorWithSensitivity;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

/**
 * TODO Delete this file
 */

public class SwapPortfolioImpliedFundingAndForwardCurveFunction extends AbstractFunction implements FunctionInvoker {
  private static final String FUNDING_CURVE_NAME = "Funding Curve";
  private static final String LIBOR_CURVE_NAME = "Libor Curve";
  private final Currency _currency;
  //TODO all interpolators should be passed in
  private final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> _interpolator; // TODO this should not be hard-coded
  // TODO this should depend on the type of _fundingInterpolator
  private final Interpolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> _interpolatorWithSensitivity;
  private final ParRateCalculator _swapRateCalculator = ParRateCalculator.getInstance();
  private final String _fundingCurveName;
  private final String _forwardCurveName;

  public SwapPortfolioImpliedFundingAndForwardCurveFunction(final Currency currency) {
    Validate.notNull(currency);
    _currency = currency;
    // TODO these should depend on what will be parameters: rootfinder type and jacobian calculation method
    _fundingCurveName = ValueRequirementNames.YIELD_CURVE + "_" + _currency + "_Broyden_AnalyticJacobian_FundingCurve";
    _forwardCurveName = ValueRequirementNames.YIELD_CURVE + "_" + _currency + "_Broyden_AnalyticJacobian_ForwardCurve";
    final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> cubicInterpolator = new NaturalCubicSplineInterpolator1D();
    final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> cubicInterpolatorWithSense = new CubicSplineInterpolatorWithSensitivities1D();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> linearExtrapolator = new LinearExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineDataBundle, InterpolationResult> flatExtrapolator = new FlatExtrapolator<Interpolator1DCubicSplineDataBundle, InterpolationResult>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> linearExtrapolatorWithSensitivities =
      new LinearExtrapolatorWithSensitivity<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    final ExtrapolatorMethod<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> flatExtrapolatorWithSensitivities =
      new FlatExtrapolatorWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>();
    _interpolator = new Extrapolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult>(linearExtrapolator, flatExtrapolator, cubicInterpolator);
    _interpolatorWithSensitivity = new Extrapolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities>(linearExtrapolatorWithSensitivities,
        flatExtrapolatorWithSensitivities, cubicInterpolatorWithSense);
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
//    final double[] marketRates = new double[numberOfSwaps];
    final double[] fundingNodeTimes = new double[fundingCurve.getMaturities().size()];
    final double[] forwardNodeTimes = new double[forwardCurve.getMaturities().size()];
    final double[] initialRatesGuess = new double[numberOfSwaps]; // TODO is this where the fixed rate should live?
    int i = 0, nFix, nFloat;
    // final ZonedDateTime[] referenceRatePaymentDates; TODO use this when the offsets are actually calculated
    for (final Position position : node.getPositions()) {
      swapSecurity = (SwapSecurity) position;
      payLeg = swapSecurity.getPayLeg();
      receiveLeg = swapSecurity.getReceiveLeg();
      effectiveDate = swapSecurity.getEffectiveDate().toZonedDateTime();
      maturityDate = swapSecurity.getMaturityDate().toZonedDateTime();
      final double[] payLegPaymentTimes = SwapScheduleCalculator.getPaymentTimes(effectiveDate, maturityDate, payLeg, calendar, now);
      final double[] receiveLegPaymentTimes = SwapScheduleCalculator.getPaymentTimes(effectiveDate, maturityDate, receiveLeg, calendar, now);
      payFixed = payLeg instanceof FixedInterestRateLeg;
      FixedFloatSwap swap;
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
      
      //TODO This does not have a swap value
      swap = new FixedFloatSwap(fixedPaymentTimes,floatPaymentTimes, 0.0,forwardStartOffsets, forwardEndOffsets,FUNDING_CURVE_NAME,LIBOR_CURVE_NAME);
      YieldCurveBundle bundle = new YieldCurveBundle();

      bundle.setCurve(FUNDING_CURVE_NAME, fundingCurve);
      bundle.setCurve(LIBOR_CURVE_NAME, forwardCurve);
      // swap rates from bbg
   
      fundingNodeTimes[i] = Math.max(fixedPaymentTimes[nFix - 1], floatPaymentTimes[nFloat - 1] + forwardEndOffsets[nFloat - 1]);
      // forwardNodeTimes[i] = something
      initialRatesGuess[i] = 0.05;
      swaps.add(swap);
      i++;
    }


    LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    FixedNodeInterpolator1D fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolatorWithSensitivity);
    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolatorWithSensitivity);
    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
    final JacobianCalculator jacobian = new MultipleYieldCurveFinderJacobian(swaps, unknownCurves, null,ParRateCurveSensitivityCalculator.getInstance());

    unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolator);
    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolator);
    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveFinder = new MultipleYieldCurveFinderFunction(swaps,unknownCurves, null,ParRateDifferanceCalculator.getInstance());


    // TODO this should not be hard-coded
    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
    final double[] yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    final YieldAndDiscountCurve fundingResult = new InterpolatedYieldCurve(fundingNodeTimes, yields, _interpolator);
    final YieldAndDiscountCurve forwardResult = new InterpolatedYieldCurve(forwardNodeTimes, yields, _interpolator);
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
