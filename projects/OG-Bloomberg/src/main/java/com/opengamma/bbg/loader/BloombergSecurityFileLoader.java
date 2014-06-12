/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_CHAIN;
import static com.opengamma.bbg.BloombergConstants.FIELD_OPT_EXPIRE_DT;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.DefaultSecurityLoader;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Loads security data from Bloomberg and add/update the security data to SecurityMaster
 * <p>
 * Security Identifiers to load is read from a supplied file or from position master, which one to used is specified
 * from command line options
 * <pre>
 *    usage: java com.opengamma.bbg.loader.BloombergSecurityFileLoader [options]... [files]...
 *    -h,--help                                       Print this message
 *    -o,--optionSize [size]                          First x number of options to load
 *    -s,--scheme [BLOOMBERG_TICKER, BLOOMBERG_BUID]  Identification scheme
 * </pre>
 */
public class BloombergSecurityFileLoader {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSecurityFileLoader.class);

  /* package */static final String CONTEXT_CONFIGURATION_PATH = "/com/opengamma/bbg/loader/bloomberg-security-loader-context.xml";

  private static final String HELP_OPTION = "help";
  private static final String OPTION = "option";
  private static final String IDENTIFICATION_SCHEME = "scheme";

  private final DefaultSecurityLoader _securityLoader;
  private ReferenceDataProvider _refDataProvider;
  private String[] _files;
  private int _optionSize;
  private ExternalScheme _scheme;

  /**
   * Creates an instance.
   * 
   * @param securityMaster  the security master, not null
   * @param securityProvider  bloomberg security loader, not null
   */
  public BloombergSecurityFileLoader(final SecurityProvider securityProvider, final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securityLoader = new DefaultSecurityLoader(securityMaster, securityProvider);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the refDataProvider field.
   * @return the refDataProvider
   */
  public ReferenceDataProvider getRefDataProvider() {
    return _refDataProvider;
  }

  /**
   * Sets the reference data provider.
   * @param refDataProvider  the reference data provider
   */
  public void setRefDataProvider(ReferenceDataProvider refDataProvider) {
    _refDataProvider = refDataProvider;
  }
  
  /**
   * Gets the scheme field.
   * @return the scheme
   */
  public ExternalScheme getScheme() {
    return _scheme;
  }

  /**
   * Sets the scheme field.
   * @param scheme  the scheme
   */
  public void setScheme(ExternalScheme scheme) {
    _scheme = scheme;
  }

  //-------------------------------------------------------------------------
  public void run() {
    
    Set<ExternalIdBundle> identifiers = readInputFiles();
    
    Map<ExternalIdBundle, UniqueId> loadedSecurities = _securityLoader.loadSecurities(identifiers);
    List<ExternalIdBundle> errors = findErrors(loadedSecurities);
    if (_optionSize > 0) {
      Set<ExternalIdBundle> optionIdentifiers = loadOptionIdentifiers(identifiers);
      Map<ExternalIdBundle, UniqueId> loadedOptionSecurities = _securityLoader.loadSecurities(optionIdentifiers);
      errors.addAll(findErrors(loadedOptionSecurities));
    }
   
    //print securities with errors
    if (!errors.isEmpty()) {
      s_logger.warn("Unable to load the following securities");
      for (ExternalIdBundle identifierBundle : errors) {
        s_logger.warn("{}", identifierBundle);
      }
    }
    
//    for (ExternalIdBundle bundle : findSucesses(loadedSecurities)) {
//      System.err.println(bundle.getIdentifier(SecurityUtils.ISIN));
//    }
  }

  private List<ExternalIdBundle> findErrors(final Map<ExternalIdBundle, UniqueId> loadedSecurities) {
    List<ExternalIdBundle> result = Lists.newArrayList();
    for (Entry<ExternalIdBundle, UniqueId> entry : loadedSecurities.entrySet()) {
      if (entry.getValue() == null) {
        result.add(entry.getKey());
      }
    }
    return result;
  }

  private Set<ExternalIdBundle> loadOptionIdentifiers(final Set<ExternalIdBundle> identifiers) {
    Set<String> bloombergKeys = new HashSet<String>();
    for (ExternalIdBundle dsids : identifiers) {
      ExternalId preferredIdentifier = BloombergDomainIdentifierResolver.resolvePreferredIdentifier(dsids);
      bloombergKeys.add(BloombergDomainIdentifierResolver.toBloombergKey(preferredIdentifier));
    }

    Set<ExternalIdBundle> optionsIdentifiers = new HashSet<ExternalIdBundle>();
    Map<String, FudgeMsg> refDataResultMap = _refDataProvider.getReferenceDataIgnoreCache(bloombergKeys, Collections.singleton(FIELD_OPT_CHAIN));
    for (Entry<String, FudgeMsg> entry : refDataResultMap.entrySet()) {
      FudgeMsg fieldContainer = entry.getValue();
      //process the options
      int nAdded = 0;
      for (FudgeField field : fieldContainer.getAllByName(BloombergConstants.FIELD_OPT_CHAIN)) {
        FudgeMsg optionContainer = (FudgeMsg) field.getValue();
        String optionTickerStr = optionContainer.getString("Security Description");
        String expiryStr = ReferenceDataProviderUtils.singleFieldSearchIgnoreCache(optionTickerStr, FIELD_OPT_EXPIRE_DT, _refDataProvider);
        LocalDate expiryLocalDate = null;
        try {
          expiryLocalDate = LocalDate.parse(expiryStr);
        } catch (Exception e) {
          throw new OpenGammaRuntimeException(expiryStr + " returned from bloomberg not in format yyyy-mm-dd", e);
        }
        int year = expiryLocalDate.getYear();
        int month = expiryLocalDate.getMonthValue();
        int day = expiryLocalDate.getDayOfMonth();
        Expiry expiry = new Expiry(DateUtils.getUTCDate(year, month, day));

        if (expiry.getExpiry().toInstant().toEpochMilli() < (System.currentTimeMillis() + (25L * 60L * 60L * 1000L))) {
          s_logger.info("Option {} in future, so passing on it.", optionTickerStr);
          continue;
        }

        ExternalId optionTicker = ExternalSchemes.bloombergTickerSecurityId(optionTickerStr);
        optionsIdentifiers.add(ExternalIdBundle.of(optionTicker));

        nAdded++;
        if (nAdded >= _optionSize) {
          break;
        }
      }
    }
    return optionsIdentifiers;
  }

  /**
   * Sets the files field.
   * @param files  the files
   */
  public void setFiles(String[] files) {
    _files = files;
  }

  /**
   * Sets the optionSize field.
   * @param optionSize  the optionSize
   */
  public void setOptionSize(int optionSize) {
    _optionSize = optionSize;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {  // CSIGNORE
    Options options = createOptions();
    processCommandLineOptions(args, options);
  }

  /**
   * @param args
   * @param options
   */
  private static void processCommandLineOptions(String[] args, Options options) {
    CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      usage(options);
      return;
    }
    if (line.hasOption(HELP_OPTION)) {
      usage(options);
      return;
    }

    String[] files = line.getArgs();
    BloombergSecurityFileLoader securityLoader = getBloombergSecurityFileLoader();
    securityLoader.setFiles(files);
    
    if (line.hasOption(OPTION)) {
      try {
        securityLoader.setOptionSize(Integer.parseInt(line.getOptionValue(OPTION)));
      } catch (NumberFormatException ex) {
        s_logger.warn("{} option size is not an integer", line.getOptionValue(OPTION));
      }
    }
    
    if (line.hasOption(IDENTIFICATION_SCHEME)) {
      String schemeValue = line.getOptionValue(IDENTIFICATION_SCHEME);
      securityLoader.setScheme(ExternalScheme.of(schemeValue));
    } else {
      securityLoader.setScheme(ExternalSchemes.BLOOMBERG_TICKER);
    }
    securityLoader.run();
  }
  
  private static Options createOptions() {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createOptionSize());
    options.addOption(createSchemeOption());
    return options;
  }

  private static Option createSchemeOption() {
    OptionBuilder.withLongOpt(IDENTIFICATION_SCHEME);
    OptionBuilder.withDescription("Identification scheme");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("BLOOMBERG_TICKER, BLOOMBERG_BUID");
    return OptionBuilder.create("s");
  }

  private static Option createHelpOption() {
    return new Option("h", HELP_OPTION, false, "Print this message");
  }

  private static Option createOptionSize() {
    OptionBuilder.withLongOpt(OPTION);
    OptionBuilder.withDescription("First x number of options to load");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("size");
    return OptionBuilder.create("o");
  }

  private static void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + BloombergSecurityFileLoader.class.getName() + " [options]... [files]...", options);
  }

  private Set<ExternalIdBundle> readInputFiles() {
    Set<String> securities = new HashSet<String>();
    if (_files != null) {
      for (String file : _files) {
        try {
          securities.addAll(FileUtils.readLines(new File(file)));
        } catch (IOException e) {
          s_logger.warn("Problem reading from input file={}", file);
          throw new OpenGammaRuntimeException("Problem reading from " + file, e);
        }
      }
    }
    Set<ExternalIdBundle> result = new HashSet<ExternalIdBundle>();
    for (String secDes : securities) {
      if (!StringUtils.isBlank(secDes)) {
        result.add(ExternalIdBundle.of(ExternalId.of(getScheme(), secDes.trim())));
      }
    }
    return result;
  }

  private static BloombergSecurityFileLoader getBloombergSecurityFileLoader() {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONTEXT_CONFIGURATION_PATH);
    context.start();
    BloombergSecurityFileLoader loader = (BloombergSecurityFileLoader) context.getBean("securityLoader");
    return loader;
  }

}
