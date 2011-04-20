<#escape x as x?html>
{
    "templateData": {
            "name": "${region.name}",
            "full_name": "${region.fullName}",
            "classification": "${region.classification}"
    },
    "keys": {
        <#list region.identifiers.identifiers as item>
            "${item.scheme.name}": "${item.value}"<#if item_has_next>,</#if>
        </#list>
    },
    "parent": [
            <#list regionParents as item>
            {
                "name": "${item.region.name}",
                "id": "${item.uniqueId.objectId}"
            }<#if item_has_next>,</#if>
		</#list>
    ],
    "child": [
    	<#list regionChildren as item>
            {
                "name": "${item.region.name}",
                "id": "${item.uniqueId.objectId}"
            }<#if item_has_next>,</#if>
		</#list>
    ]
}
</#escape>