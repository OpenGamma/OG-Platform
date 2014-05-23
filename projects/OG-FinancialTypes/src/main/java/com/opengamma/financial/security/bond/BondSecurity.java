/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * An abstract base class for bond securities.
 */
@BeanDefinition
@SecurityDescription(type = BondSecurity.SECURITY_TYPE, description = "Bond")
public abstract class BondSecurity extends FinancialSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security type for bonds.
   */
  public static final String SECURITY_TYPE = "BOND";

  /**
   * The issuer name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _issuerName;
  /**
   * The issuer type.
   */
  @PropertyDefinition(validate = "notNull")
  private String _issuerType;
  /**
   * The issuer domicile.
   */
  @PropertyDefinition(validate = "notNull")
  private String _issuerDomicile;
  /**
   * The market.
   */
  @PropertyDefinition(validate = "notNull")
  private String _market;
  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The yield convention.
   */
  @PropertyDefinition(validate = "notNull")
  private YieldConvention _yieldConvention;
  /**
   * The guarantee type.
   */
  @PropertyDefinition
  private String _guaranteeType;
  /**
   * The last trade date.
   */
  @PropertyDefinition(validate = "notNull")
  private Expiry _lastTradeDate;
  /**
   * The coupon type.
   */
  @PropertyDefinition(validate = "notNull")
  private String _couponType;
  /**
   * The coupon rate.
   */
  @PropertyDefinition
  private double _couponRate;
  /**
   * The coupon frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _couponFrequency;
  /**
   * The day count convention.
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;
  /**
   * The business day convention.
   */
  @PropertyDefinition
  private BusinessDayConvention _businessDayConvention;
  /**
   * The announcement date.
   */
  @PropertyDefinition
  private ZonedDateTime _announcementDate;
  /**
   * The interest accrual date.
   */
  @PropertyDefinition
  private ZonedDateTime _interestAccrualDate;
  /**
   * The settlement date.
   */
  @PropertyDefinition
  private ZonedDateTime _settlementDate;
  /**
   * The first coupon date.
   */
  @PropertyDefinition
  private ZonedDateTime _firstCouponDate;
  /**
   * The issuance price.
   */
  @PropertyDefinition
  private Double _issuancePrice;
  /**
   * The total amount issued.
   */
  @PropertyDefinition
  private double _totalAmountIssued;
  /**
   * The minimum amount.
   */
  @PropertyDefinition
  private double _minimumAmount;
  /**
   * The minimum increment.
   */
  @PropertyDefinition
  private double _minimumIncrement;
  /**
   * The par amount.
   */
  @PropertyDefinition
  private double _parAmount;
  /**
   * The redemption value.
   */
  @PropertyDefinition
  private double _redemptionValue;

  /**
   * Creates an empty instance.
   * <p>
   * The security details should be set before use.
   */
  protected BondSecurity() {
    super(SECURITY_TYPE);
  }

  protected BondSecurity(String issuerName, String issuerType, String issuerDomicile, String market, Currency currency,
      YieldConvention yieldConvention, Expiry lastTradeDate, String couponType, double couponRate, Frequency couponFrequency,
      DayCount dayCountConvention, ZonedDateTime interestAccrualDate, ZonedDateTime settlementDate, ZonedDateTime firstCouponDate,
      Double issuancePrice, double totalAmountIssued, double minimumAmount, double minimumIncrement, double parAmount, double redemptionValue) {
    super(SECURITY_TYPE);
    setIssuerName(issuerName);
    setIssuerType(issuerType);
    setIssuerDomicile(issuerDomicile);
    setMarket(market);
    setCurrency(currency);
    setYieldConvention(yieldConvention);
    setLastTradeDate(lastTradeDate);
    setCouponType(couponType);
    setCouponRate(couponRate);
    setCouponFrequency(couponFrequency);
    setDayCount(dayCountConvention);
    setInterestAccrualDate(interestAccrualDate);
    setSettlementDate(settlementDate);
    setFirstCouponDate(firstCouponDate);
    setIssuancePrice(issuancePrice);
    setTotalAmountIssued(totalAmountIssued);
    setMinimumAmount(minimumAmount);
    setMinimumIncrement(minimumIncrement);
    setParAmount(parAmount);
    setRedemptionValue(redemptionValue);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BondSecurity}.
   * @return the meta-bean, not null
   */
  public static BondSecurity.Meta meta() {
    return BondSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BondSecurity.Meta.INSTANCE);
  }

  @Override
  public BondSecurity.Meta metaBean() {
    return BondSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuer name.
   * @return the value of the property, not null
   */
  public String getIssuerName() {
    return _issuerName;
  }

  /**
   * Sets the issuer name.
   * @param issuerName  the new value of the property, not null
   */
  public void setIssuerName(String issuerName) {
    JodaBeanUtils.notNull(issuerName, "issuerName");
    this._issuerName = issuerName;
  }

  /**
   * Gets the the {@code issuerName} property.
   * @return the property, not null
   */
  public final Property<String> issuerName() {
    return metaBean().issuerName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuer type.
   * @return the value of the property, not null
   */
  public String getIssuerType() {
    return _issuerType;
  }

  /**
   * Sets the issuer type.
   * @param issuerType  the new value of the property, not null
   */
  public void setIssuerType(String issuerType) {
    JodaBeanUtils.notNull(issuerType, "issuerType");
    this._issuerType = issuerType;
  }

  /**
   * Gets the the {@code issuerType} property.
   * @return the property, not null
   */
  public final Property<String> issuerType() {
    return metaBean().issuerType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuer domicile.
   * @return the value of the property, not null
   */
  public String getIssuerDomicile() {
    return _issuerDomicile;
  }

  /**
   * Sets the issuer domicile.
   * @param issuerDomicile  the new value of the property, not null
   */
  public void setIssuerDomicile(String issuerDomicile) {
    JodaBeanUtils.notNull(issuerDomicile, "issuerDomicile");
    this._issuerDomicile = issuerDomicile;
  }

  /**
   * Gets the the {@code issuerDomicile} property.
   * @return the property, not null
   */
  public final Property<String> issuerDomicile() {
    return metaBean().issuerDomicile().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market.
   * @return the value of the property, not null
   */
  public String getMarket() {
    return _market;
  }

  /**
   * Sets the market.
   * @param market  the new value of the property, not null
   */
  public void setMarket(String market) {
    JodaBeanUtils.notNull(market, "market");
    this._market = market;
  }

  /**
   * Gets the the {@code market} property.
   * @return the property, not null
   */
  public final Property<String> market() {
    return metaBean().market().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yield convention.
   * @return the value of the property, not null
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Sets the yield convention.
   * @param yieldConvention  the new value of the property, not null
   */
  public void setYieldConvention(YieldConvention yieldConvention) {
    JodaBeanUtils.notNull(yieldConvention, "yieldConvention");
    this._yieldConvention = yieldConvention;
  }

  /**
   * Gets the the {@code yieldConvention} property.
   * @return the property, not null
   */
  public final Property<YieldConvention> yieldConvention() {
    return metaBean().yieldConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the guarantee type.
   * @return the value of the property
   */
  public String getGuaranteeType() {
    return _guaranteeType;
  }

  /**
   * Sets the guarantee type.
   * @param guaranteeType  the new value of the property
   */
  public void setGuaranteeType(String guaranteeType) {
    this._guaranteeType = guaranteeType;
  }

  /**
   * Gets the the {@code guaranteeType} property.
   * @return the property, not null
   */
  public final Property<String> guaranteeType() {
    return metaBean().guaranteeType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last trade date.
   * @return the value of the property, not null
   */
  public Expiry getLastTradeDate() {
    return _lastTradeDate;
  }

  /**
   * Sets the last trade date.
   * @param lastTradeDate  the new value of the property, not null
   */
  public void setLastTradeDate(Expiry lastTradeDate) {
    JodaBeanUtils.notNull(lastTradeDate, "lastTradeDate");
    this._lastTradeDate = lastTradeDate;
  }

  /**
   * Gets the the {@code lastTradeDate} property.
   * @return the property, not null
   */
  public final Property<Expiry> lastTradeDate() {
    return metaBean().lastTradeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon type.
   * @return the value of the property, not null
   */
  public String getCouponType() {
    return _couponType;
  }

  /**
   * Sets the coupon type.
   * @param couponType  the new value of the property, not null
   */
  public void setCouponType(String couponType) {
    JodaBeanUtils.notNull(couponType, "couponType");
    this._couponType = couponType;
  }

  /**
   * Gets the the {@code couponType} property.
   * @return the property, not null
   */
  public final Property<String> couponType() {
    return metaBean().couponType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon rate.
   * @return the value of the property
   */
  public double getCouponRate() {
    return _couponRate;
  }

  /**
   * Sets the coupon rate.
   * @param couponRate  the new value of the property
   */
  public void setCouponRate(double couponRate) {
    this._couponRate = couponRate;
  }

  /**
   * Gets the the {@code couponRate} property.
   * @return the property, not null
   */
  public final Property<Double> couponRate() {
    return metaBean().couponRate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the coupon frequency.
   * @return the value of the property, not null
   */
  public Frequency getCouponFrequency() {
    return _couponFrequency;
  }

  /**
   * Sets the coupon frequency.
   * @param couponFrequency  the new value of the property, not null
   */
  public void setCouponFrequency(Frequency couponFrequency) {
    JodaBeanUtils.notNull(couponFrequency, "couponFrequency");
    this._couponFrequency = couponFrequency;
  }

  /**
   * Gets the the {@code couponFrequency} property.
   * @return the property, not null
   */
  public final Property<Frequency> couponFrequency() {
    return metaBean().couponFrequency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count convention.
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count convention.
   * @param dayCount  the new value of the property, not null
   */
  public void setDayCount(DayCount dayCount) {
    JodaBeanUtils.notNull(dayCount, "dayCount");
    this._dayCount = dayCount;
  }

  /**
   * Gets the the {@code dayCount} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCount() {
    return metaBean().dayCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the business day convention.
   * @return the value of the property
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Sets the business day convention.
   * @param businessDayConvention  the new value of the property
   */
  public void setBusinessDayConvention(BusinessDayConvention businessDayConvention) {
    this._businessDayConvention = businessDayConvention;
  }

  /**
   * Gets the the {@code businessDayConvention} property.
   * @return the property, not null
   */
  public final Property<BusinessDayConvention> businessDayConvention() {
    return metaBean().businessDayConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the announcement date.
   * @return the value of the property
   */
  public ZonedDateTime getAnnouncementDate() {
    return _announcementDate;
  }

  /**
   * Sets the announcement date.
   * @param announcementDate  the new value of the property
   */
  public void setAnnouncementDate(ZonedDateTime announcementDate) {
    this._announcementDate = announcementDate;
  }

  /**
   * Gets the the {@code announcementDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> announcementDate() {
    return metaBean().announcementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interest accrual date.
   * @return the value of the property
   */
  public ZonedDateTime getInterestAccrualDate() {
    return _interestAccrualDate;
  }

  /**
   * Sets the interest accrual date.
   * @param interestAccrualDate  the new value of the property
   */
  public void setInterestAccrualDate(ZonedDateTime interestAccrualDate) {
    this._interestAccrualDate = interestAccrualDate;
  }

  /**
   * Gets the the {@code interestAccrualDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> interestAccrualDate() {
    return metaBean().interestAccrualDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the settlement date.
   * @return the value of the property
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the settlement date.
   * @param settlementDate  the new value of the property
   */
  public void setSettlementDate(ZonedDateTime settlementDate) {
    this._settlementDate = settlementDate;
  }

  /**
   * Gets the the {@code settlementDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> settlementDate() {
    return metaBean().settlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first coupon date.
   * @return the value of the property
   */
  public ZonedDateTime getFirstCouponDate() {
    return _firstCouponDate;
  }

  /**
   * Sets the first coupon date.
   * @param firstCouponDate  the new value of the property
   */
  public void setFirstCouponDate(ZonedDateTime firstCouponDate) {
    this._firstCouponDate = firstCouponDate;
  }

  /**
   * Gets the the {@code firstCouponDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> firstCouponDate() {
    return metaBean().firstCouponDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the issuance price.
   * @return the value of the property
   */
  public Double getIssuancePrice() {
    return _issuancePrice;
  }

  /**
   * Sets the issuance price.
   * @param issuancePrice  the new value of the property
   */
  public void setIssuancePrice(Double issuancePrice) {
    this._issuancePrice = issuancePrice;
  }

  /**
   * Gets the the {@code issuancePrice} property.
   * @return the property, not null
   */
  public final Property<Double> issuancePrice() {
    return metaBean().issuancePrice().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the total amount issued.
   * @return the value of the property
   */
  public double getTotalAmountIssued() {
    return _totalAmountIssued;
  }

  /**
   * Sets the total amount issued.
   * @param totalAmountIssued  the new value of the property
   */
  public void setTotalAmountIssued(double totalAmountIssued) {
    this._totalAmountIssued = totalAmountIssued;
  }

  /**
   * Gets the the {@code totalAmountIssued} property.
   * @return the property, not null
   */
  public final Property<Double> totalAmountIssued() {
    return metaBean().totalAmountIssued().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum amount.
   * @return the value of the property
   */
  public double getMinimumAmount() {
    return _minimumAmount;
  }

  /**
   * Sets the minimum amount.
   * @param minimumAmount  the new value of the property
   */
  public void setMinimumAmount(double minimumAmount) {
    this._minimumAmount = minimumAmount;
  }

  /**
   * Gets the the {@code minimumAmount} property.
   * @return the property, not null
   */
  public final Property<Double> minimumAmount() {
    return metaBean().minimumAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum increment.
   * @return the value of the property
   */
  public double getMinimumIncrement() {
    return _minimumIncrement;
  }

  /**
   * Sets the minimum increment.
   * @param minimumIncrement  the new value of the property
   */
  public void setMinimumIncrement(double minimumIncrement) {
    this._minimumIncrement = minimumIncrement;
  }

  /**
   * Gets the the {@code minimumIncrement} property.
   * @return the property, not null
   */
  public final Property<Double> minimumIncrement() {
    return metaBean().minimumIncrement().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the par amount.
   * @return the value of the property
   */
  public double getParAmount() {
    return _parAmount;
  }

  /**
   * Sets the par amount.
   * @param parAmount  the new value of the property
   */
  public void setParAmount(double parAmount) {
    this._parAmount = parAmount;
  }

  /**
   * Gets the the {@code parAmount} property.
   * @return the property, not null
   */
  public final Property<Double> parAmount() {
    return metaBean().parAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the redemption value.
   * @return the value of the property
   */
  public double getRedemptionValue() {
    return _redemptionValue;
  }

  /**
   * Sets the redemption value.
   * @param redemptionValue  the new value of the property
   */
  public void setRedemptionValue(double redemptionValue) {
    this._redemptionValue = redemptionValue;
  }

  /**
   * Gets the the {@code redemptionValue} property.
   * @return the property, not null
   */
  public final Property<Double> redemptionValue() {
    return metaBean().redemptionValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BondSecurity other = (BondSecurity) obj;
      return JodaBeanUtils.equal(getIssuerName(), other.getIssuerName()) &&
          JodaBeanUtils.equal(getIssuerType(), other.getIssuerType()) &&
          JodaBeanUtils.equal(getIssuerDomicile(), other.getIssuerDomicile()) &&
          JodaBeanUtils.equal(getMarket(), other.getMarket()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getYieldConvention(), other.getYieldConvention()) &&
          JodaBeanUtils.equal(getGuaranteeType(), other.getGuaranteeType()) &&
          JodaBeanUtils.equal(getLastTradeDate(), other.getLastTradeDate()) &&
          JodaBeanUtils.equal(getCouponType(), other.getCouponType()) &&
          JodaBeanUtils.equal(getCouponRate(), other.getCouponRate()) &&
          JodaBeanUtils.equal(getCouponFrequency(), other.getCouponFrequency()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getBusinessDayConvention(), other.getBusinessDayConvention()) &&
          JodaBeanUtils.equal(getAnnouncementDate(), other.getAnnouncementDate()) &&
          JodaBeanUtils.equal(getInterestAccrualDate(), other.getInterestAccrualDate()) &&
          JodaBeanUtils.equal(getSettlementDate(), other.getSettlementDate()) &&
          JodaBeanUtils.equal(getFirstCouponDate(), other.getFirstCouponDate()) &&
          JodaBeanUtils.equal(getIssuancePrice(), other.getIssuancePrice()) &&
          JodaBeanUtils.equal(getTotalAmountIssued(), other.getTotalAmountIssued()) &&
          JodaBeanUtils.equal(getMinimumAmount(), other.getMinimumAmount()) &&
          JodaBeanUtils.equal(getMinimumIncrement(), other.getMinimumIncrement()) &&
          JodaBeanUtils.equal(getParAmount(), other.getParAmount()) &&
          JodaBeanUtils.equal(getRedemptionValue(), other.getRedemptionValue()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getIssuerName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIssuerType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIssuerDomicile());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarket());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getYieldConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGuaranteeType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLastTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCouponType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCouponRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCouponFrequency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBusinessDayConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAnnouncementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterestAccrualDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSettlementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFirstCouponDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIssuancePrice());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTotalAmountIssued());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMinimumAmount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMinimumIncrement());
    hash += hash * 31 + JodaBeanUtils.hashCode(getParAmount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRedemptionValue());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(768);
    buf.append("BondSecurity{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("issuerName").append('=').append(JodaBeanUtils.toString(getIssuerName())).append(',').append(' ');
    buf.append("issuerType").append('=').append(JodaBeanUtils.toString(getIssuerType())).append(',').append(' ');
    buf.append("issuerDomicile").append('=').append(JodaBeanUtils.toString(getIssuerDomicile())).append(',').append(' ');
    buf.append("market").append('=').append(JodaBeanUtils.toString(getMarket())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("yieldConvention").append('=').append(JodaBeanUtils.toString(getYieldConvention())).append(',').append(' ');
    buf.append("guaranteeType").append('=').append(JodaBeanUtils.toString(getGuaranteeType())).append(',').append(' ');
    buf.append("lastTradeDate").append('=').append(JodaBeanUtils.toString(getLastTradeDate())).append(',').append(' ');
    buf.append("couponType").append('=').append(JodaBeanUtils.toString(getCouponType())).append(',').append(' ');
    buf.append("couponRate").append('=').append(JodaBeanUtils.toString(getCouponRate())).append(',').append(' ');
    buf.append("couponFrequency").append('=').append(JodaBeanUtils.toString(getCouponFrequency())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("businessDayConvention").append('=').append(JodaBeanUtils.toString(getBusinessDayConvention())).append(',').append(' ');
    buf.append("announcementDate").append('=').append(JodaBeanUtils.toString(getAnnouncementDate())).append(',').append(' ');
    buf.append("interestAccrualDate").append('=').append(JodaBeanUtils.toString(getInterestAccrualDate())).append(',').append(' ');
    buf.append("settlementDate").append('=').append(JodaBeanUtils.toString(getSettlementDate())).append(',').append(' ');
    buf.append("firstCouponDate").append('=').append(JodaBeanUtils.toString(getFirstCouponDate())).append(',').append(' ');
    buf.append("issuancePrice").append('=').append(JodaBeanUtils.toString(getIssuancePrice())).append(',').append(' ');
    buf.append("totalAmountIssued").append('=').append(JodaBeanUtils.toString(getTotalAmountIssued())).append(',').append(' ');
    buf.append("minimumAmount").append('=').append(JodaBeanUtils.toString(getMinimumAmount())).append(',').append(' ');
    buf.append("minimumIncrement").append('=').append(JodaBeanUtils.toString(getMinimumIncrement())).append(',').append(' ');
    buf.append("parAmount").append('=').append(JodaBeanUtils.toString(getParAmount())).append(',').append(' ');
    buf.append("redemptionValue").append('=').append(JodaBeanUtils.toString(getRedemptionValue())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BondSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code issuerName} property.
     */
    private final MetaProperty<String> _issuerName = DirectMetaProperty.ofReadWrite(
        this, "issuerName", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code issuerType} property.
     */
    private final MetaProperty<String> _issuerType = DirectMetaProperty.ofReadWrite(
        this, "issuerType", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code issuerDomicile} property.
     */
    private final MetaProperty<String> _issuerDomicile = DirectMetaProperty.ofReadWrite(
        this, "issuerDomicile", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code market} property.
     */
    private final MetaProperty<String> _market = DirectMetaProperty.ofReadWrite(
        this, "market", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", BondSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code yieldConvention} property.
     */
    private final MetaProperty<YieldConvention> _yieldConvention = DirectMetaProperty.ofReadWrite(
        this, "yieldConvention", BondSecurity.class, YieldConvention.class);
    /**
     * The meta-property for the {@code guaranteeType} property.
     */
    private final MetaProperty<String> _guaranteeType = DirectMetaProperty.ofReadWrite(
        this, "guaranteeType", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code lastTradeDate} property.
     */
    private final MetaProperty<Expiry> _lastTradeDate = DirectMetaProperty.ofReadWrite(
        this, "lastTradeDate", BondSecurity.class, Expiry.class);
    /**
     * The meta-property for the {@code couponType} property.
     */
    private final MetaProperty<String> _couponType = DirectMetaProperty.ofReadWrite(
        this, "couponType", BondSecurity.class, String.class);
    /**
     * The meta-property for the {@code couponRate} property.
     */
    private final MetaProperty<Double> _couponRate = DirectMetaProperty.ofReadWrite(
        this, "couponRate", BondSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code couponFrequency} property.
     */
    private final MetaProperty<Frequency> _couponFrequency = DirectMetaProperty.ofReadWrite(
        this, "couponFrequency", BondSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", BondSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code businessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _businessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "businessDayConvention", BondSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code announcementDate} property.
     */
    private final MetaProperty<ZonedDateTime> _announcementDate = DirectMetaProperty.ofReadWrite(
        this, "announcementDate", BondSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code interestAccrualDate} property.
     */
    private final MetaProperty<ZonedDateTime> _interestAccrualDate = DirectMetaProperty.ofReadWrite(
        this, "interestAccrualDate", BondSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<ZonedDateTime> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", BondSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code firstCouponDate} property.
     */
    private final MetaProperty<ZonedDateTime> _firstCouponDate = DirectMetaProperty.ofReadWrite(
        this, "firstCouponDate", BondSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code issuancePrice} property.
     */
    private final MetaProperty<Double> _issuancePrice = DirectMetaProperty.ofReadWrite(
        this, "issuancePrice", BondSecurity.class, Double.class);
    /**
     * The meta-property for the {@code totalAmountIssued} property.
     */
    private final MetaProperty<Double> _totalAmountIssued = DirectMetaProperty.ofReadWrite(
        this, "totalAmountIssued", BondSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code minimumAmount} property.
     */
    private final MetaProperty<Double> _minimumAmount = DirectMetaProperty.ofReadWrite(
        this, "minimumAmount", BondSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code minimumIncrement} property.
     */
    private final MetaProperty<Double> _minimumIncrement = DirectMetaProperty.ofReadWrite(
        this, "minimumIncrement", BondSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code parAmount} property.
     */
    private final MetaProperty<Double> _parAmount = DirectMetaProperty.ofReadWrite(
        this, "parAmount", BondSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code redemptionValue} property.
     */
    private final MetaProperty<Double> _redemptionValue = DirectMetaProperty.ofReadWrite(
        this, "redemptionValue", BondSecurity.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "issuerName",
        "issuerType",
        "issuerDomicile",
        "market",
        "currency",
        "yieldConvention",
        "guaranteeType",
        "lastTradeDate",
        "couponType",
        "couponRate",
        "couponFrequency",
        "dayCount",
        "businessDayConvention",
        "announcementDate",
        "interestAccrualDate",
        "settlementDate",
        "firstCouponDate",
        "issuancePrice",
        "totalAmountIssued",
        "minimumAmount",
        "minimumIncrement",
        "parAmount",
        "redemptionValue");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          return _issuerName;
        case 1459974547:  // issuerType
          return _issuerType;
        case -114049505:  // issuerDomicile
          return _issuerDomicile;
        case -1081306052:  // market
          return _market;
        case 575402001:  // currency
          return _currency;
        case -1895216418:  // yieldConvention
          return _yieldConvention;
        case 693583330:  // guaranteeType
          return _guaranteeType;
        case -1041950404:  // lastTradeDate
          return _lastTradeDate;
        case 609638528:  // couponType
          return _couponType;
        case 609556006:  // couponRate
          return _couponRate;
        case 144480214:  // couponFrequency
          return _couponFrequency;
        case 1905311443:  // dayCount
          return _dayCount;
        case -1002835891:  // businessDayConvention
          return _businessDayConvention;
        case -562907755:  // announcementDate
          return _announcementDate;
        case -693265293:  // interestAccrualDate
          return _interestAccrualDate;
        case -295948169:  // settlementDate
          return _settlementDate;
        case 793496516:  // firstCouponDate
          return _firstCouponDate;
        case -947491410:  // issuancePrice
          return _issuancePrice;
        case 1841198727:  // totalAmountIssued
          return _totalAmountIssued;
        case 2017331718:  // minimumAmount
          return _minimumAmount;
        case 1160465153:  // minimumIncrement
          return _minimumIncrement;
        case 1038626905:  // parAmount
          return _parAmount;
        case 348936710:  // redemptionValue
          return _redemptionValue;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BondSecurity> builder() {
      throw new UnsupportedOperationException("BondSecurity is an abstract class");
    }

    @Override
    public Class<? extends BondSecurity> beanType() {
      return BondSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code issuerName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> issuerName() {
      return _issuerName;
    }

    /**
     * The meta-property for the {@code issuerType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> issuerType() {
      return _issuerType;
    }

    /**
     * The meta-property for the {@code issuerDomicile} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> issuerDomicile() {
      return _issuerDomicile;
    }

    /**
     * The meta-property for the {@code market} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> market() {
      return _market;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code yieldConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<YieldConvention> yieldConvention() {
      return _yieldConvention;
    }

    /**
     * The meta-property for the {@code guaranteeType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> guaranteeType() {
      return _guaranteeType;
    }

    /**
     * The meta-property for the {@code lastTradeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Expiry> lastTradeDate() {
      return _lastTradeDate;
    }

    /**
     * The meta-property for the {@code couponType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> couponType() {
      return _couponType;
    }

    /**
     * The meta-property for the {@code couponRate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> couponRate() {
      return _couponRate;
    }

    /**
     * The meta-property for the {@code couponFrequency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Frequency> couponFrequency() {
      return _couponFrequency;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    /**
     * The meta-property for the {@code businessDayConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BusinessDayConvention> businessDayConvention() {
      return _businessDayConvention;
    }

    /**
     * The meta-property for the {@code announcementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> announcementDate() {
      return _announcementDate;
    }

    /**
     * The meta-property for the {@code interestAccrualDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> interestAccrualDate() {
      return _interestAccrualDate;
    }

    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> settlementDate() {
      return _settlementDate;
    }

    /**
     * The meta-property for the {@code firstCouponDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> firstCouponDate() {
      return _firstCouponDate;
    }

    /**
     * The meta-property for the {@code issuancePrice} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> issuancePrice() {
      return _issuancePrice;
    }

    /**
     * The meta-property for the {@code totalAmountIssued} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> totalAmountIssued() {
      return _totalAmountIssued;
    }

    /**
     * The meta-property for the {@code minimumAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> minimumAmount() {
      return _minimumAmount;
    }

    /**
     * The meta-property for the {@code minimumIncrement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> minimumIncrement() {
      return _minimumIncrement;
    }

    /**
     * The meta-property for the {@code parAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> parAmount() {
      return _parAmount;
    }

    /**
     * The meta-property for the {@code redemptionValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> redemptionValue() {
      return _redemptionValue;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          return ((BondSecurity) bean).getIssuerName();
        case 1459974547:  // issuerType
          return ((BondSecurity) bean).getIssuerType();
        case -114049505:  // issuerDomicile
          return ((BondSecurity) bean).getIssuerDomicile();
        case -1081306052:  // market
          return ((BondSecurity) bean).getMarket();
        case 575402001:  // currency
          return ((BondSecurity) bean).getCurrency();
        case -1895216418:  // yieldConvention
          return ((BondSecurity) bean).getYieldConvention();
        case 693583330:  // guaranteeType
          return ((BondSecurity) bean).getGuaranteeType();
        case -1041950404:  // lastTradeDate
          return ((BondSecurity) bean).getLastTradeDate();
        case 609638528:  // couponType
          return ((BondSecurity) bean).getCouponType();
        case 609556006:  // couponRate
          return ((BondSecurity) bean).getCouponRate();
        case 144480214:  // couponFrequency
          return ((BondSecurity) bean).getCouponFrequency();
        case 1905311443:  // dayCount
          return ((BondSecurity) bean).getDayCount();
        case -1002835891:  // businessDayConvention
          return ((BondSecurity) bean).getBusinessDayConvention();
        case -562907755:  // announcementDate
          return ((BondSecurity) bean).getAnnouncementDate();
        case -693265293:  // interestAccrualDate
          return ((BondSecurity) bean).getInterestAccrualDate();
        case -295948169:  // settlementDate
          return ((BondSecurity) bean).getSettlementDate();
        case 793496516:  // firstCouponDate
          return ((BondSecurity) bean).getFirstCouponDate();
        case -947491410:  // issuancePrice
          return ((BondSecurity) bean).getIssuancePrice();
        case 1841198727:  // totalAmountIssued
          return ((BondSecurity) bean).getTotalAmountIssued();
        case 2017331718:  // minimumAmount
          return ((BondSecurity) bean).getMinimumAmount();
        case 1160465153:  // minimumIncrement
          return ((BondSecurity) bean).getMinimumIncrement();
        case 1038626905:  // parAmount
          return ((BondSecurity) bean).getParAmount();
        case 348936710:  // redemptionValue
          return ((BondSecurity) bean).getRedemptionValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1459772644:  // issuerName
          ((BondSecurity) bean).setIssuerName((String) newValue);
          return;
        case 1459974547:  // issuerType
          ((BondSecurity) bean).setIssuerType((String) newValue);
          return;
        case -114049505:  // issuerDomicile
          ((BondSecurity) bean).setIssuerDomicile((String) newValue);
          return;
        case -1081306052:  // market
          ((BondSecurity) bean).setMarket((String) newValue);
          return;
        case 575402001:  // currency
          ((BondSecurity) bean).setCurrency((Currency) newValue);
          return;
        case -1895216418:  // yieldConvention
          ((BondSecurity) bean).setYieldConvention((YieldConvention) newValue);
          return;
        case 693583330:  // guaranteeType
          ((BondSecurity) bean).setGuaranteeType((String) newValue);
          return;
        case -1041950404:  // lastTradeDate
          ((BondSecurity) bean).setLastTradeDate((Expiry) newValue);
          return;
        case 609638528:  // couponType
          ((BondSecurity) bean).setCouponType((String) newValue);
          return;
        case 609556006:  // couponRate
          ((BondSecurity) bean).setCouponRate((Double) newValue);
          return;
        case 144480214:  // couponFrequency
          ((BondSecurity) bean).setCouponFrequency((Frequency) newValue);
          return;
        case 1905311443:  // dayCount
          ((BondSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case -1002835891:  // businessDayConvention
          ((BondSecurity) bean).setBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case -562907755:  // announcementDate
          ((BondSecurity) bean).setAnnouncementDate((ZonedDateTime) newValue);
          return;
        case -693265293:  // interestAccrualDate
          ((BondSecurity) bean).setInterestAccrualDate((ZonedDateTime) newValue);
          return;
        case -295948169:  // settlementDate
          ((BondSecurity) bean).setSettlementDate((ZonedDateTime) newValue);
          return;
        case 793496516:  // firstCouponDate
          ((BondSecurity) bean).setFirstCouponDate((ZonedDateTime) newValue);
          return;
        case -947491410:  // issuancePrice
          ((BondSecurity) bean).setIssuancePrice((Double) newValue);
          return;
        case 1841198727:  // totalAmountIssued
          ((BondSecurity) bean).setTotalAmountIssued((Double) newValue);
          return;
        case 2017331718:  // minimumAmount
          ((BondSecurity) bean).setMinimumAmount((Double) newValue);
          return;
        case 1160465153:  // minimumIncrement
          ((BondSecurity) bean).setMinimumIncrement((Double) newValue);
          return;
        case 1038626905:  // parAmount
          ((BondSecurity) bean).setParAmount((Double) newValue);
          return;
        case 348936710:  // redemptionValue
          ((BondSecurity) bean).setRedemptionValue((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((BondSecurity) bean)._issuerName, "issuerName");
      JodaBeanUtils.notNull(((BondSecurity) bean)._issuerType, "issuerType");
      JodaBeanUtils.notNull(((BondSecurity) bean)._issuerDomicile, "issuerDomicile");
      JodaBeanUtils.notNull(((BondSecurity) bean)._market, "market");
      JodaBeanUtils.notNull(((BondSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((BondSecurity) bean)._yieldConvention, "yieldConvention");
      JodaBeanUtils.notNull(((BondSecurity) bean)._lastTradeDate, "lastTradeDate");
      JodaBeanUtils.notNull(((BondSecurity) bean)._couponType, "couponType");
      JodaBeanUtils.notNull(((BondSecurity) bean)._couponFrequency, "couponFrequency");
      JodaBeanUtils.notNull(((BondSecurity) bean)._dayCount, "dayCount");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
