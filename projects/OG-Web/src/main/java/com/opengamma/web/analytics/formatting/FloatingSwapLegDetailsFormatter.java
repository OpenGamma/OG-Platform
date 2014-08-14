/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.ACCRUAL_YEAR_FRACTION;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.DISCOUNTED_PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.DISCOUNTED_PROJECTED_PAYMENT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.END_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.END_FIXING_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.FIXED_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.FIXING_FRACTIONS;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.FORWARD_RATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.GEARING;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.INDEX_TERM;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.NOTIONAL;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.PAYMENT_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.PAYMENT_DATE;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.PAYMENT_DISCOUNT_FACTOR;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.PAYMENT_TIME;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.PROJECTED_AMOUNT;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.SPREAD;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.START_ACCRUAL_DATES;
import static com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails.START_FIXING_DATES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Formatter for the details of the fixed leg of a swap.
 */
/* package */class FloatingSwapLegDetailsFormatter extends AbstractFormatter<FloatingSwapLegDetails> {
  /** Column labels */
  private static final String[] COLUMN_LABELS = new String[] {START_ACCRUAL_DATES, END_ACCRUAL_DATES, ACCRUAL_YEAR_FRACTION,
    START_FIXING_DATES, END_FIXING_DATES, FIXING_FRACTIONS, FORWARD_RATE, FIXED_RATE, PAYMENT_DATE, PAYMENT_TIME, PAYMENT_DISCOUNT_FACTOR,
    PAYMENT_AMOUNT, PROJECTED_AMOUNT, NOTIONAL, SPREAD, GEARING, INDEX_TERM, DISCOUNTED_PAYMENT_AMOUNT, DISCOUNTED_PROJECTED_PAYMENT };
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
  /** Formats the spread into basis points */
  private final AbstractFormatter<Double> _basisPointFormatter;

  /**
   * @param caFormatter The currency amount formatter, not null
   * @param rateFormatter The rate formatter, not null
   * @param basisPointFormatter The basis point formatter, not null
   */
  /* package */FloatingSwapLegDetailsFormatter(final AbstractFormatter<CurrencyAmount> caFormatter, final AbstractFormatter<Double> rateFormatter,
      final AbstractFormatter<Double> basisPointFormatter) {
    super(FloatingSwapLegDetails.class);
    ArgumentChecker.notNull(caFormatter, "currency amount formatter");
    ArgumentChecker.notNull(rateFormatter, "rate formatter");
    ArgumentChecker.notNull(basisPointFormatter, "basis point formatter");
    _caFormatter = caFormatter;
    _rateFormatter = rateFormatter;
    _basisPointFormatter = basisPointFormatter;
    addFormatter(new Formatter<FloatingSwapLegDetails>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(final FloatingSwapLegDetails value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final FloatingSwapLegDetails value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Floating Swap Leg Details (" + value.getNumberOfCashFlows() + ")";
  }

  /**
   * Expands the details into a matrix.
   * @param value The fixed swap leg details
   * @param valueSpec The value specification
   * @return The expanded format.
   */
  /* package */Map<String, Object> formatExpanded(final FloatingSwapLegDetails value, final ValueSpecification valueSpec) {
    final int rowCount = value.getNumberOfCashFlows();
    final String[] yLabels = new String[rowCount];
    Arrays.fill(yLabels, "");
    final Map<String, Object> results = new HashMap<>();
    results.put(X_LABELS, COLUMN_LABELS);
    results.put(Y_LABELS, yLabels);
    final Object[][] values = new Object[rowCount][COLUMN_COUNT];
    for (int i = 0; i < rowCount; i++) {
      values[i][0] = value.getAccrualStart()[i] == null ? "-" : value.getAccrualStart()[i].toString();
      values[i][1] = value.getAccrualEnd()[i] == null ? "-" : value.getAccrualEnd()[i].toString();
      values[i][2] = value.getAccrualYearFractions()[i];
      values[i][3] = value.getFixingStart()[i] == null ? "-" : value.getFixingStart()[i].toString();
      values[i][4] = value.getFixingEnd()[i] == null ? "-" : value.getFixingEnd()[i].toString();
      values[i][5] = value.getFixingYearFractions()[i] == null ? "-" : value.getFixingYearFractions()[i];
      values[i][6] = value.getForwardRates()[i] == null ? "-" : _rateFormatter.formatCell(value.getForwardRates()[i], valueSpec, null);
      values[i][7] = value.getFixedRates()[i] == null ? "-" : _rateFormatter.formatCell(value.getFixedRates()[i], valueSpec, null);
      values[i][8] = value.getPaymentDates()[i] == null ? "-" : value.getPaymentDates()[i].toString();
      values[i][9] = value.getPaymentTimes()[i];
      values[i][10] = value.getPaymentDiscountFactors()[i];
      values[i][11] = value.getPaymentAmounts()[i] == null ? "-" : _caFormatter.formatCell(value.getPaymentAmounts()[i], valueSpec, null);
      values[i][12] = value.getProjectedAmounts()[i] == null ? "-" : _caFormatter.formatCell(value.getProjectedAmounts()[i], valueSpec, null);
      values[i][13] = _caFormatter.formatCell(value.getNotionals()[i], valueSpec, null);
      values[i][14] = _basisPointFormatter.formatCell(value.getSpreads()[i], valueSpec, null);
      values[i][15] = value.getGearings()[i];
      values[i][16] = expandTenors(value.getIndexTenors()[i]);
      values[i][17] = value.getDiscountedPaymentAmounts()[i] == null ? "-" : _caFormatter.formatCell(value.getDiscountedPaymentAmounts()[i], valueSpec, null);
      values[i][18] = value.getDiscountedProjectedAmounts()[i] == null ? "-" : _caFormatter.formatCell(value.getDiscountedProjectedAmounts()[i], valueSpec, null);
    }
    results.put(MATRIX, values);
    return results;
  }
  
  /**
   * Returns the index tenors in a formatted string format.
   * <p>
   * If the index tenors are null or empty, then return <em>-</em>, otherwise return a comma separated list of ordered 
   * index tenors.
   * @param indexTenors the index tenors to format.
   * @return the formatted index tenors.
   */
  private String expandTenors(Set<Tenor> indexTenors) {
    if (indexTenors == null || indexTenors.isEmpty()) {
      return "-";
    } else {
      List<Tenor> orderedTenors = new ArrayList<>(indexTenors);
      Collections.sort(orderedTenors);
      StringBuffer buffer = new StringBuffer();
      for (Tenor tenor: orderedTenors) {
        if (buffer.length() > 0) {
          buffer.append(", ");
        }
        buffer.append(tenor.toFormattedString());
      }
      return buffer.toString();
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
