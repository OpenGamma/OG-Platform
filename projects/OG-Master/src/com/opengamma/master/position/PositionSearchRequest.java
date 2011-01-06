/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
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
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * Request for searching for positions.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link PositionHistoryRequest} for more details on how history works.
 */
@BeanDefinition
public class PositionSearchRequest extends AbstractSearchRequest {

  /**
   * The list of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty list will return no positions.
   */
  @PropertyDefinition(set = "manual")
  private List<UniqueIdentifier> _positionIds;
  /**
   * The list of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   */
  @PropertyDefinition(set = "manual")
  private List<UniqueIdentifier> _tradeIds;
  /**
   * The security keys to match, null to not match on security keys.
   */
  @PropertyDefinition
  private IdentifierSearch _securityKeys;
  /**
   * The data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerKey;
  /**
   * The minimum quantity, inclusive, null for no minimum.
   */
  @PropertyDefinition
  private BigDecimal _minQuantity;
  /**
   * The maximum quantity, exclusive, null for no maximum.
   */
  @PropertyDefinition
  private BigDecimal _maxQuantity;

  /**
   * Creates an instance.
   */
  public PositionSearchRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a position id to the list.
   * @param positionId  the position id to add, not null
   */
  public void addPositionId(UniqueIdentifier positionId) {
    if (_positionIds == null) {
      _positionIds = new ArrayList<UniqueIdentifier>();
    }
    _positionIds.add(positionId);
  }

  /**
   * Sets the list of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty list will return no positions.
   * @param positionIds  the new value of the property
   */
  public void setPositionIds(List<UniqueIdentifier> positionIds) {
    if (positionIds == null) {
      _positionIds = null;
    } else {
      _positionIds = new ArrayList<UniqueIdentifier>(positionIds);
    }
  }

  /**
   * Adds a trade id to the list.
   * @param tradeId  the trade id to add, not null
   */
  public void addTradeId(UniqueIdentifier tradeId) {
    if (_tradeIds == null) {
      _tradeIds = new ArrayList<UniqueIdentifier>();
    }
    _tradeIds.add(tradeId);
  }

  /**
   * Sets the list of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @param tradeIds  the new value of the property
   */
  public void setTradeIds(List<UniqueIdentifier> tradeIds) {
    if (tradeIds == null) {
      _tradeIds = null;
    } else {
      _tradeIds = new ArrayList<UniqueIdentifier>(tradeIds);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifier  the identifier to add as a bundle, not null
   */
  public void addSecurityKey(Identifier identifier) {
    addSecurityKeys(Arrays.asList(identifier));
  }

  /**
   * Adds a collection of security key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifiers  the bundle to add, not nullO
   */
  public void addSecurityKeys(Identifier... identifiers) {
    ArgumentChecker.notNull(identifiers, "bundle");
    if (getSecurityKeys() == null) {
      setSecurityKeys(new IdentifierSearch(identifiers));
    } else {
      getSecurityKeys().addIdentifiers(identifiers);
    }
  }

  /**
   * Adds a collection of security key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifiers  the bundle to add, not nullO
   */
  public void addSecurityKeys(Iterable<Identifier> identifiers) {
    ArgumentChecker.notNull(identifiers, "bundle");
    if (getSecurityKeys() == null) {
      setSecurityKeys(new IdentifierSearch(identifiers));
    } else {
      getSecurityKeys().addIdentifiers(identifiers);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PositionSearchRequest}.
   * @return the meta-bean, not null
   */
  public static PositionSearchRequest.Meta meta() {
    return PositionSearchRequest.Meta.INSTANCE;
  }

  @Override
  public PositionSearchRequest.Meta metaBean() {
    return PositionSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -137459505:  // positionIds
        return getPositionIds();
      case 1271202484:  // tradeIds
        return getTradeIds();
      case 807958868:  // securityKeys
        return getSecurityKeys();
      case 2064682670:  // providerKey
        return getProviderKey();
      case 69860605:  // minQuantity
        return getMinQuantity();
      case 747293199:  // maxQuantity
        return getMaxQuantity();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -137459505:  // positionIds
        setPositionIds((List<UniqueIdentifier>) newValue);
        return;
      case 1271202484:  // tradeIds
        setTradeIds((List<UniqueIdentifier>) newValue);
        return;
      case 807958868:  // securityKeys
        setSecurityKeys((IdentifierSearch) newValue);
        return;
      case 2064682670:  // providerKey
        setProviderKey((Identifier) newValue);
        return;
      case 69860605:  // minQuantity
        setMinQuantity((BigDecimal) newValue);
        return;
      case 747293199:  // maxQuantity
        setMaxQuantity((BigDecimal) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty list will return no positions.
   * @return the value of the property
   */
  public List<UniqueIdentifier> getPositionIds() {
    return _positionIds;
  }

  /**
   * Gets the the {@code positionIds} property.
   * Note that an empty list will return no positions.
   * @return the property, not null
   */
  public final Property<List<UniqueIdentifier>> positionIds() {
    return metaBean().positionIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the value of the property
   */
  public List<UniqueIdentifier> getTradeIds() {
    return _tradeIds;
  }

  /**
   * Gets the the {@code tradeIds} property.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the property, not null
   */
  public final Property<List<UniqueIdentifier>> tradeIds() {
    return metaBean().tradeIds().createProperty(this);
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
   * Gets the identifier of the data provider, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getProviderKey() {
    return _providerKey;
  }

  /**
   * Sets the identifier of the data provider, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param providerKey  the new value of the property
   */
  public void setProviderKey(Identifier providerKey) {
    this._providerKey = providerKey;
  }

  /**
   * Gets the the {@code providerKey} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> providerKey() {
    return metaBean().providerKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum quantity, inclusive, null for no minimum.
   * @return the value of the property
   */
  public BigDecimal getMinQuantity() {
    return _minQuantity;
  }

  /**
   * Sets the minimum quantity, inclusive, null for no minimum.
   * @param minQuantity  the new value of the property
   */
  public void setMinQuantity(BigDecimal minQuantity) {
    this._minQuantity = minQuantity;
  }

  /**
   * Gets the the {@code minQuantity} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> minQuantity() {
    return metaBean().minQuantity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum quantity, exclusive, null for no maximum.
   * @return the value of the property
   */
  public BigDecimal getMaxQuantity() {
    return _maxQuantity;
  }

  /**
   * Sets the maximum quantity, exclusive, null for no maximum.
   * @param maxQuantity  the new value of the property
   */
  public void setMaxQuantity(BigDecimal maxQuantity) {
    this._maxQuantity = maxQuantity;
  }

  /**
   * Gets the the {@code maxQuantity} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> maxQuantity() {
    return metaBean().maxQuantity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PositionSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code positionIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<UniqueIdentifier>> _positionIds = DirectMetaProperty.ofReadWrite(this, "positionIds", (Class) List.class);
    /**
     * The meta-property for the {@code tradeIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<UniqueIdentifier>> _tradeIds = DirectMetaProperty.ofReadWrite(this, "tradeIds", (Class) List.class);
    /**
     * The meta-property for the {@code securityKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _securityKeys = DirectMetaProperty.ofReadWrite(this, "securityKeys", IdentifierSearch.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(this, "providerKey", Identifier.class);
    /**
     * The meta-property for the {@code minQuantity} property.
     */
    private final MetaProperty<BigDecimal> _minQuantity = DirectMetaProperty.ofReadWrite(this, "minQuantity", BigDecimal.class);
    /**
     * The meta-property for the {@code maxQuantity} property.
     */
    private final MetaProperty<BigDecimal> _maxQuantity = DirectMetaProperty.ofReadWrite(this, "maxQuantity", BigDecimal.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("positionIds", _positionIds);
      temp.put("tradeIds", _tradeIds);
      temp.put("securityKeys", _securityKeys);
      temp.put("providerKey", _providerKey);
      temp.put("minQuantity", _minQuantity);
      temp.put("maxQuantity", _maxQuantity);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public PositionSearchRequest createBean() {
      return new PositionSearchRequest();
    }

    @Override
    public Class<? extends PositionSearchRequest> beanType() {
      return PositionSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code positionIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<UniqueIdentifier>> positionIds() {
      return _positionIds;
    }

    /**
     * The meta-property for the {@code tradeIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<UniqueIdentifier>> tradeIds() {
      return _tradeIds;
    }

    /**
     * The meta-property for the {@code securityKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> securityKeys() {
      return _securityKeys;
    }

    /**
     * The meta-property for the {@code providerKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> providerKey() {
      return _providerKey;
    }

    /**
     * The meta-property for the {@code minQuantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> minQuantity() {
      return _minQuantity;
    }

    /**
     * The meta-property for the {@code maxQuantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> maxQuantity() {
      return _maxQuantity;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
