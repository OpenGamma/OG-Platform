/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange;

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
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Request for searching for exchanges.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link ExchangeHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class ExchangeSearchRequest extends AbstractSearchRequest {

  /**
   * The set of exchange object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no exchanges.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectIdentifier> _exchangeIds;
  /**
   * The exchange keys to match, null to not match on exchange keys.
   */
  @PropertyDefinition
  private IdentifierSearch _exchangeKeys;
  /**
   * The exchange name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;

  /**
   * Creates an instance.
   */
  public ExchangeSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param exchangeKey  the exchange key identifier to search for, not null
   */
  public ExchangeSearchRequest(Identifier exchangeKey) {
    addExchangeKey(exchangeKey);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param exchangeKeys  the exchange key identifiers to search for, not null
   */
  public ExchangeSearchRequest(IdentifierBundle exchangeKeys) {
    addExchangeKeys(exchangeKeys);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange object identifier to the set.
   * 
   * @param exchangeId  the exchange object identifier to add, not null
   */
  public void addExchangeId(ObjectIdentifiable exchangeId) {
    ArgumentChecker.notNull(exchangeId, "exchangeId");
    if (_exchangeIds == null) {
      _exchangeIds = new ArrayList<ObjectIdentifier>();
    }
    _exchangeIds.add(exchangeId.getObjectId());
  }

  /**
   * Sets the set of exchange object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no exchanges.
   * 
   * @param exchangeIds  the new exchange identifiers, null clears the exchange id search
   */
  public void setExchangeIds(Iterable<? extends ObjectIdentifiable> exchangeIds) {
    if (exchangeIds == null) {
      _exchangeIds = null;
    } else {
      _exchangeIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable exchangeId : exchangeIds) {
        _exchangeIds.add(exchangeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKey  the exchange key identifier to add, not null
   */
  public void addExchangeKey(Identifier exchangeKey) {
    ArgumentChecker.notNull(exchangeKey, "exchangeKey");
    addExchangeKeys(Arrays.asList(exchangeKey));
  }

  /**
   * Adds a collection of exchange key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKeys  the exchange key identifiers to add, not null
   */
  public void addExchangeKeys(Identifier... exchangeKeys) {
    ArgumentChecker.notNull(exchangeKeys, "exchangeKeys");
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(exchangeKeys));
    } else {
      getExchangeKeys().addIdentifiers(exchangeKeys);
    }
  }

  /**
   * Adds a collection of exchange key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKeys  the exchange key identifiers to add, not null
   */
  public void addExchangeKeys(Iterable<Identifier> exchangeKeys) {
    ArgumentChecker.notNull(exchangeKeys, "exchangeKeys");
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(exchangeKeys));
    } else {
      getExchangeKeys().addIdentifiers(exchangeKeys);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExchangeSearchRequest}.
   * @return the meta-bean, not null
   */
  public static ExchangeSearchRequest.Meta meta() {
    return ExchangeSearchRequest.Meta.INSTANCE;
  }

  @Override
  public ExchangeSearchRequest.Meta metaBean() {
    return ExchangeSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -1755006571:  // exchangeIds
        return getExchangeIds();
      case 1429431991:  // exchangeKeys
        return getExchangeKeys();
      case 3373707:  // name
        return getName();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -1755006571:  // exchangeIds
        setExchangeIds((List<ObjectIdentifier>) newValue);
        return;
      case 1429431991:  // exchangeKeys
        setExchangeKeys((IdentifierSearch) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of exchange object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no exchanges.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getExchangeIds() {
    return _exchangeIds;
  }

  /**
   * Gets the the {@code exchangeIds} property.
   * Note that an empty set will return no exchanges.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> exchangeIds() {
    return metaBean().exchangeIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange keys to match, null to not match on exchange keys.
   * @return the value of the property
   */
  public IdentifierSearch getExchangeKeys() {
    return _exchangeKeys;
  }

  /**
   * Sets the exchange keys to match, null to not match on exchange keys.
   * @param exchangeKeys  the new value of the property
   */
  public void setExchangeKeys(IdentifierSearch exchangeKeys) {
    this._exchangeKeys = exchangeKeys;
  }

  /**
   * Gets the the {@code exchangeKeys} property.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> exchangeKeys() {
    return metaBean().exchangeKeys().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the exchange name, wildcards allowed, null to not match on name.
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
   * The meta-bean for {@code ExchangeSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code exchangeIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectIdentifier>> _exchangeIds = DirectMetaProperty.ofReadWrite(this, "exchangeIds", (Class) List.class);
    /**
     * The meta-property for the {@code exchangeKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _exchangeKeys = DirectMetaProperty.ofReadWrite(this, "exchangeKeys", IdentifierSearch.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("exchangeIds", _exchangeIds);
      temp.put("exchangeKeys", _exchangeKeys);
      temp.put("name", _name);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ExchangeSearchRequest createBean() {
      return new ExchangeSearchRequest();
    }

    @Override
    public Class<? extends ExchangeSearchRequest> beanType() {
      return ExchangeSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code exchangeIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectIdentifier>> exchangeIds() {
      return _exchangeIds;
    }

    /**
     * The meta-property for the {@code exchangeKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> exchangeKeys() {
      return _exchangeKeys;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
