/**
 *  SmartThings: Other Hub Alexa Trigger v2.1
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
 *
 *    2.1 (10/20/2018)
 *			- Initial Release
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "Other Hub Alexa Trigger", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Actuator"
		capability "Refresh"
		capability "Switch Level"
						
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}

	tiles (scale:2) {
		
		standardTile("info", "generic", height:2, width:4) {
			state "default", label:'Set Switch Level to Activate Corresponding Alexa Trigger #'
		}
		controlTile("slider", "device.level", "slider",	height: 2, width: 2) {
			state "level", action:"switch level.setLevel"
		}
		
		childDeviceTiles("deviceList")		
	}
	
	preferences() {
		input "numberOfTriggers", "enum",
			title:"Number of Alexa Triggers?",
			defaultValue: "1",
			options: numberOfTriggersOptions
	}
}

private getNumberOfTriggersOptions() {
	def options = []
	(1..100).each {		
		options << "${it}"
	}
	return options
}

private getNumberOfTriggersSetting() {
	return safeToInt(settings?.numberOfTriggers)
}


def installed() {
	sendEvent(name:"level", value:0, displayed:false)
}


def updated() {
	if (device.currentValue("level") == null) {
		sendEvent(name:"level", value:0, displayed:false)
	}
	if (childDevices?.size() < numberOfTriggersSetting) {
		runIn(2, createChildDevices)
	}	
}

def createChildDevices() {
	for (int x = 1; x <= numberOfTriggersSetting; x += 1) {
		if (!findChild(x)) {
			addChild(x)
		}
	}
}

private addChild(num) {
	def name = "Alexa Trigger ${num}"
	logDebug "Creating ${name} Child Device"

	def child = addChildDevice(
			"krlaframboise",
			"Other Hub Motion Sensor",
			getChildDNI(num), 
			null, 
			[
				completedSetup: true,
				isComponent: true,
				label: name,
				componentName: name,
				componentLabel: name
			]
		)	
	
	if (child) {
		sendEvent(name:"temperature", value:68, unit:"F", displayed:false)
		sendMotionEvent(num, "inactive")	
	}
}


void refresh() {	
	executeCmd("refresh")
}

private executeCmd(cmd, args="") {
	parent.childAction("${device.deviceNetworkId}", "$cmd", "$args")
}


void setLevel(level, duration=null) {
	if (level) {
		logDebug "setLevel(${level})"
		
		sendMotionEvent(level, "active")
		
		runIn(2, resetMotion,[data:[num:level]])	
	}
}

def resetMotion(data) {
	sendMotionEvent(data?.num, "inactive")
}

private sendMotionEvent(num, value) {
	def child = findChild(num)
	if (child) {	
		child.sendEvent(name:"motion", value:value, displayed:true, isStateChange:true)
	}
	else {
		logDebug "Alexa Trigger ${num} Not Found"
	}
}


private findChild(num) {
	childDevices?.find {
		"${it.deviceNetworkId}" == "${getChildDNI(num)}"
	}	
}

private getChildDNI(num) {
	return device.deviceNetworkId + "--${num}"
}

private safeToInt(value, defaultValue=0) {
	return "${value}".isInteger() ? "${value}".toInteger() : defaultValue
}


private logDebug(msg) {
	log.debug "${msg}"
}