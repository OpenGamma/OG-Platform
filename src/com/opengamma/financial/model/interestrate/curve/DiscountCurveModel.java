/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public interface DiscountCurveModel<T> {

  public YieldAndDiscountCurve getCurve(Set<T> data, ZonedDateTime date);
}
