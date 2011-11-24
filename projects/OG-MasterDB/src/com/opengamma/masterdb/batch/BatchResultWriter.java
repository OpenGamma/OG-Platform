/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ResultWriter;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.functional.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.util.functional.Functional.any;
import static com.opengamma.util.functional.Functional.newArray;


public class BatchResultWriter extends AbstractBatchResultWriter implements ResultWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(BatchResultWriter.class);

  /**
   * It is possible to disable writing errors into
   *
   * rsk_compute_failure
   * rsk_failure
   * rsk_failure_reason
   *
   * by setting this to false.
   *
   */
  private final boolean _writeErrors = true;

  Map<ValueSpecification, BatchResultWriterFailure> _errorCache = Maps.newHashMap();


  public BatchResultWriter(DbConnector dbConnector,
                           Set<ComputationTarget> computationTargets,
                           RiskRun riskRun,
                           Collection<RiskValueName> valueNames,
                           Collection<RiskValueRequirement> valueRequirements,
                           Collection<RiskValueSpecification> valueSpecifications) {
    this(dbConnector,
        computationTargets,
        riskRun,
        valueNames,
        valueRequirements,
        valueSpecifications,
        new ResultConverterCache());
  }

  public BatchResultWriter(
      DbConnector dbConnector,
      Set<ComputationTarget> computationTargets,
      RiskRun riskRun,
      Collection<RiskValueName> valueNames,
      Collection<RiskValueRequirement> valueRequirements,
      Collection<RiskValueSpecification> valueSpecifications,
      ResultConverterCache resultConverterCache) {

    super(dbConnector, riskRun, resultConverterCache, computationTargets, valueNames, valueRequirements, valueSpecifications);
  }

  public boolean isWriteErrors() {
    return _writeErrors;
  }


  // --------------------------------------------------------------------------

  @Override
  public synchronized void write(ViewResultModel resultModel) {
    if (resultModel.getAllResults().isEmpty()) {
      s_logger.info("{}: Nothing to insert into DB", resultModel);
      return;
    }

    if (!isInitialized()) {
      initialize();
    }

    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      joinSession();

      writeImpl(resultModel);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void writeImpl(ViewResultModel resultModel) {

    // STAGE 1. Populate error information in the cache.
    // This is done for all items and will populate table rsk_compute_failure. 
    for (ViewResultEntry result : resultModel.getAllResults()) {
      populateErrorCache(result);
    }


    for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {


      // STAGE 2. Work out which targets:
      // 1) succeeded and should be written into rsk_value (because ALL items for that target succeeded)
      // 2) failed and should be written into rsk_failure (because AT LEAST ONE item for that target failed)


      ViewCalculationResultModel viewCalculationResultModel = resultModel.getCalculationResult(calcConfigName);

      Set<ComputationTargetSpecification> successfulTargets = newHashSet();
      Set<ComputationTargetSpecification> failedTargets = newHashSet();

      viewCalculationResultModel.getAllTargets();

      for (ComputationTargetSpecification targetSpecification : viewCalculationResultModel.getAllTargets()) {

        Collection<ComputedValue> values = viewCalculationResultModel.getAllValues(targetSpecification);

        if (failedTargets.contains(targetSpecification) || any(values, new Function1<ComputedValue, Boolean>() {
          @Override
          /**
           * Predcate checking for failed computation values or values for which there is no converter
           */
          public Boolean execute(ComputedValue cv) {
            if (cv.getInvocationResult() != InvocationResult.SUCCESS) {
              s_logger.error("The calculation of {} has failed, {}:{} ", newArray(cv.getSpecification(), cv.getInvocationResult(), cv.getExceptionMsg()));
              return true;
            } else {
              Object value = cv.getValue();
              try {
                getResultConverterCache().getConverter(value);
              } catch (IllegalArgumentException e) {
                s_logger.error("Cannot insert value of type " + value.getClass() + " for " + cv.getSpecification(), e);
                return true;
              }
              return false;
            }
          }
        })) {
          successfulTargets.add(targetSpecification);
        } else {
          successfulTargets.remove(targetSpecification);
          failedTargets.add(targetSpecification);
        }
      }

      // STAGE 3. Based on the results of stage 2, work out
      // SQL statements to write risk into rsk_value and rsk_failure (& rsk_failure_reason)

      List<SqlParameterSource> successes = newArrayList();
      List<SqlParameterSource> failures = newArrayList();
      List<SqlParameterSource> failureReasons = newArrayList();

      int riskRunId = getRiskRunId();

      Date evalInstant = new Date();

      int calcConfId = getCalculationConfigurationId(calcConfigName);

      for (ComputationTargetSpecification compTargetSpec : viewCalculationResultModel.getAllTargets()) {

        if (successfulTargets.contains(compTargetSpec)) {

          // make sure the values are not already in db, don't want to insert twice
          StatusEntry.Status status = getStatus(calcConfigName, compTargetSpec);
          if (status == StatusEntry.Status.SUCCESS) {
            continue;
          }

          for (ComputedValue computedValue : viewCalculationResultModel.getAllValues(compTargetSpec)) {

            @SuppressWarnings("unchecked")
            ResultConverter<Object> resultConverter = (ResultConverter<Object>) getResultConverterCache().getConverter(computedValue.getValue());
            Map<String, Double> valuesAsDoubles = resultConverter.convert(computedValue.getSpecification().getValueName(), computedValue.getValue());

            int computationTargetId = getComputationTargetId(compTargetSpec);

            for (Map.Entry<String, Double> riskValueEntry : valuesAsDoubles.entrySet()) {
              ValueSpecification specification = computedValue.getSpecification();

              ValueRequirement requirement = computedValue.getRequirement();

              int valueRequirementId = getValueRequirementId(requirement.getConstraints());
              int valueSpecificationId = getValueSpecificationId(specification.getProperties());
              int valueNameId = getValueNameId(specification.getValueName());
              int functionUniqueId = getFunctionUniqueId(specification.getFunctionUniqueId());
              int computeNodeId = getComputeNodeId(computedValue.getComputeNodeId());

              RiskValue riskValue = new RiskValue();

              riskValue.setId(generateUniqueId());
              riskValue.setCalculationConfigurationId(calcConfId);
              riskValue.setValueNameId(valueNameId);
              riskValue.setValueRequirementId(valueRequirementId);
              riskValue.setValueSpecificationId(valueSpecificationId);
              riskValue.setFunctionUniqueId(functionUniqueId);
              riskValue.setComputationTargetId(computationTargetId);
              riskValue.setRunId(riskRunId);
              riskValue.setValue(riskValueEntry.getValue());
              riskValue.setEvalInstant(evalInstant);
              riskValue.setComputeNodeId(computeNodeId);
              successes.add(riskValue.toSqlParameterSource());

            }
          }

          // the check below ensures that
          // if there is a partial failure (some successes, some failures) for a target,
          // only the failures will be written out in the database
        } else if (failedTargets.contains(compTargetSpec)) {

          if (!isWriteErrors()) {
            continue;
          }

          int computationTargetId = getComputationTargetId(compTargetSpec);

          for (ComputedValue computedValue : viewCalculationResultModel.getAllValues(compTargetSpec)) {
            ValueSpecification specification = computedValue.getSpecification();

            ValueRequirement requirement = computedValue.getRequirement();

            int valueRequirementId = getValueRequirementId(requirement.getConstraints());
            int valueSpecificationId = getValueSpecificationId(specification.getProperties());
            int valueNameId = getValueNameId(specification.getValueName());
            int functionUniqueId = getFunctionUniqueId(specification.getFunctionUniqueId());
            int computeNodeId = getComputeNodeId(computedValue.getComputeNodeId());


            RiskFailure failure = new RiskFailure();
            failure.setId(generateUniqueId());
            failure.setCalculationConfigurationId(calcConfId);
            failure.setValueNameId(valueNameId);
            failure.setValueRequirementId(valueRequirementId);
            failure.setValueSpecificationId(valueSpecificationId);
            failure.setFunctionUniqueId(functionUniqueId);
            failure.setComputationTargetId(computationTargetId);
            failure.setRunId(riskRunId);
            failure.setEvalInstant(evalInstant);
            failure.setComputeNodeId(computeNodeId);
            failures.add(failure.toSqlParameterSource());

            switch (computedValue.getInvocationResult()) {

              case MISSING_INPUTS:
              case FUNCTION_THREW_EXCEPTION:

                BatchResultWriterFailure cachedFailure = _errorCache.get(specification);
                if (cachedFailure != null) {
                  for (Number computeFailureId : cachedFailure.getComputeFailureIds()) {
                    FailureReason reason = new FailureReason();
                    reason.setId(generateUniqueId());
                    reason.setRiskFailure(failure);
                    reason.setComputeFailureId(computeFailureId.longValue());
                    failureReasons.add(reason.toSqlParameterSource());
                  }
                }

                break;

              case SUCCESS:

                // maybe this output succeeded, but some other outputs for the same target failed.
                s_logger.debug("Not adding any failure reasons for partial failures / unsupported outputs for now");
                break;

              default:
                throw new RuntimeException("Should not get here");
            }

          }

        } else {
          // probably a PRIMITIVE target. See targetOutputMode == ResultOutputMode.NONE check above.
          s_logger.debug("Not writing anything for target {}", compTargetSpec);
        }
      }


      // STAGE 4. Actually execute the statements worked out in stage 3.

      if (successes.isEmpty()
          && failures.isEmpty()
          && failureReasons.isEmpty()
          && successfulTargets.isEmpty()
          && failedTargets.isEmpty()) {
        s_logger.debug("Nothing to write to DB for {}", resultModel);
        return;
      }

      // need to figure out why these 2 lines are needed for the insertRows() code not to block...
      // this is bad - loss of transactionality...
      getSessionFactory().getCurrentSession().getTransaction().commit();
      getSessionFactory().getCurrentSession().beginTransaction();

      insertRows("risk", RiskValue.sqlInsertRisk(), successes);
      insertRows("risk failure", RiskFailure.sqlInsertRiskFailure(), failures);
      insertRows("risk failure reason", FailureReason.sqlInsertRiskFailureReason(), failureReasons);

      upsertStatusEntries(calcConfigName, StatusEntry.Status.SUCCESS, successfulTargets);
      upsertStatusEntries(calcConfigName, StatusEntry.Status.FAILURE, failedTargets);

    }
  }

  // --------------------------------------------------------------------------

  private void populateErrorCache(ViewResultEntry item) {
    BatchResultWriterFailure cachedFailure = new BatchResultWriterFailure();

    switch (item.getComputedValue().getInvocationResult()) {

      case FUNCTION_THREW_EXCEPTION:

        // an "original" failure
        //
        // There will only be 1 failure reason.

        ComputeFailure computeFailure = getComputeFailureFromDb(item);
        cachedFailure.addComputeFailureId(computeFailure.getId());

        break;

      case MISSING_INPUTS:

        // There may be 1-N failure reasons - one for each failed
        // function in the subtree below this node. (This
        // only includes "original", i.e., lowest-level, failures.)

        for (ValueSpecification missingInput : item.getComputedValue().getMissingInputs()) {

          BatchResultWriterFailure inputFailure = _errorCache.get(missingInput);

          if (inputFailure == null) {

            ComputeFailureKey computeFailureKey = new ComputeFailureKey(
                missingInput.getFunctionUniqueId(),
                "N/A",
                "Missing input " + missingInput,
                "N/A");
            computeFailure = getComputeFailureFromDb(computeFailureKey);
            cachedFailure.addComputeFailureId(computeFailure.getId());

          } else {

            cachedFailure.addComputeFailureIds(inputFailure.getComputeFailureIds());

          }
        }

        break;
    }

    if (!cachedFailure.getComputeFailureIds().isEmpty()) {
      _errorCache.put(item.getComputedValue().getSpecification(), cachedFailure);
    }
  }

  /*package*/ ComputeFailure getComputeFailureFromDb(ViewResultEntry item) {
    if (item.getComputedValue().getInvocationResult() != InvocationResult.FUNCTION_THREW_EXCEPTION) {
      throw new IllegalArgumentException("Please give a failed item");
    }

    ComputeFailureKey computeFailureKey = new ComputeFailureKey(
        item.getComputedValue().getSpecification().getFunctionUniqueId(),
        item.getComputedValue().getExceptionClass(),
        item.getComputedValue().getExceptionMsg(),
        item.getComputedValue().getStackTrace());
    return getComputeFailureFromDb(computeFailureKey);
  }

  /**
   * Instances of this class are saved in the computation cache for each
   * failure (whether the failure is 'original' or due to missing inputs).
   * The set of Longs is a set of compute failure IDs (referencing
   * rsk_compute_failure(id)). The set is built bottom up. 
   * For example, if A has two children, B and C, and B has failed
   * due to error 12, and C has failed due to errors 15 and 16, then
   * A has failed due to errors 12, 15, and 16.
   */
  public static class BatchResultWriterFailure implements MissingInput, Serializable {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;
    private Set<Number> _computeFailureIds = new HashSet<Number>();

    public Set<Number> getComputeFailureIds() {
      return Collections.unmodifiableSet(_computeFailureIds);
    }

    public void setComputeFailureIds(Set<Number> computeFailureIds) {
      _computeFailureIds = computeFailureIds;
    }

    public void addComputeFailureId(Number computeFailureId) {
      addComputeFailureIds(Collections.singleton(computeFailureId));
    }

    public void addComputeFailureIds(Set<? extends Number> computeFailureIds) {
      _computeFailureIds.addAll(computeFailureIds);
    }
  }

  // --------------------------------------------------------------------------
}
