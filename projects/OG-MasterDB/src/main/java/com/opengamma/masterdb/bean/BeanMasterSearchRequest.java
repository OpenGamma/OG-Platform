/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to the fields necessary to search in a {@code DbBeanMaster}.
 */
@BeanDefinition
public class BeanMasterSearchRequest extends AbstractSearchRequest {

  /**
   * The set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   */
  @PropertyDefinition
  private List<ObjectId> _objectIds;
  /**
   * The security external identifiers to match, null to not match on security identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _externalIdSearch;
  /**
   * The external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _externalIdValue;
  /**
   * The external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   */
  @PropertyDefinition
  private String _externalIdScheme;
  /**
   * Map of attributes to search for.
   * The returned documents must match all of the specified attributes.
   * Wildcards are allowed for the values. Nulls are not allowed.
   */
  @PropertyDefinition
  private final Map<String, String> _attributes = Maps.newHashMap();
  /**
   * Map of indexed properties to search for.
   * The returned documents must match all of the specified properties.
   * Wildcards are allowed for the values. Nulls are not allowed.
   */
  @PropertyDefinition
  private final Map<String, String> _indexedProperties = Maps.newHashMap();
  /**
   * The descriptive name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The main type, null to not match on type.
   */
  @PropertyDefinition
  private Character _mainType;
  /**
   * The sub type, null to not match on type.
   */
  @PropertyDefinition
  private String _subType;
  /**
   * The actual Java type, normally just the short form, null to not match on type.
   */
  @PropertyDefinition
  private String _actualType;
  /**
   * The sort order SQL to use.
   */
  @PropertyDefinition
  private String _sortOrderSql;

  //-------------------------------------------------------------------------
  /**
   * Sets the set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   * 
   * @param securityIds  the new security identifiers, null clears the security id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> securityIds) {
    if (securityIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable securityId : securityIds) {
        _objectIds.add(securityId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security external identifier to the collection to search for.
   * Unless customized, the search will match.
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityId  the security key identifier to add, not null
   */
  public void addExternalId(ExternalId securityId) {
    ArgumentChecker.notNull(securityId, "securityId");
    addExternalIds(Arrays.asList(securityId));
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match.
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityIds  the security key identifiers to add, not null
   */
  public void addExternalIds(ExternalId... securityIds) {
    ArgumentChecker.notNull(securityIds, "securityIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(securityIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(securityIds));
    }
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match.
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityIds  the security key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> securityIds) {
    ArgumentChecker.notNull(securityIds, "securityIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(securityIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(securityIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch}.
   * 
   * @param type  the type to set, not null
   */
  public void setExternalIdSearchType(ExternalIdSearchType type) {
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setExternalIdSearch(getExternalIdSearch().withSearchType(type));
    }
  }

  /**
   * Adds a key-value pair to the set of attributes to search for.
   * <p>
   * Attributes are used to tag the object with additional information.
   * 
   * @param key  the key to add, not null
   * @param value  the value to add, not null
   */
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BeanMasterSearchRequest}.
   * @return the meta-bean, not null
   */
  public static BeanMasterSearchRequest.Meta meta() {
    return BeanMasterSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BeanMasterSearchRequest.Meta.INSTANCE);
  }

  @Override
  public BeanMasterSearchRequest.Meta metaBean() {
    return BeanMasterSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Sets the set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   * @param objectIds  the new value of the property
   */
  public void setObjectIds(List<ObjectId> objectIds) {
    this._objectIds = objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty set will return no securities.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security external identifiers to match, null to not match on security identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the security external identifiers to match, null to not match on security identifiers.
   * @param externalIdSearch  the new value of the property
   */
  public void setExternalIdSearch(ExternalIdSearch externalIdSearch) {
    this._externalIdSearch = externalIdSearch;
  }

  /**
   * Gets the the {@code externalIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> externalIdSearch() {
    return metaBean().externalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getExternalIdValue() {
    return _externalIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param externalIdValue  the new value of the property
   */
  public void setExternalIdValue(String externalIdValue) {
    this._externalIdValue = externalIdValue;
  }

  /**
   * Gets the the {@code externalIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> externalIdValue() {
    return metaBean().externalIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   * @return the value of the property
   */
  public String getExternalIdScheme() {
    return _externalIdScheme;
  }

  /**
   * Sets the external identifier scheme, matching against the <b>scheme</b> of the identifiers,
   * null not to match by identifier scheme. Wildcards are allowed.
   * @param externalIdScheme  the new value of the property
   */
  public void setExternalIdScheme(String externalIdScheme) {
    this._externalIdScheme = externalIdScheme;
  }

  /**
   * Gets the the {@code externalIdScheme} property.
   * null not to match by identifier scheme. Wildcards are allowed.
   * @return the property, not null
   */
  public final Property<String> externalIdScheme() {
    return metaBean().externalIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets map of attributes to search for.
   * The returned documents must match all of the specified attributes.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @return the value of the property, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets map of attributes to search for.
   * The returned documents must match all of the specified attributes.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @param attributes  the new value of the property, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * The returned documents must match all of the specified attributes.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets map of indexed properties to search for.
   * The returned documents must match all of the specified properties.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @return the value of the property, not null
   */
  public Map<String, String> getIndexedProperties() {
    return _indexedProperties;
  }

  /**
   * Sets map of indexed properties to search for.
   * The returned documents must match all of the specified properties.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @param indexedProperties  the new value of the property, not null
   */
  public void setIndexedProperties(Map<String, String> indexedProperties) {
    JodaBeanUtils.notNull(indexedProperties, "indexedProperties");
    this._indexedProperties.clear();
    this._indexedProperties.putAll(indexedProperties);
  }

  /**
   * Gets the the {@code indexedProperties} property.
   * The returned documents must match all of the specified properties.
   * Wildcards are allowed for the values. Nulls are not allowed.
   * @return the property, not null
   */
  public final Property<Map<String, String>> indexedProperties() {
    return metaBean().indexedProperties().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the descriptive name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the descriptive name, wildcards allowed, null to not match on name.
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
   * Gets the main type, null to not match on type.
   * @return the value of the property
   */
  public Character getMainType() {
    return _mainType;
  }

  /**
   * Sets the main type, null to not match on type.
   * @param mainType  the new value of the property
   */
  public void setMainType(Character mainType) {
    this._mainType = mainType;
  }

  /**
   * Gets the the {@code mainType} property.
   * @return the property, not null
   */
  public final Property<Character> mainType() {
    return metaBean().mainType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sub type, null to not match on type.
   * @return the value of the property
   */
  public String getSubType() {
    return _subType;
  }

  /**
   * Sets the sub type, null to not match on type.
   * @param subType  the new value of the property
   */
  public void setSubType(String subType) {
    this._subType = subType;
  }

  /**
   * Gets the the {@code subType} property.
   * @return the property, not null
   */
  public final Property<String> subType() {
    return metaBean().subType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the actual Java type, normally just the short form, null to not match on type.
   * @return the value of the property
   */
  public String getActualType() {
    return _actualType;
  }

  /**
   * Sets the actual Java type, normally just the short form, null to not match on type.
   * @param actualType  the new value of the property
   */
  public void setActualType(String actualType) {
    this._actualType = actualType;
  }

  /**
   * Gets the the {@code actualType} property.
   * @return the property, not null
   */
  public final Property<String> actualType() {
    return metaBean().actualType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order SQL to use.
   * @return the value of the property
   */
  public String getSortOrderSql() {
    return _sortOrderSql;
  }

  /**
   * Sets the sort order SQL to use.
   * @param sortOrderSql  the new value of the property
   */
  public void setSortOrderSql(String sortOrderSql) {
    this._sortOrderSql = sortOrderSql;
  }

  /**
   * Gets the the {@code sortOrderSql} property.
   * @return the property, not null
   */
  public final Property<String> sortOrderSql() {
    return metaBean().sortOrderSql().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public BeanMasterSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BeanMasterSearchRequest other = (BeanMasterSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getExternalIdValue(), other.getExternalIdValue()) &&
          JodaBeanUtils.equal(getExternalIdScheme(), other.getExternalIdScheme()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getIndexedProperties(), other.getIndexedProperties()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getMainType(), other.getMainType()) &&
          JodaBeanUtils.equal(getSubType(), other.getSubType()) &&
          JodaBeanUtils.equal(getActualType(), other.getActualType()) &&
          JodaBeanUtils.equal(getSortOrderSql(), other.getSortOrderSql()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdSearch());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdValue());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdScheme());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndexedProperties());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMainType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSubType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getActualType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSortOrderSql());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(384);
    buf.append("BeanMasterSearchRequest{");
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
    buf.append("objectIds").append('=').append(JodaBeanUtils.toString(getObjectIds())).append(',').append(' ');
    buf.append("externalIdSearch").append('=').append(JodaBeanUtils.toString(getExternalIdSearch())).append(',').append(' ');
    buf.append("externalIdValue").append('=').append(JodaBeanUtils.toString(getExternalIdValue())).append(',').append(' ');
    buf.append("externalIdScheme").append('=').append(JodaBeanUtils.toString(getExternalIdScheme())).append(',').append(' ');
    buf.append("attributes").append('=').append(JodaBeanUtils.toString(getAttributes())).append(',').append(' ');
    buf.append("indexedProperties").append('=').append(JodaBeanUtils.toString(getIndexedProperties())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("mainType").append('=').append(JodaBeanUtils.toString(getMainType())).append(',').append(' ');
    buf.append("subType").append('=').append(JodaBeanUtils.toString(getSubType())).append(',').append(' ');
    buf.append("actualType").append('=').append(JodaBeanUtils.toString(getActualType())).append(',').append(' ');
    buf.append("sortOrderSql").append('=').append(JodaBeanUtils.toString(getSortOrderSql())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BeanMasterSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", BeanMasterSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", BeanMasterSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code externalIdValue} property.
     */
    private final MetaProperty<String> _externalIdValue = DirectMetaProperty.ofReadWrite(
        this, "externalIdValue", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code externalIdScheme} property.
     */
    private final MetaProperty<String> _externalIdScheme = DirectMetaProperty.ofReadWrite(
        this, "externalIdScheme", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", BeanMasterSearchRequest.class, (Class) Map.class);
    /**
     * The meta-property for the {@code indexedProperties} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _indexedProperties = DirectMetaProperty.ofReadWrite(
        this, "indexedProperties", BeanMasterSearchRequest.class, (Class) Map.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code mainType} property.
     */
    private final MetaProperty<Character> _mainType = DirectMetaProperty.ofReadWrite(
        this, "mainType", BeanMasterSearchRequest.class, Character.class);
    /**
     * The meta-property for the {@code subType} property.
     */
    private final MetaProperty<String> _subType = DirectMetaProperty.ofReadWrite(
        this, "subType", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code actualType} property.
     */
    private final MetaProperty<String> _actualType = DirectMetaProperty.ofReadWrite(
        this, "actualType", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrderSql} property.
     */
    private final MetaProperty<String> _sortOrderSql = DirectMetaProperty.ofReadWrite(
        this, "sortOrderSql", BeanMasterSearchRequest.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "externalIdSearch",
        "externalIdValue",
        "externalIdScheme",
        "attributes",
        "indexedProperties",
        "name",
        "mainType",
        "subType",
        "actualType",
        "sortOrderSql");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return _objectIds;
        case -265376882:  // externalIdSearch
          return _externalIdSearch;
        case 2072311499:  // externalIdValue
          return _externalIdValue;
        case -267027573:  // externalIdScheme
          return _externalIdScheme;
        case 405645655:  // attributes
          return _attributes;
        case 2013657348:  // indexedProperties
          return _indexedProperties;
        case 3373707:  // name
          return _name;
        case -8420205:  // mainType
          return _mainType;
        case -1868521062:  // subType
          return _subType;
        case -785631768:  // actualType
          return _actualType;
        case 1226420062:  // sortOrderSql
          return _sortOrderSql;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BeanMasterSearchRequest> builder() {
      return new DirectBeanBuilder<BeanMasterSearchRequest>(new BeanMasterSearchRequest());
    }

    @Override
    public Class<? extends BeanMasterSearchRequest> beanType() {
      return BeanMasterSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code objectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> objectIds() {
      return _objectIds;
    }

    /**
     * The meta-property for the {@code externalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> externalIdSearch() {
      return _externalIdSearch;
    }

    /**
     * The meta-property for the {@code externalIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> externalIdValue() {
      return _externalIdValue;
    }

    /**
     * The meta-property for the {@code externalIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> externalIdScheme() {
      return _externalIdScheme;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code indexedProperties} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> indexedProperties() {
      return _indexedProperties;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code mainType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Character> mainType() {
      return _mainType;
    }

    /**
     * The meta-property for the {@code subType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> subType() {
      return _subType;
    }

    /**
     * The meta-property for the {@code actualType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> actualType() {
      return _actualType;
    }

    /**
     * The meta-property for the {@code sortOrderSql} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> sortOrderSql() {
      return _sortOrderSql;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((BeanMasterSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((BeanMasterSearchRequest) bean).getExternalIdSearch();
        case 2072311499:  // externalIdValue
          return ((BeanMasterSearchRequest) bean).getExternalIdValue();
        case -267027573:  // externalIdScheme
          return ((BeanMasterSearchRequest) bean).getExternalIdScheme();
        case 405645655:  // attributes
          return ((BeanMasterSearchRequest) bean).getAttributes();
        case 2013657348:  // indexedProperties
          return ((BeanMasterSearchRequest) bean).getIndexedProperties();
        case 3373707:  // name
          return ((BeanMasterSearchRequest) bean).getName();
        case -8420205:  // mainType
          return ((BeanMasterSearchRequest) bean).getMainType();
        case -1868521062:  // subType
          return ((BeanMasterSearchRequest) bean).getSubType();
        case -785631768:  // actualType
          return ((BeanMasterSearchRequest) bean).getActualType();
        case 1226420062:  // sortOrderSql
          return ((BeanMasterSearchRequest) bean).getSortOrderSql();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((BeanMasterSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((BeanMasterSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 2072311499:  // externalIdValue
          ((BeanMasterSearchRequest) bean).setExternalIdValue((String) newValue);
          return;
        case -267027573:  // externalIdScheme
          ((BeanMasterSearchRequest) bean).setExternalIdScheme((String) newValue);
          return;
        case 405645655:  // attributes
          ((BeanMasterSearchRequest) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 2013657348:  // indexedProperties
          ((BeanMasterSearchRequest) bean).setIndexedProperties((Map<String, String>) newValue);
          return;
        case 3373707:  // name
          ((BeanMasterSearchRequest) bean).setName((String) newValue);
          return;
        case -8420205:  // mainType
          ((BeanMasterSearchRequest) bean).setMainType((Character) newValue);
          return;
        case -1868521062:  // subType
          ((BeanMasterSearchRequest) bean).setSubType((String) newValue);
          return;
        case -785631768:  // actualType
          ((BeanMasterSearchRequest) bean).setActualType((String) newValue);
          return;
        case 1226420062:  // sortOrderSql
          ((BeanMasterSearchRequest) bean).setSortOrderSql((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((BeanMasterSearchRequest) bean)._attributes, "attributes");
      JodaBeanUtils.notNull(((BeanMasterSearchRequest) bean)._indexedProperties, "indexedProperties");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
