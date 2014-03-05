/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

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
 * Request for searching for securities.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link SecurityHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class SecuritySearchRequest extends AbstractSearchRequest {

  /**
   * The set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   */
  @PropertyDefinition(set = "manual")
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
   * The security name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The security type, null to not match on type.
   */
  @PropertyDefinition
  private String _securityType;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private SecuritySearchSortOrder _sortOrder = SecuritySearchSortOrder.OBJECT_ID_ASC;
  /**
   * The depth of security data to return.
   * False will only return the basic information held in the {@code ManageableSecurity} class.
   * True will load the full security subclass for each returned security.
   * By default this is true returning all the data.
   */
  @PropertyDefinition
  private boolean _fullDetail = true;

  /**
   * Creates an instance.
   */
  public SecuritySearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param securityId  the security external identifier to search for, not null
   */
  public SecuritySearchRequest(ExternalId securityId) {
    addExternalId(securityId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param securityBundle  the security bundle to search for, not null
   */
  public SecuritySearchRequest(ExternalIdBundle securityBundle) {
    addExternalIds(securityBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security object identifier to the set.
   * 
   * @param securityId  the security object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable securityId) {
    ArgumentChecker.notNull(securityId, "securityId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(securityId.getObjectId());
  }

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

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof SecurityDocument == false) {
      return false;
    }
    SecurityDocument document = (SecurityDocument) obj;
    ManageableSecurity security = document.getSecurity();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(security.getExternalIdBundle()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), document.getName()) == false) {
      return false;
    }
    if (getSecurityType() != null && getSecurityType().equals(security.getSecurityType()) == false) {
      return false;
    }
    if (getExternalIdValue() != null) {
      for (ExternalId identifier : security.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdValue(), identifier.getValue()) == false) {
          return false;
        }
      }
    }
    if (getExternalIdScheme() != null) {
      for (ExternalId identifier : security.getExternalIdBundle()) {
        if (RegexUtils.wildcardMatch(getExternalIdScheme(), identifier.getScheme().getName()) == false) {
          return false;
        }
      }
    }
    if (getAttributes().size() > 0) {
      for (Entry<String, String> entry : getAttributes().entrySet()) {
        if (security.getAttributes().containsKey(entry.getKey()) == false) {
          return false;
        }
        String otherValue = security.getAttributes().get(entry.getKey());
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
   * The meta-bean for {@code SecuritySearchRequest}.
   * @return the meta-bean, not null
   */
  public static SecuritySearchRequest.Meta meta() {
    return SecuritySearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SecuritySearchRequest.Meta.INSTANCE);
  }

  @Override
  public SecuritySearchRequest.Meta metaBean() {
    return SecuritySearchRequest.Meta.INSTANCE;
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
   * Gets the security name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the security name, wildcards allowed, null to not match on name.
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
   * Gets the security type, null to not match on type.
   * @return the value of the property
   */
  public String getSecurityType() {
    return _securityType;
  }

  /**
   * Sets the security type, null to not match on type.
   * @param securityType  the new value of the property
   */
  public void setSecurityType(String securityType) {
    this._securityType = securityType;
  }

  /**
   * Gets the the {@code securityType} property.
   * @return the property, not null
   */
  public final Property<String> securityType() {
    return metaBean().securityType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public SecuritySearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(SecuritySearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<SecuritySearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the depth of security data to return.
   * False will only return the basic information held in the {@code ManageableSecurity} class.
   * True will load the full security subclass for each returned security.
   * By default this is true returning all the data.
   * @return the value of the property
   */
  public boolean isFullDetail() {
    return _fullDetail;
  }

  /**
   * Sets the depth of security data to return.
   * False will only return the basic information held in the {@code ManageableSecurity} class.
   * True will load the full security subclass for each returned security.
   * By default this is true returning all the data.
   * @param fullDetail  the new value of the property
   */
  public void setFullDetail(boolean fullDetail) {
    this._fullDetail = fullDetail;
  }

  /**
   * Gets the the {@code fullDetail} property.
   * False will only return the basic information held in the {@code ManageableSecurity} class.
   * True will load the full security subclass for each returned security.
   * By default this is true returning all the data.
   * @return the property, not null
   */
  public final Property<Boolean> fullDetail() {
    return metaBean().fullDetail().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SecuritySearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SecuritySearchRequest other = (SecuritySearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getExternalIdValue(), other.getExternalIdValue()) &&
          JodaBeanUtils.equal(getExternalIdScheme(), other.getExternalIdScheme()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getSecurityType(), other.getSecurityType()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          (isFullDetail() == other.isFullDetail()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    hash += hash * 31 + JodaBeanUtils.hashCode(isFullDetail());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("SecuritySearchRequest{");
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
    buf.append("securityType").append('=').append(JodaBeanUtils.toString(getSecurityType())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
    buf.append("fullDetail").append('=').append(JodaBeanUtils.toString(isFullDetail())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SecuritySearchRequest}.
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
        this, "objectIds", SecuritySearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", SecuritySearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code externalIdValue} property.
     */
    private final MetaProperty<String> _externalIdValue = DirectMetaProperty.ofReadWrite(
        this, "externalIdValue", SecuritySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code externalIdScheme} property.
     */
    private final MetaProperty<String> _externalIdScheme = DirectMetaProperty.ofReadWrite(
        this, "externalIdScheme", SecuritySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", SecuritySearchRequest.class, (Class) Map.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", SecuritySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code securityType} property.
     */
    private final MetaProperty<String> _securityType = DirectMetaProperty.ofReadWrite(
        this, "securityType", SecuritySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<SecuritySearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", SecuritySearchRequest.class, SecuritySearchSortOrder.class);
    /**
     * The meta-property for the {@code fullDetail} property.
     */
    private final MetaProperty<Boolean> _fullDetail = DirectMetaProperty.ofReadWrite(
        this, "fullDetail", SecuritySearchRequest.class, Boolean.TYPE);
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
        "securityType",
        "sortOrder",
        "fullDetail");

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
        case 808245914:  // securityType
          return _securityType;
        case -26774448:  // sortOrder
          return _sortOrder;
        case -1233600576:  // fullDetail
          return _fullDetail;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SecuritySearchRequest> builder() {
      return new DirectBeanBuilder<SecuritySearchRequest>(new SecuritySearchRequest());
    }

    @Override
    public Class<? extends SecuritySearchRequest> beanType() {
      return SecuritySearchRequest.class;
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
     * The meta-property for the {@code securityType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> securityType() {
      return _securityType;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SecuritySearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    /**
     * The meta-property for the {@code fullDetail} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> fullDetail() {
      return _fullDetail;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((SecuritySearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((SecuritySearchRequest) bean).getExternalIdSearch();
        case 2072311499:  // externalIdValue
          return ((SecuritySearchRequest) bean).getExternalIdValue();
        case -267027573:  // externalIdScheme
          return ((SecuritySearchRequest) bean).getExternalIdScheme();
        case 405645655:  // attributes
          return ((SecuritySearchRequest) bean).getAttributes();
        case 3373707:  // name
          return ((SecuritySearchRequest) bean).getName();
        case 808245914:  // securityType
          return ((SecuritySearchRequest) bean).getSecurityType();
        case -26774448:  // sortOrder
          return ((SecuritySearchRequest) bean).getSortOrder();
        case -1233600576:  // fullDetail
          return ((SecuritySearchRequest) bean).isFullDetail();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((SecuritySearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((SecuritySearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 2072311499:  // externalIdValue
          ((SecuritySearchRequest) bean).setExternalIdValue((String) newValue);
          return;
        case -267027573:  // externalIdScheme
          ((SecuritySearchRequest) bean).setExternalIdScheme((String) newValue);
          return;
        case 405645655:  // attributes
          ((SecuritySearchRequest) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 3373707:  // name
          ((SecuritySearchRequest) bean).setName((String) newValue);
          return;
        case 808245914:  // securityType
          ((SecuritySearchRequest) bean).setSecurityType((String) newValue);
          return;
        case -26774448:  // sortOrder
          ((SecuritySearchRequest) bean).setSortOrder((SecuritySearchSortOrder) newValue);
          return;
        case -1233600576:  // fullDetail
          ((SecuritySearchRequest) bean).setFullDetail((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SecuritySearchRequest) bean)._attributes, "attributes");
      JodaBeanUtils.notNull(((SecuritySearchRequest) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
