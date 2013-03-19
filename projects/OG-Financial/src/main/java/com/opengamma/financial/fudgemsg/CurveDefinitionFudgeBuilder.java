/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@FudgeBuilderFor(CurveDefinition.class)
public class CurveDefinitionFudgeBuilder implements FudgeBuilder<CurveDefinition> {
  private static final String UNIQUE_ID_FIELD = "uniqueId";
  private static final String NAME_FIELD = "name";
  private static final String NODE_FIELD = "node";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
    message.add(NAME_FIELD, object.getName());
    for (final CurveNode node : object.getNodes()) {
      serializer.addToMessage(message, NODE_FIELD, null, node);
    }
    return message;
  }

  @Override
  public CurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final Set<CurveNode> nodes = new TreeSet<>();
    final List<FudgeField> nodesFields = message.getAllByName(NODE_FIELD);
    for (final FudgeField nodeField : nodesFields) {
      final Object obj = deserializer.fieldValueToObject(nodeField);
      nodes.add((CurveNode) obj);
    }
    final CurveDefinition curveDefinition = new CurveDefinition(name, nodes);
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return curveDefinition;
  }


}
