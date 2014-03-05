/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.id.ExternalId;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition of a yield curve.
 * <p>
 * This definition uses a set of {@code FixedIncomeStrip} instances.
 * <p>
 * This class is mutable.
 */
@Config(description = "Yield curve definition", group = ConfigGroups.CURVES_LEGACY)
@BeanDefinition
public class YieldCurveDefinition extends DirectBean implements Serializable, UniqueIdentifiable,
    MutableUniqueIdentifiable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the yield curve.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The currency that the curve is for.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The region that the curve is for.
   */
  @PropertyDefinition
  private ExternalId _regionId;
  /**
   * The display name of the curve.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The name of the interpolator to use.
   */
  @PropertyDefinition(validate = "notNull")
  private String _interpolatorName;
  /**
   * The name of the left extrapolator.
   */
  @PropertyDefinition(validate = "notNull")
  private String _leftExtrapolatorName;
  /**
   * The name of the right extrapolator.
   */
  @PropertyDefinition(validate = "notNull")
  private String _rightExtrapolatorName;
  /**
   * Whether to interpolate between yields (true) or discount factors (false)
   */
  @PropertyDefinition
  private boolean _interpolateYields;
  /**
   * The underlying strips.
   */
  @PropertyDefinition
  private final SortedSet<FixedIncomeStrip> _strips = new TreeSet<FixedIncomeStrip>();

  /**
   * Creates an instance for Joda-Beans.
   */
  private YieldCurveDefinition() {
  }

  /**
   * Creates an instance.
   *
   * @param currency  the currency of the curve, not null
   * @param region  the region of the curve, may be null
   * @param name  the display name, may be null
   * @param interpolatorName  the interpolator name, not null
   * @param leftExtrapolatorName  the left extrapolator name, not null
   * @param rightExtrapolatorName  the right extrapolator name, not null
   * @param interpolateYields  whether to interpolate yields (true) or discount factors (false)
   */
  public YieldCurveDefinition(final Currency currency, final ExternalId region, final String name,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName,
      final boolean interpolateYields) {
    this(currency, region, name, interpolatorName, leftExtrapolatorName, rightExtrapolatorName,
        interpolateYields, Collections.<FixedIncomeStrip>emptySet());
  }

  /**
   * Creates an instance.
   *
   * @param currency  the currency of the curve, not null
   * @param region  the region of the curve, may be null
   * @param name  the display name, may be null
   * @param interpolatorName  the interpolator name, not null
   * @param leftExtrapolatorName  the left extrapolator name, not null
   * @param rightExtrapolatorName  the right extrapolator name, not null
   * @param interpolateYields  whether to interpolate yields (true) or discount factors (false)
   * @param strips  the underlying strips, null treated as empty list
   */
  public YieldCurveDefinition(final Currency currency, final ExternalId region, final String name,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName,
      final boolean interpolateYields, final Iterable<? extends FixedIncomeStrip> strips) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(interpolatorName, "interpolatorName");
    _currency = currency;
    _regionId = region;
    _name = name;
    _interpolatorName = interpolatorName;
    _leftExtrapolatorName = leftExtrapolatorName;
    _rightExtrapolatorName = rightExtrapolatorName;
    _interpolateYields = interpolateYields;
    if (strips != null) {
      for (final FixedIncomeStrip strip : strips) {
        addStrip(strip);
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a strip to the yield curve definition.
   *
   * @param strip  the strip to add, not null
   */
  public void addStrip(final FixedIncomeStrip strip) {
    ArgumentChecker.notNull(strip, "Strip");
    _strips.add(strip);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code YieldCurveDefinition}.
   * @return the meta-bean, not null
   */
  public static YieldCurveDefinition.Meta meta() {
    return YieldCurveDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(YieldCurveDefinition.Meta.INSTANCE);
  }

  @Override
  public YieldCurveDefinition.Meta metaBean() {
    return YieldCurveDefinition.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the yield curve.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the yield curve.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency that the curve is for.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency that the curve is for.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region that the curve is for.
   * @return the value of the property
   */
  public ExternalId getRegionId() {
    return _regionId;
  }

  /**
   * Sets the region that the curve is for.
   * @param regionId  the new value of the property
   */
  public void setRegionId(ExternalId regionId) {
    this._regionId = regionId;
  }

  /**
   * Gets the the {@code regionId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> regionId() {
    return metaBean().regionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the display name of the curve.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the display name of the curve.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the interpolator to use.
   * @return the value of the property, not null
   */
  public String getInterpolatorName() {
    return _interpolatorName;
  }

  /**
   * Sets the name of the interpolator to use.
   * @param interpolatorName  the new value of the property, not null
   */
  public void setInterpolatorName(String interpolatorName) {
    JodaBeanUtils.notNull(interpolatorName, "interpolatorName");
    this._interpolatorName = interpolatorName;
  }

  /**
   * Gets the the {@code interpolatorName} property.
   * @return the property, not null
   */
  public final Property<String> interpolatorName() {
    return metaBean().interpolatorName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the left extrapolator.
   * @return the value of the property, not null
   */
  public String getLeftExtrapolatorName() {
    return _leftExtrapolatorName;
  }

  /**
   * Sets the name of the left extrapolator.
   * @param leftExtrapolatorName  the new value of the property, not null
   */
  public void setLeftExtrapolatorName(String leftExtrapolatorName) {
    JodaBeanUtils.notNull(leftExtrapolatorName, "leftExtrapolatorName");
    this._leftExtrapolatorName = leftExtrapolatorName;
  }

  /**
   * Gets the the {@code leftExtrapolatorName} property.
   * @return the property, not null
   */
  public final Property<String> leftExtrapolatorName() {
    return metaBean().leftExtrapolatorName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of the right extrapolator.
   * @return the value of the property, not null
   */
  public String getRightExtrapolatorName() {
    return _rightExtrapolatorName;
  }

  /**
   * Sets the name of the right extrapolator.
   * @param rightExtrapolatorName  the new value of the property, not null
   */
  public void setRightExtrapolatorName(String rightExtrapolatorName) {
    JodaBeanUtils.notNull(rightExtrapolatorName, "rightExtrapolatorName");
    this._rightExtrapolatorName = rightExtrapolatorName;
  }

  /**
   * Gets the the {@code rightExtrapolatorName} property.
   * @return the property, not null
   */
  public final Property<String> rightExtrapolatorName() {
    return metaBean().rightExtrapolatorName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to interpolate between yields (true) or discount factors (false)
   * @return the value of the property
   */
  public boolean isInterpolateYields() {
    return _interpolateYields;
  }

  /**
   * Sets whether to interpolate between yields (true) or discount factors (false)
   * @param interpolateYields  the new value of the property
   */
  public void setInterpolateYields(boolean interpolateYields) {
    this._interpolateYields = interpolateYields;
  }

  /**
   * Gets the the {@code interpolateYields} property.
   * @return the property, not null
   */
  public final Property<Boolean> interpolateYields() {
    return metaBean().interpolateYields().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying strips.
   * @return the value of the property, not null
   */
  public SortedSet<FixedIncomeStrip> getStrips() {
    return _strips;
  }

  /**
   * Sets the underlying strips.
   * @param strips  the new value of the property, not null
   */
  public void setStrips(SortedSet<FixedIncomeStrip> strips) {
    JodaBeanUtils.notNull(strips, "strips");
    this._strips.clear();
    this._strips.addAll(strips);
  }

  /**
   * Gets the the {@code strips} property.
   * @return the property, not null
   */
  public final Property<SortedSet<FixedIncomeStrip>> strips() {
    return metaBean().strips().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public YieldCurveDefinition clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      YieldCurveDefinition other = (YieldCurveDefinition) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getRegionId(), other.getRegionId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getInterpolatorName(), other.getInterpolatorName()) &&
          JodaBeanUtils.equal(getLeftExtrapolatorName(), other.getLeftExtrapolatorName()) &&
          JodaBeanUtils.equal(getRightExtrapolatorName(), other.getRightExtrapolatorName()) &&
          (isInterpolateYields() == other.isInterpolateYields()) &&
          JodaBeanUtils.equal(getStrips(), other.getStrips());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInterpolatorName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLeftExtrapolatorName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRightExtrapolatorName());
    hash += hash * 31 + JodaBeanUtils.hashCode(isInterpolateYields());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStrips());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("YieldCurveDefinition{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("regionId").append('=').append(JodaBeanUtils.toString(getRegionId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("interpolatorName").append('=').append(JodaBeanUtils.toString(getInterpolatorName())).append(',').append(' ');
    buf.append("leftExtrapolatorName").append('=').append(JodaBeanUtils.toString(getLeftExtrapolatorName())).append(',').append(' ');
    buf.append("rightExtrapolatorName").append('=').append(JodaBeanUtils.toString(getRightExtrapolatorName())).append(',').append(' ');
    buf.append("interpolateYields").append('=').append(JodaBeanUtils.toString(isInterpolateYields())).append(',').append(' ');
    buf.append("strips").append('=').append(JodaBeanUtils.toString(getStrips())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code YieldCurveDefinition}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", YieldCurveDefinition.class, UniqueId.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", YieldCurveDefinition.class, Currency.class);
    /**
     * The meta-property for the {@code regionId} property.
     */
    private final MetaProperty<ExternalId> _regionId = DirectMetaProperty.ofReadWrite(
        this, "regionId", YieldCurveDefinition.class, ExternalId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", YieldCurveDefinition.class, String.class);
    /**
     * The meta-property for the {@code interpolatorName} property.
     */
    private final MetaProperty<String> _interpolatorName = DirectMetaProperty.ofReadWrite(
        this, "interpolatorName", YieldCurveDefinition.class, String.class);
    /**
     * The meta-property for the {@code leftExtrapolatorName} property.
     */
    private final MetaProperty<String> _leftExtrapolatorName = DirectMetaProperty.ofReadWrite(
        this, "leftExtrapolatorName", YieldCurveDefinition.class, String.class);
    /**
     * The meta-property for the {@code rightExtrapolatorName} property.
     */
    private final MetaProperty<String> _rightExtrapolatorName = DirectMetaProperty.ofReadWrite(
        this, "rightExtrapolatorName", YieldCurveDefinition.class, String.class);
    /**
     * The meta-property for the {@code interpolateYields} property.
     */
    private final MetaProperty<Boolean> _interpolateYields = DirectMetaProperty.ofReadWrite(
        this, "interpolateYields", YieldCurveDefinition.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code strips} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<SortedSet<FixedIncomeStrip>> _strips = DirectMetaProperty.ofReadWrite(
        this, "strips", YieldCurveDefinition.class, (Class) SortedSet.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "currency",
        "regionId",
        "name",
        "interpolatorName",
        "leftExtrapolatorName",
        "rightExtrapolatorName",
        "interpolateYields",
        "strips");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 575402001:  // currency
          return _currency;
        case -690339025:  // regionId
          return _regionId;
        case 3373707:  // name
          return _name;
        case -1247314958:  // interpolatorName
          return _interpolatorName;
        case -718701979:  // leftExtrapolatorName
          return _leftExtrapolatorName;
        case -556150150:  // rightExtrapolatorName
          return _rightExtrapolatorName;
        case -987835673:  // interpolateYields
          return _interpolateYields;
        case -891985829:  // strips
          return _strips;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends YieldCurveDefinition> builder() {
      return new DirectBeanBuilder<YieldCurveDefinition>(new YieldCurveDefinition());
    }

    @Override
    public Class<? extends YieldCurveDefinition> beanType() {
      return YieldCurveDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code regionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> regionId() {
      return _regionId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code interpolatorName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> interpolatorName() {
      return _interpolatorName;
    }

    /**
     * The meta-property for the {@code leftExtrapolatorName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> leftExtrapolatorName() {
      return _leftExtrapolatorName;
    }

    /**
     * The meta-property for the {@code rightExtrapolatorName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> rightExtrapolatorName() {
      return _rightExtrapolatorName;
    }

    /**
     * The meta-property for the {@code interpolateYields} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> interpolateYields() {
      return _interpolateYields;
    }

    /**
     * The meta-property for the {@code strips} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SortedSet<FixedIncomeStrip>> strips() {
      return _strips;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((YieldCurveDefinition) bean).getUniqueId();
        case 575402001:  // currency
          return ((YieldCurveDefinition) bean).getCurrency();
        case -690339025:  // regionId
          return ((YieldCurveDefinition) bean).getRegionId();
        case 3373707:  // name
          return ((YieldCurveDefinition) bean).getName();
        case -1247314958:  // interpolatorName
          return ((YieldCurveDefinition) bean).getInterpolatorName();
        case -718701979:  // leftExtrapolatorName
          return ((YieldCurveDefinition) bean).getLeftExtrapolatorName();
        case -556150150:  // rightExtrapolatorName
          return ((YieldCurveDefinition) bean).getRightExtrapolatorName();
        case -987835673:  // interpolateYields
          return ((YieldCurveDefinition) bean).isInterpolateYields();
        case -891985829:  // strips
          return ((YieldCurveDefinition) bean).getStrips();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((YieldCurveDefinition) bean).setUniqueId((UniqueId) newValue);
          return;
        case 575402001:  // currency
          ((YieldCurveDefinition) bean).setCurrency((Currency) newValue);
          return;
        case -690339025:  // regionId
          ((YieldCurveDefinition) bean).setRegionId((ExternalId) newValue);
          return;
        case 3373707:  // name
          ((YieldCurveDefinition) bean).setName((String) newValue);
          return;
        case -1247314958:  // interpolatorName
          ((YieldCurveDefinition) bean).setInterpolatorName((String) newValue);
          return;
        case -718701979:  // leftExtrapolatorName
          ((YieldCurveDefinition) bean).setLeftExtrapolatorName((String) newValue);
          return;
        case -556150150:  // rightExtrapolatorName
          ((YieldCurveDefinition) bean).setRightExtrapolatorName((String) newValue);
          return;
        case -987835673:  // interpolateYields
          ((YieldCurveDefinition) bean).setInterpolateYields((Boolean) newValue);
          return;
        case -891985829:  // strips
          ((YieldCurveDefinition) bean).setStrips((SortedSet<FixedIncomeStrip>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((YieldCurveDefinition) bean)._currency, "currency");
      JodaBeanUtils.notNull(((YieldCurveDefinition) bean)._interpolatorName, "interpolatorName");
      JodaBeanUtils.notNull(((YieldCurveDefinition) bean)._leftExtrapolatorName, "leftExtrapolatorName");
      JodaBeanUtils.notNull(((YieldCurveDefinition) bean)._rightExtrapolatorName, "rightExtrapolatorName");
      JodaBeanUtils.notNull(((YieldCurveDefinition) bean)._strips, "strips");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
