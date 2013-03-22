/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class SpreadCurveFunctions {

  protected static final Collection<ZonedDateTime> BUCKET_DATES = new ArrayList<>();

  //FIXME: Derive these instead of hardcoding
  static {
    BUCKET_DATES.add(DateUtils.getUTCDate(2013, 9, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2014, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2015, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2016, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2017, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2018, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2019, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2020, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2021, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2022, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2023, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2028, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2033, 3, 20));
    BUCKET_DATES.add(DateUtils.getUTCDate(2043, 3, 20));
  }
  public static final ZonedDateTime[] BUCKET_DATES_ARRAY = BUCKET_DATES.toArray(new ZonedDateTime[BUCKET_DATES.size()]);

  /**
   * Format the spread curve for a given cds.
   * For IMM dates set all spreads to bucket maturity is in.
   * For non-IMM dates take subset of spreads that correspond to buckets.
   *
   * @param cds the cds security
   * @param spreadCurve the spread curve
   * @return the spread curve for the given cds
   */
  public static double[] getSpreadCurve(final LegacyVanillaCreditDefaultSwapDefinition cds, final NodalTenorDoubleCurve spreadCurve, final ZonedDateTime[] bucketDates) {
    ArgumentChecker.notNull(spreadCurve, "spread curve");
    ArgumentChecker.notNull(bucketDates, "bucket dates");
    ArgumentChecker.isTrue(spreadCurve.size() > 0, "spread curve had no values");
    final double[] spreads = new double[bucketDates.length];

    // if IMM date take flat spread from imm curve (all values set to single bucket spread)
    if (IMMDateGenerator.isIMMDate((cds.getMaturityDate()))) {
      // find index of bucket this cds maturity is in - should really implement a custom comparator and do a binary search
      Double spreadRate = Double.valueOf(0.0);
      for (final Tenor tenor : spreadCurve.getXData()) {
        final ZonedDateTime startDate = DateUtils.getUTCDate(cds.getStartDate().getYear(), cds.getStartDate().getMonth().getValue(), cds.getStartDate().getDayOfMonth());
        final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
        if (!bucketDate.isAfter(cds.getMaturityDate())) {
          spreadRate = spreadCurve.getYValue(tenor);
        } else {
          break; // stop when we find desired bucket
        }
      }
      // set all spreads to desired spread
      Arrays.fill(spreads, spreadRate.doubleValue());
      return spreads;
    }

    // non-IMM date take spread from subset of dates that we want
    int i = 0;
    for (final Tenor tenor : spreadCurve.getXData()) {
      final ZonedDateTime startDate = DateUtils.getUTCDate(cds.getStartDate().getYear(), cds.getStartDate().getMonth().getValue(), cds.getStartDate().getDayOfMonth());
      final ZonedDateTime bucketDate = startDate.plus(tenor.getPeriod());
      final int index = Arrays.binarySearch(bucketDates, bucketDate);
      if (index >= 0) {
        spreads[i++] = spreadCurve.getYValue(tenor).doubleValue();
      }
    }
    // if spread curve ends before required buckets take last spread entry
    for (int j = spreads.length - 1; j >= 0; j--) {
      final double lastspread = spreadCurve.getYData()[spreadCurve.getYData().length - 1].doubleValue();
      if (spreads[j] == 0) {
        spreads[j] = lastspread;
      } else {
        break;
      }
    }
    return spreads;
  }

}
