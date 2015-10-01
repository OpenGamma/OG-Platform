/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.List;

import com.bethecoder.table.AsciiTableInstance;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Helper class for formatting cashflow results.
 */
public final class CashFlowFormatter {

  private static final ImmutableList<String> FIXED_HEADERS = ImmutableList.of(
      "currency", "start accrual date", "end accrual date", "df", "payment date",
      "payment amount", "present value", "notional", "fixed rate");

  private static final ImmutableList<String> FLOAT_HEADERS = ImmutableList.of(
      "currency", "start accrual date", "end accrual date", "fixing year fraction", "df",
      "payment date", "payment amount", "present value", "notional", "rate");
  private static final String N_A = "n/a";

  private final Formatter _formatter;

  private CashFlowFormatter(Formatter formatter) {
    _formatter = formatter;
  }

  /**
   * A formatter which formats results as a CSV.
   *
   * @return a csv result formatter
   */
  public static CashFlowFormatter csvFormatter() {
    return new CashFlowFormatter(new CsvTableFormatter());
  }

  /**
   * A formatter which formats results as an ascii table.
   *
   * @return an ascii table result formatter
   */
  public static CashFlowFormatter asciiFormatter() {
    return new CashFlowFormatter(new AsciiTableFormatter());
  }

  /**
   * Output trade cashflows as an ascii table (1 table per set of cashflows)
   *
   * @param results  sequence of cashflows
   * @return the formatted cashflows
   */
  public String formatSwapCashflows(Iterable<SwapLegCashFlows> results) {

    StringBuilder sb = new StringBuilder();
    for (SwapLegCashFlows value : results) {
      if (value instanceof FixedLegCashFlows) {
        FixedLegCashFlows fixedLegCashFlows = (FixedLegCashFlows) value;
        List<List<String>> rows = Lists.newArrayList();
        for (int k = 0; k < fixedLegCashFlows.getNumberOfCashFlows(); k++) {
          FixedCashFlowDetails flowDetails = fixedLegCashFlows.getCashFlowDetails().get(k);

          String df = String.valueOf(flowDetails.getDf() != null ? flowDetails.getDf() : N_A);
          String rate = String.valueOf(flowDetails.getRate() != null ? flowDetails.getRate() : N_A);

          List<String> row = ImmutableList.of(flowDetails.getNotional().getCurrency().toString(),
                                              flowDetails.getAccrualStartDate().toString(),
                                              flowDetails.getAccrualEndDate().toString(),
                                              df,
                                              flowDetails.getPaymentDate().toString(),
                                              caToString(flowDetails.getProjectedAmount()),
                                              caToString(flowDetails.getPresentValue()),
                                              caToString(flowDetails.getNotional()),
                                              rate);
          rows.add(row);
        }
        sb.append(_formatter.format(FIXED_HEADERS, rows));
      } else if (value instanceof FloatingLegCashFlows) {
        FloatingLegCashFlows legCashFlows = (FloatingLegCashFlows) value;
        List<List<String>> rows = Lists.newArrayList();
        for (int k = 0; k < legCashFlows.getNumberOfCashFlows(); k++) {
          FloatingCashFlowDetails flowDetails = legCashFlows.getCashFlowDetails().get(k);

          // rate can be missing if for instance we have a compounded period containing multiple sub periods
          String rate = String.valueOf(flowDetails.getFixedRate() != null ?
                                          flowDetails.getFixedRate() :
                                          flowDetails.getForwardRate() != null ?
                                              flowDetails.getForwardRate() :
                                              N_A);
          String df = String.valueOf(flowDetails.getDf() != null ? flowDetails.getDf() : N_A);

          rows.add(ImmutableList.of(flowDetails.getNotional().getCurrency().toString(),
                                    flowDetails.getAccrualStartDate().toString(),
                                    flowDetails.getAccrualEndDate().toString(),
                                    String.valueOf(flowDetails.getFixingYearFrac()),
                                    df,
                                    flowDetails.getPaymentDate().toString(),
                                    caToString(flowDetails.getProjectedAmount()),
                                    caToString(flowDetails.getPresentValue()),
                                    caToString(flowDetails.getNotional()),
                                    rate));

        }
        sb.append(_formatter.format(FLOAT_HEADERS, rows));
      }
    }
    return sb.toString();
  }

  private interface Formatter {
    String format(List<String> headers, List<List<String>> rows);
  }

  private static class AsciiTableFormatter implements Formatter {

    @Override
    public String format(List<String> headers, List<List<String>> rows) {
      String[][] arrayTable = new String[rows.size()][];

      for (int i = 0; i < rows.size(); i++) {
        arrayTable[i] = rows.get(i).toArray(new String[headers.size()]);
      }

      return AsciiTableInstance.get().getTable(headers.toArray(new String[headers.size()]), arrayTable);

    }

  }

  private static class CsvTableFormatter implements Formatter {

    @Override
    public String format(List<String> headers, List<List<String>> rows) {

      List<String> outputRows = Lists.newArrayList();
      outputRows.add(Joiner.on(",").join(headers));
      for (List<String> row : rows) {
        outputRows.add(Joiner.on(",").join(row));
      }

      return Joiner.on("\n").join(outputRows) + "\n";

    }

  }

  private String caToString(CurrencyAmount ca) {
    if (ca == null) {
      return N_A;
    }
    return Double.toString(ca.getAmount());
  }
}
