/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import com.google.common.collect.ImmutableList;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A security for bond futures.
 */
@BeanDefinition
public class BondFutureSecurity extends FutureSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The deliverables.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<BondFutureDeliverable> _basket = new ArrayList<BondFutureDeliverable>();
  /**
   * The first notice date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _firstNoticeDate;
  /**
   * The last notice date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _lastNoticeDate;
  /**
   * The first delivery date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _firstDeliveryDate;
  /**
   * The last delivery date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _lastDeliveryDate;

  BondFutureSecurity() { //For builder
    super();
  }

  /**
   * Constructs a bond future with the assumption that the first and last notice date are the same as the expiry date.
   * 
   * @param expiry the expiry of the bond future.
   * @param tradingExchange the trading exchange of the bond future.
   * @param settlementExchange the settlement exchange of the bond future.
   * @param currency the currency of the bond future.
   * @param unitAmount the unit amount of the bond future.
   * @param basket the deliverable basket of bonds.
   * @param firstDeliveryDate the first delivery date of the bond future.
   * @param lastDeliveryDate  the last delivery date of the bond future.
   * @param category the bond future category.
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, double unitAmount,
      Collection<? extends BondFutureDeliverable> basket, ZonedDateTime firstDeliveryDate, ZonedDateTime lastDeliveryDate, String category) {
    this(expiry,
         tradingExchange,
         settlementExchange,
         currency,
         unitAmount,
         basket,
         expiry.getExpiry(),
         expiry.getExpiry(),
         firstDeliveryDate,
         lastDeliveryDate,
         category);
  }
  
  /**
   * Constructs a bond future.
   * 
   * @param expiry the expiry of the bond future.
   * @param tradingExchange the trading exchange of the bond future.
   * @param settlementExchange the settlement exchange of the bond future.
   * @param currency the currency of the bond future.
   * @param unitAmount the unit amount of the bond future.
   * @param basket the deliverable basket of bonds.
   * @param firstNoticeDate the first notice date of the bond future.
   * @param lastNoticeDate the last notice date of the bond future.
   * @param firstDeliveryDate the first delivery date of the bond future.
   * @param lastDeliveryDate  the last delivery date of the bond future.
   * @param category the bond future category.
   */
  public BondFutureSecurity(Expiry expiry,
                            String tradingExchange,
                            String settlementExchange,
                            Currency currency,
                            double unitAmount,
                            Collection<? extends BondFutureDeliverable> basket,
                            ZonedDateTime firstNoticeDate,
                            ZonedDateTime lastNoticeDate,
                            ZonedDateTime firstDeliveryDate,
                            ZonedDateTime lastDeliveryDate,
                            String category) {
    super(expiry, tradingExchange, settlementExchange, currency, unitAmount, category);
    setBasket(ImmutableList.copyOf(basket));
    setFirstNoticeDate(firstNoticeDate);
    setLastNoticeDate(lastNoticeDate);
    setFirstDeliveryDate(firstDeliveryDate);
    setLastDeliveryDate(lastDeliveryDate);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BondFutureSecurity}.
   * @return the meta-bean, not null
   */
  public static BondFutureSecurity.Meta meta() {
    return BondFutureSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BondFutureSecurity.Meta.INSTANCE);
  }

  @Override
  public BondFutureSecurity.Meta metaBean() {
    return BondFutureSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the deliverables.
   * @return the value of the property, not null
   */
  public List<BondFutureDeliverable> getBasket() {
    return _basket;
  }

  /**
   * Sets the deliverables.
   * @param basket  the new value of the property, not null
   */
  public void setBasket(List<BondFutureDeliverable> basket) {
    JodaBeanUtils.notNull(basket, "basket");
    this._basket.clear();
    this._basket.addAll(basket);
  }

  /**
   * Gets the the {@code basket} property.
   * @return the property, not null
   */
  public final Property<List<BondFutureDeliverable>> basket() {
    return metaBean().basket().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first notice date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getFirstNoticeDate() {
    return _firstNoticeDate;
  }

  /**
   * Sets the first notice date.
   * @param firstNoticeDate  the new value of the property, not null
   */
  public void setFirstNoticeDate(ZonedDateTime firstNoticeDate) {
    JodaBeanUtils.notNull(firstNoticeDate, "firstNoticeDate");
    this._firstNoticeDate = firstNoticeDate;
  }

  /**
   * Gets the the {@code firstNoticeDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> firstNoticeDate() {
    return metaBean().firstNoticeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last notice date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getLastNoticeDate() {
    return _lastNoticeDate;
  }

  /**
   * Sets the last notice date.
   * @param lastNoticeDate  the new value of the property, not null
   */
  public void setLastNoticeDate(ZonedDateTime lastNoticeDate) {
    JodaBeanUtils.notNull(lastNoticeDate, "lastNoticeDate");
    this._lastNoticeDate = lastNoticeDate;
  }

  /**
   * Gets the the {@code lastNoticeDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> lastNoticeDate() {
    return metaBean().lastNoticeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first delivery date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getFirstDeliveryDate() {
    return _firstDeliveryDate;
  }

  /**
   * Sets the first delivery date.
   * @param firstDeliveryDate  the new value of the property, not null
   */
  public void setFirstDeliveryDate(ZonedDateTime firstDeliveryDate) {
    JodaBeanUtils.notNull(firstDeliveryDate, "firstDeliveryDate");
    this._firstDeliveryDate = firstDeliveryDate;
  }

  /**
   * Gets the the {@code firstDeliveryDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> firstDeliveryDate() {
    return metaBean().firstDeliveryDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last delivery date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getLastDeliveryDate() {
    return _lastDeliveryDate;
  }

  /**
   * Sets the last delivery date.
   * @param lastDeliveryDate  the new value of the property, not null
   */
  public void setLastDeliveryDate(ZonedDateTime lastDeliveryDate) {
    JodaBeanUtils.notNull(lastDeliveryDate, "lastDeliveryDate");
    this._lastDeliveryDate = lastDeliveryDate;
  }

  /**
   * Gets the the {@code lastDeliveryDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> lastDeliveryDate() {
    return metaBean().lastDeliveryDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public BondFutureSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BondFutureSecurity other = (BondFutureSecurity) obj;
      return JodaBeanUtils.equal(getBasket(), other.getBasket()) &&
          JodaBeanUtils.equal(getFirstNoticeDate(), other.getFirstNoticeDate()) &&
          JodaBeanUtils.equal(getLastNoticeDate(), other.getLastNoticeDate()) &&
          JodaBeanUtils.equal(getFirstDeliveryDate(), other.getFirstDeliveryDate()) &&
          JodaBeanUtils.equal(getLastDeliveryDate(), other.getLastDeliveryDate()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getBasket());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFirstNoticeDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLastNoticeDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFirstDeliveryDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLastDeliveryDate());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("BondFutureSecurity{");
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
    buf.append("basket").append('=').append(JodaBeanUtils.toString(getBasket())).append(',').append(' ');
    buf.append("firstNoticeDate").append('=').append(JodaBeanUtils.toString(getFirstNoticeDate())).append(',').append(' ');
    buf.append("lastNoticeDate").append('=').append(JodaBeanUtils.toString(getLastNoticeDate())).append(',').append(' ');
    buf.append("firstDeliveryDate").append('=').append(JodaBeanUtils.toString(getFirstDeliveryDate())).append(',').append(' ');
    buf.append("lastDeliveryDate").append('=').append(JodaBeanUtils.toString(getLastDeliveryDate())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BondFutureSecurity}.
   */
  public static class Meta extends FutureSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code basket} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<BondFutureDeliverable>> _basket = DirectMetaProperty.ofReadWrite(
        this, "basket", BondFutureSecurity.class, (Class) List.class);
    /**
     * The meta-property for the {@code firstNoticeDate} property.
     */
    private final MetaProperty<ZonedDateTime> _firstNoticeDate = DirectMetaProperty.ofReadWrite(
        this, "firstNoticeDate", BondFutureSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code lastNoticeDate} property.
     */
    private final MetaProperty<ZonedDateTime> _lastNoticeDate = DirectMetaProperty.ofReadWrite(
        this, "lastNoticeDate", BondFutureSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code firstDeliveryDate} property.
     */
    private final MetaProperty<ZonedDateTime> _firstDeliveryDate = DirectMetaProperty.ofReadWrite(
        this, "firstDeliveryDate", BondFutureSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code lastDeliveryDate} property.
     */
    private final MetaProperty<ZonedDateTime> _lastDeliveryDate = DirectMetaProperty.ofReadWrite(
        this, "lastDeliveryDate", BondFutureSecurity.class, ZonedDateTime.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "basket",
        "firstNoticeDate",
        "lastNoticeDate",
        "firstDeliveryDate",
        "lastDeliveryDate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1396196922:  // basket
          return _basket;
        case -1085415050:  // firstNoticeDate
          return _firstNoticeDate;
        case -1060668964:  // lastNoticeDate
          return _lastNoticeDate;
        case 1755448466:  // firstDeliveryDate
          return _firstDeliveryDate;
        case -233366664:  // lastDeliveryDate
          return _lastDeliveryDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BondFutureSecurity> builder() {
      return new DirectBeanBuilder<BondFutureSecurity>(new BondFutureSecurity());
    }

    @Override
    public Class<? extends BondFutureSecurity> beanType() {
      return BondFutureSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code basket} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<BondFutureDeliverable>> basket() {
      return _basket;
    }

    /**
     * The meta-property for the {@code firstNoticeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> firstNoticeDate() {
      return _firstNoticeDate;
    }

    /**
     * The meta-property for the {@code lastNoticeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> lastNoticeDate() {
      return _lastNoticeDate;
    }

    /**
     * The meta-property for the {@code firstDeliveryDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> firstDeliveryDate() {
      return _firstDeliveryDate;
    }

    /**
     * The meta-property for the {@code lastDeliveryDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> lastDeliveryDate() {
      return _lastDeliveryDate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1396196922:  // basket
          return ((BondFutureSecurity) bean).getBasket();
        case -1085415050:  // firstNoticeDate
          return ((BondFutureSecurity) bean).getFirstNoticeDate();
        case -1060668964:  // lastNoticeDate
          return ((BondFutureSecurity) bean).getLastNoticeDate();
        case 1755448466:  // firstDeliveryDate
          return ((BondFutureSecurity) bean).getFirstDeliveryDate();
        case -233366664:  // lastDeliveryDate
          return ((BondFutureSecurity) bean).getLastDeliveryDate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1396196922:  // basket
          ((BondFutureSecurity) bean).setBasket((List<BondFutureDeliverable>) newValue);
          return;
        case -1085415050:  // firstNoticeDate
          ((BondFutureSecurity) bean).setFirstNoticeDate((ZonedDateTime) newValue);
          return;
        case -1060668964:  // lastNoticeDate
          ((BondFutureSecurity) bean).setLastNoticeDate((ZonedDateTime) newValue);
          return;
        case 1755448466:  // firstDeliveryDate
          ((BondFutureSecurity) bean).setFirstDeliveryDate((ZonedDateTime) newValue);
          return;
        case -233366664:  // lastDeliveryDate
          ((BondFutureSecurity) bean).setLastDeliveryDate((ZonedDateTime) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((BondFutureSecurity) bean)._basket, "basket");
      JodaBeanUtils.notNull(((BondFutureSecurity) bean)._firstNoticeDate, "firstNoticeDate");
      JodaBeanUtils.notNull(((BondFutureSecurity) bean)._lastNoticeDate, "lastNoticeDate");
      JodaBeanUtils.notNull(((BondFutureSecurity) bean)._firstDeliveryDate, "firstDeliveryDate");
      JodaBeanUtils.notNull(((BondFutureSecurity) bean)._lastDeliveryDate, "lastDeliveryDate");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
