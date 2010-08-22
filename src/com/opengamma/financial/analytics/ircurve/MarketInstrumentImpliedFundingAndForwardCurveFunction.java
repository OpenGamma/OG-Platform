/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.opengamma.engine.world.Region;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.ParRateCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.ParRateDifferenceCalculator;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.libor.definition.Libor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;

/**
 * 
 */
public class MarketInstrumentImpliedFundingAndForwardCurveFunction extends AbstractFunction implements FunctionInvoker {
  private static final String SPOT_TICKER = "US00O/N Index"; //TODO shouldn't be hard-coded
  private static final String FLOAT_REFERENCE_TICKER = ""; //TODO shouldn't be hard-coded
  private static final String FUNDING_CURVE_NAME = "Funding Curve";
  private static final String LIBOR_CURVE_NAME = "Libor Curve";

  private final Currency _currency;
  private YieldCurveDefinition _fundingDefinition;
  private YieldCurveDefinition _forwardDefinition;
  private UniqueIdentifier _referenceRateIdentifier;
  private ValueRequirement _referenceRateRequirement;
  private ValueRequirement _spotRateRequirement;
  private Set<ValueRequirement> _requirements;
  private ValueSpecification _fundingCurveResult;
  private ValueSpecification _forwardCurveResult;
  private ValueSpecification _jacobianResult; 
  private Set<ValueSpecification> _results;
//  private final Interpolator1D<? extends Interpolator1DDataBundle> _fundingInterpolator;
//  private final Interpolator1D<? extends Interpolator1DDataBundle> _forwardInterpolator;
//  private final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> _fundingSensitivityCalculator;
//  private final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> _forwardSensitivityCalculator;

  public MarketInstrumentImpliedFundingAndForwardCurveFunction(final Currency currency, String fundingInterpolatorName, String fundingLeftExtrapolatorName, String fundingRightExtrapolatorName,
      String forwardInterpolatorName, String forwardLeftExtrapolatorName, String forwardRightExtrapolatorName) {
    Validate.notNull(currency);
    _currency = currency;
 //   _fundingInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(fundingInterpolatorName, fundingLeftExtrapolatorName, fundingRightExtrapolatorName);
 //   _fundingSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator(fundingInterpolatorName, fundingLeftExtrapolatorName, 
//        fundingRightExtrapolatorName);
 //   _forwardInterpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(forwardInterpolatorName, forwardLeftExtrapolatorName, forwardRightExtrapolatorName);
 //   _forwardSensitivityCalculator = CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory.getSensitivityCalculator(forwardInterpolatorName, forwardLeftExtrapolatorName, 
 //       forwardRightExtrapolatorName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
//    final LocalDate now = executionContext.getSnapshotClock().today();
//    final Calendar calendar = new HolidayRepositoryCalendarAdapter(OpenGammaExecutionContext.getHolidayRepository(executionContext), _currency);
//    final Region region = OpenGammaExecutionContext.getRegionRepository(executionContext).getHierarchyNode(now.toLocalDate(), _currency.getUniqueIdentifier());
//    final List<InterestRateDerivative> derivatives = new ArrayList<InterestRateDerivative>();
//    final Set<FixedIncomeStrip> fundingStrips = _fundingDefinition.getStrips();
//    final int nFund = fundingStrips.size();
//    final Set<FixedIncomeStrip> forwardStrips = _forwardDefinition.getStrips();
//    final int nForward = forwardStrips.size();
//    final double[] marketRates = new double[nFund + nForward];
//    final double[] initialRatesGuess = new double[nFund + nForward];
//    final double[] fundingNodeTimes = new double[nFund];
//    InterestRateDerivative derivative;
//    ValueRequirement stripRequirement;
//    Double rate = (Double) inputs.getValue(_spotRateRequirement);
//    if (rate == null) {
//      throw new NullPointerException("Could not get spot rate for " + _currency);
//    }
//    rate = (Double) inputs.getValue(_referenceRateRequirement);
//    if (rate == null) {
//      throw new NullPointerException("Could not get first floating rate for " + _currency);
//    }
//    //final double referenceFloatingRate = rate;
//    int i = 0;
//    for (final ResolvedFixedIncomeStrip strip : fundingStrips) {     
//      stripRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
//      rate = (Double) inputs.getValue(stripRequirement);
//      if (rate == null) {
//        throw new NullPointerException("Could not get market data for " + strip);
//      }
//      if (strip.getInstrumentType() != StripInstrumentType.FUTURE) {
//        rate = rate / 100.;
//      }
//      derivative = getInterestRateDerivative(strip, calendar, region, now,rate);
//      if (derivative == null) {
//        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
//      }
//      derivatives.add(derivative);
//      initialRatesGuess[i] = 0.05;
//      
//      fundingNodeTimes[i] = getLastTime(derivative);
//      i++;
//    }
//    final double[] forwardNodeTimes = new double[nForward];
//    
//    int j = 0;
//    for (final FixedIncomeStrip strip : forwardStrips) {
//      
//      stripRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
//      rate = (Double) inputs.getValue(stripRequirement);
//      if (rate == null) {
//        throw new NullPointerException("Could not get market data for " + strip);
//      }
//      if (strip.getInstrumentType() != StripInstrumentType.FUTURE) {
//        rate = rate/100.;
//      }
//      derivative = getInterestRateDerivative(strip, calendar, region, now,rate);
//      if (derivative == null) {
//        throw new NullPointerException("Had a null InterestRateDefinition for " + strip);
//      }
//      derivatives.add(derivative);
//      initialRatesGuess[i] = 0.05;
//      
//      marketRates[i] = rate;
//      forwardNodeTimes[j] = getLastTime(derivative);
//      i++;
//      j++;
//    }

//    LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
//    FixedNodeInterpolator1D fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolatorWithSensitivity);
//    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
//    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolatorWithSensitivity);
//    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
//    final JacobianCalculator jacobian = new MultipleYieldCurveFinderJacobian(derivatives, unknownCurves, null,ParRateCurveSensitivityCalculator.getInstance());
//
//    unknownCurves = new LinkedHashMap<String, FixedNodeInterpolator1D>();
//    fnInterpolator = new FixedNodeInterpolator1D(forwardNodeTimes, _interpolator);
//    unknownCurves.put(LIBOR_CURVE_NAME, fnInterpolator);
//    fnInterpolator = new FixedNodeInterpolator1D(fundingNodeTimes, _interpolator);
//    unknownCurves.put(FUNDING_CURVE_NAME, fnInterpolator);
//    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveFinder = new MultipleYieldCurveFinderFunction(derivatives, unknownCurves, null,ParRateDifferanceCalculator.getInstance());
//
//
//    final NewtonVectorRootFinder rootFinder = new BroydenVectorRootFinder(1e-7, 1e-7, 100, jacobian, DecompositionFactory.getDecomposition(DecompositionFactory.SV_COMMONS_NAME));
//    final double[] yields = rootFinder.getRoot(curveFinder, new DoubleMatrix1D(initialRatesGuess)).getData();
//    final double[] forwardYields = Arrays.copyOfRange(yields, 0, nForward);
//    final double[] fundingYields = Arrays.copyOfRange(yields, nForward, yields.length);
//    final YieldAndDiscountCurve fundingCurve = new InterpolatedYieldCurve(fundingNodeTimes, fundingYields, _interpolator);
//    final YieldAndDiscountCurve forwardCurve = new InterpolatedYieldCurve(forwardNodeTimes, forwardYields, _interpolator);
//    final DoubleMatrix2D jacobianMatrix = jacobian.evaluate(new DoubleMatrix1D(yields), (Function1D<DoubleMatrix1D, DoubleMatrix1D>[]) null);
//    return Sets.newHashSet(new ComputedValue(_fundingCurveResult, fundingCurve), new ComputedValue(_forwardCurveResult, forwardCurve), new ComputedValue(_jacobianResult, jacobianMatrix.getData()));
    return null;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
//    final InterpolatedYieldCurveDefinitionSource curveSource = OpenGammaCompilationContext.getDiscountCurveSource(context);
//    _fundingDefinition = curveSource.getDefinition(_currency, "Funding");
//    _forwardDefinition = curveSource.getDefinition(_currency, "Forward");
////    _requirements = Collections.unmodifiableSet(buildRequirements(_fundingDefinition));
//    _fundingCurveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, _currency));
//    _forwardCurveResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, _currency));
//    _jacobianResult = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FUNDING_AND_FORWARD_JACOBIAN, _currency));
//    _results = Sets.newHashSet(_fundingCurveResult, _forwardCurveResult, _jacobianResult);
  }

//  public Set<ValueRequirement> buildRequirements(final YieldCurveDefinition definition) {
//    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
//    for (final FixedIncomeStrip strip : definition.getStrips()) {
//      final ValueRequirement requirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, strip.getMarketDataSpecification());
//      result.add(requirement);
//    }
//
//    //TODO all of this section will need to be removed
//    final String scheme = IdentificationScheme.BLOOMBERG_TICKER.getName();
//    _referenceRateIdentifier = UniqueIdentifier.of(scheme, FLOAT_REFERENCE_TICKER);
//    final FixedIncomeStrip referenceRate = new FixedIncomeStrip(Period.ofMonths(6), _referenceRateIdentifier, StripInstrumentType.LIBOR);
//    final FixedIncomeStrip spotRate = new FixedIncomeStrip(Period.ofDays(1), UniqueIdentifier.of(scheme, SPOT_TICKER), StripInstrumentType.CASH);
//    _referenceRateRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, referenceRate.getMarketDataSpecification());
//    _spotRateRequirement = new ValueRequirement(MarketDataRequirementNames.INDICATIVE_VALUE, spotRate.getMarketDataSpecification());
//    result.add(_referenceRateRequirement);
//    result.add(_spotRateRequirement);
//    return result;
//  }

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

//  //TODO everything from here down is rubbish
//  private InterestRateDerivative getInterestRateDerivative(final FixedIncomeStrip strip, final Calendar calendar, final Region region, final LocalDate now, final double rateOrPrice) {
//    if (strip.getInstrumentType() == StripInstrument.SWAP) {
//      return getSwap(strip, calendar, region, now, rateOrPrice);
//    } else if (strip.getInstrumentType() == StripInstrument.CASH) {
//      return getCash(strip, calendar, now,rateOrPrice);
//    } else if (strip.getInstrumentType() == StripInstrument.FRA) {
//      return getFRA(strip, calendar, now,rateOrPrice);
//    } else if (strip.getInstrumentType() == StripInstrument.FUTURE) {
//      return getIRFuture(strip, calendar, now,rateOrPrice);
//    } else if (strip.getInstrumentType() == StripInstrument.LIBOR) {
//      return getLibor(strip, calendar, now,rateOrPrice);
//    }
//    return null;
//  }
//
//  private Cash getCash(final FixedIncomeStrip cashStrip, final Calendar calendar, final LocalDate now, final double cashRate) {
//    final DayCount dayCount = cashStrip.getDayCount();
//    final BusinessDayConvention convention = cashStrip.getBusinessDayConvention();
//    final ZonedDateTime start = cashStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime end = cashStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
//    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
//    final double t = dayCount.getDayCountFraction(startAdjusted, endAdjusted);
//
//    return new Cash(t, cashRate, FUNDING_CURVE_NAME);
//
//  }
//
//  private ForwardRateAgreement getFRA(final FixedIncomeStrip fraStrip, final Calendar calendar, final LocalDate now, final double strike) {
//    final DayCount dayCount = fraStrip.getDayCount();
//    final BusinessDayConvention convention = fraStrip.getBusinessDayConvention();
//    final ZonedDateTime start = fraStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime end = fraStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
//    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
//    final ZonedDateTime nowWithTime = now.atStartOfDayInZone(TimeZone.UTC);
//    final double startTime = dayCount.getDayCountFraction(nowWithTime, startAdjusted);
//    final double endTime = dayCount.getDayCountFraction(nowWithTime, endAdjusted);
//
//    return new ForwardRateAgreement(startTime, endTime, strike, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
//
//  }
//
//  private InterestRateFuture getIRFuture(final FixedIncomeStrip irFutureStrip, final Calendar calendar, final LocalDate now, final double price) {
//    final DayCount dayCount = irFutureStrip.getDayCount();
//    final BusinessDayConvention convention = irFutureStrip.getBusinessDayConvention();
//    final ZonedDateTime start = irFutureStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime end = irFutureStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
//    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
//    final ZonedDateTime nowWithTime = now.atStartOfDayInZone(TimeZone.UTC);
//    final double startTime = dayCount.getDayCountFraction(nowWithTime, startAdjusted);
//    final double endTime = dayCount.getDayCountFraction(nowWithTime, endAdjusted);
//
//    return new InterestRateFuture(startTime, endTime - startTime, price, LIBOR_CURVE_NAME);
//
//  }
//
//  private Libor getLibor(final FixedIncomeStrip liborStrip, final Calendar calendar, final LocalDate now, final double liborRate) {
//    final DayCount dayCount = liborStrip.getDayCount();
//    final BusinessDayConvention convention = liborStrip.getBusinessDayConvention();
//    final ZonedDateTime start = liborStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime end = liborStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC);
//    final ZonedDateTime startAdjusted = convention.adjustDate(calendar, start);
//    final ZonedDateTime endAdjusted = convention.adjustDate(calendar, end);
//    final double t = dayCount.getDayCountFraction(startAdjusted, endAdjusted);
//
//    return new Libor(t, liborRate, LIBOR_CURVE_NAME);
//
//  }
//
//  private FixedFloatSwap getSwap(final FixedIncomeStrip swapStrip, final Calendar calendar, final Region region, final LocalDate now, final double swapRate) {
//    final BusinessDayConvention convention = swapStrip.getBusinessDayConvention();
//    final ZonedDateTime effectiveDate = swapStrip.getStartDate().atStartOfDayInZone(TimeZone.UTC); //TODO change this
//    final ZonedDateTime maturityDate = swapStrip.getEndDate().atStartOfDayInZone(TimeZone.UTC); //TODO change this
//    final Frequency frequency = PeriodFrequency.SEMI_ANNUAL; //TODO change this
//    final DayCount dayCount = swapStrip.getDayCount();
//    final ZonedDateTime[] unadjustedDates = ScheduleCalculator.getUnadjustedDateSchedule(effectiveDate, maturityDate, frequency);
//    final ZonedDateTime[] adjustedDates = ScheduleCalculator.getAdjustedDateSchedule(unadjustedDates, convention, calendar);
//    final double[] swapPaymentDates = ScheduleCalculator.getTimes(adjustedDates, dayCount, now.atStartOfDayInZone(TimeZone.UTC));
//    final int n = swapPaymentDates.length;
//    final double[] delta = new double[n];
//    for (int i = 0; i < n; i++) {
//      delta[i] = 0;
//    }
//    return new FixedFloatSwap(swapPaymentDates,  swapPaymentDates, swapRate,delta, delta, FUNDING_CURVE_NAME, LIBOR_CURVE_NAME);
//  }
  //TODO everything from here down is rubbish
 
}