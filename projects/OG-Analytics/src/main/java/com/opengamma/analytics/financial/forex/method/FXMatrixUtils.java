/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility class to manipulate FXMatrix.
 */
public class FXMatrixUtils {

  /**
   * Merges two FXMatrix. The two matrix should have at least one currency in common. The matrix return is a new matrix.
   * The merged matrix will contain the data in the initial matrix1 and all the currencies of the two matrices.
   * The missing currencies from matrix2 are added one by one and the exchange rate data created is coherent with some data in the initial matrices.
   * If the data in the initial matrices are not coherent between them, there is no guarantee which data will be used and the final result may be incoherent.
   * @param matrix1 The first matrix.
   * @param matrix2 The second matrix.
   * @return The merged matrix.
   */
  public static FXMatrix merge(final FXMatrix matrix1, final FXMatrix matrix2) {
    ArgumentChecker.notNull(matrix1, "first FX matrix");
    ArgumentChecker.notNull(matrix2, "second FX matrix");
    // Implementation note: Check is one matrix is empty
    if (matrix1.getNumberOfCurrencies() == 0) {
      return new FXMatrix(matrix2);
    }
    if (matrix2.getNumberOfCurrencies() == 0) {
      return new FXMatrix(matrix1);
    }
    // Implementation note: Finding a common currency
    final Set<Currency> set1 = matrix1.getCurrencies().keySet();
    final Set<Currency> set2 = matrix2.getCurrencies().keySet();
    final Set<Currency> intersection = Sets.intersection(set1, set2);
    if (intersection.isEmpty()) {
      throw new OpenGammaRuntimeException("Currency sets of the two matrix don't have an interesection");
    }
    final Currency ccyCommon = intersection.iterator().next(); // Implementation note: take any currency in the intersection.
    // Implementation note: New matrix with the same element as matrix1
    final FXMatrix result = new FXMatrix(matrix1);
    // Implementation note: adding the missing currencies from matrix2 one by one.
    for (Currency loopccy : set2) {
      if (!set1.contains(loopccy)) {
        result.addCurrency(loopccy, ccyCommon, matrix2.getFxRate(loopccy, ccyCommon));
      }
    }
    return result;
  }

  /**
   * Compares two FX Matrix within a given tolerance. The comparison is done only on one secondary diagonal. 
   * Other rates will also be correct if the input matrices are coherent
   * @param matrix1 The first matrix.
   * @param matrix2 The second matrix.
   * @param tolerance The tolerance.
   * @return The comparison result. Will be true if for each element in the diagonal tested, the two matrix have a difference in exchange rate lower thatn the tolerance.
   */
  public static boolean compare(final FXMatrix matrix1, final FXMatrix matrix2, final double tolerance) {
    // Implementation note: Compare currency set
    final Set<Currency> set1 = matrix1.getCurrencies().keySet();
    final Set<Currency> set2 = matrix2.getCurrencies().keySet();
    if (!set1.equals(set2)) {
      return false;
    }
    // Implementation note: Compare one diagonal (other will be correct also if matrices are coherent).
    final Iterator<Currency> iterator = set1.iterator();
    if (set1.size() > 0) {
      final Currency initialCurrency = iterator.next();
      while (iterator.hasNext()) {
        final Currency otherCurrency = iterator.next();
        final boolean correct = Math.abs(matrix1.getFxRate(initialCurrency, otherCurrency) - matrix2.getFxRate(initialCurrency, otherCurrency)) < tolerance;
        if (!correct) {
          return false;
        }
      }
    }
    return true;
  }

}
