/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for configuration documents.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link ConfigHistoryRequest} for more details on how history works.
 * 
 * @param <T> the configuration element type
 */
@PublicSPI
@BeanDefinition
public class ConfigSearchRequest<T> extends AbstractSearchRequest {

  /**
   * The set of configuration object identifiers, null to not limit by configuration object identifiers.
   * Note that an empty set will return no configurations.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _configIds;
  /**
   * The name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition(set = "manual")
  private String _name;
  /**
   * The class of the configuration.
   */
  @PropertyDefinition(validate = "notNull")
  private Class<?> _type = Object.class;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSearchSortOrder _sortOrder = ConfigSearchSortOrder.VERSION_FROM_INSTANT_DESC;

  /**
   * Regular expression pattern for matching the name using wildcards (* and ?).
   * <p>
   * This is null if {@code _name} is null or doesn't contain any wildcard characters.
   */
  private Pattern _namePattern;

  /**
   * Creates an instance.
   */
  public ConfigSearchRequest() {
  }

  /**
   * Creates an instance with a configuration type.
   * 
   * @param type the configuration type, not null
   */
  public ConfigSearchRequest(Class<T> type) {
    ArgumentChecker.notNull(type, "type");
    _type = type;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single configuration object identifier to the set.
   * 
   * @param configId the configuration object identifier to add, not null
   */
  public void addConfigId(ObjectIdentifiable configId) {
    ArgumentChecker.notNull(configId, "configId");
    if (_configIds == null) {
      _configIds = new ArrayList<ObjectId>();
    }
    _configIds.add(configId.getObjectId());
  }

  /**
   * Sets the set of configuration object identifiers, null to not limit by configuration object identifiers. Note that an empty set will return no configurations.
   * 
   * @param configIds the new configuration identifiers, null clears the configuration id search
   */
  public void setConfigIds(Iterable<? extends ObjectIdentifiable> configIds) {
    if (configIds == null) {
      _configIds = null;
    } else {
      _configIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable configId : configIds) {
        _configIds.add(configId.getObjectId());
      }
    }
  }

  /**
   * Sets the name to match, wildcards allowed, null to not match on name.
   * <p>
   * The wildcard characters are:
   * <ul>
   *   <li>'*' - matches any number of characters, including none</li>
   *   <li>'?' - matches exactly one character</li>
   * </ul>
   *
   * @param name  the new value of the property
   */
  public void setName(String name) {
    _name = name;

    if (name == null || !containsWildcards(name)) {
      // If the name doesn't contain wildcards there's no point creating a pattern.
      // This can slow things down unnecessarily when performing a large number of matches where the names
      // don't contains wildcards. This is common when using links.
      _namePattern = null;
    } else {
      _namePattern = RegexUtils.wildcardsToPattern(name);
    }
  }

  /**
   * Returns true if {@code name} contains either of the wildcard characters '*' or '?'.
   *
   * @param name a name for matching against a config object
   * @return true if {@code name} contains either of the wildcard characters '*' or '?'
   */
  private static boolean containsWildcards(String name) {
    return name.indexOf('*') != -1 || name.indexOf('?') != -1;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument doc) {
    if (!(doc instanceof ConfigDocument)) {
      return false;
    }
    final ConfigDocument configDoc = (ConfigDocument) doc;
    if (getConfigIds() != null && !getConfigIds().contains(configDoc.getObjectId())) {
      return false;
    }
    if (_namePattern != null) {
      // if there is a pattern then we're doing a wildcard match. try matching the pattern
      if (!_namePattern.matcher(configDoc.getName()).matches()) {
        return false;
      }
    } else if (getName() != null) {
      // if there is no pattern but there is a name we're looking for an exact match
      if (!getName().equals(configDoc.getName())) {
        return false;
      }
    }
    return super.matches(doc) && (getType() == null || getType().isAssignableFrom(configDoc.getType()));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConfigSearchRequest}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ConfigSearchRequest.Meta meta() {
    return ConfigSearchRequest.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code ConfigSearchRequest}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> ConfigSearchRequest.Meta<R> metaConfigSearchRequest(Class<R> cls) {
    return ConfigSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ConfigSearchRequest.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ConfigSearchRequest.Meta<T> metaBean() {
    return ConfigSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of configuration object identifiers, null to not limit by configuration object identifiers.
   * Note that an empty set will return no configurations.
   * @return the value of the property
   */
  public List<ObjectId> getConfigIds() {
    return _configIds;
  }

  /**
   * Gets the the {@code configIds} property.
   * Note that an empty set will return no configurations.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> configIds() {
    return metaBean().configIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
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
   * Gets the class of the configuration.
   * @return the value of the property, not null
   */
  public Class<?> getType() {
    return _type;
  }

  /**
   * Sets the class of the configuration.
   * @param type  the new value of the property, not null
   */
  public void setType(Class<?> type) {
    JodaBeanUtils.notNull(type, "type");
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * @return the property, not null
   */
  public final Property<Class<?>> type() {
    return metaBean().type().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public ConfigSearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(ConfigSearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<ConfigSearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ConfigSearchRequest<T> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ConfigSearchRequest<?> other = (ConfigSearchRequest<?>) obj;
      return JodaBeanUtils.equal(getConfigIds(), other.getConfigIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getType());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("ConfigSearchRequest{");
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
    buf.append("configIds").append('=').append(JodaBeanUtils.toString(getConfigIds())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("type").append('=').append(JodaBeanUtils.toString(getType())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConfigSearchRequest}.
   * @param <T>  the type
   */
  public static class Meta<T> extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code configIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _configIds = DirectMetaProperty.ofReadWrite(
        this, "configIds", ConfigSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ConfigSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code type} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Class<?>> _type = DirectMetaProperty.ofReadWrite(
        this, "type", ConfigSearchRequest.class, (Class) Class.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<ConfigSearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", ConfigSearchRequest.class, ConfigSearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "configIds",
        "name",
        "type",
        "sortOrder");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -804471786:  // configIds
          return _configIds;
        case 3373707:  // name
          return _name;
        case 3575610:  // type
          return _type;
        case -26774448:  // sortOrder
          return _sortOrder;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ConfigSearchRequest<T>> builder() {
      return new DirectBeanBuilder<ConfigSearchRequest<T>>(new ConfigSearchRequest<T>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ConfigSearchRequest<T>> beanType() {
      return (Class) ConfigSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code configIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> configIds() {
      return _configIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Class<?>> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -804471786:  // configIds
          return ((ConfigSearchRequest<?>) bean).getConfigIds();
        case 3373707:  // name
          return ((ConfigSearchRequest<?>) bean).getName();
        case 3575610:  // type
          return ((ConfigSearchRequest<?>) bean).getType();
        case -26774448:  // sortOrder
          return ((ConfigSearchRequest<?>) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -804471786:  // configIds
          ((ConfigSearchRequest<T>) bean).setConfigIds((List<ObjectId>) newValue);
          return;
        case 3373707:  // name
          ((ConfigSearchRequest<T>) bean).setName((String) newValue);
          return;
        case 3575610:  // type
          ((ConfigSearchRequest<T>) bean).setType((Class<?>) newValue);
          return;
        case -26774448:  // sortOrder
          ((ConfigSearchRequest<T>) bean).setSortOrder((ConfigSearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ConfigSearchRequest<?>) bean)._type, "type");
      JodaBeanUtils.notNull(((ConfigSearchRequest<?>) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
