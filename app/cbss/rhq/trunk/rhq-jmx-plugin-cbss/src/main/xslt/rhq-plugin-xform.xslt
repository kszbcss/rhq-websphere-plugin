<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:c="urn:xmlns:rhq-configuration" xmlns:p="urn:xmlns:rhq-plugin">
    <xsl:template match="/p:plugin/p:server/p:plugin-configuration/c:simple-property[@name='type']/c:property-options/c:option[@name='WebSphere']">
        <c:option value="be.fgov.kszbcss.websphere.rhq.ems.metadata.WebsphereConnectionTypeDescriptor" name="WebSphere"/>
	</xsl:template>
    
    <xsl:template match="/p:plugin/p:server/p:plugin-configuration/c:template[@name='WebSphere']">
        <c:template name="WebSphere" description="Connect to WebSphere 6.1 or 7.0">
            <c:simple-property name="type" default="be.fgov.kszbcss.websphere.rhq.ems.metadata.WebsphereConnectionTypeDescriptor"/>
            <c:simple-property name="connectorAddress" default="localhost:9100"/>
            <c:simple-property name="principal" default="admin"/>
        </c:template>
    </xsl:template>
    
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>