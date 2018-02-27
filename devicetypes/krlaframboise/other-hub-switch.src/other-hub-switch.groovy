/**
 *  SmartThings: Other Hub Switch v2.0.1
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
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
metadata {
	definition (name: "Other Hub Switch", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Sensor"
		capability "Actuator"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Light"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Voltage Measurement"
				
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', 
					action: "switch.on", 
					nextState: "turningOn",
					icon: "st.switches.switch.off", 
					backgroundColor: "#ffffff"
				attributeState "turningOn", label: 'TURNING ON',
					action: "switch.off", 
					nextState: "off", 
					icon: "st.switches.switch.on", 
					backgroundColor: "#00a0dc"				
				attributeState "on", label: 'ON', 
					action: "switch.off",
					nextState: "turningOff",
					icon: "st.switches.switch.on", 
					backgroundColor: "#00a0dc"
				attributeState "turningOff", label: 'TURNING OFF', 
					action: "switch.off", 
					nextState: "off",
					icon: "st.switches.switch.off", 
					backgroundColor: "#ffffff"
			}
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "status", 
					label:'${currentValue}', 
					backgroundColor:"#ffffff"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "energy", label:'${currentValue} kWh', backgroundColor: "#cccccc"
		}
		valueTile("power", "device.power", width: 2, height: 2) {
			state "power", label:'${currentValue} W', backgroundColor: "#cccccc"
		}
		valueTile("voltage", "device.voltage", width: 2, height: 2) {
			state "voltage", label:'${currentValue} V', backgroundColor: "#cccccc"
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh-icon"
		}
		main "switch"
		details(["switch", "levelSliderControl", "energy", "power", "voltage", "refresh"])
	}
}

void on() {
	executeCmd("on")
}

void off() {
	executeCmd("off")
}

void setLevel(level) {	
	executeCmd("setLevel", level)	
}

void refresh() {	
	executeCmd("refresh")
}

private executeCmd(cmd, args="") {
	parent.childAction("${device.deviceNetworkId}", "$cmd", "$args")
}