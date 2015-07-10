/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cds;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Immutable set of {@link CreditDefaultSwapIndexComponent} that
 * represents the CreditDefaultSwapIndexDefinitionSecurity components
 * <p>
 * It uses a comparator based on the ObligorCode of each components
 * as opposed to natural ordering of weight and name.
 * <p>
 * Note that ideally we would use a Map keyed on RED code with values
 * of the components, sorted by the values. However, standard maps
 * are sorted by keys so would not be usable. Instead this class
 * maintains a Map to ensure each RED code only appears once and
 * a sorted set of the components.
 */
@BeanDefinition(builderScope = "private")
public final class CDSIndexComponentBundle
    implements ImmutableBean, Iterable<CreditDefaultSwapIndexComponent>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Comparator to use for sorting if one is not specified by client
   */
  private static final Comparator<CreditDefaultSwapIndexComponent> DEFAULT_COMPARATOR = new CDSIndexComponentObligorComparator();

  /**
   * The set of cdsIndex components.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSortedSet<CreditDefaultSwapIndexComponent> _components;

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@link CDSIndexComponentBundle} from an array of
   * CreditDefaultSwapIndexComponents.
   *
   * @param components  an array of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(CreditDefaultSwapIndexComponent... components) {
    return create(Arrays.asList(components));
  }

  /**
   * Obtains a {@link CDSIndexComponentBundle} from a collection of
   * CreditDefaultSwapIndexComponents.
   *
   * @param components  the collection of components, no nulls, not null
   * @return the cdsIndex components bundle, not null
   */
  public static CDSIndexComponentBundle of(Iterable<CreditDefaultSwapIndexComponent> components) {
    return create(components);
  }

  /**
   * Obtains an {@link CDSIndexComponentBundle} from a collection of
   * {@link CreditDefaultSwapIndexComponent}.
   * 
   * @param components  the collection of components
   * @return the bundle, not null
   */
  private static CDSIndexComponentBundle create(Iterable<CreditDefaultSwapIndexComponent> components) {
    return new CDSIndexComponentBundle(components);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a cdsIndex components bundle from a set of cdsIndex component.
   * 
   * @param components  the set of components assigned, not null
   */
  @ImmutableConstructor
  private CDSIndexComponentBundle(Iterable<CreditDefaultSwapIndexComponent> components) {
    this(components, DEFAULT_COMPARATOR);
  }

  /**
   * Creates a cdsIndex components bundle from a set of cdsIndex
   * component and a comparator.
   *
   * @param components  the set of components assigned, not null
   */
  private CDSIndexComponentBundle(Iterable<CreditDefaultSwapIndexComponent> components,
                                  Comparator<? super CreditDefaultSwapIndexComponent> comparator) {
    ArgumentChecker.notEmpty(components, "components");
    ArgumentChecker.noNulls(components, "components");
    ArgumentChecker.notNull(comparator, "comparator");
    _components = ImmutableSortedSet.copyOf(comparator, deduplicate(components));
  }

  private static Iterable<CreditDefaultSwapIndexComponent> deduplicate(Iterable<CreditDefaultSwapIndexComponent> components) {
    Map<ExternalId, CreditDefaultSwapIndexComponent> redCodeMapping = Maps.newHashMap();
    for (CreditDefaultSwapIndexComponent component : components) {
      redCodeMapping.put(component.getObligorRedCode(), component);
    }
    return redCodeMapping.values();
  }

  //-------------------------------------------------------------------------
  // this method exists to place this map into the equals/hashCode check
  // this should not be necessary, but tests fail unless it is there
  @DerivedProperty
  private Map<ExternalId, CreditDefaultSwapIndexComponent> getRedCodeMapping() {
    Map<ExternalId, CreditDefaultSwapIndexComponent> redCodeMapping;
    redCodeMapping = Maps.uniqueIndex(_components, new Function<CreditDefaultSwapIndexComponent, ExternalId>() {
      @Override
      public ExternalId apply(CreditDefaultSwapIndexComponent input) {
        return input.getObligorRedCode();
      }
    });
    return redCodeMapping;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified
   * {@link CreditDefaultSwapIndexComponent}s added.
   * This instance is immutable and unaffected by this method call.
   *
   * @param components the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponents(final CreditDefaultSwapIndexComponent... components) {
    return withCDSIndexComponents(Arrays.asList(components));
  }

  /**
   * Returns a new {@link CDSIndexComponentBundle} with the specified
   * {@link CreditDefaultSwapIndexComponent}s added.
   * This instance is immutable and unaffected by this method call.
   * 
   * @param components the identifiers to add to the returned bundle, not null
   * @return the new bundle, not null
   */
  public CDSIndexComponentBundle withCDSIndexComponents(final Iterable<CreditDefaultSwapIndexComponent> components) {
    Map<ExternalId, CreditDefaultSwapIndexComponent> redCodeMapping = getRedCodeMapping();
    final Set<CreditDefaultSwapIndexComponent> updatedComponents = Sets.newLinkedHashSet(_components);
    for (CreditDefaultSwapIndexComponent component : components) {
      if (!component.equals(redCodeMapping.get(component.getObligorRedCode()))) {
        updatedComponents.add(component);
      }
    }
    return new CDSIndexComponentBundle(updatedComponents, _components.comparator());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a new bundle using a custom comparator for ordering.
   * Primarily useful for display.
   * <p>
   * NOTE: The comparator will not be transported across a network connection.
   * 
   * @param comparator comparator specifying how to order the ExternalIds
   * @return the new copy of the bundle, ordered by the comparator
   */
  public CDSIndexComponentBundle withCustomIdOrdering(final Comparator<CreditDefaultSwapIndexComponent> comparator) {
    // TODO: remove this method as it does not belong in the data structure world
    return new CDSIndexComponentBundle(_components, comparator);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of components in the bundle.
   * 
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _components.size();
  }

  /**
   * Returns true if this bundle contains no components.
   * 
   * @return true if this bundle contains no components, false otherwise
   */
  public boolean isEmpty() {
    return _components.isEmpty();
  }

  /**
   * Returns an iterator over the components in the bundle.
   * 
   * @return the components in the bundle, not null
   */
  @Override
  public Iterator<CreditDefaultSwapIndexComponent> iterator() {
    return _components.iterator();
  }

  private static class CDSIndexComponentObligorComparator implements Comparator<CreditDefaultSwapIndexComponent>, Serializable {
    /** Serialization version. */
    private static final long serialVersionUID = -520411860559280920L;

    @Override
    public int compare(final CreditDefaultSwapIndexComponent left, final CreditDefaultSwapIndexComponent right) {
      return left.getObligorRedCode().compareTo(right.getObligorRedCode());
    }
  }

  //-------------------------------------------------------------------------
  // class is far from perfect, with a derived property used to include data
  // eliminated by use of a comparator on the main sorted set
  // overriding equals/hash code ensures everything hangs together
  // really the class need fundamental rework
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CDSIndexComponentBundle other = (CDSIndexComponentBundle) obj;
      return JodaBeanUtils.equal(getComponents(), other.getComponents()) &&
          JodaBeanUtils.equal(getRedCodeMapping(), other.getRedCodeMapping());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getComponents());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRedCodeMapping());
    return hash;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CDSIndexComponentBundle}.
   * @return the meta-bean, not null
   */
  public static CDSIndexComponentBundle.Meta meta() {
    return CDSIndexComponentBundle.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CDSIndexComponentBundle.Meta.INSTANCE);
  }

  @Override
  public CDSIndexComponentBundle.Meta metaBean() {
    return CDSIndexComponentBundle.Meta.INSTANCE;
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
   * Gets the set of cdsIndex components.
   * @return the value of the property, not null
   */
  public ImmutableSortedSet<CreditDefaultSwapIndexComponent> getComponents() {
    return _components;
  }

  //-----------------------------------------------------------------------
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CDSIndexComponentBundle{");
    buf.append("components").append('=').append(getComponents()).append(',').append(' ');
    buf.append("redCodeMapping").append('=').append(JodaBeanUtils.toString(getRedCodeMapping()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CDSIndexComponentBundle}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code components} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSortedSet<CreditDefaultSwapIndexComponent>> _components = DirectMetaProperty.ofImmutable(
        this, "components", CDSIndexComponentBundle.class, (Class) ImmutableSortedSet.class);
    /**
     * The meta-property for the {@code redCodeMapping} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalId, CreditDefaultSwapIndexComponent>> _redCodeMapping = DirectMetaProperty.ofDerived(
        this, "redCodeMapping", CDSIndexComponentBundle.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "components",
        "redCodeMapping");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -447446250:  // components
          return _components;
        case 256084176:  // redCodeMapping
          return _redCodeMapping;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CDSIndexComponentBundle> builder() {
      return new CDSIndexComponentBundle.Builder();
    }

    @Override
    public Class<? extends CDSIndexComponentBundle> beanType() {
      return CDSIndexComponentBundle.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code components} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSortedSet<CreditDefaultSwapIndexComponent>> components() {
      return _components;
    }

    /**
     * The meta-property for the {@code redCodeMapping} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Map<ExternalId, CreditDefaultSwapIndexComponent>> redCodeMapping() {
      return _redCodeMapping;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -447446250:  // components
          return ((CDSIndexComponentBundle) bean).getComponents();
        case 256084176:  // redCodeMapping
          return ((CDSIndexComponentBundle) bean).getRedCodeMapping();
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
   * The bean-builder for {@code CDSIndexComponentBundle}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<CDSIndexComponentBundle> {

    private SortedSet<CreditDefaultSwapIndexComponent> _components = new TreeSet<CreditDefaultSwapIndexComponent>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -447446250:  // components
          return _components;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -447446250:  // components
          this._components = (SortedSet<CreditDefaultSwapIndexComponent>) newValue;
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
    public CDSIndexComponentBundle build() {
      return new CDSIndexComponentBundle(
          _components);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("CDSIndexComponentBundle.Builder{");
      buf.append("components").append('=').append(JodaBeanUtils.toString(_components));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
