<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

   <xsl:template match="/">
  
      <html>
      <head>
      <title>Timer Control</title>
      <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
      <script>
        var back = "<xsl:value-of select="//@back" />";
      </script>      
      <script src="/themes/calvi/javascript/kbbuttons.js"></script>
      
      </head>
      <body onload="Initialize();">
      
      <table class='KBListTable'>
        <tr class='Row'>
          <td class='itemdata' id='headingtitle'>
            <span class='solidText'>File Manager Table</span>
          </td>
          <td class='itemdataNoWrap' id='page' style="text-align:right;width:100">
            <xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@end" />
          </td>
        </tr>
        <tr>
          <td class='itemdata' id='contextline'>
            <span class='solidText'>contextline</span>    
          </td>
          <td class='itemdataNoWrap' id='pages' style="text-align:right">
           of <xsl:value-of select="//@total" />
          </td> 
        </tr>
        <tr height='10'></tr>
      </table>
      
      <table class='KBListTable'>
        <xsl:if test="//@start = 0">
          <tr>
            <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
              <xsl:attribute name="id">item_0</xsl:attribute>
              <xsl:attribute name="value">/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons</xsl:attribute>
              <xsl:attribute name="contextline">Return to Main Menu</xsl:attribute>
              <span class='solidText'>Main Menu</span>
            </td>
          </tr>
        </xsl:if> 
        <xsl:if test="//@start > 0">
          <tr>
            <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
              <xsl:attribute name="id">item_0</xsl:attribute>
              <xsl:attribute name="value">/servlet/KBFileManagerRes?start=<xsl:value-of select="//@start - //@show" />&amp;show=<xsl:value-of select="//@show" />&amp;path=<xsl:value-of select="//@path" /></xsl:attribute>
              <xsl:attribute name="contextline">Return to Previous Page</xsl:attribute>
              <span class='solidText'>Previous Page</span>
            </td>
          </tr>              
        </xsl:if>
              
        <xsl:apply-templates select="//file" mode="rows" />
        
        <xsl:if test="//@end != //@total">
            <tr>
              <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2'>
              <xsl:attribute name="id">item_<xsl:value-of select="//@show + 1" /></xsl:attribute>
              <xsl:attribute name="value">/servlet/KBFileManagerRes?start=<xsl:value-of select="//@end" />&amp;show=<xsl:value-of select="//@show" />&amp;path=<xsl:value-of select="//@path" /></xsl:attribute>
              <xsl:attribute name="contextline">Advance to Next Page</xsl:attribute>
              <span class='solidText'>Next Page</span>
            </td>
          </tr>
        </xsl:if>      
      </table>
      
      </body>
      </html>   
   
	</xsl:template>

	<xsl:template match="file" mode="rows">
    <tr>
      <td width='100%' style="padding-left:10px;" activeClass='ActiveRow' defaultClass='Row' class='Row'>
        <xsl:attribute name="id">item_<xsl:value-of select="position()" /></xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="action" /></xsl:attribute>
        <xsl:attribute name="contextline">Item <xsl:value-of select="position() + //@start" /> of <xsl:value-of select="//@total" /></xsl:attribute>          
        <xsl:value-of select="name" />
      </td>
      <td class='itemdataNoWrap'>
        <xsl:if test="size != ''">
          <xsl:value-of select="size" />.<xsl:value-of select="size//@units" />
        </xsl:if>
        <xsl:if test="size = ''">
          <span class='solidText'>Folder</span>
        </xsl:if>  
      </td>
    </tr> 
	</xsl:template>
   
</xsl:stylesheet>