/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.ShiftType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveNodeId;
import com.opengamma.sesame.TenorCurveNodeId;
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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Applies a point shift to the market quotes (precalibration) used to build a multicurve bundle.
 * The curves must be furnished with CurveNodeId data for this shift to operate.
 * If the CurveNodeId information is missing, then the Perturbation will never find the point
 * to shift and the scenarios will be the same as the base case.
 */
@BeanDefinition
public final class MulticurveInputPointShift implements Perturbation, ImmutableBean {

  private static final Logger s_logger = LoggerFactory.getLogger(MulticurveInputPointShift.class);

  /**
   * How the shift should be applied to the node values.
   * <p/>
   * A relative shift of 0.1 (+10%) scales the point value by 1.1, a relative shift of -0.2 (-20%) scales the
   * point value by 0.8.
   * <p/>
   * An absolute shift adds the shift amount to the curve value at the nodal point.
   */
  @PropertyDefinition(validate = "notNull", get = "private")
  private final ShiftType _shiftType;

  /**
   * The shifts to apply to the curve nodes, keyed by the ID of node.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<CurveNodeId, Double> _shifts;

  /**
   * Creates a shift that adds a fixed amount to every mapped market data value.
   * <p/>
   * Futures market data is handled as a special case so the shifted data makes sense.
   *
   * @param shifts the amount to add to each market data value
   * @return a shift that adds a fixed amount to each mapped market data value
   */
  public static MulticurveInputPointShift absolute(Map<CurveNodeId, Double> shifts) {
    return new MulticurveInputPointShift(ShiftType.ABSOLUTE, shifts);
  }

  /**
   * Creates a shift that multiplies every mapped market data value by a fixed factor.
   * <p/>
   * Futures market data is handled as a special case so the shifted data makes sense
   *
   * @param shifts the factor to multiply the mapped values by
   * @return a shift that multiplies the mapped market data values by a fixed factor
   */
  public static MulticurveInputPointShift relative(Map<CurveNodeId, Double> shifts) {
    return new MulticurveInputPointShift(ShiftType.RELATIVE, shifts);
  }

  /**
   * Applies the shifts to the curve input data.
   *
   * @param marketData   a piece of market data with type {@link CurveInputs}
   * @param matchDetails details of the match which the {@link MarketDataFilter} was applied to the market data
   * @return the shifted data
   */
  @Override
  public CurveInputs apply(Object marketData, MatchDetails matchDetails) {
    SnapshotDataBundle shiftedData = new SnapshotDataBundle();
    CurveInputs curveInputs = ((CurveInputs) marketData);
    SnapshotDataBundle nodeData = curveInputs.getNodeData();

    for (CurveNodeWithIdentifier nodeWithId : curveInputs.getNodes()) {
      CurveNode node = nodeWithId.getCurveNode();
      ExternalId id = nodeWithId.getIdentifier();
      Double value = nodeData.getDataPoint(id);

      if (value != null) {
        shiftedData.setDataPoint(id, shift(value, node));
      } else {
        s_logger.info("No data found for curve node with ID {}", id);
      }
    }
    return new CurveInputs(curveInputs.getNodes(), shiftedData);
  }

  private double shift(double value, CurveNode node) {
    double shiftAmount = calcShiftAmount(node);
    // futures are quoted the other way round, i.e. (1 - value)
    if (node instanceof RateFutureNode) {
      return 1 - shift(shiftAmount, 1 - value);
    } else {
      return shift(shiftAmount, value);
    }
  }

  private double shift(double shiftAmount, double value) {
    switch (_shiftType) {
      case ABSOLUTE:
        return value + shiftAmount;
      case RELATIVE:
        return value * (1 + shiftAmount);
      default:
        // should never happen
        throw new IllegalStateException("Unexpected shift type: " + _shiftType);
    }
  }

  private double calcShiftAmount(CurveNode node) {
    if (node instanceof RateFutureNode) {
      // TODO not sure how to get proper expiry for futures to create CurveNodeId
      return 0D;
    } else {
      CurveNodeId nodeId = TenorCurveNodeId.of(node.getResolvedMaturity());
      Double shiftAmount = _shifts.get(nodeId);
      // this node is not mapped to be shifted
      if (shiftAmount == null) {
        return 0D;
      }
      return shiftAmount;
    }
  }

  @Override
  public Class<CurveInputs> getMarketDataType() {
    return CurveInputs.class;
  }

  @Override
  public Class<? extends MatchDetails> getMatchDetailsType() {
    return MulticurveMatchDetails.class;
  }

  @Override
  public PerturbationTarget getTargetType() {
    return PerturbationTarget.INPUT;
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF

  /**
   * The meta-bean for {@code MulticurveInputPointShift}.
   *
   * @return the meta-bean, not null
   */
  public static MulticurveInputPointShift.Meta meta() {
    return MulticurveInputPointShift.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MulticurveInputPointShift.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   *
   * @return the builder, not null
   */
  public static MulticurveInputPointShift.Builder builder() {
    return new MulticurveInputPointShift.Builder();
  }

  private MulticurveInputPointShift(
      ShiftType shiftType,
      Map<CurveNodeId, Double> shifts) {
    JodaBeanUtils.notNull(shiftType, "shiftType");
    JodaBeanUtils.notNull(shifts, "shifts");
    this._shiftType = shiftType;
    this._shifts = ImmutableMap.copyOf(shifts);
  }

  @Override
  public MulticurveInputPointShift.Meta metaBean() {
    return MulticurveInputPointShift.Meta.INSTANCE;
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
   * Gets how the shift should be applied to the node values.
   * <p/>
   * A relative shift of 0.1 (+10%) scales the point value by 1.1, a relative shift of -0.2 (-20%) scales the
   * point value by 0.8.
   * <p/>
   * An absolute shift adds the shift amount to the curve value at the nodal point.
   *
   * @return the value of the property, not null
   */
  private ShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------

  /**
   * Gets the shifts to apply to the curve nodes, keyed by the ID of node.
   *
   * @return the value of the property, not null
   */
  public Map<CurveNodeId, Double> getShifts() {
    return _shifts;
  }

  //-----------------------------------------------------------------------

  /**
   * Returns a builder that allows this bean to be mutated.
   *
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
      MulticurveInputPointShift other = (MulticurveInputPointShift) obj;
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
    buf.append("MulticurveInputPointShift{");
    buf.append("shiftType").append('=').append(getShiftType()).append(',').append(' ');
    buf.append("shifts").append('=').append(JodaBeanUtils.toString(getShifts()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------

  /**
   * The meta-bean for {@code MulticurveInputPointShift}.
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
        this, "shiftType", MulticurveInputPointShift.class, ShiftType.class);
    /**
     * The meta-property for the {@code shifts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private final MetaProperty<Map<CurveNodeId, Double>> _shifts = DirectMetaProperty.ofImmutable(
        this, "shifts", MulticurveInputPointShift.class, (Class) Map.class);
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
    public MulticurveInputPointShift.Builder builder() {
      return new MulticurveInputPointShift.Builder();
    }

    @Override
    public Class<? extends MulticurveInputPointShift> beanType() {
      return MulticurveInputPointShift.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------

    /**
     * The meta-property for the {@code shiftType} property.
     *
     * @return the meta-property, not null
     */
    public MetaProperty<ShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shifts} property.
     *
     * @return the meta-property, not null
     */
    public MetaProperty<Map<CurveNodeId, Double>> shifts() {
      return _shifts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((MulticurveInputPointShift) bean).getShiftType();
        case -903338959:  // shifts
          return ((MulticurveInputPointShift) bean).getShifts();
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
   * The bean-builder for {@code MulticurveInputPointShift}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<MulticurveInputPointShift> {

    private ShiftType _shiftType;
    private Map<CurveNodeId, Double> _shifts = new HashMap<CurveNodeId, Double>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     *
     * @param beanToCopy the bean to copy from, not null
     */
    private Builder(MulticurveInputPointShift beanToCopy) {
      this._shiftType = beanToCopy.getShiftType();
      this._shifts = new HashMap<CurveNodeId, Double>(beanToCopy.getShifts());
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
          this._shifts = (Map<CurveNodeId, Double>) newValue;
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
    public MulticurveInputPointShift build() {
      return new MulticurveInputPointShift(
          _shiftType,
          _shifts);
    }

    //-----------------------------------------------------------------------

    /**
     * Sets the {@code shiftType} property in the builder.
     *
     * @param shiftType the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftType(ShiftType shiftType) {
      JodaBeanUtils.notNull(shiftType, "shiftType");
      this._shiftType = shiftType;
      return this;
    }

    /**
     * Sets the {@code shifts} property in the builder.
     *
     * @param shifts the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(Map<CurveNodeId, Double> shifts) {
      JodaBeanUtils.notNull(shifts, "shifts");
      this._shifts = shifts;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("MulticurveInputPointShift.Builder{");
      buf.append("shiftType").append('=').append(JodaBeanUtils.toString(_shiftType)).append(',').append(' ');
      buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
