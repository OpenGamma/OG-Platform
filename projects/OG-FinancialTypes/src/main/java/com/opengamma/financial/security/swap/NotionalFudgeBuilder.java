/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code Notional} implementations.
 */
public class NotionalFudgeBuilder extends AbstractFudgeBuilder {

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code CommodityNotional}.
   */
  @FudgeBuilderFor(CommodityNotional.class)
  public static class CommodityNotionalBuilder extends NotionalFudgeBuilder implements FudgeBuilder<CommodityNotional>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CommodityNotional object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public CommodityNotional buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new CommodityNotional();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code InterestRateNotional}.
   */
  @FudgeBuilderFor(InterestRateNotional.class)
  public static class InterestRateNotionalBuilder extends NotionalFudgeBuilder implements FudgeBuilder<InterestRateNotional>  {
    /** Field name. */
    public static final String CURRENCY_FIELD_NAME = "currency";
    /** Field name. */
    public static final String AMOUNT_FIELD_NAME = "amount";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InterestRateNotional object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
      addToMessage(msg, AMOUNT_FIELD_NAME, object.getAmount());
      return msg;
    }

    @Override
    public InterestRateNotional buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      Currency currency = msg.getValue(Currency.class, CURRENCY_FIELD_NAME);
      double amount = msg.getDouble(AMOUNT_FIELD_NAME);
      return new InterestRateNotional(currency, amount);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code SecurityNotional}.
   */
  @FudgeBuilderFor(SecurityNotional.class)
  public static class SecurityNotionalBuilder extends NotionalFudgeBuilder implements FudgeBuilder<SecurityNotional>  {
    /** Field name. */
    public static final String NOTIONAL_IDENTIFIER_FIELD_NAME = "notionalIdentifier";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SecurityNotional object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, NOTIONAL_IDENTIFIER_FIELD_NAME, UniqueIdFudgeBuilder.toFudgeMsg(serializer, object.getNotionalId()));
      return msg;
    }

    @Override
    public SecurityNotional buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      UniqueId id = UniqueIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(NOTIONAL_IDENTIFIER_FIELD_NAME));
      return new SecurityNotional(id);
    }
  }

}
