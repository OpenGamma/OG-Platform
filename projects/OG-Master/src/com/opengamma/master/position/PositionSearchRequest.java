/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Request for searching for positions.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link PositionHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class PositionSearchRequest extends AbstractSearchRequest {

  /**
   * The set of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty set will return no positions.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectIdentifier> _positionIds;
  /**
   * The set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectIdentifier> _tradeIds;
  /**
   * The security keys to match, null to not match on security keys.
   */
  @PropertyDefinition
  private IdentifierSearch _securityKeys;
  /**
   * The identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code securityKeys}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _identifierValue;
  /**
   * The position data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _positionProviderKey;
  /**
   * The trade data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _tradeProviderKey;
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
   * Adds a single position object identifier to the set.
   * 
   * @param positionId  the position object identifier to add, not null
   */
  public void addPositionId(ObjectIdentifiable positionId) {
    ArgumentChecker.notNull(positionId, "positionId");
    if (_positionIds == null) {
      _positionIds = new ArrayList<ObjectIdentifier>();
    }
    _positionIds.add(positionId.getObjectId());
  }

  /**
   * Sets the set of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty set will return no positions.
   * 
   * @param positionIds  the new position identifiers, null clears the position id search
   */
  public void setPositionIds(Iterable<? extends ObjectIdentifiable> positionIds) {
    if (positionIds == null) {
      _positionIds = null;
    } else {
      _positionIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable positionId : positionIds) {
        _positionIds.add(positionId.getObjectId());
      }
    }
  }

  /**
   * Adds a single trade object identifier to the set.
   * 
   * @param tradeId  the trade object identifier to add, not null
   */
  public void addTradeId(ObjectIdentifiable tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    if (_tradeIds == null) {
      _tradeIds = new ArrayList<ObjectIdentifier>();
    }
    _tradeIds.add(tradeId.getObjectId());
  }

  /**
   * Sets the set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty set will return no positions.
   * 
   * @param tradeIds  the new trade identifiers, null clears the trade id search
   */
  public void setTradeIds(Iterable<? extends ObjectIdentifiable> tradeIds) {
    if (tradeIds == null) {
      _tradeIds = null;
    } else {
      _tradeIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable tradeId : tradeIds) {
        _tradeIds.add(tradeId.getObjectId());
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
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof PositionDocument == false) {
      return false;
    }
    final PositionDocument document = (PositionDocument) obj;
    final ManageablePosition position = document.getPosition();
    if (getPositionIds() != null && getPositionIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getTradeIds() != null && position.matchesAnyTrade(getTradeIds()) == false) {
      return false;
    }
    if (getSecurityKeys() != null && getSecurityKeys().matches(position.getSecurityLink().getAllIdentifiers()) == false) {
      return false;
    }
    if (getPositionProviderKey() != null && getPositionProviderKey().equals(position.getProviderKey()) == false) {
      return false;
    }
    if (getTradeProviderKey() != null && position.matchesAnyTradeProviderKey(getTradeProviderKey()) == false) {
      return false;
    }
    if (getMinQuantity() != null && (position.getQuantity() == null || position.getQuantity().compareTo(getMinQuantity()) < 0)) {
      return false;
    }
    if (getMaxQuantity() != null && (position.getQuantity() == null || position.getQuantity().compareTo(getMaxQuantity()) >= 0)) {
      return false;
    }
    return true;
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
  static {
    JodaBeanUtils.registerMetaBean(PositionSearchRequest.Meta.INSTANCE);
  }

  @Override
  public PositionSearchRequest.Meta metaBean() {
    return PositionSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -137459505:  // positionIds
        return getPositionIds();
      case 1271202484:  // tradeIds
        return getTradeIds();
      case 807958868:  // securityKeys
        return getSecurityKeys();
      case 2085582408:  // identifierValue
        return getIdentifierValue();
      case -370050619:  // positionProviderKey
        return getPositionProviderKey();
      case -510247254:  // tradeProviderKey
        return getTradeProviderKey();
      case 69860605:  // minQuantity
        return getMinQuantity();
      case 747293199:  // maxQuantity
        return getMaxQuantity();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -137459505:  // positionIds
        setPositionIds((List<ObjectIdentifier>) newValue);
        return;
      case 1271202484:  // tradeIds
        setTradeIds((List<ObjectIdentifier>) newValue);
        return;
      case 807958868:  // securityKeys
        setSecurityKeys((IdentifierSearch) newValue);
        return;
      case 2085582408:  // identifierValue
        setIdentifierValue((String) newValue);
        return;
      case -370050619:  // positionProviderKey
        setPositionProviderKey((Identifier) newValue);
        return;
      case -510247254:  // tradeProviderKey
        setTradeProviderKey((Identifier) newValue);
        return;
      case 69860605:  // minQuantity
        setMinQuantity((BigDecimal) newValue);
        return;
      case 747293199:  // maxQuantity
        setMaxQuantity((BigDecimal) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PositionSearchRequest other = (PositionSearchRequest) obj;
      return JodaBeanUtils.equal(getPositionIds(), other.getPositionIds()) &&
          JodaBeanUtils.equal(getTradeIds(), other.getTradeIds()) &&
          JodaBeanUtils.equal(getSecurityKeys(), other.getSecurityKeys()) &&
          JodaBeanUtils.equal(getIdentifierValue(), other.getIdentifierValue()) &&
          JodaBeanUtils.equal(getPositionProviderKey(), other.getPositionProviderKey()) &&
          JodaBeanUtils.equal(getTradeProviderKey(), other.getTradeProviderKey()) &&
          JodaBeanUtils.equal(getMinQuantity(), other.getMinQuantity()) &&
          JodaBeanUtils.equal(getMaxQuantity(), other.getMaxQuantity()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityKeys());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMinQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaxQuantity());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty set will return no positions.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getPositionIds() {
    return _positionIds;
  }

  /**
   * Gets the the {@code positionIds} property.
   * Note that an empty set will return no positions.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> positionIds() {
    return metaBean().positionIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getTradeIds() {
    return _tradeIds;
  }

  /**
   * Gets the the {@code tradeIds} property.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> tradeIds() {
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
   * Gets the identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code securityKeys}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getIdentifierValue() {
    return _identifierValue;
  }

  /**
   * Sets the identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code securityKeys}
   * search is useful for exact machine searching.
   * @param identifierValue  the new value of the property
   */
  public void setIdentifierValue(String identifierValue) {
    this._identifierValue = identifierValue;
  }

  /**
   * Gets the the {@code identifierValue} property.
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code securityKeys}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> identifierValue() {
    return metaBean().identifierValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getPositionProviderKey() {
    return _positionProviderKey;
  }

  /**
   * Sets the position data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param positionProviderKey  the new value of the property
   */
  public void setPositionProviderKey(Identifier positionProviderKey) {
    this._positionProviderKey = positionProviderKey;
  }

  /**
   * Gets the the {@code positionProviderKey} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> positionProviderKey() {
    return metaBean().positionProviderKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getTradeProviderKey() {
    return _tradeProviderKey;
  }

  /**
   * Sets the trade data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param tradeProviderKey  the new value of the property
   */
  public void setTradeProviderKey(Identifier tradeProviderKey) {
    this._tradeProviderKey = tradeProviderKey;
  }

  /**
   * Gets the the {@code tradeProviderKey} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> tradeProviderKey() {
    return metaBean().tradeProviderKey().createProperty(this);
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
    private final MetaProperty<List<ObjectIdentifier>> _positionIds = DirectMetaProperty.ofReadWrite(
        this, "positionIds", PositionSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code tradeIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectIdentifier>> _tradeIds = DirectMetaProperty.ofReadWrite(
        this, "tradeIds", PositionSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code securityKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _securityKeys = DirectMetaProperty.ofReadWrite(
        this, "securityKeys", PositionSearchRequest.class, IdentifierSearch.class);
    /**
     * The meta-property for the {@code identifierValue} property.
     */
    private final MetaProperty<String> _identifierValue = DirectMetaProperty.ofReadWrite(
        this, "identifierValue", PositionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code positionProviderKey} property.
     */
    private final MetaProperty<Identifier> _positionProviderKey = DirectMetaProperty.ofReadWrite(
        this, "positionProviderKey", PositionSearchRequest.class, Identifier.class);
    /**
     * The meta-property for the {@code tradeProviderKey} property.
     */
    private final MetaProperty<Identifier> _tradeProviderKey = DirectMetaProperty.ofReadWrite(
        this, "tradeProviderKey", PositionSearchRequest.class, Identifier.class);
    /**
     * The meta-property for the {@code minQuantity} property.
     */
    private final MetaProperty<BigDecimal> _minQuantity = DirectMetaProperty.ofReadWrite(
        this, "minQuantity", PositionSearchRequest.class, BigDecimal.class);
    /**
     * The meta-property for the {@code maxQuantity} property.
     */
    private final MetaProperty<BigDecimal> _maxQuantity = DirectMetaProperty.ofReadWrite(
        this, "maxQuantity", PositionSearchRequest.class, BigDecimal.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "positionIds",
        "tradeIds",
        "securityKeys",
        "identifierValue",
        "positionProviderKey",
        "tradeProviderKey",
        "minQuantity",
        "maxQuantity");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -137459505:  // positionIds
          return _positionIds;
        case 1271202484:  // tradeIds
          return _tradeIds;
        case 807958868:  // securityKeys
          return _securityKeys;
        case 2085582408:  // identifierValue
          return _identifierValue;
        case -370050619:  // positionProviderKey
          return _positionProviderKey;
        case -510247254:  // tradeProviderKey
          return _tradeProviderKey;
        case 69860605:  // minQuantity
          return _minQuantity;
        case 747293199:  // maxQuantity
          return _maxQuantity;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PositionSearchRequest> builder() {
      return new DirectBeanBuilder<PositionSearchRequest>(new PositionSearchRequest());
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
    public final MetaProperty<List<ObjectIdentifier>> positionIds() {
      return _positionIds;
    }

    /**
     * The meta-property for the {@code tradeIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectIdentifier>> tradeIds() {
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
     * The meta-property for the {@code identifierValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> identifierValue() {
      return _identifierValue;
    }

    /**
     * The meta-property for the {@code positionProviderKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> positionProviderKey() {
      return _positionProviderKey;
    }

    /**
     * The meta-property for the {@code tradeProviderKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> tradeProviderKey() {
      return _tradeProviderKey;
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
