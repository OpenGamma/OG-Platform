/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.ExternalId;

/**
 * 
 */
public interface ConventionSource {

  Convention getConvention(ExternalId identifier);

  CMSLegConvention getCMSLegConvention(ExternalId identifier);

  CompoundingIborLegConvention getCompoundingIborLegConvention(ExternalId identifier);

  DepositConvention getDepositConvention(ExternalId identifier);

  EquityConvention getEquityConvention(ExternalId identifier);

  InterestRateFutureConvention getInterestRateFutureConvention(ExternalId identifier);

  FXForwardAndSwapConvention getFXForwardAndSwapConvention(ExternalId identifier);

  FXSpotConvention getFXSpotConvention(ExternalId identifier);

  IborIndexConvention getIborIndexConvention(ExternalId identifier);

  OISLegConvention getOISLegConvention(ExternalId identifier);

  OvernightIndexConvention getOvernightIndexConvention(ExternalId identifier);


}
