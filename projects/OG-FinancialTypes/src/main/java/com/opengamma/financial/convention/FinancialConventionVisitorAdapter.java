/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * Default implementation of the visitor that throws {@link UnsupportedOperationException}
 * for all convention types.
 *
 * @param <T> the return type of the visitor query
 */
public class FinancialConventionVisitorAdapter<T> implements FinancialConventionVisitor<T> {

  @Override
  public T visitBondConvention(final BondConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitCMSLegConvention(final CMSLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitDepositConvention(final DepositConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitEquityConvention(final EquityConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitDeliverablePriceQuotedSwapFutureConvention(final DeliverablePriceQuotedSwapFutureConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFederalFundsFutureConvention(final FederalFundsFutureConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFixedInterestRateSwapLegConvention(final FixedInterestRateSwapLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFloatingInterestRateSwapLegConvention(final FloatingInterestRateSwapLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFXForwardAndSwapConvention(final FXForwardAndSwapConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFXSpotConvention(final FXSpotConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitIborIndexConvention(final IborIndexConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitIMMFRAConvention(final RollDateFRAConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitIMMSwapConvention(final RollDateSwapConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitInflationLegConvention(final InflationLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitInterestRateFutureConvention(final InterestRateFutureConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitOISLegConvention(final OISLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitONCompoundedLegRollDateConvention(final ONCompoundedLegRollDateConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitOvernightIndexConvention(final OvernightIndexConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitPriceIndexConvention(final PriceIndexConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitSwapConvention(final SwapConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitSwapIndexConvention(final SwapIndexConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
    return getErrorMessage(convention);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an exception.
   * 
   * @param convention  the convention
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  private T getErrorMessage(final FinancialConvention convention) {
    throw new UnsupportedOperationException("This visitor does not support conventions of type " + convention.getClass());
  }

}
