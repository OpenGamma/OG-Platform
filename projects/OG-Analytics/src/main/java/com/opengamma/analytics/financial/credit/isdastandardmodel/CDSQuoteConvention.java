/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

/**
 * CDS can be quoted in one of three ways. The pre-April 2009 Par Spreads; and the post April 2009 'Big Bang' points up-front
 * (PUF) and Quoted Spreads
 * @see {@link ParSpread}, {@link QuotedSpread} and {@link PointsUpFront}
 */
public interface CDSQuoteConvention {

  double getCoupon();

}
