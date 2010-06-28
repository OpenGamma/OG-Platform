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
 * A portfolio that can be managed.
 */
public final class ManagedPortfolio {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The node name.
   */
  private String _name;
  /**
   * The root node.
   */
  private ManagedPortfolioNode _rootNode;

  /**
   * Creates an instance.
   */
  public ManagedPortfolio() {
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
   * Gets the root node.
   * @return the root node
   */
  public ManagedPortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node.
   * @param rootNode  the root node, not null
   */
  public void setRootNode(ManagedPortfolioNode rootNode) {
    _rootNode = rootNode;
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
  private static final String ROOT_FIELD_NAME = "root";

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
    if (_rootNode != null) {
      msg.add(ROOT_FIELD_NAME, _rootNode.toFudgeMsg(context));
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static ManagedPortfolio fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    ManagedPortfolio mp = new ManagedPortfolio();
    if (msg.hasField(UID_FIELD_NAME)) {
      mp.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(NAME_FIELD_NAME)) {
      mp.setName(msg.getString(NAME_FIELD_NAME));
    }
    if (msg.hasField(ROOT_FIELD_NAME)) {
      mp.setRootNode(ManagedPortfolioNode.fromFudgeMsg(context, msg.getMessage(ROOT_FIELD_NAME)));
    }
    return mp;
  }

}
