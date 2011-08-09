/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.fudgemsg;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.UniqueId;

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
  private static void encodePositions(final MutableFudgeMsg message, final FudgeSerializer serializer, final Collection<Position> collection) {
    if (!collection.isEmpty()) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (Position position : collection) {
        msg.add(null, null, PositionBuilder.buildMessageImpl(serializer, position));
      }
      message.add(FIELD_POSITIONS, msg);
    }
  }

  private static void encodeSubNodes(final MutableFudgeMsg message, final FudgeSerializer serializer, final Collection<PortfolioNode> collection) {
    if (!collection.isEmpty()) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (PortfolioNode node : collection) {
        msg.add(null, null, buildMessageImpl(serializer, node));
      }
      message.add(FIELD_SUBNODES, msg);
    }
  }

  private static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final PortfolioNode node) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, FIELD_IDENTIFIER, null, node.getUniqueId());
    message.add(FIELD_NAME, node.getName());
    encodePositions(message, serializer, node.getPositions());
    encodeSubNodes(message, serializer, node.getChildNodes());
    return message;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final PortfolioNode node) {
    final MutableFudgeMsg message = buildMessageImpl(serializer, node);
    serializer.addToMessage(message, FIELD_PARENT, null, node.getParentNodeId());
    return message;
  }

  // -------------------------------------------------------------------------
  private static void readPositions(FudgeDeserializer deserializer, FudgeMsg message, PortfolioNodeImpl node) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final PositionImpl position = PositionBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          position.setParentNodeId(node.getUniqueId());
          node.addPosition(position);
        }
      }
    }
  }

  private static void readSubNodes(FudgeDeserializer deserializer, FudgeMsg message, PortfolioNodeImpl node) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final PortfolioNodeImpl child = buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          child.setParentNodeId(node.getUniqueId());
          node.addChildNode(child);
        }
      }
    }
  }

  private static PortfolioNodeImpl buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueId id = idField != null ? deserializer.fieldValueToObject(UniqueId.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    final PortfolioNodeImpl node = new PortfolioNodeImpl(name);
    if (id != null) {
      node.setUniqueId(id);
    }
    readPositions(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_POSITIONS)), node);
    readSubNodes(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_SUBNODES)), node);
    return node;
  }

  @Override
  public PortfolioNode buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final PortfolioNodeImpl node = buildObjectImpl(deserializer, message);
    final FudgeField parentField = message.getByName(FIELD_PARENT);
    final UniqueId parentId = (parentField != null) ? deserializer.fieldValueToObject(UniqueId.class, parentField) : null;
    if (parentId != null) {
      node.setParentNodeId(parentId);
    }
    return node;
  }

}
