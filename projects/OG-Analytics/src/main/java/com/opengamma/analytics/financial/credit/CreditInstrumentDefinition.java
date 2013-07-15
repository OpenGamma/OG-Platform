/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * 
 */
public interface CreditInstrumentDefinition {

  <DATA_TYPE, RESULT_TYPE> RESULT_TYPE accept(CreditInstrumentDefinitionVisitor<DATA_TYPE, RESULT_TYPE> visitor, DATA_TYPE data);

  <RESULT_TYPE> RESULT_TYPE accept(CreditInstrumentDefinitionVisitor<Void, RESULT_TYPE> visitor);

}
