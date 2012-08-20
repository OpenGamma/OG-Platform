/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.cds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class ISDAStagedDataManager {

  private static Pattern gridRegex = Pattern.compile("([A-Z]{3}+)_([0-9]{8}+)\\.xls", Pattern.CASE_INSENSITIVE);

  private static DateTimeFormatter gridFormatter = DateTimeFormatters.pattern("yyyyMMdd");

  private static DateTimeFormatter stagedFormatter = DateTimeFormatters.pattern("dd_MM_yyyy");

  private static String resourceDirectory = "resources";
  private static String stagedCurveDirectory = "isda_staged_curves";
  private static String stagedCurveBaseName = "IsdaIrCurve_";

  public ISDAStagedCurve loadStagedCurveForGrid(final String gridFilename) throws IOException, JAXBException {

    InputStream is = null;

    try {

      final Matcher matcher = gridRegex.matcher(gridFilename);

      if (!matcher.matches()) {
        return null;
      }

      final LocalDate testDate = LocalDate.parse(matcher.group(2), gridFormatter);
      final String path = resourceDirectory + File.separator + stagedCurveDirectory + File.separator
          + stagedCurveBaseName + matcher.group(1) + "_" + testDate.toString(stagedFormatter) + ".xml";
      is = getClass().getClassLoader().getResourceAsStream(path);

      if (is == null) {
        return null;
      }

      final JAXBContext jaxbContext = JAXBContext.newInstance(ISDAStagedCurve.class);
      final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (ISDAStagedCurve) jaxbUnmarshaller.unmarshal(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

}
