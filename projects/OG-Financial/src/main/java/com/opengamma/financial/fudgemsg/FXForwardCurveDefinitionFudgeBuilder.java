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

import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@FudgeBuilderFor(FXForwardCurveDefinition.class)
public class FXForwardCurveDefinitionFudgeBuilder implements FudgeBuilder<FXForwardCurveDefinition> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXForwardCurveDefinition object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add("target", FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add("name", object.getName());
    for (final Tenor tenor : object.getTenors()) {
      serializer.addToMessage(message, "tenor", null, tenor);
    }
    return message;
  }

  @Override
  public FXForwardCurveDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UnorderedCurrencyPair target = deserializer.fieldValueToObject(UnorderedCurrencyPair.class, message.getByName("target"));
    final String name = message.getString("name");
    final List<FudgeField> tenorFields = message.getAllByName("tenor");
    final List<Tenor> tenors = new ArrayList<Tenor>();
    for (final FudgeField tenorField : tenorFields) {
      tenors.add(deserializer.fieldValueToObject(Tenor.class, tenorField));
    }
    return FXForwardCurveDefinition.of(name, target, tenors);
  }

}
