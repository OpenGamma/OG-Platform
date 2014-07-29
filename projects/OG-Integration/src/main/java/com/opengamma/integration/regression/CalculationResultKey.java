/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
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

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;

/**
 * Key for identifying values in results.
 * This is consistent across results generated by different server and database instances. The targets can
 * be positions (in which case {@link #getTargetId positionId} will be non-null) or portfolio nodes.
 */
@BeanDefinition
public final class CalculationResultKey implements ImmutableBean, Comparable<CalculationResultKey> {

  @PropertyDefinition(validate = "notNull")
  private final String _calcConfigName;

  @PropertyDefinition(validate = "notNull")
  private final String _valueName;

  @PropertyDefinition(validate = "notNull")
  private final ValueProperties _properties;

  @PropertyDefinition
  private final List<String> _path;

  @PropertyDefinition
  private final ObjectId _targetId;

  public static CalculationResultKey forPosition(String calcConfigName,
                                                 String valueName,
                                                 ValueProperties properties,
                                                 ObjectId positionId) {
    ArgumentChecker.notNull(positionId, "positionId");
    return new CalculationResultKey(calcConfigName, valueName, properties, null, positionId);
  }

  public static CalculationResultKey forCurrency(String calcConfigName,
                                                 String valueName,
                                                 ValueProperties properties,
                                                 ObjectId currencyId) {
    ArgumentChecker.notNull(currencyId, "currencyId");
    return new CalculationResultKey(calcConfigName, valueName, properties, null, currencyId);
  }

  public static CalculationResultKey forPositionWithParentNode(String calcConfigName,
                                                               String valueName,
                                                               ValueProperties properties,
                                                               List<String> path,
                                                               ObjectId positionId) {
    ArgumentChecker.notNull(path, "path");
    ArgumentChecker.notNull(positionId, "positionId");
    return new CalculationResultKey(calcConfigName, valueName, properties, path, positionId);
  }

  public static CalculationResultKey forNode(String calcConfigName,
                                             String valueName,
                                             ValueProperties properties,
                                             List<String> path) {
    ArgumentChecker.notNull(path, "path");
    return new CalculationResultKey(calcConfigName, valueName, properties, path, null);
  }

  public static CalculationResultKey forTrade(String calcConfigName,
                                              String valueName,
                                              ValueProperties properties,
                                              ObjectId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    return new CalculationResultKey(calcConfigName, valueName, properties, null, tradeId);
  }

  // TODO can't let this be regenerated because of a joda beans bug handling nullable lists
  @ImmutableConstructor
  private CalculationResultKey(String calcConfigName,
                              String valueName,
                              ValueProperties properties,
                              List<String> path,
                              ObjectId targetId) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(properties, "properties");
    _calcConfigName = calcConfigName;
    _valueName = valueName;
    _properties = properties;
    if (path == null) {
      _path = null;
    } else {
      // Joda beans generates a constructor which calls this without the null check and throws an NPE if path is null
      _path = ImmutableList.copyOf(path);
    }
    _targetId = targetId;
  }

  @Override
  public int compareTo(CalculationResultKey other) {
    return new CompareToBuilder()
        .append(getCalcConfigName(), other.getCalcConfigName())
        .append(getTargetId(), other.getTargetId())
        .appendSuper(comparePaths(getPath(), other.getPath()))
        .append(getValueName(), other.getValueName())
        .append(getProperties(), other.getProperties())
        .toComparison();
  }

  private static int comparePaths(List<String> path1, List<String> path2) {
    if (path1 == null && path2 == null) {
      return 0;
    }
    if (path1 == null) {
      return 1;
    } else if (path2 == null) {
      return -1;
    }
    if (path1.isEmpty() && path2.isEmpty()) {
      return 0;
    }
    if (path1.isEmpty()) {
      return -1;
    } else if (path2.isEmpty()) {
      return 1;
    } else {
      String s1 = path1.get(0);
      String s2 = path2.get(0);
      int cmp = s1.compareTo(s2);
      if (cmp != 0) {
        return cmp;
      } else {
        return comparePaths(path1.subList(1, path1.size()), path2.subList(1, path2.size()));
      }
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CalculationResultKey}.
   * @return the meta-bean, not null
   */
  public static CalculationResultKey.Meta meta() {
    return CalculationResultKey.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CalculationResultKey.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CalculationResultKey.Builder builder() {
    return new CalculationResultKey.Builder();
  }

  @Override
  public CalculationResultKey.Meta metaBean() {
    return CalculationResultKey.Meta.INSTANCE;
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
   * Gets the calcConfigName.
   * @return the value of the property, not null
   */
  public String getCalcConfigName() {
    return _calcConfigName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valueName.
   * @return the value of the property, not null
   */
  public String getValueName() {
    return _valueName;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the properties.
   * @return the value of the property, not null
   */
  public ValueProperties getProperties() {
    return _properties;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the path.
   * @return the value of the property
   */
  public List<String> getPath() {
    return _path;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the targetId.
   * @return the value of the property
   */
  public ObjectId getTargetId() {
    return _targetId;
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
      CalculationResultKey other = (CalculationResultKey) obj;
      return JodaBeanUtils.equal(getCalcConfigName(), other.getCalcConfigName()) &&
          JodaBeanUtils.equal(getValueName(), other.getValueName()) &&
          JodaBeanUtils.equal(getProperties(), other.getProperties()) &&
          JodaBeanUtils.equal(getPath(), other.getPath()) &&
          JodaBeanUtils.equal(getTargetId(), other.getTargetId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCalcConfigName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValueName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProperties());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPath());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTargetId());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("CalculationResultKey{");
    buf.append("calcConfigName").append('=').append(getCalcConfigName()).append(',').append(' ');
    buf.append("valueName").append('=').append(getValueName()).append(',').append(' ');
    buf.append("properties").append('=').append(getProperties()).append(',').append(' ');
    buf.append("path").append('=').append(getPath()).append(',').append(' ');
    buf.append("targetId").append('=').append(JodaBeanUtils.toString(getTargetId()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CalculationResultKey}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code calcConfigName} property.
     */
    private final MetaProperty<String> _calcConfigName = DirectMetaProperty.ofImmutable(
        this, "calcConfigName", CalculationResultKey.class, String.class);
    /**
     * The meta-property for the {@code valueName} property.
     */
    private final MetaProperty<String> _valueName = DirectMetaProperty.ofImmutable(
        this, "valueName", CalculationResultKey.class, String.class);
    /**
     * The meta-property for the {@code properties} property.
     */
    private final MetaProperty<ValueProperties> _properties = DirectMetaProperty.ofImmutable(
        this, "properties", CalculationResultKey.class, ValueProperties.class);
    /**
     * The meta-property for the {@code path} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<String>> _path = DirectMetaProperty.ofImmutable(
        this, "path", CalculationResultKey.class, (Class) List.class);
    /**
     * The meta-property for the {@code targetId} property.
     */
    private final MetaProperty<ObjectId> _targetId = DirectMetaProperty.ofImmutable(
        this, "targetId", CalculationResultKey.class, ObjectId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "calcConfigName",
        "valueName",
        "properties",
        "path",
        "targetId");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 875311394:  // calcConfigName
          return _calcConfigName;
        case -765894756:  // valueName
          return _valueName;
        case -926053069:  // properties
          return _properties;
        case 3433509:  // path
          return _path;
        case -441951604:  // targetId
          return _targetId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CalculationResultKey.Builder builder() {
      return new CalculationResultKey.Builder();
    }

    @Override
    public Class<? extends CalculationResultKey> beanType() {
      return CalculationResultKey.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code calcConfigName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> calcConfigName() {
      return _calcConfigName;
    }

    /**
     * The meta-property for the {@code valueName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> valueName() {
      return _valueName;
    }

    /**
     * The meta-property for the {@code properties} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ValueProperties> properties() {
      return _properties;
    }

    /**
     * The meta-property for the {@code path} property.
     * @return the meta-property, not null
     */
    public MetaProperty<List<String>> path() {
      return _path;
    }

    /**
     * The meta-property for the {@code targetId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ObjectId> targetId() {
      return _targetId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 875311394:  // calcConfigName
          return ((CalculationResultKey) bean).getCalcConfigName();
        case -765894756:  // valueName
          return ((CalculationResultKey) bean).getValueName();
        case -926053069:  // properties
          return ((CalculationResultKey) bean).getProperties();
        case 3433509:  // path
          return ((CalculationResultKey) bean).getPath();
        case -441951604:  // targetId
          return ((CalculationResultKey) bean).getTargetId();
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
   * The bean-builder for {@code CalculationResultKey}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CalculationResultKey> {

    private String _calcConfigName;
    private String _valueName;
    private ValueProperties _properties;
    private List<String> _path;
    private ObjectId _targetId;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CalculationResultKey beanToCopy) {
      this._calcConfigName = beanToCopy.getCalcConfigName();
      this._valueName = beanToCopy.getValueName();
      this._properties = beanToCopy.getProperties();
      this._path = (beanToCopy.getPath() != null ? new ArrayList<String>(beanToCopy.getPath()) : null);
      this._targetId = beanToCopy.getTargetId();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 875311394:  // calcConfigName
          return _calcConfigName;
        case -765894756:  // valueName
          return _valueName;
        case -926053069:  // properties
          return _properties;
        case 3433509:  // path
          return _path;
        case -441951604:  // targetId
          return _targetId;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 875311394:  // calcConfigName
          this._calcConfigName = (String) newValue;
          break;
        case -765894756:  // valueName
          this._valueName = (String) newValue;
          break;
        case -926053069:  // properties
          this._properties = (ValueProperties) newValue;
          break;
        case 3433509:  // path
          this._path = (List<String>) newValue;
          break;
        case -441951604:  // targetId
          this._targetId = (ObjectId) newValue;
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
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public CalculationResultKey build() {
      return new CalculationResultKey(
          _calcConfigName,
          _valueName,
          _properties,
          _path,
          _targetId);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code calcConfigName} property in the builder.
     * @param calcConfigName  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder calcConfigName(String calcConfigName) {
      JodaBeanUtils.notNull(calcConfigName, "calcConfigName");
      this._calcConfigName = calcConfigName;
      return this;
    }

    /**
     * Sets the {@code valueName} property in the builder.
     * @param valueName  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder valueName(String valueName) {
      JodaBeanUtils.notNull(valueName, "valueName");
      this._valueName = valueName;
      return this;
    }

    /**
     * Sets the {@code properties} property in the builder.
     * @param properties  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder properties(ValueProperties properties) {
      JodaBeanUtils.notNull(properties, "properties");
      this._properties = properties;
      return this;
    }

    /**
     * Sets the {@code path} property in the builder.
     * @param path  the new value
     * @return this, for chaining, not null
     */
    public Builder path(List<String> path) {
      this._path = path;
      return this;
    }

    /**
     * Sets the {@code targetId} property in the builder.
     * @param targetId  the new value
     * @return this, for chaining, not null
     */
    public Builder targetId(ObjectId targetId) {
      this._targetId = targetId;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(192);
      buf.append("CalculationResultKey.Builder{");
      buf.append("calcConfigName").append('=').append(JodaBeanUtils.toString(_calcConfigName)).append(',').append(' ');
      buf.append("valueName").append('=').append(JodaBeanUtils.toString(_valueName)).append(',').append(' ');
      buf.append("properties").append('=').append(JodaBeanUtils.toString(_properties)).append(',').append(' ');
      buf.append("path").append('=').append(JodaBeanUtils.toString(_path)).append(',').append(' ');
      buf.append("targetId").append('=').append(JodaBeanUtils.toString(_targetId));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
