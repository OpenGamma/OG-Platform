/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;

/**
 * A portfolio node that can be managed.
 */
public final class ManagedPortfolioNode {

  /**
   * The portfolio unique identifier.
   */
  private UniqueIdentifier _portfolioUid;
  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentNodeUid;
  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The node name.
   */
  private String _name;
  /**
   * The child nodes.
   */
  private List<PortfolioNodeSummary> _childNodes = new ArrayList<PortfolioNodeSummary>();
  /**
   * The child positions.
   */
  private List<PositionSummary> _positions = new ArrayList<PositionSummary>();

  /**
   * Creates an instance.
   */
  public ManagedPortfolioNode() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the portfolio unique identifier of the position.
   * @return the portfolio unique identifier
   */
  public UniqueIdentifier getPortfolioUid() {
    return _portfolioUid;
  }

  /**
   * Sets the portfolio unique identifier of the position.
   * @param uid  the portfolio unique identifier
   */
  public void setPortfolioUid(UniqueIdentifier uid) {
    _portfolioUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier of the position.
   * @return the parent node unique identifier
   */
  public UniqueIdentifier getParentNodeUid() {
    return _parentNodeUid;
  }

  /**
   * Sets the parent node unique identifier of the position.
   * @param uid  the parent node unique identifier
   */
  public void setParentNodeUid(UniqueIdentifier uid) {
    _parentNodeUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the unique identifier
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier of the position.
   * @param uid  the unique identifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the name
   */
  public void setName(String name) {
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of child nodes.
   * @return the child nodes, modifiable affecting the state of this object, not null
   */
  public List<PortfolioNodeSummary> getChildNodes() {
    return _childNodes;
  }

//  /**
//   * Sets the list of child nodes.
//   * @param childNodes  the child nodes, assigned, not null
//   */
//  public void setChildNodes(List<PortfolioNodeSummary> childNodes) {
//    Validate.notNull(childNodes, "List must not be null");
//    _childNodes = childNodes;
//  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of positions.
   * @return the positions, modifiable affecting the state of this object, not null
   */
  public List<PositionSummary> getPositions() {
    return _positions;
  }

//  /**
//   * Sets the list of positions.
//   * @param positions  the positions, assigned, not null
//   */
//  public void setPositions(List<PositionSummary> positions) {
//    Validate.notNull(positions, "List must not be null");
//    _positions = positions;
//  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
