/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code EnergyFutureSecurity}.
 */
@FudgeBuilderFor(EnergyFutureSecurity.class)
public class EnergyFutureSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EnergyFutureSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_FIELD_NAME = "underlyingIdentifier";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EnergyFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EnergyFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EnergyFutureSecurity object, final MutableFudgeMsg msg) {
    CommodityFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
  }

  @Override
  public EnergyFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EnergyFutureSecurity object = new EnergyFutureSecurity();
    EnergyFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EnergyFutureSecurity object) {
    CommodityFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_FIELD_NAME)));
  }

}
