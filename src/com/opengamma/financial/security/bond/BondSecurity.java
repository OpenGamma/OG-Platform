/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.util.time.Expiry;

/**
 * A {@code Security} used to model bonds.
 */
public abstract class BondSecurity extends FinancialSecurity {

  /**
   * The security type for bonds.
   */
  public static final String BOND_TYPE = "BOND";

  private final String _issuerName;
  private final String _issuerType;
  private final String _issuerDomicile;
  private final String _market;
  private final Currency _currency;
  private final YieldConvention _yieldConvention;
  private final String _guaranteeType;
  private final Expiry _maturity;
  private final String _couponType;
  private final double _couponRate;
  private final Frequency _couponFrequency;
  private final DayCount _dayCountConvention;
  private final BusinessDayConvention _businessDayConvention;
  private final LocalDate _announcementDate;
  private final LocalDate _interestAccrualDate;
  private final LocalDate _settlementDate;
  private final LocalDate _firstCouponDate;
  private final double _issuancePrice;
  private final double _totalAmountIssued;
  private final double _minimumAmount;
  private final double _minimumIncrement;
  private final double _parAmount;
  private final double _redemptionValue;

  /**
   * Creates a bond security.
   */
  // CSOFF: We need lots of parameters
  public BondSecurity(
      final String issuerName,
      final String issuerType,
      final String issuerDomicile,
      final String market,
      final Currency currency,
      final YieldConvention yieldConvention,
      final String guaranteeType,
      final Expiry maturity,
      final String couponType,
      final double couponRate,
      final Frequency couponFrequency,
      final DayCount dayCountConvention,
      final BusinessDayConvention businessDayConvention,
      final LocalDate announcementDate,
      final LocalDate interestAccrualDate,
      final LocalDate settlementDate,
      final LocalDate firstCouponDate,
      final double issuancePrice,
      final double totalAmountIssued,
      final double minimumAmount,
      final double minimumIncrement,
      final double parAmount,
      final double redemptionValue
    ) {
    // CSON: We need lots of parameters
    super(BOND_TYPE);
    _issuerName = issuerName;
    _issuerType = issuerType;
    _issuerDomicile = issuerDomicile;
    _market = market;
    _currency = currency;
    _yieldConvention = yieldConvention;
    _guaranteeType = guaranteeType;
    _maturity = maturity;
    _couponType = couponType;
    _couponRate = couponRate;
    _couponFrequency = couponFrequency;
    _dayCountConvention = dayCountConvention;
    _businessDayConvention = businessDayConvention;
    _announcementDate = announcementDate;
    _interestAccrualDate = interestAccrualDate;
    _settlementDate = settlementDate;
    _firstCouponDate = firstCouponDate;
    _issuancePrice = issuancePrice;
    _totalAmountIssued = totalAmountIssued;
    _minimumAmount = minimumAmount;
    _minimumIncrement = minimumIncrement;
    _parAmount = parAmount;
    _redemptionValue = redemptionValue;
  }

  //-------------------------------------------------------------------------
  /**
   * @return the issuerName
   */
  public String getIssuerName() {
    return _issuerName;
  }

  /**
   * @return the issuerType
   */
  public String getIssuerType() {
    return _issuerType;
  }

  /**
   * @return the issuerDomicile
   */
  public String getIssuerDomicile() {
    return _issuerDomicile;
  }

  /**
   * @return the market
   */
  public String getMarket() {
    return _market;
  }

  /**
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @return the yieldConvention
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * @return the guaranteeType
   */
  public String getGuaranteeType() {
    return _guaranteeType;
  }

  /**
   * @return the maturity
   */
  public Expiry getMaturity() {
    return _maturity;
  }

  /**
   * @return the couponType
   */
  public String getCouponType() {
    return _couponType;
  }

  /**
   * @return the couponRate
   */
  public double getCouponRate() {
    return _couponRate;
  }

  /**
   * @return the couponFrequency
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * @return the announcementDate
   */
  public LocalDate getAnnouncementDate() {
    return _announcementDate;
  }

  /**
   * @return the interestAccrualDate
   */
  public LocalDate getInterestAccrualDate() {
    return _interestAccrualDate;
  }

  /**
   * @return the settlementDate
   */
  public LocalDate getSettlementDate() {
    return _settlementDate;
  }

  /**
   * @return the firstCouponDate
   */
  public LocalDate getFirstCouponDate() {
    return _firstCouponDate;
  }

  /**
   * @return the issuancePrice
   */
  public double getIssuancePrice() {
    return _issuancePrice;
  }

  /**
   * @return the totalAmountIssued
   */
  public double getTotalAmountIssued() {
    return _totalAmountIssued;
  }

  /**
   * @return the minimumAmount
   */
  public double getMinimumAmount() {
    return _minimumAmount;
  }

  /**
   * @return the minimumIncrement
   */
  public double getMinimumIncrement() {
    return _minimumIncrement;
  }

  /**
   * @return the parAmount
   */
  public double getParAmount() {
    return _parAmount;
  }

  /**
   * @return the redemptionValue
   */
  public double getRedemptionValue() {
    return _redemptionValue;
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

  //-------------------------------------------------------------------------
  public abstract <T> T accept(BondSecurityVisitor<T> visitor);

  public final <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return accept((BondSecurityVisitor<T>) visitor);
  }

}
