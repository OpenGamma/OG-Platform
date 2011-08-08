/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code PortfolioNode}.
 */
public class PortfolioNodeImpl implements PortfolioNode, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  // DON'T CHECK IN WITH =true
  private static final boolean DEBUG_FLAG = false;
  private static final int DEBUG_MAX_NODES = 1;
  private static final int DEBUG_MAX_POSITIONS = 1;

  /**
   * The unique identifier of the node.
   */
  private UniqueId _uniqueId;
  /**
   * The unique identifier of the parent node.
   */
  private UniqueId _parentNodeId;
  /**
   * The display name of the node.
   */
  private String _name;
  /**
   * The list of child nodes.
   */
  private final List<PortfolioNode> _childNodes = new ArrayList<PortfolioNode>();
  /**
   * The list of child positions.
   */
  private final List<Position> _positions = new ArrayList<Position>();

  /**
   * Creates a portfolio node with an empty name.
   */
  public PortfolioNodeImpl() {
    _name = "";
  }

  /**
   * Creates a portfolio node with a given name.
   * 
   * @param name  the name of the portfolio node, null treated as empty
   */
  public PortfolioNodeImpl(String name) {
    _name = StringUtils.defaultString(name);
  }

  /**
   * Creates a portfolio with the specified identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param name  the name to use, null treated as empty
   */
  public PortfolioNodeImpl(UniqueId uniqueId, String name) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _uniqueId = uniqueId;
    _name = StringUtils.defaultString(name);
  }

  /**
   * Creates a deep copy of the specified portfolio node.
   * 
   * @param copyFrom  the instance to copy fields from, not null
   */
  public PortfolioNodeImpl(final PortfolioNode copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _uniqueId = copyFrom.getUniqueId();
    _name = copyFrom.getName();
    for (PortfolioNode child : copyFrom.getChildNodes()) {
      PortfolioNodeImpl clonedNode = new PortfolioNodeImpl(child);
      clonedNode.setParentNodeId(_uniqueId);
      _childNodes.add(clonedNode);
    }
    for (Position position : copyFrom.getPositions()) {
      PositionImpl clonedPosition = new PositionImpl(position);
      clonedPosition.setParentNodeId(_uniqueId);
      _positions.add(clonedPosition);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the node.
   * 
   * @return the identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the node.
   * 
   * @param uniqueId  the new unique identifier, not null
   */
  public void setUniqueId(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _uniqueId = uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the parent node, or {@code null} if this is a root node.
   * 
   * @return the unique identifier, null if root node
   */
  @Override
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  /**
   * Sets the unique identifier of the parent node, or {@code null} if this is a root node.
   * 
   * @param parentNodeId  the new parent node, null if root node
   */
  public void setParentNodeId(final UniqueId parentNodeId) {
    _parentNodeId = parentNodeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the node intended for display purposes.
   * 
   * @return the name, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the node intended for display purposes.
   * 
   * @param name  the name, not null
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the total size of the children, including nodes and positions.
   * 
   * @return the combined size of the node and position lists
   */
  @Override
  public int size() {
    return _childNodes.size() + _positions.size();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the nodes which are immediate children of this node.
   * 
   * @return the child nodes, unmodifiable, not null
   */
  @Override
  public List<PortfolioNode> getChildNodes() {
    if (DEBUG_FLAG) {
      if (_childNodes.size() > DEBUG_MAX_NODES) {
        return Collections.unmodifiableList(_childNodes.subList(0, DEBUG_MAX_NODES));
      }
    }
    return Collections.unmodifiableList(_childNodes);
  }

  /**
   * Adds a node to the list of immediate children.
   * 
   * @param childNode  the child node to add, not null
   */
  public void addChildNode(PortfolioNode childNode) {
    ArgumentChecker.notNull(childNode, "child node");
    if (!ObjectUtils.equals(getUniqueId(), childNode.getParentNodeId())) {
      final PortfolioNodeImpl newChildNode = new PortfolioNodeImpl(childNode);
      newChildNode.setParentNodeId(getUniqueId());
      childNode = newChildNode;
    }
    _childNodes.add(childNode);
  }

  /**
   * Adds a collection of nodes to the list of immediate children.
   * 
   * @param childNodes the child nodes to add, not null
   */
  public void addChildNodes(Collection<? extends PortfolioNode> childNodes) {
    ArgumentChecker.noNulls(childNodes, "child node");
    for (PortfolioNode child : childNodes) {
      addChildNode(child);
    }
  }

  /**
   * Removes a node from the list of immediate children.
   * 
   * @param childNode  the child node to remove, not null
   */
  public void removeChildNode(PortfolioNode childNode) {
    _childNodes.remove(childNode);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions which are immediate children of this node.
   * 
   * @return the positions, unmodifiable, not null
   */
  @Override
  public List<Position> getPositions() {
    if (DEBUG_FLAG) {
      if (_positions.size() > DEBUG_MAX_POSITIONS) {
        return Collections.unmodifiableList(_positions.subList(0, DEBUG_MAX_POSITIONS));
      }
    }
    return Collections.unmodifiableList(_positions);
  }

  /**
   * Adds a node to the list of immediate children.
   * 
   * @param position  the position to add, not null
   */
  public void addPosition(Position position) {
    ArgumentChecker.notNull(position, "child node");
    if (!ObjectUtils.equals(getUniqueId(), position.getParentNodeId())) {
      final PositionImpl newPosition = new PositionImpl(position);
      newPosition.setParentNodeId(getUniqueId());
      position = newPosition;
    }
    _positions.add(position);
  }

  /**
   * Adds a collection of nodes to the list of immediate children.
   * 
   * @param positions the positions to add, not null
   */
  public void addPositions(Collection<? extends Position> positions) {
    ArgumentChecker.noNulls(positions, "positions");
    for (Position position : positions) {
      addPosition(position);
    }
  }

  /**
   * Removes a position from the list.
   * 
   * @param position  the position to remove, not null
   */
  public void removePosition(Position position) {
    _positions.remove(position);
  }

  //-------------------------------------------------------------------------
  /**
   * Recursively finds a specific node from this node by identifier.
   * If this node matches it is returned.
   * 
   * @param uniqueId  the identifier, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getNode(UniqueId uniqueId) {
    return getNode(this, uniqueId);
  }

  /**
   * Recursively finds a specific node from a node by identifier.
   * 
   * @param node  the node to process, not null
   * @param uniqueId  the identifier, null returns null
   * @return the node, null if not found
   */
  private static PortfolioNode getNode(PortfolioNode node, UniqueId uniqueId) {
    if (uniqueId != null) {
      if (uniqueId.equals(node.getUniqueId())) {
        return node;
      }
      for (PortfolioNode child : node.getChildNodes()) {
        PortfolioNode result = getNode(child, uniqueId);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Recursively finds a specific position from this node by identifier.
   * 
   * @param uniqueId  the identifier, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(UniqueId uniqueId) {
    return getPosition(this, uniqueId);
  }

  /**
   * Recursively finds a specific position from a node by identifier.
   * 
   * @param node  the node to process, not null
   * @param uniqueId  the identifier, null returns null
   * @return the position, null if not found
   */
  private static Position getPosition(PortfolioNode node, UniqueId uniqueId) {
    if (uniqueId != null) {
      for (Position child : node.getPositions()) {
        if (uniqueId.equals(child.getUniqueId())) {
          return child;
        }
      }
      for (PortfolioNode child : node.getChildNodes()) {
        Position result = getPosition(child, uniqueId);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Gets a full-detail string containing all child nodes and positions.
   * 
   * @return the full-detail string, not null
   */
  public String toLongString() {
    StrBuilder childBuf = new StrBuilder(1024);
    childBuf.append("[");
    for (int i = 0; i < getChildNodes().size(); i++) {
      PortfolioNode child = getChildNodes().get(i);
      if (child instanceof PortfolioNodeImpl) {
        childBuf.append(((PortfolioNodeImpl) child).toLongString());
      } else {
        childBuf.append(child.toString());
      }
      if (i != getChildNodes().size() - 1) {
        childBuf.append(",");
      }
    }
    childBuf.append("]");
    return new StrBuilder(childBuf.size() + 128)
        .append("PortfolioNode[uniqueId=")
        .append(getUniqueId())
        .append(",childNodes=")
        .append(childBuf)
        .append(",positions=")
        .append(getPositions()).append("]")
        .toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PortfolioNodeImpl) {
      final PortfolioNodeImpl other = (PortfolioNodeImpl) obj;
      final List<PortfolioNode> otherChildNodes = other.getChildNodes();
      final List<Position> otherPositions = other.getPositions();
      return ObjectUtils.equals(getUniqueId(), other.getUniqueId()) &&
          ObjectUtils.equals(getParentNodeId(), other.getParentNodeId()) &&
          ObjectUtils.equals(getName(), other.getName()) &&
          getChildNodes().size() == otherChildNodes.size() &&
          getPositions().size() == otherPositions.size() &&
          getChildNodes().equals(otherChildNodes) &&
          getPositions().equals(otherPositions);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 0;
    int prime = 31;
    if (getUniqueId() != null) {
      result += getUniqueId().hashCode();
    }
    result *= prime;
    if (getParentNodeId() != null) {
      result += getParentNodeId().hashCode();
    }
    result *= prime;
    if (getName() != null) {
      result += getName().hashCode();
    }
    result *= prime;
    result += getChildNodes().size();
    result *= prime;
    result += getPositions().size();
    // intentionally skip the contained children and positions
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder(128)
        .append("PortfolioNode[")
        .append(getUniqueId())
        .append(", ")
        .append(getChildNodes().size())
        .append(" child-nodes, ")
        .append(getPositions().size())
        .append(" positions]")
        .toString();
  }

}
