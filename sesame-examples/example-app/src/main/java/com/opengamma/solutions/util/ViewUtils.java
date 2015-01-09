/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bethecoder.table.AsciiTableInstance;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.engine.ResultItem;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for views
 */
public final class ViewUtils {

  private ViewUtils() { /* private constructor */ }

  public static void outputMultipleCurrencyAmount(String label, Result result) {

    if (result.isSuccess()) {
      MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
      System.out.println(label + ": PV "  + mca.toString());
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }
  }

  public static void outputBucketedCurveSensitivities(String label, Result result) {

    if (result.isSuccess()) {
      System.out.println(label  + ": Bucketed PV01");

      BucketedCurveSensitivities bcs = (BucketedCurveSensitivities) result.getValue();
      Map sensitivities = bcs.getSensitivities();
      Iterator entryIterator = sensitivities.entrySet().iterator();
      while (entryIterator.hasNext()) {
        Map.Entry entry = (Map.Entry) entryIterator.next();
        Pair pair = (Pair) entry.getKey();
        DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D) sensitivities.get(pair);
        System.out.println("  " + pair.getFirst().toString() + ": " + pair.getSecond().toString());
        for (int i=0; i < matrix.getLabels().length; i++) {
          System.out.println("    " + matrix.getLabels()[i].toString() + ": " + matrix.getValues()[i]);
        }
      }
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }

  }

  public static String format(Results results) {
    String[] columnNames = results.getColumnNames().toArray(new String[results.getColumnNames().size()]);
    String[] headers = new String[columnNames.length + 1];
    headers[0] = "Input";
    System.arraycopy(columnNames, 0, headers, 1, columnNames.length);
    String[][] rows = new String[results.getRows().size()][headers.length];
    for (int i = 0; i < rows.length; i++) {
      ResultRow row = results.get(i);
      rows[i][0] = formatInput(row.getInput());
      for (int j = 0; j < results.getColumnNames().size(); j++) {
        ResultItem result = row.get(j);
        if (result.getResult().isSuccess()) {
          rows[i][j+1] = result.getResult().getValue().toString();
        } else {
          rows[i][j+1] = "[FAIL] " + result.getResult().getFailureMessage();
        }
      }
    }
    StringBuilder sb = new StringBuilder(AsciiTableInstance.get().getTable(headers, rows));
    sb.append(System.lineSeparator());
    sb.append("Calculated in ").append(results.getViewTimer().getTotalDuration().toMillis()).append("ms");
    return sb.toString();
  }

  private static String formatInput(Object input) {
    if (input instanceof FinancialSecurity) {
      FinancialSecurity security = (FinancialSecurity) input;
      if (!StringUtils.isBlank(security.getName())) {
        return security.getName();
      }
      if (!security.getExternalIdBundle().isEmpty()) {
        return security.getExternalIdBundle().toString();
      }
      if (security.getUniqueId() != null) {
        return security.getUniqueId().toString();
      }
      return security.getSecurityType();
    }
    return input.toString();
  }

}