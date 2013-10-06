/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * Visitor for {@link Convention}s.
 * @param <T> The return type.
 */
public interface ConventionVisitor<T> {

  /**
   * Visits {@link CMSLegConvention}s.
   * @param convention The convention, not null
   * @return The return value
   */
  T visitCMSLegConvention(CMSLegConvention convention);

  /**
   * Visits {@link CompoundingIborLegConvention}s.
   * @param convention The convention, not null
   * @return The return value
   */
  T visitCompoundingIborLegConvention(CompoundingIborLegConvention convention);

  /**
   * Visits {@link DepositConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitDepositConvention(DepositConvention convention);

  /**
   * Visits {@link EquityConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitEquityConvention(EquityConvention convention);

  /**
   * Visits {@link DeliverablePriceQuotedSwapFutureConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitDeliverablePriceQuotedSwapFutureConvention(DeliverablePriceQuotedSwapFutureConvention convention);

  /**
   * Visits {@link FederalFundsFutureConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitFederalFundsFutureConvention(FederalFundsFutureConvention convention);

  /**
   * Visits {@link FXForwardAndSwapConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitFXForwardAndSwapConvention(FXForwardAndSwapConvention convention);

  /**
   * Visits {@link FXSpotConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitFXSpotConvention(FXSpotConvention convention);

  /**
   * Visits {@link IborIndexConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitIborIndexConvention(IborIndexConvention convention);

  /**
   * Visits {@link IMMSwapConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitIMMSwapConvention(IMMSwapConvention convention);

  /**
   * Visits {@link InflationLegConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitInflationLegConvention(InflationLegConvention convention);

  /**
   * Visits {@link InterestRateFutureConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitInterestRateFutureConvention(InterestRateFutureConvention convention);

  /**
   * Visits {@link OISLegConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitOISLegConvention(OISLegConvention convention);

  /**
   * Visits {@link OvernightIndexConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitOvernightIndexConvention(OvernightIndexConvention convention);

  /**
   * Visits {@link PriceIndexConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitPriceIndexConvention(PriceIndexConvention convention);

  /**
   * Visits {@link SwapConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitSwapConvention(SwapConvention convention);

  /**
   * Visits {@link SwapFixedLegConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitSwapFixedLegConvention(SwapFixedLegConvention convention);

  /**
   * Visits {@link SwapIndexConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitSwapIndexConvention(SwapIndexConvention convention);

  /**
   * Visits {@link VanillaIborLegConvention}
   * @param convention The convention, not null
   * @return The return value
   */
  T visitVanillaIborLegConvention(VanillaIborLegConvention convention);
}
