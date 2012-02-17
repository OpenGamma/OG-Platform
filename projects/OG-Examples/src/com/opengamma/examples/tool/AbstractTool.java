/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tool;

import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.component.factory.tool.ToolContextUtils;
import com.opengamma.financial.portfolio.loader.LoaderContext;
import com.opengamma.financial.tool.ToolContext;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractTool {

  /**
   * The tool context.
   */
  private ToolContext _toolContext;

  /**
   * Initializes the tool statically.
   * 
   * @return true if successful
   */
  public static final boolean init() {
    try {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      URL logbackResource = ClassLoader.getSystemResource("com/opengamma/examples/server/logback.xml");
      configurator.doConfigure(logbackResource);
      return true;
      
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Runs the tool.
   * This starts the tool context and calls {@link #run(ToolContext)}.
   * This will catch exceptions and print a stack trace.
   * 
   * @return true if successful
   */
  public final boolean run() {
    try {
      System.out.println("Starting " + getClass().getSimpleName());
      ToolContext toolContext = ToolContextUtils.getToolContext("classpath:toolcontext/toolcontext-example.properties");
      System.out.println("Running " + getClass().getSimpleName());
      run(toolContext);
      System.out.println("Finished " + getClass().getSimpleName());
      return true;
      
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    } finally {
      if (_toolContext != null) {
        _toolContext.close();
      }
    }
  }

  /**
   * Runs the tool, calling {@code doRun}.
   * This will catch not handle exceptions.
   * 
   * @param toolContext  the tool context, not null
   * @throws RuntimeException if an error occurs
   */
  public final void run(ToolContext toolContext) {
    _toolContext = toolContext;
    doRun();
  }

  /**
   * Override in subclasses to implement the tool.
   * 
   * @throws RuntimeException if an error occurs
   */
  protected abstract void doRun();

  /**
   * Gets the tool context.
   * 
   * @return the context, not null during {@code doRun}
   */
  protected ToolContext getToolContext() {
    return _toolContext;
  }

  /**
   * Gets the loader context.
   * 
   * @return the loader context, not null
   */
  protected LoaderContext getLoaderContext() {
    LoaderContext loaderContext = new LoaderContext();
    loaderContext.setConfigMaster(getToolContext().getConfigMaster());
    loaderContext.setSecurityMaster(getToolContext().getSecurityMaster());
    loaderContext.setPositionMaster(getToolContext().getPositionMaster());
    loaderContext.setPortfolioMaster(getToolContext().getPortfolioMaster());
    loaderContext.setHistoricalTimeSeriesMaster(getToolContext().getHistoricalTimeSeriesMaster());
    loaderContext.setExchangeSource(getToolContext().getExchangeSource());
    loaderContext.setHolidaySource(getToolContext().getHolidaySource());
    loaderContext.setSecuritySource(getToolContext().getSecuritySource());
    loaderContext.setHistoricalTimeSeriesSource(getToolContext().getHistoricalTimeSeriesSource());
    loaderContext.setConventionBundleSource(getToolContext().getConventionBundleSource());
    return loaderContext;
  }

}
