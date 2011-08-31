/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.future.CommodityFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code CommodityFutureSecurity}.
 */
@FudgeBuilderFor(CommodityFutureSecurity.class)
public class CommodityFutureSecurityBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String COMMODITY_TYPE_KEY = "commodityType";
  /** Field name. */
  public static final String UNIT_NUMBER_KEY = "unitNumber";
  /** Field name. */
  public static final String UNIT_NAME_KEY = "unitName";

  public static void toFudgeMsg(FudgeSerializer serializer, CommodityFutureSecurity object, final MutableFudgeMsg msg) {
    FutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, COMMODITY_TYPE_KEY, object.getCommodityType());
    addToMessage(msg, UNIT_NUMBER_KEY, object.getUnitNumber());
    addToMessage(msg, UNIT_NAME_KEY, object.getUnitName());
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, CommodityFutureSecurity object) {
    FutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setCommodityType(msg.getString(COMMODITY_TYPE_KEY));
    object.setUnitNumber(msg.getDouble(UNIT_NUMBER_KEY));
    object.setUnitName(msg.getString(UNIT_NAME_KEY));
  }

}
