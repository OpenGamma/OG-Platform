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

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.StandardCDSQuotingConvention;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSQuoteConvention;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ParSpread;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.QuotedSpread;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.financial.analytics.model.credit.isdanew.CDSAnalyticConverter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class SpreadCurveFunctions {

  //private final static PresentValueCreditDefaultSwap cdsPresentValueCalculator = new PresentValueCreditDefaultSwap();
 // private static PresentValueStandardCreditDefaultSwap cdsPresentValueCalculator = new PresentValueStandardCreditDefaultSwap();

  private static final Collection<Tenor> BUCKET_TENORS = new ArrayList<>();
  private static final double s_tenminus4 = 1e-4;
  private static final double s_tenminus2 = 1e-2;
  private static MarketQuoteConverter PUF_converter = new MarketQuoteConverter();

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

  public static final ZonedDateTime[] getPillarDates(final ZonedDateTime now, final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return getDefaultBuckets(now);
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        final Tenor tenor = Tenor.of(Period.parse(tenorOrDate));
        dates.add(IMMDateGenerator.getNextIMMDate(now, tenor));
      } else { // date
        final LocalDate date = LocalDate.parse(tenorOrDate);
        dates.add(date.atStartOfDay(now.getZone()));
      }
    }
    return dates.toArray(new ZonedDateTime[dates.size()]);
  }

  public static final ZonedDateTime[] getPillarDatesFromBucketStart(final ZonedDateTime bucketStart, final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return getDefaultBucketsFromBucketStart(bucketStart);
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        final Tenor tenor = Tenor.of(Period.parse(tenorOrDate));
        dates.add(bucketStart.plus(tenor.getPeriod()));
      } else { // date
        final LocalDate date = LocalDate.parse(tenorOrDate);
        dates.add(date.atStartOfDay(bucketStart.getZone()));
      }
    }
    return dates.toArray(new ZonedDateTime[dates.size()]);
  }

  public static final ZonedDateTime[] getPillarDatesNoAdjustment(final ZonedDateTime now, final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return getDefaultBuckets(now);
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        final Tenor tenor = Tenor.of(Period.parse(tenorOrDate));
        dates.add(IMMDateGenerator.getNextIMMDateNoAdjustment(now, tenor));
      } else { // date
        final LocalDate date = LocalDate.parse(tenorOrDate);
        dates.add(date.atStartOfDay(now.getZone()));
      }
    }
    return dates.toArray(new ZonedDateTime[dates.size()]);
  }

  public static final ZonedDateTime[] getPillarDates(final ZonedDateTime now, final Tenor[] tenors) {
    final ZonedDateTime[] dates = new ZonedDateTime[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      dates[i] = IMMDateGenerator.getNextIMMDate(now, tenors[i]);
    }
    return dates;
  }

  public static final ZonedDateTime[] getPillarDatesFromBucketStart(final ZonedDateTime bucketStart, final Tenor[] tenors) {
    final ZonedDateTime[] dates = new ZonedDateTime[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      dates[i] = bucketStart.plus(tenors[i].getPeriod());
    }
    return dates;
  }

  public static final ZonedDateTime[] getPillarDatesNoAdjustment(final ZonedDateTime now, final Tenor[] tenors) {
    final ZonedDateTime[] dates = new ZonedDateTime[tenors.length];
    for (int i = 0; i < tenors.length; i++) {
      dates[i] = IMMDateGenerator.getNextIMMDateNoAdjustment(now, tenors[i]);
    }
    return dates;
  }

  public static final ZonedDateTime[] getDefaultBuckets(final ZonedDateTime now) {
    final ZonedDateTime[] dates = new ZonedDateTime[BUCKET_TENORS.size()];
    int i = 0;
    for (final Tenor tenor : BUCKET_TENORS) {
      dates[i++] = IMMDateGenerator.getNextIMMDate(now, tenor);
    }
    return dates;
  }

  public static final ZonedDateTime[] getDefaultBucketsFromBucketStart(final ZonedDateTime bucketStart) {
    final ZonedDateTime[] dates = new ZonedDateTime[BUCKET_TENORS.size()];
    int i = 0;
    for (final Tenor tenor : BUCKET_TENORS) {
      dates[i++] = bucketStart.plus(tenor.getPeriod());
    }
    return dates;
  }

  public static final Tenor[] getBuckets(final String inputs) {
    if (inputs == null || inputs.isEmpty()) {
      return BUCKET_TENORS.toArray(new Tenor[BUCKET_TENORS.size()]);
    }
    final List<Tenor> tenors = new ArrayList<>();
    for (final String tenorOrDate : inputs.split(",")) {
      if (tenorOrDate.startsWith("P")) { // tenor
        final Tenor tenor = new Tenor(Period.parse(tenorOrDate));
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
   * Doesn't handle PUF and non-IMM.
   *
   * @param cds the cds security
   * @param spreadCurve the spread curve
   * @param bucketDates the bucket dates
   * @param quoteConvention the quote convention (e.g Spread, points upfront etc)
   * @return the spread curve for the given cds
   */
  @Deprecated
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
          spreadRate = PUF_converter.pufToQuotedSpread(analytic, cds.getParSpread() * s_tenminus4, isdaCurve, spreadRate / 100.0);
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
   * Get spread curve for the given tenors
   *
   * @param spreadCurve the spread curve
   * @param bucketDates the bucket dates
   * @param quoteConvention the convention, spread or puf
   * @return the spread curve
   */
  public static double[] getSpreadCurveNew(final NodalTenorDoubleCurve spreadCurve, final ZonedDateTime[] bucketDates,
                                           final ZonedDateTime startDate, final StandardCDSQuotingConvention quoteConvention) {
    ArgumentChecker.notNull(spreadCurve, "spread curve");
    ArgumentChecker.notNull(bucketDates, "bucket dates");
    ArgumentChecker.isTrue(spreadCurve.size() > 0, "spread curve had no values");
    final double[] spreads = new double[bucketDates.length];
    // PUF normalised to 1%, spreads to 1 BP
    final double multiplier = StandardCDSQuotingConvention.POINTS_UPFRONT.equals(quoteConvention) ? s_tenminus2 : s_tenminus4;

    // take spreads from subset of dates that we want
    int i = 0;
    for (final Tenor tenor : spreadCurve.getXData()) {
      final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
      final int index = Arrays.binarySearch(bucketDates, bucketDate);
      if (index >= 0) {
        spreads[i++] = spreadCurve.getYValue(tenor) * multiplier;
      }
    }
    // if spread curve ends before required buckets take last spread entry
    for (int j = spreads.length - 1; j >= 0; j--) {
      final double lastspread = spreadCurve.getYData()[spreadCurve.getYData().length - 1] * multiplier;
      if (spreads[j] == 0) {
        spreads[j] = lastspread;
      } else {
        break;
      }
    }
    return spreads;
  }

  /**
   * Get analytic quote objects for the spread / credit curve.
   * If priced cds matures on an IMM date set spreads to quoted otherwise take them as par.
   *
   * If quoteConvention is SPREAD, set quoted vs. par spread based on IMM maturity
   *
   * @param pricedCDSMaturity the maturity of the priced cds
   * @param values the quotes
   * @param quoteConvention the quote convention e.g. SPREAD or PUF
   * @param normalise control whether to normalise spreads to fractional vlaues
   * @return the cds quote conventions
   */
  public static CDSQuoteConvention[] getQuotes(final ZonedDateTime pricedCDSMaturity, final double[] values, final double coupon, final StandardCDSQuotingConvention quoteConvention, final boolean normalise) {
    final CDSQuoteConvention[] result = new CDSQuoteConvention[values.length];
    // PUF normalised to 1%, spreads to 1 BP
    double multiplier = 1;
    if (normalise) {
      multiplier = StandardCDSQuotingConvention.POINTS_UPFRONT.equals(quoteConvention) ? s_tenminus2 : s_tenminus4;
    }
    for (int i = 0; i < values.length; i++) {
      if (StandardCDSQuotingConvention.SPREAD.equals(quoteConvention)) {
        result[i] = IMMDateGenerator.isIMMDate(pricedCDSMaturity) ? new QuotedSpread(coupon * s_tenminus4, values[i] * multiplier) : new ParSpread(values[i] * multiplier);
      } else if (StandardCDSQuotingConvention.POINTS_UPFRONT.equals(quoteConvention)) {
        result[i] = new PointsUpFront(coupon * s_tenminus4, values[i] * multiplier);
      } else {
        throw new OpenGammaRuntimeException("Unsupported quote type: " + quoteConvention);
      }
    }
    return result;
  }

}
