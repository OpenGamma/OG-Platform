/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fx;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A Hibernate bean representation of {@link FXForwardSecurity}.
 */
@BeanDefinition
public class NonDeliverableFXForwardSecurityBean extends SecurityBean {

  @PropertyDefinition
  private ZonedDateTimeBean _forwardDate;
  @PropertyDefinition
  private ExternalIdBean _region;
  @PropertyDefinition
  private CurrencyBean _payCurrency;
  @PropertyDefinition
  private double _payAmount;
  @PropertyDefinition
  private CurrencyBean _receiveCurrency;
  @PropertyDefinition
  private double _receiveAmount;
  @PropertyDefinition
  private boolean _deliverInReceiveCurrency;

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof NonDeliverableFXForwardSecurityBean)) {
      return false;
    }
    NonDeliverableFXForwardSecurityBean fxForward = (NonDeliverableFXForwardSecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), fxForward.getId())
      .append(getForwardDate(), fxForward.getForwardDate())
      .append(getPayCurrency(), fxForward.getPayCurrency())
      .append(getPayAmount(), fxForward.getPayAmount())
      .append(getReceiveCurrency(), fxForward.getReceiveCurrency())
      .append(getReceiveAmount(), fxForward.getReceiveAmount())
      .append(getRegion(), fxForward.getRegion())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getForwardDate())
      .append(getPayCurrency())
      .append(getPayAmount())
      .append(getReceiveCurrency())
      .append(getReceiveAmount())
      .toHashCode();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code NonDeliverableFXForwardSecurityBean}.
   * @return the meta-bean, not null
   */
  public static NonDeliverableFXForwardSecurityBean.Meta meta() {
    return NonDeliverableFXForwardSecurityBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(NonDeliverableFXForwardSecurityBean.Meta.INSTANCE);
  }

  @Override
  public NonDeliverableFXForwardSecurityBean.Meta metaBean() {
    return NonDeliverableFXForwardSecurityBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the forwardDate.
   * @return the value of the property
   */
  public ZonedDateTimeBean getForwardDate() {
    return _forwardDate;
  }

  /**
   * Sets the forwardDate.
   * @param forwardDate  the new value of the property
   */
  public void setForwardDate(ZonedDateTimeBean forwardDate) {
    this._forwardDate = forwardDate;
  }

  /**
   * Gets the the {@code forwardDate} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTimeBean> forwardDate() {
    return metaBean().forwardDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region.
   * @return the value of the property
   */
  public ExternalIdBean getRegion() {
    return _region;
  }

  /**
   * Sets the region.
   * @param region  the new value of the property
   */
  public void setRegion(ExternalIdBean region) {
    this._region = region;
  }

  /**
   * Gets the the {@code region} property.
   * @return the property, not null
   */
  public final Property<ExternalIdBean> region() {
    return metaBean().region().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payCurrency.
   * @return the value of the property
   */
  public CurrencyBean getPayCurrency() {
    return _payCurrency;
  }

  /**
   * Sets the payCurrency.
   * @param payCurrency  the new value of the property
   */
  public void setPayCurrency(CurrencyBean payCurrency) {
    this._payCurrency = payCurrency;
  }

  /**
   * Gets the the {@code payCurrency} property.
   * @return the property, not null
   */
  public final Property<CurrencyBean> payCurrency() {
    return metaBean().payCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payAmount.
   * @return the value of the property
   */
  public double getPayAmount() {
    return _payAmount;
  }

  /**
   * Sets the payAmount.
   * @param payAmount  the new value of the property
   */
  public void setPayAmount(double payAmount) {
    this._payAmount = payAmount;
  }

  /**
   * Gets the the {@code payAmount} property.
   * @return the property, not null
   */
  public final Property<Double> payAmount() {
    return metaBean().payAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the receiveCurrency.
   * @return the value of the property
   */
  public CurrencyBean getReceiveCurrency() {
    return _receiveCurrency;
  }

  /**
   * Sets the receiveCurrency.
   * @param receiveCurrency  the new value of the property
   */
  public void setReceiveCurrency(CurrencyBean receiveCurrency) {
    this._receiveCurrency = receiveCurrency;
  }

  /**
   * Gets the the {@code receiveCurrency} property.
   * @return the property, not null
   */
  public final Property<CurrencyBean> receiveCurrency() {
    return metaBean().receiveCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the receiveAmount.
   * @return the value of the property
   */
  public double getReceiveAmount() {
    return _receiveAmount;
  }

  /**
   * Sets the receiveAmount.
   * @param receiveAmount  the new value of the property
   */
  public void setReceiveAmount(double receiveAmount) {
    this._receiveAmount = receiveAmount;
  }

  /**
   * Gets the the {@code receiveAmount} property.
   * @return the property, not null
   */
  public final Property<Double> receiveAmount() {
    return metaBean().receiveAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the deliverInReceiveCurrency.
   * @return the value of the property
   */
  public boolean isDeliverInReceiveCurrency() {
    return _deliverInReceiveCurrency;
  }

  /**
   * Sets the deliverInReceiveCurrency.
   * @param deliverInReceiveCurrency  the new value of the property
   */
  public void setDeliverInReceiveCurrency(boolean deliverInReceiveCurrency) {
    this._deliverInReceiveCurrency = deliverInReceiveCurrency;
  }

  /**
   * Gets the the {@code deliverInReceiveCurrency} property.
   * @return the property, not null
   */
  public final Property<Boolean> deliverInReceiveCurrency() {
    return metaBean().deliverInReceiveCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public NonDeliverableFXForwardSecurityBean clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("NonDeliverableFXForwardSecurityBean{");
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
    buf.append("forwardDate").append('=').append(JodaBeanUtils.toString(getForwardDate())).append(',').append(' ');
    buf.append("region").append('=').append(JodaBeanUtils.toString(getRegion())).append(',').append(' ');
    buf.append("payCurrency").append('=').append(JodaBeanUtils.toString(getPayCurrency())).append(',').append(' ');
    buf.append("payAmount").append('=').append(JodaBeanUtils.toString(getPayAmount())).append(',').append(' ');
    buf.append("receiveCurrency").append('=').append(JodaBeanUtils.toString(getReceiveCurrency())).append(',').append(' ');
    buf.append("receiveAmount").append('=').append(JodaBeanUtils.toString(getReceiveAmount())).append(',').append(' ');
    buf.append("deliverInReceiveCurrency").append('=').append(JodaBeanUtils.toString(isDeliverInReceiveCurrency())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code NonDeliverableFXForwardSecurityBean}.
   */
  public static class Meta extends SecurityBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code forwardDate} property.
     */
    private final MetaProperty<ZonedDateTimeBean> _forwardDate = DirectMetaProperty.ofReadWrite(
        this, "forwardDate", NonDeliverableFXForwardSecurityBean.class, ZonedDateTimeBean.class);
    /**
     * The meta-property for the {@code region} property.
     */
    private final MetaProperty<ExternalIdBean> _region = DirectMetaProperty.ofReadWrite(
        this, "region", NonDeliverableFXForwardSecurityBean.class, ExternalIdBean.class);
    /**
     * The meta-property for the {@code payCurrency} property.
     */
    private final MetaProperty<CurrencyBean> _payCurrency = DirectMetaProperty.ofReadWrite(
        this, "payCurrency", NonDeliverableFXForwardSecurityBean.class, CurrencyBean.class);
    /**
     * The meta-property for the {@code payAmount} property.
     */
    private final MetaProperty<Double> _payAmount = DirectMetaProperty.ofReadWrite(
        this, "payAmount", NonDeliverableFXForwardSecurityBean.class, Double.TYPE);
    /**
     * The meta-property for the {@code receiveCurrency} property.
     */
    private final MetaProperty<CurrencyBean> _receiveCurrency = DirectMetaProperty.ofReadWrite(
        this, "receiveCurrency", NonDeliverableFXForwardSecurityBean.class, CurrencyBean.class);
    /**
     * The meta-property for the {@code receiveAmount} property.
     */
    private final MetaProperty<Double> _receiveAmount = DirectMetaProperty.ofReadWrite(
        this, "receiveAmount", NonDeliverableFXForwardSecurityBean.class, Double.TYPE);
    /**
     * The meta-property for the {@code deliverInReceiveCurrency} property.
     */
    private final MetaProperty<Boolean> _deliverInReceiveCurrency = DirectMetaProperty.ofReadWrite(
        this, "deliverInReceiveCurrency", NonDeliverableFXForwardSecurityBean.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "forwardDate",
        "region",
        "payCurrency",
        "payAmount",
        "receiveCurrency",
        "receiveAmount",
        "deliverInReceiveCurrency");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1652755475:  // forwardDate
          return _forwardDate;
        case -934795532:  // region
          return _region;
        case -295641895:  // payCurrency
          return _payCurrency;
        case -1338781920:  // payAmount
          return _payAmount;
        case -1228590060:  // receiveCurrency
          return _receiveCurrency;
        case 984267035:  // receiveAmount
          return _receiveAmount;
        case 2073187722:  // deliverInReceiveCurrency
          return _deliverInReceiveCurrency;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends NonDeliverableFXForwardSecurityBean> builder() {
      return new DirectBeanBuilder<NonDeliverableFXForwardSecurityBean>(new NonDeliverableFXForwardSecurityBean());
    }

    @Override
    public Class<? extends NonDeliverableFXForwardSecurityBean> beanType() {
      return NonDeliverableFXForwardSecurityBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code forwardDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTimeBean> forwardDate() {
      return _forwardDate;
    }

    /**
     * The meta-property for the {@code region} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBean> region() {
      return _region;
    }

    /**
     * The meta-property for the {@code payCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyBean> payCurrency() {
      return _payCurrency;
    }

    /**
     * The meta-property for the {@code payAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> payAmount() {
      return _payAmount;
    }

    /**
     * The meta-property for the {@code receiveCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyBean> receiveCurrency() {
      return _receiveCurrency;
    }

    /**
     * The meta-property for the {@code receiveAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> receiveAmount() {
      return _receiveAmount;
    }

    /**
     * The meta-property for the {@code deliverInReceiveCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> deliverInReceiveCurrency() {
      return _deliverInReceiveCurrency;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1652755475:  // forwardDate
          return ((NonDeliverableFXForwardSecurityBean) bean).getForwardDate();
        case -934795532:  // region
          return ((NonDeliverableFXForwardSecurityBean) bean).getRegion();
        case -295641895:  // payCurrency
          return ((NonDeliverableFXForwardSecurityBean) bean).getPayCurrency();
        case -1338781920:  // payAmount
          return ((NonDeliverableFXForwardSecurityBean) bean).getPayAmount();
        case -1228590060:  // receiveCurrency
          return ((NonDeliverableFXForwardSecurityBean) bean).getReceiveCurrency();
        case 984267035:  // receiveAmount
          return ((NonDeliverableFXForwardSecurityBean) bean).getReceiveAmount();
        case 2073187722:  // deliverInReceiveCurrency
          return ((NonDeliverableFXForwardSecurityBean) bean).isDeliverInReceiveCurrency();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1652755475:  // forwardDate
          ((NonDeliverableFXForwardSecurityBean) bean).setForwardDate((ZonedDateTimeBean) newValue);
          return;
        case -934795532:  // region
          ((NonDeliverableFXForwardSecurityBean) bean).setRegion((ExternalIdBean) newValue);
          return;
        case -295641895:  // payCurrency
          ((NonDeliverableFXForwardSecurityBean) bean).setPayCurrency((CurrencyBean) newValue);
          return;
        case -1338781920:  // payAmount
          ((NonDeliverableFXForwardSecurityBean) bean).setPayAmount((Double) newValue);
          return;
        case -1228590060:  // receiveCurrency
          ((NonDeliverableFXForwardSecurityBean) bean).setReceiveCurrency((CurrencyBean) newValue);
          return;
        case 984267035:  // receiveAmount
          ((NonDeliverableFXForwardSecurityBean) bean).setReceiveAmount((Double) newValue);
          return;
        case 2073187722:  // deliverInReceiveCurrency
          ((NonDeliverableFXForwardSecurityBean) bean).setDeliverInReceiveCurrency((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
