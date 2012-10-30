/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveInstrumentProvider;
import com.opengamma.financial.analytics.forwardcurve.ForwardSwapCurveSpecification;
import com.opengamma.util.money.Currency;

/**
 * 
 */
@FudgeBuilderFor(ForwardSwapCurveSpecification.class)
public class ForwardSwapCurveSpecificationBuilder implements FudgeBuilder<ForwardSwapCurveSpecification> {
  private static final String TARGET_FIELD = "target";
  private static final String NAME_FIELD = "name";
  private static final String INSTRUMENT_PROVIDER_FIELD = "curveInstrumentProvider";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardSwapCurveSpecification object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(TARGET_FIELD, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(object.getTarget()), object.getTarget().getClass()));
    message.add(NAME_FIELD, object.getName());
    serializer.addToMessageWithClassHeaders(message, INSTRUMENT_PROVIDER_FIELD, null, object.getCurveInstrumentProvider());
    return message;
  }

  @Override
  public ForwardSwapCurveSpecification buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Currency target = deserializer.fieldValueToObject(Currency.class, message.getByName(TARGET_FIELD));
    final String name = message.getString(NAME_FIELD);
    final ForwardSwapCurveInstrumentProvider provider = deserializer.fieldValueToObject(ForwardSwapCurveInstrumentProvider.class, message.getByName(INSTRUMENT_PROVIDER_FIELD));
    return new ForwardSwapCurveSpecification(name, target, provider);
  }

}
