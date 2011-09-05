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

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;

/**
 * A Fudge builder for {@code FXForwardSecurity}.
 */
@FudgeBuilderFor(FXForwardSecurity.class)
public class FXForwardSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FXForwardSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  /** Field name. */
  public static final String FORWARD_DATE_KEY = "forwardDate";
  /** Field name. */
  public static final String REGION_KEY = "region";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FXForwardSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    FXForwardSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, FXForwardSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, FORWARD_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getForwardDate()));
    addToMessage(msg, REGION_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getRegionId()));
  }

  @Override
  public FXForwardSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FXForwardSecurity object = new FXForwardSecurity();
    FXForwardSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FXForwardSecurity object) {
    FinancialSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_KEY)));
    object.setForwardDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FORWARD_DATE_KEY)));
    object.setRegionId(ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_KEY)));
  }

}
