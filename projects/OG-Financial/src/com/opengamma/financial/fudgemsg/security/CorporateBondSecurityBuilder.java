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

import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code CorporateBondSecurity}.
 */
@FudgeBuilderFor(CorporateBondSecurity.class)
public class CorporateBondSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<CorporateBondSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CorporateBondSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CorporateBondSecurityBuilder.buildMessage(serializer, object, msg);
    return msg;
  }

  public static void buildMessage(FudgeSerializer serializer, CorporateBondSecurity object, final MutableFudgeMsg msg) {
    BondSecurityBuilder.buildMessage(serializer, object, msg);
  }

  @Override
  public CorporateBondSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CorporateBondSecurity object = FinancialSecurityBuilder.backdoorCreateClass(CorporateBondSecurity.class);
    CorporateBondSecurityBuilder.buildObject(deserializer, msg, object);
    return object;
  }

  public static void buildObject(FudgeDeserializer deserializer, FudgeMsg msg, CorporateBondSecurity object) {
    BondSecurityBuilder.buildObject(deserializer, msg, object);
  }

}
