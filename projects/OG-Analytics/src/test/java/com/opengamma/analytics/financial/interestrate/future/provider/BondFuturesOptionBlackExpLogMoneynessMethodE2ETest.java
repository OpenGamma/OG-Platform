/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionBlackExpLogMoneynessMethodE2ETest {

  /* Interpolators for volatility surface */
  private static final Interpolator1D SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.SQUARE_LINEAR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_SQUARE_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  /* Interpolation is done along y direction first */
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, SQUARE_FLAT);

  private static final ZonedDateTime VALUATION_DATE = ZonedDateTime.of(2014, 2, 17, 9, 0, 0, 0, ZoneId.of("Z"));
  private static final DayCount ACT365 = DayCounts.ACT_365;
  private static final Currency EUR = Currency.EUR;
  private static final LegalEntityFilter<LegalEntity> SHORT_NAME_FILTER = new LegalEntityShortName();

  /* Interpolators for discount curve */
  private static final Interpolator1D LINEAR_FLAT_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);

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
    ISSUER_DISCOUNT.setCurve(Pairs.<Object, LegalEntityFilter<LegalEntity>>of(curveName, SHORT_NAME_FILTER),
        curveYield);
    ISSUER_DISCOUNT.getMulticurveProvider().setCurve(EUR, curveYield);

    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundles = new LinkedHashMap<>();
    unitMap.put(curveName, Pairs.of(nNodes, nNodes));
    unitBundles.put(curveName, Pairs.of(new CurveBuildingBlock(unitMap), new DoubleMatrix2D(jacobian)));
  }
  
  //  private static final BondFuturesSecurityDefinition BOND_FUTURES = new BondFuturesSecurityDefinition();



  private static double timeCalculator(Tenor maturity) {
    if (maturity.getPeriod().getDays() > 0) {
      return maturity.getPeriod().getDays() / 365.0;
    } else if (maturity.getPeriod().getMonths() > 0) {
      return maturity.getPeriod().getMonths() * 30.0 / 365.0;
    }
    return maturity.getPeriod().getYears();
  }

  private double spreadCalculator(BondFixedSecurity bond, IssuerProviderInterface issuerCurves, double marketPrice,
      int spreadMaxIterations, double spreadTolerance) {
    return 0.0;
  }

  //  private double 
}
