/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import javax.time.calendar.DateTimeProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * 
 *
 */
public class ForwardSwapSecurity extends SwapSecurity {

  /**
   * 
   */
  protected static final String FORWARDSTARTDATE_KEY = "forwardStartDate";

  private final ZonedDateTime _forwardStartDate;

  /**
   * @param tradeDate the trade date
   * @param effectiveDate the 'effective' or 'value' date
   * @param maturityDate the 'maturity' or 'termination' date
   * @param counterparty the counterparty
   * @param payLeg the pay leg
   * @param receiveLeg the receive leg
   * @param forwardStartDate the start date of the forward swap
   */
  public ForwardSwapSecurity(final ZonedDateTime tradeDate, final ZonedDateTime effectiveDate,
      final ZonedDateTime maturityDate, final String counterparty, final SwapLeg payLeg, final SwapLeg receiveLeg,
      final ZonedDateTime forwardStartDate) {
    super(tradeDate, effectiveDate, maturityDate, counterparty, payLeg, receiveLeg);

    Validate.notNull(forwardStartDate);

    if (forwardStartDate.isBefore(effectiveDate)) {
      throw new IllegalArgumentException("Forward start date cannot be before effective date");
    }
    if (forwardStartDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Forward start date cannot be after the maturity date");
    }
    _forwardStartDate = forwardStartDate;
  }

  public ZonedDateTime getForwardStartDate() {
    return _forwardStartDate;
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    context.objectToFudgeMsg(message, FORWARDSTARTDATE_KEY, null, getForwardStartDate());
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

  public static ForwardSwapSecurity fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final ForwardSwapSecurity security = new ForwardSwapSecurity(ZonedDateTime.of(context.fieldValueToObject(
        DateTimeProvider.class, message.getByName(TRADEDATE_KEY)), TimeZone.UTC), ZonedDateTime.of(context
        .fieldValueToObject(DateTimeProvider.class, message.getByName(EFFECTIVEDATE_KEY)), TimeZone.UTC), ZonedDateTime
        .of(context.fieldValueToObject(DateTimeProvider.class, message.getByName(MATURITYDATE_KEY)), TimeZone.UTC),
        message.getString(COUNTERPARTY_KEY), context.fieldValueToObject(SwapLeg.class, message.getByName(PAYLEG_KEY)),
        context.fieldValueToObject(SwapLeg.class, message.getByName(RECEIVELEG_KEY)), context.fieldValueToObject(
            ZonedDateTime.class, message.getByName(FORWARDSTARTDATE_KEY)));
    security.fromFudgeMsgImpl(context, message);
    return security;
  }

}
