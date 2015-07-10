/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
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
  private Set<ObjectId> _positionObjectIds;
  /**
   * The set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   */
  @PropertyDefinition(set = "manual")
  private Set<ObjectId> _tradeObjectIds;
  /**
   * The security external identifiers to match, null to not match on security identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _securityIdSearch;
  /**
   * The external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _securityIdValue;
  /**
   * The position data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _positionProviderId;
  /**
   * The trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _tradeProviderId;
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
  public void addPositionObjectId(ObjectIdentifiable positionId) {
    ArgumentChecker.notNull(positionId, "positionId");
    if (_positionObjectIds == null) {
      _positionObjectIds = new LinkedHashSet<>();
    }
    _positionObjectIds.add(positionId.getObjectId());
  }

  /**
   * Sets the set of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty set will return no positions.
   * 
   * @param positionIds  the new position identifiers, null clears the position id search
   */
  public void setPositionObjectIds(Iterable<? extends ObjectIdentifiable> positionIds) {
    if (positionIds == null) {
      _positionObjectIds = null;
    } else {
      _positionObjectIds = new LinkedHashSet<>();
      for (ObjectIdentifiable positionId : positionIds) {
        _positionObjectIds.add(positionId.getObjectId());
      }
    }
  }

  /**
   * Adds a single trade object identifier to the set.
   * 
   * @param tradeId  the trade object identifier to add, not null
   */
  public void addTradeObjectId(ObjectIdentifiable tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    if (_tradeObjectIds == null) {
      _tradeObjectIds = new LinkedHashSet<>();
    }
    _tradeObjectIds.add(tradeId.getObjectId());
  }

  /**
   * Sets the set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty set will return no positions.
   * 
   * @param tradeIds  the new trade identifiers, null clears the trade id search
   */
  public void setTradeObjectIds(Iterable<? extends ObjectIdentifiable> tradeIds) {
    if (tradeIds == null) {
      _tradeObjectIds = null;
    } else {
      _tradeObjectIds = new LinkedHashSet<>();
      for (ObjectIdentifiable tradeId : tradeIds) {
        _tradeObjectIds.add(tradeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityId  the security key identifier to add, not null
   */
  public void addSecurityExternalId(ExternalId securityId) {
    ArgumentChecker.notNull(securityId, "securityId");
    addSecurityExternalIds(Arrays.asList(securityId));
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityIds  the security key identifiers to add, not null
   */
  public void addSecurityExternalIds(ExternalId... securityIds) {
    ArgumentChecker.notNull(securityIds, "securityIds");
    if (getSecurityIdSearch() == null) {
      setSecurityIdSearch(ExternalIdSearch.of(securityIds));
    } else {
      setSecurityIdSearch(getSecurityIdSearch().withExternalIdsAdded(securityIds));
    }
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param securityIds  the security key identifiers to add, not null
   */
  public void addSecurityExternalIds(Iterable<ExternalId> securityIds) {
    ArgumentChecker.notNull(securityIds, "securityIds");
    if (getSecurityIdSearch() == null) {
      setSecurityIdSearch(ExternalIdSearch.of(securityIds));
    } else {
      setSecurityIdSearch(getSecurityIdSearch().withExternalIdsAdded(securityIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch} for securities.
   * 
   * @param type  the type to set, not null
   */
  public void setSecurityExternalIdSearchType(ExternalIdSearchType type) {
    if (getSecurityIdSearch() == null) {
      setSecurityIdSearch(ExternalIdSearch.of(type));
    } else {
      setSecurityIdSearch(getSecurityIdSearch().withSearchType(type));
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
    if (getPositionObjectIds() != null && getPositionObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getTradeObjectIds() != null && position.matchesAnyTrade(getTradeObjectIds()) == false) {
      return false;
    }
    if (getSecurityIdSearch() != null && getSecurityIdSearch().matches(position.getSecurityLink().getAllExternalIds()) == false) {
      return false;
    }
    if (getPositionProviderId() != null && getPositionProviderId().equals(position.getProviderId()) == false) {
      return false;
    }
    if (getTradeProviderId() != null && position.matchesAnyTradeProviderId(getTradeProviderId()) == false) {
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

  //-----------------------------------------------------------------------
  /**
   * Gets the set of position object identifiers, null to not limit by position object identifiers.
   * Note that an empty set will return no positions.
   * @return the value of the property
   */
  public Set<ObjectId> getPositionObjectIds() {
    return _positionObjectIds;
  }

  /**
   * Gets the the {@code positionObjectIds} property.
   * Note that an empty set will return no positions.
   * @return the property, not null
   */
  public final Property<Set<ObjectId>> positionObjectIds() {
    return metaBean().positionObjectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the value of the property
   */
  public Set<ObjectId> getTradeObjectIds() {
    return _tradeObjectIds;
  }

  /**
   * Gets the the {@code tradeObjectIds} property.
   * Each returned position will contain at least one of these trades.
   * Note that an empty list will return no positions.
   * @return the property, not null
   */
  public final Property<Set<ObjectId>> tradeObjectIds() {
    return metaBean().tradeObjectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the security external identifiers to match, null to not match on security identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getSecurityIdSearch() {
    return _securityIdSearch;
  }

  /**
   * Sets the security external identifiers to match, null to not match on security identifiers.
   * @param securityIdSearch  the new value of the property
   */
  public void setSecurityIdSearch(ExternalIdSearch securityIdSearch) {
    this._securityIdSearch = securityIdSearch;
  }

  /**
   * Gets the the {@code securityIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> securityIdSearch() {
    return metaBean().securityIdSearch().createProperty(this);
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
  public String getSecurityIdValue() {
    return _securityIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param securityIdValue  the new value of the property
   */
  public void setSecurityIdValue(String securityIdValue) {
    this._securityIdValue = securityIdValue;
  }

  /**
   * Gets the the {@code securityIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> securityIdValue() {
    return metaBean().securityIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getPositionProviderId() {
    return _positionProviderId;
  }

  /**
   * Sets the position data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param positionProviderId  the new value of the property
   */
  public void setPositionProviderId(ExternalId positionProviderId) {
    this._positionProviderId = positionProviderId;
  }

  /**
   * Gets the the {@code positionProviderId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> positionProviderId() {
    return metaBean().positionProviderId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getTradeProviderId() {
    return _tradeProviderId;
  }

  /**
   * Sets the trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param tradeProviderId  the new value of the property
   */
  public void setTradeProviderId(ExternalId tradeProviderId) {
    this._tradeProviderId = tradeProviderId;
  }

  /**
   * Gets the the {@code tradeProviderId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> tradeProviderId() {
    return metaBean().tradeProviderId().createProperty(this);
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
  @Override
  public PositionSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PositionSearchRequest other = (PositionSearchRequest) obj;
      return JodaBeanUtils.equal(getPositionObjectIds(), other.getPositionObjectIds()) &&
          JodaBeanUtils.equal(getTradeObjectIds(), other.getTradeObjectIds()) &&
          JodaBeanUtils.equal(getSecurityIdSearch(), other.getSecurityIdSearch()) &&
          JodaBeanUtils.equal(getSecurityIdValue(), other.getSecurityIdValue()) &&
          JodaBeanUtils.equal(getPositionProviderId(), other.getPositionProviderId()) &&
          JodaBeanUtils.equal(getTradeProviderId(), other.getTradeProviderId()) &&
          JodaBeanUtils.equal(getMinQuantity(), other.getMinQuantity()) &&
          JodaBeanUtils.equal(getMaxQuantity(), other.getMaxQuantity()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityIdSearch());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityIdValue());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionProviderId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeProviderId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMinQuantity());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxQuantity());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("PositionSearchRequest{");
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
    buf.append("positionObjectIds").append('=').append(JodaBeanUtils.toString(getPositionObjectIds())).append(',').append(' ');
    buf.append("tradeObjectIds").append('=').append(JodaBeanUtils.toString(getTradeObjectIds())).append(',').append(' ');
    buf.append("securityIdSearch").append('=').append(JodaBeanUtils.toString(getSecurityIdSearch())).append(',').append(' ');
    buf.append("securityIdValue").append('=').append(JodaBeanUtils.toString(getSecurityIdValue())).append(',').append(' ');
    buf.append("positionProviderId").append('=').append(JodaBeanUtils.toString(getPositionProviderId())).append(',').append(' ');
    buf.append("tradeProviderId").append('=').append(JodaBeanUtils.toString(getTradeProviderId())).append(',').append(' ');
    buf.append("minQuantity").append('=').append(JodaBeanUtils.toString(getMinQuantity())).append(',').append(' ');
    buf.append("maxQuantity").append('=').append(JodaBeanUtils.toString(getMaxQuantity())).append(',').append(' ');
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
     * The meta-property for the {@code positionObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ObjectId>> _positionObjectIds = DirectMetaProperty.ofReadWrite(
        this, "positionObjectIds", PositionSearchRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code tradeObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ObjectId>> _tradeObjectIds = DirectMetaProperty.ofReadWrite(
        this, "tradeObjectIds", PositionSearchRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code securityIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _securityIdSearch = DirectMetaProperty.ofReadWrite(
        this, "securityIdSearch", PositionSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code securityIdValue} property.
     */
    private final MetaProperty<String> _securityIdValue = DirectMetaProperty.ofReadWrite(
        this, "securityIdValue", PositionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code positionProviderId} property.
     */
    private final MetaProperty<ExternalId> _positionProviderId = DirectMetaProperty.ofReadWrite(
        this, "positionProviderId", PositionSearchRequest.class, ExternalId.class);
    /**
     * The meta-property for the {@code tradeProviderId} property.
     */
    private final MetaProperty<ExternalId> _tradeProviderId = DirectMetaProperty.ofReadWrite(
        this, "tradeProviderId", PositionSearchRequest.class, ExternalId.class);
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
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "positionObjectIds",
        "tradeObjectIds",
        "securityIdSearch",
        "securityIdValue",
        "positionProviderId",
        "tradeProviderId",
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
        case -88800304:  // positionObjectIds
          return _positionObjectIds;
        case 572505589:  // tradeObjectIds
          return _tradeObjectIds;
        case 1137408515:  // securityIdSearch
          return _securityIdSearch;
        case -930478666:  // securityIdValue
          return _securityIdValue;
        case 680799477:  // positionProviderId
          return _positionProviderId;
        case -293554320:  // tradeProviderId
          return _tradeProviderId;
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
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code positionObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ObjectId>> positionObjectIds() {
      return _positionObjectIds;
    }

    /**
     * The meta-property for the {@code tradeObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ObjectId>> tradeObjectIds() {
      return _tradeObjectIds;
    }

    /**
     * The meta-property for the {@code securityIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> securityIdSearch() {
      return _securityIdSearch;
    }

    /**
     * The meta-property for the {@code securityIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> securityIdValue() {
      return _securityIdValue;
    }

    /**
     * The meta-property for the {@code positionProviderId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> positionProviderId() {
      return _positionProviderId;
    }

    /**
     * The meta-property for the {@code tradeProviderId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> tradeProviderId() {
      return _tradeProviderId;
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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -88800304:  // positionObjectIds
          return ((PositionSearchRequest) bean).getPositionObjectIds();
        case 572505589:  // tradeObjectIds
          return ((PositionSearchRequest) bean).getTradeObjectIds();
        case 1137408515:  // securityIdSearch
          return ((PositionSearchRequest) bean).getSecurityIdSearch();
        case -930478666:  // securityIdValue
          return ((PositionSearchRequest) bean).getSecurityIdValue();
        case 680799477:  // positionProviderId
          return ((PositionSearchRequest) bean).getPositionProviderId();
        case -293554320:  // tradeProviderId
          return ((PositionSearchRequest) bean).getTradeProviderId();
        case 69860605:  // minQuantity
          return ((PositionSearchRequest) bean).getMinQuantity();
        case 747293199:  // maxQuantity
          return ((PositionSearchRequest) bean).getMaxQuantity();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -88800304:  // positionObjectIds
          ((PositionSearchRequest) bean).setPositionObjectIds((Set<ObjectId>) newValue);
          return;
        case 572505589:  // tradeObjectIds
          ((PositionSearchRequest) bean).setTradeObjectIds((Set<ObjectId>) newValue);
          return;
        case 1137408515:  // securityIdSearch
          ((PositionSearchRequest) bean).setSecurityIdSearch((ExternalIdSearch) newValue);
          return;
        case -930478666:  // securityIdValue
          ((PositionSearchRequest) bean).setSecurityIdValue((String) newValue);
          return;
        case 680799477:  // positionProviderId
          ((PositionSearchRequest) bean).setPositionProviderId((ExternalId) newValue);
          return;
        case -293554320:  // tradeProviderId
          ((PositionSearchRequest) bean).setTradeProviderId((ExternalId) newValue);
          return;
        case 69860605:  // minQuantity
          ((PositionSearchRequest) bean).setMinQuantity((BigDecimal) newValue);
          return;
        case 747293199:  // maxQuantity
          ((PositionSearchRequest) bean).setMaxQuantity((BigDecimal) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
