package com.opengamma.financial.analytics.test.unittest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.impl.NonVersionedRedisHistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

public class CurveFixingTSLoader {

  private static final Logger s_logger = LoggerFactory.getLogger(CurveFixingTSLoader.class);
  private final NonVersionedRedisHistoricalTimeSeriesSource _timeSeriesSource;

  public CurveFixingTSLoader(NonVersionedRedisHistoricalTimeSeriesSource timeSeriesSource) {
    ArgumentChecker.notNull(timeSeriesSource, "timeSeriesSource");
    _timeSeriesSource = timeSeriesSource;
  }

  /**
   * Gets the timeSeriesSource.
   * @return the timeSeriesSource
   */
  protected NonVersionedRedisHistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  public void loadCurveFixingCSVFile(String fileName) {
    loadCurveFixingCSVFile(new File(fileName));
  }

  public void loadCurveFixingCSVFile(File file) {
    s_logger.info("Loading from file {}", file.getAbsolutePath());
    try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
      loadCurveFixingCSVFile(stream);
    } catch (IOException ioe) {
      s_logger.error("Unable to open file " + file, ioe);
      throw new OpenGammaRuntimeException("Unable to open file " + file, ioe);
    }
  }

  public void loadCurveFixingCSVFile(InputStream stream) throws IOException {
    // The calling code is responsible for closing the underlying stream.
    @SuppressWarnings("resource")
    //assume first line is the header
    CSVReader csvReader = new CSVReader(new InputStreamReader(stream), CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1);

    String[] currLine = null;
    int lineNum = 0;
    Map<UniqueId, LocalDateDoubleTimeSeriesBuilder> timeseriesMap = Maps.newHashMap();
    while ((currLine = csvReader.readNext()) != null) {
      lineNum++;
      if ((currLine.length == 0) || currLine[0].startsWith("#")) {
        s_logger.debug("Empty line on {}", lineNum);
      } else if (currLine.length != 4) {
        s_logger.error("Invalid number of fields ({}) in CSV on line {}", currLine.length, lineNum);
      } else {
        final String curveName = StringUtils.trimToNull(currLine[0]);
        if (curveName == null) {
          s_logger.error("Invalid curve name in CSV on line {}", lineNum);
          continue;
        }
        final String tenor = StringUtils.trimToNull(currLine[1]);
        if (tenor == null) {
          s_logger.error("Invalid tenor: {} in CSV on line {}", currLine[1], lineNum);
          continue;
        }
        final String dateStr = StringUtils.trimToNull(currLine[2]);
        LocalDate date = null;
        try {
          date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException ex) {
          s_logger.error("Invalid date format in CSV on line {}", lineNum);
          continue;
        }
        final String valueStr = StringUtils.trimToNull(currLine[3]);
        Double value = null;
        try {
          value = Double.parseDouble(valueStr);
        } catch (NumberFormatException ex) {
          s_logger.error("Invalid amount in CSV on line {}", lineNum);
          continue;
        }
        String idName = String.format("%s-%s", curveName, tenor);
        UniqueId uniqueId = UniqueId.of(ExternalSchemes.ISDA.getName(), idName);

        LocalDateDoubleTimeSeriesBuilder tsBuilder = timeseriesMap.get(uniqueId);
        if (tsBuilder == null) {
          tsBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
          timeseriesMap.put(uniqueId, tsBuilder);
        }
        tsBuilder.put(date, value);
      }
    }
    s_logger.info("Populating {} time series for fixing data", timeseriesMap.size());
    for (Entry<UniqueId, LocalDateDoubleTimeSeriesBuilder> entry : timeseriesMap.entrySet()) {
      s_logger.info("Fixing series {} has {} elements", entry.getKey(), entry.getValue().size());
      getTimeSeriesSource().updateTimeSeries(entry.getKey(), entry.getValue().build());
    }
  }
}
