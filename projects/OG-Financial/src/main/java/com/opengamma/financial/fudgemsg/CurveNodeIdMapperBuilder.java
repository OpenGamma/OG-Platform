/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@FudgeBuilderFor(CurveNodeIdMapper.class)
public class CurveNodeIdMapperBuilder implements FudgeBuilder<CurveNodeIdMapper> {
  private static final String CREDIT_SPREAD = "creditSpreadIds";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeIdMapper object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CurveNodeIdMapper.class);

    if (object.getCreditSpreadIds() != null) {
      final MutableFudgeMsg creditSpreadIdsMessage = serializer.newMessage();
      for (final Map.Entry<Tenor, CurveInstrumentProvider> entry : object.getCreditSpreadIds().entrySet()) {
        serializer.addToMessageWithClassHeaders(creditSpreadIdsMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
      }
      message.add(CREDIT_SPREAD, creditSpreadIdsMessage);
    }
    return message;
  }

  @Override
  public CurveNodeIdMapper buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    Map<Tenor, CurveInstrumentProvider> creditSpreadIds = null;
    if (message.hasField(CREDIT_SPREAD)) {
      creditSpreadIds = new HashMap<>();
      final FudgeMsg creditSpreadIdsMessage = message.getMessage(CREDIT_SPREAD);
      for (final FudgeField field : creditSpreadIdsMessage.getAllFields()) {
        creditSpreadIds.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }
    return new CurveNodeIdMapper(creditSpreadIds);
  }

}
