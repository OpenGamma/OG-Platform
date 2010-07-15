/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * A bond future.
 */
public class BondFutureSecurity extends FutureSecurity {

  /**
   * 
   */
  protected static final String BASKET_KEY = "basket";
  /**
   * 
   */
  protected static final String TYPE_KEY = "type";

  /** The basket of bonds that could be delivered to satisfy the contract. */
  private final Set<BondFutureDeliverable> _basket;
  /** The type, such as Bund, Long Bond. */
  private final String _type;

  /**
   * Creates a bond future.
   * @param expiry  the expiry date/time of the future
   * @param tradingExchange  the exchange that the future is traded on
   * @param settlementExchange  the exchange that the future is settled on
   * @param currency  the currency of the contract
   * @param type  the type, such as Bund, Long Bond
   * @param basket  the basket of bonds that could be delivered to satisfy the contract
   */
  public BondFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency currency, 
                            String type, Set<BondFutureDeliverable> basket) {
    super(expiry, tradingExchange, settlementExchange, currency);
    _basket = basket;
    _type = type;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the basket of bonds that could be delivered to satisfy the contract.
   * @return the basket of bonds
   */
  public Set<BondFutureDeliverable> getBasket() {
    return _basket;
  }

  /**
   * Gets the bond type, such as Bund, Long Bond.
   * @return the bond type
   */
  public String getBondType() {
    return _type;
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitBondFutureSecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsg(message, BASKET_KEY, null, getBasket());
    message.add(TYPE_KEY, getBondType());
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // Everything set from constructor
  }

  @SuppressWarnings("unchecked")
  public static BondFutureSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final BondFutureSecurity security = new BondFutureSecurity(context.fieldValueToObject(Expiry.class, message
        .getByName(EXPIRY_KEY)), message.getString(TRADINGEXCHANGE_KEY), message.getString(SETTLEMENTEXCHANGE_KEY),
        context.fieldValueToObject(Currency.class, message.getByName(CURRENCY_KEY)), message.getString(TYPE_KEY),
        context.fieldValueToObject(Set.class, message.getByName(BASKET_KEY)));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }
}
