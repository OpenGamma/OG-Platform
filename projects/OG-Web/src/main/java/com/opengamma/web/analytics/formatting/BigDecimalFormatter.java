/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.DoubleValueDecimalPlaceFormatter;
import com.opengamma.web.server.conversion.DoubleValueFormatter;
import com.opengamma.web.server.conversion.DoubleValueSignificantFiguresFormatter;
import com.opengamma.web.server.conversion.DoubleValueSizeBasedDecimalPlaceFormatter;
import com.opengamma.web.server.conversion.PercentageValueSignificantFiguresFormatter;

/**
 *
 */
/* package */ class BigDecimalFormatter extends AbstractFormatter<BigDecimal> {

  private static final Logger s_logger = LoggerFactory.getLogger(BigDecimalFormatter.class);
  private static final Map<String, DoubleValueFormatter> s_formatters = Maps.newHashMap();
  private static final DoubleValueFormatter s_defaultFormatter = DoubleValueSignificantFiguresFormatter.NON_CCY_5SF;
  private static final DoubleValueFormatter s_defaultCcyFormatter = DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT;

  static {
    // General
    s_formatters.put(ValueRequirementNames.DISCOUNT_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.INSTANTANEOUS_FORWARD_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOLATILITY_SURFACE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOLATILITY_SURFACE_DATA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.COST_OF_CARRY, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Pricing
    s_formatters.put(ValueRequirementNames.PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.GAMMA_PV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.DV01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.GAMMA_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.RR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.IR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.JUMP_TO_DEFAULT, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PAR_RATE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE_THETA, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.POSITION_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.VALUE_FAIR_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.SECURITY_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.SECURITY_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.UNDERLYING_MARKET_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.UNDERLYING_MODEL_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));
    s_formatters.put(ValueRequirementNames.DAILY_PRICE, DoubleValueSignificantFiguresFormatter.of(5, true));

    // PnL
    s_formatters.put(ValueRequirementNames.PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.DAILY_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.MTM_PNL, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Greeks
    s_formatters.put(ValueRequirementNames.DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DELTA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.STRIKE_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.STRIKE_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.GAMMA_P_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VEGA_BLEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CARRY_RHO, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.BUCKETED_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.BUCKETED_GAMMA_CS01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.BUCKETED_IR01, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.YIELD_CURVE_JACOBIAN, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FX_IMPLIED_TRANSITION_MATRIX, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_ULTIMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.SPEED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.SPEED_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DVANNA_DVOL, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VOMMA_P, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.VARIANCE_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DUAL_DELTA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DUAL_GAMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VEGA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VANNA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.FORWARD_VOMMA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.DRIFTLESS_THETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Position/value greeks
    addBulkConversion("(POSITION_|VALUE_).*", DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Series analysis
    s_formatters.put(ValueRequirementNames.SKEW, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.FISHER_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.PEARSON_KURTOSIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // VaR
    s_formatters.put(ValueRequirementNames.HISTORICAL_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.PARAMETRIC_VAR, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.HISTORICAL_VAR_STDDEV, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
    s_formatters.put(ValueRequirementNames.CONDITIONAL_HISTORICAL_VAR,
                     DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);

    // Capital Asset Pricing
    s_formatters.put(ValueRequirementNames.CAPM_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_RESIDUALS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ADJUSTED_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_TSTATS, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_ALPHA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_BETA_PVALUES, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_MEAN_SQUARE_ERROR, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_R_SQUARED, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_ALPHA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.CAPM_REGRESSION_STANDARD_ERROR_OF_BETA, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Traditional Risk-Reward
    s_formatters.put(ValueRequirementNames.SHARPE_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.TREYNOR_RATIO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.JENSENS_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.TOTAL_RISK_ALPHA, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.WEIGHT, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);

    // Bonds
    s_formatters.put(ValueRequirementNames.CLEAN_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.YTM, PercentageValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.MARKET_YTM, PercentageValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.MARKET_DIRTY_PRICE, DoubleValueDecimalPlaceFormatter.NON_CCY_6DP);
    s_formatters.put(ValueRequirementNames.MACAULAY_DURATION, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.CONVEXITY, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.Z_SPREAD, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.CONVERTION_FACTOR, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.IMPLIED_REPO, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.GROSS_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.NET_BASIS, DoubleValueDecimalPlaceFormatter.NON_CCY_4DP);
    s_formatters.put(ValueRequirementNames.NS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);
    s_formatters.put(ValueRequirementNames.NSS_BOND_CURVE, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // Options
    s_formatters.put(ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY, DoubleValueSignificantFiguresFormatter.NON_CCY_5SF);

    // FX
    s_formatters.put(ValueRequirementNames.FX_PRESENT_VALUE, DoubleValueSizeBasedDecimalPlaceFormatter.CCY_DEFAULT);
  }

  private final ResultsFormatter.CurrencyDisplay _currencyDisplay;

  /* package */ BigDecimalFormatter(ResultsFormatter.CurrencyDisplay currencyDisplay) {
    super(BigDecimal.class);
    ArgumentChecker.notNull(currencyDisplay, "currencyDisplay");
    _currencyDisplay = currencyDisplay;
    addFormatter(new Formatter<BigDecimal>(Format.HISTORY) {
      @Override
      Object format(BigDecimal value, ValueSpecification valueSpec, Object inlineKey) {
        return getFormatter(valueSpec).getRoundedValue(value);
      }
    });
    addFormatter(new Formatter<BigDecimal>(Format.EXPANDED) {
      @Override
      Object format(BigDecimal value, ValueSpecification valueSpec, Object inlineKey) {
        return formatCell(value, valueSpec, inlineKey);
      }
    });
  }
  
  private static void addBulkConversion(String valueRequirementFieldNamePattern, DoubleValueFormatter conversionSettings) {
    Pattern pattern = Pattern.compile(valueRequirementFieldNamePattern);
    for (Field field : ValueRequirementNames.class.getFields()) {
      if ((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == (Modifier.STATIC | Modifier.PUBLIC) &&
          field.isSynthetic() == false &&
          String.class.equals(field.getType()) && pattern.matcher(field.getName()).matches()) {
        String fieldValue;
        try {
          fieldValue = (String) field.get(null);
          s_formatters.put(fieldValue, conversionSettings);
        } catch (Exception e) {
          s_logger.debug("Unexpected exception initializing formatter", e);
        }
      }
    }
  }

  private static DoubleValueFormatter getFormatter(ValueSpecification valueSpec) {
    if (valueSpec == null) {
      return s_defaultFormatter;
    }
    DoubleValueFormatter valueNameFormatter = s_formatters.get(valueSpec.getValueName());
    if (valueNameFormatter != null) {
      return valueNameFormatter;
    } else {
      if (valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY) != null) {
        return s_defaultCcyFormatter;
      }
      return s_defaultFormatter;
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.DOUBLE;
  }

  @Override
  public String formatCell(BigDecimal value, ValueSpecification valueSpec, Object inlineKey) {
    DoubleValueFormatter formatter = getFormatter(valueSpec);
    String formattedNumber = formatter.format(value);
    return formatter.isCurrencyAmount() && _currencyDisplay == DISPLAY_CURRENCY ?
        formatWithCurrency(formattedNumber, valueSpec) :
        formattedNumber;
  }

  private String formatWithCurrency(String formattedNumber, ValueSpecification valueSpec) {
    Set<String> currencyValues = valueSpec.getProperties().getValues(ValuePropertyNames.CURRENCY);
    return currencyValues == null || currencyValues.isEmpty() ?
        formattedNumber :
        currencyValues.iterator().next() + " " + formattedNumber;
  }
}
