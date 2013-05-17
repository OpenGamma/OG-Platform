/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Static utility class for formatting axis labels reasonably.
 */
public class LabelFormatter {
  public static String format(Object o) {
    if (o instanceof Tenor) {
      return formatTenor((Tenor) o);
    } else if (o instanceof LocalDate) {
      return formatDate((LocalDate) o);
    } else if (o instanceof Pair) {
      return formatPair((Pair<?, ?>) o);
    } else if (o instanceof Double) {
      return formatDouble((Double) o);
    } else if (o instanceof Integer) {
      return formatInteger((Integer) o);
    } else {
      return o.toString();
    }
  }

  private static String formatInteger(Integer o) {
    return Integer.toString(o);
  }

  //private static DoubleValueSizeBasedDecimalPlaceFormatter s_formatter = new DoubleValueSizeBasedDecimalPlaceFormatter(3, 0, 100, false);
  private static DoubleValueSignificantFiguresFormatter s_formatter = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;
  
  private static String formatDouble(Double o) {
    return s_formatter.format(BigDecimal.valueOf(o));
  }

  private static String formatPair(Pair<?, ?> o) {
    Object firstObj = o.getFirst();
    Object secondObj = o.getSecond();
    if ((firstObj instanceof Integer) && (secondObj instanceof FXVolQuoteType)) {
      Integer first = (Integer) firstObj;
      FXVolQuoteType second = (FXVolQuoteType) secondObj;
      StringBuilder sb = new StringBuilder();
      sb.append(Integer.toString(first));
      sb.append("/");
      sb.append(second.name());
      return sb.toString();
    }
    return o.toString();
  }
  
  private static final DateTimeFormatter s_isoLocalDate = DateTimeFormatter.ISO_LOCAL_DATE;
  
  private static String formatDate(LocalDate o) {
    return s_isoLocalDate.format(o);
  }

  private static String formatTenor(Tenor o) {
    return o.getPeriod().toString().replaceFirst("P", "");
  }
}
