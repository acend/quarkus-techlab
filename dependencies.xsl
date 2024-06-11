<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:p="http://maven.apache.org/POM/4.0.0" xmlns:csv="csv:csv">
  <xsl:output method="text"/>

  <xsl:param name="delim" select="','" />
  <xsl:param name="break" select="'&#xA;'" />

  <csv:columns>
    <column>groupId</column>
    <column>artifactId</column>
    <column>version</column>
    <column>scope</column>
  </csv:columns>

  <xsl:template match="/">
    <!-- Output the CSV header -->
    <xsl:for-each select="document('')/*/csv:columns/*">
      <xsl:value-of select="."/>
      <xsl:if test="position() != last()">
        <xsl:value-of select="$delim"/>
      </xsl:if>
    </xsl:for-each>
    
    <xsl:value-of select="$break"/>
  
    <xsl:apply-templates select="p:project/p:dependencies/p:dependency" />
  </xsl:template>

  <!-- Dependency Block -->
  <xsl:template match="p:dependency">
    <xsl:variable name="dependency" select="."/>

    <!-- Loop csv columns -->
    <xsl:for-each select="document('')/*/csv:columns/*">
      <!-- get column -->
      <xsl:variable name="column" select="."/>
      <xsl:variable name="value" select="$dependency/*[name() = $column]"/>
        
      <!-- Quote the value if required -->
      <xsl:choose>
        <xsl:when test="contains($value, '&quot;')">
          <xsl:variable name="x" select="replace($value, '&quot;',  '&quot;&quot;')"/>
          <xsl:value-of select="concat('&quot;', $x, '&quot;')"/>
        </xsl:when>
        <xsl:when test="contains($value, $delim)">
          <xsl:value-of select="concat('&quot;', $value, '&quot;')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$value"/>
        </xsl:otherwise>
      </xsl:choose>
        
      <!-- Add the delimiter unless we are the last expression -->
      <xsl:if test="position() != last()">
        <xsl:value-of select="$delim"/>
      </xsl:if>
    </xsl:for-each>

    <!-- Add a newline at the end -->
    <xsl:value-of select="$break"/>
  </xsl:template>
</xsl:stylesheet>
