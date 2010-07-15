/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * An interest rate future.
 */
public class InterestRateFutureSecurity extends FutureSecurity {

  protected static final String CASHRATETYPE_KEY = "cashRateType";

  /** The cash rate type. */
  private final String _cashRateType; // REVIEW: jim 28-May-2010 -- we might want to make this UniqueIdentifier like FloatingInterestRateLeg...

  /**
   * Creates an interest rate future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param cashRateType  the cash rate type
   */
  public InterestRateFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String cashRateType) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _cashRateType = cashRateType;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the cash rate type.
   * @return the cash rate type
   */
  public String getCashRateType() {
    return _cashRateType;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitInterestRateFutureSecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    message.add(CASHRATETYPE_KEY, getCashRateType());
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // Everything set by constructor
  }

  public static InterestRateFutureSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final InterestRateFutureSecurity security = new InterestRateFutureSecurity(context.fieldValueToObject(Expiry.class,
        message.getByName(EXPIRY_KEY)), message.getString(TRADINGEXCHANGE_KEY), message
        .getString(SETTLEMENTEXCHANGE_KEY),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)), message
            .getString(CASHRATETYPE_KEY));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
