<#escape x as x?html>
{
  "types" : [
    <#list configDescriptionMap?keys as key>
    {"value" : "${key}", "name" : "${configDescriptionMap[key]}"}<#if key_has_next>,</#if>
    </#list>
  ],
  "groups" : [
    <#list configGroupMap?keys as groupKey>
    {
      "group" : "${groupKey}",
      "types" : [
        <#list configGroupMap[groupKey]?keys as typeKey>
        {"value" : "${typeKey}", "name" : "${configGroupMap[groupKey][typeKey]}"}<#if typeKey_has_next>,</#if>
        </#list>
      ]
    }<#if groupKey_has_next>,</#if>
    </#list>
  ],
  "curveSpecificationBuilderConfiguration" : [<#list curveSpecs as curveSpec>"${curveSpec}"<#if curveSpec_has_next>,</#if></#list>]
}
</#escape>