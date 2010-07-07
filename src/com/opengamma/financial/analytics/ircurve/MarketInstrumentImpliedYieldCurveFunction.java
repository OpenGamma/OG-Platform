/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeFieldContainer;

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
import com.opengamma.financial.HolidayRepository;
import com.opengamma.financial.InMemoryRegionRepository;
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
import com.opengamma.financial.interestrate.SingleCurveFinder;
import com.opengamma.financial.interestrate.SingleCurveJacobian;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.Libor;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataFieldNames;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CubicSplineInterpolatorWithSensitivities1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DWithSensitivities;
import com.opengamma.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

/**
 * 
 */
public class MarketInstrumentImpliedYieldCurveFunction extends AbstractFunction implements FunctionInvoker {
  private static final String SPOT_TICKER = "US00O/N Index"; //TODO shouldn't be hard-coded
  private static final String FLOAT_REFERENCE_TICKER = "US0006M Index"; //TODO shouldn't be hard-coded
  private final Currency _currency;
  private InterpolatedYieldAndDiscountCurveDefinition _definition;
  private UniqueIdentifier _referenceRateIdentifier;
  private ValueRequirement _referenceRateRequirement;
  private ValueRequirement _spotRateRequirement;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _curveResult;
  private ValueSpecification _jacobianResult;
  private Set<ValueSpecification> _results;
  // TODO this should not be hard-coded
  private final Interpolator1D<? extends Interpolator1DDataBundle, InterpolationResult> _interpolator = new NaturalCubicSplineInterpolator1D();
  // TODO this should depend on the type of _interpolator
  private final Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> _interpolatorWithSensitivity = new CubicSplineInterpolatorWithSensitivities1D();
  
  // TODO kirk 2010-07-05 -- Must take in a curve definition name as well, rather than hard-coding to
  // "ForwardAndFunding".

  public MarketInstrumentImpliedYieldCurveFunction(final Currency currency) {
    Validate.notNull(currency);
    _currency = currency;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final LocalDate now = executionContext.getSnapshotClock().today();
    HolidayRepository holidayRepository = OpenGammaExecutionContext.getHolidayRepository(executionContext);
    if (holidayRepository == null) {
      throw new IllegalStateException("Must have a holiday repository in the execution context");
    }
    final Calendar calendar = new HolidayRepositoryCalendarAdapter(holidayRepository, _currency);
    final Region region = OpenGammaExecutionContext.getRegionRepository(executionContext).getHierarchyNodes(now.toLocalDate(), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.ISO_CURRENCY_3, _currency.getISOCode()).iterator().next();
    //final Region region = OpenGammaExecutionContext.getRegionRepository(executionContext).getHierarchyNode(now.toLocalDate(), _currency.getUniqueIdentifier());
    final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
    final Set<FixedIncomeStrip> strips = _definition.getStrips();
    final int n = strips.size();
    final double[] marketRates = new double[n];
    final double[] initialRatesGuess = new double[n];
    final double[] nodeTimes = new double[n];
    InterestRateDerivative derivative;
    ValueRequirement stripRequirement;
    FudgeFieldContainer fudgeFields = (FudgeFieldContainer) inputs.getValue(_spotRateRequirement);
    Double rate = fudgeFields.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    if (rate == null) {
      throw new NullPointerException("Could not get spot rate for " + _currency);
    }
    final double spotRate = rate;
    fudgeFields = (FudgeFieldContainer) inputs.getValue(_referenceRateRequirement);
    rate = fudgeFields.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
    if (rate == null) {
      throw new NullPointerException("Could not get first floating rate for " + _currency);
    }
    final double referenceFloatingRate = rate;
    int i = 0;
    for (final FixedIncomeStrip strip : strips) {
      derivative = getInterestRateDerivative(strip, calendar, region, now, referenceFloatingRate);
      if (derivative == null) {
        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
      }
      derivatives.add(derivative);
      initialRatesGuess[i] = 0.01;
      stripRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      fudgeFields = (FudgeFieldContainer) inputs.getValue(stripRequirement);
      rate = fudgeFields.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD);
      if (rate == null) {
        throw new NullPointerException("Could not get market data for " + strip);
      }
      if (strip.getInstrumentType() == StripInstrument.FUTURE) {
        rate = (100. - rate) / 100.;
      } else {
        rate /= 100;
      }
      marketRates[i] = rate;
      nodeTimes[i] = getLastTime(derivative);
      i++;
    }
    final SingleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle> jacobian = new SingleCurveJacobian<Interpolator1DCubicSplineWithSensitivitiesDataBundle>(derivatives, spotRate,
        nodeTimes, _interpolatorWithSensitivity);
    final SingleCurveFinder curveFinder = new SingleCurveFinder(derivatives, marketRates, spotRate, nodeTimes, _interpolator);
    NewtonVectorRootFinder rootFinder; 
    double[] yields = null;
    try {
     rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.LU_COMMONS_NAME));
     yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    } catch(IllegalArgumentException e) {
      rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
      yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
    }
    final YieldAndDiscountCurve curve = new InterpolatedYieldCurve(nodeTimes, yields, _interpolator);
    final DoubleMatrix2D jacobianMatrix = jacobian.evaluate(new DoubleMatrix1D(yields), (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
    return Sets.newHashSet(new ComputedValue(_curveResult, curve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()));
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final InterpolatedYieldAndDiscountCurveSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
    _definition = curveSource.getDefinition(_currency, "ForwardAndFunding");
    _requirements = Collections.unmodifiableSet(buildRequirements(_definition));
    _curveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, _currency));
    _jacobianResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_AND_FORWARD_JACOBIAN, _currency));
    _results = Sets.newHashSet(_curveResult, _jacobianResult);
  }

  public Set<ValueRequirement> buildRequirements(final InterpolatedYieldAndDiscountCurveDefinition definition) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    for (final FixedIncomeStrip strip : definition.getStrips()) {
      final ValueRequirement requirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, strip.getMarketDataSpecification());
      result.add(requirement);
    }

    //TODO all of this section will need to be removed
    final String scheme = IdentificationScheme.BLOOMBERG_TICKER.getName();
    _referenceRateIdentifier = UniqueIdentifier.of(scheme, FLOAT_REFERENCE_TICKER);
    final FixedIncomeStrip referenceRate = new FixedIncomeStrip(Period.ofMonths(6), _referenceRateIdentifier, StripInstrument.LIBOR);
    final FixedIncomeStrip spotRate = new FixedIncomeStrip(Period.ofDays(1), UniqueIdentifier.of(scheme, SPOT_TICKER), StripInstrument.CASH);
    _referenceRateRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, referenceRate.getMarketDataSpecification());
    _spotRateRequirement = new ValueRequirement(ValueRequirementNames.MARKET_DATA_HEADER, spotRate.getMarketDataSpecification());
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
    return "JointFundingAndForwardYieldCurveFunction";
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
    return new Cash(t);
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
    return new ForwardRateAgreement(startTime, endTime);
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
    return new InterestRateFuture(startTime, endTime);
  }

  private Libor getLibor(final FixedIncomeStrip liborStrip, final Calendar calendar, final LocalDate now) {
    final DayCount dayCount = liborStrip.getDayCount();
    final BusinessDayConvention convention = liborStrip.getBusinessDayConvention();
    final ZonedDateTime start = liborStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime end = liborStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
    final double t = dayCount.getDayCountFraction(startAdjusted, endAdjusted);
    return new Libor(t);
  }

  private Swap getSwap(final FixedIncomeStrip swapStrip, final Calendar calendar, final Region region, final LocalDate now, final double floatingRate) {
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
    return new Swap(swapPaymentDates, swapPaymentDates, delta, delta);
  }
  private double getLastTime(final InterestRateDerivative derivative) {
    if (derivative instanceof Swap) {
      return getLastSwapTime((Swap) derivative);
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

  private double getLastSwapTime(final Swap swap) {
    final int nFix = swap.getNumberOfFixedPayments() - 1;
    final int nFloat = swap.getNumberOfFloatingPayments() - 1;
    return Math.max(swap.getFixedPaymentTimes()[nFix], swap.getFloatingPaymentTimes()[nFloat] + swap.getDeltaEnd()[nFloat]);
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
