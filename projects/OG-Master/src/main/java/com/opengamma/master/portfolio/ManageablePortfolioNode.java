/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
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

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A node in a portfolio tree allowing an arbitrary structure within an organization
 * for the management of positions.
 * <p>
 * Each node can have child nodes forming a tree structure.
 * Each node can also have a collection of positions referred to by identifier.
 * The details of each position is held in the position master.
 */
@PublicSPI
@BeanDefinition
public class ManageablePortfolioNode extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The parent node unique identifier, null if the root node.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueId _parentNodeId;
  /**
   * The portfolio unique identifier.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueId _portfolioId;
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
  private final List<ObjectId> _positionIds = new ArrayList<ObjectId>();

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
   * Adds a position object identifier to this node.
   * 
   * @param positionId  the object identifier of the position, not null
   */
  public void addPosition(final ObjectIdentifiable positionId) {
    ArgumentChecker.notNull(positionId, "positionId");
    getPositionIds().add(positionId.getObjectId());
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
   * Checks if any node object identifier matches one in the specified list.
   * 
   * @param nodeObjectIds  the object identifiers to match against, not null
   * @return true if at least one identifier matches
   */
  public boolean matchesAnyNode(List<ObjectId> nodeObjectIds) {
    ArgumentChecker.notNull(nodeObjectIds, "objectIds");
    if (nodeObjectIds.contains(getUniqueId().getObjectId())) {
      return true;
    }
    for (ManageablePortfolioNode childNode : getChildNodes()) {
      if (childNode.matchesAnyNode(nodeObjectIds)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if any position object identifier matches one in the specified list.
   * 
   * @param positionObjectIds  the position object identifiers to match against, not null
   * @return true if at least one identifier matches
   */
  public boolean matchesAnyPosition(List<ObjectId> positionObjectIds) {
    ArgumentChecker.notNull(positionObjectIds, "positionObjectIds");
    if (CollectionUtils.containsAny(positionObjectIds, getPositionIds())) {
      return true;
    }
    for (ManageablePortfolioNode childNode : getChildNodes()) {
      if (childNode.matchesAnyPosition(positionObjectIds)) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a node with the specified object identifier, which may be this node.
   * This scans all child nodes depth-first until the node is found.
   * 
   * @param nodeObjectId  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  public ManageablePortfolioNode findNodeByObjectId(final ObjectIdentifiable nodeObjectId) {
    ArgumentChecker.notNull(nodeObjectId, "nodeObjectId");
    return findNodeByObjectId0(nodeObjectId.getObjectId());
  }

  /**
   * Finds a node with the specified object identifier, which may be this node.
   * This scans all child nodes depth-first until the node is found.
   * 
   * @param nodeObjectId  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  private ManageablePortfolioNode findNodeByObjectId0(final ObjectId nodeObjectId) {
    if (getUniqueId().equalObjectId(nodeObjectId)) {
      return this;
    }
    for (ManageablePortfolioNode childNode : getChildNodes()) {
      ManageablePortfolioNode found = childNode.findNodeByObjectId0(nodeObjectId);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  /**
   * Finds the stack of nodes from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes returning all the nodes
   * in the hierarchy from the root node to the specified node.
   * The specified node is at the top of the stack.
   * 
   * @param nodeObjectId  the node object identifier, not null
   * @return the node stack, empty if not found, not null
   */
  public Stack<ManageablePortfolioNode> findNodeStackByObjectId(final ObjectIdentifiable nodeObjectId) {
    ArgumentChecker.notNull(nodeObjectId, "nodeObjectId");
    Stack<ManageablePortfolioNode> stack = new Stack<ManageablePortfolioNode>();
    Stack<ManageablePortfolioNode> result = findNodeStackByObjectId0(stack, nodeObjectId.getObjectId());
    return result == null ? stack : result;
  }

  /**
   * Finds the stack of nodes from the tree below this node by identifier.
   * 
   * @param stack  the stack of nodes, not null
   * @param nodeObjectId  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  private Stack<ManageablePortfolioNode> findNodeStackByObjectId0(final Stack<ManageablePortfolioNode> stack, final ObjectId nodeObjectId) {
    stack.push(this);
    if (getUniqueId().equalObjectId(nodeObjectId)) {
      return stack;
    }
    for (ManageablePortfolioNode childNode : getChildNodes()) {
      Stack<ManageablePortfolioNode> found = childNode.findNodeStackByObjectId0(stack, nodeObjectId);
      if (found != null) {
        return found;
      }
    }
    stack.pop();
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Removes a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * 
   * @param nodeObjectId  the node object identifier, not null
   * @return true if a node was removed
   */
  public boolean removeNode(final ObjectIdentifiable nodeObjectId) {
    ArgumentChecker.notNull(nodeObjectId, "nodeObjectId");
    return removeNode0(nodeObjectId.getObjectId());
  }

  /**
   * Removes a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * 
   * @param nodeObjectId  the node object identifier, not null
   * @return true if a node was removed
   */
  private boolean removeNode0(final ObjectId nodeObjectId) {
    for (Iterator<ManageablePortfolioNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final ManageablePortfolioNode child = it.next();
      if (child.getUniqueId().equalObjectId(nodeObjectId)) {
        it.remove();
        return true;
      }
      if (child.removeNode0(nodeObjectId)) {
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

  static {
    JodaBeanUtils.registerMetaBean(ManageablePortfolioNode.Meta.INSTANCE);
  }

  @Override
  public ManageablePortfolioNode.Meta metaBean() {
    return ManageablePortfolioNode.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the portfolio node unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier, null if the root node.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  /**
   * Sets the parent node unique identifier, null if the root node.
   * This field is managed by the master.
   * @param parentNodeId  the new value of the property
   */
  public void setParentNodeId(UniqueId parentNodeId) {
    this._parentNodeId = parentNodeId;
  }

  /**
   * Gets the the {@code parentNodeId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueId> parentNodeId() {
    return metaBean().parentNodeId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueId getPortfolioId() {
    return _portfolioId;
  }

  /**
   * Sets the portfolio unique identifier.
   * This field is managed by the master.
   * @param portfolioId  the new value of the property
   */
  public void setPortfolioId(UniqueId portfolioId) {
    this._portfolioId = portfolioId;
  }

  /**
   * Gets the the {@code portfolioId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueId> portfolioId() {
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
   * @return the value of the property, not null
   */
  public List<ManageablePortfolioNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Sets the root node of the tree, not null.
   * @param childNodes  the new value of the property, not null
   */
  public void setChildNodes(List<ManageablePortfolioNode> childNodes) {
    JodaBeanUtils.notNull(childNodes, "childNodes");
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
   * @return the value of the property, not null
   */
  public List<ObjectId> getPositionIds() {
    return _positionIds;
  }

  /**
   * Sets the object identifiers of positions attached to this node, not null.
   * The identifiers should not have versions.
   * @param positionIds  the new value of the property, not null
   */
  public void setPositionIds(List<ObjectId> positionIds) {
    JodaBeanUtils.notNull(positionIds, "positionIds");
    this._positionIds.clear();
    this._positionIds.addAll(positionIds);
  }

  /**
   * Gets the the {@code positionIds} property.
   * The identifiers should not have versions.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> positionIds() {
    return metaBean().positionIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageablePortfolioNode clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageablePortfolioNode other = (ManageablePortfolioNode) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getParentNodeId(), other.getParentNodeId()) &&
          JodaBeanUtils.equal(getPortfolioId(), other.getPortfolioId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getChildNodes(), other.getChildNodes()) &&
          JodaBeanUtils.equal(getPositionIds(), other.getPositionIds());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getParentNodeId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPortfolioId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getChildNodes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPositionIds());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("ManageablePortfolioNode{");
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
    buf.append("parentNodeId").append('=').append(JodaBeanUtils.toString(getParentNodeId())).append(',').append(' ');
    buf.append("portfolioId").append('=').append(JodaBeanUtils.toString(getPortfolioId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("childNodes").append('=').append(JodaBeanUtils.toString(getChildNodes())).append(',').append(' ');
    buf.append("positionIds").append('=').append(JodaBeanUtils.toString(getPositionIds())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageablePortfolioNode}.
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
        this, "uniqueId", ManageablePortfolioNode.class, UniqueId.class);
    /**
     * The meta-property for the {@code parentNodeId} property.
     */
    private final MetaProperty<UniqueId> _parentNodeId = DirectMetaProperty.ofReadWrite(
        this, "parentNodeId", ManageablePortfolioNode.class, UniqueId.class);
    /**
     * The meta-property for the {@code portfolioId} property.
     */
    private final MetaProperty<UniqueId> _portfolioId = DirectMetaProperty.ofReadWrite(
        this, "portfolioId", ManageablePortfolioNode.class, UniqueId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", ManageablePortfolioNode.class, String.class);
    /**
     * The meta-property for the {@code childNodes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageablePortfolioNode>> _childNodes = DirectMetaProperty.ofReadWrite(
        this, "childNodes", ManageablePortfolioNode.class, (Class) List.class);
    /**
     * The meta-property for the {@code positionIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _positionIds = DirectMetaProperty.ofReadWrite(
        this, "positionIds", ManageablePortfolioNode.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "parentNodeId",
        "portfolioId",
        "name",
        "childNodes",
        "positionIds");

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
        case 915246087:  // parentNodeId
          return _parentNodeId;
        case -5186429:  // portfolioId
          return _portfolioId;
        case 3373707:  // name
          return _name;
        case 1339293429:  // childNodes
          return _childNodes;
        case -137459505:  // positionIds
          return _positionIds;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageablePortfolioNode> builder() {
      return new DirectBeanBuilder<ManageablePortfolioNode>(new ManageablePortfolioNode());
    }

    @Override
    public Class<? extends ManageablePortfolioNode> beanType() {
      return ManageablePortfolioNode.class;
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
     * The meta-property for the {@code parentNodeId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> parentNodeId() {
      return _parentNodeId;
    }

    /**
     * The meta-property for the {@code portfolioId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> portfolioId() {
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
    public final MetaProperty<List<ObjectId>> positionIds() {
      return _positionIds;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageablePortfolioNode) bean).getUniqueId();
        case 915246087:  // parentNodeId
          return ((ManageablePortfolioNode) bean).getParentNodeId();
        case -5186429:  // portfolioId
          return ((ManageablePortfolioNode) bean).getPortfolioId();
        case 3373707:  // name
          return ((ManageablePortfolioNode) bean).getName();
        case 1339293429:  // childNodes
          return ((ManageablePortfolioNode) bean).getChildNodes();
        case -137459505:  // positionIds
          return ((ManageablePortfolioNode) bean).getPositionIds();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageablePortfolioNode) bean).setUniqueId((UniqueId) newValue);
          return;
        case 915246087:  // parentNodeId
          ((ManageablePortfolioNode) bean).setParentNodeId((UniqueId) newValue);
          return;
        case -5186429:  // portfolioId
          ((ManageablePortfolioNode) bean).setPortfolioId((UniqueId) newValue);
          return;
        case 3373707:  // name
          ((ManageablePortfolioNode) bean).setName((String) newValue);
          return;
        case 1339293429:  // childNodes
          ((ManageablePortfolioNode) bean).setChildNodes((List<ManageablePortfolioNode>) newValue);
          return;
        case -137459505:  // positionIds
          ((ManageablePortfolioNode) bean).setPositionIds((List<ObjectId>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageablePortfolioNode) bean)._childNodes, "childNodes");
      JodaBeanUtils.notNull(((ManageablePortfolioNode) bean)._positionIds, "positionIds");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
