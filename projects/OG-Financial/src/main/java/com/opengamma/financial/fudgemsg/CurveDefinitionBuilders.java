/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
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
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.FixedDateInterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.id.UniqueId;

/**
 * Contains builders for all classes that extend {@link CurveDefinition}
 */
public final class CurveDefinitionBuilders {
  /** The unique id field name */
  private static final String UNIQUE_ID_FIELD = "uniqueId";
  /** The name field name */
  private static final String NAME_FIELD = "name";
  /** The node field name */
  private static final String NODE_FIELD = "node";

  /**
   * Private constructor.
   */
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
    /** Interpolator field name */
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    /** Right extrapolator field name */
    private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
    /** Left extrapolator field name */
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

  /**
   * Fudge builder for a {@link FixedDateInterpolatedCurveDefinition}
   */
  @FudgeBuilderFor(FixedDateInterpolatedCurveDefinition.class)
  public static final class FixedDateInterpolatedCurveDefintion implements FudgeBuilder<FixedDateInterpolatedCurveDefinition> {
    /** Interpolator field name */
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    /** Right extrapolator field name */
    private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
    /** Left extrapolator field name */
    private static final String LEFT_EXTRAPOLATOR_NAME_FIELD = "leftExtrapolatorName";
    /** Fixed dates field name */
    private static final String FIXED_DATE_FIELD = "fixedDate";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FixedDateInterpolatedCurveDefinition object) {
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
      final List<LocalDate> fixedDates = object.getFixedDates();
      for (final LocalDate fixedDate : fixedDates) {
        serializer.addToMessageWithClassHeaders(message, FIXED_DATE_FIELD, null, fixedDate, LocalDate.class);
      }
      return message;
    }

    @Override
    public FixedDateInterpolatedCurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final Set<CurveNode> nodes = new TreeSet<>();
      final List<FudgeField> nodesFields = message.getAllByName(NODE_FIELD);
      for (final FudgeField nodeField : nodesFields) {
        final Object obj = deserializer.fieldValueToObject(nodeField);
        nodes.add((CurveNode) obj);
      }
      final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
      final FixedDateInterpolatedCurveDefinition curveDefinition;
      final List<FudgeField> fixedDatesFields = message.getAllByName(FIXED_DATE_FIELD);
      final List<LocalDate> fixedDates = new ArrayList<>();
      for (final FudgeField fixedDateField : fixedDatesFields) {
        fixedDates.add(deserializer.fieldValueToObject(LocalDate.class, fixedDateField));
      }
      if (message.hasField(RIGHT_EXTRAPOLATOR_NAME_FIELD)) {
        final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_NAME_FIELD);
        final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_NAME_FIELD);
        curveDefinition = new FixedDateInterpolatedCurveDefinition(name, nodes, interpolatorName, rightExtrapolatorName, leftExtrapolatorName, fixedDates);
      } else {
        curveDefinition = new FixedDateInterpolatedCurveDefinition(name, nodes, interpolatorName, fixedDates);
      }
      final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueId != null) {
        curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
      }
      return curveDefinition;
    }

  }

}
