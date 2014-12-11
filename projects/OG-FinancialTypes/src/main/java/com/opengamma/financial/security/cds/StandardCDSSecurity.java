/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;

/**
 *
 */
@BeanDefinition
public abstract class StandardCDSSecurity extends CreditDefaultSwapSecurity {

  /** Serialization version */
  private static final long serialVersionUID = 2L;

  /**
   * The quoted spread.
   */
  @PropertyDefinition(validate = "notNull")
  private double _quotedSpread;

  /**
   * The up-front amount.
   */
  @PropertyDefinition(validate = "notNull")
  private InterestRateNotional _upfrontAmount;

  StandardCDSSecurity(String securityType) { // For Fudge builder
    super(securityType);
  }

  public StandardCDSSecurity(final boolean isBuy, final ExternalId protectionSeller, final ExternalId protectionBuyer, final ExternalId referenceEntity, //CSIGNORE
      final DebtSeniority debtSeniority, final RestructuringClause restructuringClause, final ExternalId regionId, final ZonedDateTime startDate,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final StubType stubType, final Frequency couponFrequency, final DayCount dayCount,
      final BusinessDayConvention businessDayConvention, final boolean immAdjustMaturityDate, final boolean adjustEffectiveDate,
      final boolean adjustMaturityDate, final InterestRateNotional notional, final boolean includeAccruedPremium,
      final boolean protectionStart, final double quotedSpread, final InterestRateNotional upfrontAmount, final String securityType) {

    super(isBuy,
          protectionSeller,
          protectionBuyer,
          referenceEntity,
          debtSeniority,
          restructuringClause,
          regionId,
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
          protectionStart,
          securityType);
    setQuotedSpread(quotedSpread);
    setUpfrontAmount(upfrontAmount);
  }

  protected StandardCDSSecurity(boolean isBuy,  // CSIGNORE: number of parameters is appropriate here
                                ExternalId protectionSeller,
                                ExternalId protectionBuyer,
                                ExternalId referenceEntity,
                                DebtSeniority debtSeniority,
                                RestructuringClause restructuringClause,
                                ExternalId regionId,
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
                                String securityType,
                                double quotedSpread,
                                InterestRateNotional upfrontAmount) {
    super(isBuy,
          protectionSeller,
          protectionBuyer,
          referenceEntity,
          debtSeniority,
          restructuringClause,
          regionId,
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
          protectionStart,
          securityType);
    _quotedSpread = quotedSpread;
    _upfrontAmount = upfrontAmount;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code StandardCDSSecurity}.
   * @return the meta-bean, not null
   */
  public static StandardCDSSecurity.Meta meta() {
    return StandardCDSSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(StandardCDSSecurity.Meta.INSTANCE);
  }

  @Override
  public StandardCDSSecurity.Meta metaBean() {
    return StandardCDSSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the quoted spread.
   * @return the value of the property, not null
   */
  public double getQuotedSpread() {
    return _quotedSpread;
  }

  /**
   * Sets the quoted spread.
   * @param quotedSpread  the new value of the property, not null
   */
  public void setQuotedSpread(double quotedSpread) {
    JodaBeanUtils.notNull(quotedSpread, "quotedSpread");
    this._quotedSpread = quotedSpread;
  }

  /**
   * Gets the the {@code quotedSpread} property.
   * @return the property, not null
   */
  public final Property<Double> quotedSpread() {
    return metaBean().quotedSpread().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the up-front amount.
   * @return the value of the property, not null
   */
  public InterestRateNotional getUpfrontAmount() {
    return _upfrontAmount;
  }

  /**
   * Sets the up-front amount.
   * @param upfrontAmount  the new value of the property, not null
   */
  public void setUpfrontAmount(InterestRateNotional upfrontAmount) {
    JodaBeanUtils.notNull(upfrontAmount, "upfrontAmount");
    this._upfrontAmount = upfrontAmount;
  }

  /**
   * Gets the the {@code upfrontAmount} property.
   * @return the property, not null
   */
  public final Property<InterestRateNotional> upfrontAmount() {
    return metaBean().upfrontAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      StandardCDSSecurity other = (StandardCDSSecurity) obj;
      return JodaBeanUtils.equal(getQuotedSpread(), other.getQuotedSpread()) &&
          JodaBeanUtils.equal(getUpfrontAmount(), other.getUpfrontAmount()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getQuotedSpread());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUpfrontAmount());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("StandardCDSSecurity{");
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
    buf.append("quotedSpread").append('=').append(JodaBeanUtils.toString(getQuotedSpread())).append(',').append(' ');
    buf.append("upfrontAmount").append('=').append(JodaBeanUtils.toString(getUpfrontAmount())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code StandardCDSSecurity}.
   */
  public static class Meta extends CreditDefaultSwapSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code quotedSpread} property.
     */
    private final MetaProperty<Double> _quotedSpread = DirectMetaProperty.ofReadWrite(
        this, "quotedSpread", StandardCDSSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code upfrontAmount} property.
     */
    private final MetaProperty<InterestRateNotional> _upfrontAmount = DirectMetaProperty.ofReadWrite(
        this, "upfrontAmount", StandardCDSSecurity.class, InterestRateNotional.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "quotedSpread",
        "upfrontAmount");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -963526405:  // quotedSpread
          return _quotedSpread;
        case -716346778:  // upfrontAmount
          return _upfrontAmount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends StandardCDSSecurity> builder() {
      throw new UnsupportedOperationException("StandardCDSSecurity is an abstract class");
    }

    @Override
    public Class<? extends StandardCDSSecurity> beanType() {
      return StandardCDSSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code quotedSpread} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> quotedSpread() {
      return _quotedSpread;
    }

    /**
     * The meta-property for the {@code upfrontAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterestRateNotional> upfrontAmount() {
      return _upfrontAmount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -963526405:  // quotedSpread
          return ((StandardCDSSecurity) bean).getQuotedSpread();
        case -716346778:  // upfrontAmount
          return ((StandardCDSSecurity) bean).getUpfrontAmount();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -963526405:  // quotedSpread
          ((StandardCDSSecurity) bean).setQuotedSpread((Double) newValue);
          return;
        case -716346778:  // upfrontAmount
          ((StandardCDSSecurity) bean).setUpfrontAmount((InterestRateNotional) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((StandardCDSSecurity) bean)._quotedSpread, "quotedSpread");
      JodaBeanUtils.notNull(((StandardCDSSecurity) bean)._upfrontAmount, "upfrontAmount");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
