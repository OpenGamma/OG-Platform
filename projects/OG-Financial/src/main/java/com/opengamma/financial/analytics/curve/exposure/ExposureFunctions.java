/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.id.ExternalId;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
@BeanDefinition
@Config(description = "Exposure functions", group = ConfigGroups.CURVES)
public class ExposureFunctions extends DirectBean implements Serializable, UniqueIdentifiable, MutableUniqueIdentifiable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The unique id.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;

  /**
   * The name of this configuration.
   */
  @PropertyDefinition(validate = "notNull")
  private String _name;

  /**
   * A list of exposure functions and curve construction configuration names in order of priority.
   */
  @PropertyDefinition(validate = "notNull")
  private List<String> _exposureFunctions;

  /**
   * A map of ids to curve configuration names.
   */
  @PropertyDefinition(validate = "notNull")
  private Map<ExternalId, String> _idsToNames;

  /**
   * For the builder
   */
  /* package */ExposureFunctions() {
  }

  /**
   * @param name The name of the configuration, not null
   * @param exposureFunctions The list of exposure functions and curve construction configuration names in order, not null
   * @param idsToNames A map of ids to curve configuration names, not null
   * of priority, not null or empty
   */
  public ExposureFunctions(final String name, final List<String> exposureFunctions, final Map<ExternalId, String> idsToNames) {
    ArgumentChecker.notEmpty(exposureFunctions, "exposure functions");
    setName(name);
    setExposureFunctions(exposureFunctions);
    setIdsToNames(idsToNames);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ExposureFunctions}.
   * @return the meta-bean, not null
   */
  public static ExposureFunctions.Meta meta() {
    return ExposureFunctions.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ExposureFunctions.Meta.INSTANCE);
  }

  @Override
  public ExposureFunctions.Meta metaBean() {
    return ExposureFunctions.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique id.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique id.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name of this configuration.
   * @return the value of the property, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of this configuration.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
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
   * Gets a list of exposure functions and curve construction configuration names in order of priority.
   * @return the value of the property, not null
   */
  public List<String> getExposureFunctions() {
    return _exposureFunctions;
  }

  /**
   * Sets a list of exposure functions and curve construction configuration names in order of priority.
   * @param exposureFunctions  the new value of the property, not null
   */
  public void setExposureFunctions(List<String> exposureFunctions) {
    JodaBeanUtils.notNull(exposureFunctions, "exposureFunctions");
    this._exposureFunctions = exposureFunctions;
  }

  /**
   * Gets the the {@code exposureFunctions} property.
   * @return the property, not null
   */
  public final Property<List<String>> exposureFunctions() {
    return metaBean().exposureFunctions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a map of ids to curve configuration names.
   * @return the value of the property, not null
   */
  public Map<ExternalId, String> getIdsToNames() {
    return _idsToNames;
  }

  /**
   * Sets a map of ids to curve configuration names.
   * @param idsToNames  the new value of the property, not null
   */
  public void setIdsToNames(Map<ExternalId, String> idsToNames) {
    JodaBeanUtils.notNull(idsToNames, "idsToNames");
    this._idsToNames = idsToNames;
  }

  /**
   * Gets the the {@code idsToNames} property.
   * @return the property, not null
   */
  public final Property<Map<ExternalId, String>> idsToNames() {
    return metaBean().idsToNames().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ExposureFunctions clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ExposureFunctions other = (ExposureFunctions) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getExposureFunctions(), other.getExposureFunctions()) &&
          JodaBeanUtils.equal(getIdsToNames(), other.getIdsToNames());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExposureFunctions());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdsToNames());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("ExposureFunctions{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("exposureFunctions").append('=').append(JodaBeanUtils.toString(getExposureFunctions())).append(',').append(' ');
    buf.append("idsToNames").append('=').append(JodaBeanUtils.toString(getIdsToNames())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ExposureFunctions}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ExposureFunctions.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ExposureFunctions.class, String.class);
    /**
     * The meta-property for the {@code exposureFunctions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<String>> _exposureFunctions = DirectMetaProperty.ofReadWrite(
        this, "exposureFunctions", ExposureFunctions.class, (Class) List.class);
    /**
     * The meta-property for the {@code idsToNames} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<ExternalId, String>> _idsToNames = DirectMetaProperty.ofReadWrite(
        this, "idsToNames", ExposureFunctions.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "exposureFunctions",
        "idsToNames");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3373707:  // name
          return _name;
        case -1787010476:  // exposureFunctions
          return _exposureFunctions;
        case -462409803:  // idsToNames
          return _idsToNames;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ExposureFunctions> builder() {
      return new DirectBeanBuilder<ExposureFunctions>(new ExposureFunctions());
    }

    @Override
    public Class<? extends ExposureFunctions> beanType() {
      return ExposureFunctions.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code exposureFunctions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<String>> exposureFunctions() {
      return _exposureFunctions;
    }

    /**
     * The meta-property for the {@code idsToNames} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<ExternalId, String>> idsToNames() {
      return _idsToNames;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ExposureFunctions) bean).getUniqueId();
        case 3373707:  // name
          return ((ExposureFunctions) bean).getName();
        case -1787010476:  // exposureFunctions
          return ((ExposureFunctions) bean).getExposureFunctions();
        case -462409803:  // idsToNames
          return ((ExposureFunctions) bean).getIdsToNames();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ExposureFunctions) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((ExposureFunctions) bean).setName((String) newValue);
          return;
        case -1787010476:  // exposureFunctions
          ((ExposureFunctions) bean).setExposureFunctions((List<String>) newValue);
          return;
        case -462409803:  // idsToNames
          ((ExposureFunctions) bean).setIdsToNames((Map<ExternalId, String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ExposureFunctions) bean)._name, "name");
      JodaBeanUtils.notNull(((ExposureFunctions) bean)._exposureFunctions, "exposureFunctions");
      JodaBeanUtils.notNull(((ExposureFunctions) bean)._idsToNames, "idsToNames");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
