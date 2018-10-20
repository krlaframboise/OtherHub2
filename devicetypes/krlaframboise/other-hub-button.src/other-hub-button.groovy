/**
 *  SmartThings: Other Hub Button v2.1
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
	definition (name: "Other Hub Button", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Bridge"
		capability "Refresh"
		capability "Button"
		capability "Sensor"		
		capability "Battery"
		
		attribute "status", "string"
		attribute "deviceId", "number"
		attribute "lastRefresh", "string"
		attribute "otherHubData", "string"
	}

	tiles (scale: 2){
		multiAttributeTile(name:"button", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.button", key: "PRIMARY_CONTROL") {
				attributeState("button", label:'${currentValue}', 
					icon:"st.unknown.zwave.remote-controller",backgroundColor:"#cccccc")
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
		standardTile("refresh", "device.refresh", height:2, width:2) {
			state "default", label:'Refresh', 
				action:"refresh.refresh", 
				icon:"st.secondary.refresh-icon"
		}
		main "motion"
		details(["button", "battery", "refresh"])
	}
}

void refresh() {	
	executeCmd("refresh")
}

private executeCmd(cmd, args="") {	
	parent.childAction("${device.deviceNetworkId}", "$cmd", "$args")
}