<#escape x as x?html>
{
  "types" : [<#list configTypes as configType>"${configType}"<#if configType_has_next>,</#if></#list>],
  "curveSpecificationBuilderConfiguration" : [<#list curveSpecs as curveSpec>"${curveSpec}"<#if curveSpec_has_next>,</#if></#list>]
}
</#escape>