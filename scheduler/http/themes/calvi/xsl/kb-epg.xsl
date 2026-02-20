<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
  
  <html>
  <head>
    <title>Timer Control</title>
    <link rel="stylesheet" type="text/css" href="/themes/calvi/css/kb-epg.css"/>
    <xsl:apply-templates select="//navigation" />
    <script>
    var backTarget = "/servlet/ApplyTransformRes?xml=root&amp;xsl=kb-buttons";
    </script>
    <script src="/themes/calvi/javascript/kbepg.js"></script>
  </head>
  
  <body onload="Initialize();" >
  
  <table class ='epgHeader'>
    <tr>   
      <td align='center' valign="top" class='epgProgram' activeClass="epgProgramSelected" normalClass="epgProgram" progName='Back' progAdd='/servlet/KBEpgDataRes' progTime='n/a' desc='Press select button to close or Left for Previous Page and Right for Next Page of program items' id='0-0'><span class='epgTitle'>Program Guide for <xsl:apply-templates select="//@title" mode="cols" /></span></td>
    </tr>
  </table>   
  
  <table class='epgTableTimes' name='epgtable'>
    <col width="110px"/>
    <tr>
      <th class='epgTimes'>&#160;</th>
      <xsl:apply-templates select="//time" mode="cols" />
    </tr>
    <tr>
      <td class='epgTimes'>&#160;</td>
      <xsl:apply-templates select="//now_pointer" mode="cols" />
    </tr>
  </table>
  
   <table class='epgTable' name='epgtable'>
    <col width="110px"/>
    <xsl:apply-templates select="//channel" mode="rows" ></xsl:apply-templates>
  </table>
  
  <table class='epgFooter'>
    <tr>
      <td id='item_Title' class='epg_item_title'>&#160;</td>
      <td id='item_Time' class='epg_item_time'>&#160;</td>
    </tr>
    <tr>
      <td id='item_Details' colspan='2' class='epg_item_details'>&#160;</td>
    </tr>
  </table>
  
  </body>
  </html>   
   
  </xsl:template>

  <xsl:template match="time" mode="cols">
    <th class='epgTimes' colspan='30'>
      <xsl:value-of select="." />
    </th>
  </xsl:template>

  <xsl:template match="now_pointer" mode="cols">
    <td style="text-align: right; padding: 0px;">
      <xsl:attribute name="colspan"><xsl:value-of select="@min + 2" /></xsl:attribute>
      <img src="/themes/calvi/images/pointer-left.gif"/>
    </td>
    <td style="text-align: left; padding: 0px;">
      <img src="/themes/calvi/images/pointer-right.gif"/>
    </td>
  </xsl:template> 

  <xsl:template match="channel" mode="rows">
    <xsl:variable name="filter" select="display-name"/>
    <xsl:variable name="rowPosition" select="position() * 5"/>
    <tr>
      <!-- Add blank row at top of EPG to separate -->
      <xsl:if test="position() = 1">
        <tr><td class='epgChannel'>&#160;</td></tr>
      </xsl:if>
      <td class='epgChannel'><xsl:value-of select="substring(display-name, 0, 11)" /></td>
      <xsl:apply-templates select="//programme[@channel=$filter]">
        <xsl:with-param name="rowPosition" select="$rowPosition" />
      </xsl:apply-templates>
    </tr>

    <xsl:if test="count(//schedule[@channel=$filter and @overlapCount=0]) > 0">
      <tr>
        <td></td>
        <xsl:apply-templates select="//schedule[@channel=$filter and @overlapCount=0]">
          <xsl:with-param name="rowPosition" select="$rowPosition + 1" />
        </xsl:apply-templates> 
      </tr>  
    </xsl:if>

    <xsl:if test="count(//schedule[@channel=$filter and @overlapCount=1]) > 0">
      <tr>
        <td></td>
        <xsl:apply-templates select="//schedule[@channel=$filter and @overlapCount=1]">
          <xsl:with-param name="rowPosition" select="$rowPosition + 2" />
        </xsl:apply-templates> 
      </tr>             
    </xsl:if>

    <xsl:if test="count(//schedule[@channel=$filter and @overlapCount=2]) > 0">
    <tr>
      <td></td>
      <xsl:apply-templates select="//schedule[@channel=$filter and @overlapCount=2]">
        <xsl:with-param name="rowPosition" select="$rowPosition + 3" />
      </xsl:apply-templates> 
    </tr>             
    </xsl:if>  
    
    <xsl:if test="count(//schedule[@channel=$filter and @overlapCount=3]) > 0">
      <tr>
        <td></td>
        <xsl:apply-templates select="//schedule[@channel=$filter and @overlapCount=3]">
          <xsl:with-param name="rowPosition" select="$rowPosition + 4" />
        </xsl:apply-templates> 
      </tr>             
    </xsl:if>
      
  </xsl:template>

  <xsl:template name="ProgramScheduleText">
    <xsl:if test="scheduled/@state = 0">Scheduled <xsl:value-of select="scheduled/start"/>-<xsl:value-of select="scheduled/stop"/> (Waiting) : </xsl:if>
    <xsl:if test="scheduled/@state = 1">Scheduled <xsl:value-of select="scheduled/start"/>-<xsl:value-of select="scheduled/stop"/> (Skipped) : </xsl:if>
    <xsl:if test="scheduled/@state = 2">Scheduled <xsl:value-of select="scheduled/start"/>-<xsl:value-of select="scheduled/stop"/> (Running) : </xsl:if>
    <xsl:if test="scheduled/@state = 3">Scheduled <xsl:value-of select="scheduled/start"/>-<xsl:value-of select="scheduled/stop"/> (Finished) : </xsl:if> 
    <xsl:if test="scheduled/@state = 5">Scheduled <xsl:value-of select="scheduled/start"/>-<xsl:value-of select="scheduled/stop"/> (Error) : </xsl:if>           
  </xsl:template> 
 
  <xsl:template match="programme">
      <xsl:param name="rowPosition"/>
      <td activeClass='epgProgramSelected' normalClass='epgProgram' class='epgProgram'>
        <xsl:if test="scheduled/@state = 0">
           <xsl:attribute name="activeClass">epgScheduleSelected</xsl:attribute>
           <xsl:attribute name="normalClass">epgSchedule</xsl:attribute>
           <xsl:attribute name="class">epgSchedule</xsl:attribute>
        </xsl:if>      
  
        <xsl:if test="scheduled/@state = 1">
           <xsl:attribute name="activeClass">epgScheduleSkippedSelected</xsl:attribute>
           <xsl:attribute name="normalClass">epgScheduleSkipped</xsl:attribute>
           <xsl:attribute name="class">epgScheduleSkipped</xsl:attribute>
        </xsl:if> 
        
        <xsl:if test="scheduled/@state = 2">
           <xsl:attribute name="activeClass">epgScheduleRunningSelected</xsl:attribute>
           <xsl:attribute name="normalClass">epgScheduleRunning</xsl:attribute>
           <xsl:attribute name="class">epgScheduleRunning</xsl:attribute>
        </xsl:if> 
        
        <xsl:if test="scheduled/@state = 3">
           <xsl:attribute name="activeClass">epgScheduleFinishedSelected</xsl:attribute>
           <xsl:attribute name="normalClass">epgScheduleFinished</xsl:attribute>
           <xsl:attribute name="class">epgScheduleFinished</xsl:attribute>
        </xsl:if>
        
        <xsl:if test="scheduled/@state = 5">
           <xsl:attribute name="activeClass">epgScheduleErrorSelected</xsl:attribute>
           <xsl:attribute name="normalClass">epgScheduleError</xsl:attribute>
           <xsl:attribute name="class">epgScheduleError</xsl:attribute>
        </xsl:if>
        
        <xsl:attribute name="id"><xsl:value-of select="$rowPosition" />-<xsl:value-of select="position() - 1" /></xsl:attribute>
        <xsl:if test="length/@fits = 1">
           <xsl:attribute name="style">border-right: 6px solid #BBBBFF; border-left: 6px solid #BBBBFF;</xsl:attribute>
           <xsl:if test="scheduled/@state &gt; -1 and scheduled/@state != 2">
              <xsl:attribute name="style">border-right: 6px solid #50FF50; border-left: 6px solid #50FF50;</xsl:attribute>
           </xsl:if>
           <xsl:if test="scheduled/@state = 2">
              <xsl:attribute name="style">border-right: 6px solid #FF5050; border-left: 6px solid #FF5050;</xsl:attribute>
           </xsl:if>         
        </xsl:if>
        
        <xsl:if test="length/@fits = 2">
           <xsl:attribute name="style">border-left: 6px solid #BBBBFF;</xsl:attribute>
           <xsl:if test="scheduled/@state &gt; -1 and scheduled/@state != 2">
              <xsl:attribute name="style">border-left: 6px solid #50FF50;</xsl:attribute>
           </xsl:if>
           <xsl:if test="scheduled/@state = 2">
              <xsl:attribute name="style">border-left: 6px solid #FF5050;</xsl:attribute>
           </xsl:if>            
        </xsl:if>
  
        <xsl:if test="length/@fits = 3">
           <xsl:attribute name="style">border-right: 6px solid #BBBBFF;</xsl:attribute>
           <xsl:if test="scheduled/@state &gt; -1 and scheduled/@state != 2">
              <xsl:attribute name="style">border-right: 6px solid #50FF50;</xsl:attribute>
           </xsl:if>
           <xsl:if test="scheduled/@state = 2">
              <xsl:attribute name="style">border-right: 6px solid #FF5050;</xsl:attribute>
           </xsl:if>           
        </xsl:if>
  
        <xsl:variable name="maxSize" select="500" />
        <xsl:choose>
          <xsl:when test="string-length(desc) = 0">
             <xsl:attribute name="desc">
             <xsl:if test="string-length(sub-title) > 1">
               <xsl:value-of select="sub-title" />
             </xsl:if>
             <xsl:call-template name="ProgramScheduleText"/>&#160;</xsl:attribute>
          </xsl:when>      
          <xsl:when test="string-length(desc) &lt; $maxSize">
             <xsl:attribute name="desc">
             <xsl:if test="string-length(sub-title) > 1">
               [<xsl:value-of select="sub-title" />]:
             </xsl:if> 
             <xsl:call-template name="ProgramScheduleText"/><xsl:value-of select="desc" /></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
              <xsl:attribute name="desc">
              <xsl:if test="string-length(sub-title) > 1">
               [<xsl:value-of select="sub-title" />]:
             </xsl:if>
             <xsl:call-template name="ProgramScheduleText"/>
             <xsl:value-of select="substring(desc, 0, number($maxSize)-2)" />--></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
  
        <xsl:attribute name="progName"><xsl:value-of select="title" /></xsl:attribute>
        <xsl:attribute name="progTime"><xsl:value-of select="@start" />-<xsl:value-of select="@stop" />(<xsl:value-of select="programLength" />)</xsl:attribute>
        <xsl:attribute name="progAdd"><xsl:value-of select="showDetails" /></xsl:attribute>
        <xsl:attribute name="progChan"><xsl:value-of select="@channel" /></xsl:attribute>
        <xsl:attribute name="colspan"><xsl:value-of select="length" /></xsl:attribute>
        <xsl:attribute name="title"><xsl:value-of select="title" /></xsl:attribute>
  
        <span class="solidText"><xsl:value-of select="title" /> </span>
      </td>

  </xsl:template>
 
  <xsl:template match="schedule">
   
    <xsl:param name="rowPosition"/>
    <xsl:if test="@prePadding > 0">
       <td class='epgClear'>
         <xsl:attribute name="colspan"><xsl:value-of select="@prePadding" /></xsl:attribute>
       </td>
    </xsl:if> 
 
    <td activeClass='epgScheduleSelected' normalClass='epgSchedule' class='epgSchedule'>
      <xsl:if test="@fits = 1">
         <xsl:attribute name="style">border-right: 6px solid #ff47f9; border-left: 6px solid #ff47f9;</xsl:attribute>
      </xsl:if>
  
      <xsl:if test="@fits = 2">
         <xsl:attribute name="style">border-left: 6px solid #ff47f9;</xsl:attribute>
      </xsl:if>
  
      <xsl:if test="@fits = 3">
         <xsl:attribute name="style">border-right: 6px solid #ff47f9;</xsl:attribute>
      </xsl:if>
      
      <xsl:if test="itemState = 1">
         <xsl:attribute name="activeClass">epgScheduleSkippedSelected</xsl:attribute>
         <xsl:attribute name="normalClass">epgScheduleSkipped</xsl:attribute>
         <xsl:attribute name="class">epgScheduleSkipped</xsl:attribute>
      </xsl:if> 
      
      <xsl:if test="itemState = 2">
         <xsl:attribute name="activeClass">epgScheduleRunningSelected</xsl:attribute>
         <xsl:attribute name="normalClass">epgScheduleRunning</xsl:attribute>
         <xsl:attribute name="class">epgScheduleRunning</xsl:attribute>
      </xsl:if> 
      
      <xsl:if test="itemState = 3">
         <xsl:attribute name="activeClass">epgScheduleFinishedSelected</xsl:attribute>
         <xsl:attribute name="normalClass">epgScheduleFinished</xsl:attribute>
         <xsl:attribute name="class">epgScheduleFinished</xsl:attribute>
      </xsl:if>
      
      <xsl:if test="itemState = 5">
         <xsl:attribute name="activeClass">epgScheduleErrorSelected</xsl:attribute>
         <xsl:attribute name="normalClass">epgScheduleError</xsl:attribute>
         <xsl:attribute name="class">epgScheduleError</xsl:attribute>
      </xsl:if>
  
      <xsl:attribute name="id"><xsl:value-of select="$rowPosition" />-<xsl:value-of select="position() - 1" /></xsl:attribute>
      <xsl:attribute name="desc"><xsl:value-of select="@start" />-<xsl:value-of select="@stop" />(<xsl:value-of select="@duration" />) <xsl:value-of select="@channel" /> - <xsl:value-of select="itemStatus" /></xsl:attribute>
      <xsl:attribute name="progName"><xsl:value-of select="title" /></xsl:attribute>
      <xsl:attribute name="progTime"><xsl:value-of select="@start" />-<xsl:value-of select="@stop" />(<xsl:value-of select="@duration" />)</xsl:attribute>
      <xsl:attribute name="progAdd"><xsl:value-of select="progAdd" /></xsl:attribute>
      <xsl:attribute name="colspan"><xsl:value-of select="@span" /></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="title" /></xsl:attribute>
      <xsl:attribute name="progEdit"><xsl:value-of select="progEdit" /></xsl:attribute>
  
      <span class='solidText'>
         <xsl:value-of select="@start" /> (<xsl:value-of select="@duration" />) <xsl:value-of select="itemStatus" />
      </span>
    </td>

  </xsl:template> 
 
  <xsl:template match="navigation">
    <script>
      var prevURL = '<xsl:value-of select="previous" />';
      var nextURL = '<xsl:value-of select="next" />';
      var reqYear = '<xsl:value-of select="//@year" />';
      var reqMonth = '<xsl:value-of select="//@month" />';
      var reqDay = '<xsl:value-of select="//@day" />';
      var reqStart = '<xsl:value-of select="//@start" />';
      var reqShow = '<xsl:value-of select="//@show" />';
      var selected = '<xsl:value-of select="selected" />';
    </script>
  </xsl:template>  
    
</xsl:stylesheet>