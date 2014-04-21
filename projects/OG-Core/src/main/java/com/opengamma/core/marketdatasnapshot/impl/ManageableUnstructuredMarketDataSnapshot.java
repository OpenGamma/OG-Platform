/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Mutable snapshot of market data.
 * <p>
 * This class is mutable and not thread-safe.
 */
@BeanDefinition
public class ManageableUnstructuredMarketDataSnapshot implements Bean, UnstructuredMarketDataSnapshot, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The values.
   * <p>
   * Note the use of {@link LinkedHashMap} to preserve the ordering of items.
   * This is used by yield curve based logic as the ordering of the market data
   * may have meaning to someone manipulating the* snapshot (PLAT-1889).
   */
  @PropertyDefinition(get = "manual", set = "manual")
  private final Map<ExternalIdBundle, Map<String, ValueSnapshot>> _values = Maps.newLinkedHashMap();
  /**
   * The index for lookup operations.
   */
  // not a property
  private final Map<ExternalId, Map<String, ExternalIdBundle>> _index = Maps.newHashMap();

  /**
   * Creates an empty snapshot.
   */
  public ManageableUnstructuredMarketDataSnapshot() {
  }

  /**
   * Creates a snapshot initialised from a template.
   * 
   * @param copyFrom the template to initialise from
   */
  public ManageableUnstructuredMarketDataSnapshot(final UnstructuredMarketDataSnapshot copyFrom) {
    for (final ExternalIdBundle target : copyFrom.getTargets()) {
      final Map<String, ValueSnapshot> values = copyFrom.getTargetValues(target);
      if (values != null) {
        _values.put(target, new LinkedHashMap<String, ValueSnapshot>(values));
        for (final ExternalId identifier : target) {
          Map<String, ExternalIdBundle> index = _index.get(identifier);
          if (index == null) {
            index = new HashMap<String, ExternalIdBundle>();
            _index.put(identifier, index);
          }
          for (final String value : values.keySet()) {
            index.put(value, target);
          }
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  protected ValueSnapshot getImpl(final ExternalIdBundle identifiers, final String valueName) {
    final Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      return values.get(valueName);
    } else {
      return null;
    }
  }

  @Override
  public boolean isEmpty() {
    return _values.isEmpty();
  }

  @Override
  public ValueSnapshot getValue(final ExternalId identifier, final String valueName) {
    final Map<String, ExternalIdBundle> index = _index.get(identifier);
    if (index != null) {
      final ExternalIdBundle key = index.get(valueName);
      if (key != null) {
        return getImpl(key, valueName);
      }
    }
    return null;
  }

  @Override
  public ValueSnapshot getValue(final ExternalIdBundle identifiers, final String valueName) {
    ValueSnapshot value = getImpl(identifiers, valueName);
    if (value != null) {
      return value;
    } else {
      for (final ExternalId identifier : identifiers) {
        value = getValue(identifier, valueName);
        if (value != null) {
          return value;
        }
      }
      return null;
    }
  }

  @Override
  public Set<ExternalIdBundle> getTargets() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  @Override
  public Map<String, ValueSnapshot> getTargetValues(final ExternalIdBundle identifiers) {
    final Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      return Collections.unmodifiableMap(values);
    } else {
      return null;
    }
  }

  /**
   * Stores a value against the target identifier, replacing any previous association
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   * @param value the value to associate, not null
   */
  public void putValue(final ExternalId identifier, final String valueName, final ValueSnapshot value) {
    Map<String, ExternalIdBundle> index = _index.get(identifier);
    ExternalIdBundle key;
    if (index == null) {
      index = new HashMap<String, ExternalIdBundle>();
      _index.put(identifier, index);
      key = ExternalIdBundle.of(identifier);
      index.put(valueName, key);
    } else {
      key = index.get(valueName);
      if (key == null) {
        key = ExternalIdBundle.of(identifier);
        index.put(valueName, key);
      }
    }
    Map<String, ValueSnapshot> values = _values.get(key);
    if (values == null) {
      values = new HashMap<String, ValueSnapshot>();
      _values.put(key, values);
    }
    values.put(valueName, value);
  }

  /**
   * Stores a value against the target identifiers. Any values previously stored against any of the identifiers in the bundle will be replaced.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   * @param value the value to associate, not null
   */
  public void putValue(final ExternalIdBundle identifiers, final String valueName, final ValueSnapshot value) {
    Map<String, ValueSnapshot> values = _values.get(identifiers);
    if (values != null) {
      if (values.put(valueName, value) != null) {
        // Already have a value for this bundle/valueName pair so don't need to update the index
        return;
      }
    } else {
      values = new HashMap<String, ValueSnapshot>();
      _values.put(identifiers, values);
      values.put(valueName, value);
    }
    removeValue(identifiers, valueName);
    for (final ExternalId identifier : identifiers) {
      Map<String, ExternalIdBundle> index = _index.get(identifier);
      if (index == null) {
        index = new HashMap<String, ExternalIdBundle>();
        _index.put(identifier, index);
      }
      index.put(valueName, identifiers);
    }
  }

  /**
   * Removes a value held against a target identifier.
   * 
   * @param identifier the target identifier, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalId identifier, final String valueName) {
    final Map<String, ExternalIdBundle> index = _index.get(identifier);
    if (index != null) {
      final ExternalIdBundle key = index.remove(valueName);
      if (key != null) {
        if (index.isEmpty()) {
          _index.remove(identifier);
        }
        final Map<String, ValueSnapshot> values = _values.get(key);
        if (values != null) {
          if (values.remove(valueName) != null) {
            if (values.isEmpty()) {
              _values.remove(key);
            }
          }
        }
        removeValue(key, valueName);
      }
    }
  }

  /**
   * Removes a value held against a target identifier bundle.
   * 
   * @param identifiers the target identifiers, not null
   * @param valueName the value name, not null
   */
  public void removeValue(final ExternalIdBundle identifiers, final String valueName) {
    for (final ExternalId identifier : identifiers) {
      removeValue(identifier, valueName);
    }
  }

  /**
   * Gets the values.
   * <p>
   * Note the use of {@link LinkedHashMap} to preserve the ordering of items.
   * This is used by yield curve based logic as the ordering of the market data
   * may have meaning to someone manipulating the* snapshot (PLAT-1889).
   * @return the value of the property
   */
  private Map<ExternalIdBundle, Map<String, ValueSnapshot>> getValues() {
    return _values;
  }

  /**
   * Sets the values.
   * <p>
   * Note the use of {@link LinkedHashMap} to preserve the ordering of items.
   * This is used by yield curve based logic as the ordering of the market data
   * may have meaning to someone manipulating the* snapshot (PLAT-1889).
   * @param values  the new value of the property
   */
  private void setValues(Map<ExternalIdBundle, Map<String, ValueSnapshot>> values) {
    this._values.clear();
    for (Entry<ExternalIdBundle, Map<String, ValueSnapshot>> entry : values.entrySet()) {
      for (Entry<String, ValueSnapshot> innerEntry : entry.getValue().entrySet()) {
        putValue(entry.getKey(), innerEntry.getKey(), innerEntry.getValue());
      }
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableUnstructuredMarketDataSnapshot}.
   * @return the meta-bean, not null
   */
  public static ManageableUnstructuredMarketDataSnapshot.Meta meta() {
    return ManageableUnstructuredMarketDataSnapshot.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableUnstructuredMarketDataSnapshot.Meta.INSTANCE);
  }

  @Override
  public ManageableUnstructuredMarketDataSnapshot.Meta metaBean() {
    return ManageableUnstructuredMarketDataSnapshot.Meta.INSTANCE;
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
   * Gets the the {@code values} property.
   * <p>
   * Note the use of {@link LinkedHashMap} to preserve the ordering of items.
   * This is used by yield curve based logic as the ordering of the market data
   * may have meaning to someone manipulating the* snapshot (PLAT-1889).
   * @return the property, not null
   */
  public final Property<Map<ExternalIdBundle, Map<String, ValueSnapshot>>> values() {
    return metaBean().values().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableUnstructuredMarketDataSnapshot clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableUnstructuredMarketDataSnapshot other = (ManageableUnstructuredMarketDataSnapshot) obj;
      return JodaBeanUtils.equal(getValues(), other.getValues());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getValues());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ManageableUnstructuredMarketDataSnapshot{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("values").append('=').append(JodaBeanUtils.toString(getValues())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableUnstructuredMarketDataSnapshot}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalIdBundle, Map<String, ValueSnapshot>>> _values = DirectMetaProperty.ofReadWrite(
        this, "values", ManageableUnstructuredMarketDataSnapshot.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "values");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return _values;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableUnstructuredMarketDataSnapshot> builder() {
      return new DirectBeanBuilder<ManageableUnstructuredMarketDataSnapshot>(new ManageableUnstructuredMarketDataSnapshot());
    }

    @Override
    public Class<? extends ManageableUnstructuredMarketDataSnapshot> beanType() {
      return ManageableUnstructuredMarketDataSnapshot.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<ExternalIdBundle, Map<String, ValueSnapshot>>> values() {
      return _values;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          return ((ManageableUnstructuredMarketDataSnapshot) bean).getValues();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -823812830:  // values
          ((ManageableUnstructuredMarketDataSnapshot) bean).setValues((Map<ExternalIdBundle, Map<String, ValueSnapshot>>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableUnstructuredMarketDataSnapshot) bean)._values, "values");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
