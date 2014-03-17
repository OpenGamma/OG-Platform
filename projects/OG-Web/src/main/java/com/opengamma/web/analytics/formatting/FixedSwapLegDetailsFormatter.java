/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.DISCOUNTED_PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.DISCOUNT_FACTOR;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.END_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.FIXED_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.NOTIONAL;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.PAYMENT_TIME;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.PAYMENT_YEAR_FRACTION;
import static com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails.START_ACCRUAL_DATES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Formatter for the details of the fixed leg of a swap.
 */
/* package */ class FixedSwapLegDetailsFormatter extends AbstractFormatter<FixedSwapLegDetails> {
  /** Column labels */
  private static final String[] COLUMN_LABELS = new String[] {START_ACCRUAL_DATES, END_ACCRUAL_DATES, DISCOUNT_FACTOR,
    PAYMENT_TIME, PAYMENT_YEAR_FRACTION, PAYMENT_AMOUNT, NOTIONAL, FIXED_RATE, DISCOUNTED_PAYMENT_AMOUNT};
  /** Number of columns */
  private static final int COLUMN_COUNT = COLUMN_LABELS.length;
  /** x labels field */
  private static final String X_LABELS = "xLabels";
  /** y labels field */
  private static final String Y_LABELS = "yLabels";
  /** Values matrix */
  private static final String MATRIX = "matrix";
  /** A currency amount formatter */
  private final AbstractFormatter<CurrencyAmount> _caFormatter;
  /** Formats rates */
  private final AbstractFormatter<Double> _rateFormatter;

  /**
   * @param caFormatter The currency amount formatter, not null
   * @param rateFormatter The rate formatter, not null
   */
  /* package */ FixedSwapLegDetailsFormatter(final AbstractFormatter<CurrencyAmount> caFormatter, final AbstractFormatter<Double> rateFormatter) {
    super(FixedSwapLegDetails.class);
    ArgumentChecker.notNull(caFormatter, "currency amount formatter");
    ArgumentChecker.notNull(rateFormatter, "rate formatter");
    _caFormatter = caFormatter;
    _rateFormatter = rateFormatter;
    addFormatter(new Formatter<FixedSwapLegDetails>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(FixedSwapLegDetails value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(FixedSwapLegDetails value, ValueSpecification valueSpec, Object inlineKey) {
    return "Fixed Swap Leg Details (" + value.getNumberOfCashFlows() + ")";
  }

  /**
   * Expands the details into a matrix.
   * @param value The fixed swap leg details
   * @param valueSpec The value specification
   * @return The expanded format.
   */
  /* package */ Map<String, Object> formatExpanded(FixedSwapLegDetails value, ValueSpecification valueSpec) {
    int rowCount = value.getNumberOfCashFlows();
    String[] yLabels = new String[rowCount];
    Arrays.fill(yLabels, "");
    Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    Object[][] values = new Object[rowCount][COLUMN_COUNT];
    for (int i = 0; i < rowCount; i++) {
      values[i][0] = value.getAccrualStart()[i] == null ? "-" : value.getAccrualStart()[i].toString();
      values[i][1] = value.getAccrualEnd()[i] == null ? "-" : value.getAccrualEnd()[i].toString();
      values[i][2] = value.getDiscountFactors()[i];
      values[i][3] = value.getPaymentTimes()[i];
      values[i][4] = value.getPaymentFractions()[i];
      values[i][5] = _caFormatter.formatCell(value.getPaymentAmounts()[i], valueSpec, null);
      values[i][6] = _caFormatter.formatCell(value.getNotionals()[i], valueSpec, null);
      values[i][7] = _rateFormatter.formatCell(value.getFixedRates()[i], valueSpec, null); 
      values[i][8] = _caFormatter.formatCell(value.getDiscountedPaymentAmounts()[i], valueSpec, null);
    }
    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
