<#escape x as x?html>
    <@page title="${type} Structure">
        <@section title="${type}">
            <@table items=properties empty="" headers=["Property", "Type", "Optional", "Read-only", "Endpoint"]; prop>
            <td>${prop.name}</td>
            <td>
                <#if prop.type == "map">
                    {${prop.keyTypeName}: ${prop.valueTypeName}}
                <#else>
                    <#if prop.type == "array">[</#if>
                    <#if prop.isBean>
                        <#list prop.typeNames as typeName>
                            <a href="${typeName}">${typeName}</a><#if typeName_has_next>, </#if>
                        </#list>
                    <#else>
                    ${prop.typeNames[0]}
                    </#if>
                    <#if prop.type == "array">]</#if>
                </#if>
            </td>
            <td><#if prop.isOptional>true</#if></td>
            <td><#if prop.isReadOnly>true</#if></td>
            <td></td>
            </@table>
        </@section>
        <#if hasUnderlying>
            <@subsection title="Underlying Security">
                <#list underlyingTypeNames as typeName>
                    <a href="${typeName}">${typeName}</a><#if typeName_has_next>, </#if>
                </#list>
            </@subsection>
        </#if>
    </@page>
</#escape>
