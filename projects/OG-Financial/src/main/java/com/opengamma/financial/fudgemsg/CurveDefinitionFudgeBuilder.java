/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.Collections;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.id.UniqueId;

/**
 * 
 */
@FudgeBuilderFor(CurveDefinition.class)
public class CurveDefinitionFudgeBuilder implements FudgeBuilder<CurveDefinition> {
  private static final String UNIQUE_ID_FIELD = "uniqueId";
  private static final String NAME_FIELD = "name";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(UNIQUE_ID_FIELD, object.getUniqueId());
    message.add(NAME_FIELD, object.getName());
    return message;
  }

  @Override
  public CurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name = message.getString(NAME_FIELD);
    final CurveDefinition curveDefinition = new CurveDefinition(name, Collections.EMPTY_SET);
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return null;
  }


}
