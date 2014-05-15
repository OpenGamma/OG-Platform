/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class with methods related to bond TRS valued by discounting.
 */
public final class BondTotalReturnSwapDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BondTotalReturnSwapDiscountingMethod INSTANCE = new BondTotalReturnSwapDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondTotalReturnSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondTotalReturnSwapDiscountingMethod() {
  }

  /** The present value and present value curve sensitivity calculators used for bonds calculation */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final PresentValueCurveSensitivityIssuerCalculator PVCSIC = PresentValueCurveSensitivityIssuerCalculator.getInstance();

  /**
   * Computes the present value of a bond TRS.
   * @param trs The bond total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bond TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PVIC, issuerMulticurves);
    final MultipleCurrencyAmount bondPV = trs.getAsset().accept(PVIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return bondPV.plus(fundingLegPV);
  }

  /**
   * Computes the present value of the asset leg of a bond TRS. The present value is equal to the bond present value.
   * @param trs The bond total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueAssetLeg(final BondTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bond TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount bondPV = trs.getAsset().accept(PVIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return bondPV;
  }

  /**
   * Computes the present value of the funding leg of a bond TRS.
   * @param trs The bond total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFundingLeg(final BondTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bond TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PresentValueIssuerCalculator.getInstance(), issuerMulticurves);
    return fundingLegPV;
  }

  /**
   * Computes the present value curve sensitivity of a bond TRS. 
   * The sensitivity to the (issuer) curves used in the bond valuation and the sensitivity to the curves used in the funding leg valuation are computed.
   * @param trs The bond total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bond TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    MultipleCurrencyMulticurveSensitivity fundingLegCS = trs.getFundingLeg().accept(PVCSIC, issuerMulticurves);
    MultipleCurrencyMulticurveSensitivity bondCS = trs.getAsset().accept(PVCSIC, issuerMulticurves).multipliedBy(trs.getQuantity());
    return bondCS.plus(fundingLegCS);
  }

}
