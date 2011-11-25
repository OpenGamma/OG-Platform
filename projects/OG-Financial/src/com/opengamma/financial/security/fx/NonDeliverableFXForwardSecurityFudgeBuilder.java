/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code NonDeliverableFXForwardSecurity}.
 */
@FudgeBuilderFor(NonDeliverableFXForwardSecurity.class)
public class NonDeliverableFXForwardSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<NonDeliverableFXForwardSecurity> {

  /** Field name. */
  public static final String UNDERLYING_IDENTIFIER_FIELD_NAME = "underlyingIdentifier";
  /** Field name. */
  public static final String FORWARD_DATE_FIELD_NAME = "forwardDate";
  /** Field name. */
  public static final String REGION_FIELD_NAME = "region";
  /** Field name. */
  public static final String DELIVERY_IN_RECEIVE_CURRENCY_FIELD_NAME = "deliverableInRecieveCurrency";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, NonDeliverableFXForwardSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    NonDeliverableFXForwardSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, NonDeliverableFXForwardSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingId()));
    addToMessage(msg, FORWARD_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getForwardDate()));
    addToMessage(msg, REGION_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, DELIVERY_IN_RECEIVE_CURRENCY_FIELD_NAME, object.isDeliveryInReceiveCurrency());
  }

  @Override
  public NonDeliverableFXForwardSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    NonDeliverableFXForwardSecurity object = new NonDeliverableFXForwardSecurity();
    NonDeliverableFXForwardSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, NonDeliverableFXForwardSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_IDENTIFIER_FIELD_NAME)));
    object.setForwardDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FORWARD_DATE_FIELD_NAME)));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_FIELD_NAME)));
    object.setDeliveryInReceiveCurrency(msg.getBoolean(DELIVERY_IN_RECEIVE_CURRENCY_FIELD_NAME));
  }

}
