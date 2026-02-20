<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
   
      <html>
        <head>
          <title>Timer Control</title>
          <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb.css"/>
          <script>
           var start = "<xsl:value-of select="//start" />";
           var show = "<xsl:value-of select="//show" />";
          </script>
          <script src="/themes/calvi/javascript/kbsearchepg.js"></script>
        </head>
        
        <body onload="Initialize();">
        
        <table class='KBButtonTable'>
          <tr><td align="center" valign="middle">
            <table>
              <tr>
                <td></td>
                <td><div class="PageTitle">Program Search Menu</div></td>
              </tr>
              <tr> 
                <td></td>
                <td id="back" class="FormInput"><center>Main Menu</center></td>
              </tr>
              <tr> 
                <td class="TextR">Name</td>
                <td id="name" class="FormInput" width="500" height="40" value=""></td>
              </tr>
              <tr> 
                <td class="TextR">Search Type</td>
                <td id="type" class="FormInput" width="250" value="0" option_0="Title/Sub/Desc" option_1="Title" option_2="Subtitle" option_3="Description" option_4="Actor"><span class="solidText">Type</span></td>
              </tr>        
              <tr> 
                <td class="TextR">Channel</td>
                <xsl:apply-templates select="//channel" mode="cell" />
              </tr>
               <tr> 
                <td class="TextR">Category</td>
                <xsl:apply-templates select="//category" mode="cell" />
              </tr>
            </table>
          </td></tr>
        </table>
        
        <div id="keyboard" style="position:absolute; left:0; top:0; visibility: hidden; border: 3px solid #FFFFFF;">
           <table border="0" cellspacing="15" bgcolor="#386EB8">
  
           <tr>
             <td id="keyboardtext" colspan="10" height="40" class="FormInput"></td>
           </tr>
  
           <tr>
             <td id="0-0" align="center" value="1" class="FormInput">1</td>
             <td id="1-0" align="center" value="2" class="FormInput">2</td>
             <td id="2-0" align="center" value="3" class="FormInput">3</td>
             <td id="3-0" align="center" value="4" class="FormInput">4</td>
             <td id="4-0" align="center" value="5" class="FormInput">5</td>
             <td id="5-0" align="center" value="6" class="FormInput">6</td>
             <td id="6-0" align="center" value="7" class="FormInput">7</td>
             <td id="7-0" align="center" value="8" class="FormInput">8</td>
             <td id="8-0" align="center" value="9" class="FormInput">9</td>
             <td id="9-0" align="center" value="0" class="FormInput">0</td>
           </tr>          
           
           <tr>
             <td id="0-1" align="center" value="a" class="FormInput">a</td>
             <td id="1-1" align="center" value="b" class="FormInput">b</td>
             <td id="2-1" align="center" value="c" class="FormInput">c</td>
             <td id="3-1" align="center" value="d" class="FormInput">d</td>
             <td id="4-1" align="center" value="e" class="FormInput">e</td>
             <td id="5-1" align="center" value="f" class="FormInput">f</td>
             <td id="6-1" align="center" value="g" class="FormInput">g</td>
             <td id="7-1" align="center" value="h" class="FormInput">h</td>
             <td id="8-1" align="center" value="i" class="FormInput">i</td>
             <td id="9-1" align="center" value="j" class="FormInput">j</td>
           </tr>
           
           <tr>
             <td id="0-2" align="center" value="k" class="FormInput">k</td>
             <td id="1-2" align="center" value="l" class="FormInput">l</td>
             <td id="2-2" align="center" value="m" class="FormInput">m</td>
             <td id="3-2" align="center" value="n" class="FormInput">n</td>
             <td id="4-2" align="center" value="o" class="FormInput">o</td>
             <td id="5-2" align="center" value="p" class="FormInput">p</td>
             <td id="6-2" align="center" value="q" class="FormInput">q</td>
             <td id="7-2" align="center" value="r" class="FormInput">r</td>
             <td id="8-2" align="center" value="s" class="FormInput">s</td>
             <td id="9-2" align="center" value="t" class="FormInput">t</td>
           </tr>         
           
           <tr>
             <td id="0-3" align="center" value="u" class="FormInput">u</td>
             <td id="1-3" align="center" value="v" class="FormInput">v</td>
             <td id="2-3" align="center" value="w" class="FormInput">w</td>
             <td id="3-3" align="center" value="x" class="FormInput">x</td>
             <td id="4-3" align="center" value="y" class="FormInput">y</td>
             <td id="5-3" align="center" value="z" class="FormInput">z</td>
             <td id="6-3" align="center" value="[" class="FormInput">[</td>
             <td id="7-3" align="center" value="]" class="FormInput">]</td>
             <td id="8-3" align="center" value=" " class="FormInput">_</td>
             <td id="9-3" align="center" value="&lt;" class="FormInput">&lt;</td>
           </tr> 
  
           <tr>
             <td id="0-4" align="center" colspan="10" value="ok" class="FormInput">OK</td>
           </tr> 
  
           </table>
        </div>
        
          <form action="/servlet/KBEpgItemsRes" method="GET" name="data" accept-charset="UTF-8">
          <input type="hidden" name="action" value="04"/>
          <input type="hidden" name="name" value=""/>
          <input type="hidden" name="type" value=""/>
          <input type="hidden" name="cat" value=""/>
          <input type="hidden" name="chan" value=""/>
          <input type="hidden" name="start" value=""/>
          <input type="hidden" name="show" value=""/> 
          </form>
          
        </body>
      </html>
   
	</xsl:template>

	<xsl:template match="channel" mode="cell">	
	   <td id="channel" class="FormInput" width="250">
	   <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
	   <xsl:apply-templates select="option" mode="option" />
	   <span class="solidText">Test Chan</span></td>
	</xsl:template>
	
	<xsl:template match="category" mode="cell">	
	   <td id="category" class="FormInput" width="500">
	   <xsl:attribute name="value"><xsl:value-of select="@value" /></xsl:attribute>
	   <xsl:apply-templates select="option" mode="option" />
	   <span class="solidText">Test Cat</span></td>
	</xsl:template>   
   
	<xsl:template match="option" mode="option">	
	   <xsl:attribute name="option_{position()-1}"><xsl:value-of select="." /></xsl:attribute>
	</xsl:template>	
	   
</xsl:stylesheet>