var row = 0;
var col = 0;

var lastCellLeft = 0;
var lastCellRight = 0;

function addEvent(objObject, strEventName, fnHandler)
{
   if (objObject.addEventListener) // DOM-compliant way to add an event listener
   {
      objObject.addEventListener(strEventName, fnHandler, false);
   }
   else if (objObject.attachEvent) // IE/windows way to add an event listener
   {
      objObject.attachEvent("on" + strEventName, fnHandler);
   }
}

function Initialize()
{
   addEvent(document, "keydown", onKeyDownEvent);

	getRowCookie();
	getStartingPoint();

	document.title = "page ready";

	if(selected.length > 0)
	   findSelectedItem();

	selectInput();
}

function findSelectedItem()
{
   var cell = null;
   var finished = false;

   for(rowT = 5; rowT < 300; rowT = rowT+5)
   {
      for(colT = 0; colT < 100; colT++)
      {
         cell = document.getElementById(rowT + "-" + colT);

         if(cell == null)
         {
            break;
         }

         var name = cell.getAttribute("progName");
         var chan = cell.getAttribute("progChan");
         if(name != null && chan != null)
         {
            if((name + "-" + chan) == selected)
            {
               row = rowT;
               col = colT;
               finished = true;
            }
         }
      }

      if(finished)
         break;
   }
}

function onKeyDownEvent(nKeyCode)
{
	var keyCode = nKeyCode.keyCode;
   //alert(keyCode);
   //scrollTo(0,row*2);

	if(keyCode == 39) //right
	{
      unSelectInput();
      incrementCount(true);
      selectInput();
	}
	else if(keyCode == 37) //left
	{
   	unSelectInput();
   	decrementCount(true);
      selectInput();
	}
	else if(keyCode == 40) //down
	{
   	unSelectInput();
   	addChannel();
   	selectInput();
   	setRowCookie();
	}
	else if(keyCode == 38) //up
	{
   	unSelectInput();
   	decChannel();
   	selectInput();
   	setRowCookie();
	}
	else if(keyCode == 13)
	{
		processEnter();
	}
	else if (keyCode == 36 || keyCode == 27) // home
	{
   	document.location.href= backTarget;;
	}
	else if(keyCode == 33)
	{
	   document.location.href = nextURL;
	}
	else if(keyCode == 34)
	{
	   document.location.href = prevURL;
	}
	else if(keyCode == 107) // keypad +
	{
		var newZoom = parseInt(reqShow) + 1;
		if(newZoom > 5) newZoom = 5;
		var zoomIn = "/servlet/KBEpgDataRes?action=01&year=" + reqYear + "&month=" + reqMonth + "&day=" + reqDay + "&start=" + reqStart;
		zoomIn += "&span=" + newZoom;
		document.location.href = zoomIn;
	}
	else if(keyCode == 109) // keypad -
	{
		var newZoom = parseInt(reqShow) - 1;
		if(newZoom < 1) newZoom = 1;
		var zoomIn = "/servlet/KBEpgDataRes?action=01&year=" + reqYear + "&month=" + reqMonth + "&day=" + reqDay + "&start=" + reqStart;
		zoomIn += "&span=" + newZoom;
		document.location.href = zoomIn;
	}

	return false;
}

function processEnter()
{
   var cell = document.getElementById(row + "-" + col);
   if(cell != null)
   {
      var progAdd = cell.getAttribute("progAdd");

      if(cell.getAttribute("progAdd"))
      {
         //alert("Adding " + cell.getAttribute("progAdd"));
         document.location.href = cell.getAttribute("progAdd");
      }
      else if(cell.getAttribute("progEdit"))
      {
         //alert("editing " + cell.progEdit);
         document.location.href = cell.getAttribute("progEdit");;
      }
   }
}

function unSelectInput()
{
	var cell = document.getElementById(row + "-" + col);
	if(cell)
	{
	   cell.className = cell.getAttribute("normalClass");
		//cell.className = "epgProgram";

		lastCellLeft = cell.offsetLeft;
		lastCellRight = cell.offsetLeft + cell.clientWidth;
	}
}

function selectInput()
{
	var cell = document.getElementById(row + "-" + col);
	if(cell != null)
	{
	   displayItemDetails(cell);
	}
	else if(decrementCount(false))
	{
	   cell = document.getElementById(row + "-" + col);
	   displayItemDetails(cell);
	}
	else
	{
	   row = 0;
	   decrementCount(false);
	   cell = document.getElementById(row + "-" + col);
	   displayItemDetails(cell);
	}
}

function displayItemDetails(cell)
{
   if(cell == null) return;

   var className = cell.getAttribute("activeClass");

   if(className == null) return;

   cell.className = className;

  var item_details = document.getElementById("item_Details");
	var item_title = document.getElementById("item_Title");
	var item_time = document.getElementById("item_Time");

	item_details.innerHTML = "<span class='solidTextEPG'>" + cell.getAttribute("desc") + "</span>";
	item_title.innerHTML = "<span class='solidTextEPG'>" + cell.getAttribute("progName") + "</span>";
	item_time.innerHTML = "<span class='solidTextEPG'>" + cell.getAttribute("progTime") + "</span>";

}

function incrementCount(jump)
{
   var temp_col = col;
   col++;

   var cell = document.getElementById(row + "-" + col);

   if(cell != null) return true;

   if(jump)
   {
      setStartingPoint("left");
      document.location.href = nextURL;
      return true;
   }
   else
   {
      col = temp_col;
      return false;
   }
}

function decrementCount(jump)
{
   if(col == 0) // already at the beginning
   {
      if(jump)
      {
         setStartingPoint("right");
         document.location.href = prevURL;
      }

      return false;
   }

   var cell = null;
   var temp_col = col;

   while(cell == null && col > -1)
   {
      col--;
      cell = document.getElementById(row + "-" + col);
   }

   if(cell == null)
   {
      if(jump)
      {
         setStartingPoint("right");
         document.location.href = prevURL;
         return true;
      }

      col = temp_col;
      return false;
   }
   else
      return true;


//   alert(chan + "-" + hour + "-" + pCount);
}

function findSelectedCell()
{
   var curCol = 0
   var buff = 5;
   var found = false;

   var cell = document.getElementById(row + "-" + curCol);

   while(curCol < 50)
   {
      if(cell != null)
      {
   		var cellLeft = cell.offsetLeft;
   		var cellRight = cell.offsetLeft + cell.clientWidth;

   		if(cellLeft > lastCellLeft-buff && cellLeft < lastCellLeft+buff)
   		{
   		   window.status = "01";
   		   col = curCol;
   		   found = true;
   		   break;
   		}
   		if(cellLeft > lastCellLeft && cellRight < lastCellRight)
   		{
   		   window.status = "02";
   		   col = curCol;
   		   found = true;
   		   break;
   		}
   		else if(cellRight > lastCellRight-buff && cellRight < lastCellRight+buff)
   		{
   		   window.status = "03";
   		   col = curCol;
   		   found = true;
   		   break;
   		}
   		else if(cellLeft < lastCellLeft-buff && cellRight > lastCellRight+buff)
   		{
   		   window.status = "04";
   		   col = curCol;
   		   found = true;
   		   break;
   		}
   		else if(cellLeft > lastCellLeft-buff)
   		{
   		   window.status = "05";
   		   col = curCol;
   		   found = true;
   		   break;
   		}
      }
      curCol++;
      cell = document.getElementById(row + "-" + curCol);
   }

   if(!found)
   {
      window.status = "Not Matched";
      col = curCol-1;
   }
}

function addChannel()
{
   var temp_col = col;
   var temp_row = row;

   col = 0;
   row++;

   var cell = document.getElementById(row + "-" + col);

   while(cell == null && row < 160)
   {
      row++;
      cell = document.getElementById(row + "-" + col);
      if(cell != null) break;
   }

   if(cell == null)
   {
      col = temp_col;
      row = temp_row;
   }
   else
      findSelectedCell();

}

function decChannel()
{
   var temp_col = col;
   var temp_row = row;

   col = 0;
   row--;

   var cell = document.getElementById(row + "-" + col);

   while(cell == null && row >= 0)
   {
      row--;
      cell = document.getElementById(row + "-" + col);
      if(cell != null) break;
   }

   if(cell == null)
   {
      col = temp_col;
      row = temp_row;
   }
   else
      findSelectedCell();
}


//
// Cookie stuff
//

function getStartingPoint()
{
   if(readCookie("EPGstartCookie") == "right")
      col = 40;
   else
      col = 0;
}

function setStartingPoint(where)
{
   setCookie("EPGstartCookie", where, 1);
}

function getRowCookie()
{
   row = readCookie("EPGrowCookie");
}

function setRowCookie()
{
   setCookie("EPGrowCookie", row, 1);
}

function setCookie(cookieName, cookieValue, nDays)
{
   var today = new Date();
   var expire = new Date();

   if (nDays==null || nDays==0)
      nDays=1;

   expire.setTime(today.getTime() + 3600000*24*nDays);

   document.cookie = cookieName+"=" + escape(cookieValue) + ";expires="+expire.toGMTString();

}

function readCookie(cookieName)
{
   var theCookie=""+document.cookie;
   var ind=theCookie.indexOf(cookieName);

   if (ind==-1 || cookieName=="")
      return "";

   var ind1=theCookie.indexOf(';',ind);

   if (ind1==-1)
      ind1=theCookie.length;

   return unescape(theCookie.substring(ind+cookieName.length+1,ind1));

}
