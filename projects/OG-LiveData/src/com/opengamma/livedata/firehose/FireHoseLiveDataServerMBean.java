/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.firehose;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.server.LiveDataServerMBean;

/**
 * JMX management of a FireHoseLiveData server.
 */
public class FireHoseLiveDataServerMBean extends LiveDataServerMBean {

  public FireHoseLiveDataServerMBean(final FireHoseLiveDataServer server) {
    super(server);
  }

  @Override
  protected FireHoseLiveDataServer getServer() {
    return (FireHoseLiveDataServer) super.getServer();
  }

  /**
   * Override if the raw market data should be post-processed before returning to the user (e.g. substitute field
   * names in the Fudge messages). The parameter passed is immutable - if different data should be returned a new
   * map must be allocated.
   * 
   * @param marketValues the values to post-process
   * @return the updates values (or the original map if there are no changes)
   */
  protected Map<String, FudgeMsg> exportStateOfTheWorldImpl(final Map<String, FudgeMsg> marketValues) {
    return marketValues;
  }

  protected String csvEscape(final String csv) {
    for (int i = 0; i < csv.length(); i++) {
      char c = csv.charAt(i);
      if ((c <= 32) || (c == '\"') || (c == ',')) {
        final StringBuilder sb = new StringBuilder(csv.length() * 2).append('\"');
        for (int j = 0; j < i; j++) {
          sb.append(csv.charAt(j));
        }
        for (int j = i; j < csv.length(); j++) {
          c = csv.charAt(j);
          if (c == '\"') {
            sb.append("\"\"");
          } else if (c < 32) {
            final String oct = Integer.toOctalString(c);
            sb.append("\\0");
            if (oct.length() < 2) {
              sb.append('0');
            }
            sb.append(oct);
          } else {
            sb.append(c);
          }
        }
        return sb.append('\"').toString();
      }
    }
    return csv;
  }

  @ManagedOperation(description = "Exports the current (unnormalized) 'state-of-the-world' to a CSV file.")
  @ManagedOperationParameters({@ManagedOperationParameter(name = "filename", description = "Filename to write.)") })
  public void exportStateOfTheWorld(final String filename) {
    try {
      if (!(getServer().getFireHose() instanceof AbstractFireHoseLiveData)) {
        throw new OpenGammaRuntimeException("Underlying fire hose is not an AbstractFireHoseLiveData");
      }
      Map<String, FudgeMsg> marketValues = ((AbstractFireHoseLiveData) getServer().getFireHose()).getMarketValues();
      marketValues = exportStateOfTheWorldImpl(marketValues);
      Collection<String> fields = new HashSet<String>();
      for (Map.Entry<String, FudgeMsg> marketValue : marketValues.entrySet()) {
        for (FudgeField field : marketValue.getValue()) {
          if (field.getName() != null) {
            fields.add(field.getName());
          }
        }
      }
      fields = new ArrayList<String>(fields);
      Collections.sort((List<String>) fields);
      final PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)));
      out.print("Identifier");
      for (String field : fields) {
        out.print(',');
        out.print(field);
      }
      out.println();
      for (Map.Entry<String, FudgeMsg> marketValue : marketValues.entrySet()) {
        out.print(csvEscape(marketValue.getKey()));
        for (String field : fields) {
          out.print(',');
          final Object value = marketValue.getValue().getValue(field);
          if (value != null) {
            out.print(csvEscape(value.toString()));
          }
        }
        out.println();
      }
      out.close();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Couldn't write to '" + filename + "' - " + e.getMessage());
    }
  }

  /**
   * Returns an indication as to whether the full market data has been loaded yet or not.
   * 
   * @return true if all data has been loaded, false if it is still loading (or the connection has failed)
   */
  @ManagedAttribute(description = "Indicates whether the full 'state-of-the-world' has been received yet (if supported).")
  public boolean isMarketDataComplete() {
    return getServer().getFireHose().isMarketDataComplete();
  }

  /**
   * Returns an indication of the number of lines of market data available (i.e. that which would be exported
   * by {@link #exportStateOfTheWorld}.
   * 
   * @return the number of market data lines, or -1 if this is not supported
   */
  @ManagedAttribute(description = "Indicates the number of market data lives available (or -1 if unsupported).")
  public int getMarketDataSize() {
    if (getServer().getFireHose() instanceof AbstractFireHoseLiveData) {
      return ((AbstractFireHoseLiveData) getServer().getFireHose()).getMarketValues().size();
    } else {
      return -1;
    }
  }

}
