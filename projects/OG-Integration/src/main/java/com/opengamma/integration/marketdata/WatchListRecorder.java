/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata;

import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.LocalTime;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.RemoteSecuritySource;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Recorder for watch lists for tick recording.
 */
public class WatchListRecorder {

  private static final Logger s_logger = LoggerFactory.getLogger(WatchListRecorder.class);
  private static final int VALIDITY_PERIOD_DAYS = 1;

  private final ViewProcessor _viewProcessor;
  private final SecuritySource _securitySource;
  private final List<String> _schemes; // in order of preference
  private PrintWriter _writer = new PrintWriter(System.out);

  public WatchListRecorder(final ViewProcessor viewProcessor, final SecuritySource securitySource) {
    _viewProcessor = viewProcessor;
    _securitySource = securitySource;
    _schemes = new ArrayList<String>();
  }

  public void addWatchScheme(final ExternalScheme scheme) {
    _schemes.add(scheme.getName());
  }

  public void setPrintWriter(final PrintWriter writer) {
    _writer = writer;
  }

  private void emitRequirement(final ValueRequirement requirement, final Set<ExternalId> emitted, final Set<ExternalId> emittedRecently) {
    UniqueId id = requirement.getTargetSpecification().getUniqueId();
    if (requirement.getTargetSpecification().getType() == ComputationTargetType.SECURITY) {
      Security security;
      try {
        security = _securitySource.get(id);
      } catch (DataNotFoundException ex) {
        s_logger.warn("Couldn't resolve security {}", id);
        security = null;
      }
      if (security != null) {
      schemeLoop:
        for (String scheme : _schemes) {
          for (ExternalId sid : security.getExternalIdBundle()) {
            if (scheme.equals(sid.getScheme().getName())) {
              if (emittedRecently != null) {
                if (emittedRecently.add(sid)) {
                  if (!emitted.add(sid)) {
                    _writer.print("# ");
                  }
                  _writer.println(sid.toString());
                }
              } else {
                if (emitted.add(sid)) {
                  _writer.println(sid.toString());
                }
              }
              break schemeLoop;
            }
          }
        }
      }
    } else if (_schemes.contains(id.getScheme())) {
      final ExternalId sid = id.toExternalId();
      if (emittedRecently != null) {
        if (emittedRecently.add(sid)) {
          if (!emitted.add(sid)) {
            _writer.print("# ");
          }
          _writer.println(sid.toString());
        }
      } else {
        if (emitted.add(sid)) {
          _writer.println(sid.toString());
        }
      }
    }
  }
  
  private void addInterestRates(Set<ExternalId> emitted) {
    _writer.println("# Adding generated interest rates");
    String scheme = ExternalSchemes.BLOOMBERG_TICKER.getName();
    List<String> monthCodes = ImmutableList.of("H", "M", "U", "Z");
    List<String> securityCodes = ImmutableList.of("ED", "ER", "L", "ES", "EF");
    List<String> addSpace = ImmutableList.of("L");
    if (_schemes.contains(scheme)) {
      for (String securityCode : securityCodes) {
        for (int year = 0; year < 10; year++) {
          for (String month : monthCodes) {
            String idValue = securityCode;
            if (addSpace.contains(securityCode)) {
              idValue += " ";
            }
            idValue += month + year + " " + BloombergConstants.MARKET_SECTOR_COMDTY;
            ExternalId identifier = ExternalId.of(scheme, idValue);
            if (emitted.add(identifier)) {
              _writer.println(identifier.toString());
            }
          }
        }
      }
    }
  }

  public void run() {
    final Collection<ConfigItem<ViewDefinition>> viewDefinitions = _viewProcessor.getConfigSource().getAll(ViewDefinition.class, VersionCorrection.LATEST);
        
    final Set<ExternalId> emitted = Sets.newHashSet();
    final Set<ExternalId> emittedRecently = Sets.newHashSet();
    final Instant now = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).with(LocalTime.MIDDAY).toInstant();
    s_logger.info("{} view(s) defined in demo view processor", viewDefinitions.size());
    _writer.println("# Automatically generated");
    
    ViewClient client = _viewProcessor.createViewClient(UserPrincipal.getLocalUser());
    final List<CompiledViewDefinition> compilations = new LinkedList<CompiledViewDefinition>();
    client.setResultListener(new AbstractViewResultListener() {
      
      @Override
      public UserPrincipal getUser() {
        return UserPrincipal.getLocalUser();
      }
      
      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
        compilations.add(compiledViewDefinition);
      }

      @Override
      public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
        s_logger.error("Error while compiling view definition " + viewDefinitions + " for instant " + valuationTime, exception);
      }

    });
    
    for (ConfigItem<ViewDefinition> viewDefinition : viewDefinitions) {
      
      if (viewDefinition.getName().startsWith("10K")) { 
        // Don't do the huge ones!
        s_logger.warn("Skipping {}", viewDefinition);
        _writer.println();
        _writer.print("# Skipping ");
        _writer.println(viewDefinition);
        continue;
      }
      
      s_logger.debug("Compiling view {}", viewDefinition);
      _writer.println();
      _writer.println("# " + viewDefinition);
      
      client.attachToViewProcess(viewDefinition.getUniqueId(), generateExecutionOptions(now));
      try {
        client.waitForCompletion();
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted while waiting for '{}' to complete" + viewDefinition);
      }
      client.detachFromViewProcess();
      
      if (compilations.size() == 0) {
        _writer.println("# ERROR - Failed to compile " + viewDefinition);
      } else {
        _writer.println("# " + compilations.size() + " different compilations of " + viewDefinition + " for the next " + VALIDITY_PERIOD_DAYS + " days");
      }
      
      for (int i = 0; i < compilations.size(); i++) {
        CompiledViewDefinition compilation = compilations.get(i);
        final Map<ValueRequirement, ValueSpecification> liveData = compilation.getMarketDataRequirements();
        s_logger.info("{} live data requirements for view {} for compilation {}", new Object[] {liveData.size(), viewDefinition, compilation.toString()});
        _writer.println("# " + (i + 1) + " of " + compilations.size() + " - " + compilation);
        for (ValueRequirement requirement : liveData.keySet()) {
          s_logger.debug("Requirement {}", requirement);
          emitRequirement(requirement, emitted, emittedRecently);
        }
        _writer.flush();
        emittedRecently.clear();
      }
      
      compilations.clear();
    }
    client.shutdown();
    addInterestRates(emitted);
  }

  private ViewExecutionOptions generateExecutionOptions(Instant now) {
    List<ViewCycleExecutionOptions> executionOptionsList = new ArrayList<ViewCycleExecutionOptions>();
    for (int i = 0; i < VALIDITY_PERIOD_DAYS; i++) {
      Instant valuationTime = now.plus(i, TimeUnit.DAYS);
      executionOptionsList.add(new ViewCycleExecutionOptions(valuationTime));
    }
    ViewCycleExecutionSequence executionSequence = new ArbitraryViewCycleExecutionSequence(executionOptionsList);
    return ExecutionOptions.of(executionSequence, ExecutionFlags.none().compileOnly().get());
  }

  //-------------------------------------------------------------------------
  /**
   * Main entry point.
   * 
   * @param args  the arguments
   * @throws Exception if an error occurs
   */
  public static void main(final String[] args) throws Exception {  // CSIGNORE
    CommandLineParser parser = new PosixParser();
    Options options = new Options();
    Option outputFileOpt = new Option("o", "output", true, "output file");
    outputFileOpt.setRequired(true);
    options.addOption(outputFileOpt);
    Option urlOpt = new Option("u", "url", true, "server url");
    options.addOption(urlOpt);
    String outputFile = "watchList.txt";
    String url = "http://localhost:8080/jax";
    try {
      CommandLine cmd = parser.parse(options, args);
      outputFile = cmd.getOptionValue("output");
      url = cmd.getOptionValue("url");
    } catch (ParseException exp) {
      s_logger.error("Option parsing failed: {}", exp.getMessage());
      System.exit(0);
    }
    
    final WatchListRecorder recorder = create(URI.create(url), "main", "combined");
    recorder.addWatchScheme(ExternalSchemes.BLOOMBERG_TICKER);
    recorder.addWatchScheme(ExternalSchemes.BLOOMBERG_BUID);
    final PrintWriter pw = new PrintWriter(outputFile);
    recorder.setPrintWriter(pw);
    recorder.run();
    
    pw.close();
    System.exit(0);
  }

  private static WatchListRecorder create(final URI serverUri, final String viewProcessorClassifier, final String securitySourceClassifier) {
    RemoteComponentServer remote = new RemoteComponentServer(serverUri);
    ComponentServer server = remote.getComponentServer();
    ComponentInfo viewProcessorInfo = server.getComponentInfo(ViewProcessor.class, viewProcessorClassifier);
    ComponentInfo securitySourceInfo = server.getComponentInfo(SecuritySource.class, securitySourceClassifier);
    
    URI uri = URI.create(viewProcessorInfo.getAttribute(ComponentInfoAttributes.JMS_BROKER_URI));
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(uri);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName("WatchListRecorder");
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(uri);
    JmsConnector jmsConnector = factory.getObjectCreating();
    
    ViewProcessor viewProcessor = new RemoteViewProcessor(viewProcessorInfo.getUri(), jmsConnector, Executors.newSingleThreadScheduledExecutor());
    SecuritySource securitySource = new RemoteSecuritySource(securitySourceInfo.getUri());
    return new WatchListRecorder(viewProcessor, securitySource);
  }

}
