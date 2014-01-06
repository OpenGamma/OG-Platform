<#escape x as x?html>
{
    "template_data": {
        "name": "${legalEntity.name}",
        "object_id": "${legalEntity.uniqueId.objectId}",
        <#if deleted>
        "deleted": "${legalEntityDoc.versionToInstant}",
        </#if>
        "version_id": "${legalEntity.uniqueId.version}"
    },
    "keys": {
      <#list legalEntity.externalIdBundle.externalIds as item>
      "${item.scheme.name}": "${item.value}"<#if item_has_next>,</#if>
      </#list>
    },
    "ratings": [
      <#list legalEntity.ratings as item>
      {
      "rater": "${item.rater}",
      "seniority": "${item.seniorityLevel}",
      "score": "${item.score}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
    "capabilities": [
      <#list legalEntity.capabilities as item>
      {
      "name": "${item.name}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
    "issued_securities": [
      <#list legalEntity.issuedSecurities as item>
      {
      "link": "${item}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
    "obligations": [
      <#list legalEntity.obligations as item>
      {
      "name": "${item.name}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
    "accounts": [
      <#list legalEntity.accounts as item>
      {
      "name": "${item.name}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
    "attributes": [
      <#list legalEntity.attributes?keys as prop>
      {
        "name": "${prop}",
        "value": "${legalEntity.attributes[prop]}"
      }<#if prop_has_next>,</#if>
      </#list>
    ],
    "root_portfolio": <#if legalEntity.rootPortfolio?has_content> "${legalEntity.rootPortfolio.portfolio}"  <#else> null </#if>
}
</#escape>