/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Boats between Orbis and Geenie
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
        1.6 - Modified for ShootSource (Moogra)
	1.5 - Fix for infinity looping [Information]
	1.4 - Ship/boat is now showed
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	1.2 - It should be 2 ships not 1 [Information]
	1.1 - Add timer variable for easy edit [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.tools.packets);

var Orbis_btf;
var Boat_to_Orbis;
var Orbis_docked;
var Geenie_btf;
var Boat_to_Geenie;
var Geenie_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000152);
    Geenie_btf = em.getChannelServer().getMapFactory().getMap(260000110);
    Boat_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090410);
    Boat_to_Geenie = em.getChannelServer().getMapFactory().getMap(200090400);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000100);
    Geenie_docked = em.getChannelServer().getMapFactory().getMap(260000100);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000151);
    Geenie_Station = em.getChannelServer().getMapFactory().getMap(260000100);
    scheduleNew();
}

function scheduleNew() {
    Geenie_Station.setDocked(true);
    Orbis_Station.setDocked(true);
    Geenie_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", 240000);
    em.schedule("takeoff", 300000);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
    Geenie_Station.setDocked(false);
    Orbis_Station.setDocked(false);
    Geenie_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    Orbis_Station.broadcastMessage(MaplePacketCreator.boatPacket(false));
    em.setProperty("docked","false");
    var temp1 = Orbis_btf.getCharacters().iterator();
    while(temp1.hasNext())
        temp1.next().changeMap(Boat_to_Geenie, Boat_to_Geenie.getPortal(0));
    var temp2 = Geenie_btf.getCharacters().iterator();
    while(temp2.hasNext())
        temp2.next().changeMap(Boat_to_Orbis, Boat_to_Orbis.getPortal(0));
    em.schedule("arrived", 600000);
}

function arrived() {
    var temp1 = Boat_to_Orbis.getCharacters().iterator();
    while(temp1.hasNext())
        temp1.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
    var temp2 = Boat_to_Geenie.getCharacters().iterator();
    while(temp2.hasNext())
        temp2.next().changeMap(Geenie_docked, Geenie_docked.getPortal(0));
    scheduleNew();
}

function cancelSchedule() {
}
