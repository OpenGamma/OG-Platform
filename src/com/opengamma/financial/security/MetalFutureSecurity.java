/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * A metal commodity future.
 */
public class MetalFutureSecurity extends CommodityFutureSecurity {

  /**
   *
   */
  protected static final String UNDERLYINGIDENTIFIER_KEY = "underlyingIdentifier";

  /** The underlying identifier. */
  private Identifier _underlyingIdentifier;

  /**
   * Creates a metal commodity future.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type
   * @param unitNumber  the unit number
   * @param unitName  the unit name
   * @param underlyingIdentifier  the underlying identifier
   */
  public MetalFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type, final Double unitNumber, final String unitName, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency, type, unitNumber, unitName);
    _underlyingIdentifier = underlyingIdentifier;
  }

  /**
   * Creates a metal commodity future with no amount.
   * @param expiry  the expiry of the future
   * @param tradingExchange  the exchange that the future is trading on
   * @param settlementExchange  the exchange where the future is settled
   * @param currency  the currency
   * @param type  the type
   * @param underlyingIdentifier  the underlying identifier
   */
  public MetalFutureSecurity(
      final Expiry expiry, final String tradingExchange, final String settlementExchange,
      final Currency currency, final String type, final Identifier underlyingIdentifier) {
    super(expiry, tradingExchange, settlementExchange, currency, type);
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
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitMetalFutureSecurity(this);
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
    // Everything set in constructor
  }

  public static MetalFutureSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final FudgeField underlyingIdentifier = message.getByName(UNDERLYINGIDENTIFIER_KEY);
    final MetalFutureSecurity security = new MetalFutureSecurity(context.fieldValueToObject(Expiry.class, message
        .getByName(EXPIRY_KEY)), message.getString(TRADINGEXCHANGE_KEY), message.getString(SETTLEMENTEXCHANGE_KEY),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)), message
            .getString(COMMODITYTYPE_KEY), message.getDouble(UNITNUMBER_KEY), message.getString(UNITNAME_KEY),
        (underlyingIdentifier != null) ? context.fieldValueToObject(Identifier.class, underlyingIdentifier) : null);
    security.fromFudgeMsgImpl(context, message);
    return security;
  }
}
