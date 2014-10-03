/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.CouponFixedRateVisitor;
import com.opengamma.analytics.financial.interestrate.CouponFixingDatesVisitor;
import com.opengamma.analytics.financial.interestrate.CouponFixingYearFractionVisitor;
import com.opengamma.analytics.financial.interestrate.CouponGearingVisitor;
import com.opengamma.analytics.financial.interestrate.CouponSpreadVisitor;
import com.opengamma.analytics.financial.interestrate.CouponTenorVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.provider.CouponForwardRateVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * A cash flow that pays a variable amount on a settlement date.
 */
@BeanDefinition(hierarchy = "immutable")
public final class FloatingCashFlowDetails extends AbstractCashFlowDetails {
  
  /**
   * The visitor used to calculate the fixing period dates of the cash flow.
   */
  private static final CouponFixingDatesVisitor FIXING_DATES_VISITOR = new CouponFixingDatesVisitor();
  
  /**
   * The visitor used to calculate the fixing year fraction of the cash flow.
   */
  private static final CouponFixingYearFractionVisitor FIXING_YEAR_FRACTION_VISITOR =
      new CouponFixingYearFractionVisitor();
  
  /**
   * The visitor used to calculate the fixed, or reset rate, of the cash flow.
   */
  private static final CouponFixedRateVisitor FIXED_RATE_VISITOR = new CouponFixedRateVisitor();
  
  /**
   * The visitor used to calculate the forward rate of the cash flow.
   */
  private static final CouponForwardRateVisitor FORWARD_RATE_VISITOR = new CouponForwardRateVisitor();
  
  /**
   * The visitor used to retrieve the spread on the cash flow.
   */
  private static final InstrumentDefinitionVisitor<Void, Double> SPREAD_VISITOR = CouponSpreadVisitor.getInstance();
  
  /**
   * The visitor used to retrieve the gearing on the cash flow.
   */
  private static final InstrumentDefinitionVisitor<Void, Double> GEARING_VISITOR = CouponGearingVisitor.getInstance();
  
  /**
   * The visitor used to retrieve the index tenors of the cash flow.
   */
  private static final InstrumentDefinitionVisitor<Void, Set<Tenor>> INDEX_TENOR_VISITOR =
      CouponTenorVisitor.getInstance();
  
  /**
   * The visitor used to retrieve the present values of the cash flows.
   */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> PV_VISITOR = 
      PresentValueDiscountingCalculator.getInstance();
  
  /**
   * The fixing start date of the cash flow.
   */
  @PropertyDefinition
  private final LocalDate _fixingStartDate;
  
  /**
   * The fixing end date of the cash flow.
   */
  @PropertyDefinition
  private final LocalDate _fixingEndDate;

  /**
   * The fixing year fraction of the cash flow.
   */
  @PropertyDefinition
  private final double _fixingYearFrac;

  /**
   * The fixed, or reset, rate of the cash flow.
   */
  @PropertyDefinition
  private final Double _fixedRate;

  /**
   * The forward rate of the cash flow.
   */
  @PropertyDefinition
  private final Double _forwardRate;
  
  /**
   * The spread of the cash flow.
   */
  @PropertyDefinition
  private final Double _spread;
  
  /**
   * The gearing of the cash flow.
   */
  @PropertyDefinition
  private final Double _gearing;
  
  /**
   * The index tenors of the cash flow.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<Tenor> _indexTenors;

  /**
   * The projected amount of the cash flow.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyAmount _projectedAmount;

  /**
   * The present value of the cash flow.
   */
  @PropertyDefinition(validate = "notNull")
  private final CurrencyAmount _presentValue;
  
  /**
   * Constructor that uses the definition and derivative versions of a payment to construct a description of a fixed cash
   * flow.
   * @param definition the definition representation of a cash flow.
   * @param derivative the derivative representation of a cash flow.
   * @param curves the curve bundle used to retrieve discount factors.
   */
  public FloatingCashFlowDetails(PaymentDefinition definition, Payment derivative, MulticurveProviderInterface curves) {
    super(definition, derivative, curves);
    Pair<LocalDate, LocalDate> fixingDates = definition.accept(FIXING_DATES_VISITOR);
    _fixingStartDate = fixingDates.getFirst();
    _fixingEndDate = fixingDates.getSecond();
    _fixingYearFrac = definition.accept(FIXING_YEAR_FRACTION_VISITOR);
    double fixedRate = Double.NaN;
    try {
      fixedRate = derivative.accept(FIXED_RATE_VISITOR);
    } catch (UnsupportedOperationException e) {
      // Expected if floating coupon has not fixed
    }
    _fixedRate = fixedRate;
    double forwardRate = Double.NaN;
    try {
      forwardRate = derivative.accept(FORWARD_RATE_VISITOR, curves);
    } catch (UnsupportedOperationException e) {
      // May happen if compounding
    }
    _forwardRate = forwardRate;
    _spread = definition.accept(SPREAD_VISITOR);
    _gearing = definition.accept(GEARING_VISITOR);
    _indexTenors = definition.accept(INDEX_TENOR_VISITOR);
    _presentValue = derivative.accept(PV_VISITOR, curves).getCurrencyAmount(derivative.getCurrency());
    _projectedAmount = _presentValue.multipliedBy(1 / getDf());
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FloatingCashFlowDetails}.
   * @return the meta-bean, not null
   */
  public static FloatingCashFlowDetails.Meta meta() {
    return FloatingCashFlowDetails.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FloatingCashFlowDetails.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FloatingCashFlowDetails.Builder builder() {
    return new FloatingCashFlowDetails.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  private FloatingCashFlowDetails(FloatingCashFlowDetails.Builder builder) {
    super(builder);
    JodaBeanUtils.notNull(builder._indexTenors, "indexTenors");
    JodaBeanUtils.notNull(builder._projectedAmount, "projectedAmount");
    JodaBeanUtils.notNull(builder._presentValue, "presentValue");
    this._fixingStartDate = builder._fixingStartDate;
    this._fixingEndDate = builder._fixingEndDate;
    this._fixingYearFrac = builder._fixingYearFrac;
    this._fixedRate = builder._fixedRate;
    this._forwardRate = builder._forwardRate;
    this._spread = builder._spread;
    this._gearing = builder._gearing;
    this._indexTenors = ImmutableSet.copyOf(builder._indexTenors);
    this._projectedAmount = builder._projectedAmount;
    this._presentValue = builder._presentValue;
  }

  @Override
  public FloatingCashFlowDetails.Meta metaBean() {
    return FloatingCashFlowDetails.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing start date of the cash flow.
   * @return the value of the property
   */
  public LocalDate getFixingStartDate() {
    return _fixingStartDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing end date of the cash flow.
   * @return the value of the property
   */
  public LocalDate getFixingEndDate() {
    return _fixingEndDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing year fraction of the cash flow.
   * @return the value of the property
   */
  public double getFixingYearFrac() {
    return _fixingYearFrac;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixed, or reset, rate of the cash flow.
   * @return the value of the property
   */
  public Double getFixedRate() {
    return _fixedRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the forward rate of the cash flow.
   * @return the value of the property
   */
  public Double getForwardRate() {
    return _forwardRate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the spread of the cash flow.
   * @return the value of the property
   */
  public Double getSpread() {
    return _spread;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the gearing of the cash flow.
   * @return the value of the property
   */
  public Double getGearing() {
    return _gearing;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the index tenors of the cash flow.
   * @return the value of the property, not null
   */
  public Set<Tenor> getIndexTenors() {
    return _indexTenors;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the projected amount of the cash flow.
   * @return the value of the property, not null
   */
  public CurrencyAmount getProjectedAmount() {
    return _projectedAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the present value of the cash flow.
   * @return the value of the property, not null
   */
  public CurrencyAmount getPresentValue() {
    return _presentValue;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FloatingCashFlowDetails other = (FloatingCashFlowDetails) obj;
      return JodaBeanUtils.equal(getFixingStartDate(), other.getFixingStartDate()) &&
          JodaBeanUtils.equal(getFixingEndDate(), other.getFixingEndDate()) &&
          JodaBeanUtils.equal(getFixingYearFrac(), other.getFixingYearFrac()) &&
          JodaBeanUtils.equal(getFixedRate(), other.getFixedRate()) &&
          JodaBeanUtils.equal(getForwardRate(), other.getForwardRate()) &&
          JodaBeanUtils.equal(getSpread(), other.getSpread()) &&
          JodaBeanUtils.equal(getGearing(), other.getGearing()) &&
          JodaBeanUtils.equal(getIndexTenors(), other.getIndexTenors()) &&
          JodaBeanUtils.equal(getProjectedAmount(), other.getProjectedAmount()) &&
          JodaBeanUtils.equal(getPresentValue(), other.getPresentValue()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingStartDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingEndDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingYearFrac());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixedRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getForwardRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpread());
    hash += hash * 31 + JodaBeanUtils.hashCode(getGearing());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIndexTenors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProjectedAmount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPresentValue());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("FloatingCashFlowDetails{");
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
    buf.append("fixingStartDate").append('=').append(JodaBeanUtils.toString(getFixingStartDate())).append(',').append(' ');
    buf.append("fixingEndDate").append('=').append(JodaBeanUtils.toString(getFixingEndDate())).append(',').append(' ');
    buf.append("fixingYearFrac").append('=').append(JodaBeanUtils.toString(getFixingYearFrac())).append(',').append(' ');
    buf.append("fixedRate").append('=').append(JodaBeanUtils.toString(getFixedRate())).append(',').append(' ');
    buf.append("forwardRate").append('=').append(JodaBeanUtils.toString(getForwardRate())).append(',').append(' ');
    buf.append("spread").append('=').append(JodaBeanUtils.toString(getSpread())).append(',').append(' ');
    buf.append("gearing").append('=').append(JodaBeanUtils.toString(getGearing())).append(',').append(' ');
    buf.append("indexTenors").append('=').append(JodaBeanUtils.toString(getIndexTenors())).append(',').append(' ');
    buf.append("projectedAmount").append('=').append(JodaBeanUtils.toString(getProjectedAmount())).append(',').append(' ');
    buf.append("presentValue").append('=').append(JodaBeanUtils.toString(getPresentValue())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FloatingCashFlowDetails}.
   */
  public static final class Meta extends AbstractCashFlowDetails.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fixingStartDate} property.
     */
    private final MetaProperty<LocalDate> _fixingStartDate = DirectMetaProperty.ofImmutable(
        this, "fixingStartDate", FloatingCashFlowDetails.class, LocalDate.class);
    /**
     * The meta-property for the {@code fixingEndDate} property.
     */
    private final MetaProperty<LocalDate> _fixingEndDate = DirectMetaProperty.ofImmutable(
        this, "fixingEndDate", FloatingCashFlowDetails.class, LocalDate.class);
    /**
     * The meta-property for the {@code fixingYearFrac} property.
     */
    private final MetaProperty<Double> _fixingYearFrac = DirectMetaProperty.ofImmutable(
        this, "fixingYearFrac", FloatingCashFlowDetails.class, Double.TYPE);
    /**
     * The meta-property for the {@code fixedRate} property.
     */
    private final MetaProperty<Double> _fixedRate = DirectMetaProperty.ofImmutable(
        this, "fixedRate", FloatingCashFlowDetails.class, Double.class);
    /**
     * The meta-property for the {@code forwardRate} property.
     */
    private final MetaProperty<Double> _forwardRate = DirectMetaProperty.ofImmutable(
        this, "forwardRate", FloatingCashFlowDetails.class, Double.class);
    /**
     * The meta-property for the {@code spread} property.
     */
    private final MetaProperty<Double> _spread = DirectMetaProperty.ofImmutable(
        this, "spread", FloatingCashFlowDetails.class, Double.class);
    /**
     * The meta-property for the {@code gearing} property.
     */
    private final MetaProperty<Double> _gearing = DirectMetaProperty.ofImmutable(
        this, "gearing", FloatingCashFlowDetails.class, Double.class);
    /**
     * The meta-property for the {@code indexTenors} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Tenor>> _indexTenors = DirectMetaProperty.ofImmutable(
        this, "indexTenors", FloatingCashFlowDetails.class, (Class) Set.class);
    /**
     * The meta-property for the {@code projectedAmount} property.
     */
    private final MetaProperty<CurrencyAmount> _projectedAmount = DirectMetaProperty.ofImmutable(
        this, "projectedAmount", FloatingCashFlowDetails.class, CurrencyAmount.class);
    /**
     * The meta-property for the {@code presentValue} property.
     */
    private final MetaProperty<CurrencyAmount> _presentValue = DirectMetaProperty.ofImmutable(
        this, "presentValue", FloatingCashFlowDetails.class, CurrencyAmount.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "fixingStartDate",
        "fixingEndDate",
        "fixingYearFrac",
        "fixedRate",
        "forwardRate",
        "spread",
        "gearing",
        "indexTenors",
        "projectedAmount",
        "presentValue");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1560444413:  // fixingStartDate
          return _fixingStartDate;
        case -1312163140:  // fixingEndDate
          return _fixingEndDate;
        case -967693672:  // fixingYearFrac
          return _fixingYearFrac;
        case 747425396:  // fixedRate
          return _fixedRate;
        case 1653172549:  // forwardRate
          return _forwardRate;
        case -895684237:  // spread
          return _spread;
        case -91774989:  // gearing
          return _gearing;
        case 1358155045:  // indexTenors
          return _indexTenors;
        case -5687312:  // projectedAmount
          return _projectedAmount;
        case 686253430:  // presentValue
          return _presentValue;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FloatingCashFlowDetails.Builder builder() {
      return new FloatingCashFlowDetails.Builder();
    }

    @Override
    public Class<? extends FloatingCashFlowDetails> beanType() {
      return FloatingCashFlowDetails.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fixingStartDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> fixingStartDate() {
      return _fixingStartDate;
    }

    /**
     * The meta-property for the {@code fixingEndDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> fixingEndDate() {
      return _fixingEndDate;
    }

    /**
     * The meta-property for the {@code fixingYearFrac} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> fixingYearFrac() {
      return _fixingYearFrac;
    }

    /**
     * The meta-property for the {@code fixedRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> fixedRate() {
      return _fixedRate;
    }

    /**
     * The meta-property for the {@code forwardRate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> forwardRate() {
      return _forwardRate;
    }

    /**
     * The meta-property for the {@code spread} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> spread() {
      return _spread;
    }

    /**
     * The meta-property for the {@code gearing} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> gearing() {
      return _gearing;
    }

    /**
     * The meta-property for the {@code indexTenors} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Set<Tenor>> indexTenors() {
      return _indexTenors;
    }

    /**
     * The meta-property for the {@code projectedAmount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> projectedAmount() {
      return _projectedAmount;
    }

    /**
     * The meta-property for the {@code presentValue} property.
     * @return the meta-property, not null
     */
    public MetaProperty<CurrencyAmount> presentValue() {
      return _presentValue;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1560444413:  // fixingStartDate
          return ((FloatingCashFlowDetails) bean).getFixingStartDate();
        case -1312163140:  // fixingEndDate
          return ((FloatingCashFlowDetails) bean).getFixingEndDate();
        case -967693672:  // fixingYearFrac
          return ((FloatingCashFlowDetails) bean).getFixingYearFrac();
        case 747425396:  // fixedRate
          return ((FloatingCashFlowDetails) bean).getFixedRate();
        case 1653172549:  // forwardRate
          return ((FloatingCashFlowDetails) bean).getForwardRate();
        case -895684237:  // spread
          return ((FloatingCashFlowDetails) bean).getSpread();
        case -91774989:  // gearing
          return ((FloatingCashFlowDetails) bean).getGearing();
        case 1358155045:  // indexTenors
          return ((FloatingCashFlowDetails) bean).getIndexTenors();
        case -5687312:  // projectedAmount
          return ((FloatingCashFlowDetails) bean).getProjectedAmount();
        case 686253430:  // presentValue
          return ((FloatingCashFlowDetails) bean).getPresentValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FloatingCashFlowDetails}.
   */
  public static final class Builder extends AbstractCashFlowDetails.Builder {

    private LocalDate _fixingStartDate;
    private LocalDate _fixingEndDate;
    private double _fixingYearFrac;
    private Double _fixedRate;
    private Double _forwardRate;
    private Double _spread;
    private Double _gearing;
    private Set<Tenor> _indexTenors = new HashSet<Tenor>();
    private CurrencyAmount _projectedAmount;
    private CurrencyAmount _presentValue;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FloatingCashFlowDetails beanToCopy) {
      this._fixingStartDate = beanToCopy.getFixingStartDate();
      this._fixingEndDate = beanToCopy.getFixingEndDate();
      this._fixingYearFrac = beanToCopy.getFixingYearFrac();
      this._fixedRate = beanToCopy.getFixedRate();
      this._forwardRate = beanToCopy.getForwardRate();
      this._spread = beanToCopy.getSpread();
      this._gearing = beanToCopy.getGearing();
      this._indexTenors = new HashSet<Tenor>(beanToCopy.getIndexTenors());
      this._projectedAmount = beanToCopy.getProjectedAmount();
      this._presentValue = beanToCopy.getPresentValue();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1560444413:  // fixingStartDate
          return _fixingStartDate;
        case -1312163140:  // fixingEndDate
          return _fixingEndDate;
        case -967693672:  // fixingYearFrac
          return _fixingYearFrac;
        case 747425396:  // fixedRate
          return _fixedRate;
        case 1653172549:  // forwardRate
          return _forwardRate;
        case -895684237:  // spread
          return _spread;
        case -91774989:  // gearing
          return _gearing;
        case 1358155045:  // indexTenors
          return _indexTenors;
        case -5687312:  // projectedAmount
          return _projectedAmount;
        case 686253430:  // presentValue
          return _presentValue;
        default:
          return super.get(propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1560444413:  // fixingStartDate
          this._fixingStartDate = (LocalDate) newValue;
          break;
        case -1312163140:  // fixingEndDate
          this._fixingEndDate = (LocalDate) newValue;
          break;
        case -967693672:  // fixingYearFrac
          this._fixingYearFrac = (Double) newValue;
          break;
        case 747425396:  // fixedRate
          this._fixedRate = (Double) newValue;
          break;
        case 1653172549:  // forwardRate
          this._forwardRate = (Double) newValue;
          break;
        case -895684237:  // spread
          this._spread = (Double) newValue;
          break;
        case -91774989:  // gearing
          this._gearing = (Double) newValue;
          break;
        case 1358155045:  // indexTenors
          this._indexTenors = (Set<Tenor>) newValue;
          break;
        case -5687312:  // projectedAmount
          this._projectedAmount = (CurrencyAmount) newValue;
          break;
        case 686253430:  // presentValue
          this._presentValue = (CurrencyAmount) newValue;
          break;
        default:
          super.set(propertyName, newValue);
          break;
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FloatingCashFlowDetails build() {
      return new FloatingCashFlowDetails(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code fixingStartDate} property in the builder.
     * @param fixingStartDate  the new value
     * @return this, for chaining, not null
     */
    public Builder fixingStartDate(LocalDate fixingStartDate) {
      this._fixingStartDate = fixingStartDate;
      return this;
    }

    /**
     * Sets the {@code fixingEndDate} property in the builder.
     * @param fixingEndDate  the new value
     * @return this, for chaining, not null
     */
    public Builder fixingEndDate(LocalDate fixingEndDate) {
      this._fixingEndDate = fixingEndDate;
      return this;
    }

    /**
     * Sets the {@code fixingYearFrac} property in the builder.
     * @param fixingYearFrac  the new value
     * @return this, for chaining, not null
     */
    public Builder fixingYearFrac(double fixingYearFrac) {
      this._fixingYearFrac = fixingYearFrac;
      return this;
    }

    /**
     * Sets the {@code fixedRate} property in the builder.
     * @param fixedRate  the new value
     * @return this, for chaining, not null
     */
    public Builder fixedRate(Double fixedRate) {
      this._fixedRate = fixedRate;
      return this;
    }

    /**
     * Sets the {@code forwardRate} property in the builder.
     * @param forwardRate  the new value
     * @return this, for chaining, not null
     */
    public Builder forwardRate(Double forwardRate) {
      this._forwardRate = forwardRate;
      return this;
    }

    /**
     * Sets the {@code spread} property in the builder.
     * @param spread  the new value
     * @return this, for chaining, not null
     */
    public Builder spread(Double spread) {
      this._spread = spread;
      return this;
    }

    /**
     * Sets the {@code gearing} property in the builder.
     * @param gearing  the new value
     * @return this, for chaining, not null
     */
    public Builder gearing(Double gearing) {
      this._gearing = gearing;
      return this;
    }

    /**
     * Sets the {@code indexTenors} property in the builder.
     * @param indexTenors  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder indexTenors(Set<Tenor> indexTenors) {
      JodaBeanUtils.notNull(indexTenors, "indexTenors");
      this._indexTenors = indexTenors;
      return this;
    }

    /**
     * Sets the {@code projectedAmount} property in the builder.
     * @param projectedAmount  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder projectedAmount(CurrencyAmount projectedAmount) {
      JodaBeanUtils.notNull(projectedAmount, "projectedAmount");
      this._projectedAmount = projectedAmount;
      return this;
    }

    /**
     * Sets the {@code presentValue} property in the builder.
     * @param presentValue  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder presentValue(CurrencyAmount presentValue) {
      JodaBeanUtils.notNull(presentValue, "presentValue");
      this._presentValue = presentValue;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(352);
      buf.append("FloatingCashFlowDetails.Builder{");
      buf.append("fixingStartDate").append('=').append(JodaBeanUtils.toString(_fixingStartDate)).append(',').append(' ');
      buf.append("fixingEndDate").append('=').append(JodaBeanUtils.toString(_fixingEndDate)).append(',').append(' ');
      buf.append("fixingYearFrac").append('=').append(JodaBeanUtils.toString(_fixingYearFrac)).append(',').append(' ');
      buf.append("fixedRate").append('=').append(JodaBeanUtils.toString(_fixedRate)).append(',').append(' ');
      buf.append("forwardRate").append('=').append(JodaBeanUtils.toString(_forwardRate)).append(',').append(' ');
      buf.append("spread").append('=').append(JodaBeanUtils.toString(_spread)).append(',').append(' ');
      buf.append("gearing").append('=').append(JodaBeanUtils.toString(_gearing)).append(',').append(' ');
      buf.append("indexTenors").append('=').append(JodaBeanUtils.toString(_indexTenors)).append(',').append(' ');
      buf.append("projectedAmount").append('=').append(JodaBeanUtils.toString(_projectedAmount)).append(',').append(' ');
      buf.append("presentValue").append('=').append(JodaBeanUtils.toString(_presentValue));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
