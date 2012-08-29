/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.CouponFrequency;
import com.opengamma.analytics.financial.credit.CreditRating;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.Region;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.ScheduleGenerationMethod;
import com.opengamma.analytics.financial.credit.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Definition of a vanilla legacy Credit Default Swap contract (i.e. transacted prior to Big Bang of April 2009)
 */
public class CreditDefaultSwapDefinition {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables of the CDS contract (defines what a CDS is)

  // Cashflow Conventions are assumed to be as below

  // Notional amount > 0 always - long/short positions are captured by the setting of 'BuySellProtection'

  // Buy protection   -> Pay premium leg, receive contingent leg  -> 'long' protection  -> 'short' credit risk
  // Sell protection  -> Receive premium leg, pay contingent leg  -> 'short' protection -> 'long' credit risk

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : How can we reduce the number of parameters in the call to the constructor?
  // TODO : Convert _couponFrequency string to an enum
  // TODO : Convert _daycountFractionConvention string to an enum
  // TODO : Convert _businessdayAdjustmentConvention string to an enum
  // TODO : Should really have more identifiers for the counterparty we are trading with

  // TODO : Add a seperate 'SurvivalCurve' class whose function is to generate the calibrated survival probabilities from the input CDS par spread term structure
  // TODO : Should the yield, survival and rating curves be part of the CDS object? Should they be passed in to the pricer seperately as market data?

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // From the users perspective, are we buying or selling protection - User input
  private final BuySellProtection _buySellProtection;

  // Identifiers for the (three) counterparties in the trade - User input
  private final String _protectionBuyer;
  private final String _protectionSeller;
  private final String _referenceEntityTicker;
  private final String _referenceEntityShortName;
  private final String _referenceEntityREDCode;

  // The currency the trade is executed in e.g. USD - User input
  private final Currency _currency;

  // The seniority of the debt of the reference entity the CDS is written on - User input, see enum
  private final DebtSeniority _debtSeniority;

  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt - User input, see enum
  private final RestructuringClause _restructuringClause;

  // The composite credit (curve) rating of the reference entity (MarkIt field)
  private final CreditRating _compositeRating;

  // The implied credit rating of the reference entity (MarkIt field)
  private final CreditRating _impliedRating;

  // The industrial classification of the reference entity
  private final Sector _sector;

  // The geographical domicile of the reference entity
  private final Region _region;

  // The country of domicile of the reference entity
  private final String _country;

  // Holiday calendar for the determination of adjusted business days in the cashflow schedule - User input
  private final Calendar _calendar;

  // The date of the contract inception - User input
  private final ZonedDateTime _startDate;

  // The effective date for protection to begin (usually T + 1 for legacy CDS) - User input
  private final ZonedDateTime _effectiveDate;

  // The maturity date of the contract (when premium and protection coverage ceases) - User input
  private final ZonedDateTime _maturityDate;

  // The date on which we want to calculate the CDS MtM - User input
  private final ZonedDateTime _valuationDate;

  // The method for generating the schedule of premium payments - User input see enum
  private final ScheduleGenerationMethod _scheduleGenerationMethod;

  // The frequency of coupon payments (usually quarterly) - User input see enum
  private final CouponFrequency _couponFrequency;

  // Day-count convention (usually Act/360) - User input
  private final String _daycountFractionConvention;

  // Business day adjustment convention (usually following) - User input
  private final String _businessdayAdjustmentConvention;

  // Flag to determine if we business day adjust the final maturity date - User input
  private final boolean _adjustMaturityDate;

  // The trade notional (in the trade currency), convention is that this will always be a positive amount - User input
  private final double _notional;

  // The quoted par spread of the trade (as this is a legacy CDS, this is the coupon applied to the premium leg 
  // to give the trade a value of zero at the contract startDate (there is no exchange of payments upfront) - User input 
  private final double _parSpread;

  // The recovery rate to be used in the calculation of the CDS MtM (can be different to the rate used to calibrate the survival curve) - User input
  private final double _valuationRecoveryRate;

  // The recovery rate to be used when calibrating the hazard rate term structure to the market par CDS spread quotes - User input
  private final double _curveRecoveryRate;

  // Flag to determine whether the accrued coupons should be included in the CDS premium leg calculation - User input
  private final boolean _includeAccruedPremium;

  // The number of integration steps (per year) - for the computation of the integral in the contingent leg - User input
  private final int _numberOfIntegrationSteps;

  // The yield curve object for discount factors - Constructed from market data
  private final YieldCurve _yieldCurve;

  // The survival curve object for survival probabilities (proxy with a YieldCurve object for now) - Constructed from market data
  private final YieldCurve _survivalCurve;

  // The term structure of rating of the reference entity (supplied by MarkIt) - proxy with a simple YieldCurve object for now
  private final YieldCurve _ratingCurve;

  // The credit key to uniquely identify a reference entities par spread CDS curve
  private final String _creditKey;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Constructor for a CDS definition object (most fields are user specified)

  // Probably not the best way of handling schedule generation, business day conventions etc - need to look at another product to see how it is done

  // How can we reduce the number of parameters?

  public CreditDefaultSwapDefinition(BuySellProtection buySellProtection,
      String protectionBuyer,
      String protectionSeller,
      String referenceEntityTicker,
      String referenceEntityShortName,
      String referenceEntityREDCode,
      Currency currency,
      DebtSeniority debtSeniority,
      RestructuringClause restructuringClause,
      CreditRating compositeRating,
      CreditRating impliedRating,
      Sector sector,
      Region region,
      String country,
      Calendar calendar,
      ZonedDateTime startDate,
      ZonedDateTime effectiveDate,
      ZonedDateTime maturityDate,
      ZonedDateTime valuationDate,
      ScheduleGenerationMethod scheduleGenerationMethod,
      CouponFrequency couponFrequency,
      String daycountFractionConvention,
      String businessdayAdjustmentConvention,
      boolean adjustMaturityDate,
      double notional,
      double parSpread,
      double valuationRecoveryRate,
      double curveRecoveryRate,
      boolean includeAccruedPremium,
      int numberOfIntegrationSteps,
      YieldCurve yieldCurve,
      YieldCurve survivalCurve,
      YieldCurve ratingCurve) {

    // TODO : Fix argument checkers

    ArgumentChecker.notNull(buySellProtection, "Buy/Sell field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(protectionBuyer, "Protection buyer field is null");
    ArgumentChecker.isFalse(protectionBuyer.isEmpty(), "Protection buyer field is empty");

    ArgumentChecker.notNull(protectionSeller, "Protection seller field is null");
    ArgumentChecker.isFalse(protectionSeller.isEmpty(), "Protection seller field is empty");

    ArgumentChecker.notNull(referenceEntityTicker, "Reference entity ticker field is null");
    ArgumentChecker.isFalse(referenceEntityTicker.isEmpty(), "Reference entity ticker field is empty");

    ArgumentChecker.notNull(referenceEntityShortName, "Reference entity short name field is null");
    ArgumentChecker.isFalse(referenceEntityShortName.isEmpty(), "Reference entity short name field is empty");

    ArgumentChecker.notNull(referenceEntityREDCode, "Reference entity RED code field is null");
    ArgumentChecker.isFalse(referenceEntityREDCode.isEmpty(), "Reference entity RED code field is empty");

    ArgumentChecker.notNull(currency, "Currency field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(debtSeniority, "Debt seniority field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(restructuringClause, "Restructuring clause field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(compositeRating, "Composite rating field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(impliedRating, "Implied rating field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(sector, "Sector field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(region, "Region field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(country, "Country field is null");
    ArgumentChecker.isFalse(country.isEmpty(), "Country field is empty");

    ArgumentChecker.notNull(calendar, "Calendar field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(startDate, "Start date field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(effectiveDate, "Effective date field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(maturityDate, "Maturity date field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(valuationDate, "Valuation date field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(scheduleGenerationMethod, "Schedule generation method field is null");
    // Do we need to check for ""?

    ArgumentChecker.notNull(couponFrequency, "Coupon frequency field is null");
    // Do we need to check for ""?

    // TODO: Add the checks for daycountConvention and businessdayAdjustmentConvention when replace the strings with enums

    // TODO: Verify don't have to check boolean adjustMaturityDate for null

    ArgumentChecker.isTrue(notional >= 0.0, "Notional amount should be greater than or equal to zero");
    ArgumentChecker.isTrue(parSpread >= 0.0, "CDS par spread should be greater than or equal to zero");

    //ArgumentChecker.isTrue(valuationRecoveryRate >= 0.0, "Valuation Recovery Rate should be greater than or equal to 0%");
    //ArgumentChecker.isTrue(valuationRecoveryRate <= 1.0, "Valuation Recovery Rate should be less than or equal to 100%");

    //ArgumentChecker.isInRangeInclusive(valuationRecoveryRate, 0.0, 1.0);
    //ArgumentChecker.isInRangeInclusive(curveRecoveryRate, 0.0, 1.0);

    // TODO : Add the logical checks for the ordering of the dates
    // TODO : Are there any logical date checks I have missed?
    //ArgumentChecker.isTrue(startDate.isBefore(valuationDate), "Start date {} must be before valuation date {}", startDate, valuationDate);
    //ArgumentChecker.isTrue(startDate.isBefore(effectiveDate), "Start date {} must be before effective date {}", startDate, effectiveDate);
    //ArgumentChecker.isTrue(startDate.isBefore(maturityDate), "Start date {} must be before maturity date {}", startDate, maturityDate);

    //ArgumentChecker.isTrue(valuationDate.isBefore(maturityDate), "Valuation date {} must be before maturity date {}", valuationDate, maturityDate);
    //ArgumentChecker.isTrue(valuationDate.isAfter(effectiveDate), "Valuation date {} must be after effective date {}", valuationDate, effectiveDate);

    /*
    // What is the return message here?
    ArgumentChecker.isInRangeInclusive(valuationRecoveryRate, 0.0, 1.0);
    ArgumentChecker.isInRangeInclusive(curveRecoveryRate, 0.0, 1.0);
    
    // TODO : How do we check the boolean primitives?
    
    // TODO : Should we impose an upper limit on the number of integration steps?
    ArgumentChecker.isTrue(numberOfIntegrationSteps > 0,  "Number of integration steps (for contingent leg valuation) should be greater than zero");
    
    // TODO : Do we need to check if the yieldCurve and survivalCurve objects are empty? Yes
     */

    // Assign the member variables

    _buySellProtection = buySellProtection;
    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _referenceEntityTicker = referenceEntityTicker;
    _referenceEntityShortName = referenceEntityShortName;
    _referenceEntityREDCode = referenceEntityREDCode;

    _currency = currency;
    _debtSeniority = debtSeniority;
    _restructuringClause = restructuringClause;

    _compositeRating = compositeRating;
    _impliedRating = impliedRating;

    _sector = sector;
    _region = region;
    _country = country;

    _calendar = calendar;

    _startDate = startDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _valuationDate = valuationDate;

    _scheduleGenerationMethod = scheduleGenerationMethod;
    _couponFrequency = couponFrequency;
    _daycountFractionConvention = daycountFractionConvention;
    _businessdayAdjustmentConvention = businessdayAdjustmentConvention;
    _adjustMaturityDate = adjustMaturityDate;

    _notional = notional;
    _parSpread = parSpread;

    _valuationRecoveryRate = valuationRecoveryRate;
    _curveRecoveryRate = curveRecoveryRate;

    _includeAccruedPremium = includeAccruedPremium;

    _numberOfIntegrationSteps = numberOfIntegrationSteps;

    _yieldCurve = yieldCurve;
    _survivalCurve = survivalCurve;
    _ratingCurve = ratingCurve;

    //REVIEW 29/8/2012 think about using UniqueId
    _creditKey = _referenceEntityTicker + ":" + _currency + ":" + _debtSeniority + ":" + _restructuringClause;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public BuySellProtection getBuySellProtection() {
    return _buySellProtection;
  }

  public String getProtectionBuyer() {
    return _protectionBuyer;
  }

  public String getProtectionSeller() {
    return _protectionSeller;
  }

  public String getReferenceEntityTicker() {
    return _referenceEntityTicker;
  }

  public String getReferenceEntityShortName() {
    return _referenceEntityShortName;
  }

  public String getReferenceEntityREDCode() {
    return _referenceEntityREDCode;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public Currency getCurrency() {
    return _currency;
  }

  public DebtSeniority getDebtSeniority() {
    return _debtSeniority;
  }

  public RestructuringClause getRestructuringClause() {
    return _restructuringClause;
  }

  public CreditRating getCompositeRating() {
    return _compositeRating;
  }

  public CreditRating getImpliedRating() {
    return _impliedRating;
  }

  public Sector getSector() {
    return _sector;
  }

  public Region getRegion() {
    return _region;
  }

  public String getCountry() {
    return _country;
  }

  public Calendar getCalendar() {
    return _calendar;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public ZonedDateTime getValuationDate() {
    return _valuationDate;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public ScheduleGenerationMethod getScheduleGenerationMethod() {
    return _scheduleGenerationMethod;
  }

  public CouponFrequency getCouponFrequency() {
    return _couponFrequency;
  }

  public String getDayCountFractionConvention() {
    return _daycountFractionConvention;
  }

  public String getBusinessDayAdjustmentConvention() {
    return _businessdayAdjustmentConvention;
  }

  public boolean getAdjustMaturityDate() {
    return _adjustMaturityDate;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public double getNotional() {
    return _notional;
  }

  public double getParSpread() {
    return _parSpread;
  }

  public double getValuationRecoveryRate() {
    return _valuationRecoveryRate;
  }

  public double getCurveRecoveryRate() {
    return _curveRecoveryRate;
  }

  public boolean getIncludeAccruedPremium() {
    return _includeAccruedPremium;
  }

  public int getNumberOfIntegrationSteps() {
    return _numberOfIntegrationSteps;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public YieldCurve getYieldCurve() {
    return _yieldCurve;
  }

  public YieldCurve getSurvivalCurve() {
    return _survivalCurve;
  }

  public YieldCurve getRatingCurve() {
    return _ratingCurve;
  }

  //----------------------------------------------------------------------------------------------------------------------------------------

  public String getCreditKey() {
    return _creditKey;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_adjustMaturityDate ? 1231 : 1237);
    result = prime * result + ((_businessdayAdjustmentConvention == null) ? 0 : _businessdayAdjustmentConvention.hashCode());
    result = prime * result + ((_buySellProtection == null) ? 0 : _buySellProtection.hashCode());
    result = prime * result + ((_calendar == null) ? 0 : _calendar.hashCode());
    result = prime * result + ((_compositeRating == null) ? 0 : _compositeRating.hashCode());
    result = prime * result + ((_country == null) ? 0 : _country.hashCode());
    result = prime * result + ((_couponFrequency == null) ? 0 : _couponFrequency.hashCode());
    result = prime * result + ((_creditKey == null) ? 0 : _creditKey.hashCode());
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_curveRecoveryRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_daycountFractionConvention == null) ? 0 : _daycountFractionConvention.hashCode());
    result = prime * result + ((_debtSeniority == null) ? 0 : _debtSeniority.hashCode());
    result = prime * result + ((_effectiveDate == null) ? 0 : _effectiveDate.hashCode());
    result = prime * result + ((_impliedRating == null) ? 0 : _impliedRating.hashCode());
    result = prime * result + (_includeAccruedPremium ? 1231 : 1237);
    result = prime * result + ((_maturityDate == null) ? 0 : _maturityDate.hashCode());
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _numberOfIntegrationSteps;
    temp = Double.doubleToLongBits(_parSpread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_protectionBuyer == null) ? 0 : _protectionBuyer.hashCode());
    result = prime * result + ((_protectionSeller == null) ? 0 : _protectionSeller.hashCode());
    result = prime * result + ((_ratingCurve == null) ? 0 : _ratingCurve.hashCode());
    result = prime * result + ((_referenceEntityREDCode == null) ? 0 : _referenceEntityREDCode.hashCode());
    result = prime * result + ((_referenceEntityShortName == null) ? 0 : _referenceEntityShortName.hashCode());
    result = prime * result + ((_referenceEntityTicker == null) ? 0 : _referenceEntityTicker.hashCode());
    result = prime * result + ((_region == null) ? 0 : _region.hashCode());
    result = prime * result + ((_restructuringClause == null) ? 0 : _restructuringClause.hashCode());
    result = prime * result + ((_scheduleGenerationMethod == null) ? 0 : _scheduleGenerationMethod.hashCode());
    result = prime * result + ((_sector == null) ? 0 : _sector.hashCode());
    result = prime * result + ((_startDate == null) ? 0 : _startDate.hashCode());
    result = prime * result + ((_survivalCurve == null) ? 0 : _survivalCurve.hashCode());
    result = prime * result + ((_valuationDate == null) ? 0 : _valuationDate.hashCode());
    temp = Double.doubleToLongBits(_valuationRecoveryRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_yieldCurve == null) ? 0 : _yieldCurve.hashCode());
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CreditDefaultSwapDefinition other = (CreditDefaultSwapDefinition) obj;
    if (_adjustMaturityDate != other._adjustMaturityDate)
      return false;
    if (_businessdayAdjustmentConvention == null) {
      if (other._businessdayAdjustmentConvention != null)
        return false;
    } else if (!_businessdayAdjustmentConvention.equals(other._businessdayAdjustmentConvention))
      return false;
    if (_buySellProtection != other._buySellProtection)
      return false;
    if (_calendar == null) {
      if (other._calendar != null)
        return false;
    } else if (!_calendar.equals(other._calendar))
      return false;
    if (_compositeRating != other._compositeRating)
      return false;
    if (_country == null) {
      if (other._country != null)
        return false;
    } else if (!_country.equals(other._country))
      return false;
    if (_couponFrequency != other._couponFrequency)
      return false;
    if (_creditKey == null) {
      if (other._creditKey != null)
        return false;
    } else if (!_creditKey.equals(other._creditKey))
      return false;
    if (_currency == null) {
      if (other._currency != null)
        return false;
    } else if (!_currency.equals(other._currency))
      return false;
    if (Double.doubleToLongBits(_curveRecoveryRate) != Double.doubleToLongBits(other._curveRecoveryRate))
      return false;
    if (_daycountFractionConvention == null) {
      if (other._daycountFractionConvention != null)
        return false;
    } else if (!_daycountFractionConvention.equals(other._daycountFractionConvention))
      return false;
    if (_debtSeniority != other._debtSeniority)
      return false;
    if (_effectiveDate == null) {
      if (other._effectiveDate != null)
        return false;
    } else if (!_effectiveDate.equals(other._effectiveDate))
      return false;
    if (_impliedRating != other._impliedRating)
      return false;
    if (_includeAccruedPremium != other._includeAccruedPremium)
      return false;
    if (_maturityDate == null) {
      if (other._maturityDate != null)
        return false;
    } else if (!_maturityDate.equals(other._maturityDate))
      return false;
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional))
      return false;
    if (_numberOfIntegrationSteps != other._numberOfIntegrationSteps)
      return false;
    if (Double.doubleToLongBits(_parSpread) != Double.doubleToLongBits(other._parSpread))
      return false;
    if (_protectionBuyer == null) {
      if (other._protectionBuyer != null)
        return false;
    } else if (!_protectionBuyer.equals(other._protectionBuyer))
      return false;
    if (_protectionSeller == null) {
      if (other._protectionSeller != null)
        return false;
    } else if (!_protectionSeller.equals(other._protectionSeller))
      return false;
    if (_ratingCurve == null) {
      if (other._ratingCurve != null)
        return false;
    } else if (!_ratingCurve.equals(other._ratingCurve))
      return false;
    if (_referenceEntityREDCode == null) {
      if (other._referenceEntityREDCode != null)
        return false;
    } else if (!_referenceEntityREDCode.equals(other._referenceEntityREDCode))
      return false;
    if (_referenceEntityShortName == null) {
      if (other._referenceEntityShortName != null)
        return false;
    } else if (!_referenceEntityShortName.equals(other._referenceEntityShortName))
      return false;
    if (_referenceEntityTicker == null) {
      if (other._referenceEntityTicker != null)
        return false;
    } else if (!_referenceEntityTicker.equals(other._referenceEntityTicker))
      return false;
    if (_region != other._region)
      return false;
    if (_restructuringClause != other._restructuringClause)
      return false;
    if (_scheduleGenerationMethod != other._scheduleGenerationMethod)
      return false;
    if (_sector != other._sector)
      return false;
    if (_startDate == null) {
      if (other._startDate != null)
        return false;
    } else if (!_startDate.equals(other._startDate))
      return false;
    if (_survivalCurve == null) {
      if (other._survivalCurve != null)
        return false;
    } else if (!_survivalCurve.equals(other._survivalCurve))
      return false;
    if (_valuationDate == null) {
      if (other._valuationDate != null)
        return false;
    } else if (!_valuationDate.equals(other._valuationDate))
      return false;
    if (Double.doubleToLongBits(_valuationRecoveryRate) != Double.doubleToLongBits(other._valuationRecoveryRate))
      return false;
    if (_yieldCurve == null) {
      if (other._yieldCurve != null)
        return false;
    } else if (!_yieldCurve.equals(other._yieldCurve))
      return false;
    return true;
  }

  //---------------------------------------------------------------------------------------------------------------------------------------- 
}

//----------------------------------------------------------------------------------------------------------------------------------------
