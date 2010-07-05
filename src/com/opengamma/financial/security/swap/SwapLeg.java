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

/**
 * Represents one leg of a swap.
 */
public class SwapLeg {

  /**
   * 
   */
  protected static final String DAYCOUNT_KEY = "dayCount";
  /**
   * 
   */
  protected static final String FREQUENCY_KEY = "frequency";
  /**
   * 
   */
  protected static final String REGION_KEY = "region";
  /**
   * 
   */
  protected static final String BUSINESSDAYCONVENTION_KEY = "businessDayConvention";
  /**
   * 
   */
  protected static final String NOTIONAL_KEY = "notional";

  private final DayCount _daycount;
  private final Frequency _frequency;
  private final Region _region;
  private final BusinessDayConvention _businessDayConvention;
  private final Notional _notional;

  /**
   * @param daycount day count convention
   * @param frequency the frequency for payments
   * @param region the region of issue
   * @param businessDayConvention the business day convention
   * @param notional the notional value of this leg
   */
  public SwapLeg(final DayCount daycount, final Frequency frequency, final Region region,
      final BusinessDayConvention businessDayConvention, final Notional notional) {
    super();
    _daycount = daycount;
    _frequency = frequency;
    _region = region;
    _businessDayConvention = businessDayConvention;
    _notional = notional;
  }

  /**
   * @return the daycount
   */
  public DayCount getDaycount() {
    return _daycount;
  }

  /**
   * @return the frequency
   */
  public Frequency getFrequency() {
    return _frequency;
  }

  /**
   * @return the holiday
   */
  public Region getRegion() {
    return _region;
  }

  /**
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * @return the notional
   */
  public Notional getNotional() {
    return _notional;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_businessDayConvention == null) ? 0 : _businessDayConvention.hashCode());
    result = prime * result + ((_daycount == null) ? 0 : _daycount.hashCode());
    result = prime * result + ((_frequency == null) ? 0 : _frequency.hashCode());
    result = prime * result + ((_notional == null) ? 0 : _notional.hashCode());
    result = prime * result + ((_region == null) ? 0 : _region.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SwapLeg other = (SwapLeg) obj;
    if (_businessDayConvention == null) {
      if (other._businessDayConvention != null) {
        return false;
      }
    } else if (!_businessDayConvention.equals(other._businessDayConvention)) {
      return false;
    }
    if (_daycount == null) {
      if (other._daycount != null) {
        return false;
      }
    } else if (!_daycount.equals(other._daycount)) {
      return false;
    }
    if (_frequency == null) {
      if (other._frequency != null) {
        return false;
      }
    } else if (!_frequency.equals(other._frequency)) {
      return false;
    }
    if (_notional == null) {
      if (other._notional != null) {
        return false;
      }
    } else if (!_notional.equals(other._notional)) {
      return false;
    }
    if (_region == null) {
      if (other._region != null) {
        return false;
      }
    } else if (!_region.equals(other._region)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SwapLeg[Day count=" + _daycount.getConventionName() + "; frequency=" + _frequency.getConventionName()
        + "; region=" + _region.getName() + "; business day convention=" + _businessDayConvention.getConventionName()
        + "; notional=" + _notional + "]";
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    context.objectToFudgeMsg(message, DAYCOUNT_KEY, null, getDaycount());
    context.objectToFudgeMsg(message, FREQUENCY_KEY, null, getFrequency());
    context.objectToFudgeMsg(message, REGION_KEY, null, getRegion());
    context.objectToFudgeMsg(message, BUSINESSDAYCONVENTION_KEY, null, getBusinessDayConvention());
    context.objectToFudgeMsg(message, NOTIONAL_KEY, null, getNotional());
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  protected void fromFudgeMsgImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    // Everything set by constructor
  }

  public static SwapLeg fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final SwapLeg leg = new SwapLeg(context.fieldValueToObject(DayCount.class, message.getByName(DAYCOUNT_KEY)),
        context.fieldValueToObject(Frequency.class, message.getByName(FREQUENCY_KEY)), context.fieldValueToObject(
            Region.class, message.getByName(REGION_KEY)), context.fieldValueToObject(BusinessDayConvention.class,
            message.getByName(BUSINESSDAYCONVENTION_KEY)), context.fieldValueToObject(Notional.class, message
            .getByName(NOTIONAL_KEY)));
    leg.fromFudgeMsgImpl(context, message);
    return leg;
  }
}
