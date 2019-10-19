<#-- @ftlvariable name="metadata" type="ru.mihkopylov.processor.Metadata" -->
# Configuration Properties

<#list metadata.properties as property>
## ${property.name}

<#if property.description??>
${property.description}
</#if>
* **type**: ${property.type}
* **sourceType**: ${property.sourceType}
<#if property.defaultValue??>
* **default value**: ${property.defaultValue}
</#if>

</#list>