<#escape x as x?html>
{
  "holidayTypes" : [<#list holidayTypes as key>"${key}"<#if key_has_next>,</#if></#list>]
}
</#escape>