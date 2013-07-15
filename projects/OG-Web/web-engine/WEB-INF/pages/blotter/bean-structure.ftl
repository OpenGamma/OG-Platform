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
            <@table items=properties empty="" headers=["Property", "Type", "Value", "Nullable", "Read-only", "Endpoint"]; prop>
            <td>${prop.name}</td>
            <td>
                <#if prop.type == "map">
                    {<@typeNames prop.types/>:<@typeNames prop.valueTypes/>}
                <#else>
                    <#if prop.type == "array">[</#if>
                    <@typeNames prop.types/>
                    <#if prop.type == "array">]</#if>
                </#if>
            </td>
            <td><#if prop.value?has_content>${prop.value}</#if></td>
            <td><#if prop.optional>true</#if></td>
            <td><#if prop.readOnly>true</#if></td>
            <td>
                <#if prop.types?has_content>
                    <#list prop.types as info>
                        <#if info.endpoint?has_content><a href="/jax/blotter/lookup/${info.endpoint}">/jax/blotter/lookup/${info.endpoint}</a></#if>
                    </#list>
                </#if>
            </td>
            </@table>
        </@section>
        <#if underlyingTypes?has_content>
            <@subsection title="Underlying Security">
                <#list underlyingTypes as info>
                    <a href="${info.expectedType}">${info.expectedType}</a><#if info_has_next>, </#if>
                </#list>
            </@subsection>
        </#if>
    </@page>
</#escape>
