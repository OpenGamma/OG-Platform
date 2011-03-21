/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.context;

import java.util.Properties;

import com.opengamma.language.function.AggregatingFunctionProvider;
import com.opengamma.language.function.FunctionDefinitionFilter;
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
    setValue(SYSTEM_SETTINGS, properties);
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

  // Arbitrary values

  @Override
  public void setValue(final String key, final Object value) {
    super.setValue(key, value);
  }

}
