/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * An indication of when something expires.
 */
public class Expiry implements InstantProvider {

  /**
   * 
   */
  protected static final String EXPIRYINSTANT_KEY = "expiryInstant";
  /**
   * 
   */
  protected static final String EXPIRYZONE_KEY = "expiryZone";
  /**
   * 
   */
  protected static final String ACCURACY_KEY = "accuracy";

  /**
   * The expiry date-time.
   */
  private final ZonedDateTime _expiry;
  /**
   * The accuracy of the expiry.
   */
  private final ExpiryAccuracy _accuracy;

  /**
   * Creates an expiry with no specific accuracy.
   * @param expiry  the expiry date-time
   */
  public Expiry(final ZonedDateTime expiry) {
    _expiry = expiry;
    _accuracy = null;
  }

  /**
   * Creates an expiry with an accuracy.
   * @param expiry  the expiry date-time
   * @param accuracy  the accuracy
   */
  public Expiry(final ZonedDateTime expiry, final ExpiryAccuracy accuracy) {
    _expiry = expiry;
    _accuracy = accuracy;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the expiry date-time.
   * @return the date-time
   */
  // we probably don't need this.
  public ZonedDateTime getExpiry() {
    return _expiry;
  }

  /**
   * Gets the accuracy of the expiry.
   * @return the accuracy
   */
  public ExpiryAccuracy getAccuracy() {
    return _accuracy;
  }

  @Override
  public Instant toInstant() {
    return _expiry.toInstant();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Expiry) {
      final Expiry other = (Expiry) obj;
      return ObjectUtils.equals(other.getAccuracy(), getAccuracy()) && other.getExpiry().equals(getExpiry());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (_accuracy != null ? _accuracy.hashCode() : 0) ^ _expiry.hashCode();
  }

  @Override
  public String toString() {
    if (_accuracy != null) {
      return "Expiry[" + _expiry + " accuracy " + _accuracy + "]";
    } else {
      return "Expiry[" + _expiry + "]";
    }
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    context.objectToFudgeMsg(message, EXPIRYINSTANT_KEY, null, getExpiry().toInstant());
    context.objectToFudgeMsg(message, EXPIRYZONE_KEY, null, getExpiry().getZone());
    context.objectToFudgeMsg(message, ACCURACY_KEY, null, getAccuracy());
    return message;
  }

  public static Expiry fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final InstantProvider expiryInstant = context.fieldValueToObject(InstantProvider.class, message
        .getByName(EXPIRYINSTANT_KEY));
    final TimeZone expiryZone = context.fieldValueToObject(TimeZone.class, message.getByName(EXPIRYZONE_KEY));
    final ExpiryAccuracy accuracy = context.fieldValueToObject(ExpiryAccuracy.class, message.getByName(ACCURACY_KEY));
    return new Expiry(ZonedDateTime.ofInstant(expiryInstant, expiryZone), accuracy);
  }
}
