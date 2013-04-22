<#escape x as x?html>
<#include "security-header.ftl"> 
  "cds_version":"${security.version}",
  "series":"${security.series}",
  "family":"${security.family}",
  "recovery":"${security.recoveryRate}",
  "currency":"${security.currency}",
  "terms":[<#list terms as tenor>"${tenor}"<#if tenor_has_next>,</#if></#list>],
  "components":[<#list components as component>
    {<#if component.bondId?has_content>
        "bond":{"scheme":"${component.bondId.scheme.name}", "value":"${component.bondId.value}"},
    <#else>
        "bond": {},
    </#if>
     "obligor":{"scheme":"${component.obligorRedCode.scheme.name}", "value":"${component.obligorRedCode.value}"}, 
     "weight": ${component.weight}, 
     "name": "${component.name}"}
     <#if component_has_next>,</#if></#list>],
<#include "security-footer.ftl"> 
</#escape>