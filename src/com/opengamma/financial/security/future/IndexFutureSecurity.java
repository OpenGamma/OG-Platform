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
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * An index future.
 */
public class IndexFutureSecurity extends FutureSecurity {

  /**
   * 
   */
  protected static final String UNDERLYINGIDENTIFIER_KEY = "underlyingIdentifier";

  /** The underlying identifier. */
  private final Identifier _underlyingIdentifier;

  /**
   * Creates an index future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param underlyingIdentifier  the underlying identifier
   */
  public IndexFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _underlyingIdentifier = underlyingIdentifier;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying identifier.
   * @return the identifier
   */
  public Identifier getUnderlyingIdentityKey() {
    return _underlyingIdentifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final FutureSecurityVisitor<T> visitor) {
    return visitor.visitIndexFutureSecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsg(message, UNDERLYINGIDENTIFIER_KEY, null, getUnderlyingIdentityKey());
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

  public static IndexFutureSecurity fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final IndexFutureSecurity security = new IndexFutureSecurity(context.fieldValueToObject(Expiry.class, message
        .getByName(EXPIRY_KEY)), message.getString(TRADINGEXCHANGE_KEY), message.getString(SETTLEMENTEXCHANGE_KEY),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)), context.fieldValueToObject(
            Identifier.class, message.getByName(UNDERLYINGIDENTIFIER_KEY)));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }
}
