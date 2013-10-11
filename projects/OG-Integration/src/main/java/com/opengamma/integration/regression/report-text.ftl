<#-- @ftlvariable name="results" type="com.opengamma.integration.regression.RegressionTestResults" -->
Regression Test Report

Base version: ${results.baseVersion}
Test version: ${results.testVersion}
Valution time: TODO

<#list results.differences as diff>

View definition: ${diff.viewDefinitionName}
Snapshot: ${diff.snapshotName}
Number of matching results: ${diff.equalResultCount}

</#list>
