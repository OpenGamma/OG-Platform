/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;
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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable bundle of external identifiers.
 * <p>
 * A bundle allows multiple {@link ExternalId external identifiers} to be grouped together when they all refer to the same conceptual object. For example, a Reuters RIC and Bloomberg Ticker might both
 * refer to the same equity.
 * <p>
 * The bundle holds a <i>set</i> of external identifiers, not a <i>map</i> from scheme to value. This permits multiple values within the same scheme to refer to the same conceptual object. For
 * example, a renamed ticker could be grouped as both the old and new value. In general however, each external identifier in a bundle will be in a different scheme.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
@BeanDefinition(builderScope = "private")
public final class ExternalIdBundle implements ImmutableBean, Iterable<ExternalId>,
    Serializable, Comparable<ExternalIdBundle>, ExternalBundleIdentifiable, ExternalIdOrBundle {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty bundle.
   */
  public static final ExternalIdBundle EMPTY = new ExternalIdBundle();

  /**
   * The set of identifiers in the bundle.
   * <p>
   * The identifiers are sorted in the natural order of {@link ExternalId} to provide
   * greater consistency in applications. The sort order is not suitable for a GUI.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSortedSet<ExternalId> _externalIds;
  /**
   * The cached hash code.
   */
  private transient int _hashCode;  // safe via racy single check idiom

  /**
   * Obtains an {@code ExternalIdBundle} from a single scheme and value. This is most useful for testing, as a bundle normally contains more than one identifier.
   * 
   * @param scheme the scheme of the single external identifier, not empty, not null
   * @param value the value of the single external identifier, not empty, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(final ExternalScheme scheme, final String value) {
    return of(ExternalId.of(scheme, value));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a single scheme and value. This is most useful for testing, as a bundle normally contains more than one identifier.
   * 
   * @param scheme the scheme of the single external identifier, not empty, not null
   * @param value the value of the single external identifier, not empty, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(final String scheme, final String value) {
    return of(ExternalId.of(scheme, value));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from an identifier.
   * 
   * @param externalId the external identifier to wrap in a bundle, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    return new ExternalIdBundle(ImmutableSortedSet.of(externalId));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from an array of identifiers.
   * 
   * @param externalIds the array of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(final ExternalId... externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return new ExternalIdBundle(ImmutableSortedSet.copyOf(externalIds));
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a collection of identifiers.
   * 
   * @param externalIds the collection of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundle of(final Iterable<ExternalId> externalIds) {
    ArgumentChecker.noNulls(externalIds, "externalIds");
    return create(externalIds);
  }

  /**
   * Parses a list of strings to an {@code ExternalIdBundle}.
   * <p>
   * This uses {@link ExternalId#parse(String)} to parse each string in the input collection.
   * 
   * @param strs the external identifiers to parse, not null
   * @return the bundle, not null
   * @throws IllegalArgumentException if any identifier cannot be parsed
   */
  public static ExternalIdBundle parse(final Iterable<String> strs) {
    ArgumentChecker.noNulls(strs, "strs");
    final List<ExternalId> externalIds = new ArrayList<ExternalId>();
    for (final String str : strs) {
      externalIds.add(ExternalId.parse(str));
    }
    return create(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundle} from a collection of identifiers.
   * 
   * @param externalIds the collection of external identifiers, validated
   * @return the bundle, not null
   */
  private static ExternalIdBundle create(final Iterable<ExternalId> externalIds) {
    return new ExternalIdBundle(ImmutableSortedSet.copyOf(externalIds));
  }

  /**
   * Creates an empty bundle.
   */
  private ExternalIdBundle() {
    _externalIds = ImmutableSortedSet.of();
  }

  /**
   * Creates a bundle from a set of identifiers.
   * 
   * @param identifiers the set of identifiers, assigned, not null
   */
  private ExternalIdBundle(final ImmutableSortedSet<ExternalId> identifiers) {
    _externalIds = identifiers;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external identifier for the specified scheme.
   * <p>
   * This returns the first identifier in the internal set that matches. The set is not sorted, so this method is not consistent.
   * 
   * @param scheme the scheme to query, null returns null
   * @return the identifier, null if not found
   */
  public ExternalId getExternalId(final ExternalScheme scheme) {
    for (final ExternalId identifier : _externalIds) {
      if (ObjectUtils.equals(scheme, identifier.getScheme())) {
        return identifier;
      }
    }
    return null;
  }

  /**
   * Returns all identifiers for a scheme.
   * 
   * @param scheme The scheme, null returns an empty set
   * @return All identifiers for the scheme, not null
   */
  public Set<ExternalId> getExternalIds(final ExternalScheme scheme) {
    final Set<ExternalId> ids = Sets.newHashSet();
    for (final ExternalId id : _externalIds) {
      if (Objects.equal(scheme, id.getScheme())) {
        ids.add(id);
      }
    }
    return ids;
  }

  /**
   * Returns all identifiers for a scheme.
   * 
   * @param scheme The scheme, null returns an empty set
   * @return All values for the scheme, not null
   */
  public Set<String> getValues(final ExternalScheme scheme) {
    final Set<String> values = Sets.newHashSet();
    for (final ExternalId id : _externalIds) {
      if (Objects.equal(scheme, id.getScheme())) {
        values.add(id.getValue());
      }
    }
    return values;
  }

  /**
   * Gets the identifier value for the specified scheme.
   * <p>
   * This returns the first identifier in the internal set that matches. The set is not sorted, so this method is not consistent.
   * 
   * @param scheme the scheme to query, null returns null
   * @return the identifier value, null if not found
   */
  public String getValue(final ExternalScheme scheme) {
    for (final ExternalId identifier : _externalIds) {
      if (ObjectUtils.equals(scheme, identifier.getScheme())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle with the specified identifier added. This instance is immutable and unaffected by this method call.
   * 
   * @param externalId the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundle withExternalId(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    final Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
    if (ids.add(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with the specified identifier added. This instance is immutable and unaffected by this method call.
   * 
   * @param externalIds the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundle withExternalIds(final Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    final Set<ExternalId> toAdd = ImmutableSortedSet.copyOf(externalIds);
    final Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
    if (ids.addAll(toAdd) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with the specified identifier removed. This instance is immutable and unaffected by this method call.
   * 
   * @param externalId the identifier to remove from the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundle withoutExternalId(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    final Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds);
    if (ids.remove(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with all references to the specified scheme removed. This instance is immutable and unaffected by this method call.
   * 
   * @param scheme the scheme to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public ExternalIdBundle withoutScheme(final ExternalScheme scheme) {
    final Set<ExternalId> ids = new HashSet<ExternalId>(_externalIds.size());
    for (final ExternalId id : _externalIds) {
      if (id.isScheme(scheme) == false) {
        ids.add(id);
      }
    }
    return create(ids);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of identifiers in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _externalIds.size();
  }

  /**
   * Returns true if this bundle contains no identifiers.
   * 
   * @return true if this bundle contains no identifiers, false otherwise
   */
  public boolean isEmpty() {
    return _externalIds.isEmpty();
  }

  /**
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  @Override
  public Iterator<ExternalId> iterator() {
    return _externalIds.iterator();
  }

  /**
   * Checks if this bundle contains all the keys from the specified bundle.
   * 
   * @param bundle the bundle to search for, empty returns true, not null
   * @return true if this bundle contains all the keys from the specified bundle
   */
  public boolean containsAll(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (final ExternalId externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this bundle contains any key from the specified bundle.
   * 
   * @param bundle the bundle to search for, empty returns false, not null
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean containsAny(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (final ExternalId externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this bundle contains the specified key.
   * 
   * @param externalId the identifier to search for, null returns false
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean contains(final ExternalId externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    final List<String> list = new ArrayList<String>();
    for (final ExternalId id : this) {
      list.add(id.toString());
    }
    return list;
  }

  /**
   * Converts this to an external identifier bundle.
   * <p>
   * This method trivially returns {@code this}
   * 
   * @return {@code this}, not null
   */
  @Override
  public ExternalIdBundle toBundle() {
    return this;
  }

  /**
   * Converts this to an external identifier bundle.
   * <p>
   * This method trivially returns {@code this}
   * 
   * @return {@code this}, not null
   */
  @Override
  public ExternalIdBundle getExternalIdBundle() {
    return this;
  }

  //-------------------------------------------------------------------
  /**
   * Compares the bundles.
   * 
   * @param other the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(final ExternalIdBundle other) {
    final Set<ExternalId> mySet = getExternalIds();
    final Set<ExternalId> otherSet = other.getExternalIds();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<ExternalId> myList = new ArrayList<ExternalId>(mySet); // already sorted
    final List<ExternalId> otherList = new ArrayList<ExternalId>(otherSet); // already sorted
    for (int i = 0; i < myList.size(); i++) {
      final int c = myList.get(i).compareTo(otherList.get(i));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdBundle) {
      final ExternalIdBundle other = (ExternalIdBundle) obj;
      return _externalIds.equals(other._externalIds);
    }
    return false;
  }

  @Override
  public int hashCode() {
    // racy single check idiom allows non-volatile variable
    // requires only one read and one write of non-volatile
    int hashCode = _hashCode;
    if (hashCode == 0) {
      hashCode = 31 + _externalIds.hashCode();
      _hashCode = hashCode;
    }
    return hashCode;
  }

  /**
   * Returns a string representation of the bundle.
   * 
   * @return a string representation of the bundle, not null
   */
  @Override
  public String toString() {
    return new StrBuilder()
        .append("Bundle")
        .append("[")
        .appendWithSeparators(_externalIds, ", ")
        .append("]")
        .toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExternalIdBundle}.
   * @return the meta-bean, not null
   */
  public static ExternalIdBundle.Meta meta() {
    return ExternalIdBundle.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExternalIdBundle.Meta.INSTANCE);
  }

  private ExternalIdBundle(
      SortedSet<ExternalId> externalIds) {
    JodaBeanUtils.notNull(externalIds, "externalIds");
    this._externalIds = ImmutableSortedSet.copyOfSorted(externalIds);
  }

  @Override
  public ExternalIdBundle.Meta metaBean() {
    return ExternalIdBundle.Meta.INSTANCE;
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
   * Gets the set of identifiers in the bundle.
   * <p>
   * The identifiers are sorted in the natural order of {@link ExternalId} to provide
   * greater consistency in applications. The sort order is not suitable for a GUI.
   * @return the value of the property, not null
   */
  public ImmutableSortedSet<ExternalId> getExternalIds() {
    return _externalIds;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExternalIdBundle}.
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
    private final MetaProperty<ImmutableSortedSet<ExternalId>> _externalIds = DirectMetaProperty.ofImmutable(
        this, "externalIds", ExternalIdBundle.class, (Class) ImmutableSortedSet.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "externalIds");

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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExternalIdBundle> builder() {
      return new ExternalIdBundle.Builder();
    }

    @Override
    public Class<? extends ExternalIdBundle> beanType() {
      return ExternalIdBundle.class;
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
    public MetaProperty<ImmutableSortedSet<ExternalId>> externalIds() {
      return _externalIds;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          return ((ExternalIdBundle) bean).getExternalIds();
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
   * The bean-builder for {@code ExternalIdBundle}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ExternalIdBundle> {

    private SortedSet<ExternalId> _externalIds = new TreeSet<ExternalId>();

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
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          this._externalIds = (SortedSet<ExternalId>) newValue;
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
    public ExternalIdBundle build() {
      return new ExternalIdBundle(
          _externalIds);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ExternalIdBundle.Builder{");
      buf.append("externalIds").append('=').append(JodaBeanUtils.toString(_externalIds));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
