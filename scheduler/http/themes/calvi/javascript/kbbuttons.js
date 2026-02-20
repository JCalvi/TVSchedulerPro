var currentSelection = 0;
var currentSelection2 = 1;
var destination = "";
var cookieID = "";
var confirmVisible = false;
var multiarrow = false;
var intervalID;
var maxItem = 0;
var maxItem2 = 0;

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
function getURLid()
{
   //alert(document.location.href);
   cookieID = document.location.href;
}

function Initialize()
{
  getURLid();
  addEvent(document, "keydown", onKeyDownEvent);
  maxItem = findMaxItem();
  maxItem2 = findMaxItem2();  
	setActiveButton(0);
	setActiveButton2(0);
	document.title = "page ready";
	window.focus();
  //Window Focus Fix - JC.
  intervalID = setTimeout(window.focus, 1000); //Single Refocus
  //intervalID = setInterval(window.focus, 1000); //Constant Refocus
	
	setConfirmPosition();

  if (document.getElementById("itemtable").getAttribute("multiarrow") == "true")
     multiarrow = true;
  //alert(multiarrow);
  
}


function onKeyDownEvent(nKeyCode)
{
      var keyCode = nKeyCode.keyCode;
      //alert(keyCode);

      if  (keyCode == 37) // left
	{
      setActiveButton2(-1);
      if (multiarrow && !confirmVisible) setActiveButton(-1);
	}
	else if	(keyCode == 39) // right
	{
      setActiveButton2(1);
      if (multiarrow && !confirmVisible) setActiveButton(1);
	}
      else if	(keyCode == 40 && !confirmVisible)  // Down
	{
      setActiveButton(1);
      if (multiarrow) setActiveButton2(1);
	}
	else if	(keyCode == 38 && !confirmVisible)  // Up
	{
      setActiveButton(-1);
      if (multiarrow) setActiveButton2(-1);
	}
	else if (keyCode == 13) // enter
	{
	   doAction();
	}
	else if (keyCode == 36 || keyCode == 27)
	{
	   if(back.length > 0)
	   {
	      document.location.href = back;
	   }
	   else
	   {
	      history.back(1);
	      //document.location.href = "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons";
	   }
	}

	return false;
}


function setConfirmPosition()
{
   
   var confirmBox = document.getElementById("confirm");
   if(confirmBox == null)
      return;
   
   var w = document.body.clientWidth;//screen.availWidth;
   var h = document.body.clientHeight;//screen.availHeight;

   var popW = confirmBox.clientWidth;
   var popH = confirmBox.clientHeight;

   var leftPos = (w - popW) / 2;
   var topPos = (h - popH) / 2;
   
   
   confirmBox.style.left = leftPos;
   confirmBox.style.top = topPos;
   
}

function doAction()
{
   
   var butSelected = document.getElementById("item_" + currentSelection);
   var needConfirm = false;
   if(butSelected.getAttribute("confirm") == "true")
      needConfirm = true;
      
   var confirmBox = document.getElementById("confirm");
   var confirmText = document.getElementById("confirmText");
   
   if(needConfirm == true && confirmBox.style.visibility == "hidden")
   {
      confirmVisible = true;
      if(confirmText)
         confirmText.innerHTML = "Please Confirm The Action!";
      confirmBox.style.visibility = "visible";
      return;
   }
   else if(needConfirm == true)
   {
      if(currentSelection2 == 1)
      {
         confirmVisible = false;
         confirmBox.style.visibility = "hidden";
         return;
      }      
   }
   
   if(destination != "")
   {
      if(confirmText)
         confirmText.innerHTML = "Processing, Please Wait...";

      // Add show screen scaling. scshow for scaling, old show for fixed row numbers.
      var show = String(destination.match(/scshow=\d+/));
      var divisor = parseInt(show.match(/\d+/));
      //alert("show="+Math.floor(document.body.clientHeight/divisor));
	    document.location.href = destination.replace("scshow="+divisor,"show="+Math.floor((document.body.clientHeight-220)/divisor));
   } 
}

function setActiveButton2(amount)
{
   var item = document.getElementById("item2_" + currentSelection2);
   if(item)
   {
      item.className = item.getAttribute("defaultClass");
   }

   currentSelection2 += amount;
   if(currentSelection2 < 0) currentSelection2 = maxItem2;
   if(currentSelection2 > maxItem2) currentSelection2 = 0;

   item = document.getElementById("item2_" + currentSelection2);
   item.className = item.getAttribute("activeClass");

}

function setActiveButton(amount)
{
   var item = document.getElementById("item_" + currentSelection);
   if(item)
   {
      item.className = item.getAttribute("defaultClass");
   }

   currentSelection += amount;
   if(currentSelection < 0) currentSelection = maxItem;
   if(currentSelection > maxItem) currentSelection = 0;
   item = document.getElementById("item_" + currentSelection);
   item.className = item.getAttribute("activeClass");
   destination = item.getAttribute("value");

   var itemTable = document.getElementById("itemTable");
   
   if(itemTable)
   {   
      document.body.scrollTop = item.offsetTop - document.body.clientHeight/2 + itemTable.offsetTop + 100;
   }

   var field = document.getElementById("contextline");
   
   if(field)
   {
      field.innerHTML = item.getAttribute("contextline");
   }
         
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

function findMaxItem()
{
   for(var x = 0; x < 1000; x++)
   {
      var item = document.getElementById("item_" + x);
      if(!item)
         return x-1;   
   } 
}

function findMaxItem2()
{
   for(var x = 0; x < 1000; x++)
   {
      var item2 = document.getElementById("item2_" + x);
      if(!item2)
         return x-1;   
   } 
}
