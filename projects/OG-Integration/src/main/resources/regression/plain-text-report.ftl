<#-- @ftlvariable name="results" type="com.opengamma.integration.regression.RegressionTestResults" -->
Regression Test Report

Base version: ${results.baseVersion}
Test version: ${results.testVersion}

<#list results.differences as diff>
View definition: ${diff.viewDefinitionName}
Snapshot: ${diff.snapshotName}
Valution time: ${diff.valuationTime}
Number of matching results: ${diff.equalResultCount}
<#if !diff.onlyBase?has_content
  && !diff.onlyTest?has_content
  && !diff.different?has_content
  && !diff.differentProperties?has_content>
No differences
</#if>

<#if diff.onlyBase?has_content>
Results only present in the base version
Total: ${diff.onlyBase?size}

  <#list diff.onlyBase?keys as key>
    <#assign value = diff.getOnlyBaseValue(key)>

  Calc config name: ${key.calcConfigName}
  Value name: ${key.valueName}
  Requirement properties: ${key.properties}
  Target type: ${value.targetType}
  Target name: ${value.targetName}
  Value: ${value.value}

  </#list>
</#if>
<#if diff.onlyTest?has_content>
Results only present in the test version
Total: ${diff.onlyTest?size}

  <#list diff.onlyTest?keys as key>
    <#assign value = diff.getOnlyTestValue(key)>
  Calc config name: ${key.calcConfigName}
  Value name: ${key.valueName}
  Requirement properties: ${key.properties}
  Target type: ${value.targetType}
  Target name: ${value.targetName}
  Value: ${value.value}

  </#list>
</#if>
<#if diff.different?has_content>
Results that differ between the versions
Total: ${diff.different?size}

  <#list diff.different?keys as key>
    <#assign value = diff.getDifferentValue(key)>
  Calc config name: ${key.calcConfigName}
  Value name: ${key.valueName}
  Requirement properties: ${key.properties}
  Target type: ${value.first.targetType}
  Target name: ${value.first.targetName}
  Base value: ${value.first.value}
  Test value: ${value.second.value}
  Base specification properties: ${value.first.specificationProperties}
  Test specification properties: ${value.second.specificationProperties}

  </#list>
</#if>
<#if diff.differentProperties?has_content>
Results with the same value but different specification properties
Total: ${diff.differentProperties?size}

  <#list diff.differentProperties?keys as key>
    <#assign value = diff.getDifferentPropertiesValue(key)>
  Calc config name: ${key.calcConfigName}
  Value name: ${key.valueName}
  Requirement properties: ${key.properties}
  Target type: ${value.first.targetType}
  Target name: ${value.first.targetName}
  Value: ${value.first.value}
  Base specification properties: ${value.first.specificationProperties}
  Test specification properties: ${value.second.specificationProperties}

  </#list>
</#if>
</#list>
