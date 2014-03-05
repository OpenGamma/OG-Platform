/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;
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

import com.google.common.collect.Maps;
import com.opengamma.core.position.Portfolio;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Portfolio}.
 */
@BeanDefinition
public class SimplePortfolio extends DirectBean
    implements Portfolio, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the portfolio.
   */
  @PropertyDefinition(validate = "notNull")
  private UniqueId _uniqueId;
  /**
   * The display name of the portfolio.
   */
  @PropertyDefinition(validate = "notNull")
  private String _name;
  /**
   * The root node.
   */
  @PropertyDefinition(validate = "notNull")
  private SimplePortfolioNode _rootNode;
  /**
   * The general purpose portfolio attributes.
   * These can be used to add arbitrary additional information to the object.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _attributes = Maps.newHashMap();

  /**
   * Creates an instance.
   */
  private SimplePortfolio() {
  }

  /**
   * Creates a portfolio with the specified name.
   * 
   * @param name  the name to use, not null
   */
  public SimplePortfolio(String name) {
    this(name, new SimplePortfolioNode());
  }

  /**
   * Creates a portfolio with the specified name and root node.
   * 
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public SimplePortfolio(String name, SimplePortfolioNode rootNode) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "root node");
    _name = name;
    _rootNode = rootNode;
  }

  /**
   * Creates a portfolio with the specified unique identifier and name.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param name  the name to use, not null
   */
  public SimplePortfolio(UniqueId uniqueId, String name) {
    this(uniqueId, name, new SimplePortfolioNode());
  }

  /**
   * Creates a portfolio with the specified unique identifier, name and root node.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param name  the name to use, not null
   * @param rootNode  the root node, not null
   */
  public SimplePortfolio(UniqueId uniqueId, String name, SimplePortfolioNode rootNode) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "rootNode");
    _uniqueId = uniqueId;
    _name = name;
    _rootNode = rootNode;
  }

  /**
   * Creates a deep copy of the specified portfolio.
   * 
   * @param copyFrom  the portfolio to copy from, not null
   */
  public SimplePortfolio(Portfolio copyFrom) {
    ArgumentChecker.notNull(copyFrom, "portfolio");
    _uniqueId = copyFrom.getUniqueId();
    _name = copyFrom.getName();
    _rootNode = new SimplePortfolioNode(copyFrom.getRootNode());
    _rootNode.setParentNodeId(null);
    if (copyFrom.getAttributes() != null) {
      for (Entry<String, String> entry : copyFrom.getAttributes().entrySet()) {
        addAttribute(entry.getKey(), entry.getValue());
      }
    }
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a full-detail string containing all child nodes and positions.
   * 
   * @return the full-detail string, not null
   */
  public String toLongString() {
    return new StrBuilder(1024)
        .append("Portfolio[")
        .append("uniqueId" + "=")
        .append(getUniqueId())
        .append(",rootNode=")
        .append(getRootNode().toLongString())
        .append("]")
        .toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SimplePortfolio) {
      final SimplePortfolio other = (SimplePortfolio) obj;
      return ObjectUtils.equals(getUniqueId(), other.getUniqueId()) &&
          ObjectUtils.equals(getName(), other.getName()) &&
          ObjectUtils.equals(getRootNode(), other.getRootNode());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 0;
    int prime = 31;
    if (getUniqueId() != null) {
      result = result * prime + getUniqueId().hashCode();
    }
    if (getName() != null) {
      result = result * prime + getName().hashCode(); 
    }
    // intentionally skip the root node
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder(128)
        .append("Portfolio[")
        .append(getUniqueId())
        .append("]")
        .toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimplePortfolio}.
   * @return the meta-bean, not null
   */
  public static SimplePortfolio.Meta meta() {
    return SimplePortfolio.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SimplePortfolio.Meta.INSTANCE);
  }

  @Override
  public SimplePortfolio.Meta metaBean() {
    return SimplePortfolio.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the portfolio.
   * @return the value of the property, not null
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the portfolio.
   * @param uniqueId  the new value of the property, not null
   */
  public void setUniqueId(UniqueId uniqueId) {
    JodaBeanUtils.notNull(uniqueId, "uniqueId");
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
   * Gets the display name of the portfolio.
   * @return the value of the property, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the display name of the portfolio.
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
   * Gets the root node.
   * @return the value of the property, not null
   */
  public SimplePortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node.
   * @param rootNode  the new value of the property, not null
   */
  public void setRootNode(SimplePortfolioNode rootNode) {
    JodaBeanUtils.notNull(rootNode, "rootNode");
    this._rootNode = rootNode;
  }

  /**
   * Gets the the {@code rootNode} property.
   * @return the property, not null
   */
  public final Property<SimplePortfolioNode> rootNode() {
    return metaBean().rootNode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose portfolio attributes.
   * These can be used to add arbitrary additional information to the object.
   * @return the value of the property, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose portfolio attributes.
   * These can be used to add arbitrary additional information to the object.
   * @param attributes  the new value of the property, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * These can be used to add arbitrary additional information to the object.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SimplePortfolio clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimplePortfolio}.
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
        this, "uniqueId", SimplePortfolio.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", SimplePortfolio.class, String.class);
    /**
     * The meta-property for the {@code rootNode} property.
     */
    private final MetaProperty<SimplePortfolioNode> _rootNode = DirectMetaProperty.ofReadWrite(
        this, "rootNode", SimplePortfolio.class, SimplePortfolioNode.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", SimplePortfolio.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "name",
        "rootNode",
        "attributes");

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
        case -167026172:  // rootNode
          return _rootNode;
        case 405645655:  // attributes
          return _attributes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimplePortfolio> builder() {
      return new DirectBeanBuilder<SimplePortfolio>(new SimplePortfolio());
    }

    @Override
    public Class<? extends SimplePortfolio> beanType() {
      return SimplePortfolio.class;
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
     * The meta-property for the {@code rootNode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<SimplePortfolioNode> rootNode() {
      return _rootNode;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((SimplePortfolio) bean).getUniqueId();
        case 3373707:  // name
          return ((SimplePortfolio) bean).getName();
        case -167026172:  // rootNode
          return ((SimplePortfolio) bean).getRootNode();
        case 405645655:  // attributes
          return ((SimplePortfolio) bean).getAttributes();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((SimplePortfolio) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((SimplePortfolio) bean).setName((String) newValue);
          return;
        case -167026172:  // rootNode
          ((SimplePortfolio) bean).setRootNode((SimplePortfolioNode) newValue);
          return;
        case 405645655:  // attributes
          ((SimplePortfolio) bean).setAttributes((Map<String, String>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SimplePortfolio) bean)._uniqueId, "uniqueId");
      JodaBeanUtils.notNull(((SimplePortfolio) bean)._name, "name");
      JodaBeanUtils.notNull(((SimplePortfolio) bean)._rootNode, "rootNode");
      JodaBeanUtils.notNull(((SimplePortfolio) bean)._attributes, "attributes");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
