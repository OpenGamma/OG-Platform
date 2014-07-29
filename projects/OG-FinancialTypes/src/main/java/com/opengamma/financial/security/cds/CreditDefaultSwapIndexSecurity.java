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
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDescription;

/**
 * A credit security based on a underlying CDS Index rather than a reference
 * entity for a standard CDS trade.
 */
@BeanDefinition
@SecurityDescription(type = CreditDefaultSwapIndexSecurity.SECURITY_TYPE, description = "Cds index")
public class CreditDefaultSwapIndexSecurity extends AbstractCreditDefaultSwapSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The security type
   */
  public static final String SECURITY_TYPE = "CDS_INDEX";

  /**
   * The date on which the upfront payment is exchanged (usually T + 3bd).
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _settlementDate;

  /**
   * Flag to determine if we business day adjust the user input settlement
   * date (not a standard feature of index CDS positions).
   */
  @PropertyDefinition(validate = "notNull")
  private boolean _adjustSettlementDate;

  /**
   * The amount of upfront exchanged (usually on T+ 3bd) - can be positive or negative.
   */
  @PropertyDefinition(validate = "notNull")
  private InterestRateNotional _upfrontPayment;

  /**
   * The fixed index coupon (fixed at the issuance of the index).
   */
  @PropertyDefinition(validate = "notNull")
  private double _indexCoupon;

  /**
   * Constructor for Joda bean usage.
   */
  CreditDefaultSwapIndexSecurity() {
    super(SECURITY_TYPE);
  }


  public CreditDefaultSwapIndexSecurity(boolean buy,  // CSIGNORE: number of parameters is appropriate here
                                        ExternalId protectionBuyer,
                                        ExternalId protectionSeller,
                                        ExternalId underlyingIndex,
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
                                        boolean protectionStart,
                                        ZonedDateTime settlementDate,
                                        boolean adjustSettlementDate,
                                        InterestRateNotional upfrontPayment,
                                        double indexCoupon) {
    super(SECURITY_TYPE,
          buy,
          protectionBuyer,
          protectionSeller,
          underlyingIndex,
          startDate,
          effectiveDate,
          maturityDate,
          stubType,
          couponFrequency,
          dayCount,
          businessDayConvention,
          immAdjustMaturityDate,
          adjustEffectiveDate,
          adjustMaturityDate,
          notional,
          includeAccruedPremium,
          protectionStart);
    setSettlementDate(settlementDate);
    setAdjustSettlementDate(adjustSettlementDate);
    setUpfrontPayment(upfrontPayment);
    setIndexCoupon(indexCoupon);
  }

  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitCreditDefaultSwapIndexSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CreditDefaultSwapIndexSecurity}.
   * @return the meta-bean, not null
   */
  public static CreditDefaultSwapIndexSecurity.Meta meta() {
    return CreditDefaultSwapIndexSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CreditDefaultSwapIndexSecurity.Meta.INSTANCE);
  }

  @Override
  public CreditDefaultSwapIndexSecurity.Meta metaBean() {
    return CreditDefaultSwapIndexSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date on which the upfront payment is exchanged (usually T + 3bd).
   * @return the value of the property, not null
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the date on which the upfront payment is exchanged (usually T + 3bd).
   * @param settlementDate  the new value of the property, not null
   */
  public void setSettlementDate(ZonedDateTime settlementDate) {
    JodaBeanUtils.notNull(settlementDate, "settlementDate");
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
   * Gets flag to determine if we business day adjust the user input settlement
   * date (not a standard feature of index CDS positions).
   * @return the value of the property, not null
   */
  public boolean isAdjustSettlementDate() {
    return _adjustSettlementDate;
  }

  /**
   * Sets flag to determine if we business day adjust the user input settlement
   * date (not a standard feature of index CDS positions).
   * @param adjustSettlementDate  the new value of the property, not null
   */
  public void setAdjustSettlementDate(boolean adjustSettlementDate) {
    JodaBeanUtils.notNull(adjustSettlementDate, "adjustSettlementDate");
    this._adjustSettlementDate = adjustSettlementDate;
  }

  /**
   * Gets the the {@code adjustSettlementDate} property.
   * date (not a standard feature of index CDS positions).
   * @return the property, not null
   */
  public final Property<Boolean> adjustSettlementDate() {
    return metaBean().adjustSettlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount of upfront exchanged (usually on T+ 3bd) - can be positive or negative.
   * @return the value of the property, not null
   */
  public InterestRateNotional getUpfrontPayment() {
    return _upfrontPayment;
  }

  /**
   * Sets the amount of upfront exchanged (usually on T+ 3bd) - can be positive or negative.
   * @param upfrontPayment  the new value of the property, not null
   */
  public void setUpfrontPayment(InterestRateNotional upfrontPayment) {
    JodaBeanUtils.notNull(upfrontPayment, "upfrontPayment");
    this._upfrontPayment = upfrontPayment;
  }

  /**
   * Gets the the {@code upfrontPayment} property.
   * @return the property, not null
   */
  public final Property<InterestRateNotional> upfrontPayment() {
    return metaBean().upfrontPayment().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixed index coupon (fixed at the issuance of the index).
   * @return the value of the property, not null
   */
  public double getIndexCoupon() {
    return _indexCoupon;
  }

  /**
   * Sets the fixed index coupon (fixed at the issuance of the index).
   * @param indexCoupon  the new value of the property, not null
   */
  public void setIndexCoupon(double indexCoupon) {
    JodaBeanUtils.notNull(indexCoupon, "indexCoupon");
    this._indexCoupon = indexCoupon;
  }

  /**
   * Gets the the {@code indexCoupon} property.
   * @return the property, not null
   */
  public final Property<Double> indexCoupon() {
    return metaBean().indexCoupon().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CreditDefaultSwapIndexSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CreditDefaultSwapIndexSecurity other = (CreditDefaultSwapIndexSecurity) obj;
      return JodaBeanUtils.equal(getSettlementDate(), other.getSettlementDate()) &&
          (isAdjustSettlementDate() == other.isAdjustSettlementDate()) &&
          JodaBeanUtils.equal(getUpfrontPayment(), other.getUpfrontPayment()) &&
          JodaBeanUtils.equal(getIndexCoupon(), other.getIndexCoupon()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getSettlementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(isAdjustSettlementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUpfrontPayment());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIndexCoupon());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("CreditDefaultSwapIndexSecurity{");
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
    buf.append("settlementDate").append('=').append(JodaBeanUtils.toString(getSettlementDate())).append(',').append(' ');
    buf.append("adjustSettlementDate").append('=').append(JodaBeanUtils.toString(isAdjustSettlementDate())).append(',').append(' ');
    buf.append("upfrontPayment").append('=').append(JodaBeanUtils.toString(getUpfrontPayment())).append(',').append(' ');
    buf.append("indexCoupon").append('=').append(JodaBeanUtils.toString(getIndexCoupon())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CreditDefaultSwapIndexSecurity}.
   */
  public static class Meta extends AbstractCreditDefaultSwapSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<ZonedDateTime> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", CreditDefaultSwapIndexSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code adjustSettlementDate} property.
     */
    private final MetaProperty<Boolean> _adjustSettlementDate = DirectMetaProperty.ofReadWrite(
        this, "adjustSettlementDate", CreditDefaultSwapIndexSecurity.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code upfrontPayment} property.
     */
    private final MetaProperty<InterestRateNotional> _upfrontPayment = DirectMetaProperty.ofReadWrite(
        this, "upfrontPayment", CreditDefaultSwapIndexSecurity.class, InterestRateNotional.class);
    /**
     * The meta-property for the {@code indexCoupon} property.
     */
    private final MetaProperty<Double> _indexCoupon = DirectMetaProperty.ofReadWrite(
        this, "indexCoupon", CreditDefaultSwapIndexSecurity.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "settlementDate",
        "adjustSettlementDate",
        "upfrontPayment",
        "indexCoupon");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -295948169:  // settlementDate
          return _settlementDate;
        case 461393382:  // adjustSettlementDate
          return _adjustSettlementDate;
        case -638821960:  // upfrontPayment
          return _upfrontPayment;
        case 880904088:  // indexCoupon
          return _indexCoupon;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CreditDefaultSwapIndexSecurity> builder() {
      return new DirectBeanBuilder<CreditDefaultSwapIndexSecurity>(new CreditDefaultSwapIndexSecurity());
    }

    @Override
    public Class<? extends CreditDefaultSwapIndexSecurity> beanType() {
      return CreditDefaultSwapIndexSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> settlementDate() {
      return _settlementDate;
    }

    /**
     * The meta-property for the {@code adjustSettlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> adjustSettlementDate() {
      return _adjustSettlementDate;
    }

    /**
     * The meta-property for the {@code upfrontPayment} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterestRateNotional> upfrontPayment() {
      return _upfrontPayment;
    }

    /**
     * The meta-property for the {@code indexCoupon} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> indexCoupon() {
      return _indexCoupon;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -295948169:  // settlementDate
          return ((CreditDefaultSwapIndexSecurity) bean).getSettlementDate();
        case 461393382:  // adjustSettlementDate
          return ((CreditDefaultSwapIndexSecurity) bean).isAdjustSettlementDate();
        case -638821960:  // upfrontPayment
          return ((CreditDefaultSwapIndexSecurity) bean).getUpfrontPayment();
        case 880904088:  // indexCoupon
          return ((CreditDefaultSwapIndexSecurity) bean).getIndexCoupon();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -295948169:  // settlementDate
          ((CreditDefaultSwapIndexSecurity) bean).setSettlementDate((ZonedDateTime) newValue);
          return;
        case 461393382:  // adjustSettlementDate
          ((CreditDefaultSwapIndexSecurity) bean).setAdjustSettlementDate((Boolean) newValue);
          return;
        case -638821960:  // upfrontPayment
          ((CreditDefaultSwapIndexSecurity) bean).setUpfrontPayment((InterestRateNotional) newValue);
          return;
        case 880904088:  // indexCoupon
          ((CreditDefaultSwapIndexSecurity) bean).setIndexCoupon((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CreditDefaultSwapIndexSecurity) bean)._settlementDate, "settlementDate");
      JodaBeanUtils.notNull(((CreditDefaultSwapIndexSecurity) bean)._adjustSettlementDate, "adjustSettlementDate");
      JodaBeanUtils.notNull(((CreditDefaultSwapIndexSecurity) bean)._upfrontPayment, "upfrontPayment");
      JodaBeanUtils.notNull(((CreditDefaultSwapIndexSecurity) bean)._indexCoupon, "indexCoupon");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
