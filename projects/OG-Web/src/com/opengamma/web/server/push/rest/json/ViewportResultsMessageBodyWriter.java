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
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.ViewportResults;
import com.opengamma.web.server.push.analytics.formatting.ResultsFormatter;

/**
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ViewportResultsMessageBodyWriter implements MessageBodyWriter<ViewportResults> {

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
    List<List<ViewportResults.Cell>> viewportCells = results.getResults();
    List<List<Object>> allResults = Lists.newArrayListWithCapacity(viewportCells.size());
    for (List<ViewportResults.Cell> rowCells : viewportCells) {
      List<Object> rowResults = Lists.newArrayListWithCapacity(rowCells.size());
      for (ViewportResults.Cell cell : rowCells) {
        Object formattedValue;
        if (cell == null) {
          formattedValue = "";
        } else {
          if (results.isExpanded()) {
            formattedValue = _formatter.formatForExpandedDisplay(cell.getValue(), cell.getValueSpecification());
          } else {
            formattedValue = _formatter.formatForDisplay(cell.getValue(), cell.getValueSpecification());
          }
        }
        Object history = cell == null ? null : cell.getHistory();
        if (history != null) {
          Object formattedHistory = _formatter.formatForHistory(history, cell.getValueSpecification());
          Object[] valueWithHistory = {formattedValue, formattedHistory};
          rowResults.add(valueWithHistory);
        } else {
          rowResults.add(formattedValue);
        }
      }
      allResults.add(rowResults);
    }
    entityStream.write(new JSONArray(allResults).toString().getBytes());
  }
}
