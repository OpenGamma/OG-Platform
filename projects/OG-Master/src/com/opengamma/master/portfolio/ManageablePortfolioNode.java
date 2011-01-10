/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

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
 * Each node can have child nodes forming a tree structure.
 * Each node can also have a collection of positions referred to by identifier.
 * The details of each position is held in the position master.
 */
@BeanDefinition
public class ManageablePortfolioNode extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The parent node unique identifier, null if the root node.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueIdentifier _parentNodeId;
  /**
   * The portfolio unique identifier.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueIdentifier _portfolioId;
  /**
   * The portfolio node name.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private String _name = "";
  /**
   * The root node of the tree, not null.
   */
  @PropertyDefinition
  private final List<ManageablePortfolioNode> _childNodes = new ArrayList<ManageablePortfolioNode>();
  /**
   * The object identifiers of positions attached to this node, not null.
   * The identifiers should not have versions.
   */
  @PropertyDefinition
  private final List<UniqueIdentifier> _positionIds = new ArrayList<UniqueIdentifier>();

  /**
   * Creates a node.
   */
  public ManageablePortfolioNode() {
  }

  /**
   * Creates a node specifying the name.
   * 
   * @param name  the name, not null
   */
  public ManageablePortfolioNode(final String name) {
    ArgumentChecker.notNull(name, "name");
    setName(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a position unique identifier to this node.
   * Any version will be removed.
   * 
   * @param positionId  the object identifier of the position, not null
   */
  public void addPosition(final UniqueIdentifier positionId) {
    ArgumentChecker.notNull(positionId, "positionId");
    getPositionIds().add(positionId.toLatest());
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a child node to this node.
   * 
   * @param childNode  the child node, not null
   */
  public void addChildNode(final ManageablePortfolioNode childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    getChildNodes().add(childNode);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a node with the specified object identifier, which may be this node.
   * This scans all child nodes depth-first until the node is found.
   * 
   * @param nodeOid  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  public ManageablePortfolioNode findNodeByObjectIdentifier(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    if (getUniqueId().getScheme().equals(nodeOid.getScheme()) &&
        getUniqueId().getValue().equals(nodeOid.getValue())) {
      return this;
    }
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      ManageablePortfolioNode found = child.findNodeByObjectIdentifier(nodeOid);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Finds a node with the specified name, which may be this node.
   * This scans all child nodes depth-first until the node is found.
   * 
   * @param name  the node name, not null
   * @return the node with the name, null if not found
   */
  public ManageablePortfolioNode findNodeByName(final String name) {
    ArgumentChecker.notNull(name, "name");
    if (name.equals(getName())) {
      return this;
    }
    for (ManageablePortfolioNode child : getChildNodes()) {
      ManageablePortfolioNode found = child.findNodeByName(name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Finds the stack of nodes from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes returning all the nodes
   * in the hierarchy from the root node to the specified node.
   * The specified node is at the top of the stack.
   * 
   * @param nodeOid  the node object identifier, not null
   * @return the node stack, empty if not found, not null
   */
  public Stack<ManageablePortfolioNode> findNodeStackByObjectIdentifier(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    Stack<ManageablePortfolioNode> stack = new Stack<ManageablePortfolioNode>();
    Stack<ManageablePortfolioNode> result = findNodeStackByObjectIdentifier0(stack, nodeOid);
    return result == null ? stack : result;
  }

  /**
   * Finds the stack of nodes from the tree below this node by identifier.
   * 
   * @param stack  the stack of nodes, not null
   * @param nodeOid  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  private Stack<ManageablePortfolioNode> findNodeStackByObjectIdentifier0(final Stack<ManageablePortfolioNode> stack, final UniqueIdentifier nodeOid) {
    stack.push(this);
    if (getUniqueId().getScheme().equals(nodeOid.getScheme()) &&
        getUniqueId().getValue().equals(nodeOid.getValue())) {
      return stack;
    }
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      Stack<ManageablePortfolioNode> found = child.findNodeStackByObjectIdentifier0(stack, nodeOid);
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
   * 
   * @param nodeOid  the node object identifier, not null
   * @return true if a node was removed
   */
  public boolean removeNode(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      if (child.getUniqueId().getScheme().equals(nodeOid.getScheme()) &&
          child.getUniqueId().getValue().equals(nodeOid.getValue())) {
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
      case -294460212:  // uniqueId
        return getUniqueId();
      case 915246087:  // parentNodeId
        return getParentNodeId();
      case -5186429:  // portfolioId
        return getPortfolioId();
      case 3373707:  // name
        return getName();
      case 1339293429:  // childNodes
        return getChildNodes();
      case -137459505:  // positionIds
        return getPositionIds();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 915246087:  // parentNodeId
        setParentNodeId((UniqueIdentifier) newValue);
        return;
      case -5186429:  // portfolioId
        setPortfolioId((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 1339293429:  // childNodes
        setChildNodes((List<ManageablePortfolioNode>) newValue);
        return;
      case -137459505:  // positionIds
        setPositionIds((List<UniqueIdentifier>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier, null if the root node.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueIdentifier getParentNodeId() {
    return _parentNodeId;
  }

  /**
   * Sets the parent node unique identifier, null if the root node.
   * This field is managed by the master.
   * @param parentNodeId  the new value of the property
   */
  public void setParentNodeId(UniqueIdentifier parentNodeId) {
    this._parentNodeId = parentNodeId;
  }

  /**
   * Gets the the {@code parentNodeId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> parentNodeId() {
    return metaBean().parentNodeId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueIdentifier getPortfolioId() {
    return _portfolioId;
  }

  /**
   * Sets the portfolio unique identifier.
   * This field is managed by the master.
   * @param portfolioId  the new value of the property
   */
  public void setPortfolioId(UniqueIdentifier portfolioId) {
    this._portfolioId = portfolioId;
  }

  /**
   * Gets the the {@code portfolioId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> portfolioId() {
    return metaBean().portfolioId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio node name.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the portfolio node name.
   * This field must not be null for the object to be valid.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the root node of the tree, not null.
   * @return the value of the property
   */
  public List<ManageablePortfolioNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Sets the root node of the tree, not null.
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
   * Gets the object identifiers of positions attached to this node, not null.
   * The identifiers should not have versions.
   * @return the value of the property
   */
  public List<UniqueIdentifier> getPositionIds() {
    return _positionIds;
  }

  /**
   * Sets the object identifiers of positions attached to this node, not null.
   * The identifiers should not have versions.
   * @param positionIds  the new value of the property
   */
  public void setPositionIds(List<UniqueIdentifier> positionIds) {
    this._positionIds.clear();
    this._positionIds.addAll(positionIds);
  }

  /**
   * Gets the the {@code positionIds} property.
   * The identifiers should not have versions.
   * @return the property, not null
   */
  public final Property<List<UniqueIdentifier>> positionIds() {
    return metaBean().positionIds().createProperty(this);
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
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code parentNodeId} property.
     */
    private final MetaProperty<UniqueIdentifier> _parentNodeId = DirectMetaProperty.ofReadWrite(this, "parentNodeId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code portfolioId} property.
     */
    private final MetaProperty<UniqueIdentifier> _portfolioId = DirectMetaProperty.ofReadWrite(this, "portfolioId", UniqueIdentifier.class);
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
     * The meta-property for the {@code positionIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<UniqueIdentifier>> _positionIds = DirectMetaProperty.ofReadWrite(this, "positionIds", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueId", _uniqueId);
      temp.put("parentNodeId", _parentNodeId);
      temp.put("portfolioId", _portfolioId);
      temp.put("name", _name);
      temp.put("childNodes", _childNodes);
      temp.put("positionIds", _positionIds);
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
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code parentNodeId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> parentNodeId() {
      return _parentNodeId;
    }

    /**
     * The meta-property for the {@code portfolioId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> portfolioId() {
      return _portfolioId;
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

    /**
     * The meta-property for the {@code positionIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<UniqueIdentifier>> positionIds() {
      return _positionIds;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
