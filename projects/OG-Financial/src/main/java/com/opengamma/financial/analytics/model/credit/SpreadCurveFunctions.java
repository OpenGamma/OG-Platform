/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.standard.PresentValueStandardCreditDefaultSwap;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.standard.PresentValueStandardCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.PointsUpFrontConverter;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.financial.analytics.model.credit.idanew.CDSAnalyticConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class SpreadCurveFunctions {

  //private final static PresentValueCreditDefaultSwap cdsPresentValueCalculator = new PresentValueCreditDefaultSwap();
  private static PresentValueStandardCreditDefaultSwap cdsPresentValueCalculator = new PresentValueStandardCreditDefaultSwap();

  private static final Collection<Tenor> BUCKET_TENORS = new ArrayList<>();
  private static final double s_tenminus4 = 1e-4;
  private static PointsUpFrontConverter PUF_converter = new PointsUpFrontConverter();

  static {
    BUCKET_TENORS.add(Tenor.SIX_MONTHS);
    BUCKET_TENORS.add(Tenor.ONE_YEAR);
    BUCKET_TENORS.add(Tenor.TWO_YEARS);
    BUCKET_TENORS.add(Tenor.THREE_YEARS);
    BUCKET_TENORS.add(Tenor.FOUR_YEARS);
    BUCKET_TENORS.add(Tenor.FIVE_YEARS);
    BUCKET_TENORS.add(Tenor.SIX_YEARS);
    BUCKET_TENORS.add(Tenor.SEVEN_YEARS);
    BUCKET_TENORS.add(Tenor.EIGHT_YEARS);
    BUCKET_TENORS.add(Tenor.NINE_YEARS);
    BUCKET_TENORS.add(Tenor.TEN_YEARS);
    BUCKET_TENORS.add(Tenor.of(Period.ofYears(15)));
    BUCKET_TENORS.add(Tenor.of(Period.ofYears(20)));
    BUCKET_TENORS.add(Tenor.of(Period.ofYears(30)));
  }

  public static final ZonedDateTime[] getIMMDates(final ZonedDateTime now, final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return getDefaultBuckets(now);
    }
    List<ZonedDateTime> dates = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        Tenor tenor = Tenor.of(Period.parse(tenorOrDate));
        dates.add(IMMDateGenerator.getNextIMMDate(now, tenor));
      } else { // date
        LocalDate date = LocalDate.parse(tenorOrDate);
        dates.add(date.atStartOfDay(now.getZone()));
      }
    }
    return dates.toArray(new ZonedDateTime[dates.size()]);
  }

  public static final ZonedDateTime[] getDefaultBuckets(final ZonedDateTime now) {
    final ZonedDateTime[] dates = new ZonedDateTime[BUCKET_TENORS.size()];
    int i = 0;
    for (final Tenor tenor : BUCKET_TENORS) {
      dates[i++] = IMMDateGenerator.getNextIMMDate(now, tenor);
    }
    return dates;
  }


  public static final Tenor[] getBuckets(final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return BUCKET_TENORS.toArray(new Tenor[BUCKET_TENORS.size()]);
    }
    List<Tenor> tenors = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        Tenor tenor = new Tenor(Period.parse(tenorOrDate));
        tenors.add(tenor);
      } else { // date
        throw new OpenGammaRuntimeException("Unsupported");
      }
    }
    return tenors.toArray(new Tenor[tenors.size()]);
  }

  /**
   * Format the spread curve for a given cds.
   * For IMM dates set all spreads to bucket maturity is in.
   * For non-IMM dates take subset of spreads that correspond to buckets.
   *
   * @param cds the cds security
   * @param spreadCurve the spread curve
   * @param bucketDates the bucket dates
   * @param quoteConvention the quote convention (e.g Spread, points upfront etc)
   * @return the spread curve for the given cds
   */
  @Deprecated
  public static double[] getSpreadCurve(final LegacyVanillaCreditDefaultSwapDefinition cds, final NodalTenorDoubleCurve spreadCurve, final ZonedDateTime[] bucketDates,
      final StandardCDSQuotingConvention quoteConvention, final ZonedDateTime valuationDate, final ISDADateCurve isdaCurve, final ZonedDateTime startDate) {
    ArgumentChecker.notNull(spreadCurve, "spread curve");
    ArgumentChecker.notNull(bucketDates, "bucket dates");
    ArgumentChecker.isTrue(spreadCurve.size() > 0, "spread curve had no values");
    final double[] spreads = new double[bucketDates.length];

    // if IMM date take flat spread from imm curve (all values set to single bucket spread)
    if (IMMDateGenerator.isIMMDate((cds.getMaturityDate()))) {
      // find index of bucket this cds maturity is in - should really implement a custom comparator and do a binary search
      Double spreadRate = spreadCurve.getYData()[0];

      for (final Tenor tenor : spreadCurve.getXData()) {
        final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
        if (!bucketDate.isAfter(cds.getMaturityDate())) {
          spreadRate = spreadCurve.getYValue(tenor);
        } else {
          break; // stop when we find desired bucket
        }
      }
      // If IMM and points upfront calculate spread
      switch (quoteConvention) {
        case SPREAD:
          break;
        case POINTS_UPFRONT:
          // can price type vary?
          //FIXME: Conversion to percentage should happen upstream or in analytics
          spreadRate = cdsPresentValueCalculator.calculateParSpreadFlat(valuationDate, cds, spreadRate / 100.0, new ZonedDateTime[] {cds.getMaturityDate() }, isdaCurve, PriceType.CLEAN);
          break;
        default:
          throw new OpenGammaRuntimeException("Unknown quote convention " + quoteConvention);
      }
      // set all spreads to desired spread
      Arrays.fill(spreads, spreadRate.doubleValue() * s_tenminus4);
      return spreads;
    }

    // non-IMM date take spread from subset of dates that we want
    int i = 0;
    for (final Tenor tenor : spreadCurve.getXData()) {
      final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
      final int index = Arrays.binarySearch(bucketDates, bucketDate);
      if (index >= 0) {
        spreads[i++] = spreadCurve.getYValue(tenor) * s_tenminus4;
      }
    }
    // if spread curve ends before required buckets take last spread entry
    for (int j = spreads.length - 1; j >= 0; j--) {
      final double lastspread = spreadCurve.getYData()[spreadCurve.getYData().length - 1] * s_tenminus4;
      if (spreads[j] == 0) {
        spreads[j] = lastspread;
      } else {
        break;
      }
    }
    return spreads;
  }

  /**
   * Format the spread curve for a given cds.
   * For IMM dates set all spreads to bucket maturity is in.
   * For non-IMM dates take subset of spreads that correspond to buckets.
   *
   * Doesn't handle PUF and non-IMM.
   *
   * @param cds the cds security
   * @param spreadCurve the spread curve
   * @param bucketDates the bucket dates
   * @param quoteConvention the quote convention (e.g Spread, points upfront etc)
   * @return the spread curve for the given cds
   */
  public static double[] getSpreadCurve(final LegacyVanillaCreditDefaultSwapDefinition cds, final NodalTenorDoubleCurve spreadCurve, final ZonedDateTime[] bucketDates,
                                        final StandardCDSQuotingConvention quoteConvention, final ZonedDateTime valuationDate, final ISDACompliantYieldCurve isdaCurve, final ZonedDateTime startDate) {
    ArgumentChecker.notNull(spreadCurve, "spread curve");
    ArgumentChecker.notNull(bucketDates, "bucket dates");
    ArgumentChecker.isTrue(spreadCurve.size() > 0, "spread curve had no values");
    final double[] spreads = new double[bucketDates.length];

    // if IMM date take flat spread from imm curve (all values set to single bucket spread)
    if (IMMDateGenerator.isIMMDate((cds.getMaturityDate()))) {
      // find index of bucket this cds maturity is in - should really implement a custom comparator and do a binary search
      Double spreadRate = spreadCurve.getYData()[0];

      for (final Tenor tenor : spreadCurve.getXData()) {
        final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
        if (!bucketDate.isAfter(cds.getMaturityDate())) {
          spreadRate = spreadCurve.getYValue(tenor);
        } else {
          break; // stop when we find desired bucket
        }
      }
      // If IMM and points upfront calculate spread
      switch (quoteConvention) {
        case SPREAD:
          break;
        case POINTS_UPFRONT:
          // can price type vary?
          //FIXME: Conversion to percentage should happen upstream or in analytics
          final CDSAnalytic analytic = CDSAnalyticConverter.create(cds, valuationDate.toLocalDate());
          spreadRate = PUF_converter.parSpread(analytic, cds.getParSpread() * s_tenminus4, isdaCurve, spreadRate / 100.0);
          break;
        default:
          throw new OpenGammaRuntimeException("Unknown quote convention " + quoteConvention);
      }
      // set all spreads to desired spread
      Arrays.fill(spreads, spreadRate.doubleValue() * s_tenminus4);
      return spreads;
    }

    // non-IMM date take spread from subset of dates that we want
    int i = 0;
    for (final Tenor tenor : spreadCurve.getXData()) {
      final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
      final int index = Arrays.binarySearch(bucketDates, bucketDate);
      if (index >= 0) {
        spreads[i++] = spreadCurve.getYValue(tenor) * s_tenminus4;
      }
    }
    // if spread curve ends before required buckets take last spread entry
    for (int j = spreads.length - 1; j >= 0; j--) {
      final double lastspread = spreadCurve.getYData()[spreadCurve.getYData().length - 1] * s_tenminus4;
      if (spreads[j] == 0) {
        spreads[j] = lastspread;
      } else {
        break;
      }
    }
    return spreads;
  }

}
