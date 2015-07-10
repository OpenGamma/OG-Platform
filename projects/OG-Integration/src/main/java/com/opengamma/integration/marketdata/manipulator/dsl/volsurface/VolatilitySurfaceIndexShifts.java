/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.marketdata.manipulator.dsl.ScenarioShiftType;
import com.opengamma.util.ArgumentChecker;

/**
 * Shifts points on a volatility surface using the index of the expiry to specify the shift amount.
 * All points for an expiry are shifted by the the same amount (absolute or relative).
 */
@BeanDefinition
public final class VolatilitySurfaceIndexShifts implements StructureManipulator<VolatilitySurface>, ImmutableBean {

  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceIndexShifts.class);

  @PropertyDefinition(validate = "notNull")
  private final ScenarioShiftType _shiftType;

  @PropertyDefinition(validate = "notNull")
  private final List<Double> _shifts;

  /**
   * Creates a new set of shifts
   * The first element of the list of shifts is applied to all points for the first expiry, the second element is
   * applied to all points for the second expiry and so on. The number of shifts doesn't have to match
   * the number of expiries in the surface. Any extra shifts are ignored. If there is no shift for an expiry
   * then no change is made to the surface at that expiry.
   *
   * @param shiftType absolute or relative
   * @param shifts the shift to apply at each expiry in the surface
   */
  @ImmutableConstructor
  public VolatilitySurfaceIndexShifts(ScenarioShiftType shiftType, List<Double> shifts) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shifts = ImmutableList.copyOf(ArgumentChecker.notEmpty(shifts, "shifts"));
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface volSurface,
                                   ValueSpecification valueSpecification,
                                   FunctionExecutionContext executionContext) {
    Surface<Double, Double, Double> surface = volSurface.getSurface();

    if (volSurface instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      BlackVolatilitySurfaceMoneynessFcnBackedByGrid blackSurface = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) volSurface;
      SmileSurfaceDataBundle shiftedSurfaceData = shiftSurfaceData(blackSurface.getGridData());
      return blackSurface.getInterpolator().getVolatilitySurface(shiftedSurfaceData);
    } else if (!(surface instanceof FunctionalDoublesSurface)) {
      return shiftNonFunctionalSurface(volSurface);
    } else {
      s_logger.warn("Unable to shift surface of type {}/{}",
                    volSurface.getClass().getName(),
                    surface.getClass().getName());
      return volSurface;
    }
  }

  /**
   * Returns a copy of the surface data with shifts applied.
   *
   * @param surfaceData surface data
   * @return a copy of the surface data with shifts applied
   */
  public SmileSurfaceDataBundle shiftSurfaceData(SmileSurfaceDataBundle surfaceData) {
    SmileSurfaceDataBundle shiftedData = surfaceData;
    int nShifts = Math.min(_shifts.size(), shiftedData.getNumExpiries());

    for (int i = 0; i < nShifts; i++) {
      double[] strikes = shiftedData.getStrikes()[i];

      for (int j = 0; j < strikes.length; j++) {
        Double shiftAmount;

        if (_shiftType == ScenarioShiftType.ABSOLUTE) {
          shiftAmount = _shifts.get(i);
        } else {
          double vol = shiftedData.getVolatilities()[i][j];
          shiftAmount = vol * _shifts.get(i);
        }
        shiftedData = shiftedData.withBumpedPoint(i, j, shiftAmount);
      }
    }
    return shiftedData;
  }

  /**
   * Returns a shifted volatility surface.
   * The surface returned by {@link VolatilitySurface#getSurface()} must not be an instance of
   * {@link FunctionalDoublesSurface}.
   *
   * @param volSurface the base surface
   * @return the surface with a shift applied
   */
  private VolatilitySurface shiftNonFunctionalSurface(VolatilitySurface volSurface) {
    Surface<Double, Double, Double> surface = volSurface.getSurface();
    Double[] xData = surface.getXData();
    Set<Double> xValues = Sets.newTreeSet(Arrays.asList(xData));
    // map the values to indices so we can find the shift at each point
    Map<Double, Integer> valuesToIndices = new HashMap<>(xValues.size());
    boolean absolute = _shiftType == ScenarioShiftType.ABSOLUTE;
    int index = 0;

    for (Double xValue : xValues) {
      valuesToIndices.put(xValue, index++);
    }
    double[] shifts = new double[xData.length];

    for (int i = 0; i < xData.length; i++) {
      Integer shiftIndex = valuesToIndices.get(xData[i]);
      Double shift;
      if (shiftIndex > _shifts.size() - 1) {
        shift = absolute ? 0d : 1d;
      } else {
        shift = _shifts.get(shiftIndex);
      }
      shifts[i] = shift;
    }
    double[] xArray = ArrayUtils.toPrimitive(xData);
    double[] yArray = ArrayUtils.toPrimitive(surface.getYData());

    if (absolute) {
      return volSurface.withMultipleAdditiveShifts(xArray, yArray, shifts);
    } else {
      return volSurface.withMultipleMultiplicativeShifts(xArray, yArray, shifts);
    }
  }

  @Override
  public Class<VolatilitySurface> getExpectedType() {
    return VolatilitySurface.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilitySurfaceIndexShifts}.
   * @return the meta-bean, not null
   */
  public static VolatilitySurfaceIndexShifts.Meta meta() {
    return VolatilitySurfaceIndexShifts.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilitySurfaceIndexShifts.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static VolatilitySurfaceIndexShifts.Builder builder() {
    return new VolatilitySurfaceIndexShifts.Builder();
  }

  @Override
  public VolatilitySurfaceIndexShifts.Meta metaBean() {
    return VolatilitySurfaceIndexShifts.Meta.INSTANCE;
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
   * Gets the shiftType.
   * @return the value of the property, not null
   */
  public ScenarioShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the shifts.
   * @return the value of the property, not null
   */
  public List<Double> getShifts() {
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
      VolatilitySurfaceIndexShifts other = (VolatilitySurfaceIndexShifts) obj;
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
    buf.append("VolatilitySurfaceIndexShifts{");
    buf.append("shiftType").append('=').append(getShiftType()).append(',').append(' ');
    buf.append("shifts").append('=').append(JodaBeanUtils.toString(getShifts()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilitySurfaceIndexShifts}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftType} property.
     */
    private final MetaProperty<ScenarioShiftType> _shiftType = DirectMetaProperty.ofImmutable(
        this, "shiftType", VolatilitySurfaceIndexShifts.class, ScenarioShiftType.class);
    /**
     * The meta-property for the {@code shifts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Double>> _shifts = DirectMetaProperty.ofImmutable(
        this, "shifts", VolatilitySurfaceIndexShifts.class, (Class) List.class);
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
    public VolatilitySurfaceIndexShifts.Builder builder() {
      return new VolatilitySurfaceIndexShifts.Builder();
    }

    @Override
    public Class<? extends VolatilitySurfaceIndexShifts> beanType() {
      return VolatilitySurfaceIndexShifts.class;
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
    public MetaProperty<ScenarioShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shifts} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<Double>> shifts() {
      return _shifts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((VolatilitySurfaceIndexShifts) bean).getShiftType();
        case -903338959:  // shifts
          return ((VolatilitySurfaceIndexShifts) bean).getShifts();
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
   * The bean-builder for {@code VolatilitySurfaceIndexShifts}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<VolatilitySurfaceIndexShifts> {

    private ScenarioShiftType _shiftType;
    private List<Double> _shifts = new ArrayList<Double>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(VolatilitySurfaceIndexShifts beanToCopy) {
      this._shiftType = beanToCopy.getShiftType();
      this._shifts = new ArrayList<Double>(beanToCopy.getShifts());
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
          this._shiftType = (ScenarioShiftType) newValue;
          break;
        case -903338959:  // shifts
          this._shifts = (List<Double>) newValue;
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
    public VolatilitySurfaceIndexShifts build() {
      return new VolatilitySurfaceIndexShifts(
          _shiftType,
          _shifts);
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
     * Sets the {@code shifts} property in the builder.
     * @param shifts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(List<Double> shifts) {
      JodaBeanUtils.notNull(shifts, "shifts");
      this._shifts = shifts;
      return this;
    }

    /**
     * Sets the {@code shifts} property in the builder
     * from an array of objects.
     * @param shifts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(Double... shifts) {
      return shifts(Arrays.asList(shifts));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("VolatilitySurfaceIndexShifts.Builder{");
      buf.append("shiftType").append('=').append(JodaBeanUtils.toString(_shiftType)).append(',').append(' ');
      buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
