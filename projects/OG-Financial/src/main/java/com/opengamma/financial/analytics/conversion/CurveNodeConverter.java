/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts the curve nodes into instruments, in particular for curve calibration.
 */
public class CurveNodeConverter {

  /**
   * Creates the converter.
   */
  public CurveNodeConverter() {
  }

  /**
   * @param conventionSource the convention source, not required
   * @deprecated use no-arg constructor
   */
  @Deprecated
  public CurveNodeConverter(final ConventionSource conventionSource) {
  }

  /**
   * Given an {@link InstrumentDefinition} (the time-independent form used in the analytics library) and a valuation time, converts to the
   * time-dependent {@link InstrumentDerivative} form.
   * @param node The curve node, not null
   * @param definition The definition, not null
   * @param now The valuation time, not null
   * @param timeSeries A fixing time series, not null if {@link #requiresFixingSeries(CurveNode)} is true and definition is an instance of {@link InstrumentDefinitionWithData}.
   * @return A derivative instrument
   */
  @SuppressWarnings("unchecked")
  public InstrumentDerivative getDerivative(CurveNodeWithIdentifier node, InstrumentDefinition<?> definition,
                                            ZonedDateTime now, HistoricalTimeSeriesBundle timeSeries) {

    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(now, "now");

    if (definition instanceof InstrumentDefinitionWithData<?, ?> && requiresFixingSeries(node.getCurveNode())) {

      if (node.getCurveNode() instanceof ZeroCouponInflationNode) {

        ArgumentChecker.notNull(timeSeries, "time series");
        ExternalId legConvention = ((ZeroCouponInflationNode) node.getCurveNode()).getInflationLegConvention();
        InflationLegConvention inflationLegConvention =
            ConventionLink.resolvable(legConvention, InflationLegConvention.class).resolve();
        ExternalId priceIndexId = inflationLegConvention.getPriceIndexConvention();

        HistoricalTimeSeries historicalTimeSeries = timeSeries.get(node.getDataField(), priceIndexId);
        if (historicalTimeSeries == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + priceIndexId);
        }
        DoubleTimeSeries<?> ts = historicalTimeSeries.getTimeSeries();
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + priceIndexId);
        }
        int length = ts.size();
        if (length == 0) {
          throw new OpenGammaRuntimeException("Price time series for " + priceIndexId + " was empty");
        }
        // the timeseries is multiply by 100 because Bloomberg do not provide the right one
        ZonedDateTimeDoubleTimeSeries multiply =
            convertTimeSeries(ZoneId.of("UTC"), (LocalDateDoubleTimeSeries) ts.multiply(100));
        return ((InstrumentDefinitionWithData<?, ZonedDateTimeDoubleTimeSeries[]>) definition).toDerivative(
            now, new ZonedDateTimeDoubleTimeSeries[] {multiply, multiply});
      }
      if (definition instanceof FederalFundsFutureTransactionDefinition) {
        ArgumentChecker.notNull(timeSeries, "time series");
        RateFutureNode nodeFFF = (RateFutureNode) node.getCurveNode();
        FederalFundsFutureConvention conventionFFF =
            ConventionLink.resolvable(nodeFFF.getFutureConvention(), FederalFundsFutureConvention.class).resolve();
        // Retrieving id of the underlying index.
        HistoricalTimeSeries historicalTimeSeriesUnderlyingIndex =
            timeSeries.get(node.getDataField(), conventionFFF.getIndexConvention());
        if (historicalTimeSeriesUnderlyingIndex == null) {
          throw new OpenGammaRuntimeException(
              "Could not get price time series for " + conventionFFF.getIndexConvention());
        }
        final DoubleTimeSeries<ZonedDateTime>[] tsArray = new DoubleTimeSeries[1];
        tsArray[0] = convertTimeSeries(now.getZone(), historicalTimeSeriesUnderlyingIndex.getTimeSeries());
        // No time series is passed for the closing price; for curve calibration only the trade price is required.
        InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]> definitonInstWithData =  //CSIGNORE
            (InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]>) definition; //CSIGNORE
        return definitonInstWithData.toDerivative(now, tsArray);
      }
      if (node.getCurveNode() instanceof RateFutureNode || node.getCurveNode() instanceof DeliverableSwapFutureNode) {
        return ((InstrumentDefinitionWithData<?, Double>) definition).toDerivative(now, (Double) null);
        // No last closing price is passed; for curve calibration only the trade price is required.
      }
      throw new OpenGammaRuntimeException("Cannot handle swaps with fixings");
    }
    return definition.toDerivative(now);
  }

  public static boolean requiresFixingSeries(CurveNode node) {
    /** Implementation node: fixing series are required for 
        - inflation swaps (starting price index) 
        - Fed Fund futures: underlying overnight index fixing (when fixing month has started) 
        - Ibor swaps (when the UseFixing flag is true)  */
    return node instanceof ZeroCouponInflationNode || node instanceof RateFutureNode;
    // [PLAT-6430] Add case for (SwapNode) node).isUseFixings()
  }

  private static ZonedDateTimeDoubleTimeSeries convertTimeSeries(ZoneId timeZone, LocalDateDoubleTimeSeries localDateTS) {
    // FIXME CASE Converting a daily historical time series to an arbitrary time. Bad idea
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext();) {
      LocalDate date = it.nextTime();
      ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }
}
