/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static com.opengamma.financial.convention.yield.SimpleYieldConvention.INDEX_LINKED_FLOAT;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.UK_IL_BOND;
import static com.opengamma.financial.convention.yield.SimpleYieldConvention.US_IL_REAL;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.provider.calculator.inflation.NetAmountInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDecorated;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * TODO : remove when inflation curves with issuer are integrated 
 */
public class BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer {

  /**
   * The unique instance of the class.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer INSTANCE = new BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondCapitalIndexedSecurityDiscountingMethodWithoutIssuer getInstance() {
    return INSTANCE;
  }

  /**
   * The present value inflation calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final NetAmountInflationCalculator NAIC = NetAmountInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSIC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  //TODO: REVIEW: Method depends on Calculator; Calculator would depend on Method (code duplicated to avoid circularity).
  /**
   * The root bracket used for yield finding.
   */
  private static final BracketRoot BRACKETER = new BracketRoot();
  /**
   * The root finder used for yield finding.
   */
  private static final RealSingleRootFinder ROOT_FINDER = new BrentSingleRootFinder();

  /**
   * Computes the present value of a capital indexed bound by index estimation and discounting. The value is the value of the nominal and the coupons but not the settlement.
   * @param bond The bond.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface provider) {
    ArgumentChecker.notNull(bond, "Bond");
    final MultipleCurrencyAmount pvNominal = bond.getNominal().accept(PVIC, provider);
    final MultipleCurrencyAmount pvCoupon = bond.getCoupon().accept(PVIC, provider);
    return pvNominal.plus(pvCoupon);
  }

  /**
   * Computes the security present value from a quoted clean real price. The real accrued are added to the clean real price,
   * the result is multiplied by the inflation index ratio and then discounted from settlement time to 0 with the discounting curve.
   * @param bondCapitalIndexedSecurity The bond security.
   * @param market The market.
   * @param cleanPriceReal The clean price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromCleanPriceReal(final BondCapitalIndexedSecurity<?> bondCapitalIndexedSecurity, final InflationProviderInterface market, final double cleanPriceReal) {
    Validate.notNull(bondCapitalIndexedSecurity, "Coupon");
    Validate.notNull(market, "Market");
    final double notional = bondCapitalIndexedSecurity.getCoupon().getNthPayment(0).getNotional();
    final double dirtyPriceReal = cleanPriceReal + bondCapitalIndexedSecurity.getAccruedInterest() / notional;
    final MultipleCurrencyAmount pv = bondCapitalIndexedSecurity.getSettlement().accept(PVIC, market.getInflationProvider());
    return pv.multipliedBy(dirtyPriceReal);
  }

  /**
   * Computes the clean real price of a bond security from a dirty real price.
   * @param bond The bond security.
   * @param dirtyPrice The dirty price.
   * @return The clean price.
   */
  public double cleanRealPriceFromDirtyRealPrice(final BondCapitalIndexedSecurity<?> bond, final double dirtyPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return dirtyPrice - bond.getAccruedInterest() / notional;
  }

  /**
   * Computes the clean price of a bond security from curves.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The clean price.
   */
  public double cleanRealPriceFromCurves(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves) {
    final double dirtyPrice = dirtyRealPriceFromCurves(bond, issuerMulticurves);
    return cleanRealPriceFromDirtyRealPrice(bond, dirtyPrice);
  }

  /**
   * Compute the dirty price of a bond security from curves.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The dirty price.
   */
  public double dirtyRealPriceFromCurves(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final MultipleCurrencyAmount pv = presentValue(bond, issuerMulticurves);
    final double df = issuerMulticurves.getMulticurveProvider().getDiscountFactor(bond.getCurrency(), bond.getSettlementTime());
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return pv.getAmount(bond.getCurrency()) / df / notional;
  }

  /**
   * Computes the dirty real price of a bond security from the clean real price.
   * @param bond The bond security.
   * @param cleanPrice The clean price.
   * @return The clean price.
   */
  public double dirtyRealPriceFromCleanRealPrice(final BondCapitalIndexedSecurity<?> bond, final double cleanPrice) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    return cleanPrice + bond.getAccruedInterest() / notional;
  }

  /**
   * The net amount paid at settlement date for a given clean real price.
   * @param bond The bond.
   * @param market The market.
   * @param cleanPriceReal The clean real price.
   * @return The net amount.
   */
  public MultipleCurrencyAmount netAmount(final BondCapitalIndexedSecurity<Coupon> bond, final InflationProviderInterface market, final double cleanPriceReal) {
    final double notional = bond.getCoupon().getNthPayment(0).getNotional();
    final double netAmountRealByUnit = cleanPriceReal + bond.getAccruedInterest() / notional;
    final MultipleCurrencyAmount netAmount = bond.getSettlement().accept(NAIC, market.getInflationProvider());
    return netAmount.multipliedBy(netAmountRealByUnit);

  }

  /**
   * Computes the dirty (real or nominal depending of the convention) price from the conventional real yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The dirty price.
   */
  public double dirtyPriceFromRealYield(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    Validate.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final YieldConvention yieldConvention = bond.getYieldConvention();
    if (yieldConvention.equals(US_IL_REAL)) {
      // Coupon period rate to next coupon and simple rate from next coupon to settlement.
      double pvAtFirstCoupon;
      if (Math.abs(yield) > 1.0E-8) {
        final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
        final double vn = Math.pow(factorOnPeriod, 1 - nbCoupon);
        pvAtFirstCoupon = ((CouponInflationGearing) bond.getCoupon().getNthPayment(0)).getFactor() / yield * (factorOnPeriod - vn) + vn;
      } else {
        pvAtFirstCoupon = ((CouponInflationGearing) bond.getCoupon().getNthPayment(0)).getFactor() / bond.getCouponPerYear() * nbCoupon + 1;
      }
      return pvAtFirstCoupon / (1 + bond.getAccrualFactorToNextCoupon() * yield / bond.getCouponPerYear());
    }

    if (yieldConvention.getName().equals(INDEX_LINKED_FLOAT.getName())) {
      double realRate = 0.0;
      if (bond.getCoupon().getNthPayment(1) instanceof CouponInflationGearing) {
        realRate = ((CouponInflationGearing) bond.getCoupon().getNthPayment(1)).getFactor() / bond.getCouponPerYear();
      } else if (bond.getCoupon().getNthPayment(1) instanceof CouponFixed) {
        realRate = ((CouponFixed) bond.getCoupon().getNthPayment(1)).getFixedRate();
      }
      double firstYearFraction = 0.0;
      double firstCouponEndFixingTime = 0.0;
      if (bond.getCoupon().getNthPayment(0) instanceof CouponInflationZeroCouponInterpolationGearing) {
        firstYearFraction = ((CouponInflationZeroCouponInterpolationGearing) bond.getCoupon().getNthPayment(0)).getPaymentYearFraction();
        firstCouponEndFixingTime = ((CouponInflationZeroCouponInterpolationGearing) bond.getCoupon().getNthPayment(0)).getReferenceEndTime()[1];
      } else if (bond.getCoupon().getNthPayment(0) instanceof CouponInflationZeroCouponMonthlyGearing) {
        firstYearFraction = ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(0)).getPaymentYearFraction();
        firstCouponEndFixingTime = ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(0)).getReferenceEndTime();
      }
      final double firstCashFlow = firstYearFraction * realRate;
      final double v = 1 / (1 + yield / bond.getCouponPerYear());
      final double rpibase = bond.getIndexStartValue();
      final double rpiLast = bond.getLastIndexKnownFixing();
      final int nbMonth = (int) ((firstCouponEndFixingTime - bond.getLastKnownFixingTime()) * 12);
      final double u = Math.pow(1 / (1 + .03), .5);
      final double a = rpiLast / rpibase * Math.pow(u, 2 * nbMonth / 12);
      if (bond.getCoupon().getNumberOfPayments() == 1) {
        return Math.pow(u * v, bond.getAccrualFactorToNextCoupon()) * (firstCashFlow + 1) * a / u;
      } else {
        double secondYearFraction = 0.0;
        if (bond.getCoupon().getNthPayment(1) instanceof CouponInflationZeroCouponInterpolationGearing) {
          secondYearFraction = ((CouponInflationZeroCouponInterpolationGearing) bond.getCoupon().getNthPayment(1)).getPaymentYearFraction();
        } else if (bond.getCoupon().getNthPayment(1) instanceof CouponInflationZeroCouponMonthlyGearing) {
          secondYearFraction = ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(1)).getPaymentYearFraction();
        }
        final double secondCashFlow = secondYearFraction * realRate;
        final double vn = Math.pow(v, nbCoupon - 1);
        final double pvAtFirstCoupon = firstCashFlow + secondCashFlow * u * v + a * realRate * v * v * (1 - vn / v) / (1 - v) + a * vn;
        return pvAtFirstCoupon * Math.pow(u * v, bond.getAccrualFactorToNextCoupon());
      }
    }
    if (yieldConvention.getName().equals(UK_IL_BOND.getName())) {
      double firstYearFraction = 0.0;
      final double realRate = ((CouponInflationGearing) bond.getCoupon().getNthPayment(1)).getFactor() / bond.getCouponPerYear();
      if (bond.getCoupon().getNthPayment(0) instanceof CouponInflationZeroCouponInterpolationGearing) {
        firstYearFraction = ((CouponInflationZeroCouponInterpolationGearing) bond.getCoupon().getNthPayment(0)).getPaymentYearFraction();
      } else if (bond.getCoupon().getNthPayment(0) instanceof CouponInflationZeroCouponMonthlyGearing) {
        firstYearFraction = ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(0)).getPaymentYearFraction();
      }
      final double firstCashFlow = firstYearFraction * realRate;
      final double v = 1 / (1 + yield / bond.getCouponPerYear());
      if (bond.getCoupon().getNumberOfPayments() == 1) {
        return Math.pow(v, bond.getAccrualFactorToNextCoupon()) * (firstCashFlow + 1);
      } else {
        double secondYearFraction = 0.0;
        if (bond.getCoupon().getNthPayment(1) instanceof CouponInflationZeroCouponInterpolationGearing) {
          secondYearFraction = ((CouponInflationZeroCouponInterpolationGearing) bond.getCoupon().getNthPayment(1)).getPaymentYearFraction();
        } else if (bond.getCoupon().getNthPayment(1) instanceof CouponInflationZeroCouponMonthlyGearing) {
          secondYearFraction = ((CouponInflationZeroCouponMonthlyGearing) bond.getCoupon().getNthPayment(1)).getPaymentYearFraction();
        }
        final double secondCashFlow = secondYearFraction * realRate;
        final double vn = Math.pow(v, nbCoupon - 1);
        final double pvAtFirstCoupon = firstCashFlow + secondCashFlow * v + realRate * v * v * (1 - vn / v) / (1 - v) + vn;
        return pvAtFirstCoupon * Math.pow(v, bond.getAccrualFactorToNextCoupon());
      }
    }
    throw new UnsupportedOperationException("The convention " + bond.getYieldConvention().getName() + " is not supported.");
  }

  /**
   * Computes the clean price (real or nominal depending on the convention) from the conventional real yield.
   * @param bond  The bond security.
   * @param yield The bond yield.
   * @return The clean price.
   */
  public double cleanPriceFromYield(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    Validate.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final double dirtyPrice = dirtyPriceFromRealYield(bond, yield);
    return cleanRealPriceFromDirtyRealPrice(bond, dirtyPrice);
  }

  /**
   * Compute the conventional yield from the dirty price.
   * @param bond The bond security.
   * @param dirtyPrice The bond dirty price.
   * @return The yield.
   */
  public double yieldRealFromDirtyRealPrice(final BondCapitalIndexedSecurity<?> bond, final double dirtyPrice) {
    /**
     * Inner function used to find the yield.
     */
    final Function1D<Double, Double> priceResidual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double y) {
        return dirtyPriceFromRealYield(bond, y) - dirtyPrice;
      }
    };
    final double[] range = BRACKETER.getBracketedPoints(priceResidual, -0.05, 0.10);
    final double yield = ROOT_FINDER.getRoot(priceResidual, range[0], range[1]);
    return yield;
  }

  /**
   * Computes the present value sensitivity of a capital indexed bound by index estimation and discounting. The sensitivity is the sensitivity of the nominal and the coupons but not the settlement.
   * @param bond The bond.
   * @param provider The provider.
   * @return The present value.
   */
  public MultipleCurrencyInflationSensitivity presentValueCurveSensitivity(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface provider) {
    ArgumentChecker.notNull(bond, "Bond");
    final MultipleCurrencyInflationSensitivity sensitivityNominal = bond.getNominal().accept(PVCSIC, provider);
    final MultipleCurrencyInflationSensitivity sensitivityCoupon = bond.getCoupon().accept(PVCSIC, provider);
    return sensitivityNominal.plus(sensitivityCoupon);
  }

  /**
   * Computes the bill yield from the curves. The yield is in the bill yield convention.
   * @param bond The bond.
   * @param provider The inflation and multi-curves provider.
   * @return The yield.
   */
  public double yieldRealFromCurves(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface provider) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(provider, "inflation and multi-curves provider");
    final double dirtyPrice = dirtyRealPriceFromCurves(bond, provider);
    final double yield = yieldRealFromDirtyRealPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Calculates the modified duration from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The modified duration
   */
  private double modifiedDurationFromYieldStandard(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(0).getNotional();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double mdAtFirstCoupon = 0;
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      mdAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getNotional() / Math.pow(factorOnPeriod, loopcpn + 1) * (loopcpn + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getNotional() / Math.pow(factorOnPeriod, loopcpn);
    }
    mdAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) / bond.getCouponPerYear();
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
    final double md = mdAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
    return md;
  }

  /**
   * Computes the modified duration of a bond from the curves.
   * @param bond  The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The modified duration.
   */
  public double modifiedDurationFromCurves(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves) {
    final double yield = yieldRealFromCurves(bond, issuerMulticurves);
    return modifiedDurationFromYieldStandard(bond, yield);
  }

  /**
   * Compute the conventional yield from the clean price.
   * @param bond The bond security.
   * @param cleanPrice The bond clean price.
   * @return The yield.
   */
  public double yieldRealFromCleanRealPrice(final BondCapitalIndexedSecurity<?> bond, final double cleanPrice) {
    final double dirtyPrice = dirtyRealPriceFromCleanRealPrice(bond, cleanPrice);
    final double yield = yieldRealFromDirtyRealPrice(bond, dirtyPrice);
    return yield;
  }

  /**
   * Computes the modified duration of a bond from the clean price.
   * @param bond  The bond security.
   * @param cleanPrice The bond clean price.
   * @return The modified duration.
   */
  public double modifiedDurationFromCleanPrice(final BondCapitalIndexedSecurity<?> bond, final double cleanPrice) {
    final double yield = yieldRealFromCleanRealPrice(bond, cleanPrice);
    return modifiedDurationFromYieldStandard(bond, yield);
  }

  /**
   * Calculates the convexity from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The convexity
   */
  private double convexityFromYieldStandard(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final int nbCoupon = bond.getCoupon().getNumberOfPayments();
    final double nominal = bond.getNominal().getNthPayment(bond.getNominal().getNumberOfPayments() - 1).getNotional();
    final double factorOnPeriod = 1 + yield / bond.getCouponPerYear();
    double cvAtFirstCoupon = 0;
    double pvAtFirstCoupon = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      cvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getNotional() / Math.pow(factorOnPeriod, loopcpn + 2) * (loopcpn + bond.getAccrualFactorToNextCoupon())
          * (loopcpn + bond.getAccrualFactorToNextCoupon() + 1) / (bond.getCouponPerYear() * bond.getCouponPerYear());
      pvAtFirstCoupon += bond.getCoupon().getNthPayment(loopcpn).getNotional() / Math.pow(factorOnPeriod, loopcpn);
    }
    cvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon + 1) * (nbCoupon - 1 + bond.getAccrualFactorToNextCoupon()) * (nbCoupon + bond.getAccrualFactorToNextCoupon())
        / (bond.getCouponPerYear() * bond.getCouponPerYear());
    pvAtFirstCoupon += nominal / Math.pow(factorOnPeriod, nbCoupon - 1);
    final double pv = pvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon());
    final double cv = cvAtFirstCoupon * Math.pow(factorOnPeriod, -bond.getAccrualFactorToNextCoupon()) / pv;
    return cv;
  }

  /**
   * Computes the convexity of a bond from the curves.
   * @param bond  The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @return The convexity.
   */
  public double convexityFromCurves(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves) {
    final double yield = yieldRealFromCurves(bond, issuerMulticurves);
    return convexityFromYieldFiniteDifference(bond, yield);
  }

  /**
   * Calculates the modified duration from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The modified duration
   */
  public double modifiedDurationFromYieldFiniteDifference(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final double price = cleanPriceFromYield(bond, yield);
    final double priceplus = cleanPriceFromYield(bond, yield + 10e-6);
    final double priceminus = cleanPriceFromYield(bond, yield - 10e-6);
    return -(priceplus - priceminus) / (2 * price * 10e-6);
  }

  /**
   * Calculates the modified duration from a standard yield.
   * @param bond The bond
   * @param yield The yield
   * @return The modified duration
   */
  public double convexityFromYieldFiniteDifference(final BondCapitalIndexedSecurity<?> bond, final double yield) {
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    ArgumentChecker.isTrue(bond.getNominal().getNumberOfPayments() == 1, "Yield: more than one nominal repayment.");
    final double price = cleanPriceFromYield(bond, yield);
    final double priceplus = cleanPriceFromYield(bond, yield + 10e-6);
    final double priceminus = cleanPriceFromYield(bond, yield - 10e-6);
    return (priceplus - 2 * price + priceminus) / (price * 10e-6 * 10e-6);
  }

  /**
   * Computes the present value of a bond security from z-spread. The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity).
   * The parallel shift is done in the curve convention.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param zSpread The z-spread.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromZSpread(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves, final double zSpread) {
    final InflationProviderInterface issuerShifted = new InflationProviderDecorated(issuerMulticurves, zSpread);
    return presentValue(bond, issuerShifted);
  }

  /**
   * Computes the present value of a bond security from z-spread. The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity).
   * The parallel shift is done in the curve convention.
   * @param bond The bond security.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param zSpread The z-spread.
   * @return The present value.
   */
  public double cleanPriceFromZSpread(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves, final double zSpread) {
    final InflationProviderInterface issuerShifted = new InflationProviderDecorated(issuerMulticurves, zSpread);
    return cleanRealPriceFromCurves(bond, issuerShifted);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanRealPrice The target clean real price.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndCleanRealPriceDirect(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves, final double cleanRealPrice) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Function1D<Double, Double> residual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return cleanPriceFromZSpread(bond, issuerMulticurves, z) - cleanRealPrice;
      }
    };

    final double[] range = BRACKETER.getBracketedPoints(residual, -0.5, 0.5); // Starting range is [-1%, 1%]
    return ROOT_FINDER.getRoot(residual, range[0], range[1]);
  }

  /**
   * Computes a bond z-spread from the curves and a present value.
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param pv The target present value.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndPV(final BondCapitalIndexedSecurity<?> bond, final InflationProviderInterface issuerMulticurves, final MultipleCurrencyAmount pv) {
    ArgumentChecker.notNull(bond, "Bond");
    ArgumentChecker.notNull(issuerMulticurves, "Issuer and multi-curves provider");
    final Currency ccy = bond.getCurrency();

    final Function1D<Double, Double> residual = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double z) {
        return presentValueFromZSpread(bond, issuerMulticurves, z).getAmount(ccy) - pv.getAmount(ccy);
      }
    };

    final double[] range = BRACKETER.getBracketedPoints(residual, -0.5, 0.5); // Starting range is [-1%, 1%]
    return ROOT_FINDER.getRoot(residual, range[0], range[1]);
  }

  /**
   * Computes a bond z-spread from the curves and a clean price. 
   * The z-spread is a parallel shift applied to the discounting curve associated to the bond (Issuer Entity) to match the CleanPrice present value.
   * @param bond The bond.
   * @param issuerMulticurves The issuer and multi-curves provider.
   * @param cleanPrice The target clean price.
   * @return The z-spread.
   */
  public double zSpreadFromCurvesAndCleanPrice(final BondCapitalIndexedSecurity<Coupon> bond, final InflationProviderInterface issuerMulticurves, final double cleanPrice) {
    return zSpreadFromCurvesAndPV(bond, issuerMulticurves, presentValueFromCleanPriceReal(bond, issuerMulticurves, cleanPrice));
  }

}
