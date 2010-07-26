/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.Region;
import com.opengamma.financial.analytics.model.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.HolidayRepositoryCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
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
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 */
public class MarketInstrumentImpliedFundingAndForwardCurveFunction extends AbstractFunction implements FunctionInvoker {
  private static final String SPOT_TICKER = "US00O/N Index"; //TODO shouldn't be hard-coded
  private static final String FLOAT_REFERENCE_TICKER = ""; //TODO shouldn't be hard-coded
  private static final String FUNDING_CURVE_NAME = "Funding Curve";
  private static final String LIBOR_CURVE_NAME = "Libor Curve";

  private final Currency _currency;
  private InterpolatedYieldAndDiscountCurveDefinition _fundingDefinition;
  private InterpolatedYieldAndDiscountCurveDefinition _forwardDefinition;
  private UniqueIdentifier _referenceRateIdentifier;
  private ValueRequirement _referenceRateRequirement;
  private ValueRequirement _spotRateRequirement;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _fundingCurveResult;
  private ValueSpecification _forwardCurveResult;
  private ValueSpecification _jacobianResult; //TODO split Jacobian into two parts
  private Set<ValueSpecification> _results;
  //TODO all interpolators should be passed in
  private final Interpolator1D<Interpolator1DCubicSplineDataBundle, InterpolationResult> _interpolator; // TODO this should not be hard-coded
  // TODO this should depend on the type of _fundingInterpolator
  private final Interpolator1D<Interpolator1DCubicSplineWithSensitivitiesDataBundle, InterpolationResultWithSensitivities> _interpolatorWithSensitivity;

  public MarketInstrumentImpliedFundingAndForwardCurveFunction(final Currency currency) {
    Validate.notNull(currency);
    _currency = currency;
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
    final LocalDate now = executionContext.getSnapshotClock().today();
    final Calendar calendar = new HolidayRepositoryCalendarAdapter(OpenGammaExecutionContext.getHolidayRepository(executionContext), _currency);
    final Region region = OpenGammaExecutionContext.getRegionRepository(executionContext).getHierarchyNode(now.toLocalDate(), _currency.getUniqueIdentifier());
    final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
    final Set<FixedIncomeStrip> fundingStrips = _fundingDefinition.getStrips();
    final int nFund = fundingStrips.size();
    final Set<FixedIncomeStrip> forwardStrips = _forwardDefinition.getStrips();
    final int nForward = forwardStrips.size();
    final double[] marketRates = new double[nFund + nForward];
    final double[] initialRatesGuess = new double[nFund + nForward];
    final double[] fundingNodeTimes = new double[nFund];
    InterestRateDerivative derivative;
    ValueRequirement stripRequirement;
    Double rate = (Double) inputs.getValue(_spotRateRequirement);
    if (rate == null) {
      throw new NullPointerException("Could not get spot rate for " + _currency);
    }
    rate = (Double) inputs.getValue(_referenceRateRequirement);
    if (rate == null) {
      throw new NullPointerException("Could not get first floating rate for " + _currency);
    }
    final double referenceFloatingRate = rate;
    int i = 0;
    for (final FixedIncomeStrip strip : fundingStrips) {
      derivative = getInterestRateDerivative(strip, calendar, region, now, referenceFloatingRate);
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = 0.05;
      stripRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
      rate = (Double) inputs.getValue(stripRequirement);
      if (rate == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      if (strip.getInstrumentType() == StripInstrument.FUTURE) {
        rate = (100. - rate) / 100.;
      }
      marketRates[i] = rate;
      fundingNodeTimes[i] = getLastTime(derivative);
      i++;
    }
    final double[] forwardNodeTimes = new double[nForward];
    int j = 0;
    for (final FixedIncomeStrip strip : forwardStrips) {
      derivative = getInterestRateDerivative(strip, calendar, region, now, referenceFloatingRate);
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = 0.05;
      stripRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
      rate = (Double) inputs.getValue(stripRequirement);
      if (rate == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      if (strip.getInstrumentType() == StripInstrument.FUTURE) {
        rate = (rate - 100.) / 100.;
      }
      marketRates[i] = rate;
      forwardNodeTimes[j] = getLastTime(derivative);
      i++;
      j++;
    }

    LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    FixedNodeInterpolator1D fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolatorWithSensitivity);
    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolatorWithSensitivity);
    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
    final JacobianCalculator jacobian = new MultipleYieldCurveFinderJacobian(derivatives, unknownCurves, null);

    unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
    fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolator);
    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolator);
    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
    final Function1D<DoubleMatrix1D,DoubleMatrix1D> curveFinder = new MultipleYieldCurveFinderFunction(derivatives, marketRates, unknownCurves, null);


    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
    final double[] yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    final double[] forwardYields = Arrays.copyOfRange(yields, 0, nForward);
    final double[] fundingYields = Arrays.copyOfRange(yields, nForward, yields.length);
    final YieldAndDiscountCurve fundingCurve = new InterpolatedYieldCurve(fundingNodeTimes, fundingYields, _interpolator);
    final YieldAndDiscountCurve forwardCurve = new InterpolatedYieldCurve(forwardNodeTimes, forwardYields, _interpolator);
    final DoubleMatrix2D jacobianMatrix = jacobian.evaluate(new DoubleMatrix1D(yields), (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
    return Sets.newHashSet(new ComputedValue(_fundingCurveResult, fundingCurve), new ComputedValue(_forwardCurveResult, forwardCurve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final InterpolatedYieldAndDiscountCurveSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
    _fundingDefinition = curveSource.getDefinition(_currency, "Funding");
    _forwardDefinition = curveSource.getDefinition(_currency, "Forward");
    _requirements = Collections.unmodifiableSet(buildRequirements(_fundingDefinition));
    _fundingCurveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, _currency));
    _forwardCurveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, _currency));
    _jacobianResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_AND_FORWARD_JACOBIAN, _currency));
    _results = Sets.newHashSet(_fundingCurveResult, _forwardCurveResult, _jacobianResult);
  }

  public Set<ValueRequirement> buildRequirements(final InterpolatedYieldAndDiscountCurveDefinition definition) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStrip strip : definition.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
      result.add(requirement);
    }

    //TODO all of this section will need to be removed
    final String scheme = IdentificationScheme.BLOOMBERG_TICKER.getName();
    _referenceRateIdentifier = UniqueIdentifier.of(scheme, FLOAT_REFERENCE_TICKER);
    final FixedIncomeStrip referenceRate = new FixedIncomeStrip(Period.ofMonths(6), _referenceRateIdentifier, StripInstrument.LIBOR);
    final FixedIncomeStrip spotRate = new FixedIncomeStrip(Period.ofDays(1), UniqueIdentifier.of(scheme, SPOT_TICKER), StripInstrument.CASH);
    _referenceRateRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, referenceRate.getMarketDataSpecification());
    _spotRateRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, spotRate.getMarketDataSpecification());
    result.add(_referenceRateRequirement);
    result.add(_spotRateRequirement);
    return result;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return ObjectUtils.equals(target.getUniqueIdentifier(), _currency.getUniqueIdentifier());
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _requirements;
    }
    return null;
  }

  @Override
  public boolean buildsOwnSubGraph() {
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return _results;
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "FundingAndForwardYieldCurveFunction";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  //TODO everything from here down is rubbish
  private InterestRateDerivative getInterestRateDerivative(final FixedIncomeStrip strip, final Calendar calendar, final Region region, final LocalDate now, final double floatingRate) {
    if (strip.getInstrumentType() == StripInstrument.SWAP) {
      return getSwap(strip, calendar, region, now, floatingRate);
    } else if (strip.getInstrumentType() == StripInstrument.CASH) {
      return getCash(strip, calendar, now);
    } else if (strip.getInstrumentType() == StripInstrument.FRA) {
      return getFRA(strip, calendar, now);
    } else if (strip.getInstrumentType() == StripInstrument.FUTURE) {
      return getIRFuture(strip, calendar, now);
    } else if (strip.getInstrumentType() == StripInstrument.LIBOR) {
      return getLibor(strip, calendar, now);
    }
    return null;
  }

  private Cash getCash(final FixedIncomeStrip cashStrip, final Calendar calendar, final LocalDate now) {
    final DayCount dayCount = cashStrip.getDayCount();
    final BusinessDayConvention convention = cashStrip.getBusinessDayConvention();
    final ZonedDateTime start = cashStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime end = cashStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
    final double t = dayCount.getDayCountFraction(startAdjusted, endAdjusted);
    return new Cash(t,FUNDING_CURVE_NAME);
  }

  private ForwardRateAgreement getFRA(final FixedIncomeStrip fraStrip, final Calendar calendar, final LocalDate now) {
    final DayCount dayCount = fraStrip.getDayCount();
    final BusinessDayConvention convention = fraStrip.getBusinessDayConvention();
    final ZonedDateTime start = fraStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime end = fraStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
    final ZonedDateTime nowWithTime = now.atStartOfDayInZone(TimeZone.UTC);
    final double startTime = dayCount.getDayCountFraction(nowWithTime, startAdjusted);
    final double endTime = dayCount.getDayCountFraction(nowWithTime, endAdjusted);
    return new ForwardRateAgreement(startTime, endTime,LIBOR_CURVE_NAME);
  }

  private InterestRateFuture getIRFuture(final FixedIncomeStrip irFutureStrip, final Calendar calendar, final LocalDate now) {
    final DayCount dayCount = irFutureStrip.getDayCount();
    final BusinessDayConvention convention = irFutureStrip.getBusinessDayConvention();
    final ZonedDateTime start = irFutureStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime end = irFutureStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
    final ZonedDateTime nowWithTime = now.atStartOfDayInZone(TimeZone.UTC);
    final double startTime = dayCount.getDayCountFraction(nowWithTime, startAdjusted);
    final double endTime = dayCount.getDayCountFraction(nowWithTime, endAdjusted);
    return new InterestRateFuture(startTime, endTime,LIBOR_CURVE_NAME);
  }

  private Libor getLibor(final FixedIncomeStrip liborStrip, final Calendar calendar, final LocalDate now) {
    final DayCount dayCount = liborStrip.getDayCount();
    final BusinessDayConvention convention = liborStrip.getBusinessDayConvention();
    final ZonedDateTime start = liborStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime end = liborStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
    final double t = dayCount.getDayCountFraction(startAdjusted, endAdjusted);
    return new Libor(t,LIBOR_CURVE_NAME);
  }

  private FixedFloatSwap getSwap(final FixedIncomeStrip swapStrip, final Calendar calendar, final Region region, final LocalDate now, final double floatingRate) {
    final BusinessDayConvention convention = swapStrip.getBusinessDayConvention();
    final ZonedDateTime effectiveDate = swapStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC); //TODO change this
    final ZonedDateTime maturityDate = swapStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC); //TODO change this
    final Frequency frequency = PeriodFrequency.SEMI_ANNUAL; //TODO change this
    final DayCount dayCount = swapStrip.getDayCount();
    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, frequency);
    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, convention, calendar);
    final double[] swapPaymentDates = ScheduleCalculator.getTimes(adjustedDates, dayCount, now.atStartOfDayInZone(TimeZone.UTC));
    final int n = swapPaymentDates.length;
    final double[] delta = new double[n];
    for (int i = 0; i < n; i++) {
      delta[i] = 0;
    }
    return new FixedFloatSwap(swapPaymentDates, swapPaymentDates, delta, delta, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
  }

  private double getLastTime(final InterestRateDerivative derivative) {
    if (derivative instanceof FixedFloatSwap) {
      return getLastSwapTime((FixedFloatSwap) derivative);
    } else if (derivative instanceof Cash) {
      return getLastCashTime((Cash) derivative);
    } else if (derivative instanceof ForwardRateAgreement) {
      return getLastFRATime((ForwardRateAgreement) derivative);
    } else if (derivative instanceof InterestRateFuture) {
      return getLastIRFutureTime((InterestRateFuture) derivative);
    } else if (derivative instanceof Libor) {
      return getLastLiborTime((Libor) derivative);
    }
    throw new IllegalArgumentException("This should never happen");
  }

  private double getLastSwapTime(final FixedFloatSwap swap) {
    final int nFix = swap.getFixedLeg().getNumberOfPayments() - 1;
    final int nFloat = swap.getFloatingLeg().getNumberOfPayments() - 1;
    return Math.max(swap.getFixedLeg().getPaymentTimes()[nFix], swap.getFloatingLeg().getPaymentTimes()[nFloat] + swap.getFloatingLeg().getDeltaEnd()[nFloat]);
  }

  private double getLastCashTime(final Cash cash) {
    return cash.getPaymentTime();
  }

  private double getLastLiborTime(final Libor libor) {
    return libor.getPaymentTime();
  }

  private double getLastFRATime(final ForwardRateAgreement fra) {
    return fra.getEndTime();
  }

  private double getLastIRFutureTime(final InterestRateFuture irFuture) {
    return irFuture.getEndTime();
  }
}
