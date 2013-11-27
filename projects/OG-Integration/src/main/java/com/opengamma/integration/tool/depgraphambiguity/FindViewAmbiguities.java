/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.depgraphambiguity;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.opengamma.engine.depgraph.ambiguity.ViewDefinitionAmbiguityTest;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.config.FunctionConfigurationBundle;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.scripts.Scriptable;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ClassUtils;

/**
 * Tool class that compiles a view against a function repository to identify any ambiguities.
 */
@Scriptable
public class FindViewAmbiguities extends AbstractTool<ToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(FindViewAmbiguities.class);

  private static final String VIEW_NAME_OPTION = "v";
  private static final String VIEW_NAME_OPTION_LONG = "view";
  // -xg com.opengamma.web.spring.DemoFunctionExclusionGroupsFactoryBean
  private static final String EXCLUSION_GROUPS_OPTION = "xg";
  private static final String EXCLUSION_GROUPS_OPTION_LONG = "exclusionGroups";
  // -fp com.opengamma.web.spring.DemoFunctionResolverFactoryBean$Priority
  private static final String FUNCTION_PRIORITY_OPTION = "fp";
  private static final String FUNCTION_PRIOTITY_OPTION_LONG = "functionPriority";

  public static void main(String[] args) { // CSIGNORE
    new FindViewAmbiguities().initAndRun(args, ToolContext.class);
    Runtime.getRuntime().halt(1);
  }

  private final class ViewDefinitionAmbiguityTestImpl extends ViewDefinitionAmbiguityTest {

    private FunctionResolver _functionResolver;

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
        final FunctionConfigurationBundle functionConfiguration = functions.getFunctionConfiguration();
        final FunctionRepository functionRepository = FunctionRepositoryFactory.constructRepository(functionConfiguration);
        final CompiledFunctionService compiledFunctionService = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), context);
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

  // AbstractTool

  @Override
  protected void doRun() throws Exception {
    final ViewDefinitionAmbiguityTest test = new ViewDefinitionAmbiguityTestImpl();
    final String viewName = getCommandLine().getOptionValue(VIEW_NAME_OPTION);
    int count = 0;
    if (viewName != null) {
      s_logger.info("Testing {}", viewName);
      final ViewDefinition viewDefinition = getToolContext().getConfigSource().getLatestByName(ViewDefinition.class, viewName);
      if (viewDefinition == null) {
        throw new IllegalArgumentException("View definition " + viewName + " not found");
      }
      test.runAmbiguityTest(viewDefinition);
      count++;
    } else {
      final Collection<ConfigItem<ViewDefinition>> viewDefinitions = getToolContext().getConfigSource().getAll(ViewDefinition.class, VersionCorrection.LATEST);
      s_logger.info("Testing {} view definition(s)", viewDefinitions.size());
      for (ConfigItem<ViewDefinition> viewDefinitionConfig : viewDefinitions) {
        final ViewDefinition viewDefinition = viewDefinitionConfig.getValue();
        s_logger.info("Testing {}", viewDefinition.getName());
        test.runAmbiguityTest(viewDefinition);
        count++;
      }
    }
    s_logger.info("{} view(s) tested", count);
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfigResource) {
    final Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createViewNameOption());
    options.addOption(createFunctionExclusionGroupsBeanOption());
    options.addOption(createFunctionPrioritiesOption());
    return options;
  }

}
