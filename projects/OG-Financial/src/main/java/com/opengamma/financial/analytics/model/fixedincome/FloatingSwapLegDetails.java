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
import com.opengamma.util.time.Tenor;

/**
 * Container for the relevant details for pricing a floating swap leg, with the
 * entries
 * <p>
 * <li></li>
 */
@BeanDefinition
public class FloatingSwapLegDetails extends DirectBean implements Serializable {
  /**
   * The start fixing date label.
   */
  public static final String START_FIXING_DATES = "Start Fixing Date";
  /**
   * The end fixing date label.
   */
  public static final String END_FIXING_DATES = "End Fixing Date";
  /**
   * The fixing fraction label.
   */
  public static final String FIXING_FRACTIONS = "Fixing Year Fraction";
  /**
   * The forward rate. Used when the fixing is in the future.
   */
  public static final String FORWARD_RATE = "Forward Rate";
  /**
   * The fixed rate. Used when the fixing is known.
   */
  public static final String FIXED_RATE = "Fixed Rate";
  /**
   * The payment date.
   */
  public static final String PAYMENT_DATE = "Payment Date";
  /**
   * The payment time.
   */
  public static final String PAYMENT_TIME = "Payment Time";
  /**
   * The notional.
   */
  public static final String NOTIONAL = "Notional";
  /**
   * The spread.
   */
  public static final String SPREAD = "Spread";
  /**
   * The payment discount factor. Used when the fixing is known.
   */
  public static final String PAYMENT_DISCOUNT_FACTOR = "Payment Discount Factor";
  /**
   * The payment amount. Used when the fixing is known.
   */
  public static final String PAYMENT_AMOUNT = "Payment Amount";
  /**
   * The projected amount.
   */
  public static final String PROJECTED_AMOUNT = "Projected Amount";
  /**
   * The index tenor.
   */
  public static final String INDEX_TERM = "Index Tenor";

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * An array of fixing start dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _fixingStart;

  /**
   * An array of fixing end dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _fixingEnd;

  /**
   * An array of fixing year fractions.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _fixingYearFractions;

  /**
   * An array of forward rates. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _forwardRates;

  /**
   * An array of fixed rates. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _fixedRates;

  /**
   * An array of payment dates.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate[] _paymentDates;

  /**
   * An array of payment times.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _paymentTimes;

  /**
   * An array of notionals.
   */
  @PropertyDefinition(validate = "notNull")
  private CurrencyAmount[] _notionals;

  /**
   * An array of spreads.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _spreads;

  /**
   * An array of gearings.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _gearings;

  /**
   * An array of payment discount factors. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _paymentDiscountFactors;

  /**
   * An array of payment amounts. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _paymentAmounts;

  /**
   * An array of projected amounts. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _projectedAmounts;

  /**
   * An array of index tenors. Can have null entries.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor[] _indexTenors;

  /**
   * For the builder.
   */
  /* package */FloatingSwapLegDetails() {
    super();
  }

  /**
   * @param fixingStart The fixing start dates, not null
   * @param fixingEnd The fixing end dates, not null
   * @param fixingYearFractions The fixing year fractions, not null
   * @param forwardRates The forward rates, not null
   * @param fixedRates The fixed rates, not null
   * @param paymentDates The payment dates, not null
   * @param paymentTimes The payment times, not null
   * @param paymentDiscountFactors The payment discount factors, not null
   * @param paymentAmounts The payment amounts, not null
   * @param projectedAmounts The projected amounts, not null
   * @param notionals The notionals, not null
   * @param spreads The spreads, not null
   * @param gearings The gearings, not null
   * @param indexTenors The index tenors, not null
   */
  public FloatingSwapLegDetails(final LocalDate[] fixingStart, final LocalDate[] fixingEnd, final double[] fixingYearFractions, final Double[] forwardRates,
      final Double[] fixedRates, final LocalDate[] paymentDates, final double[] paymentTimes, final Double[] paymentDiscountFactors, final Double[] paymentAmounts,
      final Double[] projectedAmounts, final CurrencyAmount[] notionals, final double[] spreads, final double[] gearings, final Tenor[] indexTenors) {
    ArgumentChecker.notNull(fixingStart, "fixing start");
    final int total = notionals.length;
    final int floating = forwardRates.length;
    final int fixed = fixedRates.length;
    ArgumentChecker.isTrue(total == fixed + floating, "number of fixed and floating coupons must equal the total");
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingSwapLegDetails}.
   * @return the meta-bean, not null
   */
  public static FloatingSwapLegDetails.Meta meta() {
    return FloatingSwapLegDetails.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingSwapLegDetails.Meta.INSTANCE);
  }

  @Override
  public FloatingSwapLegDetails.Meta metaBean() {
    return FloatingSwapLegDetails.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing start dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getFixingStart() {
    return _fixingStart;
  }

  /**
   * Sets an array of fixing start dates.
   * @param fixingStart  the new value of the property, not null
   */
  public void setFixingStart(final LocalDate[] fixingStart) {
    JodaBeanUtils.notNull(fixingStart, "fixingStart");
    this._fixingStart = fixingStart;
  }

  /**
   * Gets the the {@code fixingStart} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> fixingStart() {
    return metaBean().fixingStart().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing end dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getFixingEnd() {
    return _fixingEnd;
  }

  /**
   * Sets an array of fixing end dates.
   * @param fixingEnd  the new value of the property, not null
   */
  public void setFixingEnd(final LocalDate[] fixingEnd) {
    JodaBeanUtils.notNull(fixingEnd, "fixingEnd");
    this._fixingEnd = fixingEnd;
  }

  /**
   * Gets the the {@code fixingEnd} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> fixingEnd() {
    return metaBean().fixingEnd().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixing year fractions.
   * @return the value of the property, not null
   */
  public double[] getFixingYearFractions() {
    return (_fixingYearFractions != null ? _fixingYearFractions.clone() : null);
  }

  /**
   * Sets an array of fixing year fractions.
   * @param fixingYearFractions  the new value of the property, not null
   */
  public void setFixingYearFractions(final double[] fixingYearFractions) {
    JodaBeanUtils.notNull(fixingYearFractions, "fixingYearFractions");
    this._fixingYearFractions = fixingYearFractions;
  }

  /**
   * Gets the the {@code fixingYearFractions} property.
   * @return the property, not null
   */
  public final Property<double[]> fixingYearFractions() {
    return metaBean().fixingYearFractions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of forward rates. Can have null entries.
   * @return the value of the property, not null
   */
  public Double[] getForwardRates() {
    return _forwardRates;
  }

  /**
   * Sets an array of forward rates. Can have null entries.
   * @param forwardRates  the new value of the property, not null
   */
  public void setForwardRates(final Double[] forwardRates) {
    JodaBeanUtils.notNull(forwardRates, "forwardRates");
    this._forwardRates = forwardRates;
  }

  /**
   * Gets the the {@code forwardRates} property.
   * @return the property, not null
   */
  public final Property<Double[]> forwardRates() {
    return metaBean().forwardRates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of fixed rates. Can have null entries.
   * @return the value of the property, not null
   */
  public Double[] getFixedRates() {
    return _fixedRates;
  }

  /**
   * Sets an array of fixed rates. Can have null entries.
   * @param fixedRates  the new value of the property, not null
   */
  public void setFixedRates(final Double[] fixedRates) {
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
   * Gets an array of payment dates.
   * @return the value of the property, not null
   */
  public LocalDate[] getPaymentDates() {
    return _paymentDates;
  }

  /**
   * Sets an array of payment dates.
   * @param paymentDates  the new value of the property, not null
   */
  public void setPaymentDates(final LocalDate[] paymentDates) {
    JodaBeanUtils.notNull(paymentDates, "paymentDates");
    this._paymentDates = paymentDates;
  }

  /**
   * Gets the the {@code paymentDates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> paymentDates() {
    return metaBean().paymentDates().createProperty(this);
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
  public void setPaymentTimes(final double[] paymentTimes) {
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
  public void setNotionals(final CurrencyAmount[] notionals) {
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
   * Gets an array of spreads.
   * @return the value of the property, not null
   */
  public double[] getSpreads() {
    return (_spreads != null ? _spreads.clone() : null);
  }

  /**
   * Sets an array of spreads.
   * @param spreads  the new value of the property, not null
   */
  public void setSpreads(final double[] spreads) {
    JodaBeanUtils.notNull(spreads, "spreads");
    this._spreads = spreads;
  }

  /**
   * Gets the the {@code spreads} property.
   * @return the property, not null
   */
  public final Property<double[]> spreads() {
    return metaBean().spreads().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment discount factors. Can have null entries.
   * @return the value of the property, not null
   */
  public Double[] getPaymentDiscountFactors() {
    return _paymentDiscountFactors;
  }

  /**
   * Sets an array of payment discount factors. Can have null entries.
   * @param paymentDiscountFactors  the new value of the property, not null
   */
  public void setPaymentDiscountFactors(final Double[] paymentDiscountFactors) {
    JodaBeanUtils.notNull(paymentDiscountFactors, "paymentDiscountFactors");
    this._paymentDiscountFactors = paymentDiscountFactors;
  }

  /**
   * Gets the the {@code paymentDiscountFactors} property.
   * @return the property, not null
   */
  public final Property<Double[]> paymentDiscountFactors() {
    return metaBean().paymentDiscountFactors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of payment amounts. Can have null entries.
   * @return the value of the property, not null
   */
  public Double[] getPaymentAmounts() {
    return _paymentAmounts;
  }

  /**
   * Sets an array of payment amounts. Can have null entries.
   * @param paymentAmounts  the new value of the property, not null
   */
  public void setPaymentAmounts(final Double[] paymentAmounts) {
    JodaBeanUtils.notNull(paymentAmounts, "paymentAmounts");
    this._paymentAmounts = paymentAmounts;
  }

  /**
   * Gets the the {@code paymentAmounts} property.
   * @return the property, not null
   */
  public final Property<Double[]> paymentAmounts() {
    return metaBean().paymentAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of projected amounts. Can have null entries.
   * @return the value of the property, not null
   */
  public Double[] getProjectedAmounts() {
    return _projectedAmounts;
  }

  /**
   * Sets an array of projected amounts. Can have null entries.
   * @param projectedAmounts  the new value of the property, not null
   */
  public void setProjectedAmounts(final Double[] projectedAmounts) {
    JodaBeanUtils.notNull(projectedAmounts, "projectedAmounts");
    this._projectedAmounts = projectedAmounts;
  }

  /**
   * Gets the the {@code projectedAmounts} property.
   * @return the property, not null
   */
  public final Property<Double[]> projectedAmounts() {
    return metaBean().projectedAmounts().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an array of index tenors. Can have null entries.
   * @return the value of the property, not null
   */
  public Tenor[] getIndexTenors() {
    return _indexTenors;
  }

  /**
   * Sets an array of index tenors. Can have null entries.
   * @param indexTenors  the new value of the property, not null
   */
  public void setIndexTenors(final Tenor[] indexTenors) {
    JodaBeanUtils.notNull(indexTenors, "indexTenors");
    this._indexTenors = indexTenors;
  }

  /**
   * Gets the the {@code indexTenors} property.
   * @return the property, not null
   */
  public final Property<Tenor[]> indexTenors() {
    return metaBean().indexTenors().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FloatingSwapLegDetails clone() {
    final BeanBuilder<? extends FloatingSwapLegDetails> builder = metaBean().builder();
    for (final MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
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
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      final FloatingSwapLegDetails other = (FloatingSwapLegDetails) obj;
      return JodaBeanUtils.equal(getFixingStart(), other.getFixingStart()) &&
          JodaBeanUtils.equal(getFixingEnd(), other.getFixingEnd()) &&
          JodaBeanUtils.equal(getFixingYearFractions(), other.getFixingYearFractions()) &&
          JodaBeanUtils.equal(getForwardRates(), other.getForwardRates()) &&
          JodaBeanUtils.equal(getFixedRates(), other.getFixedRates()) &&
          JodaBeanUtils.equal(getPaymentDates(), other.getPaymentDates()) &&
          JodaBeanUtils.equal(getPaymentTimes(), other.getPaymentTimes()) &&
          JodaBeanUtils.equal(getNotionals(), other.getNotionals()) &&
          JodaBeanUtils.equal(getSpreads(), other.getSpreads()) &&
          JodaBeanUtils.equal(getPaymentDiscountFactors(), other.getPaymentDiscountFactors()) &&
          JodaBeanUtils.equal(getPaymentAmounts(), other.getPaymentAmounts()) &&
          JodaBeanUtils.equal(getProjectedAmounts(), other.getProjectedAmounts()) &&
          JodaBeanUtils.equal(getIndexTenors(), other.getIndexTenors());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingEnd());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingYearFractions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getForwardRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDates());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentTimes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getNotionals());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpreads());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentDiscountFactors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaymentAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProjectedAmounts());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIndexTenors());
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder(448);
    buf.append("FloatingSwapLegDetails{");
    final int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(final StringBuilder buf) {
    buf.append("fixingStart").append('=').append(JodaBeanUtils.toString(getFixingStart())).append(',').append(' ');
    buf.append("fixingEnd").append('=').append(JodaBeanUtils.toString(getFixingEnd())).append(',').append(' ');
    buf.append("fixingYearFractions").append('=').append(JodaBeanUtils.toString(getFixingYearFractions())).append(',').append(' ');
    buf.append("forwardRates").append('=').append(JodaBeanUtils.toString(getForwardRates())).append(',').append(' ');
    buf.append("fixedRates").append('=').append(JodaBeanUtils.toString(getFixedRates())).append(',').append(' ');
    buf.append("paymentDates").append('=').append(JodaBeanUtils.toString(getPaymentDates())).append(',').append(' ');
    buf.append("paymentTimes").append('=').append(JodaBeanUtils.toString(getPaymentTimes())).append(',').append(' ');
    buf.append("notionals").append('=').append(JodaBeanUtils.toString(getNotionals())).append(',').append(' ');
    buf.append("spreads").append('=').append(JodaBeanUtils.toString(getSpreads())).append(',').append(' ');
    buf.append("paymentDiscountFactors").append('=').append(JodaBeanUtils.toString(getPaymentDiscountFactors())).append(',').append(' ');
    buf.append("paymentAmounts").append('=').append(JodaBeanUtils.toString(getPaymentAmounts())).append(',').append(' ');
    buf.append("projectedAmounts").append('=').append(JodaBeanUtils.toString(getProjectedAmounts())).append(',').append(' ');
    buf.append("indexTenors").append('=').append(JodaBeanUtils.toString(getIndexTenors())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingSwapLegDetails}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fixingStart} property.
     */
    private final MetaProperty<LocalDate[]> _fixingStart = DirectMetaProperty.ofReadWrite(
        this, "fixingStart", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code fixingEnd} property.
     */
    private final MetaProperty<LocalDate[]> _fixingEnd = DirectMetaProperty.ofReadWrite(
        this, "fixingEnd", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code fixingYearFractions} property.
     */
    private final MetaProperty<double[]> _fixingYearFractions = DirectMetaProperty.ofReadWrite(
        this, "fixingYearFractions", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code forwardRates} property.
     */
    private final MetaProperty<Double[]> _forwardRates = DirectMetaProperty.ofReadWrite(
        this, "forwardRates", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code fixedRates} property.
     */
    private final MetaProperty<Double[]> _fixedRates = DirectMetaProperty.ofReadWrite(
        this, "fixedRates", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code paymentDates} property.
     */
    private final MetaProperty<LocalDate[]> _paymentDates = DirectMetaProperty.ofReadWrite(
        this, "paymentDates", FloatingSwapLegDetails.class, LocalDate[].class);
    /**
     * The meta-property for the {@code paymentTimes} property.
     */
    private final MetaProperty<double[]> _paymentTimes = DirectMetaProperty.ofReadWrite(
        this, "paymentTimes", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code notionals} property.
     */
    private final MetaProperty<CurrencyAmount[]> _notionals = DirectMetaProperty.ofReadWrite(
        this, "notionals", FloatingSwapLegDetails.class, CurrencyAmount[].class);
    /**
     * The meta-property for the {@code spreads} property.
     */
    private final MetaProperty<double[]> _spreads = DirectMetaProperty.ofReadWrite(
        this, "spreads", FloatingSwapLegDetails.class, double[].class);
    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     */
    private final MetaProperty<Double[]> _paymentDiscountFactors = DirectMetaProperty.ofReadWrite(
        this, "paymentDiscountFactors", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code paymentAmounts} property.
     */
    private final MetaProperty<Double[]> _paymentAmounts = DirectMetaProperty.ofReadWrite(
        this, "paymentAmounts", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code projectedAmounts} property.
     */
    private final MetaProperty<Double[]> _projectedAmounts = DirectMetaProperty.ofReadWrite(
        this, "projectedAmounts", FloatingSwapLegDetails.class, Double[].class);
    /**
     * The meta-property for the {@code indexTenors} property.
     */
    private final MetaProperty<Tenor[]> _indexTenors = DirectMetaProperty.ofReadWrite(
        this, "indexTenors", FloatingSwapLegDetails.class, Tenor[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "fixingStart",
        "fixingEnd",
        "fixingYearFractions",
        "forwardRates",
        "fixedRates",
        "paymentDates",
        "paymentTimes",
        "notionals",
        "spreads",
        "paymentDiscountFactors",
        "paymentAmounts",
        "projectedAmounts",
        "indexTenors");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(final String propertyName) {
      switch (propertyName.hashCode()) {
        case 270958773:  // fixingStart
          return _fixingStart;
        case 871775726:  // fixingEnd
          return _fixingEnd;
        case 309118023:  // fixingYearFractions
          return _fixingYearFractions;
        case -291258418:  // forwardRates
          return _forwardRates;
        case 1695350911:  // fixedRates
          return _fixedRates;
        case -522438625:  // paymentDates
          return _paymentDates;
        case -507430688:  // paymentTimes
          return _paymentTimes;
        case 1910080819:  // notionals
          return _notionals;
        case -1996407456:  // spreads
          return _spreads;
        case -650014307:  // paymentDiscountFactors
          return _paymentDiscountFactors;
        case -1875448267:  // paymentAmounts
          return _paymentAmounts;
        case -176306557:  // projectedAmounts
          return _projectedAmounts;
        case 1358155045:  // indexTenors
          return _indexTenors;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FloatingSwapLegDetails> builder() {
      return new DirectBeanBuilder<FloatingSwapLegDetails>(new FloatingSwapLegDetails());
    }

    @Override
    public Class<? extends FloatingSwapLegDetails> beanType() {
      return FloatingSwapLegDetails.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fixingStart} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> fixingStart() {
      return _fixingStart;
    }

    /**
     * The meta-property for the {@code fixingEnd} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> fixingEnd() {
      return _fixingEnd;
    }

    /**
     * The meta-property for the {@code fixingYearFractions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> fixingYearFractions() {
      return _fixingYearFractions;
    }

    /**
     * The meta-property for the {@code forwardRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> forwardRates() {
      return _forwardRates;
    }

    /**
     * The meta-property for the {@code fixedRates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> fixedRates() {
      return _fixedRates;
    }

    /**
     * The meta-property for the {@code paymentDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> paymentDates() {
      return _paymentDates;
    }

    /**
     * The meta-property for the {@code paymentTimes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> paymentTimes() {
      return _paymentTimes;
    }

    /**
     * The meta-property for the {@code notionals} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurrencyAmount[]> notionals() {
      return _notionals;
    }

    /**
     * The meta-property for the {@code spreads} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> spreads() {
      return _spreads;
    }

    /**
     * The meta-property for the {@code paymentDiscountFactors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> paymentDiscountFactors() {
      return _paymentDiscountFactors;
    }

    /**
     * The meta-property for the {@code paymentAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> paymentAmounts() {
      return _paymentAmounts;
    }

    /**
     * The meta-property for the {@code projectedAmounts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> projectedAmounts() {
      return _projectedAmounts;
    }

    /**
     * The meta-property for the {@code indexTenors} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor[]> indexTenors() {
      return _indexTenors;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(final Bean bean, final String propertyName, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 270958773:  // fixingStart
          return ((FloatingSwapLegDetails) bean).getFixingStart();
        case 871775726:  // fixingEnd
          return ((FloatingSwapLegDetails) bean).getFixingEnd();
        case 309118023:  // fixingYearFractions
          return ((FloatingSwapLegDetails) bean).getFixingYearFractions();
        case -291258418:  // forwardRates
          return ((FloatingSwapLegDetails) bean).getForwardRates();
        case 1695350911:  // fixedRates
          return ((FloatingSwapLegDetails) bean).getFixedRates();
        case -522438625:  // paymentDates
          return ((FloatingSwapLegDetails) bean).getPaymentDates();
        case -507430688:  // paymentTimes
          return ((FloatingSwapLegDetails) bean).getPaymentTimes();
        case 1910080819:  // notionals
          return ((FloatingSwapLegDetails) bean).getNotionals();
        case -1996407456:  // spreads
          return ((FloatingSwapLegDetails) bean).getSpreads();
        case -650014307:  // paymentDiscountFactors
          return ((FloatingSwapLegDetails) bean).getPaymentDiscountFactors();
        case -1875448267:  // paymentAmounts
          return ((FloatingSwapLegDetails) bean).getPaymentAmounts();
        case -176306557:  // projectedAmounts
          return ((FloatingSwapLegDetails) bean).getProjectedAmounts();
        case 1358155045:  // indexTenors
          return ((FloatingSwapLegDetails) bean).getIndexTenors();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(final Bean bean, final String propertyName, final Object newValue, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 270958773:  // fixingStart
          ((FloatingSwapLegDetails) bean).setFixingStart((LocalDate[]) newValue);
          return;
        case 871775726:  // fixingEnd
          ((FloatingSwapLegDetails) bean).setFixingEnd((LocalDate[]) newValue);
          return;
        case 309118023:  // fixingYearFractions
          ((FloatingSwapLegDetails) bean).setFixingYearFractions((double[]) newValue);
          return;
        case -291258418:  // forwardRates
          ((FloatingSwapLegDetails) bean).setForwardRates((Double[]) newValue);
          return;
        case 1695350911:  // fixedRates
          ((FloatingSwapLegDetails) bean).setFixedRates((Double[]) newValue);
          return;
        case -522438625:  // paymentDates
          ((FloatingSwapLegDetails) bean).setPaymentDates((LocalDate[]) newValue);
          return;
        case -507430688:  // paymentTimes
          ((FloatingSwapLegDetails) bean).setPaymentTimes((double[]) newValue);
          return;
        case 1910080819:  // notionals
          ((FloatingSwapLegDetails) bean).setNotionals((CurrencyAmount[]) newValue);
          return;
        case -1996407456:  // spreads
          ((FloatingSwapLegDetails) bean).setSpreads((double[]) newValue);
          return;
        case -650014307:  // paymentDiscountFactors
          ((FloatingSwapLegDetails) bean).setPaymentDiscountFactors((Double[]) newValue);
          return;
        case -1875448267:  // paymentAmounts
          ((FloatingSwapLegDetails) bean).setPaymentAmounts((Double[]) newValue);
          return;
        case -176306557:  // projectedAmounts
          ((FloatingSwapLegDetails) bean).setProjectedAmounts((Double[]) newValue);
          return;
        case 1358155045:  // indexTenors
          ((FloatingSwapLegDetails) bean).setIndexTenors((Tenor[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(final Bean bean) {
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingStart, "fixingStart");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingEnd, "fixingEnd");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixingYearFractions, "fixingYearFractions");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._forwardRates, "forwardRates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._fixedRates, "fixedRates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentDates, "paymentDates");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentTimes, "paymentTimes");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._notionals, "notionals");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._spreads, "spreads");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentDiscountFactors, "paymentDiscountFactors");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._paymentAmounts, "paymentAmounts");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._projectedAmounts, "projectedAmounts");
      JodaBeanUtils.notNull(((FloatingSwapLegDetails) bean)._indexTenors, "indexTenors");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
