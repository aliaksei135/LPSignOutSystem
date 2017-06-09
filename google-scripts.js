var logSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Log");
var namesSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Names");
var nsoSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Not Signed Out");

//TODO Change this in production runs to the actual sheet
var actSheet = SpreadsheetApp.openById("1LWTb6vNzIN-oJjIeUoSxR0YavDVBfwL2Gj-4lrrrtR0").getSheetByName("School House");

var namesLastRow = namesSheet.getLastRow();
var namesRange = namesSheet.getRange(2, 1, namesLastRow, 2);
var namesValues = namesRange.getValues();

//Resets all names to "signed In" on calling
//This pseudo-synchronises with, but does not interact with the Reset service running local on the client
function resetToSignedIn() {

  for(i = 0; i < namesLastRow - 1; i++){
    //Check if the name is valid
    if(namesValues[i][0] != "#N/A"){
      logSheet.appendRow([namesValues[i][0], "Signed In", "AutoScript"]);
    }else{
      //Continue in loop if name not valid
      continue;
    }
  }
}

//Generate a Not signed out list in the respective sheet
//Adds date and splitter rows to distinguish between days
function parseNotSignedOut() {
  
  var date = new Date();
  
  //Add a header row with the current date
  nsoSheet.appendRow([date])
  
  for(i = 0; i < namesLastRow - 1; i++){
    if(namesValues[i][1] != "Signed Out"){
      if(namesValues[i][1] == "#N/A"){
       continue; 
      }else{
      //Append the name to the NSO Sheet
        nsoSheet.appendRow([namesValues[i][0],]);
      }
                            
    }
                            
  }
  //Separate Days in the NSO list
  nsoSheet.appendRow(["###########Next Day###########"]);
  
}

// Adds activites for each person by name from a source sheet
//Selects name from "names" sheets and iterates over activites sheet to find that name
//Then pulls the current days activity into the names sheet
function populateActivities() {
  
  var actNamesLastRow = actSheet.getLastRow();
  var actNamesRange = actSheet.getRange(3, 1, actSheet.getLastRow(), 1);
  var actNamesValues = actNamesRange.getValues();
  
  var actRange = getTodaysRange();
  var actValues = actRange.getValues();
  
  //Iterate over names list
  for(var i = 1; i < namesLastRow; i++){
    var name = namesValues[i-1][0];
    
    //Search for same name in Activities sheet
    for(var j = 0; j < actNamesLastRow - 1; j++){
      var compName = actNamesValues[j];
      if(name == compName){
        var namesSetRange = namesSheet.getRange(i + 1, 3, 1, 3);
        namesSetRange.setValues([actValues[j]]);
      }
    }
  }
}

//Util function for fetching the current day range
function getTodaysRange() {
  
  var date = new Date();
  var today = date.getDay();
  
  var lastRow = actSheet.getLastRow();
  
  var range;
  
  //0 indexed starting Sunday
    switch(today){
    case 0: //Sunday
      //TODO Handle weekends
    case 1: //Monday
      range = actSheet.getRange(3, 2, lastRow, 3);
        break;
    case 2: //Tuesday
      range = actSheet.getRange(3, 5, lastRow, 3);
        break;
    case 3: //Wednesday
      range = actSheet.getRange(3, 8, lastRow, 3);
        break;
    case 4: //Thursday
      range = actSheet.getRange(3, 11, lastRow, 3);
        break;
    case 5: //Friday
      range = actSheet.getRange(3, 14, lastRow, 3);
        break;
    case 6: //Saturday
      //TODO Handle weekends
  }
        
  return range;
        
}   
   
      
//Event hook for adding tab menu  
function onOpen(){
 
  //Add custom menu entry to simplify running of functions
  var menuEntries = [
    {
    name : "Reset all to Signed In",
    functionName : "resetToSignedIn"
    },{ 
    name : "Refresh Activities",
    functionName : "populateActivities"
    },{
    name : "Get Not Signed Out List",
    functionName : "parseNotSignedOut"
    }];
  
  //Could probably come up with a better name than that...
  SpreadsheetApp.getActiveSpreadsheet().addMenu("LP Actions", menuEntries);
  
  //Scroll Log Sheet to bottom
  logSheet.setActiveCell(logSheet.getDataRange().offset(logSheet.getLastRow()-1, 0, 1, 1));
  
}

// Event hook for sheet edits
function onEdit(e){
  var row = logSheet.getLastRow();
  var range = logSheet.getRange(row, 1, 1, 5);
  var cell = range.getCell(1, 4);
  if(cell.isBlank()){
    addTimestamp(cell);
  }
}

// "Log" sheet edit event hook      
function addTimestamp(cell){
  var stamp = Utilities.formatDate(new Date(), "GMT-0", "HH:mm:ss dd/MM/YY");
  cell.setValue(stamp);
}
