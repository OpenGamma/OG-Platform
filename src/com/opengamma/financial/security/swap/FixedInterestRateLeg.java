/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.Region;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a fixed interest rate leg of a swap.
 */
public class FixedInterestRateLeg extends InterestRateLeg {

  /**
   * 
   */
  protected static final String FIXEDRATE_KEY = "fixedRate";

  private final double _fixedRate; // TODO: Elaine 28-May-2010 -- change rate from double to InterestRateType.

  /**
   * @param dayCount The daycount
   * @param frequency The frequency
   * @param region The region
   * @param businessDayConvention The business day convention
   * @param notional The notional
   * @param fixedRate the fixed interest rate as a decimal (e,g, 5% = 0.05)
   */
  public FixedInterestRateLeg(final DayCount dayCount, final Frequency frequency, final Region region, final BusinessDayConvention businessDayConvention, final InterestRateNotional notional,
      final double fixedRate) {
    super(dayCount, frequency, region, businessDayConvention, notional);
    ArgumentChecker.notNegative(fixedRate, "rate");
    _fixedRate = fixedRate;
  }

  /**
   * @return the fixed interest rate as a decimal (e.g. 5% = 0.05)
   */
  public double getRate() {
    return _fixedRate;
  }
  
  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    super.toFudgeMsg(context, message);
    message.add(FIXEDRATE_KEY, getRate());
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

  public static FixedInterestRateLeg fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    final FixedInterestRateLeg leg = new FixedInterestRateLeg(context.fieldValueToObject(DayCount.class, message
        .getByName(DAYCOUNT_KEY)), context.fieldValueToObject(Frequency.class, message.getByName(FREQUENCY_KEY)),
        context.fieldValueToObject(Region.class, message.getByName(REGION_KEY)), context.fieldValueToObject(
            BusinessDayConvention.class, message.getByName(BUSINESSDAYCONVENTION_KEY)), context.fieldValueToObject(
            InterestRateNotional.class, message.getByName(NOTIONAL_KEY)), message.getDouble(FIXEDRATE_KEY));
    leg.fromFudgeMsgImpl(context, message);
    return leg;
  }

}
