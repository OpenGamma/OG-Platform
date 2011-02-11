/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
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
  private List<ObjectIdentifier> _securityIds;
  /**
   * The security keys to match, null to not match on security keys.
   */
  @PropertyDefinition
  private IdentifierSearch _securityKeys;
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
   * @param securityKey  the security key identifier to search for, not null
   */
  public SecuritySearchRequest(Identifier securityKey) {
    addSecurityKey(securityKey);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param securityKeys  the security key identifiers to search for, not null
   */
  public SecuritySearchRequest(IdentifierBundle securityKeys) {
    addSecurityKeys(securityKeys);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security object identifier to the set.
   * 
   * @param securityId  the security object identifier to add, not null
   */
  public void addSecurityId(ObjectIdentifiable securityId) {
    ArgumentChecker.notNull(securityId, "securityId");
    if (_securityIds == null) {
      _securityIds = new ArrayList<ObjectIdentifier>();
    }
    _securityIds.add(securityId.getObjectId());
  }

  /**
   * Sets the set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   * 
   * @param securityIds  the new security identifiers, null clears the security id search
   */
  public void setSecurityIds(Iterable<? extends ObjectIdentifiable> securityIds) {
    if (securityIds == null) {
      _securityIds = null;
    } else {
      _securityIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable securityId : securityIds) {
        _securityIds.add(securityId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param securityKey  the security key identifier to add, not null
   */
  public void addSecurityKey(Identifier securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    addSecurityKeys(Arrays.asList(securityKey));
  }

  /**
   * Adds a collection of security key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param securityKeys  the security key identifiers to add, not null
   */
  public void addSecurityKeys(Identifier... securityKeys) {
    ArgumentChecker.notNull(securityKeys, "securityKeys");
    if (getSecurityKeys() == null) {
      setSecurityKeys(new IdentifierSearch(securityKeys));
    } else {
      getSecurityKeys().addIdentifiers(securityKeys);
    }
  }

  /**
   * Adds a collection of security key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param securityKeys  the security key identifiers to add, not null
   */
  public void addSecurityKeys(Iterable<Identifier> securityKeys) {
    ArgumentChecker.notNull(securityKeys, "securityKeys");
    if (getSecurityKeys() == null) {
      setSecurityKeys(new IdentifierSearch(securityKeys));
    } else {
      getSecurityKeys().addIdentifiers(securityKeys);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof SecurityDocument == false) {
      return false;
    }
    SecurityDocument document = (SecurityDocument) obj;
    ManageableSecurity security = document.getSecurity();
    if (getSecurityIds() != null && getSecurityIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getSecurityKeys() != null && getSecurityKeys().matches(security.getIdentifiers()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), document.getName()) == false) {
      return false;
    }
    if (getSecurityType() != null && getSecurityType().equals(security.getSecurityType()) == false) {
      return false;
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

  @Override
  public SecuritySearchRequest.Meta metaBean() {
    return SecuritySearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 1550081880:  // securityIds
        return getSecurityIds();
      case 807958868:  // securityKeys
        return getSecurityKeys();
      case 3373707:  // name
        return getName();
      case 808245914:  // securityType
        return getSecurityType();
      case -1233600576:  // fullDetail
        return isFullDetail();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 1550081880:  // securityIds
        setSecurityIds((List<ObjectIdentifier>) newValue);
        return;
      case 807958868:  // securityKeys
        setSecurityKeys((IdentifierSearch) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 808245914:  // securityType
        setSecurityType((String) newValue);
        return;
      case -1233600576:  // fullDetail
        setFullDetail((Boolean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of security object identifiers, null to not limit by security object identifiers.
   * Note that an empty set will return no securities.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getSecurityIds() {
    return _securityIds;
  }

  /**
   * Gets the the {@code securityIds} property.
   * Note that an empty set will return no securities.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> securityIds() {
    return metaBean().securityIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security keys to match, null to not match on security keys.
   * @return the value of the property
   */
  public IdentifierSearch getSecurityKeys() {
    return _securityKeys;
  }

  /**
   * Sets the security keys to match, null to not match on security keys.
   * @param securityKeys  the new value of the property
   */
  public void setSecurityKeys(IdentifierSearch securityKeys) {
    this._securityKeys = securityKeys;
  }

  /**
   * Gets the the {@code securityKeys} property.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> securityKeys() {
    return metaBean().securityKeys().createProperty(this);
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
  /**
   * The meta-bean for {@code SecuritySearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code securityIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectIdentifier>> _securityIds = DirectMetaProperty.ofReadWrite(this, "securityIds", (Class) List.class);
    /**
     * The meta-property for the {@code securityKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _securityKeys = DirectMetaProperty.ofReadWrite(this, "securityKeys", IdentifierSearch.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code securityType} property.
     */
    private final MetaProperty<String> _securityType = DirectMetaProperty.ofReadWrite(this, "securityType", String.class);
    /**
     * The meta-property for the {@code fullDetail} property.
     */
    private final MetaProperty<Boolean> _fullDetail = DirectMetaProperty.ofReadWrite(this, "fullDetail", Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("securityIds", _securityIds);
      temp.put("securityKeys", _securityKeys);
      temp.put("name", _name);
      temp.put("securityType", _securityType);
      temp.put("fullDetail", _fullDetail);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public SecuritySearchRequest createBean() {
      return new SecuritySearchRequest();
    }

    @Override
    public Class<? extends SecuritySearchRequest> beanType() {
      return SecuritySearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code securityIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectIdentifier>> securityIds() {
      return _securityIds;
    }

    /**
     * The meta-property for the {@code securityKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> securityKeys() {
      return _securityKeys;
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
     * The meta-property for the {@code fullDetail} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> fullDetail() {
      return _fullDetail;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
