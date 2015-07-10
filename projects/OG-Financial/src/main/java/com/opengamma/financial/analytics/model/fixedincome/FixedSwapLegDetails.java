/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Container for the relevant details for pricing a fixed swap leg, with the entries
 * <p>
 * <li>Start accrual date</li>
 * <li>End accrual date</li>
 * <li>Payment time</li>
 * <li>Payment year fraction</li>
 * <li>Payment amount (non discounted)</li>
 * <li>Discount factor</li>
 * <li>Notional</li>
 * <li>Rate</li>
 * <li>Discounted payment amount</li>
 * <p>
 * There is an entry for each coupon in a fixed leg.
 * @deprecated Use FixedLegCashFlows
 */
@Deprecated
@BeanDefinition
public class FixedSwapLegDetails extends DirectBean implements Serializable {
  /**
   * The start accrual dates label.
   */
  public static final String START_ACCRUAL_DATES = "Start Accrual Date";
  /**
   * The end accrual dates label.
   */
  public static final String END_ACCRUAL_DATES = "End Accrual Date";
  /**
   * The payment time label.
   */
  public static final String PAYMENT_TIME = "Payment Time";
  /**
   * The payment year fraction label.
   */
  public static final String PAYMENT_YEAR_FRACTION = "Payment Year Fraction";
  /**
   * The payment amount label.
   */
  public static final String PAYMENT_AMOUNT = "Payment Amount";
  /**
   * The discount factor label.
   */
  public static final String DISCOUNT_FACTOR = "Discount Factor";
  /**
   * The notional label.
   */
  public static final String NOTIONAL = "Notional";
  /**
   * The fixed rate label.
   */
  public static final String FIXED_RATE = "Fixed Rate";
  /**
   * The discounted payment amount
   */
  public static final String DISCOUNTED_PAYMENT_AMOUNT = "Discounted Payment Amount";

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * An array of accrual start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _accrualStart;
  /**
   * An array of accrual end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _accrualEnd;
  /**
   * An array of discount factors for the payments.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _discountFactors;
  /**
   * An array of payment times.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _paymentTimes;
  /**
   * An array of payment year fractions.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _paymentFractions;
  /**
   * An array of payment amounts.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _paymentAmounts;
  /**
   * An array of notionals.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _notionals;
  /**
   * An array of fixed rates.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _fixedRates;

  /**
   * For the builder.
   */
  /* package */FixedSwapLegDetails() {
    super();
  }

  /**
   * All arrays must be the same length.
   * @param startAccrualDates The start accrual dates, not null
   * @param endAccrualDates The end accrual dates, not null
   * @param paymentTimes The payment times, not null
   * @param paymentFractions The payment year fractions, not null
   * @param discountFactors The discount factors, not null
   * @param paymentAmounts The payment amounts, not null
   * @param notionals The notionals, not null
   * @param fixedRates The fixed rates, not null
   */
  public FixedSwapLegDetails(final LocalDate[] startAccrualDates, final LocalDate[] endAccrualDates,
      final double[] discountFactors, final double[] paymentTimes, final double[] paymentFractions,
      final CurrencyAmount[] paymentAmounts, final CurrencyAmount[] notionals, final Double[] fixedRates) {
    setAccrualStart(startAccrualDates);
    setAccrualEnd(endAccrualDates);
    setDiscountFactors(discountFactors);
    setPaymentTimes(paymentTimes);
    setPaymentFractions(paymentFractions);
    setPaymentAmounts(paymentAmounts);
    setNotionals(notionals);
    setFixedRates(fixedRates);
    final int n = startAccrualDates.length;
    ArgumentChecker.isTrue(n == endAccrualDates.length, "Must have same number of start and end accrual dates");
    ArgumentChecker.isTrue(n == discountFactors.length, "Must have same number of start accrual dates and discount factors");
    ArgumentChecker.isTrue(n == paymentTimes.length, "Must have same number of start accrual dates and payment times");
    ArgumentChecker.isTrue(n == paymentFractions.length, "Must have same number of start accrual dates and payment year fractions");
    ArgumentChecker.isTrue(n == paymentAmounts.length, "Must have same number of start accrual dates and payment amounts");
    ArgumentChecker.isTrue(n == notionals.length, "Must have same number of start accrual dates and notionals");
    ArgumentChecker.isTrue(n == fixedRates.length, "Must have same number of start accrual dates and fixed rates");
  }

  /**
   * Gets the number of cash-flows.
   * @return the number of cash-flows
   */
  @DerivedProperty
  public int getNumberOfCashFlows() {
    return getAccrualStart().length;
  }

  /**
   * Gets the discounted payment amounts.
   * @return the discounted cashflows
   */
  @DerivedProperty
  public CurrencyAmount[] getDiscountedPaymentAmounts() {
    final CurrencyAmount[] cashflows = new CurrencyAmount[getNumberOfCashFlows()];
    for (int i = 0; i < getNumberOfCashFlows(); i++) {
      final CurrencyAmount payment = getPaymentAmounts()[i];
      if (payment == null) {
        continue;
      }
      final double df = getDiscountFactors()[i];
      cashflows[i] = CurrencyAmount.of(payment.getCurrency(), payment.getAmount() * df);
    }
    return cashflows;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedSwapLegDetails}.
   * @return the meta-bean, not null
   */
  public static FixedSwapLegDetails.Meta meta() {
    return FixedSwapLegDetails.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedSwapLegDetails.Meta.INSTANCE);
  }

  @Override
  public FixedSwapLegDetails.Meta metaBean() {
    return FixedSwapLegDetails.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual start dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getAccrualStart() {
    return _accrualStart;
  }

  /**
   * Sets an array of accrual start dates.
   * @param accrualStart  the new value of the property, not null
   */
  public void setAccrualStart(LocalDate[] accrualStart) {
    JodaBeanUtils.notNull(accrualStart, "accrualStart");
    this._accrualStart = accrualStart;
  }

  /**
   * Gets the the {@code accrualStart} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> accrualStart() {
    return metaBean().accrualStart().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual end dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getAccrualEnd() {
    return _accrualEnd;
  }

  /**
   * Sets an array of accrual end dates.
   * @param accrualEnd  the new value of the property, not null
   */
  public void setAccrualEnd(LocalDate[] accrualEnd) {
    JodaBeanUtils.notNull(accrualEnd, "accrualEnd");
    this._accrualEnd = accrualEnd;
  }

  /**
   * Gets the the {@code accrualEnd} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> accrualEnd() {
    return metaBean().accrualEnd().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of discount factors for the payments.
   * @return the value of the property, not null
   */
  public double[] getDiscountFactors() {
    return _discountFactors;
  }

  /**
   * Sets an array of discount factors for the payments.
   * @param discountFactors  the new value of the property, not null
   */
  public void setDiscountFactors(double[] discountFactors) {
    JodaBeanUtils.notNull(discountFactors, "discountFactors");
    this._discountFactors = discountFactors;
  }

  /**
   * Gets the the {@code discountFactors} property.
   * @return the property, not null
   */
  public final Property<double[]> discountFactors() {
    return metaBean().discountFactors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment times.
   * @return the value of the property, not null
   */
  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  /**
   * Sets an array of payment times.
   * @param paymentTimes  the new value of the property, not null
   */
  public void setPaymentTimes(double[] paymentTimes) {
    JodaBeanUtils.notNull(paymentTimes, "paymentTimes");
    this._paymentTimes = paymentTimes;
  }

  /**
   * Gets the the {@code paymentTimes} property.
   * @return the property, not null
   */
  public final Property<double[]> paymentTimes() {
    return metaBean().paymentTimes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment year fractions.
   * @return the value of the property, not null
   */
  public double[] getPaymentFractions() {
    return _paymentFractions;
  }

  /**
   * Sets an array of payment year fractions.
   * @param paymentFractions  the new value of the property, not null
   */
  public void setPaymentFractions(double[] paymentFractions) {
    JodaBeanUtils.notNull(paymentFractions, "paymentFractions");
    this._paymentFractions = paymentFractions;
  }

  /**
   * Gets the the {@code paymentFractions} property.
   * @return the property, not null
   */
  public final Property<double[]> paymentFractions() {
    return metaBean().paymentFractions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment amounts.
   * @return the value of the property, not null
   */
  public CurrencyAmount[] getPaymentAmounts() {
    return _paymentAmounts;
  }

  /**
   * Sets an array of payment amounts.
   * @param paymentAmounts  the new value of the property, not null
   */
  public void setPaymentAmounts(CurrencyAmount[] paymentAmounts) {
    JodaBeanUtils.notNull(paymentAmounts, "paymentAmounts");
    this._paymentAmounts = paymentAmounts;
  }

  /**
   * Gets the the {@code paymentAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> paymentAmounts() {
    return metaBean().paymentAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of notionals.
   * @return the value of the property, not null
   */
  public CurrencyAmount[] getNotionals() {
    return _notionals;
  }

  /**
   * Sets an array of notionals.
   * @param notionals  the new value of the property, not null
   */
  public void setNotionals(CurrencyAmount[] notionals) {
    JodaBeanUtils.notNull(notionals, "notionals");
    this._notionals = notionals;
  }

  /**
   * Gets the the {@code notionals} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> notionals() {
    return metaBean().notionals().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixed rates.
   * @return the value of the property, not null
   */
  public Double[] getFixedRates() {
    return _fixedRates;
  }

  /**
   * Sets an array of fixed rates.
   * @param fixedRates  the new value of the property, not null
   */
  public void setFixedRates(Double[] fixedRates) {
    JodaBeanUtils.notNull(fixedRates, "fixedRates");
    this._fixedRates = fixedRates;
  }

  /**
   * Gets the the {@code fixedRates} property.
   * @return the property, not null
   */
  public final Property<Double[]> fixedRates() {
    return metaBean().fixedRates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code numberOfCashFlows} property.
   * @return the property, not null
   */
  public final Property<Integer> numberOfCashFlows() {
    return metaBean().numberOfCashFlows().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code discountedPaymentAmounts} property.
   * @return the property, not null
   */
  public final Property<CurrencyAmount[]> discountedPaymentAmounts() {
    return metaBean().discountedPaymentAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FixedSwapLegDetails clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FixedSwapLegDetails other = (FixedSwapLegDetails) obj;
      return JodaBeanUtils.equal(getAccrualStart(), other.getAccrualStart()) &&
          JodaBeanUtils.equal(getAccrualEnd(), other.getAccrualEnd()) &&
          JodaBeanUtils.equal(getDiscountFactors(), other.getDiscountFactors()) &&
          JodaBeanUtils.equal(getPaymentTimes(), other.getPaymentTimes()) &&
          JodaBeanUtils.equal(getPaymentFractions(), other.getPaymentFractions()) &&
          JodaBeanUtils.equal(getPaymentAmounts(), other.getPaymentAmounts()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getFixedRates(), other.getFixedRates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getAccrualStart());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAccrualEnd());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDiscountFactors());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentTimes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentFractions());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentAmounts());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFixedRates());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("FixedSwapLegDetails{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("accrualStart").append('=').append(JodaBeanUtils.toString(getAccrualStart())).append(',').append(' ');
    buf.append("accrualEnd").append('=').append(JodaBeanUtils.toString(getAccrualEnd())).append(',').append(' ');
    buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(getDiscountFactors())).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(getPaymentTimes())).append(',').append(' ');
    buf.append("paymentFractions").append('=').append(JodaBeanUtils.toString(getPaymentFractions())).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(getPaymentAmounts())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(getFixedRates())).append(',').append(' ');
    buf.append("numberOfCashFlows").append('=').append(JodaBeanUtils.toString(getNumberOfCashFlows())).append(',').append(' ');
    buf.append("discountedPaymentAmounts").append('=').append(JodaBeanUtils.toString(getDiscountedPaymentAmounts())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedSwapLegDetails}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code accrualStart} property.
     */
    private final MetaProperty<LocalDate[]> _accrualStart = DirectMetaProperty.ofReadWrite(
        this, "accrualStart", FixedSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code accrualEnd} property.
     */
    private final MetaProperty<LocalDate[]> _accrualEnd = DirectMetaProperty.ofReadWrite(
        this, "accrualEnd", FixedSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code discountFactors} property.
     */
    private final MetaProperty<double[]> _discountFactors = DirectMetaProperty.ofReadWrite(
        this, "discountFactors", FixedSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentTimes} property.
     */
    private final MetaProperty<double[]> _paymentTimes = DirectMetaProperty.ofReadWrite(
        this, "paymentTimes", FixedSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentFractions} property.
     */
    private final MetaProperty<double[]> _paymentFractions = DirectMetaProperty.ofReadWrite(
        this, "paymentFractions", FixedSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _paymentAmounts = DirectMetaProperty.ofReadWrite(
        this, "paymentAmounts", FixedSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    private final MetaProperty<CurrencyAmount[]> _notionals = DirectMetaProperty.ofReadWrite(
        this, "notionals", FixedSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    private final MetaProperty<Double[]> _fixedRates = DirectMetaProperty.ofReadWrite(
        this, "fixedRates", FixedSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     */
    private final MetaProperty<Integer> _numberOfCashFlows = DirectMetaProperty.ofDerived(
        this, "numberOfCashFlows", FixedSwapLegDetails.class, Integer.TYPE);
    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     */
    private final MetaProperty<CurrencyAmount[]> _discountedPaymentAmounts = DirectMetaProperty.ofDerived(
        this, "discountedPaymentAmounts", FixedSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "accrualStart",
        "accrualEnd",
        "discountFactors",
        "paymentTimes",
        "paymentFractions",
        "paymentAmounts",
        "notionals",
        "fixedRates",
        "numberOfCashFlows",
        "discountedPaymentAmounts");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return _accrualStart;
        case 1846909100:  // accrualEnd
          return _accrualEnd;
        case -91613053:  // discountFactors
          return _discountFactors;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case 1206997835:  // paymentFractions
          return _paymentFractions;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case 1910080819:  // notionals
          return _notionals;
        case 1695350911:  // fixedRates
          return _fixedRates;
        case -338982286:  // numberOfCashFlows
          return _numberOfCashFlows;
        case 178231285:  // discountedPaymentAmounts
          return _discountedPaymentAmounts;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FixedSwapLegDetails> builder() {
      return new DirectBeanBuilder<FixedSwapLegDetails>(new FixedSwapLegDetails());
    }

    @Override
    public Class<? extends FixedSwapLegDetails> beanType() {
      return FixedSwapLegDetails.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code accrualStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> accrualStart() {
      return _accrualStart;
    }

    /**
     * The meta-property for the {@code accrualEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> accrualEnd() {
      return _accrualEnd;
    }

    /**
     * The meta-property for the {@code discountFactors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> discountFactors() {
      return _discountFactors;
    }

    /**
     * The meta-property for the {@code paymentTimes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> paymentTimes() {
      return _paymentTimes;
    }

    /**
     * The meta-property for the {@code paymentFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> paymentFractions() {
      return _paymentFractions;
    }

    /**
     * The meta-property for the {@code paymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> paymentAmounts() {
      return _paymentAmounts;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code fixedRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> fixedRates() {
      return _fixedRates;
    }

    /**
     * The meta-property for the {@code numberOfCashFlows} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> numberOfCashFlows() {
      return _numberOfCashFlows;
    }

    /**
     * The meta-property for the {@code discountedPaymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> discountedPaymentAmounts() {
      return _discountedPaymentAmounts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          return ((FixedSwapLegDetails) bean).getAccrualStart();
        case 1846909100:  // accrualEnd
          return ((FixedSwapLegDetails) bean).getAccrualEnd();
        case -91613053:  // discountFactors
          return ((FixedSwapLegDetails) bean).getDiscountFactors();
        case -507430688:  // paymentTimes
          return ((FixedSwapLegDetails) bean).getPaymentTimes();
        case 1206997835:  // paymentFractions
          return ((FixedSwapLegDetails) bean).getPaymentFractions();
        case -1875448267:  // paymentAmounts
          return ((FixedSwapLegDetails) bean).getPaymentAmounts();
        case 1910080819:  // notionals
          return ((FixedSwapLegDetails) bean).getNotionals();
        case 1695350911:  // fixedRates
          return ((FixedSwapLegDetails) bean).getFixedRates();
        case -338982286:  // numberOfCashFlows
          return ((FixedSwapLegDetails) bean).getNumberOfCashFlows();
        case 178231285:  // discountedPaymentAmounts
          return ((FixedSwapLegDetails) bean).getDiscountedPaymentAmounts();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1071260659:  // accrualStart
          ((FixedSwapLegDetails) bean).setAccrualStart((LocalDate[]) newValue);
          return;
        case 1846909100:  // accrualEnd
          ((FixedSwapLegDetails) bean).setAccrualEnd((LocalDate[]) newValue);
          return;
        case -91613053:  // discountFactors
          ((FixedSwapLegDetails) bean).setDiscountFactors((double[]) newValue);
          return;
        case -507430688:  // paymentTimes
          ((FixedSwapLegDetails) bean).setPaymentTimes((double[]) newValue);
          return;
        case 1206997835:  // paymentFractions
          ((FixedSwapLegDetails) bean).setPaymentFractions((double[]) newValue);
          return;
        case -1875448267:  // paymentAmounts
          ((FixedSwapLegDetails) bean).setPaymentAmounts((CurrencyAmount[]) newValue);
          return;
        case 1910080819:  // notionals
          ((FixedSwapLegDetails) bean).setNotionals((CurrencyAmount[]) newValue);
          return;
        case 1695350911:  // fixedRates
          ((FixedSwapLegDetails) bean).setFixedRates((Double[]) newValue);
          return;
        case -338982286:  // numberOfCashFlows
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: numberOfCashFlows");
        case 178231285:  // discountedPaymentAmounts
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: discountedPaymentAmounts");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._accrualStart, "accrualStart");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._accrualEnd, "accrualEnd");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._discountFactors, "discountFactors");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._paymentTimes, "paymentTimes");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._paymentFractions, "paymentFractions");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._paymentAmounts, "paymentAmounts");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._notionals, "notionals");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._fixedRates, "fixedRates");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
