<#escape x as x?html>
{
    "template_data": {
        "name": "${region.name}",
        "object_id": "${region.uniqueId.objectId}",
        "version_id": "${region.uniqueId.version}",
        <#if deleted>
        "deleted": "${regionDoc.versionToInstant}",
        </#if>
        "full_name": "${region.fullName}",
        "classification": "${region.classification}"
    },
    "keys": {
        <#list region.externalIdBundle.externalIds as item>
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