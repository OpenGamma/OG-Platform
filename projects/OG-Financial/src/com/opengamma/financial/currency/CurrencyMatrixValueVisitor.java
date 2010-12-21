/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixedValue;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixUniqueIdentifier;

/**
 * Visitor pattern to {@link CurrencyMatrixValue}.
 * 
 * @param <T> visitor function return type
 */
public interface CurrencyMatrixValueVisitor<T> {

  T visitFixedValue(CurrencyMatrixFixedValue fixedValue);

  T visitUniqueIdentifier(CurrencyMatrixUniqueIdentifier uniqueIdentifier);

  T visitCross(CurrencyMatrixCross cross);

}
