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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code PortfolioNode}. This is tightly coupled with the behavior of {@link PositionBuilder} so
 * that the parent identifiers are not duplicated throughout the tree. Only the outer {@code PortfolioNode} will have its
 * parent identifier written - all others can be inferred during tree construction.
 */
@GenericFudgeBuilderFor(PortfolioNode.class)
public class PortfolioNodeBuilder implements FudgeBuilder<PortfolioNode> {

  /**
   * Fudge field name.
   */
  protected static final String FIELD_POSITIONS = "positions";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_SUBNODES = "subNodes";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_NAME = "name";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_IDENTIFIER = "identifier";
  /**
   * Fudge field name.
   */
  protected static final String FIELD_PARENT = "parent";

  // -------------------------------------------------------------------------
  private static FudgeFieldContainer encodePositions(final FudgeSerializationContext context, final Collection<Position> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (Position position : collection) {
      msg.add(null, null, PositionBuilder.buildMessageImpl(context, position));
    }
    return msg;
  }

  private static FudgeFieldContainer encodeSubNodes(final FudgeSerializationContext context, final Collection<PortfolioNode> collection) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    for (PortfolioNode node : collection) {
      msg.add(null, null, buildMessageImpl(context, node));
    }
    return msg;
  }

  private static MutableFudgeFieldContainer buildMessageImpl(final FudgeSerializationContext context, final PortfolioNode node) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, FIELD_IDENTIFIER, null, node.getUniqueId());
    message.add(FIELD_NAME, node.getName());
    message.add(FIELD_POSITIONS, encodePositions(context, node.getPositions()));
    message.add(FIELD_SUBNODES, encodeSubNodes(context, node.getChildNodes()));
    return message;
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final PortfolioNode node) {
    final MutableFudgeFieldContainer message = buildMessageImpl(context, node);
    context.objectToFudgeMsg(message, FIELD_PARENT, null, node.getParentNode());
    return message;
  }

  // -------------------------------------------------------------------------
  private static void readPositions(FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl node) {
    for (FudgeField field : message) {
      if (field.getValue() instanceof FudgeFieldContainer) {
        final PositionImpl position = PositionBuilder.buildObjectImpl(context, (FudgeFieldContainer) field.getValue());
        position.setPortfolioNode(node.getUniqueId());
        node.addPosition(position);
      }
    }
  }

  private static void readSubNodes(FudgeDeserializationContext context, FudgeFieldContainer message, PortfolioNodeImpl node) {
    for (FudgeField field : message) {
      if (field.getValue() instanceof FudgeFieldContainer) {
        final PortfolioNodeImpl child = buildObjectImpl(context, (FudgeFieldContainer) field.getValue());
        child.setParentNode(node.getUniqueId());
        node.addChildNode(child);
      }
    }
  }

  private static PortfolioNodeImpl buildObjectImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueIdentifier id = idField != null ? context.fieldValueToObject(UniqueIdentifier.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    final PortfolioNodeImpl node = new PortfolioNodeImpl(name);
    if (id != null) {
      node.setUniqueId(id);
    }
    readPositions(context, message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_POSITIONS)), node);
    readSubNodes(context, message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_SUBNODES)), node);
    return node;
  }

  @Override
  public PortfolioNode buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final PortfolioNodeImpl node = buildObjectImpl(context, message);
    final FudgeField parentField = message.getByName(FIELD_PARENT);
    final UniqueIdentifier parentId = (parentField != null) ? context.fieldValueToObject(UniqueIdentifier.class, parentField) : null;
    if (parentId != null) {
      node.setParentNode(parentId);
    }
    return node;
  }

}
