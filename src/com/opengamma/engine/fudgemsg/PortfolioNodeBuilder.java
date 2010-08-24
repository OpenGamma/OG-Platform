/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code PortfolioNode}.
 */
@FudgeBuilderFor(PortfolioNode.class)
public class PortfolioNodeBuilder implements FudgeBuilder<PortfolioNode> {

  private static final String FIELD_POSITIONS = "positions";
  private static final String FIELD_SUBNODES = "subNodes";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_IDENTIFIER = "identifier";

  //-------------------------------------------------------------------------
  private static FudgeFieldContainer encodePositions(FudgeSerializationContext context, Collection<Position> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Position position : collection) {
      context.objectToFudgeMsg(msg, null, null, position);
    }
    return msg;
  }

  private static FudgeFieldContainer encodeSubNodes(FudgeSerializationContext context, Collection<PortfolioNode> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (PortfolioNode node : collection) {
      context.objectToFudgeMsg(msg, null, null, node);
    }
    return msg;
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, PortfolioNode node) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, FIELD_IDENTIFIER, null, node.getUniqueIdentifier());
    message.add(FIELD_NAME, node.getName());
    message.add(FIELD_POSITIONS, encodePositions(context, node.getPositions()));
    message.add(FIELD_SUBNODES, encodeSubNodes(context, node.getChildNodes()));
    return message;
  }

  //-------------------------------------------------------------------------
  private static void readPositions(FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl node) {
    for (FudgeField field : message) {
      node.addPosition(context.fieldValueToObject(Position.class, field));
    }
  }

  private static void readSubNodes(FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl node) {
    for (FudgeField field : message) {
      node.addChildNode(context.fieldValueToObject(PortfolioNode.class, field));
    }
  }

  @Override
  public PortfolioNode buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueIdentifier id = idField != null ? context.fieldValueToObject(UniqueIdentifier.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    
    final PortfolioNodeImpl node = new PortfolioNodeImpl(name);
    if (id != null) {
      node.setUniqueIdentifier(id);
    }
    readPositions(context, message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_POSITIONS)), node);
    readSubNodes(context, message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_SUBNODES)), node);
    return node;
  }

}
