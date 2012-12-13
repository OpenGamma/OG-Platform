<#escape x as x?html>
    <#macro typeNames infoList>
        <#list infoList as info>
            <#if info.beanType>
                <a href="${info.expectedType}">${info.expectedType}</a>
            <#else>
                ${info.expectedType} <#if info.actualType?has_content>(${info.actualType})</#if>
            </#if>
        </#list>
    </#macro>
    <@page title="${type} Structure">
        <@section title="${type}">
            <@table items=properties empty="" headers=["Property", "Type", "Optional", "Read-only", "Endpoint"]; prop>
            <td>${prop.name}</td>
            <td>
                <#if prop.type == "map">
                    {<@typeNames prop.keyTypeInfo/>:<@typeNames prop.valueTypeInfo/>}
                <#else>
                    <#if prop.type == "array">[</#if>
                    <@typeNames prop.typeInfo/>
                    <#if prop.type == "array">]</#if>
                </#if>
            </td>
            <td><#if prop.isOptional>true</#if></td>
            <td><#if prop.isReadOnly>true</#if></td>
            <td>
                <#if prop.typeInfo?has_content>
                    <#list prop.typeInfo as info>
                        <#if info.endpoint?has_content><a href="/jax/blotter/lookup/${info.endpoint}">/jax/blotter/lookup/${info.endpoint}</a></#if>
                    </#list>
                </#if>
            </td>
            </@table>
        </@section>
        <#if hasUnderlying>
            <@subsection title="Underlying Security">
                <#list underlyingTypeInfo as info>
                    <a href="${info.expectedType}">${info.expectedType}</a><#if info_has_next>, </#if>
                </#list>
            </@subsection>
        </#if>
    </@page>
</#escape>
