/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db.bond;

import java.util.Date;

import com.opengamma.financial.security.db.BusinessDayConventionBean;
import com.opengamma.financial.security.db.CurrencyBean;
import com.opengamma.financial.security.db.DayCountBean;
import com.opengamma.financial.security.db.FrequencyBean;
import com.opengamma.financial.security.db.SecurityBean;

/**
 * 
 * @author Andrew
 */
public class BondSecurityBean extends SecurityBean {
  
  private BondType _bondType;
  private String _issuerName;
  private IssuerTypeBean _issuerType;
  private String _issuerDomicile;
  private MarketBean _market;
  private CurrencyBean _currency;
  private YieldConventionBean _yieldConvention;
  private GuaranteeTypeBean _guaranteeType;
  private Date _maturity;
  private CouponTypeBean _couponType;
  private double _couponRate;
  private FrequencyBean _couponFrequency;
  private DayCountBean _dayCountConvention;
  private BusinessDayConventionBean _businessDayConvention;
  private Date _announcementDate;
  private Date _interestAccrualDate;
  private Date _settlementDate;
  private Date _firstCouponDate;
  private double _issuancePrice;
  private double _totalAmountIssued;
  private double _minimumAmount;
  private double _minimumIncrement;
  private double _parAmount;
  private double _redemptionValue;
  
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
   * @return the issuerName
   */
  public String getIssuerName() {
    return _issuerName;
  }
  /**
   * @param issuerName the issuerName to set
   */
  public void setIssuerName(String issuerName) {
    _issuerName = issuerName;
  }
  /**
   * @return the issuerType
   */
  public IssuerTypeBean getIssuerType() {
    return _issuerType;
  }
  /**
   * @param issuerType the issuerType to set
   */
  public void setIssuerType(IssuerTypeBean issuerType) {
    _issuerType = issuerType;
  }
  /**
   * @return the issuerDomicile
   */
  public String getIssuerDomicile() {
    return _issuerDomicile;
  }
  /**
   * @param issuerDomicile the issuerDomicile to set
   */
  public void setIssuerDomicile(String issuerDomicile) {
    _issuerDomicile = issuerDomicile;
  }
  /**
   * @return the market
   */
  public MarketBean getMarket() {
    return _market;
  }
  /**
   * @param market the market to set
   */
  public void setMarket(MarketBean market) {
    _market = market;
  }
  /**
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }
  /**
   * @param currency the currency to set
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }
  /**
   * @return the yieldConvention
   */
  public YieldConventionBean getYieldConvention() {
    return _yieldConvention;
  }
  /**
   * @param yieldConvention the yieldConvention to set
   */
  public void setYieldConvention(YieldConventionBean yieldConvention) {
    _yieldConvention = yieldConvention;
  }
  /**
   * @return the guaranteeType
   */
  public GuaranteeTypeBean getGuaranteeType() {
    return _guaranteeType;
  }
  /**
   * @param guaranteeType the guaranteeType to set
   */
  public void setGuaranteeType(GuaranteeTypeBean guaranteeType) {
    _guaranteeType = guaranteeType;
  }
  /**
   * @return the maturity
   */
  public Date getMaturity() {
    return _maturity;
  }
  /**
   * @param maturity the maturity to set
   */
  public void setMaturity(Date maturity) {
    _maturity = maturity;
  }
  /**
   * @return the couponType
   */
  public CouponTypeBean getCouponType() {
    return _couponType;
  }
  /**
   * @param couponType the couponType to set
   */
  public void setCouponType(CouponTypeBean couponType) {
    _couponType = couponType;
  }
  /**
   * @return the couponRate
   */
  public double getCouponRate() {
    return _couponRate;
  }
  /**
   * @param couponRate the couponRate to set
   */
  public void setCouponRate(double couponRate) {
    _couponRate = couponRate;
  }
  /**
   * @return the couponFrequency
   */
  public FrequencyBean getCouponFrequency() {
    return _couponFrequency;
  }
  /**
   * @param couponFrequency the couponFrequency to set
   */
  public void setCouponFrequency(FrequencyBean couponFrequency) {
    _couponFrequency = couponFrequency;
  }
  /**
   * @return the dayCountConvention
   */
  public DayCountBean getDayCountConvention() {
    return _dayCountConvention;
  }
  /**
   * @param dayCountConvention the dayCountConvention to set
   */
  public void setDayCountConvention(DayCountBean dayCountConvention) {
    _dayCountConvention = dayCountConvention;
  }
  /**
   * @return the businessDayConvention
   */
  public BusinessDayConventionBean getBusinessDayConvention() {
    return _businessDayConvention;
  }
  /**
   * @param businessDayConvention the businessDayConvention to set
   */
  public void setBusinessDayConvention(
      BusinessDayConventionBean businessDayConvention) {
    _businessDayConvention = businessDayConvention;
  }
  /**
   * @return the announcementDate
   */
  public Date getAnnouncementDate() {
    return _announcementDate;
  }
  /**
   * @param announcementDate the announcementDate to set
   */
  public void setAnnouncementDate(Date announcementDate) {
    _announcementDate = announcementDate;
  }
  /**
   * @return the interestAccrualDate
   */
  public Date getInterestAccrualDate() {
    return _interestAccrualDate;
  }
  /**
   * @param interestAccrualDate the interestAccrualDate to set
   */
  public void setInterestAccrualDate(Date interestAccrualDate) {
    _interestAccrualDate = interestAccrualDate;
  }
  /**
   * @return the settlementDate
   */
  public Date getSettlementDate() {
    return _settlementDate;
  }
  /**
   * @param settlementDate the settlementDate to set
   */
  public void setSettlementDate(Date settlementDate) {
    _settlementDate = settlementDate;
  }
  /**
   * @return the firstCouponDate
   */
  public Date getFirstCouponDate() {
    return _firstCouponDate;
  }
  /**
   * @param firstCouponDate the firstCouponDate to set
   */
  public void setFirstCouponDate(Date firstCouponDate) {
    _firstCouponDate = firstCouponDate;
  }
  /**
   * @return the issuancePrice
   */
  public double getIssuancePrice() {
    return _issuancePrice;
  }
  /**
   * @param issuancePrice the issuancePrice to set
   */
  public void setIssuancePrice(double issuancePrice) {
    _issuancePrice = issuancePrice;
  }
  /**
   * @return the totalAmountIssued
   */
  public double getTotalAmountIssued() {
    return _totalAmountIssued;
  }
  /**
   * @param totalAmountIssued the totalAmountIssued to set
   */
  public void setTotalAmountIssued(double totalAmountIssued) {
    _totalAmountIssued = totalAmountIssued;
  }
  /**
   * @return the minimumAmount
   */
  public double getMinimumAmount() {
    return _minimumAmount;
  }
  /**
   * @param minimumAmount the minimumAmount to set
   */
  public void setMinimumAmount(double minimumAmount) {
    _minimumAmount = minimumAmount;
  }
  /**
   * @return the minimumIncrement
   */
  public double getMinimumIncrement() {
    return _minimumIncrement;
  }
  /**
   * @param minimumIncrement the minimumIncrement to set
   */
  public void setMinimumIncrement(double minimumIncrement) {
    _minimumIncrement = minimumIncrement;
  }
  /**
   * @return the parAmount
   */
  public double getParAmount() {
    return _parAmount;
  }
  /**
   * @param parAmount the parAmount to set
   */
  public void setParAmount(double parAmount) {
    _parAmount = parAmount;
  }
  /**
   * @return the redemptionValue
   */
  public double getRedemptionValue() {
    return _redemptionValue;
  }
  /**
   * @param redemptionValue the redemptionValue to set
   */
  public void setRedemptionValue(double redemptionValue) {
    _redemptionValue = redemptionValue;
  }

}