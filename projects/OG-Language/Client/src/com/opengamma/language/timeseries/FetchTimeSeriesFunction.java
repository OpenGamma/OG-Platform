/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.error.InvokeParameterConversionException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.invoke.InvalidConversionException;

/**
 * Fetches a time series from the {@link HistoricalTimeSeriesSource}.
 */
public class FetchTimeSeriesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final JavaTypeInfo<UniqueId> UNIQUE_ID = JavaTypeInfo.builder(UniqueId.class).get();
  private static final JavaTypeInfo<ExternalIdBundle> EXTERNAL_ID_BUNDLE = JavaTypeInfo.builder(ExternalIdBundle.class).get();

  private static final int IDENTIFIER = 0;
  private static final int START = 1;
  private static final int END = 2;
  private static final int DATA_FIELD = 3;
  private static final int RESOLUTION_KEY = 4;
  private static final int INCLUSIVE_START = 5;
  private static final int EXCLUSIVE_END = 6;
  private static final int DATA_SOURCE = 7;
  private static final int DATA_PROVIDER = 8;
  private static final int IDENTIFIER_VALIDITY_DATE = 9;

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter identifierParameter = new MetaParameter("identifier", JavaTypeInfo.builder(Data.class).get());
    final MetaParameter startParameter = new MetaParameter("start", JavaTypeInfo.builder(LocalDate.class).allowNull().get());
    final MetaParameter endParameter = new MetaParameter("end", JavaTypeInfo.builder(LocalDate.class).allowNull().get());
    final MetaParameter dataFieldParameter = new MetaParameter("dataField", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter resolutionKeyParameter = new MetaParameter("resolutionKey", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter inclusiveStartParameter = new MetaParameter("inclusiveStart", JavaTypeInfo.builder(Boolean.class).defaultValue(true).get());
    final MetaParameter exclusiveEndParameter = new MetaParameter("exclusiveEnd", JavaTypeInfo.builder(Boolean.class).defaultValue(true).get());
    final MetaParameter dataSourceParameter = new MetaParameter("dataSource", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter dataProviderParameter = new MetaParameter("dataProvider", JavaTypeInfo.builder(String.class).allowNull().get());
    final MetaParameter identifierValidityDateParameter = new MetaParameter("identifierValidityDate", JavaTypeInfo.builder(LocalDate.class).allowNull().get());
    return Arrays.asList(identifierParameter, startParameter, endParameter, dataFieldParameter, resolutionKeyParameter, inclusiveStartParameter, exclusiveEndParameter, dataSourceParameter,
        dataProviderParameter, identifierValidityDateParameter);
  }

  private FetchTimeSeriesFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction("FetchTimeSeries", getParameters(), this));
  }

  public FetchTimeSeriesFunction() {
    this(new DefinitionAnnotater(FetchTimeSeriesFunction.class));
  }

  protected UniqueId getUniqueId(final SessionContext sessionContext, final Object identifier) {
    try {
      return sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, identifier, UNIQUE_ID);
    } catch (InvalidConversionException e) {
      throw new InvokeParameterConversionException(IDENTIFIER, e);
    }
  }

  protected ExternalIdBundle getExternalIdBundle(final SessionContext sessionContext, final Object identifier) {
    try {
      return sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, identifier, EXTERNAL_ID_BUNDLE);
    } catch (InvalidConversionException e) {
      throw new InvokeParameterConversionException(IDENTIFIER, e);
    }
  }

  // AbstractFunctionInvoker

  private static final int FLAG_START_AND_END = 0x01;
  private static final int FLAG_SOURCE_AND_PROVIDER = 0x02;
  private static final int FLAG_RESOLUTION_KEY = 0x04;
  private static final int FLAG_FIELD = 0x08;
  private static final int FLAG_IDENTIFIER_VALIDITY_DATE = 0x10;

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    int flags = 0;
    if ((parameters[START] != null) || (parameters[END] != null)) {
      flags |= FLAG_START_AND_END;
    }
    if (parameters[DATA_FIELD] != null) {
      flags |= FLAG_FIELD;
    }
    if (parameters[RESOLUTION_KEY] != null) {
      flags |= FLAG_RESOLUTION_KEY;
    }
    if ((parameters[DATA_SOURCE] != null) && (parameters[DATA_PROVIDER] != null)) {
      flags |= FLAG_SOURCE_AND_PROVIDER;
    }
    if (parameters[IDENTIFIER_VALIDITY_DATE] != null) {
      flags |= FLAG_IDENTIFIER_VALIDITY_DATE;
    }
    final HistoricalTimeSeriesSource source = sessionContext.getGlobalContext().getHistoricalTimeSeriesSource();
    switch (flags) {
      case 0:
        return source.getHistoricalTimeSeries(getUniqueId(sessionContext, parameters[IDENTIFIER]));
      case FLAG_START_AND_END:
        return source.getHistoricalTimeSeries(getUniqueId(sessionContext, parameters[IDENTIFIER]), (LocalDate) parameters[START], (Boolean) parameters[INCLUSIVE_START], (LocalDate) parameters[END],
            (Boolean) parameters[EXCLUSIVE_END]);
      case FLAG_SOURCE_AND_PROVIDER | FLAG_FIELD:
        return source.getHistoricalTimeSeries(getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (String) parameters[DATA_SOURCE], (String) parameters[DATA_PROVIDER],
            (String) parameters[DATA_FIELD]);
      case FLAG_SOURCE_AND_PROVIDER | FLAG_FIELD | FLAG_START_AND_END:
        return source.getHistoricalTimeSeries(getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (String) parameters[DATA_SOURCE], (String) parameters[DATA_PROVIDER],
            (String) parameters[DATA_FIELD], (LocalDate) parameters[START], (Boolean) parameters[INCLUSIVE_START], (LocalDate) parameters[END],
            (Boolean) parameters[EXCLUSIVE_END]);
      case FLAG_SOURCE_AND_PROVIDER | FLAG_FIELD | FLAG_IDENTIFIER_VALIDITY_DATE:
        return source.getHistoricalTimeSeries(getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (LocalDate) parameters[IDENTIFIER_VALIDITY_DATE], (String) parameters[DATA_SOURCE],
            (String) parameters[DATA_PROVIDER], (String) parameters[DATA_FIELD]);
      case FLAG_SOURCE_AND_PROVIDER | FLAG_FIELD | FLAG_START_AND_END | FLAG_IDENTIFIER_VALIDITY_DATE:
        return source.getHistoricalTimeSeries(getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (LocalDate) parameters[IDENTIFIER_VALIDITY_DATE], (String) parameters[DATA_SOURCE],
            (String) parameters[DATA_PROVIDER], (String) parameters[DATA_FIELD], (LocalDate) parameters[START], (Boolean) parameters[INCLUSIVE_START], (LocalDate) parameters[END],
            (Boolean) parameters[EXCLUSIVE_END]);
      case FLAG_FIELD | FLAG_RESOLUTION_KEY:
        return source.getHistoricalTimeSeries((String) parameters[DATA_FIELD], getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (String) parameters[RESOLUTION_KEY]);
      case FLAG_FIELD | FLAG_RESOLUTION_KEY | FLAG_START_AND_END:
        return source.getHistoricalTimeSeries((String) parameters[DATA_FIELD], getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (String) parameters[RESOLUTION_KEY],
            (LocalDate) parameters[START], (Boolean) parameters[INCLUSIVE_START], (LocalDate) parameters[END], (Boolean) parameters[EXCLUSIVE_END]);
      case FLAG_FIELD | FLAG_RESOLUTION_KEY | FLAG_IDENTIFIER_VALIDITY_DATE:
        return source.getHistoricalTimeSeries((String) parameters[DATA_FIELD], getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (LocalDate) parameters[IDENTIFIER_VALIDITY_DATE],
            (String) parameters[RESOLUTION_KEY]);
      case FLAG_FIELD | FLAG_RESOLUTION_KEY | FLAG_START_AND_END | FLAG_IDENTIFIER_VALIDITY_DATE:
        return source.getHistoricalTimeSeries((String) parameters[DATA_FIELD], getExternalIdBundle(sessionContext, parameters[IDENTIFIER]), (LocalDate) parameters[IDENTIFIER_VALIDITY_DATE],
            (String) parameters[RESOLUTION_KEY], (LocalDate) parameters[START], (Boolean) parameters[INCLUSIVE_START], (LocalDate) parameters[END], (Boolean) parameters[EXCLUSIVE_END]);
      default:
        throw new InvokeInvalidArgumentException("Invalid combination of omitted parameters");
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
