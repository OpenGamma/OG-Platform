/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;

public class PortfolioNodeBuilder implements FudgeBuilder<PortfolioNode> {
  
  public static final String FIELD_POSITIONS = "positions";
  public static final String FIELD_SUBNODES = "subNodes";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_IDENTITYKEY = "identityKey";
  
  private static FudgeFieldContainer encodePositions (FudgeSerializationContext context, Collection<Position> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (Position position : collection) {
      context.objectToFudgeMsg (msg, null, null, position);
    }
    return msg;
  }
  
  private static FudgeFieldContainer encodeSubNodes (FudgeSerializationContext context, Collection<PortfolioNode> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    for (PortfolioNode node : collection) {
      context.objectToFudgeMsg (msg, null, null, node);
    }
    return msg;
  }
  
  public static void addPortfolioNodeFields (FudgeSerializationContext context, MutableFudgeFieldContainer msg, PortfolioNode portfolioNode) {
    // PortfolioNode
    msg.add (FIELD_POSITIONS, encodePositions (context, portfolioNode.getPositions ()));
    msg.add (FIELD_SUBNODES, encodeSubNodes (context, portfolioNode.getSubNodes ()));
    msg.add (FIELD_NAME, portfolioNode.getName ());
    // Identifiable
    msg.add (FIELD_IDENTITYKEY, portfolioNode.getIdentityKey ().getValue ()); 
  }
  
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, PortfolioNode portfolioNode) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    addPortfolioNodeFields (context, msg, portfolioNode);
    return msg;
  }
  
  private static void readPositions (FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl portfolioNode) {
    for (FudgeField field : message) {
      portfolioNode.addPosition (context.fieldValueToObject (Position.class, field));
    }
  }
  
  private static void readSubNodes (FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl portfolioNode) {
    for (FudgeField field : message) {
      portfolioNode.addSubNode (context.fieldValueToObject (PortfolioNode.class, field));
    }
  }
  
  public static void readPortfolioNodeFields (FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl portfolioNode) {
    // PortfolioNode
    readPositions (context, message.getFieldValue (FudgeFieldContainer.class, message.getByName (FIELD_POSITIONS)), portfolioNode);
    readSubNodes (context, message.getFieldValue (FudgeFieldContainer.class, message.getByName (FIELD_SUBNODES)), portfolioNode);
    // Identifiable
    portfolioNode.setIdentityKey (message.getFieldValue (String.class, message.getByName (FIELD_IDENTITYKEY)));
  }
  
  @Override
  public PortfolioNode buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final String name = message.getFieldValue (String.class, message.getByName (FIELD_NAME));
    final PortfolioNodeImpl portfolioNode = new PortfolioNodeImpl (name);
    readPortfolioNodeFields (context, message, portfolioNode);
    return portfolioNode;
  }
  
  
}