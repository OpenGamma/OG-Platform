/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Map;
import java.util.Set;

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

import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * The convention for the fixed leg of an interest rate swap.
 */
@BeanDefinition
public final class FixedInterestRateSwapLegConvention extends InterestRateSwapLegConvention {

  /**
   * Type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("FixedInterestRateSwapLeg");

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The payment lag in days.
   */
  @PropertyDefinition
  private int _paymentLag;

  /**
   * Creates an instance.
   */
  protected FixedInterestRateSwapLegConvention() {
  }

  /**
   * Creates an instance.
   * <p>
   * This instance will be incomplete with fields that are null that should not be.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   */
  public FixedInterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle) {
    super(name, externalIdBundle);
  }

  /**
   * Creates an instance.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param paymentCalendars  the payment calendars, not null
   * @param calculationCalendars  the calculation calendars, not null
   * @param maturityCalendars  the maturity calendars, not null
   * @param paymentDayConvention  the payment day convention, not null
   * @param calculationBusinessDayConvention  the calculation day convention, not null
   * @param maturityBusinessDayConvention  the maturity day convention, not null
   * @param dayCountConvention  the day count frequency, not null
   * @param paymentFrequency  the payment frequency, not null
   * @param calculationFrequency  the calculation frequency, not null
   * @param paymentRelativeTo  the payment is relative to the beginning or end of the period, not null
   * @param adjustedAccrual  whether the accrual should be adjusted
   * @param settlementDays  the number of settlement days
   * @param rollConvention  the roll convention, not null
   * @param compoundingMethod  the compounding, not null
   * @param paymentLag  the payment lag in days
   */
  public FixedInterestRateSwapLegConvention(final String name, final ExternalIdBundle externalIdBundle,
      final Set<ExternalId> paymentCalendars,
      final Set<ExternalId> calculationCalendars,
      final Set<ExternalId> maturityCalendars,
      final BusinessDayConvention paymentDayConvention,
      final BusinessDayConvention calculationBusinessDayConvention,
      final BusinessDayConvention maturityBusinessDayConvention,
      final DayCount dayCountConvention,
      final Frequency paymentFrequency,
      final Frequency calculationFrequency,
      final DateRelativeTo paymentRelativeTo,
      final boolean adjustedAccrual,
      final int settlementDays,
      final RollConvention rollConvention,
      final CompoundingMethod compoundingMethod,
      final int paymentLag) {
    super(name, externalIdBundle, paymentCalendars, calculationCalendars, maturityCalendars,
        paymentDayConvention, calculationBusinessDayConvention, maturityBusinessDayConvention,
        dayCountConvention, paymentFrequency, calculationFrequency, paymentRelativeTo,
        adjustedAccrual, settlementDays, rollConvention, compoundingMethod);
    setPaymentLag(paymentLag);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type identifying this convention.
   * 
   * @return the {@link #TYPE} constant, not null
   */
  @Override
  public ConventionType getConventionType() {
    return TYPE;
  }

  /**
   * Accepts a visitor to manage traversal of the hierarchy.
   *
   * @param <T>  the result type of the visitor
   * @param visitor  the visitor, not null
   * @return the result
   */
  @Override
  public <T> T accept(final FinancialConventionVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFixedInterestRateSwapLegConvention(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Create a leg from a convention.
   *
   * @param notional  the notional (may be simple or complex)
   * @param payOrReceive  is this a pay or receive leg?
   * @param rate  the interest rate (may be simple or complex)
   * @return the leg, not null
   */
  public FixedInterestRateSwapLeg toLeg(final InterestRateSwapNotional notional, final PayReceiveType payOrReceive, final Rate rate) {
    validate();
    final FixedInterestRateSwapLeg leg = new FixedInterestRateSwapLeg();
    leg.setRate(rate);
    leg.setPayReceiveType(payOrReceive);
    leg.setNotional(notional);
    leg.setDayCountConvention(getDayCountConvention());
    leg.setRollConvention(getRollConvention());
    // maturity date parameters
    leg.setMaturityDateBusinessDayConvention(getMaturityBusinessDayConvention());
    leg.setMaturityDateCalendars(getMaturityCalendars());
    // payment
    leg.setPaymentDateBusinessDayConvention(getPaymentDayConvention());
    leg.setPaymentDateCalendars(getPaymentCalendars());
    leg.setPaymentDateFrequency(getPaymentFrequency());
    leg.setPaymentOffset(-_paymentLag);
    leg.setPaymentDateRelativeTo(getPaymentRelativeTo());
    // accrual period parameters
    leg.setAccrualPeriodBusinessDayConvention(getCalculationBusinessDayConvention());
    leg.setAccrualPeriodCalendars(getCalculationCalendars());
    leg.setAccrualPeriodFrequency(getCalculationFrequency());
    // compounding method 
    leg.setCompoundingMethod(getCompoundingMethod());
    return leg;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedInterestRateSwapLegConvention}.
   * @return the meta-bean, not null
   */
  public static FixedInterestRateSwapLegConvention.Meta meta() {
    return FixedInterestRateSwapLegConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedInterestRateSwapLegConvention.Meta.INSTANCE);
  }

  @Override
  public FixedInterestRateSwapLegConvention.Meta metaBean() {
    return FixedInterestRateSwapLegConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment lag in days.
   * @return the value of the property
   */
  public int getPaymentLag() {
    return _paymentLag;
  }

  /**
   * Sets the payment lag in days.
   * @param paymentLag  the new value of the property
   */
  public void setPaymentLag(int paymentLag) {
    this._paymentLag = paymentLag;
  }

  /**
   * Gets the the {@code paymentLag} property.
   * @return the property, not null
   */
  public Property<Integer> paymentLag() {
    return metaBean().paymentLag().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FixedInterestRateSwapLegConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FixedInterestRateSwapLegConvention other = (FixedInterestRateSwapLegConvention) obj;
      return (getPaymentLag() == other.getPaymentLag()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentLag());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("FixedInterestRateSwapLegConvention{");
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
    buf.append("paymentLag").append('=').append(JodaBeanUtils.toString(getPaymentLag())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedInterestRateSwapLegConvention}.
   */
  public static final class Meta extends InterestRateSwapLegConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paymentLag} property.
     */
    private final MetaProperty<Integer> _paymentLag = DirectMetaProperty.ofReadWrite(
        this, "paymentLag", FixedInterestRateSwapLegConvention.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "paymentLag");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1612870060:  // paymentLag
          return _paymentLag;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FixedInterestRateSwapLegConvention> builder() {
      return new DirectBeanBuilder<FixedInterestRateSwapLegConvention>(new FixedInterestRateSwapLegConvention());
    }

    @Override
    public Class<? extends FixedInterestRateSwapLegConvention> beanType() {
      return FixedInterestRateSwapLegConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code paymentLag} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> paymentLag() {
      return _paymentLag;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1612870060:  // paymentLag
          return ((FixedInterestRateSwapLegConvention) bean).getPaymentLag();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1612870060:  // paymentLag
          ((FixedInterestRateSwapLegConvention) bean).setPaymentLag((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
