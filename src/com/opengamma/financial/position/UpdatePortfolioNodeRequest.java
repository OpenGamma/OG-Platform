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
 * A request to update the details of a portfolio node.
 */
public final class UpdatePortfolioNodeRequest {

  /**
   * The portfolio node uid.
   */
  private UniqueIdentifier _uid;
  /**
   * The portfolio node name.
   */
  private String _name;

  /**
   * Creates an instance.
   */
  public UpdatePortfolioNodeRequest() {
  }

  /**
   * Creates an instance.
   * @param portfolioNode  the portfolio node to copy, not null
   */
  public UpdatePortfolioNodeRequest(PortfolioNode portfolioNode) {
    setUniqueIdentifier(portfolioNode.getUniqueIdentifier());
    setName(portfolioNode.getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the portfolio unique identifier.
   * @param uid  the unique identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "UniqueIdentifier");
    _uid = uid;
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
    Validate.notNull(getUniqueIdentifier(), "UniqueIdentifier must not be null");
    Validate.isTrue(getUniqueIdentifier().isVersioned(), "UniqueIdentifier must be versioned");
    Validate.notEmpty(getName(), "Name must not be empty");
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
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static UpdatePortfolioNodeRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    UpdatePortfolioNodeRequest req = new UpdatePortfolioNodeRequest();
    if (msg.hasField(UID_FIELD_NAME)) {
      req.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(NAME_FIELD_NAME)) {
      req.setName(msg.getString(NAME_FIELD_NAME));
    }
    return req;
  }

}
