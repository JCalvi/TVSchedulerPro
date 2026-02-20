var keyboardVisible = false;
var keyboardX = 0;
var keyboardY = 0;
var keyBoardText = "";

var channelList = null;
var categoryList = null;
var typeList = null;

var inputValues = null;
var inputNames = null;
var inputSelected = 0;

function getTypeArray()
{
   typeList = new Array();
   
   var item = document.getElementById("type");
   if(item == null)
      return;
   
   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      typeList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

function getChannelArray()
{
   channelList = new Array();
   
   var item = document.getElementById("channel");
   if(item == null)
      return;
   
   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      channelList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

function getCategoryArray()
{
   categoryList = new Array();
   
   var item = document.getElementById("category");
   if(item == null)
      return;
   
   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      categoryList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

function Initialize()
{
   getChannelArray();
   getCategoryArray();
   getTypeArray();
   
   inputValues = new Array(0, "", 0, 0, 0);
   inputNames = new Array("back", "name", "type", "channel", "category");
   
   addEvent(document, "keydown", onKeyDownEvent);

   inputSelected = 2;
     updateType(0); 
   inputSelected = 3;
     updateChannel(0);
   inputSelected = 4;
     updateCategory(0);
   
   inputSelected = 0;
   selectInput();
}

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

function keyBoardEvents(key)
{
   if(key == 13) // enter
   {
      var cell = document.getElementById(keyboardX + "-" + keyboardY);
      if(cell)
      {
        var value = cell.getAttribute("value");
        if(value)
        {
          if(value == "ok")
          {
            var keyboard = document.getElementById("keyboard");
            keyboard.style.visibility = "hidden";
            keyboardVisible = false;             
            document.getElementById("name").innerHTML = keyBoardText;
            inputValues[1] = keyBoardText;
            unSelectInput();
            inputSelected = 2;
            selectInput();
          }
          else if(value == "<")
          {
             if(keyBoardText.length > 0)
             {
               keyBoardText = keyBoardText.substring(0, keyBoardText.length-1);
               document.getElementById("keyboardtext").innerHTML = keyBoardText;
             }
          }
          else
          {
            keyBoardText = keyBoardText + value;
            document.getElementById("keyboardtext").innerHTML = keyBoardText;
          }
        }
      }
   }
   else if(key == 40) // down
   {
      setKeyboardSelection(false);
      keyboardY = keyboardY + 1;
      if(keyboardY >= 4)
      {
        keyboardY = 4;
        keyboardX = 0;
      }
      setKeyboardSelection(true);
   }
   else if(key == 38) // up
   {
      setKeyboardSelection(false);
      keyboardY = keyboardY - 1;
      if(keyboardY < 0) keyboardY = 0;
      setKeyboardSelection(true);
   }   
   else if(key == 37) // left
   {
      setKeyboardSelection(false);
      keyboardX = keyboardX - 1;
      if(keyboardX < 0) keyboardX = 0;
      setKeyboardSelection(true);
   }      
   else if(key == 39) // right
   {
      setKeyboardSelection(false);
      keyboardX = keyboardX + 1;
      if(keyboardX > 9) keyboardX = 9;
      setKeyboardSelection(true);
   }
   else if(key == 36 || key == 27)
   {
     var keyboard = document.getElementById("keyboard");
     keyboard.style.visibility = "hidden";
     keyboardVisible = false;
   }
}

function setKeyboardSelection(active)
{
   var cell = document.getElementById(keyboardX + "-" + keyboardY);
   
   if(active && cell)
   {
     cell.className = "FormInputSelected";
   }
   else if(cell)
   {
      cell.className = "FormInput";
   }
}

function setKeyboardPosition()
{
   
   var keyboard = document.getElementById("keyboard");
   if(keyboard == null)
      return;
   
   var w = document.body.clientWidth;
   var h = document.body.clientHeight;

   var popW = keyboard.clientWidth;
   var popH = keyboard.clientHeight;

   var leftPos = (w - popW) / 2;
   var topPos = (h - popH) / 2;
   
   keyboard.style.left = leftPos;
   keyboard.style.top = topPos;
   
}

function onKeyDownEvent(evt)
{
  if(keyboardVisible == true)
  {
     keyBoardEvents(evt.keyCode);
     return;
  }
  
  if(evt.keyCode == 40) // down
  {
    unSelectInput();
    inputSelected++;
    if(inputSelected >= inputNames.length) inputSelected = 0;
    selectInput();
  }
  else if(evt.keyCode == 38)  // up
  {
    unSelectInput();
    inputSelected--;
    if(inputSelected < 0)  inputSelected = inputNames.length-1;
    selectInput();
  }
  else if(evt.keyCode == 39) // right
  {
    if(inputSelected < 1)
      return false;
    else if(inputSelected == 2)
    	updateType(1);
    else if(inputSelected == 3)
      updateChannel(1);
    else if(inputSelected == 4)
      updateCategory(1);

  }
  else if(evt.keyCode == 37) // left
  {
    if(inputSelected < 1)
      return false;
    else if(inputSelected == 2)
    	updateType(-1);      
    else if(inputSelected == 3)
      updateChannel(-1);
    else if(inputSelected == 4)
      updateCategory(-1);      
  }
  else if(evt.keyCode == 13)
  {
    if(inputSelected == 0)
    {
       if(keyboardVisible == false)
       {
         document.location.href= "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons&start=" + start + "&show=" + show;
       }
    }

    else if(inputSelected == 1)
    {
       if(keyboardVisible == false)
       {
         keyBoardText = document.getElementById("name").innerHTML;
         document.getElementById("keyboardtext").innerHTML = keyBoardText;
         
         setKeyboardPosition();
         var keyboard = document.getElementById("keyboard");
         keyboard.style.visibility = "visible";
         keyboardVisible = true;
         setKeyboardSelection(false);
         keyboardX = 0;
         keyboardY = 0;
         setKeyboardSelection(true);
       }
    }
    else
    {
      document.data.name.value = inputValues[1];
      document.data.type.value = typeList[inputValues[2]];      
      document.data.chan.value = channelList[inputValues[3]];
      document.data.cat.value = categoryList[inputValues[4]];
      document.data.start.value = start;
      document.data.show.value = show;
      document.data.submit();
    }
  }
  else if (evt.keyCode == 36 || evt.keyCode == 27) // home and esc
  {
      document.location.href= "/servlet/ApplyTransformRes?xml=root&xsl=kb-buttons&start=" + start + "&show=" + show;
  }
  return false;
}

function unSelectInput()
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		cell.className = "FormInput";
	}
}

function selectInput()
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		cell.className = "FormInputSelected";
	}
}

function updateType(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = typeList.length-1;
    if(inputValues[inputSelected] >= typeList.length) inputValues[inputSelected] = 0;
		
    cell.innerHTML = "<span class='solidText'>" + typeList[inputValues[inputSelected]] + "</span>";
  }
}

function updateChannel(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = channelList.length-1;
    if(inputValues[inputSelected] >= channelList.length) inputValues[inputSelected] = 0;
		
    cell.innerHTML = "<span class='solidText'>" + channelList[inputValues[inputSelected]] + "</span>";
  }
}

function updateCategory(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = categoryList.length-1;
    if(inputValues[inputSelected] >= categoryList.length) inputValues[inputSelected] = 0;
		
    cell.innerHTML = "<span class='solidText'>" + categoryList[inputValues[inputSelected]] + "</span>";
  }
}

function updateMatchType(amount)
{
  var cell = document.getElementById(inputNames[inputSelected]);
  if(cell)
  {
    inputValues[inputSelected] += amount;
    if(inputValues[inputSelected] < 0) inputValues[inputSelected] = matchTypeList.length-1;
    if(inputValues[inputSelected] >= matchTypeList.length) inputValues[inputSelected] = 0;
		
    cell.innerHTML = "<span class='solidText'>" + matchTypeList[inputValues[inputSelected]] + "</span>";
  }
}

