/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 *
 */
public interface ConventionVisitor<T> {

  T visitCMSLegConvention(CMSLegConvention convention);

  T visitCompoundingIborLegConvention(CompoundingIborLegConvention convention);

  T visitDepositConvention(DepositConvention convention);

  T visitEquityConvention(EquityConvention convention);

  T visitDeliverablePriceQuotedSwapFutureConvention(DeliverablePriceQuotedSwapFutureConvention convention);

  T visitFederalFundsFutureConvention(FederalFundsFutureConvention convention);

  T visitFXForwardAndSwapConvention(FXForwardAndSwapConvention convention);

  T visitFXSpotConvention(FXSpotConvention convention);

  T visitIborIndexConvention(IborIndexConvention convention);

  T visitIMMSwapConvention(IMMSwapConvention convention);

  T visitInflationLegConvention(InflationLegConvention convention);

  T visitInterestRateFutureConvention(InterestRateFutureConvention convention);

  T visitOISLegConvention(OISLegConvention convention);

  T visitOvernightIndexConvention(OvernightIndexConvention convention);

  T visitPriceIndexConvention(PriceIndexConvention convention);

  T visitSwapConvention(SwapConvention convention);

  T visitSwapFixedLegConvention(SwapFixedLegConvention convention);

  T visitSwapIndexConvention(SwapIndexConvention convention);

  T visitVanillaIborLegConvention(VanillaIborLegConvention convention);
}
