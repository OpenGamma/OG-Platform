/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

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

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;

/**
 * Class representing the common elements from both a CreditDefaultSwap and a
 * CdsIndex security. These two can be modelled very similarly with the
 * primary difference being the entity against which the trade is held:
 * the ReferenceEntity/Obligor for a CDS and an Index for the CDS Index. Note the
 * buy/sell conventions for CDS and CDSIndex are opposites.
 */
@BeanDefinition
public abstract class AbstractCreditDefaultSwapSecurity extends FinancialSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Has the protection been bought. Note that the market conventions for CDS and
   * CDS Index are the reverse of each other i.e. this flag needs to interpreted
   * in the context of the security type.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _buy;
  /**
   * The protection buyer.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _protectionBuyer;
  /**
   * The protection seller.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _protectionSeller;
  /**
   * The reference entity. Either an Obligor for a CDS, or an underlying
   * index for a CDS Index.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _referenceEntity;
  /**
   * The start date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _startDate;
  /**
   * The effective date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _effectiveDate;
  /**
   * The maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _maturityDate;
  /**
   * The stub type.
   */
  @PropertyDefinition(validate = "notNull")
  private StubType _stubType;
  /**
   * The coupon frequency.
   */
  @PropertyDefinition(validate = "notNull")
  private Frequency _couponFrequency;
  /**
   * The day-count convention.
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;
  /**
   * The business-day convention.
   */
  @PropertyDefinition(validate = "notNull")
  private BusinessDayConvention _businessDayConvention;
  /**
   * Adjust maturity to the next IMM date.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _immAdjustMaturityDate;
  /**
   * Adjust effective date.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _adjustEffectiveDate;
  /**
   * Adjust maturity date.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _adjustMaturityDate;
  /**
   * The notional.
   */
  @PropertyDefinition(validate = "notNull")
  private InterestRateNotional _notional;
  /**
   * Include accrued premium.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _includeAccruedPremium;
  /**
   * Protection start.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _protectionStart;

  public AbstractCreditDefaultSwapSecurity(String securityType) {
    super(securityType);
  }

  public AbstractCreditDefaultSwapSecurity(String securityType,
                                              boolean buy,
                                              ExternalId protectionBuyer,
                                              ExternalId protectionSeller,
                                              ExternalId referenceEntity,
                                              ZonedDateTime startDate,
                                              ZonedDateTime effectiveDate,
                                              ZonedDateTime maturityDate,
                                              StubType stubType,
                                              Frequency couponFrequency,
                                              DayCount dayCount,
                                              BusinessDayConvention businessDayConvention,
                                              boolean immAdjustMaturityDate,
                                              boolean adjustEffectiveDate,
                                              boolean adjustMaturityDate,
                                              InterestRateNotional notional,
                                              boolean includeAccruedPremium,
                                              boolean protectionStart) {
    super(securityType);
    setBuy(buy);
    setProtectionBuyer(protectionBuyer);
    setProtectionSeller(protectionSeller);
    setReferenceEntity(referenceEntity);
    setStartDate(startDate);
    setEffectiveDate(effectiveDate);
    setMaturityDate(maturityDate);
    setStubType(stubType);
    setCouponFrequency(couponFrequency);
    setDayCount(dayCount);
    setBusinessDayConvention(businessDayConvention);
    setImmAdjustMaturityDate(immAdjustMaturityDate);
    setAdjustEffectiveDate(adjustEffectiveDate);
    setAdjustMaturityDate(adjustMaturityDate);
    setNotional(notional);
    setIncludeAccruedPremium(includeAccruedPremium);
    setProtectionStart(protectionStart);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractCreditDefaultSwapSecurity}.
   * @return the meta-bean, not null
   */
  public static AbstractCreditDefaultSwapSecurity.Meta meta() {
    return AbstractCreditDefaultSwapSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractCreditDefaultSwapSecurity.Meta.INSTANCE);
  }

  @Override
  public AbstractCreditDefaultSwapSecurity.Meta metaBean() {
    return AbstractCreditDefaultSwapSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets has the protection been bought. Note that the market conventions for CDS and
   * CDS Index are the reverse of each other i.e. this flag needs to interpreted
   * in the context of the security type.
   * @return the value of the property, not null
   */
  public boolean isBuy() {
    return _buy;
  }

  /**
   * Sets has the protection been bought. Note that the market conventions for CDS and
   * CDS Index are the reverse of each other i.e. this flag needs to interpreted
   * in the context of the security type.
   * @param buy  the new value of the property, not null
   */
  public void setBuy(boolean buy) {
    JodaBeanUtils.notNull(buy, "buy");
    this._buy = buy;
  }

  /**
   * Gets the the {@code buy} property.
   * CDS Index are the reverse of each other i.e. this flag needs to interpreted
   * in the context of the security type.
   * @return the property, not null
   */
  public final Property<Boolean> buy() {
    return metaBean().buy().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the protection buyer.
   * @return the value of the property, not null
   */
  public ExternalId getProtectionBuyer() {
    return _protectionBuyer;
  }

  /**
   * Sets the protection buyer.
   * @param protectionBuyer  the new value of the property, not null
   */
  public void setProtectionBuyer(ExternalId protectionBuyer) {
    JodaBeanUtils.notNull(protectionBuyer, "protectionBuyer");
    this._protectionBuyer = protectionBuyer;
  }

  /**
   * Gets the the {@code protectionBuyer} property.
   * @return the property, not null
   */
  public final Property<ExternalId> protectionBuyer() {
    return metaBean().protectionBuyer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the protection seller.
   * @return the value of the property, not null
   */
  public ExternalId getProtectionSeller() {
    return _protectionSeller;
  }

  /**
   * Sets the protection seller.
   * @param protectionSeller  the new value of the property, not null
   */
  public void setProtectionSeller(ExternalId protectionSeller) {
    JodaBeanUtils.notNull(protectionSeller, "protectionSeller");
    this._protectionSeller = protectionSeller;
  }

  /**
   * Gets the the {@code protectionSeller} property.
   * @return the property, not null
   */
  public final Property<ExternalId> protectionSeller() {
    return metaBean().protectionSeller().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the reference entity. Either an Obligor for a CDS, or an underlying
   * index for a CDS Index.
   * @return the value of the property, not null
   */
  public ExternalId getReferenceEntity() {
    return _referenceEntity;
  }

  /**
   * Sets the reference entity. Either an Obligor for a CDS, or an underlying
   * index for a CDS Index.
   * @param referenceEntity  the new value of the property, not null
   */
  public void setReferenceEntity(ExternalId referenceEntity) {
    JodaBeanUtils.notNull(referenceEntity, "referenceEntity");
    this._referenceEntity = referenceEntity;
  }

  /**
   * Gets the the {@code referenceEntity} property.
   * index for a CDS Index.
   * @return the property, not null
   */
  public final Property<ExternalId> referenceEntity() {
    return metaBean().referenceEntity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  /**
   * Sets the start date.
   * @param startDate  the new value of the property, not null
   */
  public void setStartDate(ZonedDateTime startDate) {
    JodaBeanUtils.notNull(startDate, "startDate");
    this._startDate = startDate;
  }

  /**
   * Gets the the {@code startDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> startDate() {
    return metaBean().startDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the effective date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Sets the effective date.
   * @param effectiveDate  the new value of the property, not null
   */
  public void setEffectiveDate(ZonedDateTime effectiveDate) {
    JodaBeanUtils.notNull(effectiveDate, "effectiveDate");
    this._effectiveDate = effectiveDate;
  }

  /**
   * Gets the the {@code effectiveDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> effectiveDate() {
    return metaBean().effectiveDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturity date.
   * @param maturityDate  the new value of the property, not null
   */
  public void setMaturityDate(ZonedDateTime maturityDate) {
    JodaBeanUtils.notNull(maturityDate, "maturityDate");
    this._maturityDate = maturityDate;
  }

  /**
   * Gets the the {@code maturityDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> maturityDate() {
    return metaBean().maturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the stub type.
   * @return the value of the property, not null
   */
  public StubType getStubType() {
    return _stubType;
  }

  /**
   * Sets the stub type.
   * @param stubType  the new value of the property, not null
   */
  public void setStubType(StubType stubType) {
    JodaBeanUtils.notNull(stubType, "stubType");
    this._stubType = stubType;
  }

  /**
   * Gets the the {@code stubType} property.
   * @return the property, not null
   */
  public final Property<StubType> stubType() {
    return metaBean().stubType().createProperty(this);
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
   * Gets the day-count convention.
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day-count convention.
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
   * Gets the business-day convention.
   * @return the value of the property, not null
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Sets the business-day convention.
   * @param businessDayConvention  the new value of the property, not null
   */
  public void setBusinessDayConvention(BusinessDayConvention businessDayConvention) {
    JodaBeanUtils.notNull(businessDayConvention, "businessDayConvention");
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
   * Gets adjust maturity to the next IMM date.
   * @return the value of the property, not null
   */
  public boolean isImmAdjustMaturityDate() {
    return _immAdjustMaturityDate;
  }

  /**
   * Sets adjust maturity to the next IMM date.
   * @param immAdjustMaturityDate  the new value of the property, not null
   */
  public void setImmAdjustMaturityDate(boolean immAdjustMaturityDate) {
    JodaBeanUtils.notNull(immAdjustMaturityDate, "immAdjustMaturityDate");
    this._immAdjustMaturityDate = immAdjustMaturityDate;
  }

  /**
   * Gets the the {@code immAdjustMaturityDate} property.
   * @return the property, not null
   */
  public final Property<Boolean> immAdjustMaturityDate() {
    return metaBean().immAdjustMaturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets adjust effective date.
   * @return the value of the property, not null
   */
  public boolean isAdjustEffectiveDate() {
    return _adjustEffectiveDate;
  }

  /**
   * Sets adjust effective date.
   * @param adjustEffectiveDate  the new value of the property, not null
   */
  public void setAdjustEffectiveDate(boolean adjustEffectiveDate) {
    JodaBeanUtils.notNull(adjustEffectiveDate, "adjustEffectiveDate");
    this._adjustEffectiveDate = adjustEffectiveDate;
  }

  /**
   * Gets the the {@code adjustEffectiveDate} property.
   * @return the property, not null
   */
  public final Property<Boolean> adjustEffectiveDate() {
    return metaBean().adjustEffectiveDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets adjust maturity date.
   * @return the value of the property, not null
   */
  public boolean isAdjustMaturityDate() {
    return _adjustMaturityDate;
  }

  /**
   * Sets adjust maturity date.
   * @param adjustMaturityDate  the new value of the property, not null
   */
  public void setAdjustMaturityDate(boolean adjustMaturityDate) {
    JodaBeanUtils.notNull(adjustMaturityDate, "adjustMaturityDate");
    this._adjustMaturityDate = adjustMaturityDate;
  }

  /**
   * Gets the the {@code adjustMaturityDate} property.
   * @return the property, not null
   */
  public final Property<Boolean> adjustMaturityDate() {
    return metaBean().adjustMaturityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional.
   * @return the value of the property, not null
   */
  public InterestRateNotional getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the new value of the property, not null
   */
  public void setNotional(InterestRateNotional notional) {
    JodaBeanUtils.notNull(notional, "notional");
    this._notional = notional;
  }

  /**
   * Gets the the {@code notional} property.
   * @return the property, not null
   */
  public final Property<InterestRateNotional> notional() {
    return metaBean().notional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets include accrued premium.
   * @return the value of the property, not null
   */
  public boolean isIncludeAccruedPremium() {
    return _includeAccruedPremium;
  }

  /**
   * Sets include accrued premium.
   * @param includeAccruedPremium  the new value of the property, not null
   */
  public void setIncludeAccruedPremium(boolean includeAccruedPremium) {
    JodaBeanUtils.notNull(includeAccruedPremium, "includeAccruedPremium");
    this._includeAccruedPremium = includeAccruedPremium;
  }

  /**
   * Gets the the {@code includeAccruedPremium} property.
   * @return the property, not null
   */
  public final Property<Boolean> includeAccruedPremium() {
    return metaBean().includeAccruedPremium().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets protection start.
   * @return the value of the property, not null
   */
  public boolean isProtectionStart() {
    return _protectionStart;
  }

  /**
   * Sets protection start.
   * @param protectionStart  the new value of the property, not null
   */
  public void setProtectionStart(boolean protectionStart) {
    JodaBeanUtils.notNull(protectionStart, "protectionStart");
    this._protectionStart = protectionStart;
  }

  /**
   * Gets the the {@code protectionStart} property.
   * @return the property, not null
   */
  public final Property<Boolean> protectionStart() {
    return metaBean().protectionStart().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractCreditDefaultSwapSecurity other = (AbstractCreditDefaultSwapSecurity) obj;
      return (isBuy() == other.isBuy()) &&
          JodaBeanUtils.equal(getProtectionBuyer(), other.getProtectionBuyer()) &&
          JodaBeanUtils.equal(getProtectionSeller(), other.getProtectionSeller()) &&
          JodaBeanUtils.equal(getReferenceEntity(), other.getReferenceEntity()) &&
          JodaBeanUtils.equal(getStartDate(), other.getStartDate()) &&
          JodaBeanUtils.equal(getEffectiveDate(), other.getEffectiveDate()) &&
          JodaBeanUtils.equal(getMaturityDate(), other.getMaturityDate()) &&
          JodaBeanUtils.equal(getStubType(), other.getStubType()) &&
          JodaBeanUtils.equal(getCouponFrequency(), other.getCouponFrequency()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getBusinessDayConvention(), other.getBusinessDayConvention()) &&
          (isImmAdjustMaturityDate() == other.isImmAdjustMaturityDate()) &&
          (isAdjustEffectiveDate() == other.isAdjustEffectiveDate()) &&
          (isAdjustMaturityDate() == other.isAdjustMaturityDate()) &&
          JodaBeanUtils.equal(getNotional(), other.getNotional()) &&
          (isIncludeAccruedPremium() == other.isIncludeAccruedPremium()) &&
          (isProtectionStart() == other.isProtectionStart()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(isBuy());
    hash = hash * 31 + JodaBeanUtils.hashCode(getProtectionBuyer());
    hash = hash * 31 + JodaBeanUtils.hashCode(getProtectionSeller());
    hash = hash * 31 + JodaBeanUtils.hashCode(getReferenceEntity());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEffectiveDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaturityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStubType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCouponFrequency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getBusinessDayConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(isImmAdjustMaturityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(isAdjustEffectiveDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(isAdjustMaturityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNotional());
    hash = hash * 31 + JodaBeanUtils.hashCode(isIncludeAccruedPremium());
    hash = hash * 31 + JodaBeanUtils.hashCode(isProtectionStart());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(576);
    buf.append("AbstractCreditDefaultSwapSecurity{");
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
    buf.append("buy").append('=').append(JodaBeanUtils.toString(isBuy())).append(',').append(' ');
    buf.append("protectionBuyer").append('=').append(JodaBeanUtils.toString(getProtectionBuyer())).append(',').append(' ');
    buf.append("protectionSeller").append('=').append(JodaBeanUtils.toString(getProtectionSeller())).append(',').append(' ');
    buf.append("referenceEntity").append('=').append(JodaBeanUtils.toString(getReferenceEntity())).append(',').append(' ');
    buf.append("startDate").append('=').append(JodaBeanUtils.toString(getStartDate())).append(',').append(' ');
    buf.append("effectiveDate").append('=').append(JodaBeanUtils.toString(getEffectiveDate())).append(',').append(' ');
    buf.append("maturityDate").append('=').append(JodaBeanUtils.toString(getMaturityDate())).append(',').append(' ');
    buf.append("stubType").append('=').append(JodaBeanUtils.toString(getStubType())).append(',').append(' ');
    buf.append("couponFrequency").append('=').append(JodaBeanUtils.toString(getCouponFrequency())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("businessDayConvention").append('=').append(JodaBeanUtils.toString(getBusinessDayConvention())).append(',').append(' ');
    buf.append("immAdjustMaturityDate").append('=').append(JodaBeanUtils.toString(isImmAdjustMaturityDate())).append(',').append(' ');
    buf.append("adjustEffectiveDate").append('=').append(JodaBeanUtils.toString(isAdjustEffectiveDate())).append(',').append(' ');
    buf.append("adjustMaturityDate").append('=').append(JodaBeanUtils.toString(isAdjustMaturityDate())).append(',').append(' ');
    buf.append("notional").append('=').append(JodaBeanUtils.toString(getNotional())).append(',').append(' ');
    buf.append("includeAccruedPremium").append('=').append(JodaBeanUtils.toString(isIncludeAccruedPremium())).append(',').append(' ');
    buf.append("protectionStart").append('=').append(JodaBeanUtils.toString(isProtectionStart())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractCreditDefaultSwapSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code buy} property.
     */
    private final MetaProperty<Boolean> _buy = DirectMetaProperty.ofReadWrite(
        this, "buy", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code protectionBuyer} property.
     */
    private final MetaProperty<ExternalId> _protectionBuyer = DirectMetaProperty.ofReadWrite(
        this, "protectionBuyer", AbstractCreditDefaultSwapSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code protectionSeller} property.
     */
    private final MetaProperty<ExternalId> _protectionSeller = DirectMetaProperty.ofReadWrite(
        this, "protectionSeller", AbstractCreditDefaultSwapSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code referenceEntity} property.
     */
    private final MetaProperty<ExternalId> _referenceEntity = DirectMetaProperty.ofReadWrite(
        this, "referenceEntity", AbstractCreditDefaultSwapSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code startDate} property.
     */
    private final MetaProperty<ZonedDateTime> _startDate = DirectMetaProperty.ofReadWrite(
        this, "startDate", AbstractCreditDefaultSwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code effectiveDate} property.
     */
    private final MetaProperty<ZonedDateTime> _effectiveDate = DirectMetaProperty.ofReadWrite(
        this, "effectiveDate", AbstractCreditDefaultSwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code maturityDate} property.
     */
    private final MetaProperty<ZonedDateTime> _maturityDate = DirectMetaProperty.ofReadWrite(
        this, "maturityDate", AbstractCreditDefaultSwapSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code stubType} property.
     */
    private final MetaProperty<StubType> _stubType = DirectMetaProperty.ofReadWrite(
        this, "stubType", AbstractCreditDefaultSwapSecurity.class, StubType.class);
    /**
     * The meta-property for the {@code couponFrequency} property.
     */
    private final MetaProperty<Frequency> _couponFrequency = DirectMetaProperty.ofReadWrite(
        this, "couponFrequency", AbstractCreditDefaultSwapSecurity.class, Frequency.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", AbstractCreditDefaultSwapSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code businessDayConvention} property.
     */
    private final MetaProperty<BusinessDayConvention> _businessDayConvention = DirectMetaProperty.ofReadWrite(
        this, "businessDayConvention", AbstractCreditDefaultSwapSecurity.class, BusinessDayConvention.class);
    /**
     * The meta-property for the {@code immAdjustMaturityDate} property.
     */
    private final MetaProperty<Boolean> _immAdjustMaturityDate = DirectMetaProperty.ofReadWrite(
        this, "immAdjustMaturityDate", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code adjustEffectiveDate} property.
     */
    private final MetaProperty<Boolean> _adjustEffectiveDate = DirectMetaProperty.ofReadWrite(
        this, "adjustEffectiveDate", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code adjustMaturityDate} property.
     */
    private final MetaProperty<Boolean> _adjustMaturityDate = DirectMetaProperty.ofReadWrite(
        this, "adjustMaturityDate", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<InterestRateNotional> _notional = DirectMetaProperty.ofReadWrite(
        this, "notional", AbstractCreditDefaultSwapSecurity.class, InterestRateNotional.class);
    /**
     * The meta-property for the {@code includeAccruedPremium} property.
     */
    private final MetaProperty<Boolean> _includeAccruedPremium = DirectMetaProperty.ofReadWrite(
        this, "includeAccruedPremium", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code protectionStart} property.
     */
    private final MetaProperty<Boolean> _protectionStart = DirectMetaProperty.ofReadWrite(
        this, "protectionStart", AbstractCreditDefaultSwapSecurity.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "buy",
        "protectionBuyer",
        "protectionSeller",
        "referenceEntity",
        "startDate",
        "effectiveDate",
        "maturityDate",
        "stubType",
        "couponFrequency",
        "dayCount",
        "businessDayConvention",
        "immAdjustMaturityDate",
        "adjustEffectiveDate",
        "adjustMaturityDate",
        "notional",
        "includeAccruedPremium",
        "protectionStart");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 97926:  // buy
          return _buy;
        case 2087835226:  // protectionBuyer
          return _protectionBuyer;
        case 769920952:  // protectionSeller
          return _protectionSeller;
        case 480652046:  // referenceEntity
          return _referenceEntity;
        case -2129778896:  // startDate
          return _startDate;
        case -930389515:  // effectiveDate
          return _effectiveDate;
        case -414641441:  // maturityDate
          return _maturityDate;
        case 1873675528:  // stubType
          return _stubType;
        case 144480214:  // couponFrequency
          return _couponFrequency;
        case 1905311443:  // dayCount
          return _dayCount;
        case -1002835891:  // businessDayConvention
          return _businessDayConvention;
        case -1168632905:  // immAdjustMaturityDate
          return _immAdjustMaturityDate;
        case -490317146:  // adjustEffectiveDate
          return _adjustEffectiveDate;
        case -261898226:  // adjustMaturityDate
          return _adjustMaturityDate;
        case 1585636160:  // notional
          return _notional;
        case 2100149628:  // includeAccruedPremium
          return _includeAccruedPremium;
        case 2103482633:  // protectionStart
          return _protectionStart;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractCreditDefaultSwapSecurity> builder() {
      throw new UnsupportedOperationException("AbstractCreditDefaultSwapSecurity is an abstract class");
    }

    @Override
    public Class<? extends AbstractCreditDefaultSwapSecurity> beanType() {
      return AbstractCreditDefaultSwapSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code buy} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> buy() {
      return _buy;
    }

    /**
     * The meta-property for the {@code protectionBuyer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> protectionBuyer() {
      return _protectionBuyer;
    }

    /**
     * The meta-property for the {@code protectionSeller} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> protectionSeller() {
      return _protectionSeller;
    }

    /**
     * The meta-property for the {@code referenceEntity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> referenceEntity() {
      return _referenceEntity;
    }

    /**
     * The meta-property for the {@code startDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> startDate() {
      return _startDate;
    }

    /**
     * The meta-property for the {@code effectiveDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> effectiveDate() {
      return _effectiveDate;
    }

    /**
     * The meta-property for the {@code maturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> maturityDate() {
      return _maturityDate;
    }

    /**
     * The meta-property for the {@code stubType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<StubType> stubType() {
      return _stubType;
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
     * The meta-property for the {@code immAdjustMaturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> immAdjustMaturityDate() {
      return _immAdjustMaturityDate;
    }

    /**
     * The meta-property for the {@code adjustEffectiveDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> adjustEffectiveDate() {
      return _adjustEffectiveDate;
    }

    /**
     * The meta-property for the {@code adjustMaturityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> adjustMaturityDate() {
      return _adjustMaturityDate;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterestRateNotional> notional() {
      return _notional;
    }

    /**
     * The meta-property for the {@code includeAccruedPremium} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> includeAccruedPremium() {
      return _includeAccruedPremium;
    }

    /**
     * The meta-property for the {@code protectionStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> protectionStart() {
      return _protectionStart;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 97926:  // buy
          return ((AbstractCreditDefaultSwapSecurity) bean).isBuy();
        case 2087835226:  // protectionBuyer
          return ((AbstractCreditDefaultSwapSecurity) bean).getProtectionBuyer();
        case 769920952:  // protectionSeller
          return ((AbstractCreditDefaultSwapSecurity) bean).getProtectionSeller();
        case 480652046:  // referenceEntity
          return ((AbstractCreditDefaultSwapSecurity) bean).getReferenceEntity();
        case -2129778896:  // startDate
          return ((AbstractCreditDefaultSwapSecurity) bean).getStartDate();
        case -930389515:  // effectiveDate
          return ((AbstractCreditDefaultSwapSecurity) bean).getEffectiveDate();
        case -414641441:  // maturityDate
          return ((AbstractCreditDefaultSwapSecurity) bean).getMaturityDate();
        case 1873675528:  // stubType
          return ((AbstractCreditDefaultSwapSecurity) bean).getStubType();
        case 144480214:  // couponFrequency
          return ((AbstractCreditDefaultSwapSecurity) bean).getCouponFrequency();
        case 1905311443:  // dayCount
          return ((AbstractCreditDefaultSwapSecurity) bean).getDayCount();
        case -1002835891:  // businessDayConvention
          return ((AbstractCreditDefaultSwapSecurity) bean).getBusinessDayConvention();
        case -1168632905:  // immAdjustMaturityDate
          return ((AbstractCreditDefaultSwapSecurity) bean).isImmAdjustMaturityDate();
        case -490317146:  // adjustEffectiveDate
          return ((AbstractCreditDefaultSwapSecurity) bean).isAdjustEffectiveDate();
        case -261898226:  // adjustMaturityDate
          return ((AbstractCreditDefaultSwapSecurity) bean).isAdjustMaturityDate();
        case 1585636160:  // notional
          return ((AbstractCreditDefaultSwapSecurity) bean).getNotional();
        case 2100149628:  // includeAccruedPremium
          return ((AbstractCreditDefaultSwapSecurity) bean).isIncludeAccruedPremium();
        case 2103482633:  // protectionStart
          return ((AbstractCreditDefaultSwapSecurity) bean).isProtectionStart();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 97926:  // buy
          ((AbstractCreditDefaultSwapSecurity) bean).setBuy((Boolean) newValue);
          return;
        case 2087835226:  // protectionBuyer
          ((AbstractCreditDefaultSwapSecurity) bean).setProtectionBuyer((ExternalId) newValue);
          return;
        case 769920952:  // protectionSeller
          ((AbstractCreditDefaultSwapSecurity) bean).setProtectionSeller((ExternalId) newValue);
          return;
        case 480652046:  // referenceEntity
          ((AbstractCreditDefaultSwapSecurity) bean).setReferenceEntity((ExternalId) newValue);
          return;
        case -2129778896:  // startDate
          ((AbstractCreditDefaultSwapSecurity) bean).setStartDate((ZonedDateTime) newValue);
          return;
        case -930389515:  // effectiveDate
          ((AbstractCreditDefaultSwapSecurity) bean).setEffectiveDate((ZonedDateTime) newValue);
          return;
        case -414641441:  // maturityDate
          ((AbstractCreditDefaultSwapSecurity) bean).setMaturityDate((ZonedDateTime) newValue);
          return;
        case 1873675528:  // stubType
          ((AbstractCreditDefaultSwapSecurity) bean).setStubType((StubType) newValue);
          return;
        case 144480214:  // couponFrequency
          ((AbstractCreditDefaultSwapSecurity) bean).setCouponFrequency((Frequency) newValue);
          return;
        case 1905311443:  // dayCount
          ((AbstractCreditDefaultSwapSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case -1002835891:  // businessDayConvention
          ((AbstractCreditDefaultSwapSecurity) bean).setBusinessDayConvention((BusinessDayConvention) newValue);
          return;
        case -1168632905:  // immAdjustMaturityDate
          ((AbstractCreditDefaultSwapSecurity) bean).setImmAdjustMaturityDate((Boolean) newValue);
          return;
        case -490317146:  // adjustEffectiveDate
          ((AbstractCreditDefaultSwapSecurity) bean).setAdjustEffectiveDate((Boolean) newValue);
          return;
        case -261898226:  // adjustMaturityDate
          ((AbstractCreditDefaultSwapSecurity) bean).setAdjustMaturityDate((Boolean) newValue);
          return;
        case 1585636160:  // notional
          ((AbstractCreditDefaultSwapSecurity) bean).setNotional((InterestRateNotional) newValue);
          return;
        case 2100149628:  // includeAccruedPremium
          ((AbstractCreditDefaultSwapSecurity) bean).setIncludeAccruedPremium((Boolean) newValue);
          return;
        case 2103482633:  // protectionStart
          ((AbstractCreditDefaultSwapSecurity) bean).setProtectionStart((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._buy, "buy");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._protectionBuyer, "protectionBuyer");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._protectionSeller, "protectionSeller");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._referenceEntity, "referenceEntity");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._startDate, "startDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._effectiveDate, "effectiveDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._maturityDate, "maturityDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._stubType, "stubType");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._couponFrequency, "couponFrequency");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._dayCount, "dayCount");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._businessDayConvention, "businessDayConvention");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._immAdjustMaturityDate, "immAdjustMaturityDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._adjustEffectiveDate, "adjustEffectiveDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._adjustMaturityDate, "adjustMaturityDate");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._notional, "notional");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._includeAccruedPremium, "includeAccruedPremium");
      JodaBeanUtils.notNull(((AbstractCreditDefaultSwapSecurity) bean)._protectionStart, "protectionStart");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
