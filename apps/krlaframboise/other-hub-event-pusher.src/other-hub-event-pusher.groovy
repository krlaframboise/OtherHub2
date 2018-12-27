/**
 *  HUBITAT: Other Hub Event Pusher v2.2.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
 *
 *    2.2.0 (12/27/2018) Arn Burkhoff
 *			- Added: Execute Smarthings routine when mode changes
 *			- Bug fix: debug logging not longer logs when shut off  
 *
 *    2.1.1 (10/21/2018)
 *			- Bug fix
 *
 *    2.1 (10/20/2018)
 *			- Added PushableButton
 *
 *    2.0.2 (03/25/2018)
 *			- Added delay to real-time integration.
 *
 *    2.0.1 (02/27/2018)
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
definition(
    name: "Other Hub Event Pusher",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "Pushes events to external url",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	page(name: "main")
	page(name: "displayHubitatUrlPage")
	page(name: "refreshHubitatUrlPage")
}

def main(){
	return (
		dynamicPage(name: "main", title: "", uninstall: true, install: true){			
			section("<h2>SmartThings Other Hub Device Viewer Dashboard URL</h2>") {
				paragraph "If you open the Dashboard in the SmartThings SmartApp Other Hub Device Viewer Dashboard, the url needed for the field below is shown in the textbox at the bottom of the page."
				paragraph "The URL is also written to Live Logging when you open the Dashboard Settings"				
				input "smartThingsUrl", "text", title:"Enter the SmartThings Dashboard URL:", required: true
			}
			
			section("<h2>Select Integrated Devices</h2>") {
				paragraph "A device may appear in multiple lists below, but you only have to select each device once.",title: "Display which devices in SmartThings?"
									
				supportedCapabilities.each {				
					input "${it.prefType}Devices",
						"capability.${it.prefType}", 
						title: "${it.name} Devices:", 
						hideWhenEmpty: true,
						submitOnChange: true,
						required: false, 
						multiple: true
				}				
			}

			section("<h2>Select Modes</h2>") 
				{
				paragraph "Execute Smarthings Routines on Hubitat Mode Change (case and punctuation sensitive).",title: "Set routines in SmartThings?"
			    location.modes.each()
			    	{
					input "mode${it}",
						"text",
						title: "Execute this routine in Smarthings for ${it}:", 
						required: false, 
						multiple: false
					}				
				}
			
			section("<h2>Scheduled Integration</h2>") {
				input "refreshInterval", "enum",
					title: "How oftens hould the device data get refreshed?",
					defaultValue: "Disabled",
					required: false,
					displayDuringSetup: true,
					options: refreshIntervalOptions.collect { it.name }
			}
			
			section("<h2>Real-time Integration</h2>") {
				input "subscribedAttributes", "enum", 
						title: "Push these events to SmartThings as they happen:", 
						required: false, 
						multiple: true,
						options: supportedAttributes
				
				input "eventPushEnabled", "bool", 
					title: "Real-time Integration Enabled?", 
					defaultValue: true, 
					required: false
				input "delayedPushEnabled", "bool", 
					title: "Delayed Push Enabled?",
					defaultValue: true, 
					required: false
				paragraph "<em><strong>WARNING!</strong>You should keep 'Delayed Push' enabled.  Disabling it makes the Other Hub devices in SmartThings update faster, but it can cause your Hubitat devices and Apps to execute slower.</em>"
			}
			
			section("<h2>Hubitat Url</h2>") {
				href "displayHubitatUrlPage", title: "Send Hubitat Url to SmartThings", description: "You might need to click this if SmartThings is unable to send commands back to Hubitat."
				href "refreshHubitatUrlPage", title: "Refresh Hubitat Url", description: "Disables the existing Hubitat Url, generates a new url, and sends it to SmartThings."
			}
									
			section("<h2>Other Options</h2>") {
				label(name: "label",
						title: "Assign a name to this SmartApp",
						required: false)
				input "debugLogging", "bool", 
					title: "Enable debug logging?", 
					defaultValue: true, 
					required: false
			}
		}
	)
}

private displayHubitatUrlPage() {
	dynamicPage(name: "displayHubitatUrlPage", title: "") {
		section() {
		
			postHubitatUrlToSmartThings()
		
			paragraph "The Hubitat Url has been displayed in the Logs"
			log.info "Hubitat Url: ${hubitatUrl}"
		}
	}
}

private refreshHubitatUrlPage() {	
	dynamicPage(name: "refreshHubitatUrlPage", title: "") {
		section() {
			disableAppEndpoint()
			paragraph "The old Hubitat url has been disabled!"	
			
			if (initializeAppEndpoint()) {
				paragraph "A new Hubitat url has been created and sent to SmartThings!"				
			} 
			else {
				paragraph "Please go to the Apps Code section of Hubitat, click 'Other Hub Event Pusher', Click OAuth, and then Enable.", title: "Please enable OAuth for Other Hub Event Pusher", required: true, state: null
			}	
			
			log.info "Hubitat Url: ${hubitatUrl}"
		}
	}
}

private disableAppEndpoint() {
	if (state.accessToken) {
		try {
			revokeAccessToken()
		}
		catch (e) {
			logDebug "Unable to revoke access token: $e"
		}
		state.accessToken = null
	}	
}

private initializeAppEndpoint() {	
	if (!state.accessToken) {
		try {			
			state.accessToken = createAccessToken()
			postHubitatUrlToSmartThings()
		} 
		catch(e) {
			log.error "$e"
			state.accessToken = null
		}		
	}
	return state.accessToken
}


def uninstalled() {
	logDebug "uninstalled()"
	disableAppEndpoint()
}

def installed() {
	logDebug "installed()..."
	updated()
}

def updated() {
	logDebug "updated()..."

	unsubscribe()
	unschedule()
	initialize()	
}

def initialize() {
	logDebug "initialize()"
	initializeAppEndpoint()
	subscribeToPushEvents()
	scheduleDeviceRefresh()
	if(eventPushEnabled)
		subscribe (location, "mode", handleModeChange)

}

private subscribeToPushEvents() {
	if (eventPushEnabled) {	
		def devices = allDevices
		
		supportedCapabilities.each {		
			if (settings?.subscribedAttributes && it.attributeName in settings?.subscribedAttributes) {
				devices?.each { device ->
					if (device.hasAttribute("${it.attributeName}")) {
						subscribe(device, "${it.attributeName}", handleDeviceEvent)
					}
				}
			}
		}		
	}
	else {
		log.warn "Event Push is Disabled."
	}
}

private scheduleDeviceRefresh() {
	refreshDevices()
	switch (refreshIntervalSettingMinutes) {
		case 0:
			log.warn "Auto Refresh Disabled"
			break
		case 5:
			runEvery5Minutes(refreshDevices)
			break
		case 10:
			runEvery10Minutes(refreshDevices)
			break
		case 15:
			runEvery15Minutes(refreshDevices)
			break
		case 30:
			runEvery30Minutes(refreshDevices)
			break
		case [60, 120]:
			runEvery1Hour(refreshDevices)
			break
		default:
			runEvery3Hours(refreshDevices)
	}
}

def refreshDevices() {
	def deviceData = []
	
	allDevices?.each {
		deviceData << getDeviceData(it)		
	}
	
	if (deviceData) {
		logDebug "Sending ${deviceData?.size()} Devices to SmartThings"		
		postDeviceDataToSmartThings(deviceData)
	}	
}

private postHubitatUrlToSmartThings() {	
	def data = [url: "${hubitatUrl}".replace("https://", "")]
	postDataToSmartThings("update-other-hub-url", data)	
}

private postDeviceDataToSmartThings(data) {
	postDataToSmartThings("refresh-devices", data)
}

private postDataToSmartThings(path, data) {
	def requestParams = [
		uri:  "${smartThingsUri}${smartThingsRelativePath}/${path}",
		query: null,
		requestContentType: "application/json",
		body: data
	]
    
	httpPost(requestParams) { response ->
		def msg = ""
		if (response?.status == 200) {
			msg = "Success"
		}
		else {
			msg = "${response?.status}"
		}
		logDebug "SmartThings Response: ${msg} (${response.data})"
	}
}


private getDeviceData(device) {
	[
		id: device.deviceNetworkId,
		name: device.name,
		label: device.label ?: device.name,
		lastActivity: getDeviceLastActivity(device),
		attributes: getDeviceAttributes(device)
	]
}

private getDeviceAttributes(device) {
	def attributes = [:]
	supportedAttributes.each {	
		if (device.hasAttribute(it)) {		
			if (device.currentValue("$it") != null) {
				attributes["$it"] = device.currentValue("$it")
			}
		}
	}
	return attributes
}

private getDeviceLastActivity(device) {
	def attrs = device?.supportedAttributes?.collect { 
		device.currentState("$it")?.date?.time		
	}
	if (attrs) {
		return attrs.sort().last()
	}
	else {
		return 0
	}	
}

private getAllDevices() {
	def devices = []
	supportedCapabilities.each {
		devices += getDevicesByCapability(it)
	}		
	return devices?.unique{ it.deviceNetworkId }
}

private getDevicesByCapability(capability) {
	if (settings && settings["${capability.prefType}Devices"]) {
		return settings["${capability.prefType}Devices"]
	}
	else {
		return []
	}
}

private getSupportedAttributes() {
	def attrs = []	
	supportedCapabilities?.each { 
		if (it.attributeName) {
			attrs << it.attributeName
		}
	}
	return attrs.unique()?.sort()
}

private getSupportedCapabilities() {
	[	
		[
			name: "Actuator",
			prefType: "actuator",
			attributeName: ""
		],
		[
			name: "Sensor",
			prefType: "sensor",
			attributeName: ""
		],
		[
			name: "Acceleration Sensor",
			prefType: "accelerationSensor",
			attributeName: "acceleration"
		],
		[
			name: "Alarm",
			prefType: "alarm",
			attributeName: "alarm"
		],
		[
			name: "Battery",
			prefType: "battery",
			attributeName: "battery"
		],
		[
			name: "Carbon Monoxide Detector",
			prefType: "carbonMonoxideDetector",
			attributeName: "carbonMonoxide"
		],
		[
			name: "Contact Sensor",
			prefType: "contactSensor",
			attributeName: "contact"
		],
		[
			name: "Energy Meter",
			prefType: "energyMeter",
			attributeName: "energy"
		],
		[
			name: "Illuminance Measurement",
			prefType: "illuminanceMeasurement",
			attributeName: "illuminance"
		],
		[
			name: "Lock",
			prefType: "lock",
			attributeName: "lock"
		],		
		[
			name: "Motion Sensor", 
			prefType: "motionSensor",
			attributeName: "motion"
		],
		[
			name: "Power Meter",
			prefType: "powerMeter",
			attributeName: "power"
		],
		[
			name: "Presence Sensor",
			prefType: "presenceSensor",
			attributeName: "presence"
		],
		[
			name: "PushableButton",
			prefType: "pushableButton",
			attributeName: "pushed"
		],
		[
			name: "Relative Humidity Measurement",	
			prefType: "relativeHumidityMeasurement",
			attributeName: "humidity"
		],
		[
			name: "Smoke Detector",
			prefType: "smokeDetector",
			attributeName: "smoke"
		],
		[
			name: "Switch",
			prefType: "switch",
			attributeName: "switch"
		],
		[
			name: "Switch Level",
			prefType: "switchLevel",
			attributeName: "level"
		],
		[
			name: "Temperature Measurement",
			prefType: "temperatureMeasurement",
			attributeName: "temperature"
		],
		[
			name: "Valve",
			prefType: "valve",
			attributeName: "valve"
		],
		[
			name: "Voltage Measurement",
			prefType: "voltageMeasurement",
			attributeName: "voltage"
		],
		[
			name: "Water Sensor",
			prefType: "waterSensor",
			attributeName: "water"
		]
	]
}

def handleDeviceEvent(evt) {
	if (evt) {
		def data = [name:evt.name, value:evt.value, deviceName: evt.device?.displayName, deviceDni: evt.device?.deviceNetworkId]
		
		if (settings?.delayedPushEnabled != false) {
			logTrace "Scheduling pushEvent(${data})"
			runIn(1, pushEvent, [overwrite: false, data: data])
		}
		else {
			pushEvent(data)
		}
	}
}

def handleModeChange(evt)
	{
	logDebug "handleModeChange entered Name: ${evt.name} Value:${evt.value} Source:${evt?.source} Device:${evt?.device} Event: ${evt}"
	def	theModeName='mode'+evt.value
	def passing=""
	def b64v
	def b64d
	if ("mode${evt.value}")
		{
		passing=settings."mode${evt.value}"
		if (passing > "")
			{
			b64v=URLEncoder.encode(passing, "UTF-8");				//url encode due to spaces and punctuation
			b64d=URLEncoder.encode(evt.value, "UTF-8");				//url encode due to spaces and punctuation	
			logDebug "Mode setting: ${theModeName} pushing routine ${passing} to Smartthings as ${b64v} ${b64d}"
/*				name & deviceName is evt.name, should always be "mode"
				value is the user routine to execute, url encoded
				deviceDNI is original HE mode, url encoded
*/			def data = [name:evt.name, value:b64v, deviceName: evt.name, deviceDni: b64d]
			pushEvent(data)
			}
		else
			logDebug "Mode setting for ${theModeName} not sent, it is null"
		}
	else
		log.error "Other Hub Event Pusher: mode setting not found for ${evt.value}, adjust mode data, then save app settings"
	}	

def pushEvent(data) {
	// logDebug "pushEvent(${data})"
			
	def uri = smartThingsUri
	def path = "${smartThingsRelativePath}/event/${data.name}/${data.value}/${data.deviceDni}"
	
	if (uri) {
		def params = [
			uri: "${uri}",
			path: "${path}"
		]
		
		def msg = "Pushing ${data.name} ${data.value} to ${data.deviceName}(${data.deviceDni})"
		try {
			httpGet(params) { resp ->
				logDebug "${msg} Response: ${resp?.status}"
			}
		} catch (e) {
			log.error "${msg} Error: $e"
		}
	}	
	else {
		log.warn "${smartThingsUrl} is not a valid SmartThings Url."
	}
}


private getSmartThingsUri() {
	return urlRemoveAfterAtText(smartThingsUrl, "/api/token/")
}

private getSmartThingsRelativePath() {
	return urlRemoveAfterAtText(smartThingsUrl?.replace(smartThingsUri, ""), "/dashboard/")
}

private urlRemoveAfterAtText(url, text) {
	def i = url?.indexOf("$text")
  if (i && i > 1) {
		def removed = url.substring(i)
		return url.replace("$removed", "")
  }
	else {
		return url
	}
}


private getRefreshIntervalSettingMinutes() {
	return convertOptionSettingToInt(refreshIntervalOptions, refreshIntervalSetting)
}

private getRefreshIntervalSetting() {
	return settings?.refreshInterval ?: "Disabled"
}

private getRefreshIntervalOptions() {
	[
		[name: "Disabled", value: 0],
		[name: "5 Minutes", value: 5],
		[name: "10 Minutes", value: 10],
		[name: "15 Minutes", value: 15],
		[name: "30 Minutes", value: 30],
		[name: "1 Hour", value: 60],
		[name: "2 Hours", value: 120],
		[name: "3 Hours", value: 180],
		[name: "6 Hours", value: 360],
		[name: "9 Hours", value: 540],
		[name: "12 Hours", value: 720],
		[name: "18 Hours", value: 1080],
		[name: "24 Hours", value: 1440]
	]
}

private convertOptionSettingToInt(options, settingVal) {
	return safeToInt(options?.find { "${settingVal}" == "${it.name}" }?.value, 0)
}

private safeToInt(val, defaultVal=-1) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private getHubitatUrl() {
	def path = ""
	return "${fullApiServerUrl(path)}?access_token=${state.accessToken}"
}

mappings {
	path("/action/:cmd/:dni") {action: [GET: "api_action"]}
	path("/action/:cmd/:dni/:arg") {action: [GET: "api_action"]}	
}

private api_action() {
	logDebug "api_action()"
	def responseMsg = ""
	
	def dni = "${params?.dni}"
	def cmd = "${params?.cmd}"
	def arg = params?.arg ?: ""
	
	if (dni && cmd) {
		def device = findDeviceByDNI(dni)
		if (device) {		
			
			if (device?.hasCommand(cmd)) {
				responseMsg = "Executed ${cmd}(${arg}) on ${device.displayName}"
				try {
					if (arg) {
						if ("${cmd}" == "setLevel") {
							device.setLevel(safeToInt(arg))
						}
						else {
							device."${cmd}"(arg)
						}
					}
					else {
						device."${cmd}"()
					}
				}
				catch (e) {
					responseMsg = "$e"
				}
			}
			else {
				responseMsg = "${device.displayName}: '${cmd}' Not Supported"
			}
			
			if ("$cmd" == "refresh") {
				responseMsg = "${responseMsg}.  Refreshing ${device.displayName}"
				postDeviceDataToSmartThings([getDeviceData(device)])
			}
		}
		else {
			responseMsg = "Device '${dni}' Not Found"
		}
	}
	else {
		responseMsg = "Invalid Parameters"
	}
	
	logDebug "${responseMsg}"
	
	render contentType: "text/html", 
		data: "${responseMsg}"
}

private findDeviceByDNI(dni) {
	return allDevices?.find { "${it.deviceNetworkId}" == "$dni" }
}


private logDebug(msg)
	{
	if (debugLogging)
		log.debug "$msg"
	}

private logTrace(msg) {
	// log.trace "$msg"
}
