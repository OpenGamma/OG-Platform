/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A search request to match external identifiers.
 * <p>
 * The search combines a set of external identifiers and a matching rule.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicAPI
@BeanDefinition(builderScope = "private")
public final class ExternalIdSearch implements ImmutableBean, Iterable<ExternalId>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of identifiers.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<ExternalId> _externalIds;
  /**
   * The search type, default 'ANY'.
   */
  @PropertyDefinition(validate = "notNull")
  private final ExternalIdSearchType _searchType;

  //-------------------------------------------------------------------------
  /**
   * Creates an empty search, with the search type set to any.
   * <p>
   * This uses {@link ExternalIdSearchType#ANY}.
   * <p>
   * This search will not match anything.
   * 
   * @return the external identifier search, not null
   */
  public static ExternalIdSearch of() {
    return new ExternalIdSearch(ExternalIdSearchType.ANY, ImmutableSet.<ExternalId>of());
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * <p>
   * This uses {@link ExternalIdSearchType#ANY}.
   * 
   * @param externalIds  the identifiers, not null
   * @return the external identifier search, not null
   */
  public static ExternalIdSearch of(ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return new ExternalIdSearch(ExternalIdSearchType.ANY, Arrays.asList(externalIds));
  }

  /**
   * Creates a search of the specified type matching a collection of identifiers.
   * 
   * @param searchType  the search type, not null
   * @param externalIds  the identifiers, not null
   * @return the external identifier search, not null
   */
  public static ExternalIdSearch of(ExternalIdSearchType searchType, ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return new ExternalIdSearch(searchType, Arrays.asList(externalIds));
  }

  /**
   * Creates a search matching any of a collection of identifiers.
   * <p>
   * This uses {@link ExternalIdSearchType#ANY}.
   * 
   * @param externalIds  the collection of identifiers, not null
   * @return the external identifier search, not null
   */
  public static ExternalIdSearch of(Iterable<ExternalId> externalIds) {
    return new ExternalIdSearch(ExternalIdSearchType.ANY, externalIds);
  }

  /**
   * Creates a search of the specified type matching a collection of identifiers.
   * 
   * @param searchType  the search type, not null
   * @param externalIds  the collection of identifiers, not null
   * @return the external identifier search, not null
   */
  public static ExternalIdSearch of(ExternalIdSearchType searchType, Iterable<ExternalId> externalIds) {
    return new ExternalIdSearch(searchType, externalIds);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a search matching any of a collection of identifiers.
   * 
   * @param searchType  the search type, not null
   * @param externalIds  the collection of identifiers, not null
   */
  private ExternalIdSearch(ExternalIdSearchType searchType, Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(searchType, "searchType");
    ArgumentChecker.noNulls(externalIds, "externalIds");
    _externalIds = ImmutableSet.copyOf(externalIds);
    _searchType = searchType;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this search with an additional identifier to search for.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to add, not null
   * @return the external identifier search with the specified identifier, not null
   */
  public ExternalIdSearch withExternalIdAdded(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalId> ids = Sets.newHashSet(_externalIds);
    ids.add(externalId);
    return new ExternalIdSearch(_searchType, ids);
  }

  /**
   * Returns a copy of this search with additional identifiers to search for.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalIds  the identifiers to add, not null
   * @return the external identifier search with the specified identifier, not null
   */
  public ExternalIdSearch withExternalIdsAdded(ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    Set<ExternalId> ids = Sets.newHashSet(_externalIds);
    Iterables.addAll(ids, Arrays.asList(externalIds));
    return new ExternalIdSearch(_searchType, ids);
  }

  /**
   * Returns a copy of this search with additional identifiers to search for.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalIds  the identifiers to add, not null
   * @return the external identifier search with the specified identifier, not null
   */
  public ExternalIdSearch withExternalIdsAdded(Iterable<ExternalId> externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    Set<ExternalId> ids = Sets.newHashSet(_externalIds);
    Iterables.addAll(ids, externalIds);
    return new ExternalIdSearch(_searchType, ids);
  }

  /**
   * Returns a copy of this search with the identifier removed.
   * <p>
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to remove, null ignored
   * @return the external identifier search with the specified identifier removed, not null
   */
  public ExternalIdSearch withExternalIdRemoved(ExternalId externalId) {
    if (externalId == null || contains(externalId) == false) {
      return this;
    }
    Set<ExternalId> ids = Sets.newHashSet(_externalIds);
    ids.remove(externalId);
    return new ExternalIdSearch(_searchType, ids);
  }

  /**
   * Returns a copy of this search with the specified search type.
   * 
   * @param searchType  the new search type, not null
   * @return a copy of this search with the new search type, not null
   */
  public ExternalIdSearch withSearchType(ExternalIdSearchType searchType) {
    if (searchType == _searchType) {
      return this;
    }
    return new ExternalIdSearch(searchType, _externalIds);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of identifiers.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _externalIds.size();
  }

  /**
   * Returns an iterator over the identifiers.
   * 
   * @return the identifiers, not null
   */
  public Iterator<ExternalId> iterator() {
    return _externalIds.iterator();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the identifier.
   * <p>
   * An EXACT match returns true if there is one stored identifier and it is equal to the passed in identifier.<br />
   * An ALL match returns true if this is empty or has a single identifier equal to the input.<br />
   * An ANY match returns true if the passed in identifier matches any of the stored identifiers.<br />
   * A NONE match returns true if the passed in identifier does not match any stored identifier.<br />
   * 
   * @param otherId  the identifier to search for, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(ExternalId otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherId).equals(_externalIds);
      case ALL:
        return Sets.newHashSet(otherId).containsAll(_externalIds);
      case ANY:
        return contains(otherId);
      case NONE:
        return contains(otherId) == false;
    }
    return false;
  }

  /**
   * Checks if this search matches the identifiers.
   * <p>
   * An EXACT match returns true if the passed in identifiers are the same set as the stored identifiers.<br />
   * An ALL match returns true if the passed in identifiers match are a superset or equal the stored identifiers.<br />
   * An ANY match returns true if the passed in identifiers match any of the stored identifiers.<br />
   * A NONE match returns true if none of the passed in identifiers match a stored identifier.<br />
   * 
   * @param otherId  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean matches(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    switch (_searchType) {
      case EXACT:
        return Sets.newHashSet(otherId).equals(_externalIds);
      case ALL:
        return Sets.newHashSet(otherId).containsAll(_externalIds);
      case ANY:
        return containsAny(otherId);
      case NONE:
        return containsAny(otherId) == false;
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search contains all the keys from the specified identifiers.
   * <p>
   * This is the opposite check to the ALL search type in {@code matches()}.
   * This method checks if this is a superset or equal to the passed in identifiers.
   * The ALL check checks the superset the other way around.
   * 
   * @param otherId  the identifiers to search for, empty returns true, not null
   * @return true if this search contains all of the keys specified
   */
  public boolean containsAll(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    for (ExternalId identifier : otherId) {
      if (_externalIds.contains(identifier) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this search contains any key from the specified identifiers.
   * 
   * @param otherId  the identifiers to search for, empty returns false, not null
   * @return true if this search contains any of the keys specified
   */
  public boolean containsAny(Iterable<ExternalId> otherId) {
    ArgumentChecker.notNull(otherId, "otherId");
    for (ExternalId identifier : otherId) {
      if (_externalIds.contains(identifier)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this search contains the specified key.
   * 
   * @param externalId  the key to search for, null returns false
   * @return true if this search contains the specified key
   */
  public boolean contains(ExternalId externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  //-------------------------------------------------------------------
  /**
   * Checks if the specified instance can match anything.
   * 
   * @param idSearch  the identifier search, null returns true
   * @return true if the search can match anything
   */
  public static boolean canMatch(final ExternalIdSearch idSearch) {
    if (idSearch == null) {
      return true;
    }
    if (idSearch.getSearchType() == ExternalIdSearchType.NONE) {
      return true;
    }
    return idSearch.size() > 0;
  }

  /**
   * Checks if this search always matches.
   * 
   * @return true if the search always matches
   */
  public boolean alwaysMatches() {
    return (getSearchType() == ExternalIdSearchType.NONE && size() == 0);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExternalIdSearch}.
   * @return the meta-bean, not null
   */
  public static ExternalIdSearch.Meta meta() {
    return ExternalIdSearch.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExternalIdSearch.Meta.INSTANCE);
  }

  private ExternalIdSearch(
      Set<ExternalId> externalIds,
      ExternalIdSearchType searchType) {
    JodaBeanUtils.notNull(externalIds, "externalIds");
    JodaBeanUtils.notNull(searchType, "searchType");
    this._externalIds = ImmutableSet.copyOf(externalIds);
    this._searchType = searchType;
  }

  @Override
  public ExternalIdSearch.Meta metaBean() {
    return ExternalIdSearch.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of identifiers.
   * @return the value of the property, not null
   */
  public Set<ExternalId> getExternalIds() {
    return _externalIds;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the search type, default 'ANY'.
   * @return the value of the property, not null
   */
  public ExternalIdSearchType getSearchType() {
    return _searchType;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExternalIdSearch other = (ExternalIdSearch) obj;
      return JodaBeanUtils.equal(getExternalIds(), other.getExternalIds()) &&
          JodaBeanUtils.equal(getSearchType(), other.getSearchType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSearchType());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("ExternalIdSearch{");
    buf.append("externalIds").append('=').append(getExternalIds()).append(',').append(' ');
    buf.append("searchType").append('=').append(JodaBeanUtils.toString(getSearchType()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExternalIdSearch}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code externalIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalId>> _externalIds = DirectMetaProperty.ofImmutable(
        this, "externalIds", ExternalIdSearch.class, (Class) Set.class);
    /**
     * The meta-property for the {@code searchType} property.
     */
    private final MetaProperty<ExternalIdSearchType> _searchType = DirectMetaProperty.ofImmutable(
        this, "searchType", ExternalIdSearch.class, ExternalIdSearchType.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "externalIds",
        "searchType");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          return _externalIds;
        case -710454014:  // searchType
          return _searchType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExternalIdSearch> builder() {
      return new ExternalIdSearch.Builder();
    }

    @Override
    public Class<? extends ExternalIdSearch> beanType() {
      return ExternalIdSearch.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code externalIds} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Set<ExternalId>> externalIds() {
      return _externalIds;
    }

    /**
     * The meta-property for the {@code searchType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ExternalIdSearchType> searchType() {
      return _searchType;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          return ((ExternalIdSearch) bean).getExternalIds();
        case -710454014:  // searchType
          return ((ExternalIdSearch) bean).getSearchType();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ExternalIdSearch}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ExternalIdSearch> {

    private Set<ExternalId> _externalIds = new HashSet<ExternalId>();
    private ExternalIdSearchType _searchType;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          return _externalIds;
        case -710454014:  // searchType
          return _searchType;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          this._externalIds = (Set<ExternalId>) newValue;
          break;
        case -710454014:  // searchType
          this._searchType = (ExternalIdSearchType) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ExternalIdSearch build() {
      return new ExternalIdSearch(
          _externalIds,
          _searchType);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("ExternalIdSearch.Builder{");
      buf.append("externalIds").append('=').append(JodaBeanUtils.toString(_externalIds)).append(',').append(' ');
      buf.append("searchType").append('=').append(JodaBeanUtils.toString(_searchType));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
