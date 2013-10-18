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
 * <li>Accrual year fraction</li>
 * <li>Payment time</li>
 * <li>Payment year fraction</li>
 * <li>Discount factor</li>
 * <li>Notional</li>
 * <li>Rate</li>
 * <p>
 * There is an entry for each coupon in a fixed leg.
 */
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
   * The accrual fraction label.
   */
  public static final String ACCRUAL_FRACTION = "Accrual Year Fraction";

  /**
   * The payment time label.
   */
  public static final String PAYMENT_TIME = "Payment Time";

  /**
   * The payment year fraction label.
   */
  public static final String PAYMENT_YEAR_FRACTION = "Payment Year Fraction";

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

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * An array of accrual start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _startAccrualDates;

  /**
   * An array of accrual end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _endAccrualDates;

  /**
   * An array of accrual fractions.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _accrualFractions;

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
   * An array of notionals.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _notionals;

  /**
   * An array of fixed rates.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _fixedRates;

  /**
   * For the builder.
   */
  /* package */FixedSwapLegDetails() {
    super();
  }

  /**
   * All arrays must be the same length
   * @param startAccrualDates The start accrual dates, not null
   * @param endAccrualDates The end accrual dates, not null
   * @param accrualFractions The accrual fractions, not null
   * @param paymentTimes The payment times, not null
   * @param paymentFractions The payment year fractions, not null
   * @param discountFactors The discount factors, not null
   * @param notionals The notionals, not null
   * @param fixedRates The fixed rates, not null
   */
  public FixedSwapLegDetails(final LocalDate[] startAccrualDates, final LocalDate[] endAccrualDates, final double[] accrualFractions,
      final double[] discountFactors, final double[] paymentTimes, final double[] paymentFractions, final CurrencyAmount[] notionals,
      final double[] fixedRates) {
    final int n = startAccrualDates.length;
    ArgumentChecker.isTrue(n == endAccrualDates.length, "Must have same number of start and end accrual dates");
    ArgumentChecker.isTrue(n == accrualFractions.length, "Must have same number of start accrual dates and accrual fractions");
    ArgumentChecker.isTrue(n == discountFactors.length, "Must have same number of start accrual dates and discount factors");
    ArgumentChecker.isTrue(n == paymentTimes.length, "Must have same number of start accrual dates and payment times");
    ArgumentChecker.isTrue(n == paymentFractions.length, "Must have same number of start accrual dates and payment year fractions");
    ArgumentChecker.isTrue(n == notionals.length, "Must have same number of start accrual dates and notionals");
    ArgumentChecker.isTrue(n == fixedRates.length, "Must have same number of start accrual dates and fixed rates");
    setStartAccrualDates(startAccrualDates);
    setEndAccrualDates(endAccrualDates);
    setAccrualFractions(accrualFractions);
    setDiscountFactors(discountFactors);
    setPaymentTimes(paymentTimes);
    setPaymentFractions(paymentFractions);
    setNotionals(notionals);
    setFixedRates(fixedRates);
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
  public LocalDate[] getStartAccrualDates() {
    return _startAccrualDates;
  }

  /**
   * Sets an array of accrual start dates.
   * @param startAccrualDates  the new value of the property, not null
   */
  public void setStartAccrualDates(LocalDate[] startAccrualDates) {
    JodaBeanUtils.notNull(startAccrualDates, "startAccrualDates");
    this._startAccrualDates = startAccrualDates;
  }

  /**
   * Gets the the {@code startAccrualDates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> startAccrualDates() {
    return metaBean().startAccrualDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual end dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getEndAccrualDates() {
    return _endAccrualDates;
  }

  /**
   * Sets an array of accrual end dates.
   * @param endAccrualDates  the new value of the property, not null
   */
  public void setEndAccrualDates(LocalDate[] endAccrualDates) {
    JodaBeanUtils.notNull(endAccrualDates, "endAccrualDates");
    this._endAccrualDates = endAccrualDates;
  }

  /**
   * Gets the the {@code endAccrualDates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> endAccrualDates() {
    return metaBean().endAccrualDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of accrual fractions.
   * @return the value of the property, not null
   */
  public double[] getAccrualFractions() {
    return (_accrualFractions != null ? _accrualFractions.clone() : null);
  }

  /**
   * Sets an array of accrual fractions.
   * @param accrualFractions  the new value of the property, not null
   */
  public void setAccrualFractions(double[] accrualFractions) {
    JodaBeanUtils.notNull(accrualFractions, "accrualFractions");
    this._accrualFractions = accrualFractions;
  }

  /**
   * Gets the the {@code accrualFractions} property.
   * @return the property, not null
   */
  public final Property<double[]> accrualFractions() {
    return metaBean().accrualFractions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of discount factors for the payments.
   * @return the value of the property, not null
   */
  public double[] getDiscountFactors() {
    return (_discountFactors != null ? _discountFactors.clone() : null);
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
    return (_paymentTimes != null ? _paymentTimes.clone() : null);
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
    return (_paymentFractions != null ? _paymentFractions.clone() : null);
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
  public double[] getFixedRates() {
    return (_fixedRates != null ? _fixedRates.clone() : null);
  }

  /**
   * Sets an array of fixed rates.
   * @param fixedRates  the new value of the property, not null
   */
  public void setFixedRates(double[] fixedRates) {
    JodaBeanUtils.notNull(fixedRates, "fixedRates");
    this._fixedRates = fixedRates;
  }

  /**
   * Gets the the {@code fixedRates} property.
   * @return the property, not null
   */
  public final Property<double[]> fixedRates() {
    return metaBean().fixedRates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FixedSwapLegDetails clone() {
    BeanBuilder<? extends FixedSwapLegDetails> builder = metaBean().builder();
    for (MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = ((Bean) value).clone();
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FixedSwapLegDetails other = (FixedSwapLegDetails) obj;
      return JodaBeanUtils.equal(getStartAccrualDates(), other.getStartAccrualDates()) &&
          JodaBeanUtils.equal(getEndAccrualDates(), other.getEndAccrualDates()) &&
          JodaBeanUtils.equal(getAccrualFractions(), other.getAccrualFractions()) &&
          JodaBeanUtils.equal(getDiscountFactors(), other.getDiscountFactors()) &&
          JodaBeanUtils.equal(getPaymentTimes(), other.getPaymentTimes()) &&
          JodaBeanUtils.equal(getPaymentFractions(), other.getPaymentFractions()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getFixedRates(), other.getFixedRates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getStartAccrualDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEndAccrualDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAccrualFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDiscountFactors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentTimes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRates());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
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
    buf.append("startAccrualDates").append('=').append(JodaBeanUtils.toString(getStartAccrualDates())).append(',').append(' ');
    buf.append("endAccrualDates").append('=').append(JodaBeanUtils.toString(getEndAccrualDates())).append(',').append(' ');
    buf.append("accrualFractions").append('=').append(JodaBeanUtils.toString(getAccrualFractions())).append(',').append(' ');
    buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(getDiscountFactors())).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(getPaymentTimes())).append(',').append(' ');
    buf.append("paymentFractions").append('=').append(JodaBeanUtils.toString(getPaymentFractions())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(getFixedRates())).append(',').append(' ');
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
     * The meta-property for the {@code startAccrualDates} property.
     */
    private final MetaProperty<LocalDate[]> _startAccrualDates = DirectMetaProperty.ofReadWrite(
        this, "startAccrualDates", FixedSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code endAccrualDates} property.
     */
    private final MetaProperty<LocalDate[]> _endAccrualDates = DirectMetaProperty.ofReadWrite(
        this, "endAccrualDates", FixedSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code accrualFractions} property.
     */
    private final MetaProperty<double[]> _accrualFractions = DirectMetaProperty.ofReadWrite(
        this, "accrualFractions", FixedSwapLegDetails.class, double[].class);
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
     * The meta-property for the {@code notionals} property.
     */
    private final MetaProperty<CurrencyAmount[]> _notionals = DirectMetaProperty.ofReadWrite(
        this, "notionals", FixedSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    private final MetaProperty<double[]> _fixedRates = DirectMetaProperty.ofReadWrite(
        this, "fixedRates", FixedSwapLegDetails.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "startAccrualDates",
        "endAccrualDates",
        "accrualFractions",
        "discountFactors",
        "paymentTimes",
        "paymentFractions",
        "notionals",
        "fixedRates");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1870068632:  // startAccrualDates
          return _startAccrualDates;
        case -187313519:  // endAccrualDates
          return _endAccrualDates;
        case 1288547778:  // accrualFractions
          return _accrualFractions;
        case -91613053:  // discountFactors
          return _discountFactors;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case 1206997835:  // paymentFractions
          return _paymentFractions;
        case 1910080819:  // notionals
          return _notionals;
        case 1695350911:  // fixedRates
          return _fixedRates;
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
     * The meta-property for the {@code startAccrualDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> startAccrualDates() {
      return _startAccrualDates;
    }

    /**
     * The meta-property for the {@code endAccrualDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> endAccrualDates() {
      return _endAccrualDates;
    }

    /**
     * The meta-property for the {@code accrualFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> accrualFractions() {
      return _accrualFractions;
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
    public final MetaProperty<double[]> fixedRates() {
      return _fixedRates;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1870068632:  // startAccrualDates
          return ((FixedSwapLegDetails) bean).getStartAccrualDates();
        case -187313519:  // endAccrualDates
          return ((FixedSwapLegDetails) bean).getEndAccrualDates();
        case 1288547778:  // accrualFractions
          return ((FixedSwapLegDetails) bean).getAccrualFractions();
        case -91613053:  // discountFactors
          return ((FixedSwapLegDetails) bean).getDiscountFactors();
        case -507430688:  // paymentTimes
          return ((FixedSwapLegDetails) bean).getPaymentTimes();
        case 1206997835:  // paymentFractions
          return ((FixedSwapLegDetails) bean).getPaymentFractions();
        case 1910080819:  // notionals
          return ((FixedSwapLegDetails) bean).getNotionals();
        case 1695350911:  // fixedRates
          return ((FixedSwapLegDetails) bean).getFixedRates();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1870068632:  // startAccrualDates
          ((FixedSwapLegDetails) bean).setStartAccrualDates((LocalDate[]) newValue);
          return;
        case -187313519:  // endAccrualDates
          ((FixedSwapLegDetails) bean).setEndAccrualDates((LocalDate[]) newValue);
          return;
        case 1288547778:  // accrualFractions
          ((FixedSwapLegDetails) bean).setAccrualFractions((double[]) newValue);
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
        case 1910080819:  // notionals
          ((FixedSwapLegDetails) bean).setNotionals((CurrencyAmount[]) newValue);
          return;
        case 1695350911:  // fixedRates
          ((FixedSwapLegDetails) bean).setFixedRates((double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._startAccrualDates, "startAccrualDates");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._endAccrualDates, "endAccrualDates");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._accrualFractions, "accrualFractions");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._discountFactors, "discountFactors");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._paymentTimes, "paymentTimes");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._paymentFractions, "paymentFractions");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._notionals, "notionals");
      JodaBeanUtils.notNull(((FixedSwapLegDetails) bean)._fixedRates, "fixedRates");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
