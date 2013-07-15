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

import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.InterpolatedCurveSpecification;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;

/**
 * Contains fudge builders for {@link CurveSpecification} and descendant classes.
 */
public final class CurveSpecificationBuilders {
  private static final String CURVE_DATE_FIELD = "curveDate";
  private static final String NAME_FIELD = "name";
  private static final String RESOLVED_STRIPS_FIELD = "resolvedStrips";

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
        serializer.addToMessage(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip);
      }
      return message;
    }

    @Override
    public CurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
      final String name = message.getString(NAME_FIELD);
      final List<FudgeField> resolvedStripFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
      final List<CurveNodeWithIdentifier> resolvedStrips = new ArrayList<>();
      for (final FudgeField resolvedStripField : resolvedStripFields) {
        resolvedStrips.add(deserializer.fieldValueToObject(CurveNodeWithIdentifier.class, resolvedStripField));
      }
      return new CurveSpecification(curveDate, name, resolvedStrips);
    }

  }
  /**
   * Builder for converting {@link CurveSpecification} instances to/from Fudge messages.
   */
  @FudgeBuilderFor(InterpolatedCurveSpecification.class)
  public static final class InterpolatedCurveSpecificationBuilder implements FudgeBuilder<InterpolatedCurveSpecification> {
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    private static final String RIGHT_EXTRAPOLATOR_NAME_FIELD = "rightExtrapolatorName";
    private static final String LEFT_EXTRAPOLATOR_NAME_FIELD = "leftExtrapolatorName";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedCurveSpecification object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
      message.add(NAME_FIELD, object.getName());
      for (final CurveNodeWithIdentifier resolvedStrip : object.getNodes()) {
        serializer.addToMessage(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip);
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
      final List<FudgeField> resolvedStripFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
      final List<CurveNodeWithIdentifier> resolvedStrips = new ArrayList<>();
      for (final FudgeField resolvedStripField : resolvedStripFields) {
        resolvedStrips.add(deserializer.fieldValueToObject(CurveNodeWithIdentifier.class, resolvedStripField));
      }
      final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
      final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_NAME_FIELD);
      final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_NAME_FIELD);
      return new InterpolatedCurveSpecification(curveDate, name, resolvedStrips, interpolatorName, rightExtrapolatorName, leftExtrapolatorName);
    }
  }
}
