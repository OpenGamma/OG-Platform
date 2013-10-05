/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import static com.opengamma.financial.analytics.curve.NodeConverterUtils.getFixedLeg;
import static com.opengamma.financial.analytics.curve.NodeConverterUtils.getIborCompoundingLeg;
import static com.opengamma.financial.analytics.curve.NodeConverterUtils.getIborLeg;
import static com.opengamma.financial.analytics.curve.NodeConverterUtils.getOISLeg;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Convert a swap node into an Instrument definition.
 * The dates of the swap are computed in the following way:
 * - The spot date is computed from the valuation date adding the "Settlement Days" (i.e. the number of business days) of the convention.
 * - The start date is computed from the spot date adding the "StartTenor" of the node and using the business-day-convention, calendar and EOM of the convention.
 * - The end date is computed from the start date adding the "MaturityTenor" of the node and using Annuity constructor.
 * The swap notional for each leg is 1.
 * A fixed leg always has the market quote as fixed rate.
 * If both legs are floating (VanillaIborLegConvention or OISLegConvention), the receive leg has a spread equal to the market quote.
 */
public class SwapNodeConverter extends CurveNodeVisitorAdapter<InstrumentDefinition<?>> {
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;

  /**
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public SwapNodeConverter(final ConventionSource conventionSource, final HolidaySource holidaySource, final RegionSource regionSource,
      final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    ArgumentChecker.notNull(conventionSource, "convention source");
    ArgumentChecker.notNull(holidaySource, "holiday source");
    ArgumentChecker.notNull(regionSource, "region source");
    ArgumentChecker.notNull(marketData, "market data");
    ArgumentChecker.notNull(dataId, "data id");
    ArgumentChecker.notNull(valuationTime, "valuation time");
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
  }

  @Override
  public InstrumentDefinition<?> visitSwapNode(final SwapNode swapNode) {
    final Convention payLegConvention = _conventionSource.getConvention(swapNode.getPayLegConvention());
    if (payLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final Convention receiveLegConvention = _conventionSource.getConvention(swapNode.getReceiveLegConvention());
    if (receiveLegConvention == null) {
      throw new OpenGammaRuntimeException("Convention with id " + swapNode.getPayLegConvention() + " was null");
    }
    final AnnuityDefinition<? extends PaymentDefinition> payLeg;
    final AnnuityDefinition<? extends PaymentDefinition> receiveLeg;
    final boolean isFloatFloat = ((payLegConvention instanceof VanillaIborLegConvention) || (payLegConvention instanceof OISLegConvention) ||
        (payLegConvention instanceof CompoundingIborLegConvention))
        &&  ((receiveLegConvention instanceof VanillaIborLegConvention) || (receiveLegConvention instanceof OISLegConvention) ||
            (receiveLegConvention instanceof CompoundingIborLegConvention));
    final Period startTenor = swapNode.getStartTenor().getPeriod();
    final Period maturityTenor = swapNode.getMaturityTenor().getPeriod();
    if (payLegConvention instanceof SwapFixedLegConvention) {
      final SwapFixedLegConvention fixedLegConvention = (SwapFixedLegConvention) payLegConvention;
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fixedLegConvention.getRegionCalendar());
      payLeg = getFixedLeg(fixedLegConvention, startTenor, maturityTenor, true, calendar, _marketData, _dataId, _valuationTime);
    } else if (payLegConvention instanceof VanillaIborLegConvention) {
      final VanillaIborLegConvention iborLegConvention = (VanillaIborLegConvention) payLegConvention;
      final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      payLeg = getIborLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, _marketData, _dataId, _valuationTime);
    } else if (payLegConvention instanceof OISLegConvention) {
      final OISLegConvention oisLegConvention = (OISLegConvention) payLegConvention;
      final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, oisLegConvention.getOvernightIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      payLeg = getOISLeg(oisLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, _marketData, _dataId, _valuationTime);
    } else if (payLegConvention instanceof CompoundingIborLegConvention) {
      final CompoundingIborLegConvention iborLegConvention = (CompoundingIborLegConvention) payLegConvention;
      final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      payLeg = getIborCompoundingLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, true, calendar, false, _marketData, _dataId, _valuationTime);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + payLegConvention.getClass());
    }
    if (receiveLegConvention instanceof SwapFixedLegConvention) {
      final SwapFixedLegConvention fixedLegConvention = (SwapFixedLegConvention) receiveLegConvention;
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fixedLegConvention.getRegionCalendar());
      receiveLeg = getFixedLeg(fixedLegConvention, startTenor, maturityTenor, false, calendar, _marketData, _dataId, _valuationTime);
    } else if (receiveLegConvention instanceof VanillaIborLegConvention) {
      final VanillaIborLegConvention iborLegConvention = (VanillaIborLegConvention) receiveLegConvention;
      final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getIborLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, _marketData, _dataId, _valuationTime);
    } else if (receiveLegConvention instanceof OISLegConvention) {
      final OISLegConvention oisLegConvention = (OISLegConvention) receiveLegConvention;
      final OvernightIndexConvention indexConvention = _conventionSource.getConvention(OvernightIndexConvention.class, oisLegConvention.getOvernightIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getOISLeg(oisLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, _marketData, _dataId, _valuationTime);
    } else if (receiveLegConvention instanceof CompoundingIborLegConvention) {
      final CompoundingIborLegConvention iborLegConvention = (CompoundingIborLegConvention) receiveLegConvention;
      final IborIndexConvention indexConvention = _conventionSource.getConvention(IborIndexConvention.class, iborLegConvention.getIborIndexConvention());
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      receiveLeg = getIborCompoundingLeg(iborLegConvention, indexConvention, startTenor, maturityTenor, false, calendar, isFloatFloat, _marketData, _dataId, _valuationTime);
    } else {
      throw new OpenGammaRuntimeException("Cannot handle convention type " + receiveLegConvention.getClass());
    }
    return new SwapDefinition(payLeg, receiveLeg);
  }
}
