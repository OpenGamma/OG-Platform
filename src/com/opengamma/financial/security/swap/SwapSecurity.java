package com.opengamma.financial.security.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;

/**
 * Base class for swaps.
 */
public class SwapSecurity extends FinancialSecurity {

  /**
   * 
   */
  protected static final String TRADEDATE_KEY = "tradeDate";
  /**
   * 
   */
  protected static final String EFFECTIVEDATE_KEY = "effectiveDate";
  /**
   * 
   */
  protected static final String MATURITYDATE_KEY = "maturityDate";
  /**
   * 
   */
  protected static final String COUNTERPARTY_KEY = "counterparty";
  /**
   * 
   */
  protected static final String PAYLEG_KEY = "payLeg";
  /**
   * 
   */
  protected static final String RECEIVELEG_KEY = "receiveLeg";

  private static final String SECURITY_TYPE = "SWAP";
  private ZonedDateTime _tradeDate;
  private ZonedDateTime _effectiveDate;
  private ZonedDateTime _maturityDate;
  private String _counterparty;
  private SwapLeg _payLeg;
  private SwapLeg _receiveLeg;

  /**
   * @param tradeDate the date the trade begins
   * @param effectiveDate the 'effective' or 'value' date
   * @param maturityDate the 'maturity' or 'termination' date
   * @param counterparty the counterparty
   * @param payLeg the pay leg
   * @param receiveLeg the receive leg
   */
  public SwapSecurity(final ZonedDateTime tradeDate, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final String counterparty, final SwapLeg payLeg, final SwapLeg receiveLeg) {
    super(SECURITY_TYPE);
    Validate.notNull(tradeDate);
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(counterparty);
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    if (tradeDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Trade date cannot be after maturity date");
    }
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date cannot be after maturity date");
    }
    _tradeDate = tradeDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _counterparty = counterparty;
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
  }

  public SwapSecurity() {
    super(SECURITY_TYPE);
  }

  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  public void setTradeDate(final ZonedDateTime tradeDate) {
    Validate.notNull(tradeDate);
    if (tradeDate.isAfter(_maturityDate)) {
      throw new IllegalArgumentException("Trade date cannot be after maturity date");
    }
    _tradeDate = tradeDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public void setEffectiveDate(final ZonedDateTime effectiveDate) {
    Validate.notNull(effectiveDate);
    if (effectiveDate.isAfter(_maturityDate)) {
      throw new IllegalArgumentException("Effective date cannot be after maturity date");
    }
    _effectiveDate = effectiveDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public void setMaturityDate(final ZonedDateTime maturityDate) {
    Validate.notNull(maturityDate);
    _maturityDate = maturityDate;
  }

  public String getCounterparty() {
    return _counterparty;
  }

  public void setCounterparty(final String counterparty) {
    Validate.notNull(counterparty);
    _counterparty = counterparty;
  }

  public SwapLeg getPayLeg() {
    return _payLeg;
  }

  public void setPayLeg(final SwapLeg payLeg) {
    Validate.notNull(payLeg);
    _payLeg = payLeg;
  }

  public SwapLeg getReceiveLeg() {
    return _receiveLeg;
  }

  public void setReceiveLeg(final SwapLeg receiveLeg) {
    Validate.notNull(receiveLeg);
    _receiveLeg = receiveLeg;
  }

  @Override
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return null;
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsg(message, TRADEDATE_KEY, null, getTradeDate());
    context.objectToFudgeMsg(message, EFFECTIVEDATE_KEY, null, getEffectiveDate());
    context.objectToFudgeMsg(message, MATURITYDATE_KEY, null, getMaturityDate());
    message.add(COUNTERPARTY_KEY, getCounterparty());
    context.objectToFudgeMsg(message, PAYLEG_KEY, null, getPayLeg());
    context.objectToFudgeMsg(message, RECEIVELEG_KEY, null, getReceiveLeg());
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    super.fromFudgeMsgImpl(context, message);
    setMaturityDate(context.fieldValueToObject(ZonedDateTime.class, message.getByName(MATURITYDATE_KEY)));
    setTradeDate(context.fieldValueToObject(ZonedDateTime.class, message.getByName(TRADEDATE_KEY)));
    setEffectiveDate(context.fieldValueToObject(ZonedDateTime.class, message.getByName(EFFECTIVEDATE_KEY)));
    setCounterparty (message.getString (COUNTERPARTY_KEY));
    setPayLeg(context.fieldValueToObject(SwapLeg.class, message.getByName(PAYLEG_KEY)));
    setReceiveLeg(context.fieldValueToObject(SwapLeg.class, message.getByName(RECEIVELEG_KEY)));
  }

  public static SwapSecurity fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final SwapSecurity security = new SwapSecurity();
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
