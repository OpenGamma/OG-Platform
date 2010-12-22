/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

  T visitValueRequirement(CurrencyMatrixValueRequirement uniqueIdentifier);

  T visitCross(CurrencyMatrixCross cross);

}
