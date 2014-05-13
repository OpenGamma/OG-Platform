/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Class with methods related to bond security valued by discounting.
 */
public final class BondTRSDiscountingMethod {

  /**
   * The unique instance of the class.
   */
  private static final BondTRSDiscountingMethod INSTANCE = new BondTRSDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondTRSDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondTRSDiscountingMethod() {
  }

  /** The present value calculator used for bonds calculation */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();

  /**
   * Computes the present value of a bond TRS.
   * @param trs The bond total return swap.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondTotalReturnSwap trs, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(trs, "bond TRS");
    ArgumentChecker.notNull(issuerMulticurves, "issuer and multi-curve provider");
    final MultipleCurrencyAmount fundingLegPV = trs.getFundingLeg().accept(PresentValueIssuerCalculator.getInstance(), issuerMulticurves);
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
   * Computes the present value of the asset leg of a bond TRS. The present value is equal to the bond present value.
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

  // TODO: presentValueCurveSensitivity
}
