/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code MunicipalBondSecurity}.
 */
@FudgeBuilderFor(InflationBondSecurity.class)
public class InflationBondSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<InflationBondSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InflationBondSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    InflationBondSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, InflationBondSecurity object, final MutableFudgeMsg msg) {
    BondSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
  }

  @Override
  public InflationBondSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    InflationBondSecurity object = new InflationBondSecurity();
    InflationBondSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, InflationBondSecurity object) {
    BondSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
