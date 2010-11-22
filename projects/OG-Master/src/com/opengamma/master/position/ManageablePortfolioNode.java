/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A node in a portfolio tree allowing an arbitrary structure within an organization
 * for the management of positions.
 * <p>
 * Positions are logically attached to nodes such as this, however they are
 * stored and returned separately from the position master.
 */
@BeanDefinition
public class ManageablePortfolioNode extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The portfolio tree node unique identifier.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueIdentifier;
  /**
   * The portfolio tree node name.
   */
  @PropertyDefinition
  private String _name = "";
  /**
   * The root node of the tree.
   */
  @PropertyDefinition
  private final List<ManageablePortfolioNode> _childNodes = new ArrayList<ManageablePortfolioNode>();

  /**
   * Creates a node.
   */
  public ManageablePortfolioNode() {
  }

  /**
   * Creates a node specifying the name.
   * @param name  the name, not null
   */
  public ManageablePortfolioNode(final String name) {
    ArgumentChecker.notNull(name, "name");
    setName(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a child node to this node.
   * @param childNode  the child node, not null
   */
  public void addChildNode(final ManageablePortfolioNode childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    getChildNodes().add(childNode);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * @param nodeOid  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  public ManageablePortfolioNode getNode(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    if (getUniqueIdentifier().getScheme().equals(nodeOid.getScheme()) &&
        getUniqueIdentifier().getValue().equals(nodeOid.getValue())) {
      return this;
    }
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      ManageablePortfolioNode found = child.getNode(nodeOid);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Gets the stack of nodes from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes returning all the nodes
   * in the hierarchy from the root node to the specified node.
   * The specified node is at the top of the stack.
   * @param nodeOid  the node object identifier, not null
   * @return the node stack, empty if not found, not null
   */
  public Stack<ManageablePortfolioNode> getNodeStack(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    Stack<ManageablePortfolioNode> stack = new Stack<ManageablePortfolioNode>();
    Stack<ManageablePortfolioNode> result = getNodeStack0(stack, nodeOid);
    return result == null ? stack : result;
  }

  /**
   * Gets the stack of nodes from the tree below this node by identifier.
   * @param stack  the stack of nodes, not null
   * @param nodeOid  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  public Stack<ManageablePortfolioNode> getNodeStack0(final Stack<ManageablePortfolioNode> stack, final UniqueIdentifier nodeOid) {
    stack.push(this);
    if (getUniqueIdentifier().getScheme().equals(nodeOid.getScheme()) &&
        getUniqueIdentifier().getValue().equals(nodeOid.getValue())) {
      return stack;
    }
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      Stack<ManageablePortfolioNode> found = child.getNodeStack0(stack, nodeOid);
      if (found != null) {
        return found;
      }
    }
    stack.pop();
    return null;
  }

  /**
   * Removes a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * @param nodeOid  the node object identifier, not null
   * @return true if a node was removed
   */
  public boolean removeNode(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      if (child.getUniqueIdentifier().getScheme().equals(nodeOid.getScheme()) &&
          child.getUniqueIdentifier().getValue().equals(nodeOid.getValue())) {
        it.remove();
        return true;
      }
      if (child.removeNode(nodeOid)) {
        return true;
      }
    }
    return false;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageablePortfolioNode}.
   * @return the meta-bean, not null
   */
  public static ManageablePortfolioNode.Meta meta() {
    return ManageablePortfolioNode.Meta.INSTANCE;
  }

  @Override
  public ManageablePortfolioNode.Meta metaBean() {
    return ManageablePortfolioNode.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        return getUniqueIdentifier();
      case 3373707:  // name
        return getName();
      case 1339293429:  // childNodes
        return getChildNodes();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        setUniqueIdentifier((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 1339293429:  // childNodes
        setChildNodes((List<ManageablePortfolioNode>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree node unique identifier.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * Sets the portfolio tree node unique identifier.
   * @param uniqueIdentifier  the new value of the property
   */
  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    this._uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Gets the the {@code uniqueIdentifier} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueIdentifier() {
    return metaBean().uniqueIdentifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree node name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the portfolio tree node name.
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
   * Gets the root node of the tree.
   * @return the value of the property
   */
  public List<ManageablePortfolioNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Sets the root node of the tree.
   * @param childNodes  the new value of the property
   */
  public void setChildNodes(List<ManageablePortfolioNode> childNodes) {
    this._childNodes.clear();
    this._childNodes.addAll(childNodes);
  }

  /**
   * Gets the the {@code childNodes} property.
   * @return the property, not null
   */
  public final Property<List<ManageablePortfolioNode>> childNodes() {
    return metaBean().childNodes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageablePortfolioNode}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueIdentifier = DirectMetaProperty.ofReadWrite(this, "uniqueIdentifier", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code childNodes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageablePortfolioNode>> _childNodes = DirectMetaProperty.ofReadWrite(this, "childNodes", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueIdentifier", _uniqueIdentifier);
      temp.put("name", _name);
      temp.put("childNodes", _childNodes);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageablePortfolioNode createBean() {
      return new ManageablePortfolioNode();
    }

    @Override
    public Class<? extends ManageablePortfolioNode> beanType() {
      return ManageablePortfolioNode.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueIdentifier() {
      return _uniqueIdentifier;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code childNodes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ManageablePortfolioNode>> childNodes() {
      return _childNodes;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
