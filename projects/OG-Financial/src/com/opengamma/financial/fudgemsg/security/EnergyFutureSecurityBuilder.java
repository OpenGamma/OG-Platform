/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;

/**
 * A Fudge builder for {@code EnergyFutureSecurity}.
 */
@FudgeBuilderFor(EnergyFutureSecurity.class)
public class EnergyFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EnergyFutureSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EnergyFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EnergyFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EnergyFutureSecurity object, final MutableFudgeMsg msg) {
    CommodityFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getUnderlyingIdentifier()));
  }

  @Override
  public EnergyFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EnergyFutureSecurity object = FinancialSecurityBuilder.backdoorCreateClass(EnergyFutureSecurity.class);
    EnergyFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EnergyFutureSecurity object) {
    CommodityFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingIdentifier(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
  }

}
