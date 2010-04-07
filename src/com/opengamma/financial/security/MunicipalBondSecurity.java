/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.time.Expiry;

/**
 * This is a placeholder class - Municipal bonds aren't supported at the moment.
 *
 * @author Andrew
 */
public class MunicipalBondSecurity extends BondSecurity {

  /**
   * @param issuerName
   * @param issuerType
   * @param issuerDomicile
   * @param market
   * @param currency
   * @param yieldConvention
   * @param guaranteeType
   * @param maturity
   * @param couponType
   * @param couponRate
   * @param couponFrequency
   * @param dayCountConvention
   * @param businessDayConvention
   * @param announcementDate
   * @param interestAccrualDate
   * @param settlementDate
   * @param firstCouponDate
   * @param issuancePrice
   * @param totalAmountIssued
   * @param minimumAmount
   * @param minimumIncrement
   * @param parAmount
   * @param redemptionValue
   */
  public MunicipalBondSecurity(String issuerName, String issuerType,
      String issuerDomicile, String market, Currency currency,
      YieldConvention yieldConvention, String guaranteeType, Expiry maturity,
      String couponType, double couponRate, Frequency couponFrequency,
      DayCount dayCountConvention, BusinessDayConvention businessDayConvention,
      LocalDate announcementDate, LocalDate interestAccrualDate,
      LocalDate settlementDate, LocalDate firstCouponDate,
      double issuancePrice,
      double totalAmountIssued, double minimumAmount, double minimumIncrement,
      double parAmount, double redemptionValue) {
    super(issuerName, issuerType, issuerDomicile, market, currency,
        yieldConvention, guaranteeType, maturity, couponType, couponRate,
        couponFrequency, dayCountConvention, businessDayConvention,
        announcementDate, interestAccrualDate, settlementDate, firstCouponDate,
        issuancePrice, totalAmountIssued, minimumAmount,
        minimumIncrement, parAmount, redemptionValue);
    throw new OpenGammaRuntimeException ("not implemented");
  }

  @Override
  public <T> T accept(BondSecurityVisitor<T> visitor) {
    return visitor.visitMunicipalBondSecurity (this);
  }
  
}