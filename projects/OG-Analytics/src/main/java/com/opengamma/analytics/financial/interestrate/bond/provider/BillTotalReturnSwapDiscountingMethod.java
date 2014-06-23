/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.bond.definition.BillTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class with methods related to bill TRS valued by discounting.
 */
public final class BillTotalReturnSwapDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BillTotalReturnSwapDiscountingMethod INSTANCE = new BillTotalReturnSwapDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BillTotalReturnSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BillTotalReturnSwapDiscountingMethod() {
  }

  /** The present value and present value curve sensitivity calculators used for bills calculation */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();

  /**
   * Computes the present value of a bill TRS.
   * @param trs The bill total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BillTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bill TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PVIC, issuerMulticurves);
    final MultipleCurrencyAmount billPV = trs.getAsset().accept(PVIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return billPV.plus(fundingLegPV);
  }

  /**
   * Computes the present value of the asset leg of a bill TRS. The present value is equal to the bill present value.
   * @param trs The bill total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueAssetLeg(final BillTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bill TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount billPV = trs.getAsset().accept(PVIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return billPV;
  }

  /**
   * Computes the present value of the funding leg of a bill TRS.
   * @param trs The bill total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFundingLeg(final BillTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bill TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PresentValueIssuerCalculator.getInstance(), issuerMulticurves);
    return fundingLegPV;
  }

  /**
   * Computes the present value curve sensitivity of a bill TRS. 
   * The sensitivity to the (issuer) curves used in the bill valuation and the sensitivity to the curves used in the funding leg valuation are computed.
   * @param trs The bill total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BillTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bill TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    MultipleCurrencyMulticurveSensitivity fundingLegCS = trs.getFundingLeg().accept(PVCSIC, issuerMulticurves);
    MultipleCurrencyMulticurveSensitivity billCS = trs.getAsset().accept(PVCSIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return billCS.plus(fundingLegCS);
  }

}
