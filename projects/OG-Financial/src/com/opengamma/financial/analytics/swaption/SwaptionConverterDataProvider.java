/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.swaption;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.security.option.SwaptionSecurity;

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

  public InterestRateDerivative convert(final SwaptionSecurity security,
      final FixedIncomeInstrumentConverter<?> definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalDataSource dataSource) {
    if (definition instanceof SwaptionCashFixedIborDefinition) {
      final SwaptionCashFixedIborDefinition cashSettled = (SwaptionCashFixedIborDefinition) definition;
      return cashSettled.toDerivative(now, curveNames);
      //return cashSettled.toDerivative(now, new DoubleTimeSeries[] {swapFixingTS}, curveNames);
    }
    if (definition instanceof SwaptionPhysicalFixedIborDefinition) {
      final SwaptionPhysicalFixedIborDefinition physicallySettled = (SwaptionPhysicalFixedIborDefinition) definition;
      return physicallySettled.toDerivative(now, curveNames);
      //return physicallySettled.toDerivative(now, new DoubleTimeSeries[] {swapFixingTS}, curveNames);
    }
    throw new OpenGammaRuntimeException(
        "This converter can only handle SwaptionCashFixedIborDefinition and SwaptionPhysicalFixedIborDefinition");
  }
}
