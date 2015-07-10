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
    "identifiers": [
      <#list legalEntity.externalIdBundle.externalIds as item>
      {
        "scheme": "${item.scheme.name}",
        "value": "${item.value}"
      }<#if item_has_next>,</#if>
      </#list>
    ],
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
      <#list legalEntity.issuedSecurities as security>
      {
      "security": [
        <#list security.externalIds as item>{"scheme": "${item.scheme.name}", "value": "${item.value}" }<#if item_has_next>,</#if></#list>
      ]
      }<#if security_has_next>,</#if>
      </#list>
    ],
    "obligations": [
      <#list legalEntity.obligations as obligation>
      {
      "name": "${obligation.name}",
      "externalIdBundle": [
          <#list obligation.security.externalIds as item>{"scheme": "${item.scheme.name}", "value": "${item.value}" }<#if item_has_next>,</#if></#list>
      ]
      }<#if obligation_has_next>,</#if>
      </#list>
    ],
    "accounts": [
      <#list legalEntity.accounts as account>
      {
      "name": "${account.name}"
      <#if account.portfolio??>
        ,
        "portfolio": "${account.portfolio}"
      </#if>
      }<#if account_has_next>,</#if>
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
    "details": [
      <#list legalEntity.details?keys as prop>
      {
      "name": "${prop}",
      "value": "${legalEntity.details[prop]}"
      }<#if prop_has_next>,</#if>
      </#list>
    ],
    "issued_securities_oids": [
      <#list issuedSecuritiesOids as prop>
      {
      "name": "${prop.name}",
      "oid": "${prop.oid}"
      }<#if prop_has_next>,</#if>
      </#list>
    ],
    "obligations_oids": [
      <#list obligationsOids as prop>
      {
      "obligation": "${prop.obligation}",
      "name": "${prop.name}",
      "oid": "${prop.oid}"
      }<#if prop_has_next>,</#if>
      </#list>
    ],
    "root_portfolio": <#if legalEntity.rootPortfolio?has_content> "${legalEntity.rootPortfolio.portfolio}" <#else> null </#if>
}
</#escape>