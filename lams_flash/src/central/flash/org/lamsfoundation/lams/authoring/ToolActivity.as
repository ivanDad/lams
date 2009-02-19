﻿/***************************************************************************
 * Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 * =============================================================
 * License Information: http://lamsfoundation.org/licensing/lams/2.0/
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0 
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 * 
 * http://www.gnu.org/licenses/gpl.txt
 * ************************************************************************
 */

import org.lamsfoundation.lams.authoring.*;
import org.lamsfoundation.lams.common.dict.*
import org.lamsfoundation.lams.common.*
import org.lamsfoundation.lams.common.util.*
/*
*
* @author      DC
* @version     0.1
* @comments    Tool Activity Data storage class. 
* @see		   Activity
*/
class ToolActivity extends Activity{

	private var _authoringURL:String;
	private var _monitoringURL:String;
	private var _contributeURL:String;
	private var _helpURL:String;
	
	private var _toolDisplayName:String;
	private var _toolSignature:String;
	
	//generated by the LAMS server, has to do a round trip to populate them
	private var _toolContentID:Number;
	private var _toolID:Number;
	
	private var _extLmsId:String; //external LMS id for tool adapter tools

	//flags to tell UI which to disable
	private var _supportsContribute:Boolean;
	private var _supportsDefineLater:Boolean;
	private var _supportsModeration:Boolean;
	private var _supportsRunOffline:Boolean;
	private var _supportsOutputs:Boolean;
	
	private var _gradebookToolOutputDefinitionName:String;
	
	private var _competenceMappings:Array; // competences to which this activity is mapped
	
	private var _toolOutputDefinitions:Hashtable;
	
	function ToolActivity(activityUIID:Number){
		super(activityUIID);
		
		_objectType = "ToolActivity";
		
		_activityTypeID = TOOL_ACTIVITY_TYPE;
		_toolOutputDefinitions = new Hashtable("_toolOutputDefinitions");
		
		_competenceMappings = new Array();
	}
	
	//TODO: ADD A VALIDATE() FUNCTION
	
	/**
	 * Pass an object with all the fields of a ToolActivity into this function to populate:
	 * <pre><code>
	 * //activity properties:
	 * _activityTypeID = dto.activityTypeID;
	 * _activityID = dto.activityID;
	 * _activityCategoryID = dto.activityCategoryID;
	 * _activityUIID = dto.activityUIID;
	 * _learningLibraryID = dto.learningLibraryID;
	 * _learningDesignID = dto.learningDesignID;
	 * _libraryActivityID = dto.libraryActivityID;
	 * _parentActivityID = dto.parentActivityID;
	 * _parentUIID = dto.parentUIID;
	 * _orderID = dto.orderID;
	 * _groupingID = dto.groupingID;
	 * _groupingUIID = dto.groupingUIID;
	 * _title = dto.title;
	 * _description = dto.description;
	 * _helpText =  dto.helpText;
	 * _yCoord = dto.yCoord;
	 * _xCoord = dto.xCoord;
	 * _libraryActivityUIImage = dto.libraryActivityUIImage;
	 * _applyGrouping = dto.applyGrouping;
	 * _runOffline = dto.runOffline;
	 * //now removed
	 * //_offlineInstructions = dto.offlineInstructions;
	 * //_onlineInstructions = dto.onlineInstructions;
	 * _defineLater = dto.defineLater;
	 * _createDateTime = dto.createDateTime;
	 * _groupingSupportType = dto.groupingSupportType;
	 * 
	 * //Toolactivity class props
	 * _authoringURL = dto.authoringURL;
	 * _toolDisplayName = dto.toolDisplayName;
	 * _toolContentID = dto.toolContentID;
	 * _toolID = dto.toolID;
	 * _supportsContribute = dto.supportsContribute;
	 * _supportsDefineLater = dto.supportsDefineLater;
	 * _supportsModeration = dto.supportsRunOffline;
	 * </code></pre>
	 * @usage   
	 * @param   dto Object containing all ToolActivity fields:
	 * @return  Noting
	 */
	public function populateFromDTO(dto:Object):Void{
			
			//first do the super method for activity props
			super.populateFromDTO(dto);
			
			//Tool Activity class props
			if(StringUtils.isWDDXNull(dto.authoringURL)) { _authoringURL = null }
			else { _authoringURL = dto.authoringURL; }
			
			if(StringUtils.isWDDXNull(dto.helpURL)) { _helpURL = null }
			else { _helpURL = dto.helpURL; }
			
			if(StringUtils.isWDDXNull(dto.toolDisplayName)) { _toolDisplayName = null }
			else { _toolDisplayName = dto.toolDisplayName; }
			
			if(StringUtils.isWDDXNull(dto.toolSignature)) { _toolSignature = null }
			else { _toolSignature = dto.toolSignature; }
			
			if(StringUtils.isWDDXNull(dto.toolContentID)) { _toolContentID = null }
			else { _toolContentID = dto.toolContentID; }
			
			if(StringUtils.isWDDXNull(dto.toolID)) { _toolID = null }
			else { _toolID = dto.toolID; }
			
			if(StringUtils.isWDDXNull(dto.extLmsId)) { _extLmsId = null }
			else { _extLmsId = dto.extLmsId; }
			 
			if(StringUtils.isWDDXNull(dto.gradebookToolOutputDefinitionName)) { _gradebookToolOutputDefinitionName = null }
			else { _gradebookToolOutputDefinitionName = dto.gradebookToolOutputDefinitionName; }
			
			//TODO: Have the backend send this to flash for all tool activities in the design on startup
			if(!StringUtils.isWDDXNull(dto.toolOutputDefinitions)) {
				for (var i=0; i<dto.toolOutputDefinitions.length; i++) {
					addDefinition(dto.toolOutputDefinitions[i]);
				}
			}
			
			_competenceMappings = new Array();
			if(!StringUtils.isWDDXNull(dto.competenceMappingTitles)) {
				for (var i=0; i<dto.competenceMappingTitles.length; i++) {
					_competenceMappings.push(dto.competenceMappingTitles[i]);
				}
			}
			
			_supportsContribute = dto.supportsContribute;
			_supportsDefineLater = dto.supportsDefineLater;
			_supportsModeration = dto.supportsRunOffline
			_supportsOutputs = dto.supportsOutputs;
			
			activityToolContentID = _toolContentID;
	}
	
	//to data for serialising:
	
	public function toData():Object{
		var dto = super.toData();
		
		dto.authoringURL = (_authoringURL) ?  _authoringURL : Config.STRING_NULL_VALUE;	
		dto.helpURL = (_helpURL) ? _helpURL : Config.STRING_NULL_VALUE;
		dto.toolDisplayName = (_toolDisplayName) ?  _toolDisplayName: Config.STRING_NULL_VALUE;	
		dto.toolSignature = (_toolSignature) ? _toolSignature: Config.STRING_NULL_VALUE;
		
		dto.toolContentID = (_toolContentID) ?  _toolContentID: Config.NUMERIC_NULL_VALUE;	
		dto.toolID = (_toolID) ?  _toolID: Config.NUMERIC_NULL_VALUE;	
		
		dto.extLmsId = (_extLmsId) ? _extLmsId : Config.STRING_NULL_VALUE;
		
		dto.gradebookToolOutputDefinitionName = (_gradebookToolOutputDefinitionName) ? _gradebookToolOutputDefinitionName : Config.STRING_NULL_VALUE;
		
		dto.competenceMappings = new Array();
		if (_competenceMappings.length > 0) {
			for (var i=0; i<_competenceMappings.length; i++) {
				dto.competenceMappings.push(_competenceMappings[i]);
			}
		}
		
		/* THESE are internal flags, not part of the design
		dto.supportsContribute = (_supportsContribute!=null) ?  _supportsContribute: Config.BOOLEAN_NULL_VALUE;	
		dto.supportsDefineLater = (_supportsDefineLater!=null) ?  _supportsDefineLater: Config.BOOLEAN_NULL_VALUE;	
		dto.supportsModeration = (_supportsModeration!=null) ?  _supportsModeration: Config.BOOLEAN_NULL_VALUE;	
		dto.supportsRunOffline = (_supportsRunOffline!=null) ?  _supportsRunOffline: Config.BOOLEAN_NULL_VALUE;	
		*/
		
		return dto;
	}
	
	public function clone():ToolActivity{
		//var n:Activity = super.clone();
		
		var n:ToolActivity = new ToolActivity(null);
		
		//parents properties:
		n.objectType = _objectType;
		
		n.activityTypeID = _activityTypeID;
		n.activityID = _activityID;
		n.activityCategoryID = _activityCategoryID;
		n.activityUIID = _activityUIID;
		n.learningLibraryID = _learningLibraryID;
		n.learningDesignID = _learningDesignID;
		n.libraryActivityID = _libraryActivityID;
		n.parentActivityID = _parentActivityID;
		n.parentUIID = _parentUIID
		n.orderID = _orderID
		n.groupingID = _groupingID;
		n.groupingUIID = _groupingUIID
		n.title = _title;
		n.description = _description;
		n.helpText =  _helpText;
		n.yCoord = _yCoord;
		n.xCoord = _xCoord;
		n.libraryActivityUIImage = _libraryActivityUIImage;
		n.applyGrouping = _applyGrouping;
		n.runOffline = _runOffline;
		n.defineLater = _defineLater;
		n.createDateTime = _createDateTime;
		n.groupingSupportType = _groupingSupportType;

		//class props
		n.authoringURL = _authoringURL;
		n.helpURL = _helpURL;
		n.toolDisplayName = _toolDisplayName;
		n.toolSignature = _toolSignature;
		n.toolContentID = _toolContentID;
		n.toolID = _toolID;
		
		n.supportsContribute = _supportsContribute;
		n.supportsDefineLater = _supportsDefineLater;
		n.supportsModeration = _supportsRunOffline;
		n.supportsOutputs = _supportsOutputs;
		n.extLmsId = _extLmsId;
		
		n.gradebookToolOutputDefinitionName = _gradebookToolOutputDefinitionName;
		
		var numDefinitions:Number = definitions.length;
		if (numDefinitions > 0) {
			for (var i=0; i<numDefinitions; i++) {
				n.addDefinition(definitions[i]);
			}
		}
		
		n.competenceMappings = new Array();
		
		if (competenceMappings.length > 0) {
			for (var i=0; i<competenceMappings.length; i++) {
				n.competenceMappings.push(competenceMappings[i]);
			}
		}
		
		return n;
	}
	
	public function addDefinition(dto:Object):Void {
		if (!_toolOutputDefinitions.containsKey(dto.name)) {
			_toolOutputDefinitions.put(dto.name, dto);
		}
	}
	
	public function removeDefinition(key:String):Void {
		_toolOutputDefinitions.remove(key);
	}
	
	public function get definitions():Array {
		return _toolOutputDefinitions.values();
	}
	
	public function get competenceMappings():Array {
		return _competenceMappings	
	}
	
	//GETTERS + SETTERS
	
	/**
	 * 
	 * @usage   
	 * @param   newauthoringurl 
	 * @return  
	 */
	public function set authoringURL (newauthoringurl:String):Void {
		_authoringURL = newauthoringurl;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get authoringURL ():String {
		return _authoringURL;
	}
	
	public function set competenceMappings(cMappings:Array) {
		_competenceMappings = cMappings;
	}

	/**
	 * 
	 * @usage   
	 * @param   newtoolDisplayName 
	 * @return  
	 */
	public function set toolDisplayName (newtoolDisplayName:String):Void {
		_toolDisplayName = newtoolDisplayName;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get toolDisplayName ():String {
		return _toolDisplayName;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newtoolSignature 
	 * @return  
	 */
	public function set toolSignature (newtoolSignature:String):Void {
		_toolSignature = newtoolSignature;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get toolSignature ():String {
		return _toolSignature;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newtoolContentID 
	 * @return  
	 */
	public function set toolContentID (newtoolContentID:Number):Void {
		_toolContentID = newtoolContentID;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get toolContentID ():Number {
		return _toolContentID;
	}

	/**
	 * 
	 * @usage   
	 * @param   newtoolID 
	 * @return  
	 */
	public function set toolID (newtoolID:Number):Void {
		_toolID = newtoolID;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get toolID ():Number {
		return _toolID;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newsupportsContribute 
	 * @return  
	 */
	public function set supportsContribute (newsupportsContribute:Boolean):Void {
		_supportsContribute = newsupportsContribute;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get supportsContribute ():Boolean {
		return _supportsContribute;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newsupportsDefineLater 
	 * @return  
	 */
	public function set supportsDefineLater (newsupportsDefineLater:Boolean):Void {
		_supportsDefineLater = newsupportsDefineLater;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get supportsDefineLater ():Boolean {
		return _supportsDefineLater;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newsupportsModeration 
	 * @return  
	 */
	public function set supportsModeration (newsupportsModeration:Boolean):Void {
		_supportsModeration = newsupportsModeration;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get supportsModeration ():Boolean {
		return _supportsModeration;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newsupportsRunOffline 
	 * @return  
	 */
	public function set supportsRunOffline (newsupportsRunOffline:Boolean):Void {
		_supportsRunOffline = newsupportsRunOffline;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get supportsRunOffline ():Boolean {
		return _supportsRunOffline;
	}

	/**
	 * 
	 * @usage   
	 * @param   newsupportsRunOffline 
	 * @return  
	 */
	public function set supportsOutputs (newsupportsOutputs:Boolean):Void {
		_supportsOutputs = newsupportsOutputs;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get supportsOutputs ():Boolean {
		return _supportsOutputs;
	}

	/**
	 * 
	 * @usage   
	 * @param   newmonitoringUrl 
	 * @return  
	 */
	public function set monitoringUrl (newmonitoringUrl:String):Void {
		_monitoringURL = newmonitoringUrl;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get monitoringUrl ():String {
		return _monitoringURL;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   newcontributeUrl 
	 * @return  
	 */
	public function set contributeUrl (newcontributeUrl:String):Void {
		_contributeURL = newcontributeUrl;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get contributeUrl ():String {
		return _contributeURL;
	}
	
		
	/**
	 * 
	 * @usage   
	 * @param   newhelpurl 
	 * @return  
	 */
	public function set helpURL (newhelpurl:String):Void {
		_helpURL = newhelpurl;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get helpURL ():String {
		return _helpURL;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   str 
	 * @return  
	 */
	public function set extLmsId (str:String):Void {
		_extLmsId = str;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get extLmsId ():String {
		return _extLmsId;
	}
	
	/**
	 * 
	 * @usage   
	 * @param   n the name of the tooloutput 
	 * @return  
	 */
	public function set gradebookToolOutputDefinitionName (n:String):Void {
		_gradebookToolOutputDefinitionName = n;
	}
	/**
	 * 
	 * @usage   
	 * @return  
	 */
	public function get gradebookToolOutputDefinitionName ():String {
		return _gradebookToolOutputDefinitionName;
	}	
}

