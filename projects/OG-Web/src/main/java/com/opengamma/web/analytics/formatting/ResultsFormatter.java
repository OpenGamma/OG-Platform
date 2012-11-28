/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.util.ClassMap;
import com.opengamma.web.analytics.ValueTypes;

/**
 * Formats analytics data for display in a grid in the user interface or for transmitting to the client as history.
 * Data structures bigger than a single value are encoded as JSON.
 */
public class ResultsFormatter {

  private static final Logger s_logger = LoggerFactory.getLogger(ResultsFormatter.class);

  /** For formatting null values. */
  private final TypeFormatter _nullFormatter = new NullFormatter();
  /** For formatting values with no specific formatter. */
  private final TypeFormatter _defaultFormatter = new DefaultFormatter();
  /** Formatters keyed on the type of value they can format. */
  private final ClassMap<TypeFormatter<?>> _formatters = new ClassMap<TypeFormatter<?>>();
  /** Formatter for values whose type isn't know in advance or whose type can changes between calculation cycles. */
  private final UnknownTypeFormatter _unknownTypeFormatter = new UnknownTypeFormatter();

  public ResultsFormatter() {
    BigDecimalFormatter bigDecimalFormatter = new BigDecimalFormatter();
    DoubleFormatter doubleFormatter = new DoubleFormatter(bigDecimalFormatter);
    CurrencyAmountFormatter currencyAmountFormatter = new CurrencyAmountFormatter(bigDecimalFormatter);

    addFormatters(doubleFormatter,
                  bigDecimalFormatter,
                  currencyAmountFormatter,
                  new YieldCurveFormatter(),
                  new VolatilityCubeDataFormatter(),
                  new VolatilitySurfaceDataFormatter(),
                  new VolatilitySurfaceFormatter(),
                  new LabelledMatrix1DFormatter(doubleFormatter),
                  new LabelledMatrix2DFormatter(),
                  new LabelledMatrix3DFormatter(),
                  new TenorFormatter(),
                  new MultipleCurrencyAmountFormatter(doubleFormatter),
                  new MissingMarketDataSentinelFormatter(),
                  new NotCalculatedSentinelFormatter(),
                  new ForwardCurveFormatter(),
                  new BlackVolatilitySurfaceMoneynessFormatter(),
                  new LocalVolatilitySurfaceMoneynessFormatter(),
                  new BucketedGreekResultCollectionFormatter(),
                  new DoublesCurveFormatter(),
                  new LocalDateDoubleTimeSeriesFormatter(),
                  new HistoricalTimeSeriesFormatter(),
                  new DoubleArrayFormatter(),
                  new DoubleObjectArrayFormatter(),
                  new ListDoubleArrayFormatter(),
                  new PresentValueForexBlackVolatilitySensitivityFormatter(),
                  new SnapshotDataBundleFormatter(doubleFormatter),
                  new InterpolatedYieldCurveSpecificationWithSecuritiesFormatter(),
                  new HistoricalTimeSeriesBundleFormatter(),
                  new VolatilitySurfaceSpecificationFormatter(),
                  new BlackVolatilitySurfaceMoneynessFcnBackedByGridFormatter());
  }

  private void addFormatters(TypeFormatter<?>... formatters) {
    for (TypeFormatter<?> formatter : formatters) {
      _formatters.put(formatter.getType(), formatter);
    }
  }

  private TypeFormatter getFormatter(Object value, ValueSpecification valueSpec) {
    if (value == null) {
      return _nullFormatter;
    } else if (isError(value) || valueSpec == null) {
      return getFormatterForType(value.getClass());
    } else {
      Class<?> type = ValueTypes.getTypeForValueName(valueSpec.getValueName());
      if (type != null) {
        if (type.isInstance(value)) {
          return getFormatterForType(type);
        } else {
          // this happens if ValueTypes has a type for a value name but the actual value produced has a different type.
          // there are several possible causes:
          //   1) the type produced for a value has been changed (e.g. the function that produces it has been modified)
          //      but the ValueTypes config hasn't been updated to match
          //   2) the type produced for the value can change from cycle to cycle. to fix this the ValueTypes config
          //      should be modified to specify a supertype of all possible types. if there isn't a common supertype
          //      with a formatter that works for all possible values then the value name can be removed from the
          //      ValueTypes config. this will give the value name a type of UNKNOWN and the type and formatting will
          //      be decided from the value after every cycle
          //   3) the type produced for the value is always the same but the value is converted to a different type
          //      by Fudge depending on the value. e.g. an integer will be encoded as a byte if it is small enough
          //      but will be encoded as an integer if it won't fit into a byte. the fix for this scenario is the same
          //      as #2 above
          s_logger.warn("Unexpected type for value. Value name: '{}', expected type: {}, actual type: {}, value: {}",
                        new Object[]{valueSpec.getValueName(), type.getName(), value.getClass().getName(), value});
        }
      }
      return getFormatterForType(value.getClass());
    }
  }

  private TypeFormatter getFormatterForType(Class<?> type) {
    if (type == null) {
      return _unknownTypeFormatter;
    }
    TypeFormatter formatter = _formatters.get(type);
    if (formatter == null) {
      return _defaultFormatter;
    } else {
      return formatter;
    }
  }

  private static boolean isError(Object value) {
    return value instanceof MissingInput;
  }

  /**
   * Returns a formatted version of a value suitable for display in a single cell in the UI. If the data is too big
   * to fit in a single cell (e.g. a matrix) this method returns a summary value.
   * @param value The value
   * @param valueSpec The value's specification
   * @return {@code null} if the value is {@code null}, otherwise a formatted version of a value suitable
   * for display in the UI.
   */
/*
  @SuppressWarnings("unchecked")
  public Object formatForDisplay(Object value, ValueSpecification valueSpec) {
    return getFormatter(value, valueSpec).formatForDisplay(value, valueSpec);
  }
*/

  /**
   * Returns a formatted version of a value including all information. This might not fit into a single grid cell in
   * the UI, e.g. a matrix. If the value is a data structure it is encoded as JSON.
   * @param value The value
   * @param valueSpec The value's specification
   * @return {@code null} if the value is {@code null}, otherwise a formatted version of a value suitable
   * for display in the UI.
   */
/*
  @SuppressWarnings("unchecked")
  public Object formatForExpandedDisplay(Object value, ValueSpecification valueSpec) {
    return getFormatter(value, valueSpec).formatForExpandedDisplay(value, valueSpec);
  }
*/

  /**
   * Formats a single history value in a format suitable for embedding in a JSON object.
   * @param value The value
   * @param valueSpec The value's specification
   * @return A formatted value suitable for embedding in a JSON object or null if the value is null
   */
/*
  @SuppressWarnings("unchecked")
  public Object formatForHistory(Object value, ValueSpecification valueSpec) {
    return getFormatter(value, valueSpec).formatForHistory(value, valueSpec);
  }
*/

  @SuppressWarnings("unchecked")
  public Object format(Object value, ValueSpecification valueSpec, TypeFormatter.Format format) {
    return getFormatter(value, valueSpec).format(value, valueSpec, format);
  }
  
  /**
   * Returns the format type for a value type.
   * @param type The value type
   * @return The formatter used for formatting the type
   */
  public DataType getDataType(Class<?> type) {
    return getFormatterForType(type).getDataType();
  }

  /**
   * Returns the format type for a value.
   * @param value The value, possibly null
   * @param valueSpec The value's specification, possibly null
   * @return The format type for the value, not null
   */
  @SuppressWarnings("unchecked")
  public DataType getDataTypeForValue(Object value, ValueSpecification valueSpec) {
    return getFormatter(value, valueSpec).getDataTypeForValue(value);
  }
}
