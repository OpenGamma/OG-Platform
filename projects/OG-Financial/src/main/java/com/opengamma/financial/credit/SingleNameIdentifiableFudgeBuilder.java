/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.credit;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.UniqueId;
import com.opengamma.util.credit.CreditCurveIdentifier;

/**
 * Builder for converting {@link CreditCurveIdentifier} instances to / from Fudge messages.
 */
@FudgeBuilderFor(SingleNameIdentifiable.class)
public class SingleNameIdentifiableFudgeBuilder implements FudgeBuilder<SingleNameIdentifiable> {
  private static String ID_FIELD = "id";

  @SuppressWarnings("deprecation")
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SingleNameIdentifiable object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CreditCurveIdentifier.class);
    message.add(ID_FIELD, object.getUniqueId().toString());
    return message;
  }

  @SuppressWarnings("deprecation")
  @Override
  public SingleNameIdentifiable buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String uniqueId = message.getString(ID_FIELD);
    return SingleNameIdentifiable.of(UniqueId.parse(uniqueId));
  }

}
