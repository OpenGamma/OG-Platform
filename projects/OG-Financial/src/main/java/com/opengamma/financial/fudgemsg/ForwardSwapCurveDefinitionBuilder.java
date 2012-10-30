/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@FudgeBuilderFor(ForwardSwapCurveDefinition.class)
public class ForwardSwapCurveDefinitionBuilder implements FudgeBuilder<ForwardSwapCurveDefinition> {
  private static final String TARGET_FIELD = "target";
  private static final String NAME_FIELD = "name";
  private static final String TENOR_FIELD = "tenor";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardSwapCurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(TARGET_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add(NAME_FIELD, object.getName());
    for (final Tenor tenor : object.getTenors()) {
      serializer.addToMessage(message, TENOR_FIELD, null, tenor);
    }
    return message;
  }

  @Override
  public ForwardSwapCurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency target = deserializer.fieldValueToObject(Currency.class, message.getByName(TARGET_FIELD));
    final String name = message.getString(NAME_FIELD);
    final List<FudgeField> tenorFields = message.getAllByName(TENOR_FIELD);
    final List<Tenor> tenors = new ArrayList<Tenor>();
    for (final FudgeField tenorField : tenorFields) {
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, tenorField);
      tenors.add(tenor);
    }
    return new ForwardSwapCurveDefinition(name, target, tenors.toArray(new Tenor[0]));
  }
}
