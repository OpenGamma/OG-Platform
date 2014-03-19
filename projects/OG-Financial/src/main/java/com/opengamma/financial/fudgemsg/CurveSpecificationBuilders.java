/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.AbstractCurveSpecification;
import com.opengamma.financial.analytics.curve.ConstantCurveSpecification;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.curve.SpreadCurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;

/**
 * Contains fudge builders for descendant classes of {@link AbstractCurveSpecification}.
 */
public final class CurveSpecificationBuilders {
  /** The curve date field */
  private static final String CURVE_DATE_FIELD = "curveDate";
  /** The curve name field */
  private static final String NAME_FIELD = "name";
  /** The strips field */
  private static final String RESOLVED_STRIPS_FIELD = "resolvedStrips";

  /**
   * Private constructor
   */
  private CurveSpecificationBuilders() {
  }

  /**
   * Builder for converting {@link CurveSpecification} instances to/from Fudge messages.
   */
  @FudgeBuilderFor(CurveSpecification.class)
  public static final class CurveSpecificationBuilder implements FudgeBuilder<CurveSpecification> {

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveSpecification object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
      message.add(NAME_FIELD, object.getName());
      for (final CurveNodeWithIdentifier resolvedStrip : object.getNodes()) {
        serializer.addToMessageWithClassHeaders(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip, CurveNodeWithIdentifier.class);
      }
      return message;
    }

    @Override
    public CurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
      final String name = message.getString(NAME_FIELD);
      final List<FudgeField> nodeFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
      final List<CurveNodeWithIdentifier> nodes = new ArrayList<>();
      for (final FudgeField resolvedStripField : nodeFields) {
        nodes.add(deserializer.fieldValueToObject(CurveNodeWithIdentifier.class, resolvedStripField));
      }
      return new CurveSpecification(curveDate, name, nodes);
    }

  }

  /**
   * Builder for converting {@link InterpolatedCurveSpecification} instances to/from Fudge messages.
   */
  @FudgeBuilderFor(InterpolatedCurveSpecification.class)
  public static final class InterpolatedCurveSpecificationBuilder implements FudgeBuilder<InterpolatedCurveSpecification> {
    /** The interpolator name field */
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    /** The right extrapolator name field */
    private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
    /** The left extrapolator name field */
    private static final String LEFT_EXTRAPOLATOR_NAME_FIELD = "leftExtrapolatorName";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedCurveSpecification object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
      message.add(NAME_FIELD, object.getName());
      for (final CurveNodeWithIdentifier resolvedStrip : object.getNodes()) {
        serializer.addToMessageWithClassHeaders(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip, CurveNodeWithIdentifier.class);
      }
      message.add(INTERPOLATOR_NAME_FIELD, object.getInterpolatorName());
      message.add(RIGHT_EXTRAPOLATOR_NAME_FIELD, object.getRightExtrapolatorName());
      message.add(LEFT_EXTRAPOLATOR_NAME_FIELD, object.getLeftExtrapolatorName());
      return message;
    }

    @Override
    public InterpolatedCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
      final String name = message.getString(NAME_FIELD);
      final List<FudgeField> nodeFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
      final List<CurveNodeWithIdentifier> nodes = new ArrayList<>();
      for (final FudgeField resolvedStripField : nodeFields) {
        nodes.add(deserializer.fieldValueToObject(CurveNodeWithIdentifier.class, resolvedStripField));
      }
      final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
      final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_NAME_FIELD);
      final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_NAME_FIELD);
      return new InterpolatedCurveSpecification(curveDate, name, nodes, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
    }
  }

  /**
   * Builder for converting {@link ConstantCurveSpecification} instances to / from Fudge messages.
   */
  @FudgeBuilderFor(ConstantCurveSpecification.class)
  public static final class ConstantCurveSpecificationBuilder implements FudgeBuilder<ConstantCurveSpecification> {
    /** The data id field */
    private static final String DATA_ID_FIELD = "dataId";
    /** The data field */
    private static final String DATA_FIELD = "data";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ConstantCurveSpecification object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, DATA_ID_FIELD, null, object.getIdentifier());
      message.add(DATA_FIELD, null, object.getDataField());
      return message;
    }

    @Override
    public ConstantCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
      final String name = message.getString(NAME_FIELD);
      final ExternalId dataId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(DATA_ID_FIELD));
      final String dataField = message.getString(DATA_FIELD);
      return new ConstantCurveSpecification(curveDate, name, dataId, dataField);
    }
  }

  /**
   * Builder for converting {@link SpreadCurveSpecification} instances to / from Fudge messages.
   */
  @FudgeBuilderFor(SpreadCurveSpecification.class)
  public static final class SpreadCurveSpecificationBuilder implements FudgeBuilder<SpreadCurveSpecification> {
    /** The first curve field */
    private static final String FIRST_CURVE_FIELD = "firstCurve";
    /** The second curve field */
    private static final String SECOND_CURVE_FIELD = "secondCurve";
    /** The operation field */
    private static final String OPERATION_FIELD = "operation";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SpreadCurveSpecification object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, FIRST_CURVE_FIELD, null, object.getFirstCurve());
      message.add(OPERATION_FIELD, object.getOperation());
      serializer.addToMessage(message, SECOND_CURVE_FIELD, null, object.getSecondCurve());
      return message;
    }

    @Override
    public SpreadCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
      final String name = message.getString(NAME_FIELD);
      final AbstractCurveSpecification firstCurve = deserializer.fieldValueToObject(AbstractCurveSpecification.class, message.getByName(FIRST_CURVE_FIELD));
      final String operation = message.getString(OPERATION_FIELD);
      final AbstractCurveSpecification secondCurve = deserializer.fieldValueToObject(AbstractCurveSpecification.class, message.getByName(SECOND_CURVE_FIELD));
      return new SpreadCurveSpecification(curveDate, name, firstCurve, secondCurve, operation);
    }

  }
}
