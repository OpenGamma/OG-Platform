/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static com.opengamma.financial.convention.yield.SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.FRANCE_COMPOUND_METHOD;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.GERMAN_BOND;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.ITALY_TREASURY_BONDS;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.MEXICAN_BONOS;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.UK_BUMP_DMO_METHOD;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.US_STREET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueParallelCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderIssuerDecoratedSpread;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderIssuerDecoratedSpreadPeriodic;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class with methods related to bond security valued by discounting.
 * TODO sensitivities with spread to periodic compounded rates
 */
public final class BondSecurityDiscountingMethod {
  /**
   * The unique instance of the class.
   */
  private static final BondSecurityDiscountingMethod INSTANCE = new BondSecurityDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondSecurityDiscountingMethod() {
  }

  /**
   * The present value calculator (for the different parts of the bond security).
   */
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  /**
   * The present value curve sensitivity calculator (for the different parts of the bond security).
   */
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  /**
   * The present value parallel shifts curve sensitivity calculator.
   */
  private static final PresentValueParallelCurveSensitivityDiscountingCalculator PVPCSDC = PresentValueParallelCurveSensitivityDiscountingCalculator.getInstance();
  /**
   * The root bracket used for yield finding.
   */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /**
   * The root finder used for yield finding.
   */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();
  /**
   * Brackets a root
   */
  private static final BracketRoot ROOT_BRACKETER = new BracketRoot();

  /**
   * Computes the present value of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, bond.getCurrency(), bond.getIssuerEntity());
    final MultipleCurrencyAmount pvNominal = bond.getNominal().accept(PVDC, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = bond.getCoupon().accept(PVDC, multicurvesDecorated);
    return pvNominal.plus(pvCoupon);
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param multicurves The multi-curves provider.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPrice(final BondSecurity<? extends Payment, ? extends Coupon> bond, final MulticurveProviderInterface multicurves, final double cleanPrice) {
    ArgumentChecker.isTrue(bond instanceof BondFixedSecurity, "Present value from clean price available only for fixed coupon bond");
    final BondFixedSecurity bondFixed = (BondFixedSecurity) bond;
    final double dfSettle = multicurves.getDiscountFactor(bond.getCurrency(), bondFixed.getSettlementTime());
    final double pvPrice = (cleanPrice * bondFixed.getCoupon().getNthPayment(0).getNotional() + bondFixed.getAccruedInterest()) * dfSettle;
    return MultipleCurrencyAmount.of(bond.getCurrency(), pvPrice);
  }

  /**
   * Compute the present value of a bond transaction from its yield.
   * @param bond The bond transaction.
   * @param multicurves The multi-curves provider.
   * @param yield The bond yield.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromYield(final BondSecurity<? extends Payment, ? extends Coupon> bond, final MulticurveProviderInterface multicurves, final double yield) {
    ArgumentChecker.isTrue(bond instanceof BondFixedSecurity, "Present value from clean price available only for fixed coupon bond");
    final BondFixedSecurity bondFixed = (BondFixedSecurity) bond;
    final double cleanPrice = cleanPriceFromYield(bondFixed, yield);
    return presentValueFromCleanPrice(bondFixed, multicurves, cleanPrice);
  }

  /**
   * Computes the present value of a bond security from z-spread. The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity).
   * The parallel shift is done in the curve convention.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param zSpread The z-spread.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromZSpread(final BondSecurity<? extends Payment, ? extends Coupon> bond,
      final IssuerProviderInterface issuerMulticurves, final double zSpread) {
    IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpread(issuerMulticurves,
        bond.getIssuerEntity(), zSpread);
    return presentValue(bond, issuerShifted);
  }

  /**
   * Computes the present value of a bond security from z-spread. The z-spread is a parallel shift applied to continuously compounded rates or periodic compounded rates 
   * of the discounting curve associated to the bond (Issuer Entity). 
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param zSpread The z-spread.
   * @param periodic If true, the spread is added to periodic compounded rates. If false, the spread is added to continuously compounded rates
   * @param periodPerYear The number of periods per year.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromZSpread(BondSecurity<? extends Payment, ? extends Coupon> bond,
      IssuerProviderInterface issuerMulticurves, double zSpread, boolean periodic, int periodPerYear) {
    if (periodic) {
      IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpreadPeriodic(issuerMulticurves,
        bond.getIssuerEntity(), zSpread, periodPerYear);
      return presentValue(bond, issuerShifted);
    }
    return presentValueFromZSpread(bond, issuerMulticurves, zSpread);
  }

  /** 
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param zSpread The z-spread.
   * @return The Z spread sensitivity.
   */
  public double presentValueZSpreadSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves, final double zSpread) {
    final IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpread(issuerMulticurves, bond.getIssuerEntity(), zSpread);
    final StringAmount parallelSensi = presentValueParallelCurveSensitivity(bond, issuerShifted);
    return parallelSensi.getMap().get(issuerMulticurves.getName(bond.getIssuerEntity()));
  }

  /**
   * Compute the dirty price of a bond security from curves.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The dirty price.
   */
  public double dirtyPriceFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final MultipleCurrencyAmount pv = presentValue(bond, issuerMulticurves);
    final double df = issuerMulticurves.getMulticurveProvider().getDiscountFactor(bond.getCurrency(), bond.getSettlementTime());
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return pv.getAmount(bond.getCurrency()) / df / notional;
  }

  /**
   * Computes the dirty price of a bond security from a clean price.
   * @param bond The bond security.
   * @param cleanPrice The clean price.
   * @return The dirty price.
   */
  public double dirtyPriceFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return cleanPrice + bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the dirty price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceFromYield(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    final YieldConvention yieldConvention = bond.getYieldConvention();
    if (nbCoupon == 1) {
      if (yieldConvention.equals(US_STREET) || yieldConvention.equals(GERMAN_BOND) || yieldConvention.equals(ITALY_TREASURY_BONDS)
          || yieldConvention.equals(AUSTRALIA_EX_DIVIDEND) || yieldConvention.equals(MEXICAN_BONOS)) {
        return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear()) / nominal;
      }
      if (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) {
        return (nominal + bond.getCoupon().getNthPayment(0).getAmount()) / nominal * Math.pow(1.0 + yield / bond.getCouponPerYear(), -bond.getFactorToNextCoupon());
      }
    }
    if ((yieldConvention.equals(US_STREET)) || (yieldConvention.equals(UK_BUMP_DMO_METHOD)) || (yieldConvention.equals(GERMAN_BOND))
        || (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) || (yieldConvention.equals(AUSTRALIA_EX_DIVIDEND) || yieldConvention.equals(MEXICAN_BONOS))) {
      return dirtyPriceFromYieldStandard(bond, yield);
    }
    if (yieldConvention.equals(ITALY_TREASURY_BONDS)) {
      final double yieldSemiAnnual = (Math.sqrt(1 + yield) - 1) * 2;
      return dirtyPriceFromYieldStandard(bond, yieldSemiAnnual);
    }
    throw new UnsupportedOperationException("The convention " + yieldConvention.getName() + " is not supported.");
  }

  /**
   * Calculates the dirty price from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The dirty price
   */
  private double dirtyPriceFromYieldStandard(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(0).getAmount();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
    }
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    return pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / nominal;
  }

  /**
   * Computes the dirty price sensitivity to the curves.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity dirtyPriceCurveSensitivity(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final MulticurveProviderInterface multicurves = issuerMulticurves.getMulticurveProvider();
    final Currency ccy = bond.getCurrency();
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final MultipleCurrencyAmount pv = presentValue(bond, issuerMulticurves);
    final MultipleCurrencyMulticurveSensitivity sensiPv = presentValueCurveSensitivity(bond, issuerMulticurves);
    final double df = multicurves.getDiscountFactor(ccy, bond.getSettlementTime());
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listDf = new ArrayList<>();
    listDf.add(DoublesPair.of(bond.getSettlementTime(), bond.getSettlementTime() / df));
    resultMap.put(multicurves.getName(ccy), listDf);
    MulticurveSensitivity result = MulticurveSensitivity.ofYieldDiscounting(resultMap);
    result = result.multipliedBy(pv.getAmount(ccy) / notional);
    result = result.plus(sensiPv.getSensitivity(ccy).multipliedBy(1 / (df * notional)));
    return result;
  }

  /**
   * Computes the clean price of a bond security from curves.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The clean price.
   */
  public double cleanPriceFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    final double dirtyPrice = dirtyPriceFromCurves(bond, issuerMulticurves);
    return cleanPriceFromDirtyPrice(bond, dirtyPrice);
  }

  /**
   * Computes the clean price of a bond security from a dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The dirty price.
   * @return The clean price.
   */
  public double cleanPriceFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return dirtyPrice - bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the clean price from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The clean price.
   */
  public double cleanPriceFromYield(final BondFixedSecurity bond, final double yield) {
    final double dirtyPrice = dirtyPriceFromYield(bond, yield);
    final double cleanPrice = cleanPriceFromDirtyPrice(bond, dirtyPrice);
    return cleanPrice;
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The yield.
   */
  public double yieldFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    /**
     * Inner function used to find the yield.
     */
    final Function1D<Double, Double> priceResidual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return dirtyPriceFromYield(bond, y) - dirtyPrice;
      }
    };
    final double[] range = BRACKETER.getBracketedPoints(priceResidual, 0.00, 0.20);
    final double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The yield.
   */
  public double yieldFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    final double dirtyPrice = dirtyPriceFromCurves(bond, issuerMulticurves);
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Compute the conventional yield from the clean price.
   * @param bond The bond security.
   * @param cleanPrice The bond clean price.
   * @return The yield.
   */
  public double yieldFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double dirtyPrice = dirtyPriceFromCleanPrice(bond, cleanPrice);
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Computes the modified duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The modified duration.
   */
  public double modifiedDurationFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    //    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    final YieldConvention yieldConvention = bond.getYieldConvention();
    if (nbCoupon == 1) {
      if (yieldConvention.equals(US_STREET) || yieldConvention.equals(GERMAN_BOND) || yieldConvention.equals(ITALY_TREASURY_BONDS)
          || yieldConvention.equals(AUSTRALIA_EX_DIVIDEND) || yieldConvention.equals(MEXICAN_BONOS)) {
        return bond.getFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear());
      }
      if (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) {
        return bond.getFactorToNextCoupon() / bond.getCouponPerYear() / (1.0 + yield / bond.getCouponPerYear());
      }
    }
    if (yieldConvention.equals(US_STREET) || yieldConvention.equals(UK_BUMP_DMO_METHOD) || yieldConvention.equals(GERMAN_BOND)
        || (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) || (yieldConvention.equals(AUSTRALIA_EX_DIVIDEND))) {
      return modifiedDurationFromYieldStandard(bond, yield);
    }
    if (yieldConvention.equals(ITALY_TREASURY_BONDS)) {
      final double yieldSemiAnnual = (Math.sqrt(1 + yield) - 1) * 2;
      final double modifiedDurationSemiAnnual = modifiedDurationFromYieldStandard(bond, yieldSemiAnnual);
      final double modifiedDuration = modifiedDurationSemiAnnual / Math.sqrt(1 + yield);
      return modifiedDuration;
    }
    throw new UnsupportedOperationException("The convention " + yieldConvention.getName() + " is not supported for modified duration computation.");
  }

  /**
   * Calculates the modified duration from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The modified duration
   */
  private double modifiedDurationFromYieldStandard(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(0).getAmount();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double mdAtFirstCoupon = 0;
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getFactorToNextCoupon()) / bond.getCouponPerYear();
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
    }
    mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getFactorToNextCoupon()) / bond.getCouponPerYear();
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon());
    final double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / pv;
    return md;
  }

  /**
   * Computes the modified duration of a bond from the curves.
   * @param bond  The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The modified duration.
   */
  public double modifiedDurationFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    final double yield = yieldFromCurves(bond, issuerMulticurves);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the modified duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The modified duration.
   */
  public double modifiedDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the modified duration of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The modified duration.
   */
  public double modifiedDurationFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double yield = yieldFromCleanPrice(bond, cleanPrice);
    return modifiedDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macaulay duration of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The Macaulay duration.
   */
  public double macaulayDurationFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    if (((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.ITALY_TREASURY_BONDS)))
        && (nbCoupon == 1)) {
      return bond.getFactorToNextCoupon() / bond.getCouponPerYear();
    }
    if ((bond.getYieldConvention().equals(SimpleYieldConvention.US_STREET)) || (bond.getYieldConvention().equals(SimpleYieldConvention.UK_BUMP_DMO_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.GERMAN_BOND)) || (bond.getYieldConvention().equals(SimpleYieldConvention.FRANCE_COMPOUND_METHOD)) ||
        (bond.getYieldConvention().equals(SimpleYieldConvention.ITALY_TREASURY_BONDS) || bond.getYieldConvention().equals(MEXICAN_BONOS))) {
      return modifiedDurationFromYield(bond, yield) * (1 + yield / bond.getCouponPerYear());
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + " is not supported for Macaulay duration.");
  }

  /**
   * Computes the Macaulay duration of a bond from the curves.
   * @param bond  The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The Macaulay duration.
   */
  public double macaulayDurationFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    final double yield = yieldFromCurves(bond, issuerMulticurves);
    return macaulayDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macauley duration of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The Macauley duration.
   */
  public double macaulayDurationFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double yield = yieldFromCleanPrice(bond, cleanPrice);
    return macaulayDurationFromYield(bond, yield);
  }

  /**
   * Computes the Macauley duration of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The Macauley duration.
   */
  public double macaulayDurationFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return macaulayDurationFromYield(bond, yield);
  }

  /**
   * Computes the convexity of a bond from the conventional yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The convexity.
   */
  public double convexityFromYield(final BondFixedSecurity bond, final double yield) {
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final YieldConvention yieldConvention = bond.getYieldConvention();
    if (nbCoupon == 1) {
      if (yieldConvention.equals(US_STREET) || yieldConvention.equals(GERMAN_BOND) || yieldConvention.equals(ITALY_TREASURY_BONDS)
          || yieldConvention.equals(AUSTRALIA_EX_DIVIDEND) || yieldConvention.equals(MEXICAN_BONOS)) {
        final double timeToPay = bond.getFactorToNextCoupon() / bond.getCouponPerYear();
        final double disc = (1.0 + bond.getFactorToNextCoupon() * yield / bond.getCouponPerYear());
        return 2 * timeToPay * timeToPay / (disc * disc);
      }
      if (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) {
        throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + "with only one coupon is not supported.");
      }
    }
    if (yieldConvention.equals(US_STREET) || yieldConvention.equals(UK_BUMP_DMO_METHOD) || yieldConvention.equals(GERMAN_BOND)
        || (yieldConvention.equals(FRANCE_COMPOUND_METHOD)) || (yieldConvention.equals(AUSTRALIA_EX_DIVIDEND))) {
      return convexityFromYieldStandard(bond, yield);
    }
    if (yieldConvention.equals(ITALY_TREASURY_BONDS)) {
      final double yieldSemiAnnual = (Math.sqrt(1 + yield) - 1) * 2;
      final double modifiedDurationSemiAnnual = modifiedDurationFromYieldStandard(bond, yieldSemiAnnual);
      final double convexitySemiAnnual = convexityFromYieldStandard(bond, yieldSemiAnnual);
      final double ySp2 = 1.0d / (1 + yield);
      final double ySpp = -0.5 * Math.pow(ySp2, 1.5d);
      final double convexity = (convexitySemiAnnual * ySp2) - (modifiedDurationSemiAnnual * ySpp);
      return convexity;
    }
    throw new UnsupportedOperationException("The convention " + yieldConvention.getName() + " is not supported for convexity computation.");
  }

  /**
   * Calculates the convexity from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The convexity
   */
  private double convexityFromYieldStandard(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getAmount();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double cvAtFirstCoupon = 0;
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      cvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn + 2) * (loopcpn + bond.getFactorToNextCoupon())
          * (loopcpn + bond.getFactorToNextCoupon() + 1) / (bond.getCouponPerYear() * bond.getCouponPerYear());
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getAmount() / Math.pow(factorOnPeriod, loopcpn);
    }
    cvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon + 1) * (nbCoupon - 1 + bond.getFactorToNextCoupon()) * (nbCoupon + bond.getFactorToNextCoupon())
        / (bond.getCouponPerYear() * bond.getCouponPerYear());
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon());
    final double cv = cvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getFactorToNextCoupon()) / pv;
    return cv;
  }

  /**
   * Computes the convexity of a bond from the curves.
   * @param bond  The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The convexity.
   */
  public double convexityFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface issuerMulticurves) {
    final double yield = yieldFromCurves(bond, issuerMulticurves);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes the convexity of a bond from the dirty price.
   * @param bond  The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The convexity.
   */
  public double convexityFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    final double yield = yieldFromDirtyPrice(bond, dirtyPrice);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes the convexity of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The convexity.
   */
  public double convexityFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    final double yield = yieldFromCleanPrice(bond, cleanPrice);
    return convexityFromYield(bond, yield);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param pv The target present value.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndPV(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves, final MultipleCurrencyAmount pv) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getCurrency();

    final Function1D<Double, Double> residual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return presentValueFromZSpread(bond, issuerMulticurves, z).getAmount(ccy) - pv.getAmount(ccy);
      }
    };

    final double[] range = ROOT_BRACKETER.getBracketedPoints(residual, -0.01, 0.01); // Starting range is [-1%, 1%]
    return ROOT_FINDER.getRoot(residual, range[0], range[1]);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * The z-spread is a parallel shift applied to continously compounded rates or periodic compounded rates of the discounting curve 
   * associated to the bond (Issuer Entity) to match the present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param pv The target present value.
   * @param periodic If true, the spread is added to periodic compounded rates. If false, the spread is added to continuously compounded rates
   * @param periodPerYear The number of periods per year.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndPV(final BondSecurity<? extends Payment, ? extends Coupon> bond,
      final IssuerProviderInterface issuerMulticurves, final MultipleCurrencyAmount pv, final boolean periodic,
      final int periodPerYear) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getCurrency();

    final Function1D<Double, Double> residual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return presentValueFromZSpread(bond, issuerMulticurves, z, periodic, periodPerYear).getAmount(ccy) -
            pv.getAmount(ccy);
      }
    };

    double[] range = ROOT_BRACKETER.getBracketedPoints(residual, -0.01, 0.01); // Starting range is [-1%, 1%]
    return ROOT_FINDER.getRoot(residual, range[0], range[1]);
  }

  /**
   * Computes a bond present value z-spread sensitivity from the curves and a present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param pv The target present value.
   * @return The z-spread sensitivity.
   */
  public double presentValueZSpreadSensitivityFromCurvesAndPV(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves,
      final MultipleCurrencyAmount pv) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final double zSpread = zSpreadFromCurvesAndPV(bond, issuerMulticurves, pv);
    return presentValueZSpreadSensitivity(bond, issuerMulticurves, zSpread);
  }

  /**
   * Computes a bond z-spread from the curves and a clean price. 
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the CleanPrice present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanPrice The target clean price.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndClean(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves, final double cleanPrice) {
    return zSpreadFromCurvesAndPV(bond, issuerMulticurves, presentValueFromCleanPrice(bond, issuerMulticurves.getMulticurveProvider(), cleanPrice));
  }

  /**
   * Computes a bond z-spread from the curves and a clean price. 
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the CleanPrice present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanPrice The target clean price.
   * @return The z-spread.
   * @param periodic If true, the spread is added to periodic compounded rates. If false, the spread is added to continuously compounded rates
   * @param periodPerYear The number of periods per year.
   */
  public double zSpreadFromCurvesAndClean(BondSecurity<? extends Payment, ? extends Coupon> bond,
      IssuerProviderInterface issuerMulticurves, double cleanPrice, boolean periodic, int periodPerYear) {
    return zSpreadFromCurvesAndPV(bond, issuerMulticurves,
        presentValueFromCleanPrice(bond, issuerMulticurves.getMulticurveProvider(), cleanPrice), periodic,
        periodPerYear);
  }

  /**
   * Computes a bond z-spread from the curves and a yield. 
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param yield The yield.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndYield(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves, final double yield) {
    return zSpreadFromCurvesAndPV(bond, issuerMulticurves, presentValueFromYield(bond, issuerMulticurves.getMulticurveProvider(), yield));
  }

  /**
   * Computes a bond z-spread from the curves and a yield. 
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param yield The yield.
   * @return The z-spread.
   * @param periodic If true, the spread is added to periodic compounded rates. If false, the spread is added to continuously compounded rates
   * @param periodPerYear The number of periods per year.
   */
  public double zSpreadFromCurvesAndYield(BondSecurity<? extends Payment, ? extends Coupon> bond,
      IssuerProviderInterface issuerMulticurves, double yield, boolean periodic, int periodPerYear) {
    return zSpreadFromCurvesAndPV(bond, issuerMulticurves,
        presentValueFromYield(bond, issuerMulticurves.getMulticurveProvider(), yield), periodic, periodPerYear);

  }

  /**
   * Computes the bond present value z-spread sensitivity from the curves and a clean price.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanPrice The target clean price.
   * @return The z-spread sensitivity.
   */
  public double presentValueZSpreadSensitivityFromCurvesAndClean(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves,
      final double cleanPrice) {
    return presentValueZSpreadSensitivityFromCurvesAndPV(bond, issuerMulticurves, presentValueFromCleanPrice(bond, issuerMulticurves.getMulticurveProvider(), cleanPrice));
  }

  /**
   * Computes the present value curve sensitivity of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, bond.getCurrency(), bond.getIssuerEntity());
    final MultipleCurrencyMulticurveSensitivity pvcsNominal = bond.getNominal().accept(PVCSDC, multicurvesDecorated);
    final MultipleCurrencyMulticurveSensitivity pvcsCoupon = bond.getCoupon().accept(PVCSDC, multicurvesDecorated);
    return pvcsNominal.plus(pvcsCoupon);
  }

  /**
   * Computes the present value curve sensitivity to parallel curve movement of a bond security (without settlement amount payment).
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The present value curve sensitivity.
   */
  public StringAmount presentValueParallelCurveSensitivity(final BondSecurity<? extends Payment, ? extends Coupon> bond, final IssuerProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerMulticurves, bond.getCurrency(), bond.getIssuerEntity());
    final StringAmount pvpcsNominal = bond.getNominal().accept(PVPCSDC, multicurvesDecorated);
    final StringAmount pvpcsCoupon = bond.getCoupon().accept(PVPCSDC, multicurvesDecorated);
    return StringAmount.plus(pvpcsNominal, pvpcsCoupon);
  }

  /**
   * Calculates the accrued interest for a fixed-coupon bond using the yield. The accrued interest is defined
   * as dirty price - clean price.
   * @param bond The bond, not null
   * @param yield The yield
   * @return The accrued interest
   */
  public double accruedInterestFromYield(final BondFixedSecurity bond, final double yield) {
    ArgumentChecker.notNull(bond, "bond");
    return dirtyPriceFromYield(bond, yield) - cleanPriceFromYield(bond, yield);
  }

  /**
   * Calculates the accrued interest for a fixed-coupon bond using the dirty price. The accrued interest is defined
   * as dirty price - clean price.
   * @param bond The bond, not null
   * @param dirtyPrice The dirty price
   * @return The accrued interest
   */
  public double accruedInterestFromDirtyPrice(final BondFixedSecurity bond, final double dirtyPrice) {
    ArgumentChecker.notNull(bond, "bond");
    return dirtyPrice - cleanPriceFromDirtyPrice(bond, dirtyPrice);
  }

  /**
   * Calculates the accrued interest for a fixed-coupon bond using the clean price. The accrued interest is defined
   * as dirty price - clean price.
   * @param bond The bond, not null
   * @param cleanPrice The clean price
   * @return The accrued interest
   */
  public double accruedInterestFromCleanPrice(final BondFixedSecurity bond, final double cleanPrice) {
    ArgumentChecker.notNull(bond, "bond");
    return dirtyPriceFromCleanPrice(bond, cleanPrice) - cleanPrice;
  }

  /**
   * Calculates the accrued interest for a fixed-coupon bond using the curves. The accrued interest is defined
   * as dirty price - clean price.
   * @param bond The bond, not null
   * @param curves The curves, not null
   * @return The accrued interest
   */
  public double accruedInterestFromCurves(final BondFixedSecurity bond, final IssuerProviderInterface curves) {
    ArgumentChecker.notNull(bond, "bond");
    ArgumentChecker.notNull(curves, "curves");
    return dirtyPriceFromCurves(bond, curves) - cleanPriceFromCurves(bond, curves);
  }
}
