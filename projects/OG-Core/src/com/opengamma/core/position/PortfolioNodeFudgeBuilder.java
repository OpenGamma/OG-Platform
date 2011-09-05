/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.UniqueId;

/**
 * Fudge message builder for {@code PortfolioNode}. This is tightly coupled with the behavior of {@link PositionFudgeBuilder} so
 * that the parent identifiers are not duplicated throughout the tree. Only the outer {@code PortfolioNode} will have its
 * parent identifier written - all others can be inferred during tree construction.
 */
@GenericFudgeBuilderFor(PortfolioNode.class)
public class PortfolioNodeFudgeBuilder implements FudgeBuilder<PortfolioNode> {

  /** Field name. */
  public static final String FIELD_POSITIONS = "positions";
  /** Field name. */
  public static final String FIELD_SUBNODES = "subNodes";
  /** Field name. */
  public static final String FIELD_NAME = "name";
  /** Field name. */
  public static final String FIELD_IDENTIFIER = "identifier";
  /** Field name. */
  public static final String FIELD_PARENT = "parent";

  // -------------------------------------------------------------------------
  private static void encodePositions(final MutableFudgeMsg message, final FudgeSerializer serializer, final Collection<Position> collection) {
    if (!collection.isEmpty()) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (Position position : collection) {
        msg.add(null, null, PositionFudgeBuilder.buildMessageImpl(serializer, position));
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
  private static void readPositions(FudgeDeserializer deserializer, FudgeMsg message, SimplePortfolioNode node) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final SimplePosition position = PositionFudgeBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          position.setParentNodeId(node.getUniqueId());
          node.addPosition(position);
        }
      }
    }
  }

  private static void readSubNodes(FudgeDeserializer deserializer, FudgeMsg message, SimplePortfolioNode node) {
    if (message != null) {
      for (FudgeField field : message) {
        if (field.getValue() instanceof FudgeMsg) {
          final SimplePortfolioNode child = buildObjectImpl(deserializer, (FudgeMsg) field.getValue());
          child.setParentNodeId(node.getUniqueId());
          node.addChildNode(child);
        }
      }
    }
  }

  private static SimplePortfolioNode buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueId id = idField != null ? deserializer.fieldValueToObject(UniqueId.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    final SimplePortfolioNode node = new SimplePortfolioNode(name);
    if (id != null) {
      node.setUniqueId(id);
    }
    readPositions(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_POSITIONS)), node);
    readSubNodes(deserializer, message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_SUBNODES)), node);
    return node;
  }

  @Override
  public PortfolioNode buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final SimplePortfolioNode node = buildObjectImpl(deserializer, message);
    final FudgeField parentField = message.getByName(FIELD_PARENT);
    final UniqueId parentId = (parentField != null) ? deserializer.fieldValueToObject(UniqueId.class, parentField) : null;
    if (parentId != null) {
      node.setParentNodeId(parentId);
    }
    return node;
  }

}
