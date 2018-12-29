/**
 *  SmartThings: Other Hub Device Viewer
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
 *	
 *    2.2.2 (12/28/2018) Arn Burkhoff
 *			- Added: Dynamic push to Hubitat of Routine Names 
 *
 *    2.2.1 (12/28/2018) Arn Burkhoff
 *			- Added: Push routine names from SmartThings for use with HE mode change logic into IDE log 
 *			- Added: Dynamic version number on description (cant use it on module name)
 *
 *    2.2.0 (12/27/2018) Arn Burkhoff
 *			- Added support for HE mode change, executes a ST routine
 *
 *    2.1.1 (10/26/2018)
 *			- Fix for button device creation.
 *
 *    2.1 (10/20/2018)
 *			- Added support for Alexa Triggers
 *
 *    2.0.1 (02/27/2018)
 *			- Initial Release
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of
 *  the License at:
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 *  OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
def version()
	{
	return "2.2.2";
	}

definition(
    name: "Other Hub Device Viewer",
    namespace: "krlaframboise",
    author: "Kevin LaFramboise",
    description: "${version()} Provides information about the state of the specified devices from a different hub.",
    category: "My Apps",
		iconUrl: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/other-hub-device-viewer-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/other-hub-device-viewer-icon-2x.png",
    iconX3Url: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/other-hub-device-viewer-icon-3x.png")

 preferences {
	page(name:"mainPage")	
  page(name:"displaySettingsPage")
	page(name:"thresholdsPage")
	page(name:"notificationsPage")
	page(name:"otherSettingsPage")
	page(name: "displaySmartThingsUrlPage", nextPage: "mainPage")
	page(name: "displaySmartThingsRoutinesPage", nextPage: "mainPage")
	page(name: "refreshSmartThingsUrlPage")	
	page(name: "refreshSmartThingsUrlConfirmPage")		
}

// Main Menu Page
def mainPage() {	
	dynamicPage(name:"mainPage", uninstall:true, install:true) {
		def deviceCount = childDevices?.size() ?: 0
		
		if (!state.configured) {
			state.configured = true
			
			initializeAppEndpoint()			

			section("") {
				paragraph title: "Other Hub Device Viewer", 
					image: "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer/other-hub-device-viewer-icon-2x.png", 
					"Update the fields below and then tap Save and re-open the application from your Installed SmartApps list."
				
				input "childPrefix", "text",
					title:"Other Hub Device Prefix:",
					description: "The names of all the devices imported from the other hub will start with this prefix.\n\nThe prefix must start with a letter and the only supported characters are letters, numbers and hyphens.\n\nThis setting can't be changed once you leave this screen.",
					defaultValue: childPrefixSetting,
					required: true
					
				label(name: "label",
					title: "Assign a name to this SmartApp",
					required: false)
			}
		}
		else {
			section("Integration Status") {				
				paragraph "Other Hub Devices: ${deviceCount}\nLast Refreshed: ${formattedLastRefreshTime}"
			}
						
			section("SmartThings Dashboard") {				
				href "", title: "View Dashboard", style: "external", url: api_dashboardUrl()
				
				if (!deviceCount) {
					paragraph "The rest of the settings won't be visible until at least 1 device has been imported from the other hub."
					paragraph "Install the 'Other Hub Event Pusher' App in Hubitat and enter the SmartThings Dashboard Url into the corresponding field in that App."
					paragraph "To see the url can either tap the 'Display SmartThings Url' link below which will write the url to the SmartThings Live Logging window or tap the 'View Dashboard' link above which will open the url in a new window."
					paragraph "After you've finished configuring the 'Other Hub Event Pusher' App, wait until you start seeing devices named '${childPrefixSetting}' in the Mobile App's Things tab and then you can re-open this SmartApp to view the dashboard and all the settings."
				}	
				
				if (state.endpoint) {
					href "displaySmartThingsUrlPage", title: "Display SmartThings Dashboard Url", description: "Displays the url in the Live Logging section of the IDE."
				}
				href "refreshSmartThingsUrlConfirmPage", title: "Refresh SmartThings Dashboard Url", description: "Disables the existing url and generates a new one."
 				href "displaySmartThingsRoutinesPage", title: "When routine name are not auto fetched in Pusher module, tap to display your SmartThings Routine Names in Live Logging of the IDE", description: "Copy with brackets, then Paste into Pusher Module's ST Routines field."
			}			
			
				
			section("Settings") {
				if (deviceCount) {
					getPageLink("displaySettingsLink",
						"Display Settings",
						"displaySettingsPage")
					getPageLink("thresholdsLink",
						"Threshold Settings",
						"thresholdsPage")
					getPageLink("notificationsLink",
						"Notification Settings",
						"notificationsPage")
				}
				getPageLink("otherSettingsLink",
					"Other Settings",
					"otherSettingsPage")				
			}
		}		
	}
}

private getChildPrefixSetting() {
	return settings?.childPrefix ?: "OH-"	
}

private getFormattedLastRefreshTime() {
	return convertTimeToLocalDate(state.lastRefresh)?.format("MM/dd/yyyy HH:mm:ss") ?: ""
}

def displaySettingsPage() {
	dynamicPage(name:"displaySettingsPage") {
		section ("Sorting") {
			input "batterySortByValue", "bool",
				title: "Sort by Battery Value?",
				defaultValue: true,
				required: false
			input "tempSortByValue", "bool",
				title: "Sort by Measurement Value?",
				defaultValue: true,
				required: false
			input "lastEventSortByValue", "bool",
				title: "Sort by Last Event Value?",
				defaultValue: true,
				required: false			
		}	
		section ("Dashboard Settings") {			
			input "dashboardRefreshInterval", "number", 
				title: "Dashboard Refresh Interval: (60-86400 seconds)",
				range: "60..86400",
				defaultValue: 300,
				required: false
			input "dashboardDefaultView", "enum",
				title: "Default View:",
				required: false,
				options: getCapabilitySettingNames(true)
			input "dashboardMenuPosition", "enum", 
				title: "Menu Position:", 
				defaultValue: "Top of Page",
				required: false,
				options: ["Top of Page", "Bottom of Page"]
			input "dashboardLayout", "enum", 
				title: "Layout:", 
				defaultValue: "Normal",
				required: false,
				options: ["Normal", "Condensed - 1 Column", "Condensed - 2 Column", "Condensed - 3 Column"]
			input "displayOnlineOfflineStatus", "bool",
				title: "Display Online/Offline Status:",
				defaultValue: false,
				required: false
			input "customCSS", "text",
				title:"Enter CSS rules that should be appended to the dashboard's CSS file.",
				required: false
		}
		
		section ("Device Capabilities") {
			paragraph "All the capabilities supported by the selected devices are shown on the main screen by default, but this field allows you to limit the list to specific capabilities." 
			input "enabledCapabilities", "enum",
				title: "Display Which Capabilities?",
				multiple: true,
				options: getCapabilitySettingNames(false),
				required: false,
				submitOnChange: true
		}		
		section ("Device Capability Exclusions") {
			paragraph "The capability pages display all the devices that support the capability by default, but these fields allow you to exclude devices from each page."
			
			input "enabledExclusions", "enum",
				title: "Enable Device Exclusions for Which Capabilities?",
				multiple: true,
				options: getCapabilitySettingNames(true),
				required: false,
				submitOnChange: true
			
			if (settings?.enabledExclusions?.find { it == "Events" }) {
				input "lastEventExcludedDevices",
					"enum",
					title: "Exclude these devices from the Last Events page:",
					multiple: true,
					required: false,
					options:getExcludedDeviceOptions(null)
			}
			capabilitySettings().each { cap ->
				if (settings?.enabledExclusions?.find { it == getPluralName(cap) }) {
					input "${getPrefName(cap)}ExcludedDevices",
						"enum",
						title: "Exclude these devices from the ${getPluralName(cap)} page:",
						multiple: true,
						required: false,
						options: getDisplayExcludedDeviceOptions(cap)
				}
			}	
		}
	}
}

private getDisplayExcludedDeviceOptions(cap) {
	def devices = []	
	getDevicesByCapability(getCapabilityName(cap)).each { 
		if (deviceMatchesSharedCapability(it, cap)) {
			devices << it.displayName
		}
	}	
	return devices?.sort()
}

// Page for defining thresholds used for icons and notifications
def thresholdsPage() {
	dynamicPage(name:"thresholdsPage") {		
		section () {
			paragraph "The thresholds specified on this page are used to determine icons in the SmartApp and when to send notifications."			
		}
		section("Battery Thresholds") {
			input "lowBatteryThreshold", "number",
				title: "Enter Low Battery %:",
				multiple: false,
				required: false,
				range:"1..99"
		}
		section("Temperature Thresholds") {
			input "lowTempThreshold", "number",
				title: "Enter Low Temperature:",
				required: false,
				range:"-200..200"
			input "highTempThreshold", "number",
				title: "Enter High Temperature:",
				required: false,
				range:"-200..200"
		}
		section("Power Thresholds") {
			input "lowPowerThreshold", "number",
				title: "Enter Low Power in Watts:",
				required: false,
				range:"0..100000"
			input "highPowerThreshold", "number",
				title: "Enter High Power in Watts:",
				required: false,
				range:"0..100000"
		}
		section("Last Event Thresholds") {
			input "lastEventThreshold", "number",
				title: "Last event should be within:",
				required: false,
				defaultValue: 7
			input "lastEventThresholdUnit", "enum",
				title: "Choose unit of time:",
				required: false,
				defaultValue: "days",
				options: ["seconds", "minutes", "hours", "days"]
		}
	}
}

// Page for SMS and Push notification settings
def notificationsPage() {
	dynamicPage(name:"notificationsPage") {
		section ("Notification Settings") {
			paragraph "When notifications are enabled, notifications will be sent when the device value goes above or below the threshold specified in the Threshold Settings."				
			
			input "createAskAlexaMsg", "bool",
				title: "Create Ask Alexa Message?", 
				required: false,
				defaultValue: false			
			input "sendPush", "bool",
				title: "Send Push Notifications?", 
				required: false
			input("recipients", "contact", title: "Send notifications to") {
				input "phone", "phone", 
					title: "Send text message to",
					description: "Phone Number", 
					required: false
      }
			mode title: "Only send Notifications for specific mode(s)",
				required: false
			input "maxNotifications", "number",
				title: "Enter maximum number of notifications to receive within 5 minutes:",
				required: false
		}
		section ("Battery Notifications") {			
			input "batteryNotificationsEnabled", "bool",
				title: "Send battery notifications?",
				defaultValue: false,
				required: false
			input "batteryNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "batteryNotificationsExcluded", "enum",
				title: "Exclude these devices from battery notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Battery")
		}
		section ("Temperature Notifications") {
			input "temperatureNotificationsEnabled", "bool",
				title: "Send Temperature Notifications?",
				defaultValue: false,
				required: false
			input "temperatureNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "temperatureNotificationsExcluded", "enum",
				title: "Exclude these devices from temperature notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Temperature Measurement")
		}
		section ("Power Notifications") {
			input "powerNotificationsEnabled", "bool",
				title: "Send Power Notifications?",
				defaultValue: false,
				required: false
			input "powerNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "powerNotificationsExcluded", "enum",
				title: "Exclude these devices from power notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions("Power Meter")
		}
		section ("Last Event Notifications") {
			input "lastEventNotificationsEnabled", "bool",
				title: "Send Last Event notification?",
				defaultValue: false,
				required: false
			input "lastEventNotificationsRepeat", "number",
				title: "Send repeat notifications every: (hours)",
				defaultValue: 0,
				required: false
			input "lastEventNotificationsExcluded", "enum",
				title: "Exclude these devices from last event notifications:",
				multiple: true,
				required: false,
				options: getExcludedDeviceOptions(null)
		}
	}
}

private getExcludedDeviceOptions(capabilityName) {
	if (capabilityName) {
		getDevicesByCapability(capabilityName).collect { it.displayName }?.sort()
	}
	else {
		getAllDevices().collect { it.displayName }?.sort()
	}
}

// Page for misc preferences.
def otherSettingsPage() {
	dynamicPage(name:"otherSettingsPage") {		
		section ("") {			
			label(name: "label",
				title: "Assign a name",
				required: false)
			input "logging", "enum",
				title: "Types of messages to log:",
				multiple: true,
				required: false,
				defaultValue: ["debug", "info"],
				options: ["debug", "info", "trace"]
		}
		section ("Resources") {			
			paragraph "If you want to be able to use different icons, fork krlaframboise's GitHub Resources repository and change this url to the forked path.  If you do change this setting, make sure that the new location contains all the Required Files."
			href "", title: "View Required Resource List", 
				style: "external", 
				url: 			"http://htmlpreview.github.com/?https://github.com/krlaframboise/Resources/blob/master/simple-device-viewer/required-resources.html"
			input "resourcesUrl", "text",
				title: "Resources Url:",
				required: false,
				defaultValue: getResourcesUrl()
		}		
	}
}

private getPageLink(linkName, linkText, pageName, args=null) {
	def map = [
		name: "$linkName", 
		title: "$linkText",
		description: "",
		page: "$pageName",
		required: false
	]
	if (args) {
		map.params = args
	}
	href(map)
}


private displaySmartThingsUrlPage() {
	dynamicPage(name: "displaySmartThingsUrlPage", title: "") {
		section() {
			paragraph "The SmartThings Dashboard Url has been displayed in Live Logging"
			displaySmartThingsUrl()
		}
	}
}

private displaySmartThingsRoutinesPage() {
	dynamicPage(name: "displaySmartThingsRoutinesPage", title: "") {
		section() {
			paragraph "The SmartThings Routine Names shown below have been displayed in Live Logging"
			paragraph "Copy brackets and routines names into Pusher module, Routines input field"  	
			paragraph "${location.helloHome?.getPhrases()*.label}"
			log.info "SmartThings Routines: ${location.helloHome?.getPhrases()*.label}"	
		}
	}
}

private refreshSmartThingsUrlConfirmPage() {	
	dynamicPage(name: "refreshSmartThingsUrlConfirmPage", title: "") {
		section("Are you sure you want to refresh the dashboard url?") {
			paragraph "If you refresh the dashboard url, the old url will no longer work and you'll need to manually update it in the 'Other Hub Event Pusher' App."			
			href "refreshSmartThingsUrlPage", title: "Refresh SmartThings Url?"
		}
	}
}

private refreshSmartThingsUrlPage() {	
	dynamicPage(name: "refreshSmartThingsUrlPage", title: "") {
		section() {
			disableAppEndpoint()						
			if (initializeAppEndpoint()) {				
				paragraph "A new SmartThings Dashboard url has been created!"
			} 
			else {
				paragraph "Please go to the My SmartApps section of the IDE, click 'Other Hub Device Viewer', click AppSettings, click the OAuth link, and then click the 'Enable OAuth in Smart App' button.", title: "Please enable OAuth for the Other Hub Device Viewer SmartApp.", required: true, state: null
			}	
			displaySmartThingsUrl()
		}
	}
}

private disableAppEndpoint() {
	if (state.endpoint) {
		try {
			revokeAccessToken()
			logDebug "Access Token successfully revoked."
		}
		catch (e) {
			log.error "Unable to revoke Access Token: $e"
		}
		state.endpoint = null
	}	
}

private initializeAppEndpoint() {	
	if (!state.endpoint) {
		try {
			def accessToken = createAccessToken()
			if (accessToken) {
				state.endpoint = apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")				
			}
		} 
		catch(e) {
			log.error "$e"
			state.endpoint = null
		}
	}
	displaySmartThingsUrl()
	return state.endpoint
}

private displaySmartThingsUrl() {
	log.info "SmartThings Dashboard Url: ${api_dashboardUrl()}"	
}

private toggleSwitch(device, newState) {
	if (device) {
		childAction(device.id, newState)
		return "Turned ${device.displayName} ${newState.toUpperCase()}"
	}	
}

// Checks if any devices have the specificed capability
private devicesHaveCapability(name) {	
	return getAllDevices().find { hasCapability(it, name) } ? true : false
}

private getDevicesByCapability(name, excludeList=null) {	
	removeExcludedDevices(getAllDevices()
		.findAll { hasCapability(it, name.toString()) }
		.sort() { it.displayName.toLowerCase() }, excludeList)	
}

private hasCapability(device, capabilityName) {
	return device?.caps?.find { "${it}" == "${capabilityName}" } ? true : false
}

private getDeviceCapabilityListItems(cap) {
	def items = []
	getDevicesByCapability(getCapabilityName(cap), settings["${getPrefName(cap)}ExcludedDevices"])?.each { 	
		if (deviceMatchesSharedCapability(it, cap)) {
			items << getDeviceCapabilityListItem(it, cap)
		}
	}
	
	return items
}

private deviceMatchesSharedCapability(device, cap) {
	if (cap.name in ["Switch", "Light"]) {
		def isLight = (lightDevices?.find { it.id == device.id }) ? true : false				
		return ((cap.name == "Light") == isLight)
	}
	else {
		return true
	}
}

private getDeviceCapabilityListItem(device, cap) {
	def listItem = getDeviceCapabilityStatusItem(device, cap)
	listItem.deviceId = "${device.id}"
	if (listItem.image && cap.imageOnly) {
		listItem.title = "${device.displayName}"
	}
	else {
		listItem.title = "${getDeviceStatusTitle(device, listItem.status)}"
	}
	listItem
}

private getCapabilitySettingByPluralName(pluralName) {
	capabilitySettings().find { getPluralName(it)?.toLowerCase() == pluralName?.toLowerCase()}
}

private getCapabilitySettingByName(name) {
	capabilitySettings().find { it.name == name }
}

private getAllDeviceLastEventListItems() {
	removeExcludedDevices(getAllDevices(), lastEventExcludedDevices)?.collect {
		getDeviceLastEventListItem(it)		
	}
}

private getDeviceLastEventListItem(device) {
	def now = new Date().time
	def lastActivity = getLastActivityDate(device)
	def lastEventTime = lastActivity?.time ?: 0
	
	def listItem = [
		value: lastEventTime ? now - lastEventTime : Long.MAX_VALUE,
		status: lastEventTime ? "${getTimeSinceLastActivity(now - lastEventTime)}" : "N/A",
		deviceId: device.id
	]
	
	listItem.title = getDeviceStatusTitle(device, listItem.status)
	listItem.sortValue = settings.lastEventSortByValue != false ? listItem.value : device.displayName
	listItem.image = getLastEventImage(lastEventTime, device.status)
	return listItem
}

private getTimeSinceLastActivity(ms) {
	if (ms < msSecond()) {
		return "$ms MS"
	}
	else if (ms < msMinute()) {
		return "${calculateTimeSince(ms, msSecond())} SECS"
	}
	else if (ms < msHour()) {
		return "${calculateTimeSince(ms, msMinute())} MINS"
	}
	else if (ms < msDay()) {
		return "${calculateTimeSince(ms, msHour())} HRS"
	}
	else {
		return "${calculateTimeSince(ms, msDay())} DAYS"
	}		
}

private calculateTimeSince(ms, divisor) {
	return "${((float)(ms / divisor)).round()}"
}

private String getDeviceStatusTitle(device, status) {
	if (!status || status == "null") {
		status = "N/A"
	}
	if (state.refreshingDashboard) {
		return "${device.displayName}${getOnlineOfflineStatus(device.status)}"
	}
	else {
		return "${status} -- ${device.displayName}"
	}	
}

private getOnlineOfflineStatus(deviceStatus) {
	if (settings?.displayOnlineOfflineStatus && deviceStatus?.toLowerCase() in ["online", "offline"]) {
		return " (${deviceStatus.toLowerCase()})"
	}
	else {
		return ""
	}
}

private getDeviceCapabilityStatusItem(device, cap) {
	try {
		return getCapabilityStatusItem(cap, device.displayName, "${device.attrs[getAttributeName(cap)]}")
	}
	catch (e) {
		log.error "Device: ${device?.displayName} - Capability: $cap - Error: $e"
		return [
			image: "",
			sortValue: device?.displayName,
			value: "",
			status: "N/A"
		]
	}
}

private getCapabilityStatusItem(cap, sortValue, value) {
	def item = [
		image: "",
		sortValue: sortValue,
		value: value
	]
	item.status = item.value
	if ("${item.status}" != "null") {
	
		if (item.status == getActiveState(cap) && !state.refreshingDashboard) {
			item.status = "*${item.status}"
		}
			
		switch (cap.name) {
			case "Acceleration Sensor":
				item.image = getAccelerationImage(item.value)
				break
			case "Battery":			
				item.status = "${item.status}%"
				item.image = getBatteryImage(item.value)
				if (batterySortByValue != false) {
					item.sortValue = safeToInteger(item.value)
				}				
				break
			case "Temperature Measurement":
				item.image = getTemperatureImage(item.value)
				break
			case "Alarm":
				item.image = getAlarmImage(item.value)
				break
			case "Contact Sensor":
				item.image = getContactImage(item.value)
				break
			case "Lock":
				item.image = getLockImage(item.value)
				break
			case "Motion Sensor":
				item.image = getMotionImage(item.value)
				break
			case "Power Meter":
				item.image = getPowerImage(item.value)
				break
			case "Presence Sensor":
				item.image = getPresenceImage(item.value)
				break
			case ["Smoke Detector", "Carbon Monoxide Detector"]:
				item.image = getSmokeCO2Image(item.value)
				break
			case "Switch":
				item.image = getSwitchImage(item.value)
				break
			case "Light":
				item.image = getLightImage(item.value)
				break
			case "Water Sensor":
				item.image = getWaterImage(item.value)
				break
		}
		
		if (cap?.units != null) {
			item.status = "${item.status}${cap.units}"
			if (tempSortByValue != false) {
				item.sortValue = safeToFloat(item.value)
			}
		}
		else {
			item.status = "${item.status}".toUpperCase()
		}
	}
	else {
		item.status = "N/A"
	}
	return item
}

private getSelectedCapabilitySettings(timeout) {	
	if (!settings.enabledCapabilities) {
		return capabilitySettings().findAll { devicesHaveCapability(getCapabilityName(it)) }
	}
	else {
		def startTime = new Date().time
		return capabilitySettings().findAll {	
			if (new Date().time - startTime <= timeout) {
				(getPluralName(it) in settings.enabledCapabilities)
			}
		}
	}
}

private getAllDNIs() {
	return getAllDevices().collect { it.id }
}

private getAllDevices() {	
	def devices = []
	try {
		def slurper = new groovy.json.JsonSlurper()
		def cDevices = childDevices
		cDevices?.each {
			if (it.hasAttribute("otherHubData") && it.currentOtherHubData) {
				def device = slurper.parseText(it.currentOtherHubData)
				// device?.id = it.currentDeviceId
				devices << device
			}
		}
	}
	catch (e) {
		log.error "$e"
	}	
	return devices
}

private Date getLastActivityDate(device) {
	if (device?.activity) {
		return Date.parse("yyyy-MM-dd'T'HH:mm:ss", "${device.activity}".replace("+00:00", ""))		
	}
	else {
		return new Date((new Date().time - (90 * 24 * 60 * 60)))
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private String getLastEventImage(lastEventTime, deviceStatus) {
	def status = lastEventIsOld(lastEventTime, deviceStatus) ? "warning" : "ok"
	return getImagePath("${status}.png")
}

private boolean lastEventIsOld(lastEventTime, deviceStatus) {	
	try {
		// if (!lastEventTime || offlineOverride(deviceStatus)) {
		if (!lastEventTime) {
			return true
		}
		else {
			return ((new Date().time - getLastEventThresholdMS()) > lastEventTime)
		}
	}
	catch (e) {
		return true
	}
}

private String getAccelerationImage(currentState) {
	def status = (currentState == "active") ? "active" : "inactive"
	return getImagePath("acceleration-${status}.png")
}

private String getPresenceImage(currentState) {
	def status = (currentState == "present") ? "present" : "not-present"
	return getImagePath("${status}.png")
}

private String getContactImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getLockImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getMotionImage(currentState) {
	def status = (currentState == "active") ? "motion" : "no-motion"
	return  getImagePath("${status}.png")	
}

private String getSwitchImage(currentState) {
	return  getImagePath("switch-${currentState}.png")	
}

private String getLightImage(currentState) {
	return  getImagePath("light-${currentState}.png")
}

private String getAlarmImage(currentState) {
	return  getImagePath("alarm-${currentState}.png")	
}

private String getWaterImage(currentState) {
	return  getImagePath("${currentState}.png")	
}

private String getSmokeCO2Image(currentState) {
	def status = (currentState == "detected") ? "detected" : "clear"
	return getImagePath("smoke-${status}.png")	
}

private String getBatteryImage(batteryLevel) {
	def status 
	if (batteryIsLow(batteryLevel)) {
		status = "low"
	}
	else {
		switch (safeToInteger(batteryLevel, 100)) {
			case { it == 100}:
				status = "normal"
				break
			case { it >= 75 }:
				status = "normal-75"
				break
			case { it >= 50 }:
				status = "normal-50"
				break
			case { it >= 25 }:
				status = "normal-25"
				break
		}	
	}
	return  getImagePath("${status}-battery.png")	
}

private String getTemperatureImage(tempVal) {		
	def status = "normal"
	if (tempIsHigh(tempVal)) {
		status = "high"
	}
	else if (tempIsLow(tempVal)) {
		status = "low"
	}	
	return getImagePath("${status}-temp.png")
}

private String getPowerImage(powerVal) {		
	def status = "ok"
	if (powerIsHigh(powerVal)) {
		status = "warning"
	}
	else if (powerIsLow(powerVal)) {
		status = "warning"
	}	
	return getImagePath("${status}.png")
}

private String getImagePath(imageName) {
	if (iconsAreEnabled()) {
		if (state.refreshingDashboard) {
			return imageName
		}
		else {
			return "${getResourcesUrl()}/$imageName"
		}
	}
}

private boolean iconsAreEnabled() {
	return true
}

private getResourcesUrl() {
	def url = "https://raw.githubusercontent.com/krlaframboise/Resources/master/simple-device-viewer"

	if (settings?.resourcesUrl) {
		url = settings.resourcesUrl
	}
	
	return url
}

// Revokes the dashboard access token
def uninstalled() {
	logDebug "uninstalled()"
	disableAppEndpoint()	
}

def childUninstalled() {
	// Required to prevent warning on uninstall.
}


// Subscribes to events, starts schedules and initializes all settings.
def installed() {
	initialize()
}

// Resets subscriptions, scheduling and ensures all settings are initialized.
def updated() {
	unsubscribe()
	unschedule()
	state.refreshingDashboard = false
	
	initialize()
	
	logDebug "State Used: ${(state.toString().length() / 100000)*100}%"
}

private initialize() {
	if (!state.sentNotifications) {
		state.sentNotifications = []
	}
	
	runEvery5Minutes(performScheduledTasks)
	runIn(2, performScheduledTasks)
	
	initializeDevicesCache()
}

// Remove cached data for devices no longer selected and
// add cached data for newly selected devices.
void initializeDevicesCache() {
	def dnis = getAllDNIs()
	
	state.devicesCache?.removeAll { cache ->
		!dnis?.find { dni -> cache.dni == dni }
	}	
}

def performScheduledTasks() {
	state.lastPerformedScheduledTasks = new Date().time
	if (canCheckDevices(state.lastDeviceCheck)) {
		checkDevices()		
	}
	else {
		refreshDeviceActivityCache()
	}
}

void refreshDeviceActivityCache() {
	def devices = getAllDevices()
	def deviceCount = devices?.size()
	if (deviceCount) {
		def cachedTime = new Date().time
		
		for (int deviceIndex= 0; deviceIndex < deviceCount; deviceIndex++) {
			def device = devices[deviceIndex]
			
			def lastActivity
			
			def lastActivityDate = getLastActivityDate(device)
			if (lastActivityDate) {
				lastActivity = [
					name: "unknown",
					value: "",
					time: lastActivityDate.time
				]
			}
		
			if (lastActivity) {
				lastActivity.cachedTime = cachedTime
				getDeviceCache(device.id).activity = lastActivity
			}
			
		}	
		state."cachedTime" = cachedTime
	}	
}

private getDeviceCache(dni) {
	if (!state.devicesCache) {
		state.devicesCache = []
	}
	
	def deviceCache = state.devicesCache.find { cache -> "$dni" == "${cache.dni}" }
	if (!deviceCache) {
		deviceCache = [dni: "$dni", activity: [ ]]
		state.devicesCache << deviceCache
	}
	return deviceCache
}


// Generates notifications if device attributes fall outside of specified thresholds and ensures that notifications are spaced at least 5 minutes apart.
def checkDevices() {
	logDebug "Checking Device Thresholds"
	
	state.lastDeviceCheck = new Date().time
	state.currentCheckSent = 0
		
	if (settings.batteryNotificationsEnabled) {
		runIn(90, checkBatteries)
	}			
	if (settings.temperatureNotificationsEnabled) {
		runIn(61, checkTemperatures)
	}			
	if (settings.powerNotificationsEnabled) {
		runIn(30, checkPowers)
	}			
	if (settings.lastEventNotificationsEnabled) {
		checkLastEvents()
	}	
}

private canCheckDevices(lastCheck) {	
	return (settings.batteryNotificationsEnabled ||
		settings.temperatureNotificationsEnabled ||
		settings.powerNotificationsEnabled ||
		settings.lastEventNotificationsEnabled) &&
		timeElapsed((lastCheck ?: 0) + msMinute(5), true)
}

def checkTemperatures() {
	logDebug "Checking Temperatures"
	def cap = getCapabilitySettingByName("Temperature Measurement")
	
	getDevicesByCapability("Temperature Measurement", temperatureNotificationsExcluded)?.each {	
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = null
		if (tempIsHigh(item.value)) {
			message = "High Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		else if (tempIsLow(item.value)) {			
			message = "Low Temperature Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		
		handleDeviceNotification(it, message, "temperature", temperatureNotificationsRepeat)
	}
}

private boolean tempIsHigh(val) {
	isAboveThreshold(val, highTempThreshold, 73)
}

private boolean tempIsLow(val) {
	isBelowThreshold(val, lowTempThreshold, 63)	
}

def checkPowers() {
	logDebug "Checking Powers"
	def cap = getCapabilitySettingByName("Power Meter")
	
	getDevicesByCapability("Power Meter", powerNotificationsExcluded)?.each {	
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = null
		if (powerIsHigh(item.value)) {
			message = "High Power Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		else if (powerIsLow(item.value)) {			
			message = "Low Power Alert - ${getDeviceStatusTitle(it, item.status)}"			
		}
		
		handleDeviceNotification(it, message, "power", powerNotificationsRepeat)
	}
}

private boolean powerIsHigh(val) {
	isAboveThreshold(val, highPowerThreshold, 500)
}

private boolean powerIsLow(val) {
	isBelowThreshold(val, lowPowerThreshold, 50)
}

def checkBatteries() {
	logDebug "Checking Batteries"
	def cap = getCapabilitySettingByName("Battery")

	getDevicesByCapability("Battery", batteryNotificationsExcluded)?.each {
		def item = getDeviceCapabilityStatusItem(it, cap)
		
		def message = batteryIsLow(item.value) ? "Low Battery Alert - ${getDeviceStatusTitle(it, item.status)}" : null
		
		handleDeviceNotification(it, message, "battery", batteryNotificationsRepeat)
	}
}

private boolean batteryIsLow(batteryLevel) {
	isBelowThreshold(batteryLevel, lowBatteryThreshold, 25)
}

private boolean isAboveThreshold(val, threshold, int defaultThreshold) {
	if (threshold == null) {
		return false
	}
	else {
		return safeToInteger(val) > safeToInteger(threshold, defaultThreshold)
	}
}

private boolean isBelowThreshold(val, threshold, int defaultThreshold) {
	if (threshold == null) {
		return false
	}
	else {
		safeToInteger(val) < safeToInteger(threshold,defaultThreshold)
	}
}

private int safeToInteger(val, defaultVal=0) {
	try {
		if (val != null && "$val".isNumber()) {
			if ("$val".isInteger()) {
				return "$val".toInteger()
			}
			else if ("$val".isFloat()) {
				return "$val".toFloat().round().toInteger()
			}
			else if ("$val".isDouble()) {
				return "$val".toDouble().round().toInteger()
			}
			else {
				logDebug "Unable to parse $val to Integer so returning 0"
				return 0
			}
		}
		else {
			return safeToInteger(defaultVal, 0)
		}		
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return 0
	}
}

private int safeToFloat(val, defaultVal=0) {
	try {
		if (val && "$val".isNumber()) {
			if ("$val".isInteger() || "$val".isFloat() || "$val".isDouble()) {
				return "$val".toFloat()
			}			
			else {
				logDebug "Unable to parse $val to Integer so returning 0"
				return 0
			}
		}
		else {
			return safeToFloat(defaultVal, 0)
		}		
	}
	catch (e) {
		logDebug "safeToFloat($val, $defaultVal) failed with error $e"
		return 0
	}
}

def checkLastEvents() {
	logDebug "Checking Last Events"
	removeExcludedDevices(getAllDevices(), lastEventNotificationsExcluded)?.each {
		
		def item = getDeviceLastEventListItem(it)

		// def isOld = (item.value > getLastEventThresholdMS() || offlineOverride(it.status))
		def isOld = (item.value > getLastEventThresholdMS())
		
		def message = null
		if (isOld) {
			message = "Last Event Alert - ${getDeviceStatusTitle(it, item.status)}"
			// if (offlineOverride(it.status)) {
				// message = "${message} (OFFLINE)"
			// }
		}
		   		
		handleDeviceNotification(it, message, "lastEvent", lastEventNotificationsRepeat)
	}
}

private long getLastEventThresholdMS() {
	long threshold = lastEventThreshold ? lastEventThreshold : 7
	long unitMS
	switch (lastEventThresholdUnit) {
		case "seconds":
			unitMS = msSecond()
			break
		case "minutes":
			unitMS = msMinute()
			break
		case "hours":
			unitMS = msHour()
			break
		default:
			unitMS = msDay()
	}
	return (threshold * unitMS)
}

private long msSecond(multiplier=1) {
	return (1000 * multiplier)
}

private long msMinute(multiplier=1) {
	return (msSecond(60) * multiplier)
}

private long msHour(multiplier=1) {
	return (msMinute(60) * multiplier)
}

private long msDay(multiplier=1) {
	return (msHour(24) * multiplier)
}


private removeExcludedDevices(deviceList, excludeList) {
	if (excludeList) {
		def result = []
		deviceList.each {
			def displayName = "${it.displayName}"
			if (!excludeList.find { it == "$displayName" }) {
				result << it
			}
		}
		return result
	}
	else {
		return deviceList
	}
}

private handleDeviceNotification(device, message, notificationType, notificationRepeat) {
	def id = "$notificationType${device.id}"
	def lastSentMap = state.sentNotifications.find { it.id == id }
	def lastSent = lastSentMap?.lastSent
	def repeatMS = notificationRepeat ? msHour(notificationRepeat) : 0	
	def unknownStatus = message?.contains("- N/A --") ? true : false
			
	if (message && !unknownStatus) {
		if (canSendNotification(lastSent, repeatMS)){
			if (lastSent) {
				lastSentMap.lastSent = new Date().time
			}
			else {
				state.sentNotifications << [id: "$id", lastSent: new Date().time]				
			}			
			sendNotificationMessage(message)
		}
	}
	else if (unknownStatus) {
		// Do nothing because occassionally null is returned for
		// battery or last event when it really has a value causing
		// false notifications to be sent out.
	}
	else if (lastSent) {
		state.sentNotifications.remove(lastSentMap)
	}
}

private boolean canSendNotification(lastSent, repeatMS) {	
	def sendLimitExceeded = state.currentCheckSent >= (maxNotifications ? maxNotifications : 1000)
	
	if (!lastSent && !sendLimitExceeded) {
		return true
	}
	else {
		return (!sendLimitExceeded && repeatMS > 0 && timeElapsed(lastSent + repeatMS))
	}
}

private sendNotificationMessage(message) {	
	if (sendPush || recipients || phone) {
		state.currentCheckSent = state.currentCheckSent + 1
		logInfo "Sending $message"
		if (settings.createAskAlexaMsg) {
			sendLocationEvent(name: "AskAlexaMsgQueue", value: "Other Hub Device Viewer", isStateChange: true, descriptionText: "${message.replace(' - ', ' ')}", unit: "SDV")
		}
		if (sendPush) {
			sendPush(message)
		}
		if (location.contactBookEnabled && recipients) {
			sendNotificationToContacts(message, recipients)
		} else {
			if (phone && sendPush) {
				sendSmsMessage(phone, message)
			}
			else if (phone) {
				sendSms(phone, message)
			}
		}
	}
	else {
		logInfo "Could not send message because notifications have not been configured.\nMessage: $message"
	}
}

private boolean timeElapsed(timeValue, nullResult=false) {
	if (timeValue != null) {
		def currentTime = new Date().time
		return (timeValue <= currentTime)
	} else {
		return nullResult
	}
}

private getCapabilitySettingNames(includeEvents) {
	def items = []
	if (includeEvents) {
		items << "Events"
	}
	items += capabilitySettings().collect { getPluralName(it) }?.unique()
	return items.sort()
}

private getCapabilityName(capabilitySetting) {
	capabilitySetting.capabilityName ?: capabilitySetting.name
}

private String getAttributeName(capabilitySetting) {
	capabilitySetting.attributeName ?: capabilitySetting.name.toLowerCase()
}

private String getActiveState(capabilitySetting) {
	capabilitySetting.activeState ?: capabilitySetting.name.toLowerCase()
}

private String getPrefName(capabilitySetting) {
	capabilitySetting.prefName ?: getPrefType(capabilitySetting)
}

private String getPrefType(capabilitySetting) {
	capabilitySetting.prefType ?: capabilitySetting.name.toLowerCase()
}

private String getPluralName(capabilitySetting) {
	capabilitySetting.pluralName ?: "${capabilitySetting.name}s"
}


private capabilitySettings() {
	[		
		[
			name: "Acceleration Sensor",
			prefType: "accelerationSensor",
			attributeName: "acceleration",
			activeState: "active",
			imageOnly: true
		],
		[
			name: "Alarm",
			activeState: "off",
			imageOnly: true
		],
		[
			name: "Battery",
			pluralName: "Batteries"
		],
		[
			name: "Carbon Monoxide Detector",
			prefType: "carbonMonoxideDetector",
			attributeName: "carbonMonoxide",
			activeState: "detected",
			imageOnly: true
		],
		[
			name: "Contact Sensor",
			prefType: "contactSensor",
			attributeName: "contact",
			activeState: "open",
			imageOnly: true
		],
		[
			name: "Energy Meter",
			prefType: "energyMeter",
			attributeName: "energy",
			units: " kWh"
		],
		[
			name: "Illuminance Measurement",
			pluralName: "Illuminance Sensors",
			prefType: "illuminanceMeasurement",
			attributeName: "illuminance",
			units: " lx"
		],
		// [
			// name: "Light",
			// prefName: "light",
			// prefType: "switch",
			// capabilityName: "Switch",
			// attributeName: "switch",
			// activeState: "on",		
			// imageOnly: true
		// ],
		[
			name: "Lock",
			activeState: "locked",
			imageOnly: true
		],		
		[
			name: "Motion Sensor", 
			prefType: "motionSensor",
			attributeName: "motion",
			activeState: "active",
			imageOnly: true
		],
		[
			name: "Power Meter",
			prefType: "powerMeter",
			attributeName: "power",
			units: " W"
		],
		[
			name: "Presence Sensor",
			prefType: "presenceSensor",
			attributeName: "presence",
			activeState: "present",
			imageOnly: true
		],
		[
			name: "Relative Humidity Measurement",
			pluralName: "Relative Humidity Sensors",
			prefType: "relativeHumidityMeasurement",
			attributeName: "humidity",
			units: "%"
		],
		[
			name: "Smoke Detector",
			prefType: "smokeDetector",
			attributeName: "smoke",
			activeState: "detected",
			imageOnly: true
		],
		[
			name: "Switch",
			pluralName: "Switches",		
			activeState: "on",
			imageOnly: true
		],		
		[
			name: "Temperature Measurement",
			pluralName: "Temperature Sensors",
			prefType: "temperatureMeasurement",
			attributeName: "temperature",
			units: "°${location.temperatureScale}"
		],
		[
			name: "Valve",
			activeState: "open",
			attributeName: "valve"
		],
		[
			name: "Water Sensor",
			prefType: "waterSensor",
			attributeName: "water",
			activeState: "wet",
			imageOnly: true
		]
	]
}



/********************************************
*    Dashboard
********************************************/

mappings {
	path("/event/:name/:value/:deviceId") {action: [GET: "api_event"]}
	path("/refresh-devices") {action: [POST: "api_refreshDevices"]}
	path("/update-other-hub-url") {action: [POST: "api_updateOtherHubUrl"]}
	path("/dashboard") {action: [GET: "api_dashboard"]}
	path("/dashboard/:capability") {action: [GET: "api_dashboard"]}	
	path("/dashboard/:capability/:cmd") {action: [GET: "api_dashboard"]}
	path("/dashboard/:capability/:cmd/:deviceId") {action: [GET: "api_dashboard"]}	
}

private api_event() {
	log.debug "Device ${params?.deviceId} ${params?.name} is ${params?.value}"
	// logDebug "Device ${params?.deviceId} ${params?.name} is ${params?.value}"
	if (params?.name=='mode')
		{
		def STroutine=URLDecoder.decode(params.value, "UTF-8");				//url decode
		def HEmode=URLDecoder.decode(params.deviceId, "UTF-8");				//url decode
		log.debug "HE Mode ${HEmode} executing Routine ${STroutine}"
		location.helloHome?.execute(STroutine)
		return []
		}
	else
	if (params?.name=='routines')
		{
		return location.helloHome?.getPhrases()*.label
//	    def resp = []
//	    location.helloHome?.getPhrases()*.label.each {
//			resp << [name: it.displayName, value: it.currentValue("switch")]
//    		}
	    return resp
	    }
	else
		{
		childEvent("${params?.deviceId}", "${params?.name}", params.value)
		return []
		}
}

private api_dashboardUrl(capName=null) {	
	def pageName
	capName = capName ?: api_getDefaultCapabilityName()
	if (capName?.toLowerCase() == "events") {
		pageName = "events"
	}
	else {		
		def cap = getCapabilitySettingByPluralName(capName)
		pageName = (cap ? getPluralName(cap)?.toLowerCase()?.replace(" ", "-") : "") ?: "lights"		
	}
	return "${state.endpoint}dashboard/${pageName}"
}

private api_getDefaultCapabilityName() {
	if (settings?.dashboardDefaultView) {
		return settings.dashboardDefaultView
	}
	else {
		return "events"
	}	
}

def api_dashboard() {
	def cap
	def currentUrl
	def menu = ""
	def header = ""
	def footer = ""
	def refreshInterval = 300
	def html = ""

	try {
		state.refreshingDashboard = true
		header = api_getPageHeader()		
			
		if (params.capability == "events") {
			currentUrl = api_dashboardUrl("events")
			header = api_getPageHeader("Events")
		}
		else if (params.capability) {			
			cap = params.capability ? getCapabilitySettingByPluralName(params.capability?.replace("-", " ")) : null
		
			currentUrl = api_dashboardUrl(getPluralName(cap))
			header = api_getPageHeader("${getPluralName(cap)}")
		}	
		
		if (!params.capability && state.normalRefreshInterval) {
			currentUrl = api_dashboardUrl(null)
			state.normalRefreshInterval = false	// Prevents fast refresh loop
			refreshInterval = 0			
		}
		else {
			refreshInterval = api_getRefreshInterval(params.cmd)
		}
		
		menu = api_getMenuHtml(currentUrl)
		footer = api_getPageFooter(null, currentUrl)
		
		if (params.capability == "events") {
			def items = getAllDeviceLastEventListItems()?.unique()
			if (settings?.lastEventSortByValue != false) {
				items?.each { it.sortValue = (it.sortValue * -1) }
			}
			html = api_getItemsHtml(items)
		}
		else if (cap) {
			html = api_getCapabilityHtml(cap, currentUrl, params.deviceId, params.cmd)
		}
		
		html = "<section>$html</section>"
	}
	catch(e) {
		log.error "Unable to load dashboard:\n$e"
		html = api_getPageErrorHtml(e)
	}
	state.refreshingDashboard = false
	return api_renderHtmlPage(api_getPageBody(header, html, menu, footer), currentUrl, refreshInterval)	
}

private api_getRefreshInterval(cmd) {
	if (api_isToggleSwitchCmd(cmd) && state.normalRefreshInterval) {
		state.normalRefreshInterval = false // Prevents fast refresh loop
		return 3
	}
	else {
		state.normalRefreshInterval = true
		return settings.dashboardRefreshInterval ?: 300
	}
}

private api_getCapabilityHtml(cap, currentUrl, deviceId, cmd) {	
	def html = ""
	if (api_isToggleSwitchCmd(cmd)) {		
		if (deviceId) {
			html = "<h1>${api_toggleSwitch(cap, deviceId, cmd)}</h1>"
		}
		else {
			html = api_toggleSwitches (cap, cmd)
		}
	
		html = "<div class=\"command-results\">$html</div>"		
	}			
	
	if (cap.name in ["Switch","Light", "Alarm"]) {
		html += api_getToggleItemsHtml(currentUrl, getDeviceCapabilityListItems(cap))
	}
	else {
		html += api_getItemsHtml(getDeviceCapabilityListItems(cap))
	}
	return html
}

private api_isToggleSwitchCmd(cmd) {
	return (cmd in ["on", "off", "toggle"])
}

private api_getMenuHtml(currentUrl) {
	def className = api_menuAtTop() ? "top" : "bottom"
	def html = "<nav class=\"$className\">"
	
	html += api_getMenuItemHtml("Refresh", "refresh", currentUrl)
	
	html += api_getMenuItemHtml("Events", "warning", api_dashboardUrl("events"))
	
	getSelectedCapabilitySettings(15000).each {
		html += api_getMenuItemHtml(getPluralName(it), getPrefName(it), api_dashboardUrl(getPluralName(it)))
	}
	
	html += "</nav>"
	return html
}

private api_getMenuItemHtml(linkText, className, url) {
	return "<div class=\"menu-item\"><a href=\"$url\" ${api_getWaitOnClickAttr()} class=\"item-image $className\"><span>${linkText}</span></a></div>"
}

private api_toggleSwitches(cap, cmd) {
	def html = ""	
	
	getDeviceCapabilityListItems(cap).each {
		html += "<li>${api_toggleSwitch(cap, it.deviceId, cmd)}</li>"
	}
	
	if (html) {
		return "<h1>The following changes were made:</h1><ul>$html</ul>"
	}
	else {
		return "<h1>No Changes Were Made</h1>"
	}	
}

private api_toggleSwitch(cap, deviceId, cmd) {
	def device = deviceId ? getAllDevices().find { "${it.id}" == "${deviceId}" } : null
		
	if (device) {
		def newState = api_getNewSwitchState(device, cmd)
		if (newState) {
			return toggleSwitch(device, newState)
		}		
		else {
			return "Unable to determine new switch state for ${device.displayName}"
		}
	}
	else {
		return "Unable to find a device with id ${deviceId}"
	}		
}

private api_getNewSwitchState(device, cmd) {
	if (cmd in ["on", "off"]) {
		return cmd
	}
	else if (cmd == "toggle") {
		return device?.attrs?."switch" == "off" ? "on" : "off"
	}
	else {
		return ""
	}
}

private api_getToggleItemsHtml(currentUrl, listItems) {
	def html = ""
	
	listItems.unique().each {	
		html += api_getItemHtml(it.title, it.image, "${currentUrl}/toggle/${it.deviceId}", it.deviceId, it.status)
	}
	
	def pluralName
	def imageName	
	if (listItems) {
		pluralName = listItems[0] ? getPluralName(listItems[0]) : ""
		imageName = listItems[0]?.image?.replace(".png","")
		imageName = imageName?.replace("-on", "")?.replace("-off", "")
	}	
	
	return html
}

private api_getItemsHtml(listItems) {
	def html = ""		
	
	listItems?.sort { it.sortValue }
	
	listItems?.unique().each {				
		html += api_getItemHtml(it.title, it.image, null, it.deviceId, it.status)
	}
	return html
}

private api_getItemHtml(text, imageName, url, deviceId, status) {
	def imageClass = imageName ? imageName?.replace(".png", "") : ""
	def deviceClass = deviceId ? "$deviceId"?.replace(" ", "-") : "none"
	def html 
	
	if (url) {
		html = "<a class=\"item-text\" href=\"$url\" ${api_getWaitOnClickAttr()}><span class=\"label\">$text</span></a>"
	}
	else {
		html = "<div class=\"item-text\"><span class=\"label\">$text</span></div>"		
	}
	
	html = "<div class=\"item-image-text\"><div class=\"item-image $imageClass\"><span class=\"item-status\">$status</span></div>$html</div>"
	
	return "<div class=\"device-item device-id-$deviceClass\">$html</div>"
}

private api_getWaitOnClickAttr() {
	return "onclick=\"displayWaitMsg(this)\""
}

private api_getPageBody(header, content, menu, footer) {
	if (api_menuAtTop()) {
		return "$header$menu$content$footer"
	}
	else {
		return "$header$content$menu$footer"		
	}
}

private api_menuAtTop() {
	return (settings.dashboardMenuPosition != "Bottom of Page")
}

private api_getPageHeader(html=null) {
	def header = "Other Hub Device Viewer"
	header += html ? " - $html" : ""
	return "<header>$header</header>"
}

private api_getPageFooter(html, currentUrl) {
	html = html ?: ""
	return "<footer>$html<textarea class=\"dashboard-url\" rows=\"2\">${currentUrl}</textarea></footer>"
}

private api_getPageErrorHtml(e) {
	return "<div class=\"error-message\"><h1>Unable to Load Dashboard</h1><h2>Error Message:</h2><p>$e</p><p><a href=\"${api_dashboardUrl()}\">Back to Default Dashboard</a></p></div>"		
}

private api_renderHtmlPage(html, url, refreshInterval) {
	render contentType: "text/html", 
		data: "<!DOCTYPE html><html lang=\"en\"><head><title>Other Hub Device Viewer - Dashboard</title><meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><meta http-equiv=\"refresh\" content=\"${refreshInterval}; URL=${url}\">${api_getCSS()}</head><body>${html}${api_getJS()}</body></html>"
}

private api_getJS() {	
	return "<script>function displayWaitMsg(link) { link.className += \" wait\"; }</script>"
}

private api_getCSS() {
	// return "<link rel=\"stylesheet\" href=\"${getResourcesUrl()}/dashboard.css\">"
	
	def css = "body {	font-size: 100%;	text-align:center;	font-family:Helvetica,arial,sans-serif;	margin:0 0 10px 0;	background-color: #000000;}header, nav, section, footer {	display: block;	text-align:center;}header {	margin: 0 0 0 0;	padding: 4px 0 4px 0;	width: 100%;		font-weight: bold;	font-size: 100%;	background-color:#808080;	color:#ffffff;}nav.top{	padding-top: 0;}nav.bottom{	padding: 4px 4px 4px 4px;}section {	padding: 10px 20px 40px 20px;}.command-results {	background-color: #d6e9c6;	margin: 0 20px 20px 20px;	padding: 10px 20px 10px 20px;	border-radius: 100px;}.command-results h1 {	margin: 0 0 0 0;}.command-results ul {	list-style: none;}.command-results li {	line-height: 1.5;	font-size: 120%;}.dashboard-url {	display:block;	width:100%;	font-size: 80%;}.device-id-none{	background-color: #d6e9c6 !important;}.refresh {	background-image: url('refresh.png');}.acceleration-active {	background-image: url('acceleration-active.png');}.acceleration-inactive{	background-image: url('acceleration-inactive.png');}.alarm, .alarm-both {	background-image: url('alarm-both.png');}.alarm-siren {	background-image: url('alarm-siren.png');}.alarm-strobe {	background-image: url('alarm-strobe.png');}.alarm-off {	background-image: url('alarm-off.png');}.battery, .normal-battery {	background-image: url('normal-battery.png');}.normal-75-battery {	background-image: url('normal-75-battery.png');}.normal-50-battery {	background-image: url('normal-50-battery.png');}.normal-25-battery {	background-image: url('normal-25-battery.png');}.low-battery {	background-image: url('low-battery.png');}.open {	background-image: url('open.png');}.contactSensor, .closed {	background-image: url('closed.png');}.light, .light-on {	background-image: url('light-on.png');}.light-off {	background-image: url('light-off.png');}.lock, .locked{	background-image: url('locked.png');}.unlocked {	background-image: url('unlocked.png');}.motionSensor, .motion {	background-image: url('motion.png');}.no-motion {	background-image: url('no-motion.png');}.presenceSensor, .present {	background-image: url('present.png');}.not-present {	background-image: url('not-present.png');}.smokeDetector, .smoke-detected {	background-image: url('smoke-detected.png');}.smoke-clear {	background-image: url('smoke-clear.png');}.switch, .switch-on {	background-image: url('switch-on.png');}.switch-off {	background-image: url('switch-off.png');}.temperatureMeasurement, .normal-temp {	background-image: url('normal-temp.png');}.low-temp {	background-image: url('low-temp.png');}.high-temp {	background-image: url('high-temp.png');}.waterSensor, .dry {	background-image: url('dry.png');}.wet {	background-image: url('wet.png');}.ok {	background-image: url('ok.png');}.warning {	background-image: url('warning.png');}.device-item {	width: 200px;	display: inline-block;	background-color: #ffffff;	margin: 2px 2px 2px 2px;	padding: 4px 4px 4px 4px;	border-radius: 5px;}.item-image-text {	position: relative;	height: 75px;	width:100%;	display: table;}.item-image {	display: table-cell;	position: relative;	width: 35%;	border: 1px solid #cccccc;	border-radius: 5px;	background-repeat:no-repeat;	background-size:auto 70%;	background-position: center bottom;}.item-status {	width: 100%;	font-size:75%;	display:inline-block;}.item-text {	display: table-cell;	width: 65%;	position: relative;	vertical-align: middle;}a.item-text {	color:#000000;}.item-text.wait, .menu-item a.wait{	color:#ffffff;	background-image:url('wait.gif');	background-repeat:no-repeat;	background-position: center bottom;}.item-text.wait{	background-size:auto 100%;}.label {	display:inline-block;	vertical-align: middle;	line-height:1.4;	font-weight: bold;	padding-left:4px;}.menu-item {	display: inline-block;	background-color:#808080;	padding:4px 4px 4px 4px;	border:1px solid #000000;	border-radius: 5px;	font-weight:bold;}.menu-item .item-image{	display:table-cell;	background-size:auto 45%; background-position: bottom center;	height:50px;	width:75px;	border:0;	border-radius:0;}.menu-item .item-image.switch,.menu-item .item-image.light,.menu-item .item-image.battery,.menu-item .item-image.alarm,.menu-item .item-image.refresh {	background-size:auto 60%;}.menu-item a, .menu-item a:link, .menu-item a:hover, .menu-item a:active,.menu-item a:visited {	color: #ffffff;		text-decoration:none;}.menu-item:hover, .menu-item:hover a, .menu-item a:hover { 	background-color:#ffffff;	color:#000000 !important;}.menu-item span {	width: 100%;	font-size:55%;	display:inline-block;}@media (max-width: 639px){	.device-item {		width:125px;	}	.item-image-text {		height: 65px;	}	.item-image {		background-size: auto 60%;	}	.item-text .label {		font-size: 80%;		line-height: 1.2;	}}"
	
	css = css.replace("url('", "url('${getResourcesUrl()}/")
	css += api_getLayoutCSS()
	
	if (settings?.customCSS) {
		css += settings.customCSS
	}
	return "<style>$css</style>"
}

private api_getLayoutCSS() {
	def layout = settings?.dashboardLayout ?: "1 Column"
	def css = ""
	if (layout?.toLowerCase()?.contains("condensed")) {
		css = "section{padding:4px 0 0 0;}.device-item{width: 98%;padding:0 0 0 0;margin:0 0 0 0;border:0;border-radius:0;}.item-image-text{height:auto;padding-left:4px;}.item-image{background-position:left center;width:20%;border:0;border-radius:0;}.item-status{line-height:1.4;}.item-text{text-align:left;}"
	}
	if (layout?.contains("2")) {
		css += ".device-item{width: 45%;margin: 0 2px;}"
	}
	if (layout?.contains("3")) {
		css += ".device-item{width: 30%; margin: 0 1px;}"
	}
	return css ?: ""
}

private logDebug(msg) {
	if (loggingTypeEnabled("debug")) {
		log.debug msg
	}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	if (loggingTypeEnabled("info")) {
		log.info msg
	}
}

private loggingTypeEnabled(loggingType) {
	return (!settings?.logging || settings?.logging?.contains(loggingType))
}


/**************************************
	Other Hub Child Devices
**************************************/
private api_updateOtherHubUrl() {
	logTrace "api_updateOtherHubUrl"
	 
	def responseMsg = ""
	try {				
		def url = request?.JSON?.url
		if (url) {
			if (!url.startsWith("https://")) {
				url = "https://${url}"
			}
			state.otherHubUrl = url
			
			responseMsg = "Updated Other Hub Url: $url"
		}
		else {
			responseMsg = "Other Hub Url Not Specified"
		}	
	}
	catch (e) {
		log.error "$e"
		responseMsg = "Exception: ${e.message}"
	}
	render contentType: "text/html", 
		data: "${responseMsg}"	
}


private api_refreshDevices() {
	logTrace "api_refreshDevices()"
	 
	def responseMsg = ""
	try {
		state.lastRefresh = new Date().time
		
		def data = request.JSON	
		if (data) {			
			data.each { deviceData ->
				updateChildDeviceData(deviceData)
			}
			responseMsg = "Updated ${data.size()} Devices"
		}
		else {
			responseMsg = "No Device Data"
		}	
	}
	catch (e) {
		log.error "$e"
		responseMsg = "Exception: ${e.message}"
	}
	render contentType: "text/html", 
		data: "${responseMsg}"	
}

private updateChildDeviceData(deviceData) {
	logTrace "updateChildDeviceData: ${deviceData?.name}"
	
	def attrs = deviceData.attributes ?: null
	def caps = attrs ? getCapabilities(attrs) : null
	if (attrs && caps || "${attrs}".contains("pushed")) {
	
		def child = findChildByDeviceId("${deviceData.id}")		
		if (!child) {
			def deviceType = childDeviceTypes.find { devTypes -> 
				devTypes.capabilities.find { it in caps }
			}?.name
			
			if (!deviceType && "${attrs}".contains("pushed")) {
				deviceType = "Other Hub Button"
			}
			
			if (deviceType) {
				logTrace "Adding ${deviceType}: ${deviceData.name}"
				child = addNewChildDevice(deviceData, "${deviceType}")		
			}		
				
			if (!child) {
				logTrace "Adding Device: ${deviceData.name}"
				child = addNewChildDevice(deviceData, "Other Hub Device")
			}
			
			if (child) {		
				sendChildEvent(child, "deviceId", "${deviceData.id}")
			}
		}
		else {
			logTrace "Updating ${deviceData.name}"
		}
		
		if (child) {
			sendChildEvent(child, "lastRefresh", state.lastRefresh)
			
			def otherHubData = [
				id: child.deviceNetworkId, 
				displayName: child.displayName, 
				activity: lastActivity,
				attrs: attrs,
				caps: caps
			]
			
			sendChildDataEvents(child, otherHubData)
		}
	}
	else {
		logDebug "${deviceData.name} - Missing Data(attributes: ${attrs}, capabilities: ${caps})"
	}
}

private sendChildDataEvents(child, data) {
	if (data?.attrs) {
		sendChildEvent(child, "status", getChildStatus(data.attrs))
		
		if (data?.attrs?.held || data?.attrs?.pushed) {
			sendChildButtonEvents(child, data)
		}

		deviceRefreshCapabilities.each {		
			def val = data?.attrs?."${it.attributeName}"
			
			if (val != null) {				
				if (val instanceof String) {
					val = val?.toLowerCase()
				}
			
				sendChildCapabilityEvent(child, "${it.name}", "${it.attributeName}", val)			
			}
		}
	}
	
	if (data) {
		sendChildEvent(child, "otherHubData", groovy.json.JsonOutput.toJson(data))
	}
}

private sendChildButtonEvents(child, data) {
	if (data?.attrs?.pushed) {
		
		if (child.hasCommand("setLevel")) {
			logDebug "Activating Alexa Trigger #${data.attrs.pushed}"
			child.setLevel(data.attrs.pushed)
		}
		
		if (child.hasAttribute("button")) {
			child.sendEvent(name:"button", value: "pushed", data:[buttonNumber:data?.attrs?.pushed])
		}			
	}
}

private getChildStatus(attrs) {
	def attrStatuses = []
	attrs?.each { k, v ->
		def attrStatus = "$v"
		switch("$k") {
			case "battery":
				attrStatus = "${attrStatus}%"
				break
			case "temperature":
				attrStatus = "${attrStatus}°"
				break
		}
		if (!attrStatuses.find { "$it" == "$attrStatus" }) {
			attrStatuses << "$attrStatus"
		}
	}
	return attrStatuses?.join("/") ?: ""
}

private sendChildCapabilityEvent(child, capName, attrName, value) {
	if (child.hasCapability("${capName}")) {		
		if (value) {	
			def oldValue = child."current${attrName.capitalize()}"
			if ("${oldValue}" != "$value") {
			
				child.sendEvent(name: "${attrName}", value: value, displayed: true, isStateChange: true)
				
				if (attrName == "smoke" || attrName == "carbonMonoxide") {					
					if (child?.hasCommand("refreshAlarmStatus")) {
						child.refreshAlarmStatus()
					}		
				}				
			}
		}
	}
}

private sendChildEvent(child, name, value, displayed=false) {
	logTrace "sendChildEvent(${child}, ${name}, ${value}, ${displayed})"	
	child?.sendEvent(name: "$name", value: value, displayed: displayed)
}

private getCapabilities(attrValues) {
	def caps = []
	attrValues?.each { attr ->
		def capName = supportedCapabilities["${attr?.key}"]
		if (capName) {
			caps << capName
		}
	}
	return caps
}

private getSupportedCapabilities() {
	[
		"acceleration": "Acceleration Sensor",
		"alarm": "Alarm",
		"battery": "Battery",
		"carbonMonoxide": "Carbon Monoxide Detector",
		"contact": "Contact Sensor",
		"energy": "Energy Meter",
		"illuminance": "Illuminance Measurement",
		"lock": "Lock",
		"motion": "Motion Sensor",
		"power": "Power Meter",
		"presence": "Presence Sensor",
		"humidity": "Relative Humidity Measurement",
		"smoke": "Smoke Detector",
		"switch": "Switch",
		"switchLevel": "Switch Level",
		"temperature": "Temperature Measurement",
		"valve": "Valve",
		"voltage": "Voltage Measurement",
		"water": "Water Sensor"
	]
}

private addNewChildDevice(deviceData, deviceType) {
	try {		
		return addChildDevice(
			"krlaframboise",
			"${deviceType}",
			"${getChildDNI(deviceData.id)}", 
			null,
			[
				name: "${childPrefixSetting}${deviceData.name}",
				label: "${childPrefixSetting}${deviceData.label ?: deviceData.name}",completedSetup: true
			])
	}
	catch (e) {
		if ("$e".contains("UnknownDeviceTypeException")) {
			log.warn "Device Type Handler Not Installed: ${deviceType}"
		}
		else {
			log.error "$e"
		}
	}
}

private getChildDNI(deviceId) {
	return "${childDNIPrefix}${deviceId}"
}

private getChildDNIPrefix() {
	return "${childPrefixSetting}Device"
}

private getChildDeviceTypes() {
	[
		[name: "Other Hub Alarm", capabilities: ["Alarm"]],
		[name: "Other Hub Water Valve", capabilities: ["Valve"]],
		[name: "Other Hub Lock", capabilities: ["Lock"]],
		[name: "Other Hub Switch", capabilities: [
			"Switch", "Power Meter", "Energy Meter", "Voltage Measurement"]],
		[name: "Other Hub Water Sensor", capabilities: ["Water Sensor"]],
		[name: "Other Hub Presence Sensor", capabilities: ["Presence Sensor"]],
		[name: "Other Hub Contact Sensor", capabilities: ["Contact Sensor"]],
		[name: "Other Hub Smoke Detector", capabilities: ["Smoke Detector", "Carbon Monoxide Detector"]],		
		[name: "Other Hub Motion Sensor", capabilities: [
			"Illuminance Measurement", "Relative Humidity Measurement", "Acceleration Sensor", "Temperature Measurement"]]
	]
}

private convertTimeToLocalDate(utcTime) {
	def localDate = null
	if (utcTime) {
		try {
			def localTZ = TimeZone?.getTimeZone(location?.timeZone?.ID)			
			if (localTZ) {
				localDate = new Date(utcTime + localTZ?.getOffset(utcTime))
			}
			else {
				localDate = new Date(utcTime)
			}
		}
		catch (e) {
			logWarn "Unable to get formatted local time for ${utcTime}: ${e.message}"
		}
	}
	return localDate
}

private getDeviceRefreshCapabilities() {
	[		
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
			name: "Relative Humidity Measurement",
			pluralName: "Relative Humidity Sensors",
			prefType: "relativeHumidityMeasurement"
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


void childEvent(deviceId, name, value) {
	logTrace "childEvent($deviceId, $name, $value)"
	def child = findChildByDeviceId(deviceId)
	if (child) {
		def slurper = new groovy.json.JsonSlurper()
		def data = child.currentOtherHubData ? slurper.parseText(child.currentOtherHubData) : [attrs:[:], caps:[:]]
		
		data.attrs."$name" = value
		
		def now = new Date()
		data.activity = now		
			
		sendChildDataEvents(child, data)
	}
	else {
		log.warn "Device ${deviceId} not found"
	}
}

void childAction(deviceId, cmd, args="") {
	logTrace "childAction($deviceId, $cmd, $args)"
	sendRunAction(deviceId, cmd, args)
}

private sendRunAction(deviceId, cmd, args="") {
	def dni = "${deviceId}".substring(childDNIPrefix.length())
	if (args) {
		args = "/${args}"
	}
	
	def requestParams = [
		uri: "${buildOtherHubActionPath(dni, cmd, args)}",
		query: null,
		requestContentType: "application/json",
		body: []
	]
	
	try {	
		httpGet(requestParams) { response ->
			def msg = ""
			if (response?.status == 200) {
				msg = "Success"
			}
			else {
				msg = "${response?.status}"
			}
			logDebug "Other Hub Response: ${msg} (${response.data})"
		}
	}
	catch (e) {
		log.error "$e"
	}
}

private buildOtherHubActionPath(deviceId, cmd, args) {
	def path = "/action/${cmd}/${deviceId}"
	if (args) {
		path = "${path}/${args}"
	}
	
	return "${otherHubUrl}"?.replace("/?access_token", "${path}?access_token")
}

private getOtherHubUrl() {
	if (!state.otherHubUrl && settings?.hubitatUrl) {
		state.otherHubUrl = settings?.hubitatUrl
		return settings?.hubitatUrl
	}
	else {
		return state.otherHubUrl
	}
}

private findChildByDeviceId(deviceId) {	
	return childDevices?.find { "${it.deviceNetworkId}" == "${getChildDNI(deviceId)}" }
}
