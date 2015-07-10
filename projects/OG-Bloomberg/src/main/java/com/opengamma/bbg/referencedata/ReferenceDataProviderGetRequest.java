/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Request to get one or more historical time-series.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class ReferenceDataProviderGetRequest extends DirectBean {

  /**
   * The set of external identifiers to get.
   * For Bloomberg, these are typically BUIDs.
   */
  @PropertyDefinition
  private final Set<String> _identifiers = Sets.newHashSet();
  /**
   * The data fields to fetch.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<String> _fields = Sets.newHashSet();
  /**
   * The use cache flag.
   * True will use any available caching.
   * False will always try to call the underlying data source.
   */
  @PropertyDefinition
  private boolean _useCache;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance to get reference data.
   * 
   * @param identifier  the identifiers to query, not null
   * @param dataField  the data field to query, not null
   * @param useCache  true to use the cache, false to force query of the data source
   * @return the request, not null
   */
  public static ReferenceDataProviderGetRequest createGet(
      String identifier, String dataField, boolean useCache) {
    return createGet(Collections.singleton(identifier), Collections.singleton(dataField), useCache);
  }

  /**
   * Obtains an instance to get reference data.
   * 
   * @param identifier  the identifiers to query, not null
   * @param fields  the data fields to query, not null
   * @param useCache  true to use the cache, false to force query of the data source
   * @return the request, not null
   */
  public static ReferenceDataProviderGetRequest createGet(
      String identifier, Iterable<String> fields, boolean useCache) {
    return createGet(Collections.singleton(identifier), fields, useCache);
  }

  /**
   * Obtains an instance to get reference data.
   * 
   * @param identifiers  the identifiers to query, not null
   * @param fields  the data fields to query, not null
   * @param useCache  true to use the cache, false to force query of the data source
   * @return the request, not null
   */
  public static ReferenceDataProviderGetRequest createGet(
      Iterable<String> identifiers, Iterable<String> fields, boolean useCache) {
    ReferenceDataProviderGetRequest request = new ReferenceDataProviderGetRequest();
    request.addIdentifiers(identifiers);
    request.addFields(fields);
    request.setUseCache(useCache);
    return request;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  protected ReferenceDataProviderGetRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an array of identifiers to the collection to get.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void addIdentifiers(String... identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    List<String> list = new ArrayList<String>();
    for (String identifier : identifiers) {
      list.add(identifier);
    }
    getIdentifiers().addAll(list);
  }

  /**
   * Adds a collection of identifiers to the collection to get.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void addIdentifiers(Iterable<String> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    Iterables.addAll(getIdentifiers(), identifiers);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an array of fields to the collection.
   * 
   * @param fields  the fields to add, not null
   */
  public void addFields(String... fields) {
    ArgumentChecker.notNull(fields, "fields");
    List<String> list = new ArrayList<String>();
    for (String field : fields) {
      list.add(field);
    }
    getFields().addAll(list);
  }

  /**
   * Adds a collection of fields to the collection.
   * 
   * @param fields  the fields to add, not null
   */
  public void addFields(Iterable<String> fields) {
    ArgumentChecker.notNull(fields, "fields");
    Iterables.addAll(getFields(), fields);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ReferenceDataProviderGetRequest}.
   * @return the meta-bean, not null
   */
  public static ReferenceDataProviderGetRequest.Meta meta() {
    return ReferenceDataProviderGetRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ReferenceDataProviderGetRequest.Meta.INSTANCE);
  }

  @Override
  public ReferenceDataProviderGetRequest.Meta metaBean() {
    return ReferenceDataProviderGetRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of external identifiers to get.
   * For Bloomberg, these are typically BUIDs.
   * @return the value of the property, not null
   */
  public Set<String> getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets the set of external identifiers to get.
   * For Bloomberg, these are typically BUIDs.
   * @param identifiers  the new value of the property, not null
   */
  public void setIdentifiers(Set<String> identifiers) {
    JodaBeanUtils.notNull(identifiers, "identifiers");
    this._identifiers.clear();
    this._identifiers.addAll(identifiers);
  }

  /**
   * Gets the the {@code identifiers} property.
   * For Bloomberg, these are typically BUIDs.
   * @return the property, not null
   */
  public final Property<Set<String>> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data fields to fetch.
   * @return the value of the property, not null
   */
  public Set<String> getFields() {
    return _fields;
  }

  /**
   * Sets the data fields to fetch.
   * @param fields  the new value of the property, not null
   */
  public void setFields(Set<String> fields) {
    JodaBeanUtils.notNull(fields, "fields");
    this._fields.clear();
    this._fields.addAll(fields);
  }

  /**
   * Gets the the {@code fields} property.
   * @return the property, not null
   */
  public final Property<Set<String>> fields() {
    return metaBean().fields().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the use cache flag.
   * True will use any available caching.
   * False will always try to call the underlying data source.
   * @return the value of the property
   */
  public boolean isUseCache() {
    return _useCache;
  }

  /**
   * Sets the use cache flag.
   * True will use any available caching.
   * False will always try to call the underlying data source.
   * @param useCache  the new value of the property
   */
  public void setUseCache(boolean useCache) {
    this._useCache = useCache;
  }

  /**
   * Gets the the {@code useCache} property.
   * True will use any available caching.
   * False will always try to call the underlying data source.
   * @return the property, not null
   */
  public final Property<Boolean> useCache() {
    return metaBean().useCache().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ReferenceDataProviderGetRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ReferenceDataProviderGetRequest other = (ReferenceDataProviderGetRequest) obj;
      return JodaBeanUtils.equal(getIdentifiers(), other.getIdentifiers()) &&
          JodaBeanUtils.equal(getFields(), other.getFields()) &&
          (isUseCache() == other.isUseCache());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getIdentifiers());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFields());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseCache());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ReferenceDataProviderGetRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("identifiers").append('=').append(JodaBeanUtils.toString(getIdentifiers())).append(',').append(' ');
    buf.append("fields").append('=').append(JodaBeanUtils.toString(getFields())).append(',').append(' ');
    buf.append("useCache").append('=').append(JodaBeanUtils.toString(isUseCache())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ReferenceDataProviderGetRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code identifiers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _identifiers = DirectMetaProperty.ofReadWrite(
        this, "identifiers", ReferenceDataProviderGetRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code fields} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _fields = DirectMetaProperty.ofReadWrite(
        this, "fields", ReferenceDataProviderGetRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code useCache} property.
     */
    private final MetaProperty<Boolean> _useCache = DirectMetaProperty.ofReadWrite(
        this, "useCache", ReferenceDataProviderGetRequest.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "identifiers",
        "fields",
        "useCache");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1368189162:  // identifiers
          return _identifiers;
        case -1274708295:  // fields
          return _fields;
        case -309504453:  // useCache
          return _useCache;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ReferenceDataProviderGetRequest> builder() {
      return new DirectBeanBuilder<ReferenceDataProviderGetRequest>(new ReferenceDataProviderGetRequest());
    }

    @Override
    public Class<? extends ReferenceDataProviderGetRequest> beanType() {
      return ReferenceDataProviderGetRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code fields} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> fields() {
      return _fields;
    }

    /**
     * The meta-property for the {@code useCache} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useCache() {
      return _useCache;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1368189162:  // identifiers
          return ((ReferenceDataProviderGetRequest) bean).getIdentifiers();
        case -1274708295:  // fields
          return ((ReferenceDataProviderGetRequest) bean).getFields();
        case -309504453:  // useCache
          return ((ReferenceDataProviderGetRequest) bean).isUseCache();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1368189162:  // identifiers
          ((ReferenceDataProviderGetRequest) bean).setIdentifiers((Set<String>) newValue);
          return;
        case -1274708295:  // fields
          ((ReferenceDataProviderGetRequest) bean).setFields((Set<String>) newValue);
          return;
        case -309504453:  // useCache
          ((ReferenceDataProviderGetRequest) bean).setUseCache((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ReferenceDataProviderGetRequest) bean)._identifiers, "identifiers");
      JodaBeanUtils.notNull(((ReferenceDataProviderGetRequest) bean)._fields, "fields");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
