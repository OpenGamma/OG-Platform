/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.id.Identifier;

/**
 * A {@code Security} used to model equities.
 */
public class EquitySecurity extends FinancialSecurity {

  /**
   *
   */
  protected static final String TICKER_KEY = "ticker";
  /**
  *
  */
  protected static final String EXCHANGE_KEY = "exchange";
  /**
  *
  */
  protected static final String EXCHANGECODE_KEY = "exchangeCode";
  /**
  *
  */
  protected static final String COMPANYNAME_KEY = "companyName";
  /**
  *
  */
  protected static final String CURRENCY_KEY = "currency";
  /**
  *
  */
  protected static final String GICSCODE_KEY = "gicsCode";

  /**
   * The security type of equity.
   */
  public static final String EQUITY_TYPE = "EQUITY";

  /**
   * The ticker symbol.
   */
  private String _ticker;
  /**
   * The exchange.
   */
  private String _exchange;
  /**
   * The exchange code.
   */
  private String _exchangeCode;
  /**
   * The company name.
   */
  private String _companyName;
  /**
   * The currency.
   */
  private Currency _currency;
  /**
   * The GICS code.
   */
  private GICSCode _gicsCode;

  // Identifiers that might be valid for equities:
  // - Bloomberg ticker (in BbgId)
  // - CUSIP (in CUSIP)
  // - ISIN (in ISIN)
  // - Bloomberg Unique ID (in BbgUniqueId)

  /**
   * Creates an equity security.
   */
  public EquitySecurity() {
    super(EQUITY_TYPE);
  }

  /**
   * Creates a security.
   * 
   * @param scheme  the scheme of the equity identifier, not null
   * @param value  the value of the equity identifier, not null
   */
  public EquitySecurity(String scheme, String value) {
    // TODO: consider removal
    this();
    addIdentifier(Identifier.of(scheme, value));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the ticker.
   * @return the ticker
   */
  public String getTicker() {
    return _ticker;
  }

  /**
   * Sets the ticker.
   * @param ticker  the ticker to set
   */
  public void setTicker(String ticker) {
    _ticker = ticker;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange.
   * @return the exchange
   */
  public String getExchange() {
    return _exchange;
  }

  /**
   * Sets the exchange.
   * @param exchange  the exchange to set
   */
  public void setExchange(String exchange) {
    _exchange = exchange;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange code.
   * @return the exchange code
   */
  public String getExchangeCode() {
    return _exchangeCode;
  }

  /**
   * Sets the exchange code.
   * @param exchangeCode  the exchange code to set
   */
  public void setExchangeCode(String exchangeCode) {
    _exchangeCode = exchangeCode;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the company name.
   * @return the company name
   */
  public String getCompanyName() {
    return _companyName;
  }

  /**
   * Sets the company name.
   * @param companyName  the company name to set
   */
  public void setCompanyName(String companyName) {
    _companyName = companyName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the currency to set
   */
  public void setCurrency(Currency currency) {
    _currency = currency;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the GICS code.
   * @return the GICS Code
   */
  public GICSCode getGICSCode() {
    return _gicsCode;
  }

  /**
   * Sets the GICS code.
   * @param gicsCode  the GICS code to set
   */
  public void setGICSCode(GICSCode gicsCode) {
    _gicsCode = gicsCode;
  }

  //-------------------------------------------------------------------------
  /**
   * Override to use the company name as the display name.
   * @return the display name, not null
   */
  @Override
  protected String buildDefaultDisplayName() {
    if (getCompanyName() != null) {
      return getCompanyName();
    } else {
      return super.buildDefaultDisplayName();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitEquitySecurity(this);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsg(message, TICKER_KEY, null, getTicker());
    context.objectToFudgeMsg(message, EXCHANGE_KEY, null, getExchange());
    context.objectToFudgeMsg(message, EXCHANGECODE_KEY, null, getExchangeCode());
    context.objectToFudgeMsg(message, COMPANYNAME_KEY, null, getCompanyName());
    context.objectToFudgeMsg(message, CURRENCY_KEY, null, getCurrency());
    context.objectToFudgeMsg(message, GICSCODE_KEY, null, getGICSCode());
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    final FudgeField ticker = message.getByName(TICKER_KEY);
    final FudgeField exchange = message.getByName(EXCHANGE_KEY);
    final FudgeField exchangeCode = message.getByName(EXCHANGECODE_KEY);
    final FudgeField companyName = message.getByName(COMPANYNAME_KEY);
    final FudgeField currency = message.getByName(CURRENCY_KEY);
    final FudgeField gicsCode = message.getByName(GICSCODE_KEY);
    if (ticker != null) {
      setTicker(context.fieldValueToObject(String.class, ticker));
    }
    if (exchange != null) {
      setExchange(context.fieldValueToObject(String.class, exchange));
    }
    if (exchangeCode != null) {
      setExchangeCode(context.fieldValueToObject(String.class, exchangeCode));
    }
    if (companyName != null) {
      setCompanyName(context.fieldValueToObject(String.class, companyName));
    }
    if (currency != null) {
      setCurrency(context.fieldValueToObject(Currency.class, currency));
    }
    if (gicsCode != null) {
      setGICSCode(context.fieldValueToObject(GICSCode.class, gicsCode));
    }
  }

  public static EquitySecurity fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final EquitySecurity security = new EquitySecurity();
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
