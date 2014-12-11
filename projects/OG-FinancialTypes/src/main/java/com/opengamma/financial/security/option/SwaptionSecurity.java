/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.LongShort;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDescription;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A security for equity options.
 */
@BeanDefinition
@SecurityDescription(type = SwaptionSecurity.SECURITY_TYPE, description = "Swaption")
public class SwaptionSecurity extends FinancialSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "SWAPTION";

  /**
   * The payer flag. 
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   */
  @PropertyDefinition
  private boolean _payer;
  /**
   * The underlying identifier.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private ExternalId _underlyingId;

  /**
   * The link to the underlying security. Generally this will
   * hold an external id, but it may hold the security itself.
   *
   * Does not form part of the bean definition so that the
   * serialized form remains unchanged.
   *
   * Ideally, we'd use something more specific than FinancialSecurity
   * but the swaps that may be used do not have a common superclass.
   */
  private SecurityLink<FinancialSecurity> _underlyingLink;

  /**
   * The long/short type.
   */
  @PropertyDefinition(validate = "notNull")
  private LongShort _longShort = LongShort.LONG;
  /**
   * The expiry.
   */
  @PropertyDefinition(validate = "notNull")
  private Expiry _expiry;
  /**
   * The cash settled flag.
   */
  @PropertyDefinition
  private boolean _cashSettled;
  /**
   * The currency.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The notional.
   */
  @PropertyDefinition
  private Double _notional;
  /**
   * The exercise type.
   */
  @PropertyDefinition(validate = "notNull")
  private ExerciseType _exerciseType;
  /**
   * The settlement date.
   */
  @PropertyDefinition
  private ZonedDateTime _settlementDate;

  SwaptionSecurity() { //For builder
    super(SECURITY_TYPE);
  }

  public SwaptionSecurity(boolean payer, ExternalId underlyingIdentifier, boolean isLong, Expiry expiry, boolean cashSettled, Currency currency) {
    this(payer, underlyingIdentifier, isLong, expiry, cashSettled, currency, null, new EuropeanExerciseType(), null);
  }

  public SwaptionSecurity(boolean payer, ExternalId underlyingIdentifier, boolean isLong,
                          Expiry expiry, boolean cashSettled, Currency currency, Double notional, ExerciseType exerciseType, ZonedDateTime settlementDate) {
    super(SECURITY_TYPE);
    setPayer(payer);
    setUnderlyingId(underlyingIdentifier);
    setLongShort(LongShort.ofLong(isLong));
    setExpiry(expiry);
    setCashSettled(cashSettled);
    setCurrency(currency);
    setNotional(notional);
    setExerciseType(exerciseType);
    setSettlementDate(settlementDate);
  }

  public SwaptionSecurity(boolean payer, SecurityLink<FinancialSecurity> underlyingLink, boolean isLong,
      Expiry expiry, boolean cashSettled, Currency currency, Double notional, ExerciseType exerciseType, ZonedDateTime settlementDate) {
    super(SECURITY_TYPE);
    setPayer(payer);
    setUnderlyingLink(underlyingLink);
    setLongShort(LongShort.ofLong(isLong));
    setExpiry(expiry);
    setCashSettled(cashSettled);
    setCurrency(currency);
    setNotional(notional);
    setExerciseType(exerciseType);
    setSettlementDate(settlementDate);
  }

  /**
   * Sets the underlying link.
   *
   * @param underlyingLink  the new value of the property, not null
   */
  public void setUnderlyingLink(SecurityLink<FinancialSecurity> underlyingLink) {
    this._underlyingLink = ArgumentChecker.notNull(underlyingLink, "underlyingLink");
    this._underlyingId = _underlyingLink.getIdentifier().iterator().next();
  }

  /**
   * Gets the underlying security link.
   *
   * @return the underlying link, not null
   */
  public SecurityLink<FinancialSecurity> getUnderlyingLink() {
    return _underlyingLink;
  }

  /**
   * Sets the underlying identifier.
   * @param underlyingId  the new value of the property, not null
   */
  public void setUnderlyingId(ExternalId underlyingId) {
    this._underlyingId = ArgumentChecker.notNull(underlyingId, "underlyingId");
    this._underlyingLink = SecurityLink.resolvable(underlyingId, FinancialSecurity.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public final <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitSwaptionSecurity(this);
  }
  //-------------------------------------------------------------------------

  /**
   * Checks if the long/short type is long.
   *
   * @return true if long, false if short
   */
  public boolean isLong() {
    return getLongShort().isLong();
  }

  /**
   * Checks if the long/short type is short.
   *
   * @return true if short, false if long
   */
  public boolean isShort() {
    return getLongShort().isShort();
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SwaptionSecurity}.
   * @return the meta-bean, not null
   */
  public static SwaptionSecurity.Meta meta() {
    return SwaptionSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SwaptionSecurity.Meta.INSTANCE);
  }

  @Override
  public SwaptionSecurity.Meta metaBean() {
    return SwaptionSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payer flag.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @return the value of the property
   */
  public boolean isPayer() {
    return _payer;
  }

  /**
   * Sets the payer flag.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @param payer  the new value of the property
   */
  public void setPayer(boolean payer) {
    this._payer = payer;
  }

  /**
   * Gets the the {@code payer} property.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @return the property, not null
   */
  public final Property<Boolean> payer() {
    return metaBean().payer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying identifier.
   * @return the value of the property, not null
   */
  public ExternalId getUnderlyingId() {
    return _underlyingId;
  }

  /**
   * Gets the the {@code underlyingId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> underlyingId() {
    return metaBean().underlyingId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the long/short type.
   * @return the value of the property, not null
   */
  public LongShort getLongShort() {
    return _longShort;
  }

  /**
   * Sets the long/short type.
   * @param longShort  the new value of the property, not null
   */
  public void setLongShort(LongShort longShort) {
    JodaBeanUtils.notNull(longShort, "longShort");
    this._longShort = longShort;
  }

  /**
   * Gets the the {@code longShort} property.
   * @return the property, not null
   */
  public final Property<LongShort> longShort() {
    return metaBean().longShort().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the expiry.
   * @return the value of the property, not null
   */
  public Expiry getExpiry() {
    return _expiry;
  }

  /**
   * Sets the expiry.
   * @param expiry  the new value of the property, not null
   */
  public void setExpiry(Expiry expiry) {
    JodaBeanUtils.notNull(expiry, "expiry");
    this._expiry = expiry;
  }

  /**
   * Gets the the {@code expiry} property.
   * @return the property, not null
   */
  public final Property<Expiry> expiry() {
    return metaBean().expiry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cash settled flag.
   * @return the value of the property
   */
  public boolean isCashSettled() {
    return _cashSettled;
  }

  /**
   * Sets the cash settled flag.
   * @param cashSettled  the new value of the property
   */
  public void setCashSettled(boolean cashSettled) {
    this._cashSettled = cashSettled;
  }

  /**
   * Gets the the {@code cashSettled} property.
   * @return the property, not null
   */
  public final Property<Boolean> cashSettled() {
    return metaBean().cashSettled().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * Note - this field isn't used by the analytics since it duplicates information
   * on the underlying. (See PLAT-1924).
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the notional.
   * @return the value of the property
   */
  public Double getNotional() {
    return _notional;
  }

  /**
   * Sets the notional.
   * @param notional  the new value of the property
   */
  public void setNotional(Double notional) {
    this._notional = notional;
  }

  /**
   * Gets the the {@code notional} property.
   * @return the property, not null
   */
  public final Property<Double> notional() {
    return metaBean().notional().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exercise type.
   * @return the value of the property, not null
   */
  public ExerciseType getExerciseType() {
    return _exerciseType;
  }

  /**
   * Sets the exercise type.
   * @param exerciseType  the new value of the property, not null
   */
  public void setExerciseType(ExerciseType exerciseType) {
    JodaBeanUtils.notNull(exerciseType, "exerciseType");
    this._exerciseType = exerciseType;
  }

  /**
   * Gets the the {@code exerciseType} property.
   * @return the property, not null
   */
  public final Property<ExerciseType> exerciseType() {
    return metaBean().exerciseType().createProperty(this);
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
  @Override
  public SwaptionSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SwaptionSecurity other = (SwaptionSecurity) obj;
      return (isPayer() == other.isPayer()) &&
          JodaBeanUtils.equal(getUnderlyingId(), other.getUnderlyingId()) &&
          JodaBeanUtils.equal(getLongShort(), other.getLongShort()) &&
          JodaBeanUtils.equal(getExpiry(), other.getExpiry()) &&
          (isCashSettled() == other.isCashSettled()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getNotional(), other.getNotional()) &&
          JodaBeanUtils.equal(getExerciseType(), other.getExerciseType()) &&
          JodaBeanUtils.equal(getSettlementDate(), other.getSettlementDate()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(isPayer());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLongShort());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExpiry());
    hash = hash * 31 + JodaBeanUtils.hashCode(isCashSettled());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNotional());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExerciseType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementDate());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("SwaptionSecurity{");
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
    buf.append("payer").append('=').append(JodaBeanUtils.toString(isPayer())).append(',').append(' ');
    buf.append("underlyingId").append('=').append(JodaBeanUtils.toString(getUnderlyingId())).append(',').append(' ');
    buf.append("longShort").append('=').append(JodaBeanUtils.toString(getLongShort())).append(',').append(' ');
    buf.append("expiry").append('=').append(JodaBeanUtils.toString(getExpiry())).append(',').append(' ');
    buf.append("cashSettled").append('=').append(JodaBeanUtils.toString(isCashSettled())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("notional").append('=').append(JodaBeanUtils.toString(getNotional())).append(',').append(' ');
    buf.append("exerciseType").append('=').append(JodaBeanUtils.toString(getExerciseType())).append(',').append(' ');
    buf.append("settlementDate").append('=').append(JodaBeanUtils.toString(getSettlementDate())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SwaptionSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code payer} property.
     */
    private final MetaProperty<Boolean> _payer = DirectMetaProperty.ofReadWrite(
        this, "payer", SwaptionSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code underlyingId} property.
     */
    private final MetaProperty<ExternalId> _underlyingId = DirectMetaProperty.ofReadWrite(
        this, "underlyingId", SwaptionSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code longShort} property.
     */
    private final MetaProperty<LongShort> _longShort = DirectMetaProperty.ofReadWrite(
        this, "longShort", SwaptionSecurity.class, LongShort.class);
    /**
     * The meta-property for the {@code expiry} property.
     */
    private final MetaProperty<Expiry> _expiry = DirectMetaProperty.ofReadWrite(
        this, "expiry", SwaptionSecurity.class, Expiry.class);
    /**
     * The meta-property for the {@code cashSettled} property.
     */
    private final MetaProperty<Boolean> _cashSettled = DirectMetaProperty.ofReadWrite(
        this, "cashSettled", SwaptionSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", SwaptionSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code notional} property.
     */
    private final MetaProperty<Double> _notional = DirectMetaProperty.ofReadWrite(
        this, "notional", SwaptionSecurity.class, Double.class);
    /**
     * The meta-property for the {@code exerciseType} property.
     */
    private final MetaProperty<ExerciseType> _exerciseType = DirectMetaProperty.ofReadWrite(
        this, "exerciseType", SwaptionSecurity.class, ExerciseType.class);
    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<ZonedDateTime> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", SwaptionSecurity.class, ZonedDateTime.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "payer",
        "underlyingId",
        "longShort",
        "expiry",
        "cashSettled",
        "currency",
        "notional",
        "exerciseType",
        "settlementDate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 106443605:  // payer
          return _payer;
        case -771625640:  // underlyingId
          return _underlyingId;
        case 116685664:  // longShort
          return _longShort;
        case -1289159373:  // expiry
          return _expiry;
        case -871053882:  // cashSettled
          return _cashSettled;
        case 575402001:  // currency
          return _currency;
        case 1585636160:  // notional
          return _notional;
        case -466331342:  // exerciseType
          return _exerciseType;
        case -295948169:  // settlementDate
          return _settlementDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SwaptionSecurity> builder() {
      return new DirectBeanBuilder<SwaptionSecurity>(new SwaptionSecurity());
    }

    @Override
    public Class<? extends SwaptionSecurity> beanType() {
      return SwaptionSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code payer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> payer() {
      return _payer;
    }

    /**
     * The meta-property for the {@code underlyingId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> underlyingId() {
      return _underlyingId;
    }

    /**
     * The meta-property for the {@code longShort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LongShort> longShort() {
      return _longShort;
    }

    /**
     * The meta-property for the {@code expiry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Expiry> expiry() {
      return _expiry;
    }

    /**
     * The meta-property for the {@code cashSettled} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> cashSettled() {
      return _cashSettled;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code notional} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> notional() {
      return _notional;
    }

    /**
     * The meta-property for the {@code exerciseType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExerciseType> exerciseType() {
      return _exerciseType;
    }

    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> settlementDate() {
      return _settlementDate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 106443605:  // payer
          return ((SwaptionSecurity) bean).isPayer();
        case -771625640:  // underlyingId
          return ((SwaptionSecurity) bean).getUnderlyingId();
        case 116685664:  // longShort
          return ((SwaptionSecurity) bean).getLongShort();
        case -1289159373:  // expiry
          return ((SwaptionSecurity) bean).getExpiry();
        case -871053882:  // cashSettled
          return ((SwaptionSecurity) bean).isCashSettled();
        case 575402001:  // currency
          return ((SwaptionSecurity) bean).getCurrency();
        case 1585636160:  // notional
          return ((SwaptionSecurity) bean).getNotional();
        case -466331342:  // exerciseType
          return ((SwaptionSecurity) bean).getExerciseType();
        case -295948169:  // settlementDate
          return ((SwaptionSecurity) bean).getSettlementDate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 106443605:  // payer
          ((SwaptionSecurity) bean).setPayer((Boolean) newValue);
          return;
        case -771625640:  // underlyingId
          ((SwaptionSecurity) bean).setUnderlyingId((ExternalId) newValue);
          return;
        case 116685664:  // longShort
          ((SwaptionSecurity) bean).setLongShort((LongShort) newValue);
          return;
        case -1289159373:  // expiry
          ((SwaptionSecurity) bean).setExpiry((Expiry) newValue);
          return;
        case -871053882:  // cashSettled
          ((SwaptionSecurity) bean).setCashSettled((Boolean) newValue);
          return;
        case 575402001:  // currency
          ((SwaptionSecurity) bean).setCurrency((Currency) newValue);
          return;
        case 1585636160:  // notional
          ((SwaptionSecurity) bean).setNotional((Double) newValue);
          return;
        case -466331342:  // exerciseType
          ((SwaptionSecurity) bean).setExerciseType((ExerciseType) newValue);
          return;
        case -295948169:  // settlementDate
          ((SwaptionSecurity) bean).setSettlementDate((ZonedDateTime) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SwaptionSecurity) bean)._underlyingId, "underlyingId");
      JodaBeanUtils.notNull(((SwaptionSecurity) bean)._longShort, "longShort");
      JodaBeanUtils.notNull(((SwaptionSecurity) bean)._expiry, "expiry");
      JodaBeanUtils.notNull(((SwaptionSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((SwaptionSecurity) bean)._exerciseType, "exerciseType");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
