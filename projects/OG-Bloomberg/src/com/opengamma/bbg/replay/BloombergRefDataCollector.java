/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConnectorFactoryBean;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.cli.BloombergCliOptions;
import com.opengamma.bbg.cli.BloombergCliOptions.Builder;
import com.opengamma.bbg.livedata.LoggingReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A collector of Bloomberg data.
 */
public class BloombergRefDataCollector implements Lifecycle {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergRefDataCollector.class);
  /** Fudge context. */
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  /**
   * The watch list file.
   */
  private final File _watchListFile;
  /**
   * The fields file.
   */
  private final File _fieldsFile;
  /**
   * The reference data provider.
   */
  private final LoggingReferenceDataProvider _loggingRefDataProvider;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * Whether the service has started.
   */
  private final AtomicBoolean _started = new AtomicBoolean();

  /**
   * Create an instance.
   * 
   * @param fudgeContext  the fudgeContext, not null
   * @param watchListFile  the watch list file, not null
   * @param refDataProvider  the reference data provider, not null
   * @param fieldsFile  the file containing the fields
   * @param outputFile  the output file, not null
   */
  public BloombergRefDataCollector(FudgeContext fudgeContext, File watchListFile, ReferenceDataProvider refDataProvider, File fieldsFile, File outputFile) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(watchListFile, "watch list file");
    ArgumentChecker.notNull(refDataProvider, "reference data provider");
    ArgumentChecker.notNull(fieldsFile, "fields input file");
    ArgumentChecker.notNull(outputFile, "out put file");
    _fudgeContext = fudgeContext;
    _watchListFile = watchListFile;
    _fieldsFile = fieldsFile;
    _loggingRefDataProvider = new LoggingReferenceDataProvider(refDataProvider, _fudgeContext, outputFile);
  }

  /**
   * Create an instance.
   * 
   * @param watchListFile  the watch list file, not null
   * @param refDataProvider  the reference data provider, not null
   * @param fieldsFile  the file containing the fields
   * @param outputFile  the output file, not null
   */
  public BloombergRefDataCollector(File watchListFile, ReferenceDataProvider refDataProvider, File fieldsFile, File outputFile) {
    this(s_fudgeContext, watchListFile, refDataProvider, fieldsFile, outputFile);
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized void start() {
    s_logger.info("starting bloombergRefDataCollector");
    if (isRunning()) {
      s_logger.info("bloombergRefDataCollector is already running");
      return;
    }
    _started.set(true);
    _loggingRefDataProvider.getReferenceData(loadSecurities(), loadFields());
    _started.set(false);
  }

  private Set<String> loadFields() {
    Set<String> fields = Sets.newHashSet();
    LineIterator it;
    try {
      it = FileUtils.lineIterator(_fieldsFile);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("IOException when reading " + _fieldsFile, ex);
    }
    try {
      while (it.hasNext()) {
        String line = it.nextLine();
        if (StringUtils.isBlank(line) || line.charAt(0) == '#') {
          continue;
        }
        fields.add(line);
      }
    } finally {
      LineIterator.closeQuietly(it);
    }
    return fields;
  }

  private Set<String> loadSecurities() {
    Set<String> bloombergKeys = Sets.newHashSet();
    try {
      for (ExternalId identifier : BloombergDataUtils.identifierLoader(new FileReader(_watchListFile))) {
        bloombergKeys.add(BloombergDomainIdentifierResolver.toBloombergKey(identifier));
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException(_watchListFile + " cannot be found", ex);
    }
    return bloombergKeys;
  }

  @Override
  public synchronized void stop() {
    s_logger.info("stopping bloombergRefDataCollector");
    _started.set(false);
  }

  @Override
  public synchronized boolean isRunning() {
    return _started.get() == true;
  }

  //-------------------------------------------------------------------------
  /**
   * Main entry point from command line
   * 
   * @param args the args
   */
  public static void main(String[] args) { //CSIGNORE
    BloombergCliOptions bbgOptions = createOptions();
    processCommandLineOptions(args, bbgOptions);
  }

  private static void processCommandLineOptions(String[] args, BloombergCliOptions bbgOptions) {
    CommandLine cmdLine = bbgOptions.parse(args);
    if (cmdLine == null) {
      bbgOptions.printUsage(BloombergRefDataCollector.class);
      return;
    }
    if (cmdLine.getOptionValue(BloombergCliOptions.HELP_OPTION) != null) {
      bbgOptions.printUsage(BloombergRefDataCollector.class);
      return;
    }
    String dataFieldFile = cmdLine.getOptionValue(BloombergCliOptions.FIELDS_FILE_OPTION);
    String identifiersFile = cmdLine.getOptionValue(BloombergCliOptions.IDENTIFIERS_OPTION);
    String outputFile = cmdLine.getOptionValue(BloombergCliOptions.OUPUT_OPTION);
    String host = cmdLine.getOptionValue(BloombergCliOptions.HOST_OPTION);
    String port = cmdLine.getOptionValue(BloombergCliOptions.PORT_OPTION);
    
    if (port == null) {
      port = BloombergConstants.DEFAULT_PORT;
    }
    
    s_logger.info("loading ref data with host: {} port: {} fields: {} identifies: {} outputfile {}", new Object[]{host, port, dataFieldFile, identifiersFile, outputFile});
    
    BloombergConnectorFactoryBean factory = new BloombergConnectorFactoryBean("BloombergRefDataCollector", host, Integer.valueOf(port));
    BloombergConnector bloombergConnector = factory.getObjectCreating();
    BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(bloombergConnector);
    refDataProvider.start();
    
    BloombergRefDataCollector refDataCollector = new BloombergRefDataCollector(new File(identifiersFile), refDataProvider, new File(dataFieldFile), new File(outputFile));
    refDataCollector.start();
  }

  private static BloombergCliOptions createOptions() {
    Builder builder = new BloombergCliOptions.Builder()
      .withDataFieldsFile(true)
      .withIdentifiers(true)
      .withOutput(true)
      .withHost(true)
      .withPort(false);
    return builder.build();
  }

}
