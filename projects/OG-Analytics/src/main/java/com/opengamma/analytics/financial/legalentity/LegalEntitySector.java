/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
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
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.types.ParameterizedTypeImpl;
import com.opengamma.util.types.VariantType;

/**
 * Gets the sector of an {@link LegalEntity}.
 */
@BeanDefinition
public class LegalEntitySector implements LegalEntityFilter<LegalEntity>, Bean {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * True if the sector name is to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useSectorName;

  /**
   * True if the classification name is to be used as a filter.
   */
  @PropertyDefinition
  private boolean _useClassificationName;

  /**
   * A set of classifications to be used to in the filter.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Set<String> _classifications;

  /**
   * A set of types that the classifications might result in.
   */
  @PropertyDefinition
  private Set<? extends Type> _classificationValueTypes;

  /**
   * For the builder.
   */
  /* package */LegalEntitySector() {
    setUseSectorName(false);
    setUseClassificationName(false);
    setClassifications(Collections.<String>emptySet());
  }

  /**
   * @param useSectorName True if the sector name is to be used as a filter
   * @param useClassificationName True if the classification name is to be used as a filter
   * @param classifications A set of classifications to be used in the filter, not null. Can be empty
   */
  public LegalEntitySector(final boolean useSectorName, final boolean useClassificationName, final Set<String> classifications) {
    setUseSectorName(useSectorName);
    setUseClassificationName(useClassificationName);
    setClassifications(classifications);
  }

  @Override
  public Object getFilteredData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "legal entity");
    if (!(_useSectorName || _useClassificationName)) {
      return legalEntity.getSector();
    }
    final Sector sector = legalEntity.getSector();
    final Set<Object> selections = new HashSet<>();
    if (_useSectorName) {
      selections.add(sector.getName());
    }
    int classificationCount = 0;
    if (_useClassificationName) {
      final Map<String, Object> classifications = sector.getClassifications().toMap();
      if (classifications.isEmpty()) {
        throw new IllegalStateException("Sector " + legalEntity.getSector() + " does not contain any classifications");
      }
      for (final Map.Entry<String, Object> entry : classifications.entrySet()) {
        if (_classifications.contains(entry.getKey())) {
          selections.add(entry.getValue());
          classificationCount++;
        }
      }
    }
    if (classificationCount != _classifications.size()) {
      throw new IllegalStateException("Classifications " + sector.getClassifications() + " do not contain matches for " + _classifications);
    }
    return selections;
  }

  @Override
  public Type getFilteredDataType() {
    if (!(_useSectorName || _useClassificationName)) {
      return LegalEntity.meta().sector().propertyGenericType();
    }
    Type setMember = null;
    if (_useSectorName) {
      // Set may contain a String
      setMember = VariantType.either(setMember, String.class);
    }
    if (_useClassificationName) {
      Set<? extends Type> types = getClassificationValueTypes();
      if ((types == null) || types.isEmpty()) {
        // Arbitrary objects. Not good.
        setMember = VariantType.either(setMember, Object.class);
      } else {
        // Union of possible types.
        for (Type type : types) {
          setMember = VariantType.either(setMember, type);
        }
      }
    }
    return ParameterizedTypeImpl.of(Set.class, setMember);
  }

  /**
   * Sets the agencies with which to filter ratings. This also sets the {@link LegalEntitySector#_useClassificationName} field to true.
   * 
   * @param classifications The new value of the property, not null
   */
  public void setClassifications(final Set<String> classifications) {
    JodaBeanUtils.notNull(classifications, "classifications");
    if (!classifications.isEmpty()) {
      setUseClassificationName(true);
    }
    this._classifications = classifications;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code LegalEntitySector}.
   * @return the meta-bean, not null
   */
  public static LegalEntitySector.Meta meta() {
    return LegalEntitySector.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LegalEntitySector.Meta.INSTANCE);
  }

  @Override
  public LegalEntitySector.Meta metaBean() {
    return LegalEntitySector.Meta.INSTANCE;
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
   * Gets true if the sector name is to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseSectorName() {
    return _useSectorName;
  }

  /**
   * Sets true if the sector name is to be used as a filter.
   * @param useSectorName  the new value of the property
   */
  public void setUseSectorName(boolean useSectorName) {
    this._useSectorName = useSectorName;
  }

  /**
   * Gets the the {@code useSectorName} property.
   * @return the property, not null
   */
  public final Property<Boolean> useSectorName() {
    return metaBean().useSectorName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if the classification name is to be used as a filter.
   * @return the value of the property
   */
  public boolean isUseClassificationName() {
    return _useClassificationName;
  }

  /**
   * Sets true if the classification name is to be used as a filter.
   * @param useClassificationName  the new value of the property
   */
  public void setUseClassificationName(boolean useClassificationName) {
    this._useClassificationName = useClassificationName;
  }

  /**
   * Gets the the {@code useClassificationName} property.
   * @return the property, not null
   */
  public final Property<Boolean> useClassificationName() {
    return metaBean().useClassificationName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a set of classifications to be used to in the filter.
   * @return the value of the property, not null
   */
  public Set<String> getClassifications() {
    return _classifications;
  }

  /**
   * Gets the the {@code classifications} property.
   * @return the property, not null
   */
  public final Property<Set<String>> classifications() {
    return metaBean().classifications().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a set of types that the classifications might result in.
   * @return the value of the property
   */
  public Set<? extends Type> getClassificationValueTypes() {
    return _classificationValueTypes;
  }

  /**
   * Sets a set of types that the classifications might result in.
   * @param classificationValueTypes  the new value of the property
   */
  public void setClassificationValueTypes(Set<? extends Type> classificationValueTypes) {
    this._classificationValueTypes = classificationValueTypes;
  }

  /**
   * Gets the the {@code classificationValueTypes} property.
   * @return the property, not null
   */
  public final Property<Set<? extends Type>> classificationValueTypes() {
    return metaBean().classificationValueTypes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public LegalEntitySector clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      LegalEntitySector other = (LegalEntitySector) obj;
      return (isUseSectorName() == other.isUseSectorName()) &&
          (isUseClassificationName() == other.isUseClassificationName()) &&
          JodaBeanUtils.equal(getClassifications(), other.getClassifications()) &&
          JodaBeanUtils.equal(getClassificationValueTypes(), other.getClassificationValueTypes());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseSectorName());
    hash = hash * 31 + JodaBeanUtils.hashCode(isUseClassificationName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifications());
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassificationValueTypes());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("LegalEntitySector{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("useSectorName").append('=').append(JodaBeanUtils.toString(isUseSectorName())).append(',').append(' ');
    buf.append("useClassificationName").append('=').append(JodaBeanUtils.toString(isUseClassificationName())).append(',').append(' ');
    buf.append("classifications").append('=').append(JodaBeanUtils.toString(getClassifications())).append(',').append(' ');
    buf.append("classificationValueTypes").append('=').append(JodaBeanUtils.toString(getClassificationValueTypes())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LegalEntitySector}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code useSectorName} property.
     */
    private final MetaProperty<Boolean> _useSectorName = DirectMetaProperty.ofReadWrite(
        this, "useSectorName", LegalEntitySector.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code useClassificationName} property.
     */
    private final MetaProperty<Boolean> _useClassificationName = DirectMetaProperty.ofReadWrite(
        this, "useClassificationName", LegalEntitySector.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code classifications} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _classifications = DirectMetaProperty.ofReadWrite(
        this, "classifications", LegalEntitySector.class, (Class) Set.class);
    /**
     * The meta-property for the {@code classificationValueTypes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<? extends Type>> _classificationValueTypes = DirectMetaProperty.ofReadWrite(
        this, "classificationValueTypes", LegalEntitySector.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "useSectorName",
        "useClassificationName",
        "classifications",
        "classificationValueTypes");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -805976072:  // useSectorName
          return _useSectorName;
        case -555840136:  // useClassificationName
          return _useClassificationName;
        case -1032042163:  // classifications
          return _classifications;
        case -412942834:  // classificationValueTypes
          return _classificationValueTypes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends LegalEntitySector> builder() {
      return new DirectBeanBuilder<LegalEntitySector>(new LegalEntitySector());
    }

    @Override
    public Class<? extends LegalEntitySector> beanType() {
      return LegalEntitySector.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code useSectorName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useSectorName() {
      return _useSectorName;
    }

    /**
     * The meta-property for the {@code useClassificationName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useClassificationName() {
      return _useClassificationName;
    }

    /**
     * The meta-property for the {@code classifications} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> classifications() {
      return _classifications;
    }

    /**
     * The meta-property for the {@code classificationValueTypes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<? extends Type>> classificationValueTypes() {
      return _classificationValueTypes;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -805976072:  // useSectorName
          return ((LegalEntitySector) bean).isUseSectorName();
        case -555840136:  // useClassificationName
          return ((LegalEntitySector) bean).isUseClassificationName();
        case -1032042163:  // classifications
          return ((LegalEntitySector) bean).getClassifications();
        case -412942834:  // classificationValueTypes
          return ((LegalEntitySector) bean).getClassificationValueTypes();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -805976072:  // useSectorName
          ((LegalEntitySector) bean).setUseSectorName((Boolean) newValue);
          return;
        case -555840136:  // useClassificationName
          ((LegalEntitySector) bean).setUseClassificationName((Boolean) newValue);
          return;
        case -1032042163:  // classifications
          ((LegalEntitySector) bean).setClassifications((Set<String>) newValue);
          return;
        case -412942834:  // classificationValueTypes
          ((LegalEntitySector) bean).setClassificationValueTypes((Set<? extends Type>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((LegalEntitySector) bean)._classifications, "classifications");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
