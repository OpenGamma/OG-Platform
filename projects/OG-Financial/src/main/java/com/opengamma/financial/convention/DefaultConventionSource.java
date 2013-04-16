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
public class DefaultConventionSource implements ConventionSource {

  /**
   * 
   */
  public DefaultConventionSource() {
  }

  @Override
  public Convention getConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public CMSLegConvention getCMSLegConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public CompoundingIborLegConvention getCompoundingIborLegConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public DepositConvention getDepositConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public EquityConvention getEquityConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public InterestRateFutureConvention getInterestRateFutureConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public FXForwardAndSwapConvention getFXForwardAndSwapConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public FXSpotConvention getFXSpotConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public IborIndexConvention getIborIndexConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public OISLegConvention getOISLegConvention(final ExternalId identifier) {
    return null;
  }

  @Override
  public OvernightIndexConvention getOvernightIndexConvention(final ExternalId identifier) {
    return null;
  }

}
