/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/** Realistic data as of May 2015 for option on bond futures - JGB. */
public class BondFuturesOptionPremiumE2EDataSet {

  private static final Currency JPY = Currency.JPY;
  public static final double JBH5_PRICE = 1.480;
  public static final double JBU5_PRICE = 1.465;
  /** US government issuer name */
  private static final String JP_NAME = "JP GOVT";
  /** A linear interpolator with flat extrapolation */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  
  private static final String JP_CURVE_GOVT_NAME = "JPY-JP-GOVT";
  private static final double[] JP_CURVE_GOVT_TIME =
      new double[] {0.5, 1.0, 2.0, 5.0, 7.0, 10.0, 20.0 };
  private static final double[] JP_CURVE_GOVT_ZCRATE =
      new double[] {-0.0001, -0.0002, -0.0002, 0.0012, 0.0030, 0.0045, 0.0115 };
  private static final InterpolatedDoublesCurve JP_CURVE_GOVT_INT =
      new InterpolatedDoublesCurve(JP_CURVE_GOVT_TIME, JP_CURVE_GOVT_ZCRATE, LINEAR_FLAT, true, JP_CURVE_GOVT_NAME);
  private static final YieldAndDiscountCurve JPY_CURVE_GOVT = new YieldCurve(JP_CURVE_GOVT_NAME, JP_CURVE_GOVT_INT);
  
  private static final String JP_CURVE_DSC_NAME = "JPY-DSCON";
  private static final double[] JP_CURVE_DSC_TIME =
      new double[] {0.003, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0};
  private static final double[] JP_CURVE_DSC_ZCRATE =
      new double[] {0.0006, 0.0006, 0.0006, 0.0006, 0.0007, 0.0020, 0.0050 };
  private static final InterpolatedDoublesCurve JP_CURVE_DSC_INT =
      new InterpolatedDoublesCurve(JP_CURVE_DSC_TIME, JP_CURVE_DSC_ZCRATE, LINEAR_FLAT, true, JP_CURVE_DSC_NAME);
  private static final YieldAndDiscountCurve JPY_CURVE_DSC = new YieldCurve(JP_CURVE_DSC_NAME, JP_CURVE_DSC_INT);
  
  /** Issuer Provider for JPY */
  private static final MulticurveProviderDiscount DISCOUNTING_CURVES_JPY = new MulticurveProviderDiscount();
  static {
    DISCOUNTING_CURVES_JPY.setCurve(JPY, JPY_CURVE_DSC);
  }
  /** A set of issuer-specific curves for JP GOVT */
  /** Extracts the short name (i.e. issuer name) from a legal entity */
  private static final LegalEntityFilter<LegalEntity> SHORT_NAME_FILTER = new LegalEntityShortName();
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> ISSUER_SPECIFIC_JP = 
      new LinkedHashMap<>();
  static {
    ISSUER_SPECIFIC_JP.put(Pairs.of((Object) JP_NAME, SHORT_NAME_FILTER), JPY_CURVE_GOVT);
  }
  /** Curves for pricing bonds with issuer-specific risky curves */
  public static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVE_JP = 
      new IssuerProviderDiscount(DISCOUNTING_CURVES_JPY, ISSUER_SPECIFIC_JP);
  /** Japan government legal entity */
  public static final LegalEntity JP_GOVT = new LegalEntity(JP_NAME, JP_NAME, 
      Sets.newHashSet(CreditRating.of("B", "S&P", true)), Sector.of("Government"), Region.of("Japan", Country.JP,
      Currency.JPY));
  
  private static final double[] EXPIRY_OPT_BNDFUT = new double[] {
    19.0/365.0, 19.0/365.0, 19.0/365.0, 19.0/365.0, 
    49.0/365.0, 49.0/365.0, 49.0/365.0, 49.0/365.0 };
  private static final double[] STRIKE_OPT_BNDFUT = new double[] {
    1.45, 1.46, 1.47, 1.48, 1.45, 1.46, 1.47, 1.48 };
  private static final double[] VOL_OPTBND_EXP_STRIKE = new double[] {
    0.035, 0.032, 0.031, 0.028, 0.0325, 0.0315, 0.0305, 0.0295 };
  public static final InterpolatedDoublesSurface BLACK_SURFACE_BND_EXP_STRIKE = InterpolatedDoublesSurface
      .from(EXPIRY_OPT_BNDFUT, STRIKE_OPT_BNDFUT, VOL_OPTBND_EXP_STRIKE, INTERPOLATOR_LINEAR_2D);
  
}
