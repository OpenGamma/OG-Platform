/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.rowparser;

import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.fudgemsg.AnnotationReflector;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VarianceSwapLeg;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetPositionWriter;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A generic row parser for Joda beans that automatically identifies fields to be persisted to rows/populated from rows
 */
public class JodaBeanRowParser extends RowParser {

  private static final Logger s_logger = LoggerFactory.getLogger(JodaBeanRowParser.class);

  /**
   * Security properties to ignore when scanning
   */
  private static final String[] IGNORE_METAPROPERTIES = {"securityType", "uniqueid", "objectid", "securitylink", "trades", "gicscode", "parentpositionid", "providerid", "deal", "permissions" };

  /**
   * Column prefixes
   */
  private static final String POSITION_PREFIX = "position";
  private static final String TRADE_PREFIX = "trade";
  private static final String UNDERLYING_PREFIX = "underlying";
  private static final String ATTRIBUTES = "attributes";

  /**
   * Every security class name ends with this
   */
  private static final String CLASS_POSTFIX = "Security";

  /**
   * The security class that this parser is adapted to
   */
  private final Class<? extends Bean> _securityClass;

  /**
   * The underlying security class(es) for the security class above
   */
  private final List<Class<?>> _underlyingSecurityClasses;

  /**
   *  Map from column name to the field's Java type
   */
  private SortedMap<String, Class<?>> _columns = new TreeMap<String, Class<?>>();

  static {
    //Make refections available by calling AnnotationReflector.getDefaultReflector()
    OpenGammaFudgeContext.getInstance();
    // Register the automatic string converters with Joda Beans
    JodaBeanConverters.getInstance();

    // Force registration of various meta beans that might not have been loaded yet
    ManageablePosition.meta();
    ManageableTrade.meta();
    Notional.meta();
    SwapLeg.meta();
    InterestRateLeg.meta();
    FixedInterestRateLeg.meta();
    FloatingInterestRateLeg.meta();
    FloatingGearingIRLeg.meta();
    FloatingSpreadIRLeg.meta();
    VarianceSwapLeg.meta();
    FixedVarianceSwapLeg.meta();
    FloatingVarianceSwapLeg.meta();
    EquitySecurity.meta();
    SwapSecurity.meta();
    InterestRateFutureSecurity.meta();
    MetalFutureSecurity.meta();
    AgricultureFutureSecurity.meta();
    FXFutureSecurity.meta();
    SwaptionSecurity.meta();
    CashFlowSecurity.meta();
    CommodityFutureOptionSecurity.meta();
  }

  protected JodaBeanRowParser(final String securityName) throws OpenGammaRuntimeException {

    ArgumentChecker.notEmpty(securityName, "securityName");

    // Find the corresponding security class
    _securityClass = getClass(securityName + CLASS_POSTFIX);

    // Find the underlying(s)
    _underlyingSecurityClasses = getUnderlyingSecurityClasses(_securityClass);

    // Set column map
    _columns = recursiveGetColumnMap(_securityClass, "");
    for (final Class<?> securityClass : _underlyingSecurityClasses) {
      _columns.putAll(recursiveGetColumnMap(securityClass, UNDERLYING_PREFIX + securityClass.getSimpleName() + ":"));
      _columns.put(UNDERLYING_PREFIX + securityClass.getSimpleName() + ":" + ATTRIBUTES, String.class);
    }
    _columns.putAll(recursiveGetColumnMap(ManageablePosition.class, POSITION_PREFIX + ":"));
    _columns.putAll(recursiveGetColumnMap(ManageableTrade.class, TRADE_PREFIX + ":"));
    _columns.put(POSITION_PREFIX + ":" + ATTRIBUTES, String.class);
    _columns.put(TRADE_PREFIX + ":" + ATTRIBUTES, String.class);
    _columns.put(ATTRIBUTES, String.class);
  }

  private List<Class<?>> getUnderlyingSecurityClasses(final Class<? extends Bean> securityClass) {

    final List<Class<?>> result = new ArrayList<Class<?>>();

    // Futures
    if (EquityFutureSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);

      // Options
    } else if (EquityBarrierOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);
    } else if (EquityOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);
    } else if (IRFutureOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(InterestRateFutureSecurity.class);
    } else if (SwaptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(SwapSecurity.class);
    }

    return result;
  }

  /**
   * Creates a new row parser for the specified security type and tool context
   * @param securityName  the type of the security for which a row parser is to be created
   * @return              the RowParser class for the specified security type, or null if unable to identify a suitable parser
   */
  public static JodaBeanRowParser newJodaBeanRowParser(final String securityName) {
    // Now using the JodaBean parser

    ArgumentChecker.notEmpty(securityName, "securityName");

    try {
      return new JodaBeanRowParser(securityName);
    } catch (final Throwable e) {
      throw new OpenGammaRuntimeException("Could not create a row parser for security type " + securityName, e);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Import routines: construct security(ies), position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public ManageableSecurity[] constructSecurity(final Map<String, String> row) {

    ArgumentChecker.notNull(row, "row");

    final ManageableSecurity security = (ManageableSecurity) recursiveConstructBean(row, _securityClass, "");
    if (security != null) {
      final ArrayList<ManageableSecurity> securities = new ArrayList<ManageableSecurity>();
      securities.add(security);
      for (final Class<?> underlyingClass : _underlyingSecurityClasses) {
        final ManageableSecurity underlying = (ManageableSecurity) recursiveConstructBean(row, underlyingClass, UNDERLYING_PREFIX + underlyingClass.getSimpleName().toLowerCase() + ":");
        if (underlying != null) {
          securities.add(underlying);
        } else {
          s_logger.warn("Could not populate underlying security of type " + underlyingClass);
        }
      }
      return securities.toArray(new ManageableSecurity[securities.size()]);
    } else {
      return null;
    }
  }

  @Override
  public ManageablePosition constructPosition(final Map<String, String> row, final ManageableSecurity security) {

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");

    final ManageablePosition result = (ManageablePosition) recursiveConstructBean(row, ManageablePosition.class, "position:");
    if (result != null) {
      result.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));
    }
    return result;
  }

  @Override
  public ManageableTrade constructTrade(final Map<String, String> row, final ManageableSecurity security, final ManageablePosition position) {

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(position, "position");
    if (functional(row.keySet()).any(new Function1<String, Boolean>() {
      @Override
      public Boolean execute(String columnName) {
        return columnName.startsWith("trade:");
      }
    })) {
      if (!row.containsKey("trade:securitylink")) {
        if (row.containsKey("externalidbundle")) {
          row.put("trade:securitylink", row.get("externalidbundle"));
        }
      }
      final ManageableTrade result = (ManageableTrade) recursiveConstructBean(row, ManageableTrade.class, "trade:");
      if (result != null) {
        result.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));
      } else {
        throw new IllegalStateException("The trade was not constructed despite of trade data present in a row.");
      }
      return result;
    } else {
      return null;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Export routines: construct row from security, position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public Map<String, String> constructRow(final ManageableSecurity[] securities) {
    ArgumentChecker.notNull(securities, "securities");
    final Map<String, String> result = recursiveConstructRow(securities[0], "");

    for (int i = 1; i < securities.length; i++) {
      result.putAll(recursiveConstructRow(securities[i], UNDERLYING_PREFIX + securities[i].getClass().getSimpleName() + ":"));
    }
    return result;
  }

  @Override
  public Map<String, String> constructRow(final ManageablePosition position) {
    ArgumentChecker.notNull(position, "position");
    return recursiveConstructRow(position, "position:");
  }

  @Override
  public Map<String, String> constructRow(final ManageableTrade trade) {
    ArgumentChecker.notNull(trade, "trade");
    return recursiveConstructRow(trade, "trade:");
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Utility routines
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public String[] getColumns() {
    return _columns.keySet().toArray(new String[_columns.size()]);
  }

  @Override
  public int getSecurityHashCode() {
    final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
    for (final Entry<String, Class<?>> entry : _columns.entrySet()) {
      hashCodeBuilder.append(entry.getKey());
      hashCodeBuilder.append(entry.getValue().getCanonicalName());
    }
    return hashCodeBuilder.toHashCode();
  }

  /**
   * Extract a map of column (field) names and types from the properties of the specified direct bean class.
   * Appropriate member classes (such as swap legs) are recursively traversed and their columns also extracted
   * and added to the map.
   * @param clazz   The bean type from which to extract properties
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        A map of the column names and their types
   */
  private SortedMap<String, Class<?>> recursiveGetColumnMap(final Class<?> clazz, final String prefix) {

    // Scan through and capture the list of relevant properties and their types
    final SortedMap<String, Class<?>> columns = new TreeMap<String, Class<?>>();

    for (final MetaProperty<?> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {

      // Skip any undesired properties, process the rest
      if (!ignoreMetaProperty(metaProperty)) {

        // Add a column for the property (used either for the actual value
        // or for the class name in the case of a non-convertible bean
        columns.put(prefix + metaProperty.name(), metaProperty.propertyType());

        // If this is a bean without a converter recursively extract all
        // columns for the metabean and all its subclasses
        if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {

          // This is the bean (might be an abstract class/subclassed)
          final Class<? extends Bean> beanClass = metaProperty.propertyType().asSubclass(Bean.class);

          // Recursively extract this bean's properties
          columns.putAll(recursiveGetColumnMap(beanClass, prefix + metaProperty.name() + ":"));

          // Identify ALL subclasses of this bean and extract all their properties
          for (final Class<?> subClass : getSubClasses(beanClass)) {
            columns.putAll(recursiveGetColumnMap(subClass, prefix + metaProperty.name() + ":"));
          }
        }
      }
    }
    return columns;
  }

  /**
   * Build a bean of the specified type by extracting property values from the supplied map of field names to
   * values, using recursion to construct the member beans in the same manner.
   * @param row     The map from property (or column, or field) names to values
   * @param clazz   The bean type of which to construct an instance
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        The constructed security bean
   */
  private Bean recursiveConstructBean(final Map<String, String> row, final Class<?> clazz, final String prefix) {
    try {
      // Get a reference to the meta-bean
      final MetaBean metaBean = JodaBeanUtils.metaBean(clazz);

      // Get a new builder from the meta-bean
      final BeanBuilder<? extends Bean> builder = metaBean.builder();

      // Populate the bean from the supplied row using the builder
      for (final MetaProperty<?> metaProperty : JodaBeanUtils.metaBean(clazz).metaPropertyIterable()) {

        // Skip any undesired properties, process the rest
        if (!ignoreMetaProperty(metaProperty)) {

          // If this property is itself a bean without a converter, recurse to populate relevant fields
          if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {

            // Get the actual type of this bean from the relevant column
            final String className = row.get((prefix + metaProperty.name()).trim().toLowerCase());
            final Class<? extends Bean> beanClass = getClass(className);

            // Recursively set properties
            builder.set(metaProperty.name(),
                recursiveConstructBean(row, beanClass, prefix + metaProperty.name() + ":"));

            // If not a bean, or it is a bean for which a converter exists, just set value in builder using joda convert
          } else {
            // Convert raw value in row to the target property's type
            final String rawValue = row.get((prefix + metaProperty.name()).trim().toLowerCase());

            if (isConvertible(metaProperty.propertyType())) {
              // Set property value
              if (rawValue != null && !rawValue.equals("")) {
                builder.set(metaProperty.name(),
                    JodaBeanUtils.stringConverter().convertFromString(metaProperty.propertyType(), rawValue));
              } else {
                s_logger.info("Skipping empty or null value for " + prefix + metaProperty.name());
              }
            } else if (List.class.isAssignableFrom(metaProperty.propertyType()) &&
                isConvertible(JodaBeanUtils.collectionType(metaProperty, metaProperty.propertyType()))) {
              builder.set(metaProperty.name(), stringToList(rawValue, JodaBeanUtils.collectionType(metaProperty, metaProperty.propertyType())));
            } else if (Map.class.isAssignableFrom(metaProperty.propertyType()) && metaProperty.name().equalsIgnoreCase("attributes")) {

              builder.set(metaProperty.name(), SingleSheetPositionWriter.attributesToMap(rawValue));
            } else {
              throw new OpenGammaRuntimeException("Property '" + prefix + metaProperty.name() + "' (" + metaProperty.propertyType() + ") cannot be populated from a string");
            }
          }
        }
      }

      // Actually build the bean
      return builder.build();

    } catch (final Throwable ex) {
      s_logger.warn("Not creating a " + clazz.getSimpleName(), ex);
      return null;
    }
  }

  /**
   * Extracts a map of column names to values from a supplied security bean's properties, using recursion to
   * extract properties from any member beans.
   * @param bean    The bean instance from which to extract property values
   * @param prefix  The class membership path traced from the top-level bean class to the current class
   * @return        A map of extracted column names and values
   */
  private Map<String, String> recursiveConstructRow(final Bean bean, final String prefix) {
    final Map<String, String> result = new HashMap<String, String>();

    // Populate the row from the bean's properties
    for (final MetaProperty<?> metaProperty : bean.metaBean().metaPropertyIterable()) {

      // Skip any undesired properties, process the rest
      if (!ignoreMetaProperty(metaProperty)) {
        // If this property is itself a bean without a converter, recurse to populate relevant columns
        if (isBean(metaProperty.propertyType()) && !isConvertible(metaProperty.propertyType())) {

          // Store the class name in a separate column (to help identify the correct subclass during loading)
          result.put(prefix + metaProperty.name(), metaProperty.get(bean).getClass().getSimpleName());

          // Recursively extract bean's columns
          result.putAll(recursiveConstructRow((Bean) metaProperty.get(bean), prefix + metaProperty.name() + ":"));

          // If not a bean, or it is a bean for which a converter exists, just extract its value using joda convert
        } else {
          // Set the column
          if (_columns.containsKey(prefix + metaProperty.name())) {
            // Can convert
            if (isConvertible(metaProperty.propertyType())) {
              result.put(prefix + metaProperty.name(), metaProperty.getString(bean));
              // Is list, needs to be decomposed
            } else if (List.class.isAssignableFrom(metaProperty.propertyType()) &&
                isConvertible(JodaBeanUtils.collectionType(metaProperty, metaProperty.propertyType()))) {
              result.put(prefix + metaProperty.name(), listToString((List<?>) metaProperty.get(bean)));
            } else if (Map.class.isAssignableFrom(metaProperty.propertyType()) && metaProperty.name().equalsIgnoreCase("attributes")) {
              result.put(prefix + metaProperty.name(), SingleSheetPositionWriter.attributesToString((Map<String, String>) metaProperty.get(
                  bean)));
            } else {
              throw new OpenGammaRuntimeException("Property '" + prefix + metaProperty.name() + "' (" + metaProperty.propertyType() + ") cannot be converted to a string");
            }
          } else {
            s_logger.info("No matching column found for property " + prefix + metaProperty.name());
          }
        }
      }
    }
    return result;
  }

  /**
   * Converts a list of objects to a |-separated string of their JodaConverted string representations.
   * 
   * @param list  the list to be converted, not null
   * @return the |-separated string string, not null
   */
  private String listToString(final List<?> list) {
    String result = "";
    for (final Object o : list) {
      if (isConvertible(o.getClass())) {
        result = result + JodaBeanUtils.stringConverter().convertToString(o) + " | ";
      } else {
        throw new OpenGammaRuntimeException("Cannot convert " + o.getClass() + " contained in list");
      }
    }
    return result.substring(0, result.lastIndexOf('|')).trim();
  }

  /**
   * Converts a |-separated string to a list of objects using JodaConvert.
   * 
   * @param rawStr  the string to parse, not null
   * @param cls  the class to convert to, not null
   * @return the list of objects of the specified class, not null
   */
  private List<?> stringToList(final String rawStr, final Class<?> cls) {
    final List<Object> result = new ArrayList<Object>();
    for (final String s : rawStr.split("\\|")) {
      result.add(JodaBeanUtils.stringConverter().convertFromString(cls, s.trim()));
    }
    return result;
  }

  /**
   * Given a class name, look for the class in the list of packages specified by CLASS_PACKAGES and return it
   * or throw exception if not found.
   * 
   * @param className  the class name to seek, not null
   * @return the corresponding class, not null
   */
  private Class<? extends Bean> getClass(final String className) {
    Class<? extends Bean> theClass = null;
    if (className.endsWith(CLASS_POSTFIX)) {
      theClass = getFinancialSecurityClass(className);
    } else {
      theClass = getJodaBeanSubType(className);
    }

    if (theClass == null) {
      throw new OpenGammaRuntimeException("Could not load class " + className);
    }
    return theClass;
  }

  private Class<? extends Bean> getJodaBeanSubType(final String className) {
    final Reflections reflections = AnnotationReflector.getDefaultReflector().getReflector();
    final Set<String> directBeanSubTypes = reflections.getStore().getSubTypesOf(Bean.class.getName());

    Class<? extends Bean> theClass = null;
    for (final String directBeanType : directBeanSubTypes) {
      try {
        if (directBeanType.endsWith("." + className)) {
          theClass = Class.forName(directBeanType).asSubclass(Bean.class);
          break;
        }
      } catch (final Throwable ex) {
      }
    }
    return theClass;
  }

  private Class<? extends Bean> getFinancialSecurityClass(final String className) {
    final Reflections reflections = AnnotationReflector.getDefaultReflector().getReflector();
    final Set<String> financialSecuritySubTypes = reflections.getStore().getSubTypesOf(FinancialSecurity.class.getName());

    Class<? extends Bean> theClass = null;
    for (final String securityType : financialSecuritySubTypes) {
      try {
        if (securityType.endsWith("." + className)) {
          theClass = Class.forName(securityType).asSubclass(Bean.class);
          break;
        }
      } catch (final Throwable ex) {
      }
    }
    return theClass;
  }

  /**
   * Given a bean class, find its subclasses.
   * 
   * @param beanClass  the bean class
   * @return the collection of subclasses
   */
  private Collection<Class<?>> getSubClasses(final Class<? extends Bean> beanClass) {
    final Collection<Class<?>> subClasses = new ArrayList<Class<?>>();

    final Reflections reflections = AnnotationReflector.getDefaultReflector().getReflector();
    final Set<String> subTypes = reflections.getStore().getSubTypesOf(beanClass.getName());
    for (final String subType : subTypes) {
      try {
        subClasses.add(Class.forName(subType));
      } catch (final Throwable ex) {
      }
    }
    return subClasses;
  }

  /**
   * Checks whether the supplied class has a registered Joda string converter
   * @param clazz   the class to check
   * @return        the answer
   */
  private boolean isConvertible(final Class<?> clazz) {
    try {
      JodaBeanUtils.stringConverter().findConverter(clazz);
      return true;
    } catch (final Throwable ex) {
      return false;
    }
  }

  /**
   * Determines whether the supplied class is a direct bean.
   * 
   * @param clazz  the class to check, not null
   * @return true if it is a bean
   */
  private boolean isBean(final Class<?> clazz) {
    return Bean.class.isAssignableFrom(clazz);
  }

  /**
   * Checks whether the specified meta-property is to be ignored when extracting fields.
   * 
   * @param mp  the meta-property to check, not null
   * @return true if it is to be ignored
   */
  private boolean ignoreMetaProperty(final MetaProperty<?> mp) {
    if (mp.style().isSerializable() == false) {
      return true;
    }
    final String s = mp.name().trim().toLowerCase();
    for (final String t : IGNORE_METAPROPERTIES) {
      if (s.equals(t.trim().toLowerCase())) {
        return true;
      }
    }
    return false;
  }

}
