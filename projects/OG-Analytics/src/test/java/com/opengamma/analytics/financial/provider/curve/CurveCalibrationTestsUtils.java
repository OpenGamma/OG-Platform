/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.io.FileWriter;
import java.io.IOException;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

public class CurveCalibrationTestsUtils {

  public static void exportIborForwardIborCurve(ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IborIndex index, Calendar cal,
      String fileName, int startIndex, int nbDate, int jump) {
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(calibrationDate, index.getSpotLag() + startIndex * jump, cal);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, index, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().getDayCountFraction(startDate, endDate, cal);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportONForwardONCurve(ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IndexON index, Calendar cal,
      String fileName, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportONForwardIborCurve(ZonedDateTime calibrationDate, MulticurveProviderInterface multicurve, IborIndex index, Calendar cal,
      String fileName, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, cal);
        final double endTime = TimeCalculator.getTimeBetween(calibrationDate, endDate);
        final double accrualFactor = index.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[loopdate] = multicurve.getSimplyCompoundForwardRate(index, startTime[loopdate], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateDsc[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportZCRatesONCurve(ZonedDateTime calibrationDate, MulticurveProviderDiscount multicurve, IndexON index, Calendar cal,
      String fileName, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateZC = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        rateZC[loopdate] = multicurve.getCurve(index).getInterestRate(startTime[loopdate]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateZC[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void exportZCRatesIborCurve(ZonedDateTime calibrationDate, MulticurveProviderDiscount multicurve, IborIndex index, Calendar cal,
      String fileName, int nbDate, int jump) {
    ZonedDateTime startDate = calibrationDate;
    final double[] rateZC = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try {
      final FileWriter writer = new FileWriter(fileName);
      for (int loopdate = 0; loopdate < nbDate; loopdate++) {
        startTime[loopdate] = TimeCalculator.getTimeBetween(calibrationDate, startDate);
        rateZC[loopdate] = multicurve.getCurve(index).getInterestRate(startTime[loopdate]);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, cal);
        writer.append(0.0 + "," + startTime[loopdate] + "," + rateZC[loopdate] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static double initialGuess(final InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    if (instrument instanceof InterestRateFutureTransactionDefinition) {
      return 1 - ((InterestRateFutureTransactionDefinition) instrument).getTradePrice();
    }
    return 0.01;
  }

  public static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedON(final Boolean withToday, final Integer unit,
      ZonedDateTimeDoubleTimeSeries[] htsWithToday, ZonedDateTimeDoubleTimeSeries[] htsWithoutToday) {
    switch (unit) {
      case 0:
        return withToday ? htsWithToday : htsWithoutToday;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  public static ZonedDateTimeDoubleTimeSeries[] getTSSwapFixedIbor(final Boolean withToday, final Integer unit,
      ZonedDateTimeDoubleTimeSeries[] htsWithToday, ZonedDateTimeDoubleTimeSeries[] htsWithoutToday) {
    switch (unit) {
      case 0:
        return withToday ? htsWithToday : htsWithoutToday;
      case 1:
        return withToday ? htsWithToday : htsWithoutToday;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

}
