/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

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
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Applies shifts (relative or absolute) to points in a volatility surface.
 */
@BeanDefinition
public class VolatilitySurfaceShiftManipulator implements StructureManipulator<VolatilitySurface>, ImmutableBean {

  /** The type of shift to apply to the surface points. */
  @PropertyDefinition(validate = "notNull")
  private final ScenarioShiftType _shiftType;
  
  /** The shift values to apply to each point. */
  @PropertyDefinition(validate = "notNull")
  private final double[] _shiftValues;

  /* package */ VolatilitySurfaceShiftManipulator(ScenarioShiftType shiftType, double[] shiftValues) {
    _shiftValues = ArgumentChecker.notEmpty(shiftValues, "shiftValues");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface,
                                   ValueSpecification valueSpecification,
                                   FunctionExecutionContext executionContext) {
    ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    double[] xValues = getXValues(valuationTime);
    double[] yValues = getYValues(valuationTime);
    if (_shiftValues.length > 1) {
      if (_shiftType == ScenarioShiftType.ABSOLUTE) {
        return surface.withMultipleAdditiveShifts(xValues, yValues, _shiftValues);
      } else {
        return surface.withMultipleMultiplicativeShifts(xValues, yValues, _shiftValues);
      }
    } else {
      if (_shiftType == ScenarioShiftType.ABSOLUTE) {
        return surface.withSingleAdditiveShift(xValues[0], yValues[0], _shiftValues[0]);
      } else {
        return surface.withSingleMultiplicativeShift(xValues[0], yValues[0], _shiftValues[0]);
      }
    }
  }

  @Override
  public Class<VolatilitySurface> getExpectedType() {
    return VolatilitySurface.class;
  }

  protected double[] getXValues(ZonedDateTime valuationTime) {
    throw new UnsupportedOperationException("This class should be abstract but there's a bug in Joda beans");
  }

  protected double[] getYValues(ZonedDateTime valuationTime) {
    throw new UnsupportedOperationException("This class should be abstract but there's a bug in Joda beans");
  }

  @SuppressWarnings("unchecked")
  public static VolatilitySurfaceShiftManipulator create(ScenarioShiftType shiftType,
                                                         List<VolatilitySurfaceShift> shifts) {
    Object xValues = xValues(shifts);
    Object yValues = yValues(shifts);
    double[] shiftValues = shiftValues(shifts, shiftType);

    if (xValues instanceof List && yValues instanceof double[]) {
      return new DateDoubleSurfaceShift(shiftType, shiftValues, ((List<Period>) xValues), ((double[]) yValues));
    } else if (xValues instanceof double[] && yValues instanceof List) {
      return new DoubleDateSurfaceShift(shiftType, shiftValues, ((double[]) xValues), ((List<Period>) yValues));
    } else if (xValues instanceof double[] && yValues instanceof double[]) {
      return new DoubleDoubleSurfaceShift(shiftType, shiftValues, (double[]) xValues, (double[]) yValues);
    } else {
      throw new IllegalArgumentException("Invalid axis types " + xValues.getClass().getName() + " and " + yValues.getClass().getName());
    }
  }

  /**
   * Returns the x values of the points to be shifted as a list of periods or a double array.
   * @param shifts The shifts
   * @return The x values of the points to be shifted
   */
  private static Object xValues(List<VolatilitySurfaceShift> shifts) {
    List<Object> values = Lists.newArrayListWithCapacity(shifts.size());
    for (VolatilitySurfaceShift shift : shifts) {
      values.add(shift.getX());
    }
    return axisValues(values);
  }

  /**
   * Returns the y values of the points to be shifted as a list of periods or a double array.
   * @param shifts The shifts
   * @return The y values of the points to be shifted
   */
  private static Object yValues(List<VolatilitySurfaceShift> shifts) {
    List<Object> values = Lists.newArrayListWithCapacity(shifts.size());
    for (VolatilitySurfaceShift shift : shifts) {
      values.add(shift.getY());
    }
    return axisValues(values);
  }

  /**
   * Converts a list of values (Periods or Doubles) on the x or y axis to a list of Periods or a double array.
   * @param values The axis values (Periods or Doubles)
   * @return A list of Periods of a double array
   */
  private static Object axisValues(List<Object> values) {
    Class<?> type = values.get(0).getClass();
    if (Number.class.isAssignableFrom(type)) {
      return doubleValues(values);
    } else if (type.equals(Period.class)) {
      return periodValues(values);
    } else {
      throw new IllegalArgumentException("Unexpected type for point " + type.getName());
    }
  }

  /**
   * Returns a list of periods
   * @param values A list of periods
   * @return IllegalArgumentException If any of the values aren't periods
   */
  private static List<Period> periodValues(List<Object> values) {
    List<Period> periods = Lists.newArrayListWithCapacity(values.size());
    for (Object value : values) {
      if (!(value instanceof Period)) {
        throw new IllegalArgumentException("All values must be of the same type, found Period and " + value.getClass().getName());
      }
      periods.add((Period) value);
    }
    return periods;
  }

  /**
   * Returns a double array containing the values
   * @param values A list of Doubles
   * @return The values in a double array
   * @throws IllegalArgumentException If any of the values aren't Doubles
   */
  private static double[] doubleValues(List<Object> values) {
    double[] doubleVals = new double[values.size()];
    int index = 0;
    for (Object value : values) {
      if (value instanceof Number) {
        doubleVals[index++] = ((Number) value).doubleValue();
      }
    }
    return doubleVals;
  }

  /**
   * Returns the shift amounts to apply to each point.
   * If the shift type is absolute the shift amounts are taken directly from the shifts.
   * If the shift type is relative the amounts are 1 + shift amount. Relative amounts are specified as a percentage
   * increase or decrease. e.g. +10.pc (0.1) is a scaling of 1.1 and -10.pc (-0.1) is a scaling of 0.9
   * @param shifts The shifts
   * @param shiftType Whether the shifts are relative or absolute
   * @return The shift amounts to apply to each point
   */
  private static double[] shiftValues(List<VolatilitySurfaceShift> shifts, ScenarioShiftType shiftType) {
    double[] shiftValues = new double[shifts.size()];
    int index = 0;
    for (VolatilitySurfaceShift shift : ArgumentChecker.notEmpty(shifts, "shifts")) {
      if (shiftType == ScenarioShiftType.ABSOLUTE) {
        shiftValues[index++] = shift.getShift().doubleValue();
      } else {
        // relative shifts are specified as percentage change
        // e.g. 10.pc (0.1) means multiply by 1.1, -10.pc (-0.1) means multiply by 0.9
        shiftValues[index++] = shift.getShift().doubleValue() + 1;
      }
    }
    return shiftValues;
  }

  /**
   * Converts the input values into an array of year fractions for passing to the analytics.
   * If periods is a double array it is returned unchanged. If it's a list of periods then the year fraction
   * is calculated for each period relative to the valuation time (using {@link TimeCalculator}).
   * @param periods The input axis values, a list of periods or a double array
   * @param valuationTime The valuation time
   * @return An array of year fractions for the axis values
   */
  /* package */ static double[] yearFractions(List<Period> periods, ZonedDateTime valuationTime) {
    double[] yearFractions = new double[periods.size()];
    int index = 0;
    for (Period period : periods) {
      yearFractions[index++] = TimeCalculator.getTimeBetween(valuationTime, valuationTime.plus(period));
    }
    return yearFractions;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilitySurfaceShiftManipulator}.
   * @return the meta-bean, not null
   */
  public static VolatilitySurfaceShiftManipulator.Meta meta() {
    return VolatilitySurfaceShiftManipulator.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilitySurfaceShiftManipulator.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static VolatilitySurfaceShiftManipulator.Builder builder() {
    return new VolatilitySurfaceShiftManipulator.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected VolatilitySurfaceShiftManipulator(VolatilitySurfaceShiftManipulator.Builder builder) {
    JodaBeanUtils.notNull(builder._shiftType, "shiftType");
    JodaBeanUtils.notNull(builder._shiftValues, "shiftValues");
    this._shiftType = builder._shiftType;
    this._shiftValues = builder._shiftValues.clone();
  }

  @Override
  public VolatilitySurfaceShiftManipulator.Meta metaBean() {
    return VolatilitySurfaceShiftManipulator.Meta.INSTANCE;
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
   * Gets the type of shift to apply to the surface points.
   * @return the value of the property, not null
   */
  public ScenarioShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the shift values to apply to each point.
   * @return the value of the property, not null
   */
  public double[] getShiftValues() {
    return (_shiftValues != null ? _shiftValues.clone() : null);
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
      VolatilitySurfaceShiftManipulator other = (VolatilitySurfaceShiftManipulator) obj;
      return JodaBeanUtils.equal(getShiftType(), other.getShiftType()) &&
          JodaBeanUtils.equal(getShiftValues(), other.getShiftValues());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getShiftType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getShiftValues());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("VolatilitySurfaceShiftManipulator{");
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
    buf.append("shiftValues").append('=').append(JodaBeanUtils.toString(getShiftValues())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilitySurfaceShiftManipulator}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftType} property.
     */
    private final MetaProperty<ScenarioShiftType> _shiftType = DirectMetaProperty.ofImmutable(
        this, "shiftType", VolatilitySurfaceShiftManipulator.class, ScenarioShiftType.class);
    /**
     * The meta-property for the {@code shiftValues} property.
     */
    private final MetaProperty<double[]> _shiftValues = DirectMetaProperty.ofImmutable(
        this, "shiftValues", VolatilitySurfaceShiftManipulator.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "shiftType",
        "shiftValues");

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
        case -453440444:  // shiftValues
          return _shiftValues;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public VolatilitySurfaceShiftManipulator.Builder builder() {
      return new VolatilitySurfaceShiftManipulator.Builder();
    }

    @Override
    public Class<? extends VolatilitySurfaceShiftManipulator> beanType() {
      return VolatilitySurfaceShiftManipulator.class;
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
    public final MetaProperty<ScenarioShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shiftValues} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> shiftValues() {
      return _shiftValues;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((VolatilitySurfaceShiftManipulator) bean).getShiftType();
        case -453440444:  // shiftValues
          return ((VolatilitySurfaceShiftManipulator) bean).getShiftValues();
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
   * The bean-builder for {@code VolatilitySurfaceShiftManipulator}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<VolatilitySurfaceShiftManipulator> {

    private ScenarioShiftType _shiftType;
    private double[] _shiftValues;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(VolatilitySurfaceShiftManipulator beanToCopy) {
      this._shiftType = beanToCopy.getShiftType();
      this._shiftValues = beanToCopy.getShiftValues().clone();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return _shiftType;
        case -453440444:  // shiftValues
          return _shiftValues;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          this._shiftType = (ScenarioShiftType) newValue;
          break;
        case -453440444:  // shiftValues
          this._shiftValues = (double[]) newValue;
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
    public VolatilitySurfaceShiftManipulator build() {
      return new VolatilitySurfaceShiftManipulator(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code shiftType} property in the builder.
     * @param shiftType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftType(ScenarioShiftType shiftType) {
      JodaBeanUtils.notNull(shiftType, "shiftType");
      this._shiftType = shiftType;
      return this;
    }

    /**
     * Sets the {@code shiftValues} property in the builder.
     * @param shiftValues  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftValues(double[] shiftValues) {
      JodaBeanUtils.notNull(shiftValues, "shiftValues");
      this._shiftValues = shiftValues;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("VolatilitySurfaceShiftManipulator.Builder{");
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
      buf.append("shiftValues").append('=').append(JodaBeanUtils.toString(_shiftValues)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
