var keyboardVisible = false;
var keyboardX = 0;
var keyboardY = 0;
var keyBoardText = "";

var autoDel = 0;
var keepFor = 0;
var channelList = null;
var patternList = null;
var postTaskList = null;

var capTypeNames = null;
var capTypeValues = null;

var capturePathNames = null;
var capturePathValues = null;

var hour = 0;
var min = 0;
var duration = 5;
var channel = 0;
var type = 0;
var captype = 0;
var namePattern = 0;
var capPath = 0;
var postTask = 0;

var typeList = new Array("Once", "Daily", "Weekly", "Monthly", "WeekDay", "EPG");

var inputValues = null;
var inputNames = null;
var inputSelected = 0;

function getCapturePathList()
{
   capturePathNames = new Array();
   capturePathValues = new Array();
   
   var item = document.getElementById("capturePath");
   if(item == null)
      return;
   
   var index = 0;
   var name = item.getAttribute("option_" + index);
   var value = item.getAttribute("value_" + index);
   while(name != null && name != "" && value != null && value != "")
   {
      capturePathNames[index] = name;
      capturePathValues[index] = value;
      
      index++;
      name = item.getAttribute("option_" + index);
      value = item.getAttribute("value_" + index);
   }
}

function getCapturePathValue()
{
   var item = document.getElementById("capturePath");
   if(item == null) return;   
   var value = item.getAttribute("value");
   
   if(value == null && value == "") 
      return;
   
   for(var index = 0; index < capturePathValues.length; index++)
   {
      if(capturePathValues[index] == value)
         return index;
   }
}

function getNameValue()
{
  if (name == null || name == "")
    document.getElementById("name").innerHTML = "N/A";
}

function getTypeValue()
{
   var item = document.getElementById("type");
   if(item == null) return;   
   var value = item.getAttribute("value");
   if(value != null && value != "") type = value;
}

function getCapTypeValue()
{
   var item = document.getElementById("captype");
   if(item == null) return;   
   var value = item.getAttribute("value");
   
   if(value == null && value == "") 
      return;
   
   for(var index = 0; index < capTypeValues.length; index++)
   {
      if(capTypeValues[index] == value)
         return index;
   }
}

function getAutoDelValue()
{
   var item = document.getElementById("autoDel");
   if(item == null) return;   
   var value = item.getAttribute("value");
   if(value != null && value != "") autoDel = value;
}

function getkeepForValue()
{
   var item = document.getElementById("keepfor");
   if(item == null) return;    
   var value = item.getAttribute("value");
   if(value != null && value != "") keepFor = value;
}

function getHourValue()
{
   var item = document.getElementById("hour");
   if(item == null) return;    
   var value = item.getAttribute("value");
   if(value != null && value != "") hour = value;
}

function getMinValue()
{
   var item = document.getElementById("min");
   if(item == null) return;    
   var value = item.getAttribute("value");
   if(value != null && value != "") min = value;
   min = min-(min%5);
}

function getDurationValue()
{
   var item = document.getElementById("duration");
   if(item == null) return;    
   var value = item.getAttribute("value");
   if(value != null && value != "") duration = value;
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

function getPatternArray()
{
   patternList = new Array();
   
   var item = document.getElementById("namePattern");
   if(item == null)
      return;
   
   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      patternList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
}

function getCaptypeList()
{
   capTypeNames = new Array();
   capTypeValues = new Array();
   
   var item = document.getElementById("captype");
   if(item == null)
      return;
   
   var index = 0;
   var name = item.getAttribute("option_" + index);
   var value = item.getAttribute("value_" + index);
   while(name != null && name != "" && value != null && value != "")
   {
      capTypeNames[index] = name;
      capTypeValues[index] = value;
      
      index++;
      name = item.getAttribute("option_" + index);
      value = item.getAttribute("value_" + index);
   }
}

function getPostTaskArray()
{
   postTaskList = new Array();
   
   var item = document.getElementById("postTask");
   if(item == null)
      return;
   
   var index = 0;
   var option = item.getAttribute("option_" + index);
   while(option != null && option != "")
   {
      postTaskList[index] = option;
      index++;
      option = item.getAttribute("option_" + index);
   }
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

function Initialize()
{

   getNameValue();
   getkeepForValue();
   getTypeValue();
   getHourValue();
   getMinValue();
   getDurationValue();
   getAutoDelValue();
   
   getChannelArray();
   channel = getChannelIndex();
   
   getPatternArray();
   namePattern = getPatternIndex();
  
   getPostTaskArray();
   postTask = getPostTaskIndex();
   
   getCaptypeList();
   captype = getCapTypeValue();
   
   getCapturePathList();
   capPath = getCapturePathValue();
   
   inputValues = new Array(name, parseInt(hour), parseInt(min), parseInt(duration), parseInt(channel), parseInt(type), parseInt(captype), parseInt(autoDel), parseInt(keepFor), parseInt(namePattern), parseInt(capPath), parseInt(postTask));
   inputNames = new Array("name", "hour", "min", "duration", "channel", "type", "captype", "autoDel", "keepfor", "namePattern", "capturePath", "postTask");
    
   addEvent(document, "keydown", onKeyDownEvent);

   inputSelected = 1;
   updateAmount(0, 0, 23);
   inputSelected = 2;
   updateAmount(0, 0, 59);
   inputSelected = 3;
   updateAmount(0, 0, 400);
   inputSelected = 4;
   updateChannel(0);
   inputSelected = 5;
   updateType(0);
   inputSelected = 6;
   updateCapType(0);
   inputSelected = 7;
   updateAutoDel(0);
   inputSelected = 8;
   updatekeepFor(0);
   inputSelected = 9;
   updatePattern(0);
   inputSelected = 10;
   updateCapturePath(0);
   inputSelected = 11;
   updatePostTask(0);
   
   inputSelected = 1;
	selectInput();

	document.title = "page ready";
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
            inputValues[0] = keyBoardText;
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
	else if(evt.keyCode == 38)
	{
		unSelectInput();
		inputSelected--;
		if(inputSelected < 0)  inputSelected = inputNames.length-1;
		selectInput();
	}
	else if(evt.keyCode == 39) // right
	{
		if(inputSelected == 1)
			updateAmount(1, 0, 23);
		else if(inputSelected == 2)
			updateAmount(5, 0, 55);
		else if(inputSelected == 3)
			updateAmount(5, 0, 400);
		else if(inputSelected == 4)
			updateChannel(1);
		else if(inputSelected == 5)
			updateType(1);
      else if(inputSelected == 6)
         updateCapType(1);
      else if(inputSelected == 7)
         updateAutoDel(1);  
      else if(inputSelected == 8)   
         updatekeepFor(1);
      else if(inputSelected == 9)
         updatePattern(1);  
      else if(inputSelected == 10)
         updateCapturePath(1);           
      else if(inputSelected == 11)
         updatePostTask(1);      
	}
	else if(evt.keyCode == 37) // left
	{
		if(inputSelected == 1)
			updateAmount(-1, 0, 23);
		else if(inputSelected == 2)
			updateAmount(-5, 0, 55);
		else if(inputSelected == 3)
			updateAmount(-5, 0, 400);
		else if(inputSelected == 4)
			updateChannel(-1);
		else if(inputSelected == 5)
			updateType(-1);
      else if(inputSelected == 6)
         updateCapType(-1); 
      else if(inputSelected == 7)
         updateAutoDel(-1);    
      else if(inputSelected == 8)   
         updatekeepFor(-1);
      else if(inputSelected == 9)
         updatePattern(-1);   
      else if(inputSelected == 10)
         updateCapturePath(-1);            
      else if(inputSelected == 11)
         updatePostTask(-1);                       
	}
	else if(evt.keyCode == 13)
	{
	   
      if(inputSelected == 0)
      {
         if(keyboardVisible == false)
         {

            if (document.getElementById("name").innerHTML == "N/A")
               keyBoardText = "";
            else
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
   	   document.data.name.value = inputValues[0];
   	   document.data.hour.value = inputValues[1];
   	   document.data.min.value = inputValues[2];
   	   document.data.duration.value = inputValues[3];
   	   document.data.channel.value = channelList[inputValues[4]];
   	   document.data.type.value = inputValues[5];
   	   document.data.captype.value = capTypeValues[inputValues[6]];
   	   document.data.month.value = month;
   	   document.data.day.value = day;
   	   document.data.year.value = year;
   	   document.data.autoDel.value = inputValues[7];
   	   document.data.keepfor.value = inputValues[8];
   	   document.data.namePattern.value = patternList[inputValues[9]]
   	   document.data.capPath.value = capturePathValues[inputValues[10]];
   	   document.data.task.value = postTaskList[inputValues[11]];
   
   	   if(id.length > 0)
   	      document.data.id.value = id;
   	      
   	   if(index.length > 0)
   	      document.data.index.value = index;
   	      
   	   document.data.submit();
      }	   

	}
	else if (evt.keyCode == 36 || evt.keyCode == 27) // back
	{
	   document.location.href= "javascript:jumpBack();";
	}

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

function updateAmount(amount, min, max)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < min) inputValues[inputSelected] = min;
		if(inputValues[inputSelected] > max) inputValues[inputSelected] = max;

      cell.innerHTML = "<span class='solidText'>" + intToStr(inputValues[inputSelected]) + "</span>";
		//cell.innerText = intToStr(inputValues[inputSelected]);
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
		//cell.innerText = channelList[inputValues[inputSelected]];
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
		//cell.innerText = typeList[inputValues[inputSelected]];
	}
}

function updateCapturePath(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = capturePathNames.length-1;
		if(inputValues[inputSelected] >= capturePathNames.length) inputValues[inputSelected] = 0;
		
		cell.innerHTML = "<span class='solidText'>" + capturePathNames[inputValues[inputSelected]] + "</span>";
	}
}

function updateCapType(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = capTypeNames.length-1;
		if(inputValues[inputSelected] >= capTypeNames.length) inputValues[inputSelected] = 0;
		
		cell.innerHTML = "<span class='solidText'>" + capTypeNames[inputValues[inputSelected]] + "</span>";
		//cell.innerText = typeList[inputValues[inputSelected]];
	}
}

function updateAutoDel(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = 1;
		if(inputValues[inputSelected] > 1) inputValues[inputSelected] = 0;
		
		if(inputValues[inputSelected] == 0)
		   cell.innerHTML = "<span class='solidText'>False</span>";
		else
		   cell.innerHTML = "<span class='solidText'>True</span>";
	}
}

function updatePattern(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = patternList.length-1;
		if(inputValues[inputSelected] >= patternList.length) inputValues[inputSelected] = 0;
		
		cell.innerHTML = "<span class='solidText'>" + patternList[inputValues[inputSelected]] + "</span>";
	}
}

function updatekeepFor(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = 0;
		if(inputValues[inputSelected] > 60) inputValues[inputSelected] = 60;
		
		cell.innerHTML = "<span class='solidText'>" + inputValues[inputSelected] + " days</span>";
	}
}

function updatePostTask(amount)
{
	var cell = document.getElementById(inputNames[inputSelected]);
	if(cell)
	{
		inputValues[inputSelected] += amount;
		if(inputValues[inputSelected] < 0) inputValues[inputSelected] = postTaskList.length-1;
		if(inputValues[inputSelected] >= postTaskList.length) inputValues[inputSelected] = 0;
		
		cell.innerHTML = "<span class='solidText'>" + postTaskList[inputValues[inputSelected]] + "</span>";
	}
}

function getChannelIndex()
{
   var item = document.getElementById("channel");
   if(item == null) return;

   var value = item.getAttribute("value");   
   
   for(var x = 0; x < channelList.length; x++)
   {
      if(channelList[x] == value)
         return x;
   }
   return 0;
}

function getPatternIndex(pattern)
{
   var item = document.getElementById("namePattern");
   if(item == null) return;

   var value = item.getAttribute("value");   
      
   for(var x = 0; x < patternList.length; x++)
   {
      if(patternList[x] == value)
         return x;
   }
   return 0;
}

function getPostTaskIndex(pattern)
{
   var item = document.getElementById("postTask");
   if(item == null) return;

   var value = item.getAttribute("value");   
      
   for(var x = 0; x < postTaskList.length; x++)
   {
      if(postTaskList[x] == value)
         return x;
   }
   return 0;
}

function intToStr(number)
{
   if(number < 10)
      return "0" + number;
   else
      return number;
}
