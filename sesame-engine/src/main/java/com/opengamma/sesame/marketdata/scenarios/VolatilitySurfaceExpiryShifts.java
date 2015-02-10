/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.HashMap;
import java.util.List;
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
import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.time.Tenor;

/**
 * Perturbation that applies a set of shifts to a volatility surface. The same shift is applied to all points
 * with equal expiry.
 * <p>
 * The shifts are defined as a map of expiry tenor to shift amount. This class traverses all points at a given
 * tenor and applies the same shift to them.
 */
@BeanDefinition
public class VolatilitySurfaceExpiryShifts implements Perturbation, ImmutableBean {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceExpiryShifts.class);

  /**
   * Whether the shift is absolute or relative. An absolute shift adds the shift amount to the rate. Relative shifts
   * are defined in terms of how much to increase or decrease the rate by. e.g. a 10% shift multiplies the rate
   * by 1.1, a -20% shift multiplies the rate by 0.8. So for relative shifts the shifted
   * rate is {@code (rate x (1 + shiftAmount))}.
   */
  @PropertyDefinition(validate = "notNull")
  private final ShiftType _shiftType;

  /** The shifts to apply to the surface values, keyed by the expiry tenor of the points they are applied to. */
  @PropertyDefinition(validate = "notNull")
  private final Map<Tenor, Double> _shifts;

  @Override
  public Object apply(Object marketData, MatchDetails matchDetails) {
    VolatilitySurface volSurface = (VolatilitySurface) marketData;
    Surface<Double, Double, Double> surface = volSurface.getSurface();

    if (!(surface instanceof InterpolatedDoublesSurface)) {
      s_logger.warn(
          "Unable to shift surface as it is not an InterpolatedDoublesSurface, type is {}",
          surface.getClass().getName());
      return marketData;
    }
    InterpolatedDoublesSurface dblSurface = (InterpolatedDoublesSurface) surface;
    double[] shifts = new double[dblSurface.size()];
    List<Tenor> expiryTenors = volSurface.getExpiryTenors();

    if (expiryTenors.isEmpty()) {
      s_logger.warn("Unable to shift surface '{}' as it contains no expiry information", surface.getName());
      return volSurface;
    }

    for (int i = 0; i < dblSurface.size(); i++) {
      Tenor tenor = expiryTenors.get(i);

      Double shiftSize = _shifts.get(tenor);

      if (shiftSize != null) {
        shifts[i] = shiftSize;
      } else {
        shifts[i] = 0;
      }
    }
    double[] expiries = dblSurface.getXDataAsPrimitive();
    double[] strikes = dblSurface.getYDataAsPrimitive();

    switch (_shiftType) {
      case RELATIVE:
        return volSurface.withMultipleMultiplicativeShifts(expiries, strikes, shifts);
      case ABSOLUTE:
        return volSurface.withMultipleAdditiveShifts(expiries, strikes, shifts);
      default:
        s_logger.warn("Unknown shift type {}", _shiftType);
        return volSurface;
    }
  }

  @Override
  public Class<?> getMarketDataType() {
    return VolatilitySurface.class;
  }

  @Override
  public Class<? extends MatchDetails> getMatchDetailsType() {
    return StandardMatchDetails.NoDetails.class;
  }

  @Override
  public PerturbationTarget getTargetType() {
    return PerturbationTarget.OUTPUT;
  }

  /**
   * Returns an instance that will add shifts to the surface points with the specified tenors.
   *
   * @param shifts  shift amounts that are added to the surface value at the corresponding tenor
   * @return an instance that will add shifts to the surface points with the specified tenors
   */
  public static VolatilitySurfaceExpiryShifts absolute(Map<Tenor, Double> shifts) {
    return VolatilitySurfaceExpiryShifts.builder().shiftType(ShiftType.ABSOLUTE).shifts(shifts).build();
  }

  /**
   * Returns an instance that will scale the surface points with the specified tenors by the shift amount.
   * <p>
   * Shifts are interpreted as a percentage increase or decrease. e.g. a shift of 0.1 indicates an increase
   * of 10%, so the surface value will be scaled by a factor of 1.1. Similarly, a shift of -0.2 indicates
   * a decrease of 20% and a scale factor of 0.8.
   * <p>
   * The shifted surface value is {@code (value x (1 + shift))}.
   *
   * @param shifts  shift amounts that are added to the surface value at the corresponding tenor
   * @return an instance that will add shifts to the surface points with the specified tenors
   */
  public static VolatilitySurfaceExpiryShifts relative(Map<Tenor, Double> shifts) {
    return VolatilitySurfaceExpiryShifts.builder().shiftType(ShiftType.RELATIVE).shifts(shifts).build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilitySurfaceExpiryShifts}.
   * @return the meta-bean, not null
   */
  public static VolatilitySurfaceExpiryShifts.Meta meta() {
    return VolatilitySurfaceExpiryShifts.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilitySurfaceExpiryShifts.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static VolatilitySurfaceExpiryShifts.Builder builder() {
    return new VolatilitySurfaceExpiryShifts.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected VolatilitySurfaceExpiryShifts(VolatilitySurfaceExpiryShifts.Builder builder) {
    JodaBeanUtils.notNull(builder._shiftType, "shiftType");
    JodaBeanUtils.notNull(builder._shifts, "shifts");
    this._shiftType = builder._shiftType;
    this._shifts = ImmutableMap.copyOf(builder._shifts);
  }

  @Override
  public VolatilitySurfaceExpiryShifts.Meta metaBean() {
    return VolatilitySurfaceExpiryShifts.Meta.INSTANCE;
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
  public ShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the shifts to apply to the surface values, keyed by the expiry tenor of the points they are applied to.
   * @return the value of the property, not null
   */
  public Map<Tenor, Double> getShifts() {
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
      VolatilitySurfaceExpiryShifts other = (VolatilitySurfaceExpiryShifts) obj;
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
    buf.append("VolatilitySurfaceExpiryShifts{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("shiftType").append('=').append(JodaBeanUtils.toString(getShiftType())).append(',').append(' ');
    buf.append("shifts").append('=').append(JodaBeanUtils.toString(getShifts())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilitySurfaceExpiryShifts}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftType} property.
     */
    private final MetaProperty<ShiftType> _shiftType = DirectMetaProperty.ofImmutable(
        this, "shiftType", VolatilitySurfaceExpiryShifts.class, ShiftType.class);
    /**
     * The meta-property for the {@code shifts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Tenor, Double>> _shifts = DirectMetaProperty.ofImmutable(
        this, "shifts", VolatilitySurfaceExpiryShifts.class, (Class) Map.class);
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
    protected Meta() {
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
    public VolatilitySurfaceExpiryShifts.Builder builder() {
      return new VolatilitySurfaceExpiryShifts.Builder();
    }

    @Override
    public Class<? extends VolatilitySurfaceExpiryShifts> beanType() {
      return VolatilitySurfaceExpiryShifts.class;
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
    public final MetaProperty<ShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shifts} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Tenor, Double>> shifts() {
      return _shifts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((VolatilitySurfaceExpiryShifts) bean).getShiftType();
        case -903338959:  // shifts
          return ((VolatilitySurfaceExpiryShifts) bean).getShifts();
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
   * The bean-builder for {@code VolatilitySurfaceExpiryShifts}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<VolatilitySurfaceExpiryShifts> {

    private ShiftType _shiftType;
    private Map<Tenor, Double> _shifts = new HashMap<Tenor, Double>();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(VolatilitySurfaceExpiryShifts beanToCopy) {
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
    public VolatilitySurfaceExpiryShifts build() {
      return new VolatilitySurfaceExpiryShifts(this);
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
     * @param shifts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(Map<Tenor, Double> shifts) {
      JodaBeanUtils.notNull(shifts, "shifts");
      this._shifts = shifts;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("VolatilitySurfaceExpiryShifts.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("shiftType").append('=').append(JodaBeanUtils.toString(_shiftType)).append(',').append(' ');
      buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
