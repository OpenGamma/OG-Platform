<#escape x as x?html>
<#include "security-header.ftl"> 
  "cds_version":"${security.version}",
  "series":"${security.series}",
  "family":"${security.family}",
  "currency":"${security.currency}",
  "terms":[<#list terms as tenor>"${tenor}"<#if tenor_has_next>,</#if></#list>],
  "components":[<#list components as component>
    {"obligor":{"scheme":"${component.obligorRedCode.scheme.name}", "value":"${component.obligorRedCode.value}"}, 
     "weight": ${component.weight}, 
     "name": "${component.name}", 
     "bond":{"scheme":"${component.bondId.scheme.name}", "value":"${component.bondId.value}"}}
     <#if component_has_next>,</#if></#list>],
<#include "security-footer.ftl"> 
</#escape>