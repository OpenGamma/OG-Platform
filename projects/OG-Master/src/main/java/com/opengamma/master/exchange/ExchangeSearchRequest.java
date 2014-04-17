/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange;

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
  private List<ObjectId> _objectIds;
  /**
   * The exchange external identifiers to match, null to not match on exchange identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _externalIdSearch;
  /**
   * The exchange name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private ExchangeSearchSortOrder _sortOrder = ExchangeSearchSortOrder.OBJECT_ID_ASC;

  /**
   * Creates an instance.
   */
  public ExchangeSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param exchangeId  the exchange external identifier to search for, not null
   */
  public ExchangeSearchRequest(ExternalId exchangeId) {
    addExternalId(exchangeId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param exchangeBundle  the exchange external identifiers to search for, not null
   */
  public ExchangeSearchRequest(ExternalIdBundle exchangeBundle) {
    addExternalIds(exchangeBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange object identifier to the set.
   * 
   * @param exchangeId  the exchange object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable exchangeId) {
    ArgumentChecker.notNull(exchangeId, "exchangeId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(exchangeId.getObjectId());
  }

  /**
   * Sets the set of exchange object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no exchanges.
   * 
   * @param exchangeIds  the new exchange identifiers, null clears the exchange id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> exchangeIds) {
    if (exchangeIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable exchangeId : exchangeIds) {
        _objectIds.add(exchangeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeId  the exchange key identifier to add, not null
   */
  public void addExternalId(ExternalId exchangeId) {
    ArgumentChecker.notNull(exchangeId, "exchangeId");
    addExternalIds(Arrays.asList(exchangeId));
  }

  /**
   * Adds a collection of exchange external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeIds  the exchange key identifiers to add, not null
   */
  public void addExternalIds(ExternalId... exchangeIds) {
    ArgumentChecker.notNull(exchangeIds, "exchangeIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(exchangeIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(exchangeIds));
    }
  }

  /**
   * Adds a collection of exchange external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeIds  the exchange key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> exchangeIds) {
    ArgumentChecker.notNull(exchangeIds, "exchangeIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(exchangeIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(exchangeIds));
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

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof ExchangeDocument == false) {
      return false;
    }
    final ExchangeDocument document = (ExchangeDocument) obj;
    final ManageableExchange exchange = document.getExchange();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(exchange.getExternalIdBundle()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), exchange.getName()) == false) {
      return false;
    }
    return true;
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

  static {
    JodaBeanUtils.registerMetaBean(ExchangeSearchRequest.Meta.INSTANCE);
  }

  @Override
  public ExchangeSearchRequest.Meta metaBean() {
    return ExchangeSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of exchange object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no exchanges.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty set will return no exchanges.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange external identifiers to match, null to not match on exchange identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the exchange external identifiers to match, null to not match on exchange identifiers.
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
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public ExchangeSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(ExchangeSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<ExchangeSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ExchangeSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExchangeSearchRequest other = (ExchangeSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("ExchangeSearchRequest{");
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
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
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
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", ExchangeSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", ExchangeSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ExchangeSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<ExchangeSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", ExchangeSearchRequest.class, ExchangeSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "externalIdSearch",
        "name",
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
        case 3373707:  // name
          return _name;
        case -26774448:  // sortOrder
          return _sortOrder;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExchangeSearchRequest> builder() {
      return new DirectBeanBuilder<ExchangeSearchRequest>(new ExchangeSearchRequest());
    }

    @Override
    public Class<? extends ExchangeSearchRequest> beanType() {
      return ExchangeSearchRequest.class;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((ExchangeSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((ExchangeSearchRequest) bean).getExternalIdSearch();
        case 3373707:  // name
          return ((ExchangeSearchRequest) bean).getName();
        case -26774448:  // sortOrder
          return ((ExchangeSearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((ExchangeSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((ExchangeSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 3373707:  // name
          ((ExchangeSearchRequest) bean).setName((String) newValue);
          return;
        case -26774448:  // sortOrder
          ((ExchangeSearchRequest) bean).setSortOrder((ExchangeSearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ExchangeSearchRequest) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
