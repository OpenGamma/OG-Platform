/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.math.stat.descriptive.moment.Mean;

import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.YieldAverageBondFuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Pricing method for bond futures with settlement based on an average yield (used in particular for SFE-AUD bond futures).
 */
public final class YieldAverageBondFuturesSecurityDiscountingMethod implements FuturesSecurityMethod {

  /**
   * Creates the method unique instance.
   */
  private static final YieldAverageBondFuturesSecurityDiscountingMethod INSTANCE = new YieldAverageBondFuturesSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static YieldAverageBondFuturesSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private YieldAverageBondFuturesSecurityDiscountingMethod() {
  }
  
  /** Method used to compute bond yield **/
  private static final BondSecurityDiscountingMethod METHOD_BND = BondSecurityDiscountingMethod.getInstance();
  /** Function to compute average of arrays **/
  private static final Mean MEAN_FUNCTION = new Mean();
  
  @Override
  public double marginIndex(FuturesSecurity futures, double quotedPrice) {
    ArgumentChecker.isTrue(futures instanceof YieldAverageBondFuturesSecurity, "Futures should be of the type YieldAverageBondFuturesSecurity");
    return marginIndex((YieldAverageBondFuturesSecurity) futures, quotedPrice);
  }

  /**
   * For bond futures with settlement based on an average yield, the margin index is obtained as:
   * yield = 1 - quotedPrice;
   * marginIndex = dirtyPrice * notional.
   * The dirty price is the one of a bond with tenor the futures synthetic tenor, coupon equal to the synthetic coupon and yield given above.
   * @param futures The futures security.
   * @param quotedPrice The futures quoted price.
   * @return The figure used in margining.
   */
  public double marginIndex(YieldAverageBondFuturesSecurity futures, double quotedPrice) {
    final double yield = 1.0d - quotedPrice;
    final double dirtyPrice = dirtyPriceFromYield(yield, futures.getCouponRate(), futures.getTenor(), futures.getNumberCouponPerYear());
    return dirtyPrice * futures.getNotional();
  }

  @Override
  public double price(FuturesSecurity futures, ParameterProviderInterface multicurve) {
    ArgumentChecker.isTrue(futures instanceof YieldAverageBondFuturesSecurity, "Futures should be of the type YieldAverageBondFuturesSecurity");
    ArgumentChecker.isTrue(multicurve instanceof IssuerProviderInterface, "provider should be of type IssuerProviderInterface");
    return price((YieldAverageBondFuturesSecurity) futures, (IssuerProviderInterface) multicurve);
  }
  
  /**
   * Computes the price of bond futures by yield average. The yield of each underlying bond with settlement at delivery date is computed
   * and averaged. The price is 1.0 - average yield.
   * @param futures The futures security.
   * @param multicurve The multi-curve and issuer provider.
   * @return The price.
   */
  public double price(YieldAverageBondFuturesSecurity futures, IssuerProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "futures");
    ArgumentChecker.notNull(futures, "multi-curve provider");
    final int nbBonds = futures.getDeliveryBasketAtDeliveryDate().length;
    // Yield at theoretical delivery for each bond
    final double[] yield = new double[nbBonds];
    for (int loopbond = 0; loopbond < nbBonds; loopbond++) {
      yield[loopbond] = METHOD_BND.yieldFromCurves(futures.getDeliveryBasketAtDeliveryDate()[loopbond], multicurve);
    }
    final double yieldAverage = MEAN_FUNCTION.evaluate(yield); // Average yield
    final double price = 1.0d - yieldAverage;
    return price;
  }
  
  /**
   * The dirty price from the standard yield.
   * @param yield The yield
   * @param coupon The coupon
   * @param tenor The tenor (in year)
   * @param couponPerYear Number of coupon per year.
   * @return The price.
   */
  private double dirtyPriceFromYield(final double yield, final double coupon, final int tenor, final int couponPerYear) {
    final double v = 1.0d + yield / couponPerYear;
    final int n = tenor * couponPerYear;
    final double vn = Math.pow(v, -n);
    return coupon / yield * (1 - vn) + vn;
  }
  
}
