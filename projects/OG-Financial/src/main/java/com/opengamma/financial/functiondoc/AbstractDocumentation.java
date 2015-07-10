/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.functiondoc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailablePortfolioOutputs;
import com.opengamma.financial.filtering.AbstractFilteringFunction;
import com.opengamma.financial.filtering.PortfolioFilter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utility template for generating documentation from a function repository.
 */
public abstract class AbstractDocumentation implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDocumentation.class);
  private static final int MAX_SECURITIES_PER_TYPE = 20;
  private static final int MAX_PROPERTY_EXAMPLES = 5;

  /**
   * Page hook, inverting the dependency on the Wiki API.
   */
  public interface WikiPageHook {

    void emitPage(String title, String content);

  }

  private static <K> int incrementAndGet(final K key, final Map<K, AtomicInteger> store) {
    final AtomicInteger c = store.get(key);
    if (c == null) {
      store.put(key, new AtomicInteger(1));
      return 1;
    } else {
      return c.incrementAndGet();
    }
  }

  private static class SecurityTypePortfolioFilter extends AbstractFilteringFunction {

    private final Map<Pair<String, Class<?>>, AtomicInteger> _visited = new HashMap<Pair<String, Class<?>>, AtomicInteger>();

    public SecurityTypePortfolioFilter() {
      super("UniqueSecurityType");
    }

    @Override
    public boolean acceptPosition(final Position position) {
      final Security security = position.getSecurity();
      return incrementAndGet(Pairs.<String, Class<?>>of(security.getSecurityType(), security.getClass()), _visited) < MAX_SECURITIES_PER_TYPE;
    }

  }

  private static class ValueRequirementInfo {

    private final String _symbol;
    private final String _name;
    private final String _javadoc;

    public ValueRequirementInfo(final String symbol, final String name, final String javadoc) {
      _symbol = symbol;
      _name = name;
      _javadoc = javadoc;
    }

    public String getSymbol() {
      return _symbol;
    }

    public String getName() {
      return _name;
    }

    public String getJavadoc() {
      return _javadoc;
    }

  }

  private final CompiledFunctionRepository _functionRepository;
  private final FunctionExclusionGroups _functionExclusionGroups;
  private final MarketDataAvailabilityFilter _availabilityFilter = new OptimisticMarketDataAvailabilityFilter();
  private final SecurityTypePortfolioFilter _securityTypePortfolioFilter = new SecurityTypePortfolioFilter();
  private final Map<String, Set<AvailableOutput>> _availableOutputsBySecurityType = new HashMap<String, Set<AvailableOutput>>();
  private final Map<String, Set<AvailableOutput>> _availableOutputsByName = new HashMap<String, Set<AvailableOutput>>();
  private final Map<String, ValueRequirementInfo> _valueRequirementBySymbol = new HashMap<String, ValueRequirementInfo>();
  private final Map<String, ValueRequirementInfo> _valueRequirementByName = new HashMap<String, ValueRequirementInfo>();
  private final Map<String, List<ValueRequirementInfo>> _valueRequirementByCategory = new HashMap<String, List<ValueRequirementInfo>>();
  private final List<Pair<Pattern, String>> _valuePropertyDescription = new ArrayList<Pair<Pattern, String>>();
  private int[] _valuePropertyDescriptionUsed;
  private final Map<String, AtomicInteger> _undocumentedProperties = new HashMap<String, AtomicInteger>();

  private WikiPageHook _pageHook = getDefaultWikiPageHook();

  protected AbstractDocumentation(final CompiledFunctionRepository functionRepository, final FunctionExclusionGroups functionExclusionGroups) {
    ArgumentChecker.notNull(functionRepository, "functionRepository");
    _functionRepository = functionRepository;
    _functionExclusionGroups = functionExclusionGroups;
  }

  protected CompiledFunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  protected FunctionExclusionGroups getFunctionExclusionGroups() {
    return _functionExclusionGroups;
  }

  protected MarketDataAvailabilityFilter getMarketDataAvailability() {
    return _availabilityFilter;
  }

  public void setWikiPageHook(final WikiPageHook pageHook) {
    ArgumentChecker.notNull(pageHook, "pageHook");
    _pageHook = pageHook;
  }

  public WikiPageHook getWikiPageHook() {
    return _pageHook;
  }

  public WikiPageHook getDefaultWikiPageHook() {
    return new WikiPageHook() {
      @Override
      public void emitPage(final String title, final String content) {
        System.out.println("## " + title + " ##");
        System.out.println(content);
      }
    };
  }

  private <V> void storeMapSet(final String key, final V value, final Map<String, Set<V>> store) {
    Set<V> values = store.get(key);
    if (values == null) {
      values = new HashSet<V>();
      store.put(key, values);
    }
    values.add(value);
  }

  private <V> void storeMapList(final String key, final V value, final Map<String, List<V>> store) {
    List<V> values = store.get(key);
    if (values == null) {
      values = new LinkedList<V>();
      store.put(key, values);
    }
    values.add(value);
  }

  /**
   * Processes the portfolio against the function repository to determine typical properties and applicability of value requirement names to each asset class discovered. The portfolio is filtered so
   * that only a small sample of each unique asset class is considered - this is to save time when there are many portfolios to consider.
   * 
   * @param portfolio a portfolio containing a sample of asset class instances
   */
  public void processAvailablePortfolioOutputs(final Portfolio portfolio) {
    final Portfolio filtered;
    synchronized (_securityTypePortfolioFilter) {
      filtered = new PortfolioFilter(_securityTypePortfolioFilter).filter(portfolio);
    }
    if (filtered.getRootNode().getChildNodes().isEmpty() && filtered.getRootNode().getPositions().isEmpty()) {
      s_logger.debug("Ignoring {} ({})", portfolio.getName(), portfolio.getUniqueId());
    } else {
      s_logger.info("Calculating available outputs from {} ({})", portfolio.getName(), portfolio.getUniqueId());
      final AvailableOutputs outputs = new AvailablePortfolioOutputs(filtered, getFunctionRepository(), getFunctionExclusionGroups(), getMarketDataAvailability(), null);
      synchronized (_availableOutputsBySecurityType) {
        for (final AvailableOutput output : outputs.getOutputs()) {
          for (final String securityType : output.getSecurityTypes()) {
            storeMapSet(securityType, output, _availableOutputsBySecurityType);
          }
          if (output.isAvailableOnPortfolioNode()) {
            storeMapSet("", output, _availableOutputsBySecurityType);
          }
        }
      }
      synchronized (_availableOutputsByName) {
        for (final AvailableOutput output : outputs.getOutputs()) {
          storeMapSet(output.getValueName(), output, _availableOutputsByName);
        }
      }
    }
  }

  /**
   * Parse a source file that describes value requirement names complete with Javadoc. Note the parse is crude at best, so may not be able to handle arbitrary source inputs despite them being legal
   * Java and Javadoc.
   * 
   * @param sourceCodePath path to the Java source (e.g. ValueRequirementNames.java)
   */
  public void processValueRequirementNames(final String sourceCodePath) {
    try {
      final BufferedReader reader = new BufferedReader(new Reader() {

        private final Reader _underlying = new BufferedReader(new FileReader(sourceCodePath));
        private char[] _overrun;
        private int _state;
        private int _run;

        @Override
        public int read(final char[] cbuf, final int off, final int len) throws IOException {
          if (_overrun != null) {
            final int avail = _overrun.length - _run;
            if (avail > len) {
              System.arraycopy(_overrun, _run, cbuf, off, len);
              _run += len;
              return len;
            } else {
              System.arraycopy(_overrun, _run, cbuf, off, avail);
              _overrun = null;
              return avail;
            }
          }
          StringBuilder sb;
          do {
            final int chars = _underlying.read(cbuf, off, len);
            if (chars <= 0) {
              return chars;
            }
            sb = new StringBuilder(chars);
            for (int i = 0; i < chars; i++) {
              final char c = cbuf[i];
              switch (_state) {
                case 0:
                  if (c == '/') {
                    _state = 1;
                    _run = 1;
                  } else {
                    sb.append(c);
                  }
                  break;
                case 1: // /
                  switch (c) {
                    case '*':
                      _state = 2;
                      break;
                    case '/':
                      _state = 1;
                      _run++;
                      break;
                    default:
                      _state = 0;
                      for (int j = 0; j < _run; j++) {
                        sb.append('/');
                      }
                      sb.append(c);
                      break;
                  }
                  break;
                case 2: // /*
                  switch (c) {
                    case '*':
                      _state = 3;
                      break;
                    default:
                      _state = 4;
                      break;
                  }
                  break;
                case 3: // /**
                  switch (c) {
                    case '/':
                      _state = 0;
                      sb.append(' ');
                      break;
                    case '*':
                      _state = 5;
                      break;
                    default:
                      _state = 6;
                      sb.append("/**\n").append(c);
                      break;
                  }
                  break;
                case 4: // /* .*
                  if (c == '*') {
                    _state = 5;
                  }
                  break;
                case 5: // /* .* *
                  switch (c) {
                    case '*':
                      _state = 5;
                      break;
                    case '/':
                      _state = 0;
                      sb.append(' ');
                      break;
                    default:
                      _state = 4;
                      break;
                  }
                  break;
                case 6: // /** .*
                  if (c == '*') {
                    _state = 7;
                  } else {
                    sb.append(c);
                  }
                  break;
                case 7: // /** .* *
                  switch (c) {
                    case '*':
                      _state = 7;
                      break;
                    case '/':
                      _state = 0;
                      sb.append("\n*/\n");
                      break;
                    default:
                      _state = 6;
                      sb.append('*').append(c);
                      break;
                  }
                  break;
              }
            }
          } while (sb.length() == 0);
          if (sb.length() > len) {
            sb.getChars(0, len, cbuf, off);
            _overrun = new char[sb.length() - len];
            sb.getChars(len, sb.length(), _overrun, 0);
            _run = 0;
            return len;
          } else {
            sb.getChars(0, sb.length(), cbuf, off);
            return sb.length();
          }
        }

        @Override
        public void close() throws IOException {
          _underlying.close();
        }

      });
      String s;
      String section = "UNCLASSIFIED";
      StringBuilder sbJavadoc = null;
      boolean javadoc = false;
      final Pattern p = Pattern.compile("public\\s+static\\s+final\\s+String\\s+([A-Z0-9_]+)\\s*=\\s*\"(.*?)\"\\s*;\\s*$");
      while ((s = reader.readLine()) != null) {
        s = s.trim();
        if (s.startsWith("/////")) {
          section = s.substring(5).trim();
          continue;
        }
        if (s.startsWith("//")) {
          continue;
        }
        if (s.startsWith("/**")) {
          sbJavadoc = new StringBuilder();
          javadoc = true;
          continue;
        }
        if (s.startsWith("*/")) {
          javadoc = false;
          continue;
        }
        if (javadoc) {
          if (s.startsWith("* ")) {
            sbJavadoc.append(s.substring(2).trim());
          } else {
            sbJavadoc.append(s);
          }
          continue;
        }
        if (s.length() == 0) {
          continue;
        }
        if (sbJavadoc != null) {
          final Matcher m = p.matcher(s);
          if (m.matches()) {
            final ValueRequirementInfo v = new ValueRequirementInfo(m.group(1), m.group(2), sbJavadoc.toString());
            storeMapList(section, v, _valueRequirementByCategory);
            _valueRequirementByName.put(v.getName(), v);
            _valueRequirementBySymbol.put(v.getSymbol(), v);
          }
          sbJavadoc = null;
        }
      }
      reader.close();
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("I/O exception", e);
    }
  }

  public void processPropertiesFile(final String path) {
    try {
      final BufferedReader reader = new BufferedReader(new FileReader(path));
      String s;
      int line = 0;
      while ((s = reader.readLine()) != null) {
        line++;
        s = s.trim();
        if (s.startsWith("#")) {
          continue;
        }
        if (s.length() == 0) {
          continue;
        }
        final int eq = s.indexOf('=');
        if (eq < 0) {
          s_logger.error("Invalid line {} in {}", line, path);
        } else {
          final String k = s.substring(0, eq).trim();
          final String v = s.substring(eq + 1).trim();
          _valuePropertyDescription.add(Pairs.of(Pattern.compile("^" + k.replace("\\", "\\\\").replace(".", "\\.").replace("*", ".*?") + "$"), v));
        }
      }
      reader.close();
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("I/O exception", e);
    }
  }

  protected String prettySecurityType(final String type) {
    return PrettyPrintSecurityType.getTypeString(type);
  }

  /**
   * Emits a page containing all of the defined value requirement names.
   */
  protected void emitAllValueRequirementNames() {
    final StringBuilder sb = new StringBuilder();
    sb.append("This page documents the full list of Value Requirements supported end-to-end by this version of "
        + "the OpenGamma Platform, backed by the OpenGamma Analytics library. It provides the requirement name "
        + "to be used and a brief description of the value. Note the richness of output available. Values are "
        + "not constrained to scalar types. Arrays, Curves, Surfaces, Jacobians and Time Series are a few useful "
        + "examples.\n\nFor details of targets these values can be computed for and parameters/properties to "
        + "control their behaviour refer to [Top Level Value Requirements] or the pages summarising the list for "
        + "each asset class.\n");
    final List<String> categories = new ArrayList<String>(_valueRequirementByCategory.keySet());
    Collections.sort(categories);
    for (final String category : categories) {
      sb.append("\nh2. ").append(category).append("\n\n");
      sb.append("|| Value Requirement Name || Java Constant || Description ||\n");
      for (final ValueRequirementInfo info : _valueRequirementByCategory.get(category)) {
        sb.append("| ").append(info.getName()).append(" | ").append(info.getSymbol()).append(" | ");
        if (info.getJavadoc().startsWith("TODO")) {
          s_logger.error("Missing javadoc for " + info.getSymbol());
          sb.append("|\n");
        } else {
          final int dot = info.getJavadoc().indexOf('.');
          if (dot < 0) {
            sb.append(info.getJavadoc());
          } else {
            sb.append(info.getJavadoc().substring(0, dot));
          }
          sb.append(" |\n");
        }
      }
    }
    getWikiPageHook().emitPage("OpenGamma Analytics Value Requirements", sb.toString());
  }

  /**
   * Emit a grid of all the top-level value requirement names and which security types they apply to.
   */
  protected void emitTopLevelRequirementsPage() {
    final StringBuilder sb = new StringBuilder();
    sb.append("This page lists the top-level value requirements that can be requested for a portfolio as part of "
        + "a view definition. The grid indicates which requirements are applicable to each asset class. Refer to "
        + "the asset class specific pages for further details on additional constraints that can be set when "
        + "constructing views to control the behaviour of the underlying analytics library.\n\n");
    final List<String> securityTypes = new ArrayList<String>(_availableOutputsBySecurityType.keySet());
    Collections.sort(securityTypes, String.CASE_INSENSITIVE_ORDER);
    final List<String> valueRequirementNames = new ArrayList<String>(_availableOutputsByName.keySet());
    Collections.sort(valueRequirementNames, String.CASE_INSENSITIVE_ORDER);
    sb.append("|| Value Requirement Name");
    for (final String securityType : securityTypes) {
      sb.append(" || ");
      if (securityType.length() == 0) {
        sb.append("[Aggregate|Aggregate Value Requirements]");
      } else {
        sb.append('[').append(prettySecurityType(securityType)).append('|').append(prettySecurityType(securityType).replace("/", "-")).append(" Value Requirements]");
      }
    }
    sb.append(" ||\n");
    for (final String valueRequirementName : valueRequirementNames) {
      sb.append("| ").append(valueRequirementName).append(" |");
      for (final AvailableOutput output : _availableOutputsByName.get(valueRequirementName)) {
        if (output.isAvailableOnPortfolioNode()) {
          sb.append(" Yes");
          break;
        }
      }
      for (final String securityType : securityTypes) {
        if (securityType.length() == 0) {
          continue;
        }
        sb.append(" |");
        for (final AvailableOutput output : _availableOutputsByName.get(valueRequirementName)) {
          if (output.isAvailableOn(securityType)) {
            sb.append(" Yes");
            break;
          }
        }
      }
      sb.append(" |\n");
    }
    getWikiPageHook().emitPage("Top Level Value Requirements", sb.toString());
  }

  protected String javaDocToWiki(final String javadoc) {
    final String s = javadoc.replace("\n", " ").replaceAll("\\s\\s+", " ").replaceAll("\\{@link #([^\\}]+)\\}", "\\{\\{$1\\}\\}");
    // TODO: handle other javadoc entities and html markup that might be present
    return s;
  }

  protected void emitValueRequirements(final Map<String, Set<ValueProperties>> valueRequirements, final StringBuilder sb) {
    final StringBuilder sbTable = new StringBuilder();
    final StringBuilder sbDetail = new StringBuilder();
    final List<String> valueNames = new ArrayList<String>(valueRequirements.keySet());
    Collections.sort(valueNames);
    sbTable.append("|| Value Requirement Name || Properties ||\n");
    for (final String valueName : valueNames) {
      sbDetail.append("\n{anchor:").append(valueName.replace(" ", "")).append("}\nh2. ").append(valueName).append("\n\n");
      final ValueRequirementInfo valueInfo = _valueRequirementByName.get(valueName);
      if (valueInfo != null) {
        if (!valueInfo.getJavadoc().startsWith("TODO")) {
          sbDetail.append(javaDocToWiki(valueInfo.getJavadoc())).append("\n\n");
        }
        // TODO: construct links to any additional data that is specific to this asset class as the javadoc is the generic
        // blob from ValueRequirementNames
      } else {
        s_logger.error("No value requirement info for {}", valueName);
      }
      sbTable.append("| [").append(valueName).append("|#").append(valueName.replace(" ", "")).append("] | ");
      final Map<String, Set<String>> propertyValues = new HashMap<String, Set<String>>();
      for (final ValueProperties properties : valueRequirements.get(valueName)) {
        for (final String propertyName : properties.getProperties()) {
          Set<String> values = propertyValues.get(propertyName);
          if (values == null) {
            values = new HashSet<String>();
            propertyValues.put(propertyName, values);
          }
          values.addAll(properties.getValues(propertyName));
        }
      }
      final List<String> propertyNames = new ArrayList<String>(propertyValues.keySet());
      Collections.sort(propertyNames);
      int count = 0;
      boolean comma = false;
      for (final String propertyName : propertyNames) {
        if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
          continue;
        }
        if (comma) {
          sbTable.append(", ");
        } else {
          comma = true;
        }
        sbTable.append("[").append(propertyName).append("|#").append(valueName.replace(" ", "")).append('.').append(propertyName.replace(" ", "")).append(']');
        if (count == 0) {
          sbDetail.append("|| Property || Description ||\n");
        }
        sbDetail.append("| {anchor:").append(valueName.replace(" ", "")).append('.').append(propertyName.replace(" ", "")).append("} ").append(propertyName).append(" | ");
        // TODO: fetch the description of the property name in the context of this value requirement name & construct any links etc ...
        String s = valueName + "." + propertyName;
        int i = -1;
        for (final Pair<Pattern, String> description : _valuePropertyDescription) {
          i++;
          final Matcher m = description.getFirst().matcher(s);
          if (m.matches()) {
            sbDetail.append(description.getSecond());
            s = null;
            _valuePropertyDescriptionUsed[i]++;
            break;
          }
        }
        if (s != null) {
          s_logger.warn("No property description for {}", s);
          incrementAndGet(propertyName, _undocumentedProperties);
        }
        final Set<String> exampleValues = propertyValues.get(propertyName);
        if (!exampleValues.isEmpty()) {
          if (exampleValues.size() == 1) {
            sbDetail.append(" Example value: ");
          } else {
            sbDetail.append(" Example values: ");
          }
          int count2 = 0;
          boolean comma2 = false;
          for (final String exampleValue : exampleValues) {
            if (comma2) {
              sbDetail.append(", ");
            } else {
              comma2 = true;
            }
            sbDetail.append('_').append(exampleValue).append('_');
            if (++count2 >= MAX_PROPERTY_EXAMPLES) {
              break;
            }
          }
          sbDetail.append(".");
        }
        sbDetail.append(" |\n");
        count++;
      }
      sbTable.append(" |\n");
      if (count == 0) {
        sbDetail.append("This value requirement has no additional properties.\n");
      }
    }
    sb.append(sbTable.toString());
    sb.append(sbDetail.toString());
  }

  /**
   * Creates an asset class page detailing the value requirements available and properties.
   * 
   * @param securityType the security type
   */
  protected void emitSecurityTypePage(final String securityType) {
    final StringBuilder sb = new StringBuilder();
    sb.append("This page lists the value requirements that can be requested at the position level for this asset class. The properties listed may not " +
        "be produced by all functions. Where multiple functions are available for a given value requirement (for example the alternative calculation " +
        "methods available in the analytics library) each might only produce a subset of the properties given here.\n\n");
    final Map<String, Set<ValueProperties>> valueRequirements = new HashMap<String, Set<ValueProperties>>();
    for (final AvailableOutput output : _availableOutputsBySecurityType.get(securityType)) {
      storeMapSet(output.getValueName(), output.getPositionProperties(securityType), valueRequirements);
    }
    emitValueRequirements(valueRequirements, sb);
    getWikiPageHook().emitPage(prettySecurityType(securityType) + " Value Requirements", sb.toString());
  }

  /**
   * Creates a page for each asset class.
   */
  protected void emitSecurityTypePages() {
    for (final String securityType : _availableOutputsBySecurityType.keySet()) {
      if (securityType.length() == 0) {
        continue;
      }
      emitSecurityTypePage(securityType);
    }
  }

  /**
   * Creates a page for the aggregation properties.
   */
  protected void emitAggregationPage() {
    final StringBuilder sb = new StringBuilder();
    sb.append("This page lists the value requirements that can be requested at the aggregate level, if the component positions support them. " +
        "The properties listed may not be produced by all functions. Where multiple functions are available for a given value requirement, for " +
        "example the alternative calculation methods available in the analytics library, or a node contains positions in a range of asset classes, " +
        "each might only produce a subset of the properties given here.\n\n");
    final Map<String, Set<ValueProperties>> valueRequirements = new HashMap<String, Set<ValueProperties>>();
    for (final AvailableOutput output : _availableOutputsBySecurityType.get("")) {
      storeMapSet(output.getValueName(), output.getPortfolioNodeProperties(), valueRequirements);
    }
    emitValueRequirements(valueRequirements, sb);
    getWikiPageHook().emitPage("Aggregate Value Requirements", sb.toString());
  }

  protected void reportUndocumentedProperties() {
    final List<Map.Entry<String, AtomicInteger>> properties = new ArrayList<Map.Entry<String, AtomicInteger>>(_undocumentedProperties.entrySet());
    Collections.sort(properties, new Comparator<Map.Entry<String, AtomicInteger>>() {
      @Override
      public int compare(final Entry<String, AtomicInteger> o1, final Entry<String, AtomicInteger> o2) {
        return o1.getValue().get() - o2.getValue().get();
      }
    });
    for (final Map.Entry<String, AtomicInteger> property : properties) {
      s_logger.error("No property description for {} ({} times)", property.getKey(), property.getValue());
    }
  }

  protected void reportOverdocumentedProperties() {
    int i = 0;
    for (final Pair<Pattern, String> entry : _valuePropertyDescription) {
      if (_valuePropertyDescriptionUsed[i++] == 0) {
        s_logger.warn("Entry {} never used ({})", entry.getFirst().pattern(), entry.getSecond());
      }
    }
  }

  @Override
  public void run() {
    s_logger.info("Publishing stored documentation state");
    _valuePropertyDescriptionUsed = new int[_valuePropertyDescription.size()];
    emitAllValueRequirementNames();
    emitTopLevelRequirementsPage();
    emitSecurityTypePages();
    emitAggregationPage();
    reportUndocumentedProperties();
    reportOverdocumentedProperties();
  }

}
