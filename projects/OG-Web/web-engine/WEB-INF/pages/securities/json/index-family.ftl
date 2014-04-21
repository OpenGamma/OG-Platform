<#escape x as x?html>
<#include "security-header.ftl"> 
<#if members?has_content>
    "members" : {
    <#list members?keys as key>
      "${key}" : "${members[key].scheme.name}~${members[key].value}"<#if key_has_next>,</#if>
    </#list>
    },
<#else>
    "members" : {},
</#if>
<#include "security-footer.ftl"> 
</#escape>
