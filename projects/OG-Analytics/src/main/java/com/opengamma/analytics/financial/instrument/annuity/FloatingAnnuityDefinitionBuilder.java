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
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.InterpolatedStubCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.EndOfMonthRollDateAdjuster;
import com.opengamma.financial.convention.rolldate.GeneralRollDateAdjuster;

/**
 * Generates an annuity of floating rate coupons.
 */
public class FloatingAnnuityDefinitionBuilder extends AbstractAnnuityDefinitionBuilder<FloatingAnnuityDefinitionBuilder> {
  
  private double _initialRate;
  
  private IndexDeposit _index;

  private double _spread;
  
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
    if (_index instanceof IborIndex) {
      Period resetFrequency = ((IborIndex) _index).getTenor();
      return !getAccrualPeriodFrequency().equals(resetFrequency);
    } else {
      return CompoundingMethod.NONE != _compoundingMethod;
    }
  }
  
  private boolean hasSpread() {
    return !Double.isNaN(_spread);
  }
  
  private boolean hasGearing() {
    return _gearing != null && _gearing.isNaN();
  }
  
  /**
   * Returns the fixing dates relative to the specified set of accrual dates, which are either start or end dates.
   * @param accrualDates either accrual start or accrual end dates.
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
      coupons = generateFloatFlows(exchangeNotionalCouponCount, calendar);
    }
    
    if (isExchangeInitialNotional()) {
      coupons[0] = getExchangeInitialNotionalCoupon();
    }

    if (isExchangeFinalNotional()) {
      coupons[coupons.length - 1] = getExchangeFinalNotionalCoupon();
    }
    
    return new AnnuityDefinition<>(coupons, calendar);
  }

  private CouponDefinition[] generateFloatFlows(int exchangeNotionalCouponCount, Calendar calendar) {
    CouponDefinition[] coupons;
    ZonedDateTime startDate = getStartDate();
    
    ZonedDateTime[] unadjustedAccrualEndDates = getAccrualEndDates(false);
    ZonedDateTime[] unadjustedAccrualStartDates = ScheduleCalculator.getStartDates(startDate, unadjustedAccrualEndDates);
    
    ZonedDateTime[] adjustedAccrualEndDates = getAccrualEndDates();
    ZonedDateTime[] adjustedAccrualStartDates = ScheduleCalculator.getStartDates(startDate, adjustedAccrualEndDates);
    
    /*
     * We don't generate using reset freq because we don't want to generate the compounding periods, so we re-use the
     * accrual periods.
     * 
     * using accrual period freq won't work for ZC OIS
     */
    ZonedDateTime[] fixingStartDates = adjustedAccrualStartDates;
    ZonedDateTime[] fixingEndDates;
    if (_index instanceof IndexON && Period.ZERO.equals(getAccrualPeriodFrequency())) {
      /*
       * For ZC OIS, we don't want to use the accrual freq, which will be 1T, 
       */
      fixingEndDates = adjustedAccrualEndDates;
    } else {
      fixingEndDates = ScheduleCalculator.getAdjustedDateSchedule(
          fixingStartDates,
          getAccrualPeriodFrequency(), // we use the accrual freq, not the reset freq which is for generating coupon sub-periods
          _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
          _adjustedFixingDateParameters.getCalendar(), // This is using the fixing calendar instead of the reset calendar
          null); // getRollDateAdjuster()); // set to null for forward date roll bug
    }
    
    ZonedDateTime[] paymentDates;
    if (DateRelativeTo.START == getPaymentDateRelativeTo()) {
      paymentDates = getPaymentDates(adjustedAccrualStartDates);
    } else {
      paymentDates = getPaymentDates(adjustedAccrualEndDates);
    }
    
    coupons = new CouponDefinition[exchangeNotionalCouponCount + adjustedAccrualEndDates.length];

    int couponOffset = isExchangeInitialNotional() ? 1 : 0;
    
    Calendar fixingCalendar = getFixingCalendar();
    
    // TODO these will be ignored for compounding, might want to avoid generating them
    ZonedDateTime[] fixingDates;
    if (DateRelativeTo.START == _resetRelativeTo) {
      fixingDates = getFixingDates(fixingStartDates);
    } else {
      fixingDates = getFixingDates(fixingEndDates);
    }
    
    for (int c = 0; c < adjustedAccrualEndDates.length; c++) {
      CouponDefinition coupon = null;
      
      // common coupon parameters
      ZonedDateTime paymentDate = paymentDates[c];
      ZonedDateTime accrualStartDate = adjustedAccrualStartDates[c];
      ZonedDateTime accrualEndDate = adjustedAccrualEndDates[c];
      ZonedDateTime unadjustedAccrualStartDate = unadjustedAccrualStartDates[c];
      ZonedDateTime unadjustedAccrualEndDate = unadjustedAccrualEndDates[c];
      
      double notional = (isPayer() ? -1 : 1) * getNotional().getAmount(accrualStartDate.toLocalDate());
      
      // non-compounding coupon parameters
      ZonedDateTime fixingDate = fixingDates != null ? fixingDates[c] : null;
      
      // ibor coupon parameters
      ZonedDateTime fixingPeriodStartDate = fixingStartDates[c];
      ZonedDateTime fixingPeriodEndDate = fixingEndDates[c];
      
      // Check if we need to handle an interpolated stub
      boolean isStubStart = c == 0 && getStartStub() != null 
          && (StubType.SHORT_START == getStartStub().getStubType() || StubType.LONG_START == getStartStub().getStubType() || StubType.BOTH == getStartStub().getStubType())
          && ((getStartStub().getFirstInterpPeriod() != null && getStartStub().getSecondInterpPeriod() != null) || getStartStub().hasStubRate());
      boolean isStubEnd = c == adjustedAccrualEndDates.length - 1
          && getEndStub() != null && (StubType.SHORT_END == getEndStub().getStubType() || StubType.LONG_END == getEndStub().getStubType() || StubType.BOTH == getEndStub().getStubType())
          && ((getEndStub().getFirstInterpPeriod() != null && getEndStub().getSecondInterpPeriod() != null) || getEndStub().hasStubRate());

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
            c == 0);
      }
      coupons[c + couponOffset] = coupon;
    }
    return coupons;
  }

  private CouponDefinition[] generateZeroCouponFlows(int exchangeNotionalCouponCount) {
    CouponDefinition[] coupons;
    coupons = new CouponDefinition[exchangeNotionalCouponCount + 1];
    
    int couponCount = isExchangeInitialNotional() ? 1 : 0;
    Period resetFrequency;
    if (_index instanceof IborIndex) {
      resetFrequency = ((IborIndex) _index).getTenor();
    } else if (_index instanceof IndexON) {
      resetFrequency = Period.ofDays(1);
    } else {
      throw new OpenGammaRuntimeException("Unsupported zero coupon index " + _index);
    }
    boolean isCompounding = !getAccrualPeriodFrequency().equals(resetFrequency);
    
    ZonedDateTime accStartDate = getStartDate();
    ZonedDateTime accEndDate = getEndDate();
    
    double initialNotional = (isPayer() ? -1 : 1) * getNotional().getAmount(accStartDate.toLocalDate());

    boolean isStubStart = getStartStub() != null 
        && (StubType.SHORT_START == getStartStub().getStubType() || StubType.LONG_START == getStartStub().getStubType())
        && getStartStub().getFirstInterpPeriod() != null && getStartStub().getSecondInterpPeriod() != null;
    boolean isStubEnd =
        getEndStub() != null && (StubType.SHORT_END == getEndStub().getStubType() || StubType.LONG_END == getEndStub().getStubType())
        && getEndStub().getFirstInterpPeriod() != null && getEndStub().getSecondInterpPeriod() != null;

    if (isStubStart || isStubEnd) {
      coupons[couponCount] = getNoSpreadInterpolatedStubCoupon(
          getPaymentDateAdjustmentParameters().getCalendar(),
          getFixingCalendar(),
          isCompounding, // compounding
          accStartDate,
          accEndDate,
          null, // fixing date - not needed for zc
          accStartDate, // fixing start
          null, // fixing end - not needed for zc
          accEndDate, // pmt date
          getFixingBusinessDayConvention(),
          initialNotional,
          isStubStart ? getStartStub() : getEndStub());
    } else {
      if (_index instanceof IborIndex) {
        if (hasSpread()) {
          if (CompoundingMethod.FLAT == _compoundingMethod) {
            coupons[couponCount] = CouponIborCompoundingFlatSpreadDefinition.from(
                initialNotional,
                accStartDate,
                accEndDate,
                (IborIndex) _index,
                _spread,
                StubType.SHORT_START,
                getFixingBusinessDayConvention(),
                getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster,
                getFixingCalendar(),
                getRollDateAdjuster());
          } else {
            coupons[couponCount] = CouponIborCompoundingSpreadDefinition.from(
                initialNotional,
                accStartDate,
                accEndDate,
                (IborIndex) _index,
                _spread,
                StubType.SHORT_START,
                getFixingBusinessDayConvention(),
                getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster,
                getFixingCalendar(),
                getRollDateAdjuster());
          }
        } else {
          coupons[couponCount] = CouponIborCompoundingDefinition.from(
              initialNotional,
              accStartDate,
              accEndDate,
              (IborIndex) _index,
              StubType.SHORT_START, 
              getFixingBusinessDayConvention(),
              getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster,
              getResetCalendar(),
              getRollDateAdjuster());
        }
      } else if (_index instanceof IndexON) {
        coupons[couponCount] = new CouponONDefinition(
            getCurrency(),
            accEndDate, // pmt date
            accStartDate, // acc start
            accEndDate, // acc end
            getDayCount().getDayCountFraction(accStartDate, accEndDate, getPaymentDateAdjustmentParameters().getCalendar()), // pmt year frac
            initialNotional,
            (IndexON) _index,
            accStartDate, // fixing start
            accEndDate, // fixing end
            getResetCalendar()); // fixing calendar
      }
    }
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
//      coupon = new CouponONSpreadDefinition(
//          getCurrency(),
//          paymentDate,
//          accrualStartDate,
//          accrualEndDate,
//          accrualYearFraction,
//          notional,
//          (IndexON) _index,
//          fixingPeriodStartDate,
//          fixingPeriodEndDate,
//          _adjustedResetDateParameters.getCalendar(),
//          _spread);
    } else {
      coupon = new CouponONDefinition(
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
      if (getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster) {
        adjustedAccrualStartDate = adjustedAccrualStartDate.with(getRollDateAdjuster());
      }
      adjustedAccrualStartDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
          getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualStartDate);
    }
    
    ZonedDateTime adjustedAccrualEndDate = unadjustedAccrualEndDate;
    if (!isLastCoupon) {
      if (getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster) {
        adjustedAccrualEndDate = adjustedAccrualEndDate.with(getRollDateAdjuster());
      }
    }
    adjustedAccrualEndDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualEndDate);
    
    double accrualYearFraction = getDayCount().getDayCountFraction(adjustedAccrualStartDate, adjustedAccrualEndDate, getAccrualPeriodAdjustmentParameters().getCalendar());
    
    
    CouponDefinition coupon = null;
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
        paymentAccrualFactors[i] = getDayCount().getDayCountFraction(compoundingAccrualStartDates[i], compoundingAccrualEndDates[i], getAccrualPeriodAdjustmentParameters().getCalendar());
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
          } else if (couponStub.getFirstInterpPeriod() != null) {
            compoundingFixingEndDates[0] = ScheduleCalculator.getAdjustedDate(
                compoundingFixingStartDates[0],
                couponStub.getFirstInterpPeriod(),
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
        compoundingFixingYearFracs[i] = getDayCount().getDayCountFraction(compoundingFixingStartDates[i], compoundingFixingEndDates[i], _adjustedResetDateParameters.getCalendar());
      }

      ZonedDateTime[] compoundingFixingDates;
      if (DateRelativeTo.START == _resetRelativeTo) {
        compoundingFixingDates = getFixingDates(compoundingFixingStartDates);
      } else {
        compoundingFixingDates = getFixingDates(compoundingFixingEndDates);
      }
      
      if (couponStub == null || !couponStub.isInterpolated()) {
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
      } else if (couponStub.isInterpolated()) {
        ZonedDateTime firstInterpolatedDate = ScheduleCalculator.getAdjustedDate(
            compoundingFixingStartDates[0],
            couponStub.getFirstInterpPeriod(),
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
        double firstInterpolatedYearFraction = getDayCount().getDayCountFraction(compoundingFixingStartDates[0], firstInterpolatedDate, _adjustedFixingDateParameters.getCalendar());
        ZonedDateTime secondInterpolatedDate = ScheduleCalculator.getAdjustedDate(
            compoundingFixingStartDates[0],
            couponStub.getSecondInterpPeriod(),
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
        double secondInterpolatedYearFraction = getDayCount().getDayCountFraction(compoundingFixingStartDates[0], secondInterpolatedDate, _adjustedFixingDateParameters.getCalendar());
        
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
            secondInterpolatedDate,
            secondInterpolatedYearFraction);
      }
    } else {
      if (couponStub != null && !Double.isNaN(couponStub.getStubRate())) {
        coupon = new CouponFixedDefinition(
            getCurrency(),
            paymentDate,
            adjustedAccrualStartDate,
            adjustedAccrualEndDate,
            getDayCount().getDayCountFraction(adjustedAccrualStartDate, adjustedAccrualEndDate, getPaymentDateAdjustmentParameters().getCalendar()),
            notional,
            couponStub.getStubRate() + (hasSpread() ? _spread : 0));
      } else {
        ZonedDateTime fixingPeriodStartDate = adjustedAccrualStartDate;
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
        double fixingPeriodYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, _adjustedResetDateParameters.getCalendar());
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
              couponStub.getFirstInterpPeriod(),
              _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
              _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
          double firstInterpolatedYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, firstInterpolatedDate, _adjustedResetDateParameters.getCalendar());
          ZonedDateTime secondInterpolatedDate = ScheduleCalculator.getAdjustedDate(
              fixingPeriodStartDate,
              couponStub.getSecondInterpPeriod(),
              _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
              _adjustedFixingDateParameters.getCalendar()); // This is using the fixing calendar instead of the reset calendar
          double secondInterpolatedYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, secondInterpolatedDate, _adjustedResetDateParameters.getCalendar());
          
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
              secondInterpolatedDate,
              secondInterpolatedYearFraction);
        } else {
          ZonedDateTime actualFixingPeriodEndDate;
          if (couponStub != null && couponStub.getEffectiveDate() != null && isFirstCoupon) {
            actualFixingPeriodEndDate = ZonedDateTime.of(couponStub.getEffectiveDate(), LocalTime.of(0, 0), ZoneId.of("UTC"));
            fixingPeriodYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, actualFixingPeriodEndDate, _adjustedResetDateParameters.getCalendar());
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
      boolean isFirstCoupon) {
    
    ZonedDateTime adjustedAccrualStartDate = unadjustedAccrualStartDate;
    // Note do not roll first coupon's start date!
    if (!isFirstCoupon && getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster) {
      adjustedAccrualStartDate = adjustedAccrualStartDate.with(getRollDateAdjuster());
    }
    adjustedAccrualStartDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualStartDate);
    
    ZonedDateTime adjustedAccrualEndDate = unadjustedAccrualEndDate;
    if (getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster) {
      adjustedAccrualEndDate = adjustedAccrualEndDate.with(getRollDateAdjuster());
    }
    adjustedAccrualEndDate = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
        getAccrualPeriodAdjustmentParameters().getCalendar(), adjustedAccrualEndDate);
    
    double accrualYearFraction = getDayCount().getDayCountFraction(adjustedAccrualStartDate, adjustedAccrualEndDate, getAccrualPeriodAdjustmentParameters().getCalendar());
    
    CouponDefinition coupon = null;
    if (isCompounding()) {
//      throw new OpenGammaRuntimeException("Unsupported compounding OIS coupons");
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
      ZonedDateTime secondInterpolatedDate,
      double secondInterpolatedYearFraction) {
    final CouponDefinition coupon;
    if (hasGearing()) {
      throw new OpenGammaRuntimeException("Unsupported ibor gearing interpolated stub coupon");
    } else if (hasSpread()) {
      CouponDefinition fullCoupon = new CouponIborSpreadDefinition(
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
      coupon = InterpolatedStubCouponDefinition.from(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, secondInterpolatedDate, secondInterpolatedYearFraction);
//      coupon = CouponIborSpreadInterpolatedStubDefinition.from(
//          getCurrency(),
//          paymentDate,
//          accrualStartDate,
//          accrualEndDate,
//          accrualYearFraction,
//          notional,
//          fixingDate,
//          fixingPeriodStartDate,
//          fixingPeriodEndDate,
//          fixingPeriodYearFraction,
//          (IborIndex) _index,
//          _spread,
//          _adjustedResetDateParameters.getCalendar(),
//          initialRate,
//          firstInterpolatedDate,
//          firstInterpolatedYearFraction,
//          secondInterpolatedDate,
//          secondInterpolatedYearFraction);
    } else {
      CouponDefinition fullCoupon = new CouponIborDefinition(
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
      coupon = InterpolatedStubCouponDefinition.from(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, secondInterpolatedDate, secondInterpolatedYearFraction);
//      coupon = CouponIborInterpolatedStubDefinition.from(
//          getCurrency(),
//          paymentDate,
//          accrualStartDate,
//          accrualEndDate,
//          accrualYearFraction,
//          notional,
//          fixingDate,
//          fixingPeriodStartDate,
//          fixingPeriodEndDate,
//          fixingPeriodYearFraction,
//          (IborIndex) _index,
//          _adjustedResetDateParameters.getCalendar(),
//          initialRate,
//          firstInterpolatedDate,
//          firstInterpolatedYearFraction,
//          secondInterpolatedDate,
//          secondInterpolatedYearFraction);
    }
    return coupon;
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
      ZonedDateTime secondInterpolatedDate,
      double secondInterpolatedYearFraction) {
    final CouponDefinition coupon;
    if (hasGearing()) {
      throw new OpenGammaRuntimeException("Unsupported ibor compounding geared interpolated stub coupon");
    } else if (hasSpread()) {
      if (CompoundingMethod.FLAT == _compoundingMethod) {
        CouponDefinition fullCoupon = CouponIborCompoundingFlatSpreadDefinition.from(
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
        coupon = InterpolatedStubCouponDefinition.from(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, secondInterpolatedDate, secondInterpolatedYearFraction);
//        coupon = CouponIborCompoundingFlatSpreadInterpolatedStubDefinition.from(
//            getCurrency(),
//            paymentDate,
//            accrualStartDate,
//            accrualEndDate,
//            accrualYearFraction,
//            notional,
//            ((IborIndex) _index),
//            compoundAccrualStartDates,
//            compoundAccrualEndDates,
//            compoundAccrualYearFractions,
//            compoundFixingDates,
//            compoundFixingStartDates,
//            compoundFixingEndDates,
//            compoundFixingYearFractions,
//            _spread,
//            initialCompoundRate,
//            firstInterpolatedDate,
//            firstInterpolatedYearFraction,
//            secondInterpolatedDate,
//            secondInterpolatedYearFraction);
      } else {
        CouponDefinition fullCoupon = CouponIborCompoundingSpreadDefinition.from(
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
        coupon = InterpolatedStubCouponDefinition.from(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, secondInterpolatedDate, secondInterpolatedYearFraction);
//        coupon = CouponIborCompoundingSpreadInterpolatedStubDefinition.from(
//            getCurrency(),
//            paymentDate,
//            accrualStartDate,
//            accrualEndDate,
//            accrualYearFraction,
//            notional,
//            ((IborIndex) _index),
//            compoundAccrualStartDates,
//            compoundAccrualEndDates,
//            compoundAccrualYearFractions,
//            compoundFixingDates,
//            compoundFixingStartDates,
//            compoundFixingEndDates,
//            compoundFixingYearFractions,
//            _spread,
//            initialCompoundRate,
//            firstInterpolatedDate,
//            firstInterpolatedYearFraction,
//            secondInterpolatedDate,
//            secondInterpolatedYearFraction);
      }
    } else {
      CouponDefinition fullCoupon = CouponIborCompoundingDefinition.from(
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
      coupon = InterpolatedStubCouponDefinition.from(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, secondInterpolatedDate, secondInterpolatedYearFraction);
//      coupon = CouponIborCompoundingInterpolatedStubDefinition.from(
//          getCurrency(),
//          paymentDate,
//          accrualStartDate, 
//          accrualEndDate,
//          accrualYearFraction,
//          notional,
//          ((IborIndex) _index),
//          compoundAccrualStartDates,
//          compoundAccrualEndDates,
//          compoundAccrualYearFractions,
//          compoundFixingDates,
//          compoundFixingStartDates,
//          compoundFixingEndDates,
//          compoundFixingYearFractions,
//          initialCompoundRate,
//          firstInterpolatedDate,
//          firstInterpolatedYearFraction,
//          secondInterpolatedDate,
//          secondInterpolatedYearFraction);
    }
    return coupon;
  }  

  /**
   * 
   * @param paymentCalendar
   * @param fixingCalendar
   * @param isCompounding
   * @param accrualStartDate
   * @param accrualEndDate
   * @param fixingDate
   * @param fixingPeriodStartDate
   * @param fixingPeriodEndDate
   * @param paymentDate
   * @param fixingBusinessDayConvention
   * @param notional
   * @return
   * 
   * @deprecated old code
   */
  @Deprecated
  private CouponDefinition getNoSpreadInterpolatedStubCoupon(
      Calendar paymentCalendar,
      Calendar fixingCalendar,
      boolean isCompounding,
      ZonedDateTime accrualStartDate,
      ZonedDateTime accrualEndDate,
      ZonedDateTime fixingDate,
      ZonedDateTime fixingPeriodStartDate,
      ZonedDateTime fixingPeriodEndDate,
      ZonedDateTime paymentDate,
      BusinessDayConvention fixingBusinessDayConvention,
      double notional,
      CouponStub couponStub) {
    CouponDefinition coupon;
    
    ZonedDateTime firstInterpolatedDate;
    if (couponStub.getFirstInterpPeriod() != null) {
      firstInterpolatedDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, couponStub.getFirstInterpPeriod(), fixingBusinessDayConvention, fixingCalendar);
    } else {
      firstInterpolatedDate = ZonedDateTime.of(couponStub.getEffectiveDate(), LocalTime.of(0, 0), ZoneId.of("UTC"));
    }
    double firstInterpolatedYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, firstInterpolatedDate, fixingCalendar);
    
    ZonedDateTime secondInterpolatedDate = null;
    double secondInterpolatedYearFraction = Double.NaN;
    if (couponStub.getSecondInterpPeriod() != null) {
      secondInterpolatedDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, couponStub.getSecondInterpPeriod(), fixingBusinessDayConvention, fixingCalendar);
      secondInterpolatedYearFraction = getDayCount().getDayCountFraction(fixingPeriodStartDate, secondInterpolatedDate, fixingCalendar);
    }
    
    if (firstInterpolatedDate.equals(secondInterpolatedDate) || secondInterpolatedDate == null) {
      if (isCompounding) {
        ZonedDateTime[] compoundingAccrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
            accrualStartDate,
            accrualEndDate,
            ((IborIndex) _index).getTenor(),
            StubType.SHORT_START,
            getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
            paymentCalendar,
            getRollDateAdjuster() instanceof GeneralRollDateAdjuster ? null : getRollDateAdjuster()); // using DoM adjuster is messing up maturity date
        ZonedDateTime[] compoundingAccrualStartDates = new ZonedDateTime[compoundingAccrualEndDates.length];
        // we assume that the start date is unadjusted so adjust
        compoundingAccrualStartDates[0] = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
            paymentCalendar, accrualStartDate.with(getRollDateAdjuster()));
        System.arraycopy(compoundingAccrualEndDates, 0, compoundingAccrualStartDates, 1, compoundingAccrualEndDates.length - 1);
        
        double[] paymentAccrualFactors = new double[compoundingAccrualStartDates.length];
        double couponPaymentYearFrac = 0;
        for (int i = 0; i < paymentAccrualFactors.length; i++) {
          paymentAccrualFactors[i] = getDayCount().getDayCountFraction(compoundingAccrualStartDates[i], compoundingAccrualEndDates[i], paymentCalendar);
          couponPaymentYearFrac += paymentAccrualFactors[i];
        }
        
        ZonedDateTime[] compoundingFixingStartDates = compoundingAccrualStartDates;
        ZonedDateTime[] compoundingFixingEndDates = ScheduleCalculator.getAdjustedDateSchedule(
            compoundingFixingStartDates,
            ((IborIndex) _index).getTenor(), // we use the accrual freq, not the reset freq which is for generating coupon sub-periods
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar(), // This is using the fixing calendar instead of the reset calendar
            null); // getRollDateAdjuster()); // set to null for forward date roll bug
        compoundingFixingEndDates[0] = firstInterpolatedDate;
        
        double[] compoundingFixingYearFracs = new double[compoundingAccrualEndDates.length];
        for (int i = 0; i < compoundingAccrualEndDates.length; i++) {
          compoundingFixingYearFracs[i] = getDayCount().getDayCountFraction(compoundingFixingStartDates[i], compoundingFixingEndDates[i], fixingCalendar);
        }
        
        ZonedDateTime[] compoundingFixingDates;
        if (DateRelativeTo.START == _resetRelativeTo) {
          compoundingFixingDates = getFixingDates(compoundingFixingStartDates);
        } else {
          compoundingFixingDates = getFixingDates(compoundingFixingEndDates);
        }
        
        coupon = getIborCompoundingDefinition(
            notional,
            paymentDate,
            accrualStartDate,
            accrualEndDate,
            couponPaymentYearFrac,
            compoundingAccrualStartDates,
            compoundingAccrualEndDates,
            paymentAccrualFactors,
            compoundingFixingDates,
            compoundingFixingStartDates,
            compoundingFixingEndDates,
            compoundingFixingYearFracs,
            couponStub.getStubRate());

//        if (CompoundingMethod.FLAT == _compoundingMethod) {
//          coupon = CouponIborCompoundingFlatSpreadDefinition.from(
//              getNotional(),
//              accrualStartDate,
//              accrualEndDate,
//              (IborIndex) _index,
//              Double.isNaN(_spread) ? 0.0 : _spread,
//              StubType.SHORT_START, //(getStartStub() != null ? _startStubCompoundingStub : _endStubCompoundingStub),
//              paymentCalendar,
//              getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
//              fixingCalendar,
//              fixingBusinessDayConvention,
//              fixingCalendar,
//              _resetRelativeTo,
//              getPaymentDateRelativeTo(),
//              getRollDateAdjuster());
//        } else {
        
//          coupon = CouponIborCompoundingDefinition.from(
//              notional,
//              accrualStartDate,
//              accrualEndDate,
//              (IborIndex) _index,
//              StubType.SHORT_START, //(getStartStub() != null ? _startStubCompoundingStub : _endStubCompoundingStub),
//              getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
//              false,
//              fixingCalendar,
//              getRollDateAdjuster());
          
//        }
      } else {
        // TODO we default to CouponIborDefinition but we may have an initial stub rate
//        if (Double.isNaN(_initialStubRate)) {
          coupon = new CouponIborDefinition(
              getCurrency(),
              paymentDate,
              accrualStartDate,
              accrualEndDate,
              getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, paymentCalendar),
              notional,
              fixingDate,
              fixingPeriodStartDate,
              firstInterpolatedDate, // note we use the specified 'interpolated' date, rather than the actual fixingPeriodEndDate
              getDayCount().getDayCountFraction(fixingPeriodStartDate, firstInterpolatedDate, fixingCalendar),
              (IborIndex) _index,
              fixingCalendar);
//        } else {
//          coupon = new CouponFixedDefinition(
//              getCurrency(),
//              paymentDate,
//              accrualStartDate,
//              accrualEndDate,
//              getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, paymentCalendar),
//              notional,
//              _initialStubRate);
//        }
      }
    } else if (!accrualEndDate.isAfter(firstInterpolatedDate) || !accrualEndDate.isBefore(secondInterpolatedDate)) {
      // TODO this is messing up 1163311, which is not compounding the stub properly, first stub is full 3M rather than interped
      if (isCompounding) {
        ZonedDateTime[] compoundingAccrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(
            accrualStartDate,
            accrualEndDate,
            ((IborIndex) _index).getTenor(),
            StubType.SHORT_START,
            getAccrualPeriodAdjustmentParameters().getBusinessDayConvention(),
            paymentCalendar,
            getRollDateAdjuster());
        ZonedDateTime[] compoundingAccrualStartDates = new ZonedDateTime[compoundingAccrualEndDates.length];
        if (getRollDateAdjuster() instanceof EndOfMonthRollDateAdjuster) {
          accrualStartDate = accrualStartDate.with(getRollDateAdjuster());
        }
        compoundingAccrualStartDates[0] = getAccrualPeriodAdjustmentParameters().getBusinessDayConvention().adjustDate(
            paymentCalendar, accrualStartDate);
        System.arraycopy(compoundingAccrualEndDates, 0, compoundingAccrualStartDates, 1, compoundingAccrualEndDates.length - 1);
        
        double[] paymentAccrualFactors = new double[compoundingAccrualStartDates.length];
        double couponPaymentYearFrac = 0;
        for (int i = 0; i < paymentAccrualFactors.length; i++) {
          paymentAccrualFactors[i] = getDayCount().getDayCountFraction(compoundingAccrualStartDates[i], compoundingAccrualEndDates[i], paymentCalendar);
          couponPaymentYearFrac += paymentAccrualFactors[i];
        }
            
        ZonedDateTime[] compoundingFixingStartDates = compoundingAccrualStartDates;
        ZonedDateTime[] compoundingFixingEndDates = ScheduleCalculator.getAdjustedDateSchedule(
            compoundingFixingStartDates,
            ((IborIndex) _index).getTenor(), // we use the accrual freq, not the reset freq which is for generating coupon sub-periods
            _adjustedResetDateParameters.getBusinessDayConvention(), // TODO check that we should be using the reset date bdc // getFixingBusinessDayConvention(),
            _adjustedFixingDateParameters.getCalendar(), // This is using the fixing calendar instead of the reset calendar
            null); // getRollDateAdjuster()); // set to null for forward date roll bug
        compoundingFixingEndDates[0] = compoundingAccrualEndDates[0];

        double[] compoundingFixingYearFracs = new double[compoundingAccrualEndDates.length];
        for (int i = 0; i < compoundingAccrualEndDates.length; i++) {
          compoundingFixingYearFracs[i] = getDayCount().getDayCountFraction(compoundingFixingStartDates[i], compoundingFixingEndDates[i], fixingCalendar);
        }

        ZonedDateTime[] compoundingFixingDates;
        if (DateRelativeTo.START == _resetRelativeTo) {
          compoundingFixingDates = getFixingDates(compoundingFixingStartDates);
        } else {
          compoundingFixingDates = getFixingDates(compoundingFixingEndDates);
        }

        coupon = null;
//        coupon = CouponIborCompoundingInterpolatedStubDefinition.from(
//            getCurrency(),
//            paymentDate,
//            accrualStartDate,
//            accrualEndDate,
//            couponPaymentYearFrac,
//            notional,
//            ((IborIndex) _index),
//            compoundingAccrualStartDates,
//            compoundingAccrualEndDates,
//            paymentAccrualFactors,
//            compoundingFixingDates,
//            compoundingFixingStartDates,
//            compoundingFixingEndDates,
//            compoundingFixingYearFracs,
//            couponStub.getStubRate(),
//            firstInterpolatedDate,
//            firstInterpolatedYearFraction,
//            secondInterpolatedDate,
//            secondInterpolatedYearFraction);
      } else {
        coupon = new CouponIborDefinition(
            getCurrency(),
            paymentDate,
            accrualStartDate,
            accrualEndDate,
            getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, paymentCalendar),
            notional,
            fixingDate,
            fixingPeriodStartDate,
            fixingPeriodEndDate,
            getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, fixingCalendar),
            (IborIndex) _index,
            fixingCalendar);
      }
    } else {
      coupon = null;
//      coupon = CouponIborInterpolatedStubDefinition.from(
//          getCurrency(),
//          paymentDate,
//          accrualStartDate,
//          accrualEndDate,
//          getDayCount().getDayCountFraction(accrualStartDate, accrualEndDate, paymentCalendar),
//          notional,
//          fixingDate,
//          fixingPeriodStartDate,
//          accrualEndDate,
//          getDayCount().getDayCountFraction(fixingPeriodStartDate, accrualEndDate, fixingCalendar),
//          (IborIndex) _index,
//          fixingCalendar,
//          couponStub.getStubRate(),
//          firstInterpolatedDate,
//          firstInterpolatedYearFraction,
//          secondInterpolatedDate,
//          secondInterpolatedYearFraction);
    }
    return coupon;
  }
}
