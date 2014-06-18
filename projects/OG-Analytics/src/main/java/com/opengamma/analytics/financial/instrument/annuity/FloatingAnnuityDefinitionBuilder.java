/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.GeneralRollDateAdjuster;

/**
 * Generates an annuity of floating rate coupons.
 */
public class FloatingAnnuityDefinitionBuilder extends AbstractAnnuityDefinitionBuilder<FloatingAnnuityDefinitionBuilder> {
  
  private Double _initialRate;
  
  private IndexDeposit _index;

  private Double _spread;
  
  private Double _gearing;

  /**
   * Parameters used to adjust the reset dates. This is an optional field.
   */
  private AdjustedDateParameters _adjustedResetDateParameters;
  
  /**
   * Flag to indicate the fixing date relative to the accrual period. This is an optional field, and will default to the
   * beginning of the accrual period.
   */
  private DateRelativeTo _resetRelativeTo = DateRelativeTo.START;
  
  /**
   * Parameters used to adjust the fixing dates. This is an optional field.
   */
  private OffsetAdjustedDateParameters _adjustedFixingDateParameters;
  
  /**
   * The compounding method if the reset frequency is greater than the accrual period frequency. This is a required field
   * if the reset frequency is greater than the accrual period frequency and the annuity has a spread.
   */
  private CompoundingMethod _compoundingMethod;
  
  /**
   * The stub type of the first compounded stub period. This is an optional field, and will default to a short start stub
   * type.
   */
  private StubType _startStubCompoundingStub;
  
  /**
   * The stub type of the last compounded stub period. This is an optional field, and will default to none if unset.
   */
  private StubType _endStubCompoundingStub;

  /**
   * Sets the initial rate of the annuity. This is an optional field.
   * @param initialRate the initial rate of the annuity.
   * @return itself.
   */
  public FloatingAnnuityDefinitionBuilder initialRate(double initialRate) {
    _initialRate = initialRate;
    return this;
  }
  
  /**
   * Sets the spread of the floating rate coupons. This is an optional field.
   * @param spread the spread of the floating rate coupons.
   * @return itself.
   */
  public FloatingAnnuityDefinitionBuilder spread(double spread) {
    _spread = spread;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder gearing(double gearing) {
    _gearing = gearing;
    return this;
  }
  
  /**
   * Sets the index of the floating rate coupons. This is a required field.
   * @param index the index of the floating rate coupons.
   * @return itself.
   */
  public FloatingAnnuityDefinitionBuilder index(IndexDeposit index) {
    _index = index;
    return this;
  }
  
  /**
   * Sets the parameters used to adjust the reset dates. This is an optional field.
   * @param resetDateAdjustmentParameters the parameters used to adjust the fixing dates.
   * @return itself.
   */
  public FloatingAnnuityDefinitionBuilder resetDateAdjustmentParameters(AdjustedDateParameters resetDateAdjustmentParameters) {
    _adjustedResetDateParameters = resetDateAdjustmentParameters;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder resetRelativeTo(DateRelativeTo resetRelativeTo) {
    _resetRelativeTo = resetRelativeTo;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder fixingDateAdjustmentParameters(OffsetAdjustedDateParameters fixingDateAdjustmentParameters) {
    _adjustedFixingDateParameters = fixingDateAdjustmentParameters;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder compoundingMethod(CompoundingMethod compoundingMethod) {
    _compoundingMethod = compoundingMethod;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder startStubCompoundingMethod(StubType startStubCompoundingMethod) {
    _startStubCompoundingStub = startStubCompoundingMethod;
    return this;
  }
  
  public FloatingAnnuityDefinitionBuilder endStubCompoundingMethod(StubType endStubCompoundingMethod) {
    _endStubCompoundingStub = endStubCompoundingMethod;
    return this;
  }
  
  private boolean isCompounding() {
    if (_compoundingMethod == null) {
      return false;
    }
    
    if (_index instanceof IborIndex) {
      Period resetFrequency = ((IborIndex) _index).getTenor();
      return !getAccrualPeriodFrequency().equals(resetFrequency);
    } else {
      return CompoundingMethod.NONE != _compoundingMethod;
    }
  }
  
  private boolean hasInitialRate() {
    return _initialRate != null && !_initialRate.isNaN();
  }
  
  private boolean hasSpread() {
    return _spread != null && !_spread.isNaN();
  }
  
  private boolean hasGearing() {
    return _gearing != null && !_gearing.isNaN();
  }
  
  /**
   * Returns the fixing dates relative to the specified set of accrual dates, which are either start or end dates.
   * @param fixingDates either accrual start or accrual end dates.
   * @return the fixing dates
   */
  private ZonedDateTime[] getResetDates(ZonedDateTime[] fixingDates) {
    if (_adjustedResetDateParameters == null) {
      return fixingDates;
    }
    
    ZonedDateTime[] resetDates = new ZonedDateTime[fixingDates.length];
    Calendar fixingDateCalendar = getFixingCalendar();
    
    for (int i = 0; i < resetDates.length; i++) {
//      fixingDates[i] = _adjustedFixingDateParameters.getBusinessDayConvention().adjustDate(fixingDateCalendar, resetDates[i]);
      resetDates[i] = ScheduleCalculator.getAdjustedDate(fixingDates[i], -_adjustedFixingDateParameters.getOffset(), fixingDateCalendar);
    }
    return resetDates;
  }
  
  /**
   * Fall down the various calendars, trying to find the default calendar to use for fixings.
   * <ol>
   * <li>Fixing calendar</li>
   * <li>Accrual calendar</li>
   * </ol>
   * 
   * @return the fixing calendar.
   */
  private Calendar getFixingCalendar() {
    Calendar fixingCalendar = null;
    if (_adjustedFixingDateParameters != null) {
      fixingCalendar = _adjustedFixingDateParameters.getCalendar();
    } else if (getAccrualPeriodAdjustmentParameters() != null) {
      fixingCalendar = getAccrualPeriodAdjustmentParameters().getCalendar();
    }
    return fixingCalendar;
  }
  
  private BusinessDayConvention getFixingBusinessDayConvention() {
    BusinessDayConvention fixingBusinessDayConvention = null;
    if (_adjustedFixingDateParameters != null) {
      fixingBusinessDayConvention = _adjustedFixingDateParameters.getBusinessDayConvention();
    } else if (getAccrualPeriodAdjustmentParameters() != null) {
      fixingBusinessDayConvention = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention();
    }
    return fixingBusinessDayConvention;
  }
  
  /**
   * Generates reset dates relative to a given set of accrual dates, which may be either start or end dates.
   * @param accrualDates start or end accrual dates.
   * @return a set of reset dates relative to accrual dates
   */
  private ZonedDateTime[] getFixingDates(ZonedDateTime[] accrualDates) {
    if (_adjustedFixingDateParameters == null) {
      return accrualDates;
    }
    return ScheduleCalculator.getAdjustedDateSchedule(
        accrualDates,
        _adjustedFixingDateParameters.getBusinessDayConvention(),
        _adjustedFixingDateParameters.getCalendar(),
        _adjustedFixingDateParameters.getOffset());
  }
  
  private Calendar getResetCalendar() {
    Calendar resetCalendar = null;
    if (_adjustedResetDateParameters != null) {
      resetCalendar = _adjustedResetDateParameters.getCalendar();
    } else if (getAccrualPeriodAdjustmentParameters() != null) {
      resetCalendar = getAccrualPeriodAdjustmentParameters().getCalendar();
    }
    return resetCalendar;
  }
  
  @Override
  public AnnuityDefinition<?> build() {
    CouponDefinition[] coupons;
    
    int exchangeNotionalCouponCount = 0;
    if (isExchangeInitialNotional()) {
      exchangeNotionalCouponCount++;
    }
    if (isExchangeFinalNotional()) {
      exchangeNotionalCouponCount++;
    }

    /*
     * This assumes that the dates are adjusted, which may not always be true. Use the payment date adjustment calendar
     * if not null, otherwise use accrual date adjustment calendar.
     */
    Calendar calendar = null;
    if (getPaymentDateAdjustmentParameters() != null) {
      calendar = getPaymentDateAdjustmentParameters().getCalendar();
    } else if (getAccrualPeriodAdjustmentParameters() != null) {
      calendar = getAccrualPeriodAdjustmentParameters().getCalendar();
    }
    
    if (Period.ZERO.equals(getAccrualPeriodFrequency())) {
      coupons = generateZeroCouponFlows(exchangeNotionalCouponCount);
    } else {
      coupons = generateFloatFlows(exchangeNotionalCouponCount);
    }
    
    if (isExchangeInitialNotional()) {
      coupons[0] = getExchangeInitialNotionalCoupon();
    }

    if (isExchangeFinalNotional()) {
      coupons[coupons.length - 1] = getExchangeFinalNotionalCoupon();
    }
    
    return new AnnuityDefinition<>(coupons, calendar);
  }

  private CouponDefinition[] generateFloatFlows(int exchangeNotionalCouponCount) {
    CouponDefinition[] coupons;
    ZonedDateTime startDate = getStartDate();
    
    ZonedDateTime[] unadjustedAccrualEndDates = getAccrualEndDates(false);
    ZonedDateTime[] unadjustedAccrualStartDates = ScheduleCalculator.getStartDates(startDate, unadjustedAccrualEndDates);
    
    ZonedDateTime[] adjustedAccrualEndDates = getAccrualEndDates();
    ZonedDateTime[] adjustedAccrualStartDates = ScheduleCalculator.getStartDates(startDate, adjustedAccrualEndDates);
        
    ZonedDateTime[] paymentDates;
    if (DateRelativeTo.START == getPaymentDateRelativeTo()) {
      paymentDates = getPaymentDates(adjustedAccrualStartDates);
    } else {
      paymentDates = getPaymentDates(adjustedAccrualEndDates);
    }

    coupons = new CouponDefinition[exchangeNotionalCouponCount + adjustedAccrualEndDates.length];

    int couponOffset = isExchangeInitialNotional() ? 1 : 0;
    
    for (int c = 0; c < adjustedAccrualEndDates.length; c++) {
      CouponDefinition coupon = null;
      
      // common coupon parameters
      ZonedDateTime paymentDate = paymentDates[c];
      ZonedDateTime accrualStartDate = adjustedAccrualStartDates[c];
      ZonedDateTime accrualEndDate = adjustedAccrualEndDates[c];
      ZonedDateTime unadjustedAccrualStartDate = unadjustedAccrualStartDates[c];
      ZonedDateTime unadjustedAccrualEndDate = unadjustedAccrualEndDates[c];
      
      double notional = (isPayer() ? -1 : 1) * getNotional().getAmount(accrualStartDate.toLocalDate());
      
      // Check if we need to handle an interpolated stub
      boolean isStubStart = c == 0 && getStartStub() != null 
          && (StubType.SHORT_START == getStartStub().getStubType() || StubType.LONG_START == getStartStub().getStubType() || StubType.BOTH == getStartStub().getStubType())
          && ((getStartStub().getFirstIborIndex() != null && getStartStub().getSecondIborIndex() != null) || getStartStub().hasStubRate());
      boolean isStubEnd = c == adjustedAccrualEndDates.length - 1
          && getEndStub() != null && (StubType.SHORT_END == getEndStub().getStubType() || StubType.LONG_END == getEndStub().getStubType() || StubType.BOTH == getEndStub().getStubType())
          && ((getEndStub().getFirstIborIndex() != null && getEndStub().getSecondIborIndex() != null) || getEndStub().hasStubRate());

      
      if (_index instanceof IborIndex) {
        CouponStub stub = null;
        if (isStubStart) {
          stub = getStartStub();
        } else if (isStubEnd) {
          stub = getEndStub();
        }
        coupon = getIborCoupon(
            notional,
            paymentDate,
            unadjustedAccrualStartDate,
            unadjustedAccrualEndDate,
            stub,
            c == 0,
            c == adjustedAccrualEndDates.length - 1);
      } else if (_index instanceof IndexON) {
        CouponStub stub = null;
        if (isStubStart) {
          stub = getStartStub();
        } else if (isStubEnd) {
          stub = getEndStub();
        }
        coupon = getOISCoupon(
            notional,
            paymentDate,
            unadjustedAccrualStartDate,
            unadjustedAccrualEndDate,
            stub,
            c == 0, c == adjustedAccrualEndDates.length - 1);
      }
      coupons[c + couponOffset] = coupon;
    }
    return downCastIborCoupons(coupons);
  }

  /**
   * Function to downcast ibor coupons to help identify vanilla fix/float swaps to downstream code
   *
   * @param coupons the coupons
   * @return coupons cast into array of CouponIborDefinition or original list if not all Ibor-like
   */
  private CouponDefinition[] downCastIborCoupons(CouponDefinition[] coupons) {
    boolean allIborCoupons = true;
    for (CouponDefinition coupon : coupons) {
      if (!(coupon instanceof CouponIborDefinition)) {
        allIborCoupons = false;
        break;
      }
    }
    if (allIborCoupons) {
      CouponIborDefinition[] iborCoupons = new CouponIborDefinition[coupons.length];
      System.arraycopy(coupons, 0, iborCoupons, 0, coupons.length);
      return iborCoupons;
    }
    return coupons;
  }

  private boolean hasStubs() {
    return false;
  }

  private CouponDefinition[] generateZeroCouponFlows(int exchangeNotionalCouponCount) {
    CouponDefinition[] coupons;
    coupons = new CouponDefinition[exchangeNotionalCouponCount + 1];

    int couponOffset = isExchangeInitialNotional() ? 1 : 0;
    CouponDefinition coupon = null;
    
    // common coupon parameters
    ZonedDateTime unadjustedAccrualStartDate = getStartDate();
    ZonedDateTime unadjustedAccrualEndDate = getEndDate();
    ZonedDateTime accrualStartDate = unadjustedAccrualStartDate;
    ZonedDateTime accrualEndDate = _adjustedResetDateParameters.getBusinessDayConvention().adjustDate(_adjustedFixingDateParameters.getCalendar(), unadjustedAccrualEndDate);
    
    ZonedDateTime paymentDate;
    if (DateRelativeTo.START == getPaymentDateRelativeTo()) {
      paymentDate = getPaymentDates(new ZonedDateTime[] {accrualStartDate})[0];
    } else {
      paymentDate = getPaymentDates(new ZonedDateTime[] {accrualEndDate})[0];
    }

    double notional = (isPayer() ? -1 : 1) * getNotional().getAmount(accrualStartDate.toLocalDate());
    
    // Check if we need to handle an interpolated stub
    boolean isStubStart = getStartStub() != null 
        && (StubType.SHORT_START == getStartStub().getStubType() || StubType.LONG_START == getStartStub().getStubType() || StubType.BOTH == getStartStub().getStubType())
        && ((getStartStub().getFirstIborIndex() != null && getStartStub().getSecondIborIndex() != null) || getStartStub().hasStubRate());
    boolean isStubEnd =
        getEndStub() != null && (StubType.SHORT_END == getEndStub().getStubType() || StubType.LONG_END == getEndStub().getStubType() || StubType.BOTH == getEndStub().getStubType())
        && ((getEndStub().getFirstIborIndex() != null && getEndStub().getSecondIborIndex() != null) || getEndStub().hasStubRate());
    
    if (_index instanceof IborIndex) {
      CouponStub stub = null;
      if (isStubStart) {
        stub = getStartStub();
      } else if (isStubEnd) {
        stub = getEndStub();
      }
      coupon = getIborCoupon(
          notional,
          paymentDate,
          unadjustedAccrualStartDate,
          unadjustedAccrualEndDate,
          stub,
          true,
          true);
    } else if (_index instanceof IndexON) {
      CouponStub stub = null;
      if (isStubStart) {
        stub = getStartStub();
      } else if (isStubEnd) {
        stub = getEndStub();
      }
      coupon = getOISCoupon(
          notional,
          paymentDate,
          unadjustedAccrualStartDate,
          unadjustedAccrualEndDate,
          stub,
          true, true);
    }
    coupons[couponOffset] = coupon;
    
    return coupons;
  }
  
  private CouponDefinition getOISDefinition(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      double accrualYearFraction,
      ZonedDateTime fixingPeriodStartDate,
      ZonedDateTime fixingPeriodEndDate) {
    CouponDefinition coupon;
    if (hasGearing()) {
      throw new OpenGammaRuntimeException("Unsupported OIS geared coupon");
    } else if (hasSpread()) {
      coupon = new CouponONArithmeticAverageSpreadDefinition(
          getCurrency(),
          paymentDate,
          accrualStartDate,
          accrualEndDate,
          accrualYearFraction,
          notional,
          (IndexON) _index,
          fixingPeriodStartDate,
          fixingPeriodEndDate,
          _spread,
          _adjustedResetDateParameters.getCalendar());
    } else {
      coupon = new CouponONArithmeticAverageDefinition(
          getCurrency(),
          paymentDate,
          accrualStartDate,
          accrualEndDate,
          accrualYearFraction,
          notional,
          (IndexON) _index,
          fixingPeriodStartDate,
          fixingPeriodEndDate,
          _adjustedResetDateParameters.getCalendar());
    }
    return coupon;
  }
  
  private CouponDefinition getIborCoupon(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime unadjustedAccrualStartDate,
      ZonedDateTime unadjustedAccrualEndDate,
      CouponStub couponStub,
      boolean isFirstCoupon,
      boolean isLastCoupon) {
    
    ZonedDateTime adjustedAccrualStartDate = unadjustedAccrualStartDate;
    // Note do not roll first coupon's start date!
    if (!isFirstCoupon) {
        adjustedAccrualStartDate = adjustedAccrualStartDate.with(getRollDateAdjuster());
        adjustedAccrualStartDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
          getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualStartDate);
    }
    
    ZonedDateTime adjustedAccrualEndDate = unadjustedAccrualEndDate.with(getRollDateAdjuster());
    adjustedAccrualEndDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualEndDate);

    double accrualYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), getAccrualPeriodAdjustmentParameters().getCalendar(), getDayCount(),
                                                                              couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                                              adjustedAccrualStartDate, adjustedAccrualEndDate, isFirstCoupon, isLastCoupon);

    final CouponDefinition coupon;
    if (isCompounding()) {
      // This is common to compounding coupons

      ZonedDateTime[] compoundingAccrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
          unadjustedAccrualStartDate, // Pass in the unadjusted date - it will come out adjusted
          unadjustedAccrualEndDate, // Pass in the adjusted date - it will come out adjusted
          ((IborIndex) _index).getTenor(),
          StubType.SHORT_START,
          getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
          getAccrualPeriodAdjustmentParameters().getCalendar(),
          getRollDateAdjuster() instanceof GeneralRollDateAdjuster ? null : getRollDateAdjuster()); // using DoM adjuster is messing up maturity date
      ZonedDateTime[] compoundingAccrualStartDates = new ZonedDateTime[compoundingAccrualEndDates.length];
      compoundingAccrualStartDates[0] = adjustedAccrualStartDate;
      System.arraycopy(compoundingAccrualEndDates, 0, compoundingAccrualStartDates, 1, compoundingAccrualEndDates.length - 1);

      double[] paymentAccrualFactors = new double[compoundingAccrualStartDates.length];
      for (int i = 0; i < paymentAccrualFactors.length; i++) {
        paymentAccrualFactors[i] = AnnuityDefinitionBuilder.getDayCountFraction(((IborIndex) _index).getTenor(), getAccrualPeriodAdjustmentParameters().getCalendar(), getDayCount(),
                                                                                couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                                                compoundingAccrualStartDates[i], compoundingAccrualEndDates[i], isFirstCoupon, isLastCoupon);
      }
      
      ZonedDateTime[] compoundingFixingStartDates = compoundingAccrualStartDates;
      if (isFirstCoupon) {
        // Ensure that the forward period dates are adjusted for first compound period
        compoundingFixingStartDates[0] = _adjustedResetDateParameters.getBusinessDayConvention().adjustDate(
            _adjustedFixingDateParameters.getCalendar(), compoundingFixingStartDates[0]);
      }
      ZonedDateTime[] compoundingFixingEndDates = ScheduleCalculator.getAdjustedDateSchedule(
          compoundingFixingStartDates,
          ((IborIndex) _index).getTenor(), // we use the accrual freq, not the reset freq which is for generating coupon sub-periods
          _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
          _adjustedFixingDateParameters.getCalendar(), // This is using the fixing calendar instead of the reset calendar
          null); // getRollDateAdjuster()); // set to null for forward date roll bug
      if (couponStub != null) {
        if (!couponStub.isInterpolated() && isFirstCoupon) {
          if (couponStub.getEffectiveDate() != null) {
            compoundingFixingEndDates[0] = ZonedDateTime.of(couponStub.getEffectiveDate(), LocalTime.MAX, ZoneId.systemDefault());
          } else if (couponStub.getFirstIborIndex() != null) {
            compoundingFixingEndDates[0] = ScheduleCalculator.getAdjustedDate(
                compoundingFixingStartDates[0],
                couponStub.getFirstIborIndex().getTenor(),
                _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
                _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
          }
        } else {
          // force set first stub end date so that we get correct forward rate
          compoundingFixingEndDates[0] = compoundingAccrualEndDates[0];
        }
      }

      double[] compoundingFixingYearFracs = new double[compoundingAccrualEndDates.length];
      for (int i = 0; i < compoundingAccrualEndDates.length; i++) {
        compoundingFixingYearFracs[i] = AnnuityDefinitionBuilder.getDayCountFraction(((IborIndex) _index).getTenor(), _adjustedResetDateParameters.getCalendar(), getDayCount(),
                                                                                     couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                                                     compoundingFixingStartDates[i], compoundingFixingEndDates[i], isFirstCoupon, isLastCoupon);
      }

      ZonedDateTime[] compoundingFixingDates;
      if (DateRelativeTo.START == _resetRelativeTo) {
        compoundingFixingDates = getFixingDates(compoundingFixingStartDates);
      } else {
        compoundingFixingDates = getFixingDates(compoundingFixingEndDates);
      }
      
      if (couponStub != null && couponStub.isInterpolated()) {
        ZonedDateTime firstInterpolatedDate = ScheduleCalculator.getAdjustedDate(
            compoundingFixingStartDates[0],
            couponStub.getFirstIborIndex().getTenor(),
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
        double firstInterpolatedYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(((IborIndex) _index).getTenor(), _adjustedFixingDateParameters.getCalendar(), getDayCount(), couponStub.getStubType(), couponStub.getStubType(),
                                                                                            compoundingFixingStartDates[0], firstInterpolatedDate, isFirstCoupon, isLastCoupon);
        ZonedDateTime secondInterpolatedDate = ScheduleCalculator.getAdjustedDate(
            compoundingFixingStartDates[0],
            couponStub.getSecondIborIndex().getTenor(),
            _adjustedResetDateParameters.getBusinessDayConvention(),
            // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
        double secondInterpolatedYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(((IborIndex) _index).getTenor(), _adjustedFixingDateParameters.getCalendar(), getDayCount(), couponStub.getStubType(), couponStub.getStubType(),
                                                                                             compoundingFixingStartDates[0], secondInterpolatedDate, isFirstCoupon, isLastCoupon);
        
                                    
        coupon = getIborCompoundingInterpolatedStubDefinition(
            notional,
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            accrualYearFraction,
            compoundingAccrualStartDates,
            compoundingAccrualEndDates,
            paymentAccrualFactors,
            compoundingFixingDates,
            compoundingFixingStartDates,
            compoundingFixingEndDates,
            compoundingFixingYearFracs,
            couponStub.getStubRate(),
            firstInterpolatedDate,
            firstInterpolatedYearFraction,
            couponStub.getFirstIborIndex(),
            secondInterpolatedDate,
            secondInterpolatedYearFraction,
            couponStub.getSecondIborIndex());
      } else {
        // Check for fixed stub rate and use first over interpolated periods
        coupon = getIborCompoundingDefinition(
            notional,
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            accrualYearFraction,
            compoundingAccrualStartDates,
            compoundingAccrualEndDates,
            paymentAccrualFactors,
            compoundingFixingDates,
            compoundingFixingStartDates,
            compoundingFixingEndDates,
            compoundingFixingYearFracs,
            couponStub != null ? couponStub.getStubRate() : Double.NaN);
      }
    } else {
      boolean hasInitialStubRate = couponStub != null && !Double.isNaN(couponStub.getStubRate());
      if (hasInitialStubRate || (isFirstCoupon && hasInitialRate())) {
        double initialRate;
        if (isFirstCoupon && hasInitialRate()) {
          initialRate = _initialRate;
        } else if (hasInitialStubRate) {
          initialRate = couponStub.getStubRate();
        } else {
          throw new OpenGammaRuntimeException("Bad initial rate/stub rate");
        }
        if (hasSpread()) {
          initialRate += _spread;
        }
        coupon = new CouponFixedDefinition(
            getCurrency(),
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), getAccrualPeriodAdjustmentParameters().getCalendar(), getDayCount(),
                                                         couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                         adjustedAccrualStartDate, adjustedAccrualEndDate, isFirstCoupon, isLastCoupon),
            notional,
            initialRate);
      } else {
        // See TODO below about reset BDC used instead of fixing BDC
        ZonedDateTime fixingPeriodStartDate = _adjustedResetDateParameters.getBusinessDayConvention().adjustDate(_adjustedFixingDateParameters.getCalendar(), adjustedAccrualStartDate);
        if (isFirstCoupon) {
          // Ensure that the forward period dates are adjusted for first coupon
          fixingPeriodStartDate = _adjustedResetDateParameters.getBusinessDayConvention().adjustDate(
              _adjustedResetDateParameters.getCalendar(), fixingPeriodStartDate);
        }
        ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(
            fixingPeriodStartDate,
            getAccrualPeriodFrequency(), // we use the accrual freq, not the reset freq which is for generating coupon sub-periods
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar(), // This is using the fixing calendar instead of the reset calendar
            null); // getRollDateAdjuster()); // set to null for forward date roll bug
        double fixingPeriodYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), _adjustedResetDateParameters.getCalendar(), getDayCount(),
                                                                                       couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                                                       fixingPeriodStartDate, fixingPeriodEndDate, isFirstCoupon, isLastCoupon);
        ZonedDateTime fixingDate;
        if (DateRelativeTo.START == _resetRelativeTo) {
          fixingDate = ScheduleCalculator.getAdjustedDate(
                  fixingPeriodStartDate,
                  _adjustedFixingDateParameters.getBusinessDayConvention(),
                  _adjustedFixingDateParameters.getCalendar(),
                  _adjustedFixingDateParameters.getOffset());
        } else {
          fixingDate = ScheduleCalculator.getAdjustedDate(
              fixingPeriodEndDate,
              _adjustedFixingDateParameters.getBusinessDayConvention(),
              _adjustedFixingDateParameters.getCalendar(),
              _adjustedFixingDateParameters.getOffset());
        }
        
        if (couponStub != null && couponStub.isInterpolated()) {
          ZonedDateTime firstInterpolatedDate = ScheduleCalculator.getAdjustedDate(
              fixingPeriodStartDate,
              couponStub.getFirstIborIndex().getTenor(),
              _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
              _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
          double firstInterpolatedYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), _adjustedResetDateParameters.getCalendar(), getDayCount(), couponStub.getStubType(), couponStub.getStubType(),
                                                                                              fixingPeriodStartDate, firstInterpolatedDate, isFirstCoupon, isLastCoupon);
          ZonedDateTime secondInterpolatedDate = ScheduleCalculator.getAdjustedDate(
              fixingPeriodStartDate,
              couponStub.getSecondIborIndex().getTenor(),
              _adjustedResetDateParameters.getBusinessDayConvention(),
              // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
              _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
          double secondInterpolatedYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), _adjustedResetDateParameters.getCalendar(), getDayCount(), couponStub.getStubType(), couponStub.getStubType(),
                                                                                               fixingPeriodStartDate, secondInterpolatedDate, isFirstCoupon, isLastCoupon);
        
          coupon = getIborInterpolatedStubDefinition(
              notional,
              paymentDate,
              adjustedAccrualStartDate,
              adjustedAccrualEndDate,
              accrualYearFraction,
              fixingDate,
              fixingPeriodStartDate,
              fixingPeriodEndDate,
              fixingPeriodYearFraction,
              couponStub.getStubRate(),
              firstInterpolatedDate,
              firstInterpolatedYearFraction,
              couponStub.getFirstIborIndex(),
              secondInterpolatedDate,
              secondInterpolatedYearFraction,
              couponStub.getSecondIborIndex());
        } else {
          ZonedDateTime actualFixingPeriodEndDate;
          if (couponStub != null && couponStub.getEffectiveDate() != null && isFirstCoupon) {
            actualFixingPeriodEndDate = ZonedDateTime.of(couponStub.getEffectiveDate(), LocalTime.of(0, 0), ZoneId.of("UTC"));
            fixingPeriodYearFraction =  AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), _adjustedResetDateParameters.getCalendar(), getDayCount(), couponStub.getStubType(), couponStub.getStubType(),
                                                                                     fixingPeriodStartDate, actualFixingPeriodEndDate, isFirstCoupon, isLastCoupon);
          } else {
            actualFixingPeriodEndDate = fixingPeriodEndDate;
          }
          coupon = getIborDefinition(
              notional,
              paymentDate,
              adjustedAccrualStartDate,
              adjustedAccrualEndDate,
              accrualYearFraction,
              fixingDate,
              fixingPeriodStartDate,
              actualFixingPeriodEndDate,
              fixingPeriodYearFraction);
        }
      }
    }
    return coupon;
  }
  
  private CouponDefinition getOISCoupon(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime unadjustedAccrualStartDate,
      ZonedDateTime unadjustedAccrualEndDate,
      CouponStub couponStub,
      boolean isFirstCoupon, boolean isLastCoupon) {
    
    ZonedDateTime adjustedAccrualStartDate = unadjustedAccrualStartDate;
    // Note do not roll first coupon's start date!
    if (!isFirstCoupon) {
      adjustedAccrualStartDate = adjustedAccrualStartDate.with(getRollDateAdjuster());
    }
    adjustedAccrualStartDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualStartDate);
    
    ZonedDateTime adjustedAccrualEndDate = unadjustedAccrualEndDate.with(getRollDateAdjuster());
    adjustedAccrualEndDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualEndDate);

    double accrualYearFraction = AnnuityDefinitionBuilder.getDayCountFraction(Period.ZERO.equals(getAccrualPeriodFrequency()) ? Period.ofYears(1) : getAccrualPeriodFrequency(), getAccrualPeriodAdjustmentParameters().getCalendar(), getDayCount(),
                                                                              couponStub != null ? couponStub.getStubType() : StubType.NONE, couponStub != null ? couponStub.getStubType() : StubType.NONE,
                                                                              adjustedAccrualStartDate, adjustedAccrualEndDate, isFirstCoupon, isLastCoupon);

    final CouponDefinition coupon;
    if (isCompounding()) {
      if (hasSpread()) {
        coupon = new CouponONSpreadDefinition(
            getCurrency(),
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            accrualYearFraction,
            notional,
            (IndexON) _index,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            _adjustedResetDateParameters.getCalendar(),
            _spread);
      } else {
        coupon = new CouponONDefinition(
            getCurrency(),
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            accrualYearFraction,
            notional,
            (IndexON) _index,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            _adjustedResetDateParameters.getCalendar());
      }
    } else {
      coupon = getOISDefinition(
          notional,
          paymentDate,
          adjustedAccrualStartDate,
          adjustedAccrualEndDate,
          accrualYearFraction,
          adjustedAccrualStartDate,
          adjustedAccrualEndDate);
    }
    return coupon;
  }
  
  private CouponDefinition getIborDefinition(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      double accrualYearFraction,
      ZonedDateTime fixingDate,
      ZonedDateTime fixingPeriodStartDate,
      ZonedDateTime fixingPeriodEndDate,
      double fixingPeriodYearFraction) {
    CouponDefinition coupon;
    if (hasGearing()) {
      coupon = new CouponIborGearingDefinition(
          getCurrency(), 
          paymentDate,
          accrualStartDate,
          accrualEndDate,
          accrualYearFraction,
          notional,
          fixingDate,
          (IborIndex) _index,
          fixingPeriodStartDate,
          fixingPeriodEndDate,
          fixingPeriodYearFraction,
          _spread,
          _gearing);
    } else if (hasSpread()) {
      coupon = new CouponIborSpreadDefinition(
          getCurrency(),
          paymentDate,
          accrualStartDate,
          accrualEndDate,
          accrualYearFraction,
          notional,
          fixingDate,
          fixingPeriodStartDate,
          fixingPeriodEndDate,
          fixingPeriodYearFraction,
          (IborIndex) _index,
          _spread,
          _adjustedResetDateParameters.getCalendar());
    } else {
      coupon = new CouponIborDefinition(
          getCurrency(),
          paymentDate,
          accrualStartDate,
          accrualEndDate,
          accrualYearFraction,
          notional,
          fixingDate,
          fixingPeriodStartDate,
          fixingPeriodEndDate,
          fixingPeriodYearFraction,
          (IborIndex) _index,
          _adjustedResetDateParameters.getCalendar());
    }
    return coupon;
  }
  
  private CouponDefinition getIborInterpolatedStubDefinition(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      double accrualYearFraction,
      ZonedDateTime fixingDate,
      ZonedDateTime fixingPeriodStartDate,
      ZonedDateTime fixingPeriodEndDate,
      double fixingPeriodYearFraction,
      double initialRate,
      ZonedDateTime firstInterpolatedDate,
      double firstInterpolatedYearFraction,
      IborIndex firstStubIndex,
      ZonedDateTime secondInterpolatedDate,
      double secondInterpolatedYearFraction,
      IborIndex secondStubIndex) {
    //Apply equal weightings for both stub tenors
    double weighting = 0.5;
    return CouponIborAverageDefinition.from(paymentDate, accrualStartDate, accrualEndDate, accrualYearFraction, notional, fixingDate, firstStubIndex, secondStubIndex, weighting, weighting, getFixingCalendar(), getFixingCalendar());        
  }
  
  private CouponDefinition getIborCompoundingDefinition(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      double accrualYearFraction,
      ZonedDateTime[] compoundAccrualStartDates,
      ZonedDateTime[] compoundAccrualEndDates,
      double[] compoundAccrualYearFractions,
      ZonedDateTime[] compoundFixingDates,
      ZonedDateTime[] compoundFixingStartDates,
      ZonedDateTime[] compoundFixingEndDates,
      double[] compoundFixingYearFractions,
      double initialCompoundRate
  ) {
    final CouponDefinition coupon;
    if (hasGearing()) {
      throw new OpenGammaRuntimeException("Unsupported ibor gearing compounded definition");
    } else if (hasSpread())  {
      if (CompoundingMethod.FLAT == _compoundingMethod) {
        coupon = CouponIborCompoundingFlatSpreadDefinition.from(
            getCurrency(),
            paymentDate,
            accrualStartDate,
            accrualEndDate,
            accrualYearFraction,
            notional,
            ((IborIndex) _index),
            compoundAccrualStartDates,
            compoundAccrualEndDates,
            compoundAccrualYearFractions,
            compoundFixingDates,
            compoundFixingStartDates,
            compoundFixingEndDates,
            compoundFixingYearFractions,
            _spread,
            initialCompoundRate);
      } else if (CompoundingMethod.SPREAD_EXCLUSIVE == _compoundingMethod) {
        coupon = CouponIborCompoundingSimpleSpreadDefinition.from(
            getCurrency(), 
            paymentDate, 
            accrualStartDate, 
            accrualEndDate, 
            accrualYearFraction, 
            notional, 
            ((IborIndex) _index),
            compoundAccrualStartDates, 
            compoundAccrualEndDates, 
            compoundAccrualYearFractions, 
            compoundFixingDates, 
            compoundFixingStartDates, 
            compoundFixingEndDates, 
            compoundFixingYearFractions, 
            _spread, 
            initialCompoundRate);
      } else {
        coupon = CouponIborCompoundingSpreadDefinition.from(
            getCurrency(),
            paymentDate,
            accrualStartDate,
            accrualEndDate,
            accrualYearFraction,
            notional,
            ((IborIndex) _index),
            compoundAccrualStartDates,
            compoundAccrualEndDates,
            compoundAccrualYearFractions,
            compoundFixingDates,
            compoundFixingStartDates,
            compoundFixingEndDates,
            compoundFixingYearFractions,
            _spread,
            initialCompoundRate);
      }
    } else {
      coupon = CouponIborCompoundingDefinition.from(
          getCurrency(),
          paymentDate,
          accrualStartDate, 
          accrualEndDate,
          accrualYearFraction,
          notional,
          ((IborIndex) _index),
          compoundAccrualStartDates,
          compoundAccrualEndDates,
          compoundAccrualYearFractions,
          compoundFixingDates,
          compoundFixingStartDates,
          compoundFixingEndDates,
          compoundFixingYearFractions,
          initialCompoundRate);
    }
    return coupon;
  }
  
  private CouponDefinition getIborCompoundingInterpolatedStubDefinition(
      double notional,
      ZonedDateTime paymentDate,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      double accrualYearFraction,
      ZonedDateTime[] compoundAccrualStartDates,
      ZonedDateTime[] compoundAccrualEndDates,
      double[] compoundAccrualYearFractions,
      ZonedDateTime[] compoundFixingDates,
      ZonedDateTime[] compoundFixingStartDates,
      ZonedDateTime[] compoundFixingEndDates,
      double[] compoundFixingYearFractions,
      double initialCompoundRate,
      ZonedDateTime firstInterpolatedDate,
      double firstInterpolatedYearFraction,
      IborIndex firstStubIndex,
      ZonedDateTime secondInterpolatedDate,
      double secondInterpolatedYearFraction,
      IborIndex secondStubIndex) {
    double weighting = 0.5;
    final CouponDefinition coupon = getIborCompoundingDefinition(notional, paymentDate, accrualStartDate, accrualEndDate, accrualYearFraction, compoundAccrualStartDates, compoundAccrualEndDates, compoundAccrualYearFractions, compoundFixingDates, compoundFixingStartDates, compoundFixingEndDates, compoundFixingYearFractions, initialCompoundRate);
    return CouponIborAverageDefinition.from(coupon, compoundFixingDates[0] , firstStubIndex, secondStubIndex, weighting, weighting, getFixingCalendar(), getFixingCalendar());     
  }
}
