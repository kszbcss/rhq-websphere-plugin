<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">
    <xsl:import href="urn:docbkx:stylesheet"/>
    <xsl:include href="common.xsl"/>
    
    <xsl:param name="admon.graphics.extension">.svg</xsl:param>
    
    <!-- FOP doesn't render the right arrow correctly; use a right angle instead -->
    <xsl:param name="menuchoice.menu.separator"> > </xsl:param>
</xsl:stylesheet>