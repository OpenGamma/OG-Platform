/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code GovernmentBondSecurity}.
 */
@FudgeBuilderFor(GovernmentBondSecurity.class)
public class GovernmentBondSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<GovernmentBondSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, GovernmentBondSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    GovernmentBondSecurityBuilder.buildMessage(serializer, object, msg);
    return msg;
  }

  public static void buildMessage(FudgeSerializer serializer, GovernmentBondSecurity object, final MutableFudgeMsg msg) {
    BondSecurityBuilder.buildMessage(serializer, object, msg);
  }

  @Override
  public GovernmentBondSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    GovernmentBondSecurity object = FinancialSecurityBuilder.backdoorCreateClass(GovernmentBondSecurity.class);
    GovernmentBondSecurityBuilder.buildObject(deserializer, msg, object);
    return object;
  }

  public static void buildObject(FudgeDeserializer deserializer, FudgeMsg msg, GovernmentBondSecurity object) {
    BondSecurityBuilder.buildObject(deserializer, msg, object);
  }

}
