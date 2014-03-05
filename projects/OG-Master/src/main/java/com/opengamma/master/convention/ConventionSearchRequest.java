/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for conventions.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link ConventionHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class ConventionSearchRequest extends AbstractSearchRequest {

  /**
   * The set of convention object identifiers, null to not limit by convention object identifiers.
   * Note that an empty set will return no conventions.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The convention external identifiers to match, null to not match on convention identifiers.
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
   * The convention name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The convention type, null to not match on type.
   */
  @PropertyDefinition
  private ConventionType _conventionType;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private ConventionSearchSortOrder _sortOrder = ConventionSearchSortOrder.OBJECT_ID_ASC;

  /**
   * Creates an instance.
   */
  public ConventionSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param conventionId  the convention external identifier to search for, not null
   */
  public ConventionSearchRequest(ExternalId conventionId) {
    addExternalId(conventionId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param conventionBundle  the convention external identifiers to search for, not null
   */
  public ConventionSearchRequest(ExternalIdBundle conventionBundle) {
    addExternalIds(conventionBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single convention object identifier to the set.
   * 
   * @param conventionId  the convention object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable conventionId) {
    ArgumentChecker.notNull(conventionId, "conventionId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(conventionId.getObjectId());
  }

  /**
   * Sets the set of convention object identifiers, null to not limit by convention object identifiers.
   * Note that an empty set will return no conventions.
   * 
   * @param conventionIds  the new convention identifiers, null clears the convention id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> conventionIds) {
    if (conventionIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable conventionId : conventionIds) {
        _objectIds.add(conventionId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single convention external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param conventionId  the convention key identifier to add, not null
   */
  public void addExternalId(ExternalId conventionId) {
    ArgumentChecker.notNull(conventionId, "conventionId");
    addExternalIds(Arrays.asList(conventionId));
  }

  /**
   * Adds a collection of convention external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param conventionIds  the convention key identifiers to add, not null
   */
  public void addExternalIds(ExternalId... conventionIds) {
    ArgumentChecker.notNull(conventionIds, "conventionIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(conventionIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(conventionIds));
    }
  }

  /**
   * Adds a collection of convention external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param conventionIds  the convention key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> conventionIds) {
    ArgumentChecker.notNull(conventionIds, "conventionIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(conventionIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(conventionIds));
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

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof ConventionDocument == false) {
      return false;
    }
    final ConventionDocument document = (ConventionDocument) obj;
    final ManageableConvention convention = document.getConvention();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(convention.getExternalIdBundle()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), convention.getName()) == false) {
      return false;
    }
    if (getExternalIdValue() != null) {
      for (ExternalId identifier : convention.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdValue(), identifier.getValue()) == false) {
          return false;
        }
      }
    }
    if (getExternalIdScheme() != null) {
      for (ExternalId identifier : convention.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdScheme(), identifier.getScheme().getName()) == false) {
          return false;
        }
      }
    }
    if (getAttributes().size() > 0) {
      for (Entry<String, String> entry : getAttributes().entrySet()) {
        if (convention.getAttributes().containsKey(entry.getKey()) == false) {
          return false;
        }
        String otherValue = convention.getAttributes().get(entry.getKey());
        if (RegexUtils.wildcardMatch(entry.getValue(), otherValue) == false) {
          return false;
        }
      }
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConventionSearchRequest}.
   * @return the meta-bean, not null
   */
  public static ConventionSearchRequest.Meta meta() {
    return ConventionSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ConventionSearchRequest.Meta.INSTANCE);
  }

  @Override
  public ConventionSearchRequest.Meta metaBean() {
    return ConventionSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of convention object identifiers, null to not limit by convention object identifiers.
   * Note that an empty set will return no conventions.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty set will return no conventions.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the convention external identifiers to match, null to not match on convention identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the convention external identifiers to match, null to not match on convention identifiers.
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
   * Gets the convention name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the convention name, wildcards allowed, null to not match on name.
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
   * Gets the convention type, null to not match on type.
   * @return the value of the property
   */
  public ConventionType getConventionType() {
    return _conventionType;
  }

  /**
   * Sets the convention type, null to not match on type.
   * @param conventionType  the new value of the property
   */
  public void setConventionType(ConventionType conventionType) {
    this._conventionType = conventionType;
  }

  /**
   * Gets the the {@code conventionType} property.
   * @return the property, not null
   */
  public final Property<ConventionType> conventionType() {
    return metaBean().conventionType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public ConventionSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(ConventionSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<ConventionSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ConventionSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ConventionSearchRequest other = (ConventionSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getExternalIdValue(), other.getExternalIdValue()) &&
          JodaBeanUtils.equal(getExternalIdScheme(), other.getExternalIdScheme()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getConventionType(), other.getConventionType()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdScheme());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getConventionType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("ConventionSearchRequest{");
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
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("conventionType").append('=').append(JodaBeanUtils.toString(getConventionType())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConventionSearchRequest}.
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
        this, "objectIds", ConventionSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", ConventionSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code externalIdValue} property.
     */
    private final MetaProperty<String> _externalIdValue = DirectMetaProperty.ofReadWrite(
        this, "externalIdValue", ConventionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code externalIdScheme} property.
     */
    private final MetaProperty<String> _externalIdScheme = DirectMetaProperty.ofReadWrite(
        this, "externalIdScheme", ConventionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ConventionSearchRequest.class, (Class) Map.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ConventionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code conventionType} property.
     */
    private final MetaProperty<ConventionType> _conventionType = DirectMetaProperty.ofReadWrite(
        this, "conventionType", ConventionSearchRequest.class, ConventionType.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<ConventionSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", ConventionSearchRequest.class, ConventionSearchSortOrder.class);
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
        "name",
        "conventionType",
        "sortOrder");

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
        case 3373707:  // name
          return _name;
        case 1372339787:  // conventionType
          return _conventionType;
        case -26774448:  // sortOrder
          return _sortOrder;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ConventionSearchRequest> builder() {
      return new DirectBeanBuilder<ConventionSearchRequest>(new ConventionSearchRequest());
    }

    @Override
    public Class<? extends ConventionSearchRequest> beanType() {
      return ConventionSearchRequest.class;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code conventionType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionType> conventionType() {
      return _conventionType;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConventionSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((ConventionSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((ConventionSearchRequest) bean).getExternalIdSearch();
        case 2072311499:  // externalIdValue
          return ((ConventionSearchRequest) bean).getExternalIdValue();
        case -267027573:  // externalIdScheme
          return ((ConventionSearchRequest) bean).getExternalIdScheme();
        case 405645655:  // attributes
          return ((ConventionSearchRequest) bean).getAttributes();
        case 3373707:  // name
          return ((ConventionSearchRequest) bean).getName();
        case 1372339787:  // conventionType
          return ((ConventionSearchRequest) bean).getConventionType();
        case -26774448:  // sortOrder
          return ((ConventionSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((ConventionSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((ConventionSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 2072311499:  // externalIdValue
          ((ConventionSearchRequest) bean).setExternalIdValue((String) newValue);
          return;
        case -267027573:  // externalIdScheme
          ((ConventionSearchRequest) bean).setExternalIdScheme((String) newValue);
          return;
        case 405645655:  // attributes
          ((ConventionSearchRequest) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 3373707:  // name
          ((ConventionSearchRequest) bean).setName((String) newValue);
          return;
        case 1372339787:  // conventionType
          ((ConventionSearchRequest) bean).setConventionType((ConventionType) newValue);
          return;
        case -26774448:  // sortOrder
          ((ConventionSearchRequest) bean).setSortOrder((ConventionSearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ConventionSearchRequest) bean)._attributes, "attributes");
      JodaBeanUtils.notNull(((ConventionSearchRequest) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
