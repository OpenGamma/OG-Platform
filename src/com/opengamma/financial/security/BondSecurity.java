/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public abstract class BondSecurity extends FinancialSecurity {
  
  public static final String BOND_TYPE = "BOND";
  
  private final Expiry _maturity;
  private final double _coupon;
  private final BondFrequency _frequency;
  private final String _country;
  private final String _creditRating;
  private final Currency _currency;
  private final String _issuer;
  private final DayCount _dayCountConvention;
  private final BusinessDayConvention _businessDayConvention;
  
  public BondSecurity (final Expiry maturity, final double coupon, final BondFrequency frequency, final String country, final String creditRating, final Currency currency, final String issuer, final DayCount dayCountConvention, final BusinessDayConvention businessDayConvention) {
    _maturity = maturity;
    _coupon = coupon;
    _frequency = frequency;
    _country = country;
    _creditRating = creditRating;
    _currency = currency;
    _issuer = issuer;
    _dayCountConvention = dayCountConvention;
    _businessDayConvention = businessDayConvention;
    setSecurityType(BOND_TYPE);
  }

  /**
   * @return the maturity
   */
  public Expiry getMaturity() {
    return _maturity;
  }

  /**
   * @return the coupon
   */
  public double getCoupon() {
    return _coupon;
  }

  /**
   * @return the frequency
   */
  public BondFrequency getFrequency() {
    return _frequency;
  }

  /**
   * @return the country
   */
  public String getCountry() {
    return _country;
  }

  /**
   * @return the creditRating
   */
  public String getCreditRating() {
    return _creditRating;
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the issuer
   */
  public String getIssuer() {
    return _issuer;
  }

  /**
   * @return the dayCountConvention
   */
  public DayCount getDayCountConvention() {
    return _dayCountConvention;
  }

  /**
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }
  
}