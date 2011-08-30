/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ZonedDateTimeBuilder;

/**
 * A Fudge builder for {@code BondFutureSecurity}.
 */
@FudgeBuilderFor(BondFutureSecurity.class)
public class BondFutureSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<BondFutureSecurity> {

  /** Field name. */
  public static final String BASKET_KEY = "basket";
  /** Field name. */
  public static final String BOND_TYPE_KEY = "bondType";
  /** Field name. */
  public static final String FIRST_DELIVERY_DATE_KEY = "firstDeliveryDate";
  /** Field name. */
  public static final String LAST_DELIVERY_DATE_KEY = "lastDeliveryDate";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BondFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    BondFutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, BondFutureSecurity object, final MutableFudgeMsg msg) {
    FutureSecurityBuilder.toFudgeMsg(serializer, object, msg);
    if (object.getBasket() != null) {
      for (BondFutureDeliverable bfd : object.getBasket()) {
        addToMessage(serializer, msg, BASKET_KEY, bfd, BondFutureDeliverable.class);
      }
    }
    addToMessage(msg, BOND_TYPE_KEY, object.getBondType());
    addToMessage(msg, FIRST_DELIVERY_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getFirstDeliveryDate()));
    addToMessage(msg, LAST_DELIVERY_DATE_KEY, ZonedDateTimeBuilder.toFudgeMsg(serializer, object.getLastDeliveryDate()));
  }

  @Override
  public BondFutureSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    BondFutureSecurity object = FinancialSecurityBuilder.backdoorCreateClass(BondFutureSecurity.class);
    BondFutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BondFutureSecurity object) {
    FutureSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    List<FudgeField> basketFields = msg.getAllByName(BASKET_KEY);
    List<BondFutureDeliverable> basket = new ArrayList<BondFutureDeliverable>(basketFields.size());
    for (FudgeField field : basketFields) {
      basket.add(deserializer.fieldValueToObject(BondFutureDeliverable.class, field));
    }
    object.setBasket(basket);
    object.setBondType(msg.getString(BOND_TYPE_KEY));
    object.setFirstDeliveryDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIRST_DELIVERY_DATE_KEY)));
    object.setLastDeliveryDate(ZonedDateTimeBuilder.fromFudgeMsg(deserializer, msg.getMessage(LAST_DELIVERY_DATE_KEY)));
  }

}
