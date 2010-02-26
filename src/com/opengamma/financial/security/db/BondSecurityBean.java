/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

/**
 * 
 * @author Andrew
 */
public class BondSecurityBean extends SecurityBean {
  
  private BondType _bondType;
  private Date _maturity;
  private double _coupon;
  private FrequencyBean _frequency;
  private String _country;
  private String _creditRating;
  private CurrencyBean _currency;
  private String _issuer;
  private DayCountBean _dayCountConvention;
  private BusinessDayConventionBean _businessDayConvention;
  
  public BondSecurityBean () {
    super ();
  }
  
  public BondSecurityBean (final BondType bondType, final Date maturity, final double coupon, final FrequencyBean frequency, final String country, final String creditRating, final CurrencyBean currency, final String issuer, final DayCountBean dayCountConvention, final BusinessDayConventionBean businessDayConvention) {
    this ();
    _bondType = bondType;
    _maturity = maturity;
    _coupon = coupon;
    _frequency = frequency;
    _country = country;
    _creditRating = creditRating;
    _currency = currency;
    _issuer = issuer;
    _dayCountConvention = dayCountConvention;
    _businessDayConvention = businessDayConvention;
  }
  
  /**
   * @return the bondType
   */
  public BondType getBondType() {
    return _bondType;
  }

  /**
   * @param bondType the bondType to set
   */
  public void setBondType(BondType bondType) {
    _bondType = bondType;
  }

  /**
   * @return the maturity
   */
  public Date getMaturity() {
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
  public FrequencyBean getFrequency() {
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
  public CurrencyBean getCurrency() {
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
  public DayCountBean getDayCountConvention() {
    return _dayCountConvention;
  }

  /**
   * @return the businessDayConvention
   */
  public BusinessDayConventionBean getBusinessDayConvention() {
    return _businessDayConvention;
  }

  @Override
  public <T> T accept(SecurityBeanVisitor<T> visitor) {
    return visitor.visitBondSecurityBean (this);
  }

  /**
   * @param maturity the maturity to set
   */
  public void setMaturity(Date maturity) {
    _maturity = maturity;
  }

  /**
   * @param coupon the coupon to set
   */
  public void setCoupon(double coupon) {
    _coupon = coupon;
  }

  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(FrequencyBean frequency) {
    _frequency = frequency;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(String country) {
    _country = country;
  }

  /**
   * @param creditRating the creditRating to set
   */
  public void setCreditRating(String creditRating) {
    _creditRating = creditRating;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  /**
   * @param issuer the issuer to set
   */
  public void setIssuer(String issuer) {
    _issuer = issuer;
  }

  /**
   * @param dayCountConvention the dayCountConvention to set
   */
  public void setDayCountConvention(DayCountBean dayCountConvention) {
    _dayCountConvention = dayCountConvention;
  }

  /**
   * @param businessDayConvention the businessDayConvention to set
   */
  public void setBusinessDayConvention(
      BusinessDayConventionBean businessDayConvention) {
    _businessDayConvention = businessDayConvention;
  }
  
}