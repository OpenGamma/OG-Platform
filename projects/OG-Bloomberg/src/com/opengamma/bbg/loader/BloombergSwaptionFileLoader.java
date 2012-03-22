/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.MongoDBCachingReferenceDataProvider;
import com.opengamma.bbg.PerSecurityReferenceDataResult;
import com.opengamma.bbg.ReferenceDataResult;
import com.opengamma.bbg.test.MongoCachedReferenceData;

/**
 * Little util for parsing swaption files
 */
public class BloombergSwaptionFileLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSwaptionFileLoader.class);
  
  /* package */static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-security-loader-context.xml";
  private static final int NAME_FIELD = 0;
  private static final int BUID_FIELD = 8;
  private static BloombergReferenceDataProvider getBloombergSecurityFileLoader() {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    BloombergReferenceDataProvider dataProvider = (BloombergReferenceDataProvider) context.getBean("refDataProvider");
    return dataProvider;
  }
  
  /**
   * Little util to parse swaption tickers into a csv for further analysis.
   * @param args command line params
   */
  public static void main(String[] args) {  // CSIGNORE
    try {
      BufferedReader in = new BufferedReader(new FileReader(args[0]));
      BufferedWriter out = new BufferedWriter(new FileWriter(args[1]));
      CSVReader csvReader = new CSVReader(in);
      CSVWriter csvWriter = new CSVWriter(out);
      String[] line;
      Pattern pattern = Pattern.compile("^(\\w\\w\\w).*?(\\d+)(M|Y)(\\d+)(M|Y)\\s*?(PY|RC)\\s*?(.*)$");
      BloombergReferenceDataProvider rawBbgRefDataProvider = getBloombergSecurityFileLoader();
      MongoDBCachingReferenceDataProvider bbgRefDataProvider = MongoCachedReferenceData.makeMongoProvider(rawBbgRefDataProvider, BloombergSwaptionFileLoader.class);
      while ((line = csvReader.readNext()) != null) {
        String name = line[NAME_FIELD];
        Matcher matcher = pattern.matcher(name);
        if (matcher.matches()) {
          String ccy = matcher.group(1); 
          String swapTenorSize = matcher.group(2);
          String swapTenorUnit = matcher.group(3);
          String optionTenorSize = matcher.group(4);
          String optionTenorUnit = matcher.group(5);
          String payReceive = matcher.group(6);
          String distanceATM = matcher.group(7);
          
          String buid = "/buid/" + line[BUID_FIELD];
          ReferenceDataResult fields = bbgRefDataProvider.getFields(Collections.singleton(buid), Collections.singleton("TICKER"));
          PerSecurityReferenceDataResult result = fields.getResult(buid);
          FudgeMsg fieldData = result.getFieldData();
          String value = fieldData.getString("TICKER");
          csvWriter.writeNext(new String[] {name, ccy, swapTenorSize, swapTenorUnit, optionTenorSize, optionTenorUnit, payReceive, distanceATM, value });
        } else {
          s_logger.error("Couldn't parse " + name + " field");
        }
        
      }
    } catch (IOException ioe) {
      s_logger.error("Error while reading file", ioe);
    }
  }

}
