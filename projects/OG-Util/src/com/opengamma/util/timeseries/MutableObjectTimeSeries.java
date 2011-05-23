/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * 
 * @param <DATE_TYPE> Type of the dates
 * @param <T> Type of the data
 */
public interface MutableObjectTimeSeries<DATE_TYPE, T> extends MutableTimeSeries<DATE_TYPE, T>, ObjectTimeSeries<DATE_TYPE, T> {

}
