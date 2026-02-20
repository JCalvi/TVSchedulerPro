<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
     
  <html>
  <head>
    <title>List Selection</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css" />
    <script src="/themes/calvi/javascript/kbbuttons.js"></script>
  </head>
  
  <body onload="Initialize();">
       
  <table class='KBListTable'>
    <xsl:if test="//@title">
      <tr>
        <td class='itemdata' >    
          <xsl:value-of select="//@title" />
        </td>
        <td class='itemdataNoWrap' id='page' style="text-align:right;width:100">
          <xsl:choose>
            <xsl:when test="number(//@start) + number(//@show) &lt; count(//button)">
              <xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@show + //@start" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="//@start + 1" /> to <xsl:value-of select="//@total" />
            </xsl:otherwise>
          </xsl:choose> 
        </td>
      </tr>
    </xsl:if>
    <td class='itemdata' id='contextline' >
      <span class='solidText'>contextline</span>    
    </td>
    <td class='itemdata' id='pages' style="text-align:right" > of <xsl:value-of select="//@total" /></td>
    <tr height='10'></tr>
  </table>
  
  <table class='KBListTable'> 
    <xsl:if test="number(//@start) = 0">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
          <xsl:attribute name="id">item_0</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="//@back" /></xsl:attribute>
          <xsl:if test="contains(//@back,'epg-index')">
             <xsl:attribute name="value">/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons</xsl:attribute>
          </xsl:if>
          <xsl:attribute name="contextline">Return to Previous Menu</xsl:attribute>          
          <span class='solidText'>Previous Menu</span>
        </td>
      </tr>
    </xsl:if>     
    <xsl:if test="number(//@start) > 0">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
          <xsl:attribute name="id">item_0</xsl:attribute>
          <xsl:if test="number(//@start) - number(//@show) > -1">
             <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=<xsl:value-of select="number(//@start) - number(//@show)" />&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          </xsl:if>
          <xsl:if test="number(//@start) - number(//@show) &lt; 0">
             <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=0&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          </xsl:if>
          <xsl:attribute name="contextline">Return to Previous Page</xsl:attribute>            
          <span class='solidText'>Previous Page</span>
        </td>
      </tr>
    </xsl:if>
    <xsl:apply-templates select="//button" mode="rows">
      <xsl:with-param name="start" select="//@start" />
      <xsl:with-param name="show" select="//@show" />
    </xsl:apply-templates>
    <xsl:if test="number(//@start) + number(//@show) &lt; count(//button)">
      <tr>
        <td activeClass='ActiveRow' defaultClass='Row' class='Row' colspan='2' >
          <xsl:attribute name="id">item_<xsl:value-of select="//@show + 1" /></xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="//mainurl" />start=<xsl:value-of select="//@start + //@show" />&amp;show=<xsl:value-of select="//@show" /></xsl:attribute>
          <xsl:attribute name="contextline">Advance to Next Page</xsl:attribute>
          <span class='solidText'>Next Page</span>
        </td>
      </tr>
    </xsl:if>          
  </table>
    
  <div id="confirm" style="position:absolute; left:0; top:0; visibility: hidden;">
    <table border="0" cellspacing="15" bgcolor="#386EB8">
      <tr>
        <td colspan="2" class="questionText">
          <div id="confirmText">Please Confirm The Action!</div>
        </td>
      </tr>
      <tr>
        <td id="item2_0" value="yes" activeClass="ActiveButton" defaultClass="Button" class="Button" width="200px">Yes</td>
        <td id="item2_1" value="no" activeClass="ActiveButton" defaultClass="Button" class="Button" width="200px">No</td>
      </tr>
    </table>
  </div>        
    
  </body>
  </html>     
     
  </xsl:template>

  <xsl:template match="button" mode="rows">
    <xsl:param name="start"/>
    <xsl:param name="show"/>
    <xsl:if test="position() &gt; number($start)">
      <xsl:if test="position() &lt; number($start) + number($show) + 1">
         <tr>
           <td activeClass='ActiveRow' defaultClass='Row' class='Row' style="padding-left:10;" colspan='2'>
             <xsl:attribute name="id">item_<xsl:value-of select="position() - number($start)" /></xsl:attribute>
             <xsl:attribute name="contextline">Item <xsl:value-of select="position()" /> of <xsl:value-of select="count(//button)" /></xsl:attribute>
             <xsl:attribute name="value"><xsl:value-of select="url" /></xsl:attribute>
             <xsl:attribute name="confirm"><xsl:value-of select="confirm" /></xsl:attribute>
             <xsl:value-of select="@name" />
           </td>
         </tr>    
      </xsl:if>
    </xsl:if>
  </xsl:template>
   
</xsl:stylesheet>