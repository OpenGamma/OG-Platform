/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bethecoder.table.AsciiTableInstance;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.engine.ResultItem;
import com.opengamma.sesame.engine.ResultRow;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for views
 */
public final class ViewUtils {

  private ViewUtils() { /* private constructor */ }

  private static final Logger s_logger = LoggerFactory.getLogger(ViewUtils.class);

  /**
   * Output multiple currency amounts as an ascii table
   * @param label the label of the table
   * @param result the result object
   */
  public static void outputMultipleCurrencyAmount(String label, Result result) {

    if (result.isSuccess()) {
      MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
      System.out.println(label + ": PV "  + mca.toString());
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }
  }

  /**
   * Output bucketed sensitive as an ascii table
   * @param label the label of the table
   * @param result the result object
   */
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

  /**
   * Output results as an ascii table
   * @param results the results object
   */
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
          Object value = result.getResult().getValue();
          if (value instanceof BucketedCurveSensitivities) {
            Map<Pair<String, Currency>, DoubleLabelledMatrix1D> sensitivities = ((BucketedCurveSensitivities) value).getSensitivities();
            rows[i][j+1] = "";
            for (Map.Entry<Pair<String, Currency>, DoubleLabelledMatrix1D> entry : sensitivities.entrySet()) {
              rows[i][j+1] += entry.getKey().toString() + " - ";
              DoubleLabelledMatrix1D matrix = entry.getValue();

              for (int k =0; k < matrix.getLabels().length; k++) {
                rows[i][j+1] += matrix.getLabels()[k].toString() + ":" + matrix.getValues()[k] + ", ";
              }
            }
          } else {
            rows[i][j+1] = result.getResult().getValue().toString();
          }
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
    if (input instanceof TradeWrapper) {
      FinancialSecurity security = (FinancialSecurity) ((TradeWrapper) input).getSecurity();
      return formatSecurity(security);
    }
    if (input instanceof FinancialSecurity) {
      FinancialSecurity security = (FinancialSecurity) input;
      return formatSecurity(security);

    }
    return input.toString();
  }

  private static String formatSecurity(FinancialSecurity security) {
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

}