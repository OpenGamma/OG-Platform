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
 * @param <T> The return type
 */
public class ConventionVisitorAdapter<T> implements ConventionVisitor<T> {

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
  public T visitIMMFRAConvention(final IMMFRAConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitIMMSwapConvention(final IMMSwapConvention convention) {
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
  public T visitSwapIndexConvention(final SwapIndexConvention convention) {
    return getErrorMessage(convention);
  }

  @Override
  public T visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
    return getErrorMessage(convention);
  }

  /**
   * @param convention The convention
   * @return UnsupportedOperationException
   * @throws UnsupportedOperationException
   */
  private T getErrorMessage(final Convention convention) {
    throw new UnsupportedOperationException("This visitor does not support conventions of type " + convention.getClass());
  }
}
