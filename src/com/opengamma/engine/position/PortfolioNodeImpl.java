/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code PortfolioNode}.
 */
public class PortfolioNodeImpl implements PortfolioNode, Serializable {

  /**
   * The identity key of the node.
   */
  private Identifier _identityKey;
  /**
   * The name.
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
   * Creates a node with an empty name.
   */
  public PortfolioNodeImpl() {
    _name = "";
  }

  /**
   * Creates a node with the specified name.
   * @param name  the name to use, not null
   */
  public PortfolioNodeImpl(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the identity key of the node.
   * @return the identity key, null if not uniquely identified
   */
  @Override
  public Identifier getIdentityKey() {
    return _identityKey;
  }

  /**
   * Sets the identity key of the node.
   * @param identityKey  the new identity key, not null
   */
  public void setIdentityKey(Identifier identityKey) {
    ArgumentChecker.notNull(identityKey, "Identity key");
    if (identityKey.isNotScheme(PORTFOLIO_NODE_IDENTITY_KEY_SCHEME)) {
      throw new IllegalArgumentException("Wrong scheme specified: " + identityKey.getScheme());
    }
    _identityKey = identityKey; 
  }

  /**
   * Sets the identity key identifier of the node.
   * @param identityKey  the new identity key identifier, not null
   */
  public void setIdentityKey(String identityKey) {
    _identityKey = new Identifier(PORTFOLIO_NODE_IDENTITY_KEY_SCHEME, identityKey);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the node intended for display purposes.
   * @return the name, not null
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the node intended for display purposes.
   * @param name
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the total size of the children, including nodes and positions.
   * @return the size of the nodes and position
   */
  @Override
  public int size() {
    return _childNodes.size() + _positions.size();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the nodes which are immediate children of this node.
   * @return the child nodes, unmodifiable, never null
   */
  @Override
  public List<PortfolioNode> getChildNodes() {
    return Collections.unmodifiableList(_childNodes);
  }

  /**
   * Adds a node to the list of immediate children.
   * @param childNode  the child node to add, not null
   */
  public void addChildNode(PortfolioNode childNode) {
    ArgumentChecker.notNull(childNode, "child node");
    _childNodes.add(childNode);
  }

  /**
   * Adds a collection of nodes to the list of immediate children.
   * @param childNode  the child nodes to add, not null
   */
  public void addChildNodes(Collection<? extends PortfolioNode> childNodes) {
    ArgumentChecker.noNulls(childNodes, "child node");
    _childNodes.addAll(childNodes);
  }

  /**
   * Removes a node from the list of immediate children.
   * @param childNode  the child node to remove, not null
   */
  public void removeChildNode(PortfolioNode childNode) {
    _childNodes.remove(childNode);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the positions which are immediate children of this node.
   * @return the positions, unmodifiable, never null
   */
  @Override
  public List<Position> getPositions() {
    return Collections.unmodifiableList(_positions);
  }

  /**
   * Adds a node to the list of immediate children.
   * @param position  the position to add, not null
   */
  public void addPosition(Position position) {
    ArgumentChecker.notNull(position, "child node");
    _positions.add(position);
  }

  /**
   * Adds a collection of nodes to the list of immediate children.
   * @param childNode  the positions to add, not null
   */
  public void addPositions(Collection<? extends Position> positions) {
    ArgumentChecker.noNulls(positions, "position");
    _positions.addAll(positions);
  }

  /**
   * Removes a position from the list.
   * @param position  the position to remove, not null
   */
  public void removePosition(Position position) {
    _positions.remove(position);
  }

  //-------------------------------------------------------------------------
  /**
   * Recursively finds a specific node from this node by identity key.
   * If this node matches it is returned.
   * @param identityKey  the identity key, null returns null
   * @return the node, null if not found
   */
  @Override
  public PortfolioNode getNode(Identifier identityKey) {
    if (identityKey != null) {
      if (_identityKey.equals(identityKey)) {
        return this;
      }
      for (PortfolioNode child : _childNodes) {
        PortfolioNode result = child.getNode(identityKey);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Recursively finds a specific position from this node by identity key.
   * @param identityKey  the identity key, null returns null
   * @return the position, null if not found
   */
  @Override
  public Position getPosition(Identifier identityKey) {
    if (identityKey != null) {
      for (Position child : _positions) {
        if (_identityKey.equals(child.getIdentityKey())) {
          return child;
        }
      }
      for (PortfolioNode child : _childNodes) {
        Position result = child.getPosition(identityKey);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return new StringBuilder()
      .append("PortfolioNode[")
      .append(getName())
      .append(", ")
      .append(_childNodes.size())
      .append(" child-nodes, ")
      .append(_positions.size())
      .append(" positions]")
      .toString();
  }

}
