/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Properties;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionDefinitionFilter;
import com.opengamma.language.invoke.AggregatingTypeConverterProvider;
import com.opengamma.language.invoke.ParameterConverter;
import com.opengamma.language.invoke.ResultConverter;
import com.opengamma.language.invoke.ValueConverter;
import com.opengamma.language.livedata.AggregatingLiveDataProvider;
import com.opengamma.language.livedata.LiveDataDefinitionFilter;
import com.opengamma.language.procedure.AggregatingProcedureProvider;
import com.opengamma.language.procedure.ProcedureDefinitionFilter;
import com.opengamma.util.ArgumentChecker;

/**
 * A mutable version of {@link GlobalContext}.
 */
public class MutableGlobalContext extends GlobalContext {

  /* package */MutableGlobalContext() {
  }

  // Definition providers

  @Override
  public AggregatingFunctionProvider getFunctionProvider() {
    return getFunctionProviderImpl();
  }

  @Override
  public AggregatingLiveDataProvider getLiveDataProvider() {
    return getLiveDataProviderImpl();
  }

  @Override
  public AggregatingProcedureProvider getProcedureProvider() {
    return getProcedureProviderImpl();
  }

  // Standard context members

  public void setSystemSettings(final Properties properties) {
    ArgumentChecker.notNull(properties, "properties");
    setValue(SYSTEM_SETTINGS, properties);
  }

  public void replaceSystemSettings(final Properties properties) {
    ArgumentChecker.notNull(properties, "properties");
    replaceValue(SYSTEM_SETTINGS, properties);
  }

  public void setFunctionDefinitionFilter(final FunctionDefinitionFilter functionDefinitionFilter) {
    ArgumentChecker.notNull(functionDefinitionFilter, "functionDefinitionFilter");
    replaceValue(FUNCTION_DEFINITION_FILTER, functionDefinitionFilter);
  }

  public void setLiveDataDefinitionFilter(final LiveDataDefinitionFilter liveDataDefinitionFilter) {
    ArgumentChecker.notNull(liveDataDefinitionFilter, "liveDataDefinitionFilter");
    replaceValue(LIVEDATA_DEFINITION_FILTER, liveDataDefinitionFilter);
  }

  public void setProcedureDefinitionFilter(final ProcedureDefinitionFilter procedureDefinitionFilter) {
    ArgumentChecker.notNull(procedureDefinitionFilter, "procedureDefinitionFilter");
    replaceValue(PROCEDURE_DEFINITION_FILTER, procedureDefinitionFilter);
  }

  public void setParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(PARAMETER_CONVERTER, parameterConverter);
  }

  public void setFunctionParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(FUNCTION_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setLiveDataParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(LIVEDATA_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setProcedureParameterConverter(final ParameterConverter parameterConverter) {
    removeOrReplaceValue(PROCEDURE_PARAMETER_CONVERTER, parameterConverter);
  }

  public void setResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(RESULT_CONVERTER, resultConverter);
  }

  public void setFunctionResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(FUNCTION_RESULT_CONVERTER, resultConverter);
  }

  public void setLiveDataResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(LIVEDATA_RESULT_CONVERTER, resultConverter);
  }

  public void setProcedureResultConverter(final ResultConverter resultConverter) {
    removeOrReplaceValue(PROCEDURE_RESULT_CONVERTER, resultConverter);
  }

  public void setValueConverter(final ValueConverter valueConverter) {
    ArgumentChecker.notNull(valueConverter, "valueConverter");
    replaceValue(VALUE_CONVERTER, valueConverter);
  }

  @Override
  public AggregatingTypeConverterProvider getTypeConverterProvider() {
    return getTypeConverterProviderImpl();
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    removeOrReplaceValue(HISTORICAL_TIME_SERIES_SOURCE, historicalTimeSeriesSource);
  }
  
  public void setViewProcessor(final ViewProcessor viewProcessor) {
    removeOrReplaceValue(VIEW_PROCESSOR, viewProcessor);
  }

  public void setPositionSource(final PositionSource positionSource) {
    removeOrReplaceValue(POSITION_SOURCE, positionSource);
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    removeOrReplaceValue(SECURITY_SOURCE, securitySource);
  }

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}
