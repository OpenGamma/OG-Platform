/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.ViewportResults;
import com.opengamma.web.server.push.analytics.formatting.ResultsFormatter;

/**
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ViewportResultsMessageBodyWriter implements MessageBodyWriter<ViewportResults> {

  private static final String VALUE_KEY = "v";
  private static final String HISTORY_KEY = "h";
  private static final String TYPE_KEY = "t";
  private static final String DATA = "data";

  private final ResultsFormatter _formatter;

  public ViewportResultsMessageBodyWriter(ResultsFormatter formatter) {
    ArgumentChecker.notNull(formatter, "formatter");
    _formatter = formatter;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.equals(ViewportResults.class);
  }

  @Override
  public long getSize(ViewportResults results,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(ViewportResults results,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    // TODO move this to a JSON writer class
    List<List<ViewportResults.Cell>> viewportCells = results.getResults();
    List<List<Object>> allResults = Lists.newArrayListWithCapacity(viewportCells.size());
    for (List<ViewportResults.Cell> rowCells : viewportCells) {
      List<Object> rowResults = Lists.newArrayListWithCapacity(rowCells.size());
      int viewportColIndex = 0;
      for (ViewportResults.Cell cell : rowCells) {
        Object formattedValue;
        Object cellValue = cell.getValue();
        ValueSpecification cellValueSpec = cell.getValueSpecification();
        if (results.isExpanded()) {
          formattedValue = _formatter.formatForExpandedDisplay(cellValue, cellValueSpec);
        } else {
          formattedValue = _formatter.formatForDisplay(cellValue, cellValueSpec);
        }
        Collection<Object> history = cell.getHistory();
        Class<?> columnType = results.getColumnType(viewportColIndex++);

        if (columnType == null || history != null) {
          // if there is history or we need to send type info then we need to send an object, not just the value
          Map<String, Object> valueMap = Maps.newHashMap();
          valueMap.put(VALUE_KEY, formattedValue);
          // if the the column type isn't known then send the type with the value
          if (columnType == null) {
            Class<?> cellValueClass = cellValue == null ? null : cellValue.getClass();
            valueMap.put(TYPE_KEY, _formatter.getFormatType(cellValueClass).name());
          }
          if (history != null) {
            List<Object> formattedHistory = Lists.newArrayListWithCapacity(history.size());
            for (Object historyValue : history) {
              formattedHistory.add(_formatter.formatForHistory(historyValue, cellValueSpec));
            }
            valueMap.put(HISTORY_KEY, formattedHistory);
          }
          rowResults.add(valueMap);
        } else {
          rowResults.add(formattedValue);
        }
      }
      allResults.add(rowResults);
    }
    ImmutableMap<String, Object> resultsMap = ImmutableMap.of(ViewportVersionMessageBodyWriter.VERSION, results.getVersion(),
                                                              DATA, allResults);
    entityStream.write(new JSONObject(resultsMap).toString().getBytes());
  }
}
