/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Definition of a vanilla legacy Credit Default Swap contract (i.e. transacted prior to Big Bang 8th April 2009)
 */
public class CreditDefaultSwapDefinition {
  
  //----------------------------------------------------------------------------------------------------------------------------------------
  
  // Member variables of the CDS contract (defines what a CDS is)

  // From the users perspective, are we buying or selling protection
  private final String _buySellProtection;

  // Identifiers for the (three) counterparties in the trade
  private final String _protectionBuyer;
  private final String _protectionSeller;
  private final String _referenceEntity;
  
  // The currency the trade is executed in e.g. USD
  private final Currency _currency;
  
  // The seniority of the debt of the reference entity the CDS is written on (e.g. senior or subordinated)
  private final String _debtSeniority;
  
  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt (e.g. OR, MR, MMR or NR)
  private final String _restructuringClause;
  
  // Holiday calendar for the determination of adjusted business days in the cashflow schedule
  private final Calendar _calendar;
  
  // The date of the contract inception
  private final ZonedDateTime _startDate;
  
  // The effective date for protection to begin (usually T + 1 for legacy CDS)
  private final ZonedDateTime _effectiveDate;
  
  // The maturity date of the contract (when premium and protection coverage ceases)
  private final ZonedDateTime _maturityDate;
  
  // The date on which we want to calculate the CDS MtM
  private final ZonedDateTime _valuationDate;
  
  // The method for generating the schedule of premium payments
  private final String _scheduleGenerationMethod;
  
  // The frequency of coupon payments (usually quarterly)
  private final String _couponFrequency;
  
  // Day-count convention (usually Act/360)
  private final String _daycountFractionConvention;
  
  // Business day adjustment convention (usually following)
  private final String _businessdayAdjustmentConvention;
  
  // Flag to determine if we business day adjust the final maturity date
  private final boolean _adjustMaturityDate;
  
  // The trade notional (in the trade currency)
  private final double _notional;
  
  // The quoted par spread of the trade (as this is a legacy CDS, this is the coupon applied to the premium leg 
  // to give the trade a value of zero at the contract start date (there is no exchange of payments upfront) 
  private final double _parSpread;
  
  // The recovery rate to be used in the calculation of the CDS MtM (can be different to the rate used to calibrate the survival curve)
  private final double _valuationRecoveryRate;
  
  // The recovery rate to be used when calibrating the hazard rate term structure to the market par CDS spread quotes
  private final double _curveRecoveryRate;
  
  // Flag to determine whether the accrued coupons should be included in the CDS premium lag calculation
  private final boolean _includeAccruedPremium;
  
  // The yield curve object for discount factors
  private final YieldCurve _yieldCurve;
  
  // TODO : Add the survival curve (assuming that we want the survival probabilities as an input to the pricer - not the par CDS spread term struct)
  
  // TODO : Should the yield and survival curves be part of the CDS object? Should they be passed in to the pricer seperately?
  
  // ----------------------------------------------------------------------------------------------------------------------------------------
  
  // Constructor for a CDS definition object (all fields are user specified)
 
  // Probably not the best way of handling schedule generation, business day conventions etc - need to look at another product to see how it is done
  
  // How can we reduce the number of parameters?
  
  public CreditDefaultSwapDefinition(String buySellProtection,
                                      String protectionBuyer, 
                                      String protectionSeller, 
                                      String referenceEntity, 
                                      Currency currency, 
                                      String debtSeniority, 
                                      String restructuringClause, 
                                      Calendar calendar,
                                      ZonedDateTime startDate,
                                      ZonedDateTime effectiveDate,
                                      ZonedDateTime maturityDate,
                                      ZonedDateTime valuationDate,
                                      String scheduleGenerationMethod,
                                      String couponFrequency,
                                      String daycountFractionConvention,
                                      String businessdayAdjustmentConvention,
                                      double notional, 
                                      double parSpread, 
                                      double valuationRecoveryRate, 
                                      double curveRecoveryRate, 
                                      boolean includeAccruedPremium,
                                      boolean adjustMaturityDate,
                                      YieldCurve yieldCurve) {
    
    ArgumentChecker.isTrue(buySellProtection.isEmpty(), "Buy/Sell protection flag is empty");
    
    /*
    ArgumentChecker.isTrue(protectionSeller.isEmpty(), "Protection seller field is empty");
    ArgumentChecker.isTrue(protectionBuyer.isEmpty(), "Protection buyer field is empty");
    ArgumentChecker.isTrue(referenceEntity.isEmpty(), "Reference entity field is empty");
    
    ArgumentChecker.isTrue(debtSeniority.isEmpty(), "Debt seniority field is empty");
    ArgumentChecker.isTrue(restructuringClause.isEmpty(), "Restructuring clause field is empty");
    
    ArgumentChecker.isTrue(scheduleGenerationMethod.isEmpty(), "Schedule generation method field is empty");
    ArgumentChecker.isTrue(couponFrequency.isEmpty(), "Coupon frequency field is empty");
    ArgumentChecker.isTrue(daycountFractionConvention.isEmpty(), "Daycount fraction convention field is empty");
    ArgumentChecker.isTrue(businessdayAdjustmentConvention.isEmpty(), "Business day adjustment convention field is empty");
    
    
    //ArgumentChecker.isTrue(startdate.isBefore(valuationdate), "Start date {} must be before valuation date {}", startdate, valuationdate);
    
    */
    
    // TODO : Add all the argument checkers
    
    ArgumentChecker.isTrue(notional >= 0.0,  "Notional amount should be greater than or equal to zero");
    ArgumentChecker.isTrue(parSpread >= 0.0,  "CDS par spread should be greater than or equal to zero");
    
    ArgumentChecker.isInRangeInclusive(valuationRecoveryRate, 0.0, 1.0);
    ArgumentChecker.isInRangeInclusive(curveRecoveryRate, 0.0, 1.0);
    
    _buySellProtection = buySellProtection;
    _protectionBuyer = protectionBuyer;
    _protectionSeller = protectionSeller;
    _referenceEntity = referenceEntity;
    
    _currency = currency;
    _debtSeniority = debtSeniority;
    _restructuringClause = restructuringClause;
    
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
    
    _yieldCurve = yieldCurve;                         
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------

  public String getBuySellProtection() {
    return _buySellProtection;
  }
   
  public String getProtectionBuyer() {
    return _protectionBuyer;
  }
   
  public String getProtectionSeller() {
    return _protectionSeller;
  }
   
  public String getReferenceEntity() {
    return _referenceEntity;
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public String getDebtSeniority() {
    return _debtSeniority;
  }
  
  public String getRestructuringClause() {
    return _restructuringClause;
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
  
  public String getScheduleGenerationMethod() {
    return _scheduleGenerationMethod;
  }
  
  public String getCouponFrequency() {
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
    //System.out.println("Notional = " + _notional);
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

  //----------------------------------------------------------------------------------------------------------------------------------------
  
  YieldCurve getYieldCurve() {
    return _yieldCurve;
  }
}

//----------------------------------------------------------------------------------------------------------------------------------------
