/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CurveStrip;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@FudgeBuilderFor(CurveDefinition.class)
public class CurveDefinitionFudgeBuilder implements FudgeBuilder<CurveDefinition> {
  private static final String UNIQUE_ID_FIELD = "uniqueId";
  private static final String NAME_FIELD = "name";
  private static final String STRIP_FIELD = "strip";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(UNIQUE_ID_FIELD, object.getUniqueId());
    message.add(NAME_FIELD, object.getName());
    for (final CurveStrip strip : object.getStrips()) {
      serializer.addToMessage(message, STRIP_FIELD, null, strip);
    }
    return message;
  }

  @Override
  public CurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final Set<CurveStrip> strips = new HashSet<>();
    final List<FudgeField> stripFields = message.getAllByName(STRIP_FIELD);
    for (final FudgeField stripField : stripFields) {
      final CurveStrip strip = (CurveStrip) stripField.getValue();
      strips.add(strip);
    }
    final CurveDefinition curveDefinition = new CurveDefinition(name, strips);
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return curveDefinition;
  }


}
