/**
 *  SmartThings: Other Hub Water Sensor v2.0
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Donations: https://www.paypal.me/krlaframboise
 *
 *  Changelog:
 *
 *    2.0 (02/26/2018)
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
	definition (name: "Other Hub Water Sensor", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Refresh"
		capability "Water Sensor"
		capability "Sensor"		
		capability "Battery"
		capability "Temperature Measurement"
		
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}
	tiles (scale: 2){	
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", 
					label:'Dry', 
					icon: "st.alarm.water.dry",
					backgroundColor:"#ffffff"
				attributeState "wet", 
					label:'Wet', 
					icon:"st.alarm.water.wet", 
					backgroundColor:"#00a0dc"
			}		
			tileAttribute ("device.status", key: "SECONDARY_CONTROL") {
				attributeState "status", 
					label:'${currentValue}', 
					backgroundColor:"#ffffff"
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh-icon"
		}
		main "water"
		details(["water", "temperature", "battery", "refresh"])
	}
}

void refresh() {	
	executeCmd("refresh")
}

private executeCmd(cmd, args="") {	
	parent.childAction("${device.deviceNetworkId}", "$cmd", "$args")
}