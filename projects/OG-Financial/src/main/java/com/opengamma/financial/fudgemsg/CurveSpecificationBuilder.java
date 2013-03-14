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
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Builder for converting {@link CurveSpecification} instances to/from Fudge messages.
 */
@FudgeBuilderFor(CurveSpecification.class)
public class CurveSpecificationBuilder implements FudgeBuilder<CurveSpecification> {
  private static final String CURVE_DATE_FIELD = "curveDate";
  private static final String NAME_FIELD = "name";
  private static final String ID_FIELD = "id";
  private static final String RESOLVED_STRIPS_FIELD = "resolvedStrips";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
    message.add(NAME_FIELD, object.getName());
    serializer.addToMessage(message, ID_FIELD, null, object.getIdentifier());
    for (final CurveNodeWithIdentifier resolvedStrip : object.getStrips()) {
      serializer.addToMessage(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip);
    }
    return message;
  }

  @Override
  public CurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName(CURVE_DATE_FIELD));
    final String name = message.getString(NAME_FIELD);
    final UniqueIdentifiable id = deserializer.fieldValueToObject(UniqueIdentifiable.class, message.getByName(ID_FIELD));
    final List<FudgeField> resolvedStripFields = message.getAllByName(RESOLVED_STRIPS_FIELD);
    final List<CurveNodeWithIdentifier> resolvedStrips = new ArrayList<>();
    for (final FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(deserializer.fieldValueToObject(CurveNodeWithIdentifier.class, resolvedStripField));
    }
    return new CurveSpecification(curveDate, name, id, resolvedStrips);
  }

}
