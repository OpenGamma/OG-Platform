/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * A bundle of external identifiers with validity dates.
 * <p>
 * This is similar to {@link ExternalIdBundle} but permits each external identifier
 * to be limited by validity dates.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class ExternalIdBundleWithDates implements ImmutableBean,
    Iterable<ExternalIdWithDates>, Serializable, Comparable<ExternalIdBundleWithDates> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Singleton empty bundle.
   */
  public static final ExternalIdBundleWithDates EMPTY = new ExternalIdBundleWithDates();

  /**
   * The set of identifiers in the bundle.
   * <p>
   * The identifiers are sorted in the natural order of {@link ExternalId} to provide
   * greater consistency in applications. The sort order is not suitable for a GUI.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSortedSet<ExternalIdWithDates> _externalIds;
  /**
   * The cached hash code.
   */
  private transient int _hashCode;  // safe via racy single check idiom

  //-------------------------------------------------------------------------
  /**
   * Obtains an {@code ExternalIdBundleWithDates} from an array of identifiers.
   * 
   * @param externalIds  the array of external identifiers, no nulls, not null
   * @return the bundle, not null
   */
  public static ExternalIdBundleWithDates of(ExternalIdWithDates... externalIds) {
    ArgumentChecker.notNull(externalIds, "identifiers");
    return new ExternalIdBundleWithDates(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from an iterable of identifiers.
   * 
   * @param externalIds the iterable of external identifiers, not null
   * @return the identifier bundle with dates set to null, not null
   */
  public static ExternalIdBundleWithDates of(Iterable<ExternalIdWithDates> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    return create(externalIds);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from a bundle of identifiers.
   * 
   * @param bundle the identifier bundle, not null
   * @return the identifier bundle with dates set to null, not null
   */
  public static ExternalIdBundleWithDates of(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Set<ExternalIdWithDates> identifiers = new HashSet<ExternalIdWithDates>();
    for (ExternalId identifier : bundle) {
      identifiers.add(ExternalIdWithDates.of(identifier, null, null));
    }
    return new ExternalIdBundleWithDates(identifiers);
  }

  /**
   * Obtains an {@code ExternalIdBundleWithDates} from a collection of identifiers.
   * 
   * @param externalIds  the collection of external identifiers, validated
   * @return the bundle, not null
   */
  private static ExternalIdBundleWithDates create(Iterable<ExternalIdWithDates> externalIds) {
    return new ExternalIdBundleWithDates(ImmutableSortedSet.copyOf(externalIds));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an empty bundle.
   */
  private ExternalIdBundleWithDates() {
    _externalIds = ImmutableSortedSet.of();
  }

  /**
   * Creates a bundle from an array of identifiers.
   * 
   * @param externalIds  the array of identifiers, null returns an empty bundle
   */
  public ExternalIdBundleWithDates(ExternalIdWithDates... externalIds) {
    if ((externalIds == null) || (externalIds.length == 0)) {
      _externalIds = ImmutableSortedSet.of();
    } else {
      ArgumentChecker.noNulls(externalIds, "externalIds");
      _externalIds = ImmutableSortedSet.copyOf(externalIds);
    }
  }

  /**
   * Creates a bundle from a collection of identifiers.
   * 
   * @param externalIds  the collection of identifiers, null returns an empty bundle, no nulls in array
   */
  public ExternalIdBundleWithDates(Collection<? extends ExternalIdWithDates> externalIds) {
    if (externalIds == null) {
      _externalIds = ImmutableSortedSet.of();
    } else {
      ArgumentChecker.noNulls(externalIds, "identifiers");
      _externalIds = ImmutableSortedSet.copyOf(externalIds);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle with the specified identifier added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withExternalId(ExternalIdWithDates externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds);
    if (ids.add(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with the specified identifier removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param externalId  the identifier to remove from the returned bundle, not null
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withoutExternalId(ExternalIdWithDates externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds);
    if (ids.remove(externalId) == false) {
      return this;
    }
    return create(ids);
  }

  /**
   * Returns a new bundle with all references to the specified scheme removed.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param scheme  the scheme to remove from the returned bundle, null ignored
   * @return the new bundle, not null
   */
  public ExternalIdBundleWithDates withoutScheme(ExternalScheme scheme) {
    Set<ExternalIdWithDates> ids = new HashSet<ExternalIdWithDates>(_externalIds.size());
    for (ExternalIdWithDates id : _externalIds) {
      if (id.getExternalId().isScheme(scheme) == false) {
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
   * Returns an iterator over the identifiers in the bundle.
   * 
   * @return the identifiers in the bundle, not null
   */
  @Override
  public Iterator<ExternalIdWithDates> iterator() {
    return _externalIds.iterator();
  }

  /**
   * Checks if this bundle contains all the keys from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns true, not null
   * @return true if this bundle contains all the keys from the specified bundle
   */
  public boolean containsAll(ExternalIdBundleWithDates bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalIdWithDates externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if this bundle contains any key from the specified bundle.
   * 
   * @param bundle  the bundle to search for, empty returns false, not null
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean containsAny(ExternalIdBundleWithDates bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalIdWithDates externalId : bundle.getExternalIds()) {
      if (_externalIds.contains(externalId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if this bundle contains the specified key.
   * 
   * @param externalId  the identifier to search for, null returns false
   * @return true if this bundle contains any key from the specified bundle
   */
  public boolean contains(ExternalIdWithDates externalId) {
    return externalId != null && _externalIds.contains(externalId);
  }

  /**
   * Converts this bundle to a list of formatted strings.
   * 
   * @return the list of identifiers as strings, not null
   */
  public List<String> toStringList() {
    List<String> list = new ArrayList<String>();
    for (ExternalIdWithDates id : this) {
      list.add(id.toString());
    }
    return list;
  }

  /**
   * Returns the bundle without dates.
   * <p>
   * This returns all the identifiers ignoring the validity dates.
   * See {@link #toBundle(LocalDate)} for a better choice.
   * 
   * @return the equivalent bundle, without the dates, not null
   */
  public ExternalIdBundle toBundle() {
    return toBundle(null);
  }

  /**
   * Returns the bundle without dates as of a specific date.
   * 
   * @param validOn  the validity date, null returns all
   * @return the equivalent bundle, without the dates, not null
   */
  public ExternalIdBundle toBundle(LocalDate validOn) {
    Set<ExternalId> ids = new HashSet<ExternalId>();
    for (ExternalIdWithDates identifier : _externalIds) {
      if (identifier.isValidOn(validOn)) {
        ids.add(identifier.toExternalId());
      }
    }
    return ExternalIdBundle.of(ids);
  }

  //-------------------------------------------------------------------
  /**
   * Compares the bundles.
   * 
   * @param other  the other external identifier, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(ExternalIdBundleWithDates other) {
    final Set<ExternalIdWithDates> mySet = getExternalIds();
    final Set<ExternalIdWithDates> otherSet = other.getExternalIds();
    if (mySet.size() < otherSet.size()) {
      return -1;
    }
    if (mySet.size() > otherSet.size()) {
      return 1;
    }
    final List<ExternalIdWithDates> myList = new ArrayList<ExternalIdWithDates>(mySet); // already sorted as TreeSet
    final List<ExternalIdWithDates> otherList = new ArrayList<ExternalIdWithDates>(otherSet); // already sorted as TreeSet
    for (int i = 0; i < myList.size(); i++) {
      int c = myList.get(i).compareTo(otherList.get(i));
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ExternalIdBundleWithDates) {
      ExternalIdBundleWithDates other = (ExternalIdBundleWithDates) obj;
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
        .append("BundleWithDates")
        .append("[")
        .appendWithSeparators(_externalIds, ", ")
        .append("]")
        .toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExternalIdBundleWithDates}.
   * @return the meta-bean, not null
   */
  public static ExternalIdBundleWithDates.Meta meta() {
    return ExternalIdBundleWithDates.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExternalIdBundleWithDates.Meta.INSTANCE);
  }

  private ExternalIdBundleWithDates(
      SortedSet<ExternalIdWithDates> externalIds) {
    JodaBeanUtils.notNull(externalIds, "externalIds");
    this._externalIds = ImmutableSortedSet.copyOfSorted(externalIds);
  }

  @Override
  public ExternalIdBundleWithDates.Meta metaBean() {
    return ExternalIdBundleWithDates.Meta.INSTANCE;
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
  public ImmutableSortedSet<ExternalIdWithDates> getExternalIds() {
    return _externalIds;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExternalIdBundleWithDates}.
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
    private final MetaProperty<ImmutableSortedSet<ExternalIdWithDates>> _externalIds = DirectMetaProperty.ofImmutable(
        this, "externalIds", ExternalIdBundleWithDates.class, (Class) ImmutableSortedSet.class);
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
    public BeanBuilder<? extends ExternalIdBundleWithDates> builder() {
      return new ExternalIdBundleWithDates.Builder();
    }

    @Override
    public Class<? extends ExternalIdBundleWithDates> beanType() {
      return ExternalIdBundleWithDates.class;
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
    public MetaProperty<ImmutableSortedSet<ExternalIdWithDates>> externalIds() {
      return _externalIds;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1153096979:  // externalIds
          return ((ExternalIdBundleWithDates) bean).getExternalIds();
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
   * The bean-builder for {@code ExternalIdBundleWithDates}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<ExternalIdBundleWithDates> {

    private SortedSet<ExternalIdWithDates> _externalIds = new TreeSet<ExternalIdWithDates>();

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
          this._externalIds = (SortedSet<ExternalIdWithDates>) newValue;
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
    public ExternalIdBundleWithDates build() {
      return new ExternalIdBundleWithDates(
          _externalIds);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ExternalIdBundleWithDates.Builder{");
      buf.append("externalIds").append('=').append(JodaBeanUtils.toString(_externalIds));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
