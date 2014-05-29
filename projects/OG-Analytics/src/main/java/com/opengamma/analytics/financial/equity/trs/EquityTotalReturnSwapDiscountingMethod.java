/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with pricing methods related to equity TRS valued by discounting.
 * The equity TRS has a termination option at each valuation date. Only the cash flows up to the next unfixed payment are taken into account. 
 * The asset and funding leg are suppose to have the same fixing and payment dates. Currently only Ibor coupons are supported.
 * Reference: Equity's total return swap, OpenGamma documentation 22, version 1.0, May 2014.
 */
public final class EquityTotalReturnSwapDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final EquityTotalReturnSwapDiscountingMethod INSTANCE = new EquityTotalReturnSwapDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static EquityTotalReturnSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private EquityTotalReturnSwapDiscountingMethod() {
  }

  /** The present value and present value curve sensitivity calculators used for bonds calculation */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * Computes the present value of a equity TRS.
   * The present value of the equity leg is converted into the currency of the TRS notional currency.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    ArgumentChecker.isTrue(trs.getDividendPercentage() == 1.0, "equity TRS dividend ration should be 1.0");
    MultipleCurrencyAmount equityPv = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    // First coupon ON or Ibor: Payment up to the fist payment taken into account OR Only one payment left
    if ((trs.getFundingLeg().getNumberOfPayments() == 1) ||
        (trs.getFundingLeg().getNthPayment(0) instanceof CouponIbor) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponIborSpread)
        || (trs.getFundingLeg().getNthPayment(0) instanceof CouponON) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency())).plus(fundingLegPV);
    }
    // Second coupons fixed or Ibor: Payment up to the end of the second period taken into account.
    if ((trs.getFundingLeg().getNthPayment(1) instanceof CouponFixed)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponON)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(1).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPv0 = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      final MultipleCurrencyAmount fundingLegPv1 = trs.getFundingLeg().getNthPayment(1).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency())).plus(fundingLegPv0).plus(fundingLegPv1);
    }
    // First coupon is fixed (and the second one is not fixed or ON): in an already fixed Libor coupon: Payment up to the fist payment taken into account
    if (trs.getFundingLeg().getNthPayment(0) instanceof CouponFixed) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency())).plus(fundingLegPV);
    }
    // Other cases not covered by the pricing method.
    throw new NotImplementedException("Pricing of equity TRS not implemented for those types of coupons");
  }

  /**
   * Computes the present value of the asset leg of a equity TRS. 
   * The present value of the equity leg is converted into the currency of the TRS notional currency.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueAssetLeg(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    MultipleCurrencyAmount equityPv = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    // First coupon ON or Ibor: Payment up to the fist payment taken into account OR Only one payment left
    if ((trs.getFundingLeg().getNumberOfPayments() == 1) ||
        (trs.getFundingLeg().getNthPayment(0) instanceof CouponIbor) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponIborSpread)
        || (trs.getFundingLeg().getNthPayment(0) instanceof CouponON) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency()));
    }
    // Second coupons fixed or Ibor: Payment up to the end of the second period taken into account.
    if ((trs.getFundingLeg().getNthPayment(1) instanceof CouponFixed)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponON)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(1).getPaymentTime()));
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency()));
    }
    // First coupon is fixed (and the second one is not fixed or ON): in an already fixed Libor coupon: Payment up to the fist payment taken into account
    if (trs.getFundingLeg().getNthPayment(0) instanceof CouponFixed) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      return previousFixingPv.plus(equityMulticurves.getCurves().getFxRates().convert(equityPv, trs.getNotionalCurrency()));
    }
    // Other cases not covered by the pricing method.
    throw new NotImplementedException("Pricing of equity TRS not implemented for those types of coupons");
  }

  /**
   * Computes the present value of the funding leg of a equity TRS.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFundingLeg(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    // First coupon ON or Ibor: Payment up to the fist payment taken into account OR Only one payment left
    if ((trs.getFundingLeg().getNumberOfPayments() == 1) ||
        (trs.getFundingLeg().getNthPayment(0) instanceof CouponIbor) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponIborSpread)
        || (trs.getFundingLeg().getNthPayment(0) instanceof CouponON) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponONSpread)) {
      return trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
    }
    // Second coupons fixed or Ibor: Payment up to the end of the second period taken into account.
    if ((trs.getFundingLeg().getNthPayment(1) instanceof CouponFixed)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponON)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponONSpread)) {
      final MultipleCurrencyAmount fundingLegPv0 = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      final MultipleCurrencyAmount fundingLegPv1 = trs.getFundingLeg().getNthPayment(1).accept(PVDC, equityMulticurves.getCurves());
      return fundingLegPv0.plus(fundingLegPv1);
    }
    // First coupon is fixed (and the second one is not fixed or ON): in an already fixed Libor coupon: Payment up to the fist payment taken into account
    if (trs.getFundingLeg().getNthPayment(0) instanceof CouponFixed) {
      return trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
    }
    // Other cases not covered by the pricing method.
    throw new NotImplementedException("Pricing of equity TRS not implemented for those types of coupons");
  }

  /**
   * Computes the currency exposure of a equity TRS.
   * The currency of the equity part is in the equity currency (not converted to the notional currency like for PV); 
   * the currency of the already fixed equity part is in the notional currency; the funding leg part is in the funding leg currency.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount currencyExposure(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    MultipleCurrencyAmount equityPv = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    // First coupon ON or Ibor: Payment up to the fist payment taken into account OR Only one payment left
    if ((trs.getFundingLeg().getNumberOfPayments() == 1) ||
        (trs.getFundingLeg().getNthPayment(0) instanceof CouponIbor) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponIborSpread)
        || (trs.getFundingLeg().getNthPayment(0) instanceof CouponON) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityPv).plus(fundingLegPV);
    }
    // Second coupons fixed or Ibor: Payment up to the end of the second period taken into account.
    if ((trs.getFundingLeg().getNthPayment(1) instanceof CouponFixed)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponON)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponONSpread)) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(1).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPv0 = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      final MultipleCurrencyAmount fundingLegPv1 = trs.getFundingLeg().getNthPayment(1).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityPv).plus(fundingLegPv0).plus(fundingLegPv1);
    }
    // First coupon is fixed (and the second one is not fixed or ON): in an already fixed Libor coupon: Payment up to the fist payment taken into account
    if (trs.getFundingLeg().getNthPayment(0) instanceof CouponFixed) {
      MultipleCurrencyAmount previousFixingPv = MultipleCurrencyAmount.of(trs.getNotionalCurrency(),
          -trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), trs.getFundingLeg().getNthPayment(0).getPaymentTime()));
      final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().getNthPayment(0).accept(PVDC, equityMulticurves.getCurves());
      return previousFixingPv.plus(equityPv).plus(fundingLegPV);
    }
    // Other cases not covered by the pricing method.
    throw new NotImplementedException("Pricing of equity TRS not implemented for those types of coupons");
  }

  /**
   * Computes the exposure to the asset (equity) underlying the TRS. The exposure is reported in the equity currency.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount assetExposure(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    ArgumentChecker.isTrue(trs.getDividendPercentage() == 1.0, "equity TRS dividend ration should be 1.0");
    MultipleCurrencyAmount equityPV = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    return equityPV;
  }

  /**
   * Computes the present value curve sensitivity of a equity TRS. 
   * The sensitivity to the (issuer) curves used in the bond valuation and the sensitivity to the curves used in the funding leg valuation are computed.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    MulticurveProviderInterface multicurve = equityMulticurves.getCurves();
    Currency notionalCurrency = trs.getNotionalCurrency();
    // First coupon ON or Ibor: Payment up to the fist payment taken into account OR Only one payment left
    if ((trs.getFundingLeg().getNumberOfPayments() == 1) ||
        (trs.getFundingLeg().getNthPayment(0) instanceof CouponIbor) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponIborSpread)
        || (trs.getFundingLeg().getNthPayment(0) instanceof CouponON) || (trs.getFundingLeg().getNthPayment(0) instanceof CouponONSpread)) {
      final double time = trs.getFundingLeg().getNthPayment(0).getPaymentTime();
      final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
      final DoublesPair s = DoublesPair.of(time, time * trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), time));
      final List<DoublesPair> list = new ArrayList<>();
      list.add(s);
      mapDsc.put(multicurve.getName(notionalCurrency), list);
      MultipleCurrencyMulticurveSensitivity equityLegPvcs = new MultipleCurrencyMulticurveSensitivity();
      equityLegPvcs = equityLegPvcs.plus(notionalCurrency, MulticurveSensitivity.ofYieldDiscounting(mapDsc));
      MultipleCurrencyMulticurveSensitivity fundingLegPvcs = trs.getFundingLeg().getNthPayment(0).accept(PVCSDC, equityMulticurves.getCurves());
      return equityLegPvcs.plus(fundingLegPvcs);
    }
    // Second coupons fixed or Ibor: Payment up to the end of the second period taken into account.
    if ((trs.getFundingLeg().getNthPayment(1) instanceof CouponFixed)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponON)
        || (trs.getFundingLeg().getNthPayment(1) instanceof CouponONSpread)) {
      final double time = trs.getFundingLeg().getNthPayment(1).getPaymentTime();
      final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
      final DoublesPair s = DoublesPair.of(time, time * trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), time));
      final List<DoublesPair> list = new ArrayList<>();
      list.add(s);
      mapDsc.put(multicurve.getName(notionalCurrency), list);
      MultipleCurrencyMulticurveSensitivity equityLegPvcs = new MultipleCurrencyMulticurveSensitivity();
      equityLegPvcs = equityLegPvcs.plus(notionalCurrency, MulticurveSensitivity.ofYieldDiscounting(mapDsc));
      MultipleCurrencyMulticurveSensitivity fundingLegPvcs0 = trs.getFundingLeg().getNthPayment(0).accept(PVCSDC, equityMulticurves.getCurves());
      MultipleCurrencyMulticurveSensitivity fundingLegPvcs1 = trs.getFundingLeg().getNthPayment(1).accept(PVCSDC, equityMulticurves.getCurves());
      return equityLegPvcs.plus(fundingLegPvcs0).plus(fundingLegPvcs1);
    }
    // First coupon is fixed (and the second one is not fixed or ON): in an already fixed Libor coupon: Payment up to the fist payment taken into account
    if (trs.getFundingLeg().getNthPayment(0) instanceof CouponFixed) {
      final double time = trs.getFundingLeg().getNthPayment(0).getPaymentTime();
      final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
      final DoublesPair s = DoublesPair.of(time, time * trs.getNotionalAmount() * equityMulticurves.getCurves().getDiscountFactor(trs.getNotionalCurrency(), time));
      final List<DoublesPair> list = new ArrayList<>();
      list.add(s);
      mapDsc.put(multicurve.getName(notionalCurrency), list);
      MultipleCurrencyMulticurveSensitivity equityLegPvcs = new MultipleCurrencyMulticurveSensitivity();
      equityLegPvcs = equityLegPvcs.plus(notionalCurrency, MulticurveSensitivity.ofYieldDiscounting(mapDsc));
      MultipleCurrencyMulticurveSensitivity fundingLegPvcs = trs.getFundingLeg().getNthPayment(0).accept(PVCSDC, equityMulticurves.getCurves());
      return equityLegPvcs.plus(fundingLegPvcs);
    }
    // Other cases not covered by the pricing method.
    throw new NotImplementedException("Pricing of equity TRS not implemented for those types of coupons");
  }

}
