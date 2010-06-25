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
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

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
   * @return the parent node unique identifier, null if no parent
   */
  public UniqueIdentifier getParentNodeUid() {
    return _parentNodeUid;
  }

  /**
   * Sets the parent node unique identifier of the position.
   * @param uid  the parent node unique identifier, null if no parent
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

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String PORTFOLIO_UID_FIELD_NAME = "portfolioUid";
  /** Field name. */
  private static final String PARENT_NODE_UID_FIELD_NAME = "parentNodeUid";
  /** Field name. */
  private static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  private static final String CHILD_NODE_FIELD_NAME = "childNode";
  /** Field name. */
  private static final String POSITION_FIELD_NAME = "position";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_uid != null) {
      msg.add(UID_FIELD_NAME, _uid.toFudgeMsg(context));
    }
    if (_portfolioUid != null) {
      msg.add(PORTFOLIO_UID_FIELD_NAME, _portfolioUid.toFudgeMsg(context));
    }
    if (_parentNodeUid != null) {
      msg.add(PARENT_NODE_UID_FIELD_NAME, _parentNodeUid.toFudgeMsg(context));
    }
    if (_name != null) {
      msg.add(NAME_FIELD_NAME, _name);
    }
    for (PortfolioNodeSummary summary : _childNodes) {
      msg.add(CHILD_NODE_FIELD_NAME, summary.toFudgeMsg(context));
    }
    for (PositionSummary summary : _positions) {
      msg.add(POSITION_FIELD_NAME, summary.toFudgeMsg(context));
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static ManagedPortfolioNode fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    ManagedPortfolioNode mp = new ManagedPortfolioNode();
    if (msg.hasField(UID_FIELD_NAME)) {
      mp.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(PORTFOLIO_UID_FIELD_NAME)) {
      mp.setPortfolioUid(UniqueIdentifier.fromFudgeMsg(msg.getMessage(PORTFOLIO_UID_FIELD_NAME)));
    }
    if (msg.hasField(PARENT_NODE_UID_FIELD_NAME)) {
      mp.setParentNodeUid(UniqueIdentifier.fromFudgeMsg(msg.getMessage(PARENT_NODE_UID_FIELD_NAME)));
    }
    if (msg.hasField(NAME_FIELD_NAME)) {
      mp.setName(msg.getString(NAME_FIELD_NAME));
    }
    for (FudgeField field : msg.getAllByName(CHILD_NODE_FIELD_NAME)) {
      mp.getChildNodes().add(PortfolioNodeSummary.fromFudgeMsg(context, (FudgeFieldContainer) field.getValue()));
    }
    for (FudgeField field : msg.getAllByName(POSITION_FIELD_NAME)) {
      mp.getPositions().add(PositionSummary.fromFudgeMsg(context, (FudgeFieldContainer) field.getValue()));
    }
    return mp;
  }

}
