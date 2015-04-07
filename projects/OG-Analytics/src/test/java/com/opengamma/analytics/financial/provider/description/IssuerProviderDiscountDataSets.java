/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import static com.opengamma.util.money.Currency.EUR;
import static com.opengamma.util.money.Currency.GBP;
import static com.opengamma.util.money.Currency.USD;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.legalentity.CreditRating;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityCombiningFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityCreditRatings;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.legalentity.Region;
import com.opengamma.analytics.financial.legalentity.Sector;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Sets of market data used in tests. With issuers.
 */
public class IssuerProviderDiscountDataSets {
  /** A linear interpolator with flat extrapolation */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  /** Australian government issuer name */
  private static final String AUS_NAME = "AUSTRALIAN GOVT";
  /** Belgian government issuer name */
  private static final String BEL_NAME = "BELGIUM GOVT";
  /** German government issuer name */
  private static final String GER_NAME = "GERMANY GOVT";
  /** ITALY government issuer name */
  private static final String IT_NAME = "IT GOVT";
  /** UK government issuer name */
  private static final String UK_NAME = "UK GOVT";
  /** US government issuer name */
  private static final String US_NAME = "US GOVT";

  /** US government legal entity */
  private static final LegalEntity AUS_GOVT = new LegalEntity(AUS_NAME, AUS_NAME, Sets.newHashSet(CreditRating.of("AAA", "S&P", true)), Sector.of("Government"),
      Region.of("Australia", Country.AU, Currency.AUD));
  /** US government legal entity */
  private static final LegalEntity US_GOVT = new LegalEntity(US_NAME, US_NAME, Sets.newHashSet(CreditRating.of("AA", "S&P", true)), Sector.of("Government"), Region.of("United States", Country.US,
      Currency.USD));
  /** Belgian government legal entity */
  private static final LegalEntity BEL_GOVT = new LegalEntity(BEL_NAME, BEL_NAME, Sets.newHashSet(CreditRating.of("A", "S&P", true)), Sector.of("Government"), Region.of("Belgium", Country.BE,
      Currency.EUR));
  /** German government legal entity */
  private static final LegalEntity GER_GOVT = new LegalEntity(GER_NAME, GER_NAME, Sets.newHashSet(CreditRating.of("AAA", "S&P", true)), Sector.of("Government"), Region.of("Germany", Country.DE,
      Currency.EUR));
  /** UK government legal entity */
  private static final LegalEntity UK_GOVT = new LegalEntity(UK_NAME, UK_NAME, Sets.newHashSet(CreditRating.of("B", "S&P", true)), Sector.of("Government"), Region.of("Great Britain", Country.GB,
      Currency.GBP));
  /** Italy government legal entity */
  private static final LegalEntity IT_GOVT = new LegalEntity(IT_NAME, IT_NAME, Sets.newHashSet(CreditRating.of("B", "S&P", true)), Sector.of("Government"), Region.of("Italy", Country.IT,
      Currency.EUR));

  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final IborIndex USDLIBOR1M = MASTER_IBOR_INDEX.getIndex("USDLIBOR1M");

  private static final double[] USD_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_DSC_RATE = new double[] {0.0120, 0.0120, 0.0120, 0.0140, 0.0140, 0.0140 };
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, USD_DSC_NAME));
  private static final double[] USD_FWD1_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0 };
  private static final double[] USD_FWD1_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0175, 0.0190, 0.0200, 0.0210 };
  private static final String USD_FWD1_NAME = "USD LIBOR 3M";
  private static final YieldAndDiscountCurve USD_FWD1 = new YieldCurve(USD_FWD1_NAME, new InterpolatedDoublesCurve(USD_FWD1_TIME, USD_FWD1_RATE, LINEAR_FLAT, true, USD_FWD1_NAME));

  private static final double[] EUR_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(EUR_DSC_TIME, EUR_DSC_RATE, LINEAR_FLAT, true, EUR_DSC_NAME));
  private static final double[] EUR_FWD3_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0 };
  private static final double[] EUR_FWD3_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0175, 0.0190, 0.0200, 0.0210 };
  private static final String EUR_FWD3_NAME = "EUR EURIBOR 3M";
  private static final YieldAndDiscountCurve EUR_FWD3 = new YieldCurve(EUR_FWD3_NAME, new InterpolatedDoublesCurve(EUR_FWD3_TIME, EUR_FWD3_RATE, LINEAR_FLAT, true, EUR_FWD3_NAME));

  private static final double[] GBP_DSC_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] GBP_DSC_RATE = new double[] {0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150 };
  private static final String GBP_DSC_NAME = "GBP Dsc";
  private static final YieldAndDiscountCurve GBP_DSC = new YieldCurve(GBP_DSC_NAME, new InterpolatedDoublesCurve(GBP_DSC_TIME, GBP_DSC_RATE, LINEAR_FLAT, true, GBP_DSC_NAME));

  private static final double[] USD_US_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] USD_US_RATE = new double[] {0.0100, 0.0100, 0.0100, 0.0120, 0.0120, 0.0120 };
  private static final String USD_US_CURVE_NAME = "USD " + US_NAME;
  private static final YieldAndDiscountCurve US_USD_CURVE = new YieldCurve(USD_US_CURVE_NAME, new InterpolatedDoublesCurve(USD_US_TIME, USD_US_RATE, LINEAR_FLAT, true, USD_US_CURVE_NAME));
  private static final YieldAndDiscountCurve US_USD_CURVE_6 = new YieldCurve(USD_US_CURVE_NAME, new ConstantDoublesCurve(0.06, USD_US_CURVE_NAME));

  private static final double[] EUR_BEL_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_BEL_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String EUR_BEL_CURVE_NAME = "EUR " + BEL_NAME;
  private static final YieldAndDiscountCurve BEL_EUR_CURVE = new YieldCurve(EUR_BEL_CURVE_NAME, 
      new InterpolatedDoublesCurve(EUR_BEL_TIME, EUR_BEL_RATE, LINEAR_FLAT, true, EUR_BEL_CURVE_NAME));

  private static final double[] EUR_GER_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] EUR_GER_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String GER_EUR_CURVE_NAME = "EUR " + GER_NAME;
  private static final YieldAndDiscountCurve GER_EUR_CURVE = new YieldCurve(GER_EUR_CURVE_NAME, 
      new InterpolatedDoublesCurve(EUR_GER_TIME, EUR_GER_RATE, LINEAR_FLAT, true, GER_EUR_CURVE_NAME));

  private static final double[] UK_GBP_TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0 };
  private static final double[] UK_GBP_RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250 };
  private static final String UK_GBP_CURVE_NAME = "GBP " + UK_NAME;
  private static final YieldAndDiscountCurve UK_GBP_CURVE = new YieldCurve(UK_GBP_CURVE_NAME, 
      new InterpolatedDoublesCurve(UK_GBP_TIME, UK_GBP_RATE, LINEAR_FLAT, true, UK_GBP_CURVE_NAME));
  // AUD
  private static final String AUD_DSC_NAME = "AUD Dsc";
  private static final YieldAndDiscountCurve AUD_DSC = new YieldCurve(AUD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME, USD_DSC_RATE, LINEAR_FLAT, true, AUD_DSC_NAME));
  private static final String AUS_AUD_CURVE_NAME = "EUR " + AUS_NAME;
  private static final YieldAndDiscountCurve AUS_AUD_CURVE = new YieldCurve(AUS_AUD_CURVE_NAME, new InterpolatedDoublesCurve(EUR_GER_TIME, EUR_GER_RATE, LINEAR_FLAT, true, AUS_AUD_CURVE_NAME));

  /** Extracts the short name (i.e. issuer name) from a legal entity */
  private static final LegalEntityFilter<LegalEntity> SHORT_NAME_FILTER = new LegalEntityShortName();
  /** A set of discounting curves for EUR, USD and GBP */
  private static final MulticurveProviderDiscount DISCOUNTING_CURVES = new MulticurveProviderDiscount();
  static {
    DISCOUNTING_CURVES.setCurve(USD, USD_DSC);
    DISCOUNTING_CURVES.setCurve(USDLIBOR1M, USD_FWD1);
    DISCOUNTING_CURVES.setCurve(EUR, EUR_DSC);
    DISCOUNTING_CURVES.setCurve(EURIBOR3M, EUR_FWD3);
    DISCOUNTING_CURVES.setCurve(GBP, GBP_DSC);
  }
  /** A set of issuer-specific curves for US GOVT, BELGIUM GOVT, GERMANY GOVT and UK GOVT */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> ISSUER_SPECIFIC = new LinkedHashMap<>();
  static {
    ISSUER_SPECIFIC.put(Pairs.of((Object) US_NAME, SHORT_NAME_FILTER), US_USD_CURVE);
    ISSUER_SPECIFIC.put(Pairs.of((Object) BEL_NAME, SHORT_NAME_FILTER), BEL_EUR_CURVE);
    ISSUER_SPECIFIC.put(Pairs.of((Object) GER_NAME, SHORT_NAME_FILTER), GER_EUR_CURVE);
    ISSUER_SPECIFIC.put(Pairs.of((Object) UK_NAME, SHORT_NAME_FILTER), UK_GBP_CURVE);
    ISSUER_SPECIFIC.put(Pairs.of((Object) IT_NAME, SHORT_NAME_FILTER), BEL_EUR_CURVE);
  }
  private static final MulticurveProviderDiscount DISCOUNTING_CURVES_AUD = new MulticurveProviderDiscount();
  static {
    DISCOUNTING_CURVES_AUD.setCurve(Currency.AUD, AUD_DSC);
  }
  /** A set of issuer-specific curves for AUS */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> ISSUER_SPECIFIC_AUS = new LinkedHashMap<>();
  static {
    ISSUER_SPECIFIC_AUS.put(Pairs.of((Object) AUS_NAME, SHORT_NAME_FILTER), AUS_AUD_CURVE);
  }
  /** Extracts the country from a legal entity */
  private static final LegalEntityRegion COUNTRY_FILTER;
  /** A set of country-specific curves for US, DE, UK and GB */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> COUNTRY_SPECIFIC = new LinkedHashMap<>();
  static {
    COUNTRY_FILTER = new LegalEntityRegion();
    COUNTRY_FILTER.setUseCountry(true);
    COUNTRY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(Country.US), (LegalEntityFilter<LegalEntity>) COUNTRY_FILTER), US_USD_CURVE);
    COUNTRY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(Country.BE), (LegalEntityFilter<LegalEntity>) COUNTRY_FILTER), BEL_EUR_CURVE);
    COUNTRY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(Country.DE), (LegalEntityFilter<LegalEntity>) COUNTRY_FILTER), GER_EUR_CURVE);
    COUNTRY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(Country.GB), (LegalEntityFilter<LegalEntity>) COUNTRY_FILTER), UK_GBP_CURVE);
    COUNTRY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(Country.IT), (LegalEntityFilter<LegalEntity>) COUNTRY_FILTER), BEL_EUR_CURVE);
  }
  /** Extracts the currency from a legal entity */
  private static final LegalEntityRegion CURRENCY_FILTER;
  /** A set of currency-specific curves for USD, EUR and GBP */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> CURRENCY_SPECIFIC = new LinkedHashMap<>();
  static {
    CURRENCY_FILTER = new LegalEntityRegion();
    CURRENCY_FILTER.setUseCurrency(true);
    CURRENCY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(USD), (LegalEntityFilter<LegalEntity>) CURRENCY_FILTER), US_USD_CURVE);
    CURRENCY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(EUR), (LegalEntityFilter<LegalEntity>) CURRENCY_FILTER), GER_EUR_CURVE);
    CURRENCY_SPECIFIC.put(Pairs.of((Object) Collections.singleton(GBP), (LegalEntityFilter<LegalEntity>) CURRENCY_FILTER), UK_GBP_CURVE);
  }
  /** Extracts the country and rating from a legal entity */
  private static final LegalEntityCombiningFilter COUNTRY_RATING_FILTER;
  /** A set of country and rating-specific curves for US, BE, DE and GB */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> COUNTRY_RATING_SPECIFIC = new LinkedHashMap<>();
  static {
    COUNTRY_RATING_FILTER = new LegalEntityCombiningFilter();
    final LegalEntityCreditRatings ratingsFilter = new LegalEntityCreditRatings();
    ratingsFilter.setUseRating(true);
    final Set<LegalEntityFilter<LegalEntity>> underlyingFilters = new HashSet<>();
    underlyingFilters.add(COUNTRY_FILTER);
    underlyingFilters.add(ratingsFilter);
    COUNTRY_RATING_FILTER.setFiltersToUse(underlyingFilters);
    final Set<Object> us = new HashSet<>();
    us.add(Country.US);
    us.add(Pairs.of("S&P", "AA"));
    final Set<Object> be = new HashSet<>();
    be.add(Country.BE);
    be.add(Pairs.of("S&P", "A"));
    final Set<Object> de = new HashSet<>();
    de.add(Country.DE);
    de.add(Pairs.of("S&P", "AAA"));
    final Set<Object> gb = new HashSet<>();
    gb.add(Country.GB);
    gb.add(Pairs.of("S&P", "B"));
    final Set<Object> it = new HashSet<>();
    gb.add(Country.IT);
    gb.add(Pairs.of("S&P", "B"));
    COUNTRY_RATING_SPECIFIC.put(Pairs.of((Object) us, (LegalEntityFilter<LegalEntity>) COUNTRY_RATING_FILTER), US_USD_CURVE);
    COUNTRY_RATING_SPECIFIC.put(Pairs.of((Object) be, (LegalEntityFilter<LegalEntity>) COUNTRY_RATING_FILTER), BEL_EUR_CURVE);
    COUNTRY_RATING_SPECIFIC.put(Pairs.of((Object) de, (LegalEntityFilter<LegalEntity>) COUNTRY_RATING_FILTER), GER_EUR_CURVE);
    COUNTRY_RATING_SPECIFIC.put(Pairs.of((Object) gb, (LegalEntityFilter<LegalEntity>) COUNTRY_RATING_FILTER), UK_GBP_CURVE);
    COUNTRY_RATING_SPECIFIC.put(Pairs.of((Object) it, (LegalEntityFilter<LegalEntity>) COUNTRY_RATING_FILTER), BEL_EUR_CURVE);
  }

  /** US GOVT curve with constant 6% rate */
  private static final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> USD_GOVT_6PC = new LinkedHashMap<>();
  static {
    USD_GOVT_6PC.put(Pairs.of((Object) US_NAME, SHORT_NAME_FILTER), US_USD_CURVE_6);
  }
  /** Curves for pricing bonds with issuer-specific risky curves */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVE = new IssuerProviderDiscount(DISCOUNTING_CURVES, ISSUER_SPECIFIC);
  /** Curves for pricing bonds with country-specific risky curves */
  private static final IssuerProviderDiscount COUNTRY_SPECIFIC_MULTICURVE = new IssuerProviderDiscount(DISCOUNTING_CURVES, COUNTRY_SPECIFIC);
  /** Curves for pricing bonds with currency-specific risky curves */
  private static final IssuerProviderDiscount CURRENCY_SPECIFIC_MULTICURVE = new IssuerProviderDiscount(DISCOUNTING_CURVES, CURRENCY_SPECIFIC);
  /** Curves for pricing bonds with country and rating-specific risky curves */
  private static final IssuerProviderDiscount COUNTRY_RATING_SPECIFIC_MULTICURVE = new IssuerProviderDiscount(DISCOUNTING_CURVES, COUNTRY_RATING_SPECIFIC);
  /** Curves for pricing bonds with issuer-specific risky curves AUS*/
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVE_AUS = new IssuerProviderDiscount(DISCOUNTING_CURVES_AUD, ISSUER_SPECIFIC_AUS);

  /**
   * Returns a multi-curves provider with three discounting currencies (USD, EUR, GBP), one Ibor (EURIBOR3M) and five issuers
   * (US GOVT, BELGIUM GOVT, GERMAN GOVT, UK GOVT, ITALY GOVT).
   * @return The provider.
   */
  public static IssuerProviderDiscount getIssuerSpecificProvider() {
    return ISSUER_SPECIFIC_MULTICURVE;
  }

  /**
   * Returns a multi-curves provider with a discounting currency (AUD) and an issuers (AUS Govt).
   * @return The provider.
   */
  public static IssuerProviderDiscount getIssuerSpecificProviderAus() {
    return ISSUER_SPECIFIC_MULTICURVE_AUS;
  }

  /**
   * Returns a multi-curves provider with three discounting currencies (USD, EUR, GBP), one Ibor (EURIBOR3M) and four countries
   * (US, BE, DE, UK, IT)
   * @return The provider
   */
  public static IssuerProviderDiscount getCountrySpecificProvider() {
    return COUNTRY_SPECIFIC_MULTICURVE;
  }

  /**
   * Returns a multi-curves provider with three discounting currencies (USD, EUR, GBP) and one Ibor (EURIBOR3M)
   * @return The provider
   */
  public static IssuerProviderDiscount getCurrencySpecificProvider() {
    return CURRENCY_SPECIFIC_MULTICURVE;
  }

  /**
   * Returns a multi-curves provider with three discounting currencies (USD, EUR, GBP) and one Ibor (EURIBOR3M) and
   * a combined rating for S&P and Moody's of AA for each country.
   * @return The provider
   */
  public static IssuerProviderDiscount getCountryRatingSpecificProvider() {
    return COUNTRY_RATING_SPECIFIC_MULTICURVE;
  }

  /**
   * Returns a multi-curves provider with one currency (USD) and one issuer (US GOVT). The issuer curve is at 6% (useful for futures).
   * @return The provider.
   */
  public static IssuerProviderDiscount createIssuerProvider6() {
    return new IssuerProviderDiscount(DISCOUNTING_CURVES, USD_GOVT_6PC);
  }

  /**
   * Gets the issuer names.
   * @return The issuer names
   */
  public static String[] getIssuerNames() {
    return new String[] {US_NAME, BEL_NAME, GER_NAME, UK_NAME, IT_NAME };
  }

  /**
   * Gets the issuers.
   * @return The issuers
   */
  public static LegalEntity[] getIssuers() {
    return new LegalEntity[] {US_GOVT, BEL_GOVT, GER_GOVT, UK_GOVT, IT_GOVT };
  }

  /**
   * Gets the issuers Australia
   * @return The issuers AUS
   */
  public static LegalEntity getIssuersAUS() {
    return AUS_GOVT;
  }

}
