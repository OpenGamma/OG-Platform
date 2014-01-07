/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSDefinition;
import com.opengamma.analytics.financial.instrument.cds.ISDACDSPremiumDefinition;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * A pricing algorithm that approximates the ISDA standard model.
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * @deprecated Use classes from isdastandardmodel
 */
@Deprecated
public class ISDAApproxCDSPricingMethod {

  private static final double PRICING_TIME = 0.0;
  private static final double ONE_DAY_ACT_365F = 1.0 / 365.0;
  private static final double HALF_DAY_ACT_365F = 0.5 / 365.0;

  // The root finder parameters differ from those used in the ISDA code
  // However they result in stable solving of the hazard rate such that all ISDA test cases pass
  private static final double HAZARD_SOLVER_LOWER_BOUND = 0;
  private static final double HAZARD_SOLVER_UPPER_BOUND = 10;
  private static final double HAZARD_SOLVER_INITIAL_STEP = 0.0005;
  private static final double HAZARD_SOLVER_INITIAL_DERIVATIVE = 0.0;
  private static final double HAZARD_SOLVER_TOLERANCE = 1e-17;

  private static final ISDARootFinder HAZARD_SOLVER = new ISDARootFinder(HAZARD_SOLVER_TOLERANCE);

  // ISDA uses time lines extensively in pricing
  // A time line describes a series of t values, which are the points to consider when evaluating an integral numerically
  // As an optimisation, the discount factors for those points are also calculated and stored
  private static class Timeline {
    private final double[] _timePoints;
    private final double[] _discountFactors;

    public Timeline(final double[] timePoints, final double[] discountFactors) {
      _timePoints = timePoints;
      _discountFactors = discountFactors;
    }

    public double[] getTimePoints() {
      return _timePoints;
    }

    public double[] getDiscountFactors() {
      return _discountFactors;
    }
  }

  /**
   * Calculate the up-front charge of for a CDS according to the ISDA model,
   * given ISDA representations of the discount curve and a flat par spread from the market.
   *
   * The pricing, step-in and settlement dates need to be passed in as they are used to build the bootstrap instrument.
   *
   * @param cds The CDS to be valued
   * @param discountCurve The discount curve
   * @param flatSpread The flat par spread from the market
   * @param cleanPrice Whether the price is clean (true) or dirty (false)
   * @param pricingDate The pricing date
   * @param stepinDate The step-in date
   * @param settlementDate The settlement date
   * @param calendar The calendar
   * @return The clean or dirty price of the CDS contract, depending on the cleanPrice flag
   */
  public double calculateUpfrontCharge(final ISDACDSDerivative cds, final ISDACurve discountCurve, final double flatSpread, final boolean cleanPrice, final ZonedDateTime pricingDate,
      final ZonedDateTime stepinDate, final ZonedDateTime settlementDate, final Calendar calendar) {

    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
    final double offsetPricingTime = -offset;
    final double offsetStepinTime = cds.getStepinTime() - offset;
    final double offsetMaturityTime = cds.getMaturity() + offset;
    final double protectionStartTime = Math.max(Math.max(cds.getStartTime(), offsetStepinTime), offsetPricingTime);

    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    final Timeline accrualTimeline = buildTimeline(cds, discountCurve, null, cds.getStartTime(), offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, null, protectionStartTime, cds.getMaturity(), false);

    final double settlementDiscountFactor = discountCurve.getDiscountFactor(cds.getSettlementTime());
    final double stepinDiscountFactor = offsetStepinTime > 0.0 ? discountCurve.getDiscountFactor(offsetStepinTime) : discountCurve.getDiscountFactor(0.0);

    final double[] timePoints = {cds.getMaturity() };
    final double[] dataPoints = {flatSpread };

    final ISDACDSDefinition bootstrapCDSDefinition = makeBootstrapCDSDefinition(cds, flatSpread, calendar);
    final ISDACDSDerivative bootstrapCDS = bootstrapCDSDefinition.toDerivative(pricingDate, stepinDate, settlementDate, cds.getDiscountCurveName(), cds.getSpreadCurveName());

    final double guess = dataPoints[0] / (1.0 - cds.getRecoveryRate());

    dataPoints[0] = HAZARD_SOLVER.findRoot(new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        dataPoints[0] = x;
        final ISDACurve tempCurve = new ISDACurve(cds.getSpreadCurveName(), timePoints, dataPoints, 0.0);
        return valueCDS(bootstrapCDS, tempCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, true);
      }
    }, guess, HAZARD_SOLVER_LOWER_BOUND, HAZARD_SOLVER_UPPER_BOUND, HAZARD_SOLVER_INITIAL_STEP, HAZARD_SOLVER_INITIAL_DERIVATIVE);

    // In some cases the solver can diverge and report a root found at the upper bound
    // This needs to be reported as an error
    if (dataPoints[0] == HAZARD_SOLVER_UPPER_BOUND) {
      throw new OpenGammaRuntimeException("Failed to converge finding hazard rate");
    }

    final ISDACurve hazardRateCurve = new ISDACurve(cds.getSpreadCurveName(), timePoints, dataPoints, 0.0);

    return valueCDS(cds, hazardRateCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, cleanPrice);
  }

  /**
   * Calculate the up-front charge of for a CDS according to the ISDA model,
   * given ISDA representations of the discount curve and hazard rate function.
   *
   * @param cds The CDS to be valued
   * @param discountCurve The discount curve
   * @param hazardRateCurve The credit spread curve
   * @param cleanPrice Whether the price is clean (true) or dirty (false)
   * @return The clean or dirty price of the CDS contract, depending on the cleanPrice flag
   */
  public double calculateUpfrontCharge(final ISDACDSDerivative cds, final ISDACurve discountCurve, final ISDACurve hazardRateCurve, final boolean cleanPrice) {

    // Offset time values
    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;
    final double offsetPricingTime = -offset;
    final double offsetStepinTime = cds.getStepinTime() - offset;
    final double offsetMaturityTime = cds.getMaturity() + offset;
    final double protectionStartTime = Math.max(Math.max(cds.getStartTime(), offsetStepinTime), offsetPricingTime);

    // Constant time lines used in pricing
    final Timeline paymentTimeline = buildPaymentTimeline(cds, discountCurve);
    final Timeline accrualTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, cds.getStartTime(), offsetMaturityTime, true);
    final Timeline contingentTimeline = buildTimeline(cds, discountCurve, hazardRateCurve, protectionStartTime, cds.getMaturity(), false);

    // Constant discount factors used in pricing
    final double settlementDiscountFactor = discountCurve.getDiscountFactor(cds.getSettlementTime());
    final double stepinDiscountFactor = offsetStepinTime > 0.0 ? discountCurve.getDiscountFactor(offsetStepinTime) : discountCurve.getDiscountFactor(0.0);

    // Now value CDS, discount curve not needed since discount factors remain constant and are pre-computed
    return valueCDS(cds, hazardRateCurve, paymentTimeline, accrualTimeline, contingentTimeline, offsetStepinTime, stepinDiscountFactor, settlementDiscountFactor, cleanPrice);
  }

  /**
   * Build a CDS instrument to use for bootstrapping the hazard rate function when a flat spread is assumed.
   *
   * In this case most parameters of the bootstrap instrument, particularly the maturity date, are the same
   * as the instrument being priced.
   *
   * @param cds The CDS being priced
   * @param parSpread Flat par spread from the market
   * @return A CDS instrument suitable for bootstrapping the hazard rate function
   */
  private ISDACDSDefinition makeBootstrapCDSDefinition(final ISDACDSDerivative cds, final double parSpread, final Calendar calendar) {

    // These values are hard-coded in the ISDA C code when building the bootstrap instrument
    final double notional = 1.0;
    final boolean protectStart = true;
    final boolean payOnDefault = true;

    final ISDACDSCoupon[] premiums = cds.getPremium().getPayments();
    final ZonedDateTime startDate = premiums[0].getAccrualStartDate();
    final ZonedDateTime maturity = premiums[premiums.length - 1].getAccrualEndDate();

    final ISDACDSPremiumDefinition premiumDefinition = ISDACDSPremiumDefinition.from(startDate, maturity, cds.getCouponFrequency(), cds.getConvention(), cds.getStubType(), cds.isProtectStart(),
        notional, parSpread, cds.getPremium().getCurrency(), calendar);

    return new ISDACDSDefinition(startDate, maturity, premiumDefinition, notional, parSpread, cds.getRecoveryRate(), cds.isAccrualOnDefault(), payOnDefault, protectStart, cds.getCouponFrequency(),
        cds.getConvention(), cds.getStubType());
  }

  /**
   * Value a CDS contract according to the ISDA model, given pre-computed time-lines, discount factors and hazard-rate function.
   *
   * Note the clean price can be found by subtracting accrued premium at the pricing point from the dirty price. This value is
   * available in the ISDACDSDerivative object ({@link ISDACDSDerivative#getAccruedInterest()}).
   *
   * @param cds The CDS being valued
   * @param hazardRateCurve A representation of the hazard rate function
   * @param paymentTimeline The timeline for fee payments
   * @param accrualTimeline The timeline used to compute accrual on default for the fee leg
   * @param contingentTimeline The timeline used to price the contingent leg
   * @param stepinTime The step-in time
   * @param stepinDiscountFactor The discount factor at the step-in time
   * @param settlementDiscountFactor The discount factor at the settlement time
   * @param cleanPrice Whether to produce a clean price (true) or a dirty price (false)
   * @return The value of the CDS contract, according to the ISDA model.
   */
  private double valueCDS(final ISDACDSDerivative cds, final ISDACurve hazardRateCurve, final Timeline paymentTimeline, final Timeline accrualTimeline, final Timeline contingentTimeline,
      final double stepinTime, final double stepinDiscountFactor, final double settlementDiscountFactor, final boolean cleanPrice) {

    if (stepinTime < PRICING_TIME) {
      throw new OpenGammaRuntimeException("Cannot value a CDS with step-in date before pricing date");
    }

    final double contingentLeg = valueContingentLeg(cds, contingentTimeline, hazardRateCurve, settlementDiscountFactor);
    final double feeLeg = valueFeeLeg(cds, paymentTimeline, accrualTimeline, hazardRateCurve, stepinTime, stepinDiscountFactor, settlementDiscountFactor);
    final double dirtyPrice = (contingentLeg - feeLeg) * cds.getNotional();

    return cleanPrice ? dirtyPrice + cds.getAccruedInterest() : dirtyPrice;
  }

  /**
   * Value the premium leg of a CDS taken from the step-in date.
   * @param cds The CDS contract being priced
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param stepinDate The step-in date
   * @return PV of the CDS premium leg
   */
  private double valueFeeLeg(final ISDACDSDerivative cds, final Timeline paymentTimeline, final Timeline accrualTimeline, final ISDACurve hazardRateCurve, final double stepinTime,
      final double stepinDiscountFactor, final double settlementDiscountFactor) {

    // If the "protect start" flag is set, then the start date of the CDS is protected and observations are made at the start
    // of the day rather than the end. This is modelled by shifting all period start/end dates one day forward,
    // and adding one extra day's protection to the final period

    final ISDACDSCoupon[] coupons = cds.getPremium().getPayments();
    final int maturityIndex = coupons.length - 1;
    final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;

    ISDACDSCoupon payment;
    double periodEndTime;
    double amount, survival, discount;
    double result = 0.0;

    int startIndex, endIndex = 0;

    for (int i = 0; i < coupons.length; i++) {

      payment = coupons[i];
      periodEndTime = payment.getAccrualEndTime() - (i < maturityIndex ? offset : 0.0);

      amount = payment.getFixedRate() * payment.getPaymentYearFraction();
      survival = hazardRateCurve.getDiscountFactor(periodEndTime);
      discount = paymentTimeline.getDiscountFactors()[i];
      result += amount * survival * discount;

      if (cds.isAccrualOnDefault()) {

        startIndex = endIndex;
        while (accrualTimeline.getTimePoints()[endIndex] < periodEndTime) {
          ++endIndex;
        }

        result += valueFeeLegAccrualOnDefault(amount, accrualTimeline, hazardRateCurve, startIndex, endIndex, stepinTime, stepinDiscountFactor);
      }
    }

    return result / settlementDiscountFactor;
  }

  /**
   * Calculate the accrual-on-default portion of the PV for a specified accrual period.
   *
   * @param amount Amount of premium that would be accrued over the entire period (in the case of no default)
   * @param timeline The accrual time line, covers the entire CDS (excluding past accrual periods), not just the accrual period being evaluated
   * @param hazardRateCurve Curve describing the hazard rate function
   * @param startIndex Index in to the time line for the start of the current accrual period
   * @param endIndex Index in to the time line for the end of the current accrual period
   * @param stepinTime Step-in time for the CDS contract
   * @param stepinDiscountFactor Associated discount factor for the step-in time
   * @return Accrual-on-default portion of PV for the accrual period
   */
  private double valueFeeLegAccrualOnDefault(final double amount, final Timeline timeline, final ISDACurve hazardRateCurve, final int startIndex, final int endIndex, final double stepinTime,
      final double stepinDiscountFactor) {

    final double[] timePoints = timeline.getTimePoints();
    final double[] discountFactors = timeline.getDiscountFactors();

    final double startTime = timePoints[startIndex];
    final double endTime = timePoints[endIndex];
    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    final double accrualRate = amount / (endTime - startTime);

    double t0, t1, dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;

    t0 = subStartTime - startTime + HALF_DAY_ACT_365F;
    survival0 = hazardRateCurve.getDiscountFactor(subStartTime);
    discount0 = startTime < stepinTime || startTime < PRICING_TIME ? stepinDiscountFactor : discountFactors[startIndex];

    value = 0.0;

    for (int i = startIndex + 1; i <= endIndex; ++i) {

      if (timePoints[i] <= stepinTime) {
        continue;
      }

      t1 = timePoints[i] - startTime + HALF_DAY_ACT_365F;
      dt = t1 - t0;

      survival1 = hazardRateCurve.getDiscountFactor(timePoints[i]);
      discount1 = discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * survival0 * discount0 *
          (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

      value += valueForTimeStep;

      t0 = t1;
      survival0 = survival1;
      discount0 = discount1;
    }

    return value;
  }

  /**
   * Value the contingent leg of a CDS taken from the step-in date.
   *
   * @param cds The derivative object representing the CDS
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV of the CDS default leg
   */
  private double valueContingentLeg(final ISDACDSDerivative cds, final Timeline contingentTimeline, final ISDACurve hazardRateCurve, final double settlementDiscountFactor) {

    // The ISDA C code always forces pay on default; code is available for pay on maturity but never used
    final double recoveryRate = cds.getRecoveryRate();
    final double value = cds.isPayOnDefault() ? valueContingentLegPayOnDefault(recoveryRate, contingentTimeline, hazardRateCurve) : valueContingentLegPayOnMaturity(recoveryRate, contingentTimeline,
        hazardRateCurve);

    return value / settlementDiscountFactor;
  }

  /**
   * Value the default leg, assuming any possible payout is received at the time of default.
   *
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param timeline The contingent leg time line, bounded by startTime and maturity
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnDefault(final double recoveryRate, final Timeline timeline, final ISDACurve hazardRateCurve) {

    final double[] timePoints = timeline.getTimePoints();
    final double[] discountFactors = timeline.getDiscountFactors();

    final double maturity = timePoints[timePoints.length - 1];
    final double loss = 1.0 - recoveryRate;

    if (maturity < PRICING_TIME) {
      return 0.0;
    }

    double dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, valueForTimeStep, value;

    survival1 = hazardRateCurve.getDiscountFactor(timePoints[0]);
    discount1 = timePoints[0] > PRICING_TIME ? discountFactors[0] : 1.0;
    value = 0.0;

    for (int i = 1; i < timePoints.length; ++i) {

      dt = timePoints[i] - timePoints[i - 1];

      survival0 = survival1;
      discount0 = discount1;
      survival1 = hazardRateCurve.getDiscountFactor(timePoints[i]);
      discount1 = discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      valueForTimeStep = ((loss * lambda) / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * dt)) * survival0 * discount0;

      value += valueForTimeStep;
    }

    return value;
  }

  /**
   * Value the default leg, assuming any possible payout is received at maturity.
   *
   * @param recoveryRate Recovery rate of the CDS underlying asset
   * @param timeline The contingent leg time line, bounded by startTime and maturity
   * @param hazardRateCurve Curve describing the hazard rate function
   * @return PV for the default leg
   */
  private double valueContingentLegPayOnMaturity(final double recoveryRate, final Timeline timeline, final ISDACurve hazardRateCurve) {

    final int maturityIndex = timeline.getTimePoints().length - 1;

    if (timeline.getTimePoints()[maturityIndex] < PRICING_TIME) {
      return 0.0;
    }

    final double loss = 1.0 - recoveryRate;
    final double survival0 = hazardRateCurve.getDiscountFactor(timeline.getTimePoints()[0]);
    final double survival1 = hazardRateCurve.getDiscountFactor(timeline.getTimePoints()[maturityIndex]);
    final double discount = timeline.getDiscountFactors()[maturityIndex];

    return (survival0 - survival1) * discount * loss;
  }

  /**
   * Build a timeline based on the discount and hazard curves, and optional the CDS schedule.
   *
   * The resulting timeline object contains t values for every point on both curves, and optionally
   * for the start and end time of every accrual period. Discount factors for each time point are
   * computed and stored. The timeline is truncated to the specified start and end times (these
   * time points are included in the timeline).
   *
   * @param cds The CDS being priced
   * @param discountCurve The discount curve
   * @param hazardRateCurve The hazard curve
   * @param startTime The first point on the timeline
   * @param endTime The last point on the timeline
   * @param includeSchedule Whether to include the CDS accrual period start/end times
   * @return The populated timeline object
   */
  private Timeline buildTimeline(final ISDACDSDerivative cds, final ISDACurve discountCurve, final ISDACurve hazardRateCurve, final double startTime, final double endTime,
      final boolean includeSchedule) {

    final NavigableSet<Double> allTimePoints = new TreeSet<Double>();

    final double[] discountCurveTimePoints = discountCurve.getTimePoints();
    for (final double discountCurveTimePoint : discountCurveTimePoints) {
      allTimePoints.add(new Double(discountCurveTimePoint));
    }

    if (hazardRateCurve != null) {
      final double[] hazardRateCurveTimePoints = hazardRateCurve.getTimePoints();
      for (final double hazardRateCurveTimePoint : hazardRateCurveTimePoints) {
        allTimePoints.add(Double.valueOf(hazardRateCurveTimePoint));
      }
    } else {
      allTimePoints.add(new Double(cds.getMaturity()));
    }

    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));

    Set<Double> timePointsInRange;

    if (includeSchedule) {

      final ISDACDSCoupon[] premiums = cds.getPremium().getPayments();
      final int maturityIndex = premiums.length - 1;
      final double offset = cds.isProtectStart() ? ONE_DAY_ACT_365F : 0.0;

      final double offsetStartTime = premiums[0].getAccrualStartTime() - offset;
      allTimePoints.add(new Double(offsetStartTime));

      double periodEndTime;

      for (int i = 0; i < premiums.length; i++) {

        periodEndTime = premiums[i].getAccrualEndTime() - (i < maturityIndex ? offset : 0.0);
        allTimePoints.add(new Double(periodEndTime));
      }

      timePointsInRange = allTimePoints.subSet(new Double(offsetStartTime), true, new Double(endTime), true);

    } else {

      timePointsInRange = allTimePoints.subSet(new Double(startTime), true, new Double(endTime), true);
    }

    final Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);

    final double[] timePoints = new double[boxed.length];
    final double[] discountFactors = new double[boxed.length];

    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
      discountFactors[i] = discountCurve.getDiscountFactor(timePoints[i]);
    }

    return new Timeline(timePoints, discountFactors);
  }

  /**
   * Build a timeline based on a series of coupon payment dates. All payment
   * dates are included, and the discount factors for those dates.
   *
   * @param cds Payment dates are extracted from the CDS premium and included in the timeline
   * @param discountCurve Discount factors for each payment date are taken from the discount curve
   * @return The populated timeline object
   */
  private Timeline buildPaymentTimeline(final ISDACDSDerivative cds, final ISDACurve discountCurve) {

    final ISDACDSCoupon[] payments = cds.getPremium().getPayments();

    final double[] timePoints = new double[payments.length];
    final double[] discountFactors = new double[payments.length];

    for (int i = 0; i < payments.length; ++i) {
      timePoints[i] = payments[i].getPaymentTime();
      discountFactors[i] = discountCurve.getDiscountFactor(timePoints[i]);
    }

    return new Timeline(timePoints, discountFactors);
  }
}
