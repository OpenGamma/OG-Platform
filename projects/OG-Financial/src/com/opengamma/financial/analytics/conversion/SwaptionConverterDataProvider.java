/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class SwaptionConverterDataProvider {
  //private final DefinitionConverterDataProvider _swapConverter;

  public SwaptionConverterDataProvider(final String dataSourceName, final String fieldName, final String dataProvider) {
    Validate.notNull(dataSourceName, "data source name");
    Validate.notNull(fieldName, "field name");
    Validate.notNull(dataProvider, "data provider");
    //_swapConverter = new DefinitionConverterDataProvider(dataSourceName, fieldName, dataProvider);
  }

  @SuppressWarnings("unused")
  public InstrumentDerivative convert(final SwaptionSecurity security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesSource dataSource) {
    if (definition instanceof SwaptionCashFixedIborDefinition) {
      final SwaptionCashFixedIborDefinition cashSettled = (SwaptionCashFixedIborDefinition) definition;
      final SwapSecurity swapSecurity = null; //TODO
      //final DoubleTimeSeries<ZonedDateTime> swapFixingSeries = _swapConverter.convert(swapSecurity, definition, now, curveNames, dataSource);
      //return cashSettled.toDerivative(now, new DoubleTimeSeries[] {swapFixingTS}, curveNames);
    }
    if (definition instanceof SwaptionPhysicalFixedIborDefinition) {
      final SwaptionPhysicalFixedIborDefinition physicallySettled = (SwaptionPhysicalFixedIborDefinition) definition;
      final SwapSecurity swapSecurity = null; //TODO
      //final DoubleTimeSeries<ZonedDateTime> swapFixingSeries = _swapConverter.convert(swapSecurity, definition, now, curveNames, dataSource);
      //return physicallySettled.toDerivative(now, new DoubleTimeSeries[] {swapFixingTS}, curveNames);
    }
    throw new OpenGammaRuntimeException("This converter can only handle SwaptionCashFixedIborDefinition and SwaptionPhysicalFixedIborDefinition");
  }
}
