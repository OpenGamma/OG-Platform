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
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public final class CurveDefinitionBuilders {
  private static final String UNIQUE_ID_FIELD = "uniqueId";
  private static final String NAME_FIELD = "name";
  private static final String NODE_FIELD = "node";

  private CurveDefinitionBuilders() {
  }

  /**
   * Fudge builder for a {@link CurveDefinition}
   */
  @FudgeBuilderFor(CurveDefinition.class)
  public static final class CurveDefinitionFudgeBuilder implements FudgeBuilder<CurveDefinition> {

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

  /**
   * Fudge builder for a {@link InterpolatedCurveDefinition}
   */
  @FudgeBuilderFor(InterpolatedCurveDefinition.class)
  public static final class InterpolatedCurveDefinitionFudgeBuilder implements FudgeBuilder<InterpolatedCurveDefinition> {
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
    private static final String LEFT_EXTRAPOLATOR_NAME_FIELD = "leftExtrapolatorName";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedCurveDefinition object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
      message.add(NAME_FIELD, object.getName());
      for (final CurveNode node : object.getNodes()) {
        serializer.addToMessage(message, NODE_FIELD, null, node);
      }
      message.add(INTERPOLATOR_NAME_FIELD, object.getInterpolatorName());
      if (object.getRightExtrapolatorName() != null) {
        message.add(RIGHT_EXTRAPOLATOR_NAME_FIELD, object.getRightExtrapolatorName());
        message.add(LEFT_EXTRAPOLATOR_NAME_FIELD, object.getLeftExtrapolatorName());
      }
      return message;
    }

    @Override
    public InterpolatedCurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final Set<CurveNode> nodes = new TreeSet<>();
      final List<FudgeField> nodesFields = message.getAllByName(NODE_FIELD);
      for (final FudgeField nodeField : nodesFields) {
        final Object obj = deserializer.fieldValueToObject(nodeField);
        nodes.add((CurveNode) obj);
      }
      final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
      final InterpolatedCurveDefinition curveDefinition;
      if (message.hasField(RIGHT_EXTRAPOLATOR_NAME_FIELD)) {
        final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_NAME_FIELD);
        final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_NAME_FIELD);
        curveDefinition = new InterpolatedCurveDefinition(name, nodes, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
      } else {
        curveDefinition = new InterpolatedCurveDefinition(name, nodes, interpolatorName);
      }
      final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueId != null) {
        curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
      }
      return curveDefinition;
    }
  }
}
