<#escape x as x?html>
{
  "configTypes" : [<#list typeMap?keys as key>"${key}"<#if key_has_next>,</#if></#list>]
}
</#escape>