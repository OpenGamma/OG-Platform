/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.FileWriter;

import net.sf.saxon.event.StreamWriterToReceiver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;

import com.google.common.base.Throwables;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class GoldenCopyCreationTool extends AbstractTool<ToolContext> {

  public static void main(String[] args) {
    new GoldenCopyCreationTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {

    CommandLine commandLine = getCommandLine();

    String viewName = commandLine.getOptionValue("view-name");
    String snapshotName = commandLine.getOptionValue("snapshot-name");

    RegressionIdPreProcessor preProcessor = new RegressionIdPreProcessor(getToolContext().getPositionMaster());
    preProcessor.execute();
    
    GoldenCopyCreator goldenCopySaver = new GoldenCopyCreator(getToolContext());

    GoldenCopy goldenCopy = goldenCopySaver.run(viewName, snapshotName, snapshotName);
    
    new GoldenCopyPersistenceHelper().save(goldenCopy);
    
  }

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createViewOption());
    options.addOption(createSnapshotOption());
    options.addOption(createGoldenCopyNameOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private static Option createViewOption() {
    return OptionBuilder.isRequired(true)
        .hasArg(true)
        .withDescription("The view to create the golden copy for")
        .withLongOpt("view-name")
        .create("v");
  }

  @SuppressWarnings("static-access")
  private static Option createSnapshotOption() {
    return OptionBuilder.isRequired(true)
        .hasArg(true)
        .withDescription("The snapshot to run the view off")
        .withLongOpt("snapshot-name")
        .create("s");
  }

  @SuppressWarnings("static-access")
  private static Option createGoldenCopyNameOption() {
    return OptionBuilder.isRequired(false)
        .hasArg(true)
        .withDescription("The snapshot to run the view off")
        .withLongOpt("golden-copy-name")
        .create("n");
  }

}
