/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.time.Expiry;

/**
 * A {@code Security} used to model municipal bonds - NOT YET IMPLEMENTED.
 */
public class MunicipalBondSecurity extends BondSecurity {

  /**
   * Constructor.
   */
  // CSOFF: We need lots of parameters
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
    throw new NotImplementedException();
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(BondSecurityVisitor<T> visitor) {
    return visitor.visitMunicipalBondSecurity(this);
  }

}
