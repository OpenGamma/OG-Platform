/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.ShiftType;
import com.opengamma.financial.analytics.isda.credit.CdsQuote;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.util.time.Tenor;

/**
 * Applies a point shift to the spreads used to build the credit curves.
 */
@BeanDefinition
public final class CreditCurvePointShift implements Perturbation, ImmutableBean {

  /**
   * Whether the shift is absolute or relative. An absolute shift adds the shift amount to the rate. Relative shifts
   * are defined in terms of how much to increase or decrease the rate by. e.g. a 10% shift multiplies the rate
   * by 1.1, a -20% shift multiplies the rate by 0.8. So for relative shifts the shifted
   * rate is {@code (rate x (1 + shiftAmount))}.
   */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final ShiftType _shiftType;

  /** The shifts to apply to the curve values */
  @PropertyDefinition(validate = "notEmpty", get = "private")
  private final Map<Tenor, Double> _shifts;

  /**
   * Creates a shift that adds a fixed amount to every market data value.
   *
   * @param shifts the amount to add to each market data value per Tenor
   * @return a shift that adds a fixed amount to each market data value
   */
  public static CreditCurvePointShift absolute(Map<Tenor, Double> shifts) {
    return new CreditCurvePointShift(ShiftType.ABSOLUTE, shifts);
  }

  /**
   * Creates a shift that multiplies every market data value by a fixed factor.
   *
   * @param shifts the factor to multiply the values by, per Tenor
   * @return a shift that multiplies the market data values by a fixed factor
   */
  public static CreditCurvePointShift relative(Map<Tenor, Double> shifts) {
    return new CreditCurvePointShift(ShiftType.RELATIVE, shifts);
  }

  @Override
  public CreditCurveData apply(Object marketData, MatchDetails matchDetails) {

    CreditCurveData input = (CreditCurveData) marketData;
    CreditCurveData.Builder curveBuilder = CreditCurveData.builder();
    ImmutableSortedMap.Builder<Tenor, CdsQuote> quotesBuilder = ImmutableSortedMap.naturalOrder();
    ImmutableSortedMap<Tenor, CdsQuote> cdsQuotes = input.getCdsQuotes();

    // all shifts should be available in the input

    if (!cdsQuotes.keySet().containsAll(_shifts.keySet())) {
      throw new OpenGammaRuntimeException("Input tenors " + cdsQuotes.keySet() + " do not contain all " +
                                           _shifts.keySet() + " scenario shift tenors");
    }
    for (Map.Entry<Tenor, CdsQuote> entry : cdsQuotes.entrySet()) {
      if (_shifts.containsKey(entry.getKey())) {
        quotesBuilder.put(entry.getKey(), shift(entry.getValue(), _shifts.get(entry.getKey())));
      } else {
        quotesBuilder.put(entry.getKey(), entry.getValue());
      }
    }

    curveBuilder.cdsQuotes(quotesBuilder.build())
        .curveConventionLink(input.getCurveConventionLink())
        .recoveryRate(input.getRecoveryRate());

    return curveBuilder.build();
  }

  @Override
  public Class<?> getMarketDataType() {
    return CreditCurveData.class;
  }

  @Override
  public Class<? extends MatchDetails> getMatchDetailsType() {
    return StandardMatchDetails.NoDetails.class;
  }

  @Override
  public PerturbationTarget getTargetType() {
    return PerturbationTarget.INPUT;
  }

  private CdsQuote shift(CdsQuote quote, Double shift) {
    if (quote instanceof ParSpreadQuote) {
      ParSpreadQuote parSpreadQuote = (ParSpreadQuote) quote;
      double applyShift = _shiftType.applyShift(parSpreadQuote.getParSpread(), shift);
      return ParSpreadQuote.from(applyShift);
    } else {
      // TODO extend to include FlatQuoteSpread and PointsUpFrontQuote
      throw new OpenGammaRuntimeException("Only ParSpreadQuote is supported. Unsupported quote type: " + quote.getClass());
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CreditCurvePointShift}.
   * @return the meta-bean, not null
   */
  public static CreditCurvePointShift.Meta meta() {
    return CreditCurvePointShift.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CreditCurvePointShift.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CreditCurvePointShift.Builder builder() {
    return new CreditCurvePointShift.Builder();
  }

  private CreditCurvePointShift(
      ShiftType shiftType,
      Map<Tenor, Double> shifts) {
    JodaBeanUtils.notNull(shiftType, "shiftType");
    JodaBeanUtils.notEmpty(shifts, "shifts");
    this._shiftType = shiftType;
    this._shifts = ImmutableMap.copyOf(shifts);
  }

  @Override
  public CreditCurvePointShift.Meta metaBean() {
    return CreditCurvePointShift.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether the shift is absolute or relative. An absolute shift adds the shift amount to the rate. Relative shifts
   * are defined in terms of how much to increase or decrease the rate by. e.g. a 10% shift multiplies the rate
   * by 1.1, a -20% shift multiplies the rate by 0.8. So for relative shifts the shifted
   * rate is {@code (rate x (1 + shiftAmount))}.
   * @return the value of the property, not null
   */
  private ShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the shifts to apply to the curve values
   * @return the value of the property, not empty
   */
  private Map<Tenor, Double> getShifts() {
    return _shifts;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CreditCurvePointShift other = (CreditCurvePointShift) obj;
      return JodaBeanUtils.equal(getShiftType(), other.getShiftType()) &&
          JodaBeanUtils.equal(getShifts(), other.getShifts());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getShiftType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getShifts());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CreditCurvePointShift{");
    buf.append("shiftType").append('=').append(getShiftType()).append(',').append(' ');
    buf.append("shifts").append('=').append(JodaBeanUtils.toString(getShifts()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CreditCurvePointShift}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftType} property.
     */
    private final MetaProperty<ShiftType> _shiftType = DirectMetaProperty.ofImmutable(
        this, "shiftType", CreditCurvePointShift.class, ShiftType.class);
    /**
     * The meta-property for the {@code shifts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Tenor, Double>> _shifts = DirectMetaProperty.ofImmutable(
        this, "shifts", CreditCurvePointShift.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "shiftType",
        "shifts");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return _shiftType;
        case -903338959:  // shifts
          return _shifts;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CreditCurvePointShift.Builder builder() {
      return new CreditCurvePointShift.Builder();
    }

    @Override
    public Class<? extends CreditCurvePointShift> beanType() {
      return CreditCurvePointShift.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code shiftType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shifts} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<Tenor, Double>> shifts() {
      return _shifts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((CreditCurvePointShift) bean).getShiftType();
        case -903338959:  // shifts
          return ((CreditCurvePointShift) bean).getShifts();
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
   * The bean-builder for {@code CreditCurvePointShift}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CreditCurvePointShift> {

    private ShiftType _shiftType;
    private Map<Tenor, Double> _shifts = new HashMap<Tenor, Double>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CreditCurvePointShift beanToCopy) {
      this._shiftType = beanToCopy.getShiftType();
      this._shifts = new HashMap<Tenor, Double>(beanToCopy.getShifts());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return _shiftType;
        case -903338959:  // shifts
          return _shifts;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          this._shiftType = (ShiftType) newValue;
          break;
        case -903338959:  // shifts
          this._shifts = (Map<Tenor, Double>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
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
    public CreditCurvePointShift build() {
      return new CreditCurvePointShift(
          _shiftType,
          _shifts);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code shiftType} property in the builder.
     * @param shiftType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftType(ShiftType shiftType) {
      JodaBeanUtils.notNull(shiftType, "shiftType");
      this._shiftType = shiftType;
      return this;
    }

    /**
     * Sets the {@code shifts} property in the builder.
     * @param shifts  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder shifts(Map<Tenor, Double> shifts) {
      JodaBeanUtils.notEmpty(shifts, "shifts");
      this._shifts = shifts;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("CreditCurvePointShift.Builder{");
      buf.append("shiftType").append('=').append(JodaBeanUtils.toString(_shiftType)).append(',').append(' ');
      buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
