/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a portfolio node.
 */
public final class AddPortfolioNodeRequest {

  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentUid;
  /**
   * The portfolio node name.
   */
  private String _name;

  /**
   * Creates an instance.
   */
  public AddPortfolioNodeRequest() {
  }

  /**
   * Creates an instance.
   * @param portfolioNode  the portfolio node to copy, not null
   */
  public AddPortfolioNodeRequest(PortfolioNode portfolioNode) {
    setName(portfolioNode.getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getParentNode() {
    return _parentUid;
  }

  /**
   * Sets the parent node unique identifier.
   * @param parentUid  the unique identifier, not null
   */
  public void setParentNode(UniqueIdentifier parentUid) {
    ArgumentChecker.notNull(parentUid, "UniqueIdentifier");
    _parentUid = parentUid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name to change to.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name to change to.
   * @param name  the name
   */
  public void setName(String name) {
    _name = StringUtils.trim(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getParentNode(), "Parent must not be null");
    Validate.notEmpty(getName(), "Name must not be empty");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  private static final String PARENT_NODE_FIELD_NAME = "parentNode";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_name != null) {
      msg.add(NAME_FIELD_NAME, _name);
    }
    if (_parentUid != null) {
      context.objectToFudgeMsg(msg, PARENT_NODE_FIELD_NAME, null, _parentUid);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static AddPortfolioNodeRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    AddPortfolioNodeRequest req = new AddPortfolioNodeRequest();
    if (msg.hasField(NAME_FIELD_NAME)) {
      req.setName(msg.getString(NAME_FIELD_NAME));
    }
    if (msg.hasField(PARENT_NODE_FIELD_NAME)) {
      req.setParentNode(UniqueIdentifier.fromFudgeMsg((FudgeFieldContainer) msg.getMessage(PARENT_NODE_FIELD_NAME)));
    }
    return req;
  }

}
