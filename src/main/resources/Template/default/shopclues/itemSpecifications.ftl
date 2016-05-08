[
<#list data?values as key>
	{
	"id":"${key.FeatureId}",
	"name":"${key.FeatureName}",
	<#if key.FeatureType == "S">"inputType" : "RepresentText(Others)",<#elseif key.FeatureType == "M">"inputType" : "CheckBox",<#elseif key.FeatureType == "N">"inputType" : "SelectBox",<#elseif key.FeatureType == "E">"inputType" : "ExtendedSelectBox",<#elseif key.FeatureType == "T">"inputType" : "RepresentText(Others)",<#elseif key.FeatureType == "O">"inputType" : "RepresentNumber(Others)",<#elseif key.FeatureType == "D">"inputType" : "RepresentDate(Others)",<#elseif key.FeatureType == "Z">"inputType" : "RepresentTextArea(Others)",<#elseif key.FeatureType == "C">"inputType" : "RepresentSingle(Check Box )",</#if>
	"IsFilterFeature" : ${key.IsFilterFeature},
	"IsRequiredFeature" : ${key.IsRequiredFeature}<#if key_has_next>,</#if>
	}<#if key_has_next>,</#if>
</#list>
]