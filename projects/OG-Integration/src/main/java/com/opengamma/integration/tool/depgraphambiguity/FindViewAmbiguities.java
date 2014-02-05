/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.depgraphambiguity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.engine.RemoteEngineContextsComponentFactory;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.FullRequirementResolutionPrinter;
import com.opengamma.engine.depgraph.ambiguity.RequirementResolution;
import com.opengamma.engine.depgraph.ambiguity.ViewDefinitionAmbiguityTest;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.scripts.Scriptable;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ClassUtils;

/**
 * Tool class that compiles a view against a function repository to identify any ambiguities.
 * <p>
 * The configuration is fetched from the server, but the function exclusion groups and priorities must be specified manually as these are not readily available via REST interfaces (at the moment -
 * there is no reason why the data couldn't be available). Any ambiguities are written to a nominated output file.
 * <p>
 * Note that this can be an expensive operation - very expensive if there are multiple deep ambiguities which will cause an incredibly large number of possible terminal output resolutions (based on
 * the cross product of all possible inputs). It is normally only necessary to run on small portfolio samples, for example by setting {@link SimplePortfolioNode#DEBUG_FLAG}.
 */
@Scriptable
public class FindViewAmbiguities extends AbstractTool<ToolContext> {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FindViewAmbiguities.class);

  private static final String VIEW_NAME_OPTION = "v";
  private static final String VIEW_NAME_OPTION_LONG = "view";
  // -xg com.opengamma.web.spring.DemoFunctionExclusionGroupsFactoryBean
  private static final String EXCLUSION_GROUPS_OPTION = "xg";
  private static final String EXCLUSION_GROUPS_OPTION_LONG = "exclusionGroups";
  // -fp com.opengamma.web.spring.DemoFunctionResolverFactoryBean$Priority
  private static final String FUNCTION_PRIORITY_OPTION = "fp";
  private static final String FUNCTION_PRIOTITY_OPTION_LONG = "functionPriority";
  private static final String OUTPUT_OPTION = "o";
  private static final String OUTPUT_OPTION_LONG = "output";
  private static final String VERBOSE_OPTION = "f";
  private static final String VERBOSE_OPTION_LONG = "full";

  private final AtomicInteger _resolutions = new AtomicInteger();
  private final AtomicInteger _ambiguities = new AtomicInteger();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { // CSIGNORE
    new FindViewAmbiguities().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  private final class ViewDefinitionAmbiguityTestImpl extends ViewDefinitionAmbiguityTest {

    private final PrintStream _out;
    private FunctionResolver _functionResolver;

    public ViewDefinitionAmbiguityTestImpl(final PrintStream out) {
      _out = out;
    }

    @Override
    protected FunctionCompilationContext createFunctionCompilationContext() {
      final ComponentRepository repo = (ComponentRepository) getToolContext().getContextManager();
      return repo.getInstance(FunctionCompilationContext.class, "main");
    }

    protected FunctionPriority createFunctionPrioritizer() {
      // TODO: The prioritizer could be exposed over the network (sending the function identifier) and cached
      final String functionPriorities = getCommandLine().getOptionValue(FUNCTION_PRIORITY_OPTION);
      if (functionPriorities != null) {
        try {
          final Class<?> functionPrioritiesClass = ClassUtils.loadClass(functionPriorities);
          Object prioritiesObject = functionPrioritiesClass.newInstance();
          if (prioritiesObject instanceof InitializingBean) {
            ((InitializingBean) prioritiesObject).afterPropertiesSet();
          }
          if (prioritiesObject instanceof FactoryBean) {
            prioritiesObject = ((FactoryBean<?>) prioritiesObject).getObject();
          }
          if (prioritiesObject instanceof FunctionPriority) {
            return (FunctionPriority) prioritiesObject;
          }
        } catch (Exception e) {
          throw new OpenGammaRuntimeException("Error loading function priorities", e);
        }
      }
      return new FunctionPriority() {
        @Override
        public int getPriority(final CompiledFunctionDefinition function) {
          return 0;
        }
      };
    }

    @Override
    protected FunctionResolver createFunctionResolver() {
      if (_functionResolver == null) {
        final FunctionCompilationContext context = createFunctionCompilationContext();
        final FudgeMsg configMsg = RemoteEngineContextsComponentFactory.getConfiguration(context);
        final URI configUri = RemoteEngineContextsComponentFactory.getConfigurationUri(context);
        final URI functionsUri;
        final ExecutorService executor = Executors.newCachedThreadPool();
        try {
          functionsUri = UriEndPointDescriptionProvider.getAccessibleURI(executor, configUri, configMsg.getMessage("functionRepositoryConfiguration"));
        } finally {
          executor.shutdown();
        }
        s_logger.debug("Fetching remote functions from {}", functionsUri);
        final FunctionConfigurationSource functions = new RemoteFunctionConfigurationSource(functionsUri);
        final CompiledFunctionService compiledFunctionService = new CompiledFunctionService(functions, new CachingFunctionRepositoryCompiler(), context);
        compiledFunctionService.initialize();
        _functionResolver = new DefaultFunctionResolver(compiledFunctionService, createFunctionPrioritizer());
      }
      return _functionResolver;
    }

    @Override
    protected FunctionExclusionGroups createFunctionExclusionGroups() {
      // TODO: The exclusion groups could be exposed over the network (sending the function identifier) and cached
      final String exclusionGroups = getCommandLine().getOptionValue(EXCLUSION_GROUPS_OPTION);
      if (exclusionGroups != null) {
        try {
          final Class<?> exclusionGroupsClass = ClassUtils.loadClass(exclusionGroups);
          Object groupsObject = exclusionGroupsClass.newInstance();
          if (groupsObject instanceof InitializingBean) {
            ((InitializingBean) groupsObject).afterPropertiesSet();
          }
          if (groupsObject instanceof FactoryBean) {
            groupsObject = ((FactoryBean<?>) groupsObject).getObject();
          }
          if (groupsObject instanceof FunctionExclusionGroups) {
            return (FunctionExclusionGroups) groupsObject;
          }
          throw new IllegalArgumentException("Couldn't set exclusion groups to " + exclusionGroups + " (got " + groupsObject + ")");
        } catch (Exception e) {
          throw new OpenGammaRuntimeException("Error loading exclusion groups", e);
        }
      }
      return null;
    }

    @Override
    protected void resolved(final FullRequirementResolution resolution) {
      resolvedImpl(resolution);
      final int count = _resolutions.incrementAndGet();
      if ((count % 100) == 0) {
        s_logger.info("Checked {} resolutions", count);
      }
      if (resolution.isDeeplyAmbiguous() && getCommandLine().hasOption(VERBOSE_OPTION)) {
        synchronized (this) {
          (new FullRequirementResolutionPrinter(_out)).print(resolution);
        }
      }
    }

    protected void resolvedImpl(final FullRequirementResolution resolution) {
      super.resolved(resolution);
    }

    @Override
    protected synchronized void directAmbiguity(final FullRequirementResolution resolution) {
      final int count = _ambiguities.incrementAndGet();
      if ((count % 10) == 0) {
        s_logger.info("Found {} ambiguities", count);
      }
      _out.println(resolution.getRequirement());
      for (Collection<RequirementResolution> nestedResolutions : resolution.getResolutions()) {
        final List<String> functions = new ArrayList<String>();
        final List<ValueSpecification> specifications = new ArrayList<ValueSpecification>();
        boolean failure = false;
        for (RequirementResolution nestedResolution : nestedResolutions) {
          if (nestedResolution != null) {
            functions.add(nestedResolution.getFunction().getFunctionId());
            specifications.add(nestedResolution.getSpecification());
          } else {
            failure = true;
          }
        }
        for (String function : functions) {
          _out.println("\t" + function);
        }
        if (failure) {
          _out.println("\t+ failure(s)");
        }
        for (ValueSpecification specification : specifications) {
          _out.println("\t" + specification);
        }
        _out.println();
      }
    }

    @Override
    protected synchronized void deepAmbiguity(final FullRequirementResolution resolution) {
      for (Collection<RequirementResolution> nestedResolutions : resolution.getResolutions()) {
        for (RequirementResolution nestedResolution : nestedResolutions) {
          for (FullRequirementResolution inputResolution : nestedResolution.getInputs()) {
            resolvedImpl(inputResolution);
          }
        }
      }
      _out.println(resolution.getRequirement());
      for (Collection<RequirementResolution> nestedResolutions : resolution.getResolutions()) {
        for (RequirementResolution nestedResolution : nestedResolutions) {
          _out.println("\t" + nestedResolution.getSpecification());
        }
      }
      _out.println();
    }

  }

  private static Option createViewNameOption() {
    final Option option = new Option(VIEW_NAME_OPTION, VIEW_NAME_OPTION_LONG, true, "the view to check, omit to check all");
    option.setArgName("view");
    option.setRequired(false);
    return option;
  }

  private static Option createFunctionExclusionGroupsBeanOption() {
    final Option option = new Option(EXCLUSION_GROUPS_OPTION, EXCLUSION_GROUPS_OPTION_LONG, true, "the exclusion groups to use");
    option.setArgName("class");
    option.setRequired(false);
    return option;
  }

  private static Option createFunctionPrioritiesOption() {
    final Option option = new Option(FUNCTION_PRIORITY_OPTION, FUNCTION_PRIOTITY_OPTION_LONG, true, "the function prioritizer to use");
    option.setArgName("class");
    option.setRequired(false);
    return option;
  }

  private static Option createOutputOption() {
    final Option option = new Option(OUTPUT_OPTION, OUTPUT_OPTION_LONG, true, "the output file to write");
    option.setArgName("filename");
    option.setRequired(false);
    return option;
  }

  private static Option createVerboseOption() {
    final Option option = new Option(VERBOSE_OPTION, VERBOSE_OPTION_LONG, false, "whether to write out ambiguities in full");
    option.setRequired(false);
    return option;
  }

  private static PrintStream openStream(final String filename) throws IOException {
    if ((filename == null) || "stdout".equals(filename)) {
      return System.out;
    } else if ("stderr".equals(filename)) {
      return System.err;
    } else {
      return new PrintStream(new FileOutputStream(filename));
    }
  }

  // AbstractTool

  @Override
  protected void doRun() throws Exception {
    final PrintStream out = openStream(getCommandLine().getOptionValue(OUTPUT_OPTION));
    final ViewDefinitionAmbiguityTest test = new ViewDefinitionAmbiguityTestImpl(out);
    final String viewName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);
    int count = 0;
    if (viewName != null) {
      s_logger.info("Testing {}", viewName);
      final ViewDefinition viewDefinition = getToolContext().getConfigSource().getLatestByName(ViewDefinition.class, viewName);
      if (viewDefinition == null) {
        throw new IllegalArgumentException("View definition " + viewName + " not found");
      }
      out.println("View = " + viewName);
      test.runAmbiguityTest(viewDefinition);
      count++;
    } else {
      final Collection<ConfigItem<ViewDefinition>> viewDefinitions = getToolContext().getConfigSource().getAll(ViewDefinition.class, VersionCorrection.LATEST);
      s_logger.info("Testing {} view definition(s)", viewDefinitions.size());
      for (ConfigItem<ViewDefinition> viewDefinitionConfig : viewDefinitions) {
        final ViewDefinition viewDefinition = viewDefinitionConfig.getValue();
        s_logger.info("Testing {}", viewDefinition.getName());
        out.println("View = " + viewDefinition.getName());
        final int resolutions = _resolutions.get();
        final int ambiguities = _ambiguities.get();
        test.runAmbiguityTest(viewDefinition);
        count++;
        out.println("Resolutions = " + (_resolutions.get() - resolutions));
        out.println("Ambiguities = " + (_ambiguities.get() - ambiguities));
      }
    }
    s_logger.info("{} view(s) tested", count);
    out.println("Total resolutions = " + _resolutions.get());
    out.println("Total ambiguities = " + _ambiguities.get());
    out.close();
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfigResource) {
    final Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createViewNameOption());
    options.addOption(createFunctionExclusionGroupsBeanOption());
    options.addOption(createFunctionPrioritiesOption());
    options.addOption(createOutputOption());
    options.addOption(createVerboseOption());
    return options;
  }

}
