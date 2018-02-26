/**
 *  SmartThings: Other Hub Alarm v2.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
 *
 *    1.0 (02/26/2018)
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
	definition (name: "Other Hub Alarm", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Refresh"
		capability "Alarm"
		capability "Actuator"
		capability "Battery"
		capability "Switch"
		
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}
	
	tiles (scale: 2){	
		multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action:'alarm.strobe', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			}		
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "status", 
					label:'${currentValue}', 
					backgroundColor:"#ffffff"
			}
		}
		standardTile("both", "device.alarm", height:2, width:2) {
			state "default", label:'Both', action:"alarm.both", icon:"st.alarm.alarm.alarm"
		}
		standardTile("siren", "device.alarm", height:2, width:2) {
			state "default", label:'Siren', action:"alarm.siren", icon:"st.alarm.alarm.alarm"
		}
		standardTile("strobe", "device.alarm", height:2, width:2) {
			state "default", label:'Strobe', action:"alarm.strobe", icon:"st.alarm.alarm.alarm"
		}
		standardTile("off", "device.alarm", height:2, width:2) {
			state "default", label:'Off', action:"alarm.off"
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh-icon"
		}
		main "alarm"
		details(["alarm", "off", "siren", "strobe", "both", "battery", "refresh"])
	}
}

void off() {
	executeCmd("off")
}

void on() {
	executeCmd("both")
}

void both() {
	executeCmd("both")
}

void siren() {
	executeCmd("siren")
}

void strobe() {
	executeCmd("strobe")
}

void refresh() {	
	executeCmd("refresh")
}

private executeCmd(cmd, args="") {
	parent.childAction("${device.deviceNetworkId}", "$cmd", "$args")
}