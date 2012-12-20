/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixed;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;

/**
 * Visitor pattern to {@link CurrencyMatrixValue}.
 * 
 * @param <T> visitor function return type
 */
public interface CurrencyMatrixValueVisitor<T> {

  T visitFixed(CurrencyMatrixFixed fixedValue);

  T visitValueRequirement(CurrencyMatrixValueRequirement uniqueId);

  T visitCross(CurrencyMatrixCross cross);

}
