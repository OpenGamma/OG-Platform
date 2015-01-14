/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesE2ETest {
  private static final Currency EUR = Currency.EUR;
  private static final ZonedDateTime VALUATION_DATE = ZonedDateTime.of(2014, 2, 17, 9, 0, 0, 0, ZoneId.of("Z"));

  /* Interpolators for issuer curve */
  private static final Interpolator1D LINEAR_FLAT_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

  private static final LegalEntityFilter<LegalEntity> SHORT_NAME_FILTER = new LegalEntityShortName();
  private static final IssuerProviderDiscount ISSUER_DISCOUNT = new IssuerProviderDiscount(new FXMatrix());
  static {
    //    double[] time = new double[] {0.0027397260273972603, 0.019178082191780823, 0.0821917808219178, 0.2465753424657534,
    //        0.4931506849315068, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 15.0, 20.0, 30.0 };
    double[] rate = new double[] {6.8E-4, 6.8E-4, 6.8E-4, 6.8E-4, 8.8E-4, 8.3E-4, 0.00109, 0.00212, 0.00414, 0.00674,
        0.00838, 0.01075, 0.01311, 0.01547, 0.0174, 0.02289, 0.02577, 0.02693 };
    Tenor[] maturity = new Tenor[] {Tenor.ofDays(1), Tenor.ofDays(7), Tenor.ofMonths(1), Tenor.ofMonths(3),
        Tenor.ofMonths(6), Tenor.ofYears(1), Tenor.ofYears(2), Tenor.ofYears(3), Tenor.ofYears(4), Tenor.ofYears(5),
        Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(15),
        Tenor.ofYears(20), Tenor.ofYears(30) };
    int nNodes = maturity.length;
    double[] time = new double[nNodes];
    final double[][] jacobian = new double[nNodes][nNodes];
    for (int i = 0; i < nNodes; ++i) {
      //      LocalDate maturityDate = VALUATION_DATE.toLocalDate().plus(maturity[i].getPeriod());
      //      time[i] = ACT365.getDayCountFraction(VALUATION_DATE.toLocalDate(), maturityDate);
      time[i] = timeCalculator(maturity[i]);
      jacobian[i][i] = 1.0;
    }
    String curveName = "EURBond-Definition";
    InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(time, rate, LINEAR_FLAT_LINEAR, curveName);
    int compoundingPeriod = 1;
    YieldPeriodicCurve curveYield = YieldPeriodicCurve.from(compoundingPeriod, curve);
    ISSUER_DISCOUNT.setCurve(Pairs.<Object, LegalEntityFilter<LegalEntity>>of("EUR", SHORT_NAME_FILTER),
        curveYield);
    ISSUER_DISCOUNT.getMulticurveProvider().setCurve(EUR, curveYield);

    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
    unitMap.put(curveName, Pairs.of(nNodes, nNodes));
    unitBundles.put(curveName, Pairs.of(new CurveBuildingBlock(unitMap), new DoubleMatrix2D(jacobian)));
  }

  private static final double LAST_MARGIN_PRICE = 0.0;
  // 2014-06-10T23:59Z
  private static final ZonedDateTime TRADING_LAST_DATE = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
  private static final ZonedDateTime NOTICE_FIRST_DATE = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
  private static final ZonedDateTime NOTICE_LAST_DATE = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
  private static final ZonedDateTime DELIVERY_FIRST_DATE = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
  private static final ZonedDateTime DELIVERY_LAST_DATE = ZonedDateTime.of(2014, 6, 10, 23, 59, 0, 0, ZoneId.of("Z"));
  private static final double NOTIONAL = 1000.0;
  private static final BondFixedSecurityDefinition[] DELIVERY_BUSKET;
  static {
    // 2011-04-26T00:00Z
    ZonedDateTime firstAccrualDate = ZonedDateTime.of(2011, 4, 26, 0, 0, 0, 0, ZoneId.of("Z"));
    // 2012-04-08T00:00Z
    ZonedDateTime firstCouponDate = ZonedDateTime.of(2012, 4, 8, 0, 0, 0, 0, ZoneId.of("Z"));
    // 2016-04-08T00:00Z
    ZonedDateTime maturityDate = ZonedDateTime.of(2016, 4, 8, 0, 0, 0, 0, ZoneId.of("Z"));
    Period paymentPeriod = Period.ofMonths(12);
    double fixedRate = 0.0275;
    int settlementDays = 3;
    Calendar calendar = new MondayToFridayCalendar("A"); //TODO use the correct calendar
    DayCount dayCount = DayCounts.ACT_ACT_ICMA;
    BusinessDayConvention businessDay = BusinessDayConventions.FOLLOWING;
    YieldConvention yieldConvention = SimpleYieldConvention.GERMAN_BOND;
    boolean isEOM = false;
    LegalEntity legalEntity = new LegalEntity("DE0001141604", "EUR", null, Sector.of("EU"), Region.of("EU",
        Country.of("EU"), EUR));
    BondFixedSecurityDefinition bondFixed = BondFixedSecurityDefinition.from(EUR, firstAccrualDate, firstCouponDate,
        maturityDate, paymentPeriod, fixedRate, settlementDays, calendar, dayCount, businessDay, yieldConvention,
        isEOM, legalEntity);
      DELIVERY_BUSKET  = new BondFixedSecurityDefinition[] {bondFixed };
  }
  private static final double[] CONVERSION_FACTOR = new double[] {0.945174 };
  private static final BondFuturesSecurityDefinition BOND_FUTURES_DEFINITION = new BondFuturesSecurityDefinition(
      TRADING_LAST_DATE, NOTICE_FIRST_DATE, NOTICE_LAST_DATE, DELIVERY_FIRST_DATE, DELIVERY_LAST_DATE, NOTIONAL,
      DELIVERY_BUSKET, CONVERSION_FACTOR);
  private static int QUANTITY = 1;
  // 2008-08-27T01:00Z
  private static ZonedDateTime TRADE_DATE = ZonedDateTime.of(2008, 8, 27, 1, 0, 0, 0, ZoneId.of("Z"));;
  private static final BondFuturesTransactionDefinition TRANSACTION_DEFINITION = new BondFuturesTransactionDefinition(
      BOND_FUTURES_DEFINITION, QUANTITY, TRADE_DATE, LAST_MARGIN_PRICE);
  private static final BondFuturesTransaction TRANSACTION = TRANSACTION_DEFINITION.toDerivative(VALUATION_DATE,
      LAST_MARGIN_PRICE);

  private static final double BOND_MARKET_PRICE = 105.625;

  private static final BondSecurityDiscountingMethod BOND_CALC = BondSecurityDiscountingMethod.getInstance();

  int spreadMaxIterations = 100;
  double spreadTolerance = 1.0e-8;

  @Test
  public void spreadTest() {
    double spread = calculateBondSpread(TRANSACTION.getUnderlyingSecurity().getDeliveryBasketAtSpotDate()[0],
        ISSUER_DISCOUNT, BOND_MARKET_PRICE, spreadMaxIterations, spreadTolerance);
    System.out.println(spread);
  }


  private double calculateBondSpread(BondFixedSecurity bond, IssuerProviderInterface issuerCurves, double marketPrice,
      int spreadMaxIterations, double spreadTolerance) {
    double bondPrice = BOND_CALC.cleanPriceFromCurves(bond, issuerCurves);

    double bondPV01Value = calculateBondPrice(bond, issuerCurves, 1e-4) / 100 - bondPrice;

    double diff = marketPrice / 100 - bondPrice;

    double spread = (diff / bondPV01Value) * 1.e-4;

    for (int i = 0; i < spreadMaxIterations; i++) {
      if (Math.abs(diff) > spreadTolerance) {
        bondPrice = calculateBondPrice(bond, issuerCurves, spread) / 100;
        diff = marketPrice / 100 - bondPrice;
        spread = (spread + (diff / bondPV01Value) * 1.e-4);
      } else {
        return spread;
      }
    }
    throw new OpenGammaRuntimeException("Unable to solve spread within " + spreadTolerance + " for " +
        spreadMaxIterations + " iterations for " + bond);
  }

  private double calculateBondFuturePrice(BondFuturesSecurity bondFuture, IssuerProviderInterface issuerCurves,
      double repoRate, double spread) {
    BondFixedSecurity bondAtSpot = bondFuture.getDeliveryBasketAtSpotDate()[0];
    double bondPrice = calculateBondPrice(bondAtSpot, issuerCurves, spread) / 100;
    return calculateBondFuturePrice(bondFuture, repoRate, bondPrice);
  }

  private double calculateBondFuturePrice(BondFuturesSecurity bondFuture, double repoRate, double bondPrice) {

    BondFixedSecurity bond = bondFuture.getDeliveryBasketAtDeliveryDate()[0];
    BondFixedSecurity bondAtSpot = bondFuture.getDeliveryBasketAtSpotDate()[0];

    double accrualDeliveryDate = bond.getAccruedInterest();
    double accrualSettleDate = bondAtSpot.getAccruedInterest();

    double repoAcc = bond.getSettlementTime() - bondAtSpot.getSettlementTime();
    double convFactor = bondFuture.getConversionFactor()[0];

    double accrualFuture = 0;
    for (CouponFixed coupon : bondAtSpot.getCoupon().getPayments()) {
      if (coupon.getPaymentTime() < bondFuture.getDeliveryLastTime()) {
        double accrualFutureFrac = bond.getSettlementTime() - coupon.getPaymentTime();
        accrualFuture += coupon.getFixedRate() * coupon.getPaymentYearFraction() *
            Math.pow(1 + repoRate, accrualFutureFrac);
      } else {
        break;
      }
    }

    double finalPV = ((bondPrice + accrualSettleDate) * (Math.pow(1 + repoRate, repoAcc)) - accrualDeliveryDate - accrualFuture) /
        convFactor;
    return finalPV * 100;
  }

  private double calculateBondPrice(BondFixedSecurity bond,
      IssuerProviderInterface issuerCurves,
      double spread) {

    IssuerProviderDiscount issuerDiscount = (IssuerProviderDiscount) issuerCurves.copy();

    ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(spread);
    YieldCurve spreadCurve = new YieldCurve("spread", constantDoublesCurve);

    for (Map.Entry<Currency, YieldAndDiscountCurve> entry : issuerDiscount.getMulticurveProvider()
        .getDiscountingCurves().entrySet()) {
      // wrap base curve in spread curve
      YieldAndDiscountAddZeroFixedCurve wrappedCurve = new YieldPeriodicAddZeroFixedCurve("Wrapped", false,
          (YieldPeriodicCurve) entry.getValue(), spreadCurve);
      issuerDiscount.replaceCurve(entry.getKey(), wrappedCurve);
    }
    for (Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : issuerDiscount
        .getIssuerCurves().entrySet()) {
      // wrap base curve in spread curve
      YieldAndDiscountAddZeroFixedCurve wrappedCurve = new YieldPeriodicAddZeroFixedCurve("Wrapped", false,
          (YieldPeriodicCurve) entry.getValue(), spreadCurve);
      issuerDiscount.getIssuerCurves().put(entry.getKey(), wrappedCurve);
    }

    double bondPrice = BOND_CALC.cleanPriceFromCurves(bond, issuerDiscount);
    return bondPrice * 100;
  }

  private static double timeCalculator(Tenor maturity) {
    if (maturity.getPeriod().getDays() > 0) {
      return maturity.getPeriod().getDays() / 365.0;
    } else if (maturity.getPeriod().getMonths() > 0) {
      return maturity.getPeriod().getMonths() * 30.0 / 365.0;
    }
    return maturity.getPeriod().getYears();
  }
}
