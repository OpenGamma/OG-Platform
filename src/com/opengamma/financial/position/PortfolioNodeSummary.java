/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.UniqueIdentifier;

/**
 * A summary of a node.
 */
public final class PortfolioNodeSummary {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The node name.
   */
  private String _name;
  /**
   * The total number of positions at any depth.
   */
  private int _totalPositions;

  /**
   * Creates an instance.
   */
  public PortfolioNodeSummary() {
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
   * Gets the total number of positions at any depth.
   * @return the total number of positions
   */
  public int getTotalPositions() {
    return _totalPositions;
  }

  /**
   * Sets the total number of positions at any depth.
   * @param totalPositions  the total number of positions
   */
  public void setTotalPositions(int totalPositions) {
    _totalPositions = totalPositions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  private static final String TOTAL_POSITIONS_FIELD_NAME = "totalPositions";

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
    if (_name != null) {
      msg.add(NAME_FIELD_NAME, _name);
    }
    msg.add(TOTAL_POSITIONS_FIELD_NAME, _totalPositions);
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static PortfolioNodeSummary fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    PortfolioNodeSummary summary = new PortfolioNodeSummary();
    if (msg.hasField(UID_FIELD_NAME)) {
      summary.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(NAME_FIELD_NAME)) {
      summary.setName(msg.getString(NAME_FIELD_NAME));
    }
    summary.setTotalPositions(msg.getInt(TOTAL_POSITIONS_FIELD_NAME));
    return summary;
  }

}
