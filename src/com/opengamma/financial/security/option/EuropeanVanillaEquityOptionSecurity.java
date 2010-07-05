/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Expiry;

/**
 * A European equity option security.
 */
public class EuropeanVanillaEquityOptionSecurity extends EquityOptionSecurity implements EuropeanVanillaOption {

  /**
   * Creates the security.
   * @param optionType the option type (PUT or CALL)
   * @param strike the strike price
   * @param expiry Expire date of option
   * @param underlyingIdentifier the identifier for underlying security
   * @param currency the security currency
   * @param pointValue the option point value
   * @param exchange the exchange where security trades
   */
  public EuropeanVanillaEquityOptionSecurity(final OptionType optionType, final double strike, final Expiry expiry, final Identifier underlyingIdentifier,
      final Currency currency, final double pointValue, final String exchange) {
    super(optionType, strike, expiry, underlyingIdentifier, currency, pointValue, exchange);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(final OptionVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaOption(this);
  }

  @Override
  public <T> T accept(final EquityOptionSecurityVisitor<T> visitor) {
    return visitor.visitEuropeanVanillaEquityOptionSecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    // No additional fields
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    // No additional fields
  }

  public static EuropeanVanillaEquityOptionSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final EuropeanVanillaEquityOptionSecurity security = new EuropeanVanillaEquityOptionSecurity(context
        .fieldValueToObject(OptionType.class, message.getByName(OPTIONTYPE_KEY)), message.getDouble(STRIKE_KEY),
        context.fieldValueToObject(Expiry.class, message.getByName(EXPIRY_KEY)), context.fieldValueToObject(
            Identifier.class, message.getByName(UNDERLYINGIDENTIFIER_KEY)), context.fieldValueToObject(Currency.class,
            message.getByName(CURRENCY_KEY)), message.getDouble(POINTVALUE_KEY), message.getString(EXCHANGE_KEY));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
