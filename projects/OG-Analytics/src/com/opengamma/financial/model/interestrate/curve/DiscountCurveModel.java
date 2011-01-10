/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

/**
 * @param <T> Type of the data 
 */
public interface DiscountCurveModel<T> {

  YieldAndDiscountCurve getCurve(Set<T> data, ZonedDateTime date);
}
