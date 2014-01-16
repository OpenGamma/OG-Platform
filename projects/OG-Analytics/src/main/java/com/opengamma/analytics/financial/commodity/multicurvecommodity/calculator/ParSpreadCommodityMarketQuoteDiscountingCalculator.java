/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.calculator;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.AgricultureFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityCashSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.ForwardCommodityPhysicalSettle;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.MetalFutureTransaction;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.CommodityFutureSecurityForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.ForwardCommodityCashSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.provider.ForwardCommodityPhysicalSettleSecurityForwardMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.commodity.CommodityProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the spread to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "market quote" will depend of each instrument.
 */
public final class ParSpreadCommodityMarketQuoteDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<CommodityProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadCommodityMarketQuoteDiscountingCalculator INSTANCE = new ParSpreadCommodityMarketQuoteDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadCommodityMarketQuoteDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadCommodityMarketQuoteDiscountingCalculator() {
  }

  private static final PresentValueCommodityDiscountingCalculator PVDC = PresentValueCommodityDiscountingCalculator.getInstance();
  private static final PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueCommodityMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final CommodityFutureSecurityForwardMethod METHOD_COMMODITY_FUTURE = CommodityFutureSecurityForwardMethod.getInstance();
  private static final ForwardCommodityCashSettleSecurityForwardMethod METHOD_FWD_COMMODITY_CASH_COUPON = ForwardCommodityCashSettleSecurityForwardMethod.getInstance();
  private static final ForwardCommodityPhysicalSettleSecurityForwardMethod METHOD_FWD_COMMODITY_PHYSICAL_COUPON = ForwardCommodityPhysicalSettleSecurityForwardMethod.getInstance();

  //-----     Payment/Coupon     ------

  @Override
  public Double visitForwardCommodityCashSettle(final ForwardCommodityCashSettle forward, final CommodityProviderInterface multicurve) {
    return METHOD_FWD_COMMODITY_CASH_COUPON.parSpread(forward, multicurve);
  }

  @Override
  public Double visitForwardCommodityPhysicalSettle(final ForwardCommodityPhysicalSettle forward, final CommodityProviderInterface multicurve) {
    return METHOD_FWD_COMMODITY_PHYSICAL_COUPON.parSpread(forward, multicurve);
  }

  // -----     Swap     ------

  /**
   * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
   * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
   * of the first leg (as computed by the PresentValueMarketQuoteSensitivityDiscountingCalculator).
   * @param swap The swap.
   * @param multicurves The multi-curves provider.
   * @return The par spread.
   */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final CommodityProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "Market");
    ArgumentChecker.notNull(swap, "Swap");

    return -multicurves.getFxRates().convert(swap.accept(PVDC, multicurves), swap.getFirstLeg().getCurrency()).getAmount() / swap.getFirstLeg().accept(PVMQSC, multicurves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final CommodityProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  // -----     Futures     ------

  @Override
  public Double visitAgricultureFutureTransaction(final AgricultureFutureTransaction future, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.price(future.getUnderlying(), multicurves) - future.getReferencePrice();
  }

  @Override
  public Double visitEnergyFutureTransaction(final EnergyFutureTransaction future, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.price(future.getUnderlying(), multicurves) - future.getReferencePrice();
  }

  @Override
  public Double visitMetalFutureTransaction(final MetalFutureTransaction future, final CommodityProviderInterface multicurves) {
    return METHOD_COMMODITY_FUTURE.price(future.getUnderlying(), multicurves) - future.getReferencePrice();
  }

}
