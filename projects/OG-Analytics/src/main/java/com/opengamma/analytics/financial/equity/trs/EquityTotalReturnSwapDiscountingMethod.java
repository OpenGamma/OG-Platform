/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class with pricing methods related to equity TRS valued by discounting.
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
   * The present value of the equity leg is equal to the equity price multiply the the number of shares in the currency of the shares.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PVDC, equityMulticurves.getCurves());
    final MultipleCurrencyAmount equityPV = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    return equityPV.plus(fundingLegPV);
  }

  /**
   * Computes the present value of the asset leg of a equity TRS. 
   * The present value is equal to the equity price multiply the the number of shares in the currency of the shares.
   * @param trs The equity total return swap.
   * @param equityMulticurves The multi-curves provider with equity price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueAssetLeg(final EquityTotalReturnSwap trs, final EquityTrsDataBundle equityMulticurves) {
    ArgumentChecker.notNull(trs, "equity TRS");
    ArgumentChecker.notNull(equityMulticurves, "multi-curve provider with equity price");
    final MultipleCurrencyAmount equityPV = MultipleCurrencyAmount.of(trs.getEquity().getCurrency(),
        equityMulticurves.getSpotEquity() * trs.getEquity().getNumberOfShares());
    return equityPV;
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
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PVDC, equityMulticurves.getCurves());
    return fundingLegPV;
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
    MultipleCurrencyMulticurveSensitivity fundingLegCS = trs.getFundingLeg().accept(PVCSDC, equityMulticurves.getCurves());
    return fundingLegCS;
  }

}
