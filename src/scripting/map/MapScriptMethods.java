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
package scripting.map;

import client.MapleClient;
import client.MapleQuestStatus;
import scripting.AbstractPlayerInteraction;
import server.quest.MapleQuest;
import tools.packets.MaplePacketCreator;

public class MapScriptMethods extends AbstractPlayerInteraction {

    public MapScriptMethods(MapleClient c) {
        super(c);
    }
    String rewardstring = " 勳章成就已達人,請找達利額領取勳章!";

    public void displayAranIntro() {
        switch (c.getPlayer().getMapId()) {
            case 914090010:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene0"));
                break;
            case 914090011:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene1" + c.getPlayer().getGender()));
                break;
            case 914090012:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene2" + c.getPlayer().getGender()));
                break;
            case 914090013:
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene3"));
                break;
            case 914090100:
                lockUI();
                c.announce(MaplePacketCreator.showIntro("Effect/Direction1.img/aranTutorial/HandedPoleArm" + c.getPlayer().getGender()));
                break;
        }
    }

    public void startExplorerExperience() {
        if (c.getPlayer().getMapId() == 1020100) //Swordman
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/swordman/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020200) //Magician
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/magician/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020300) //Archer
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/archer/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020400) //Rogue
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/rogue/Scene" + c.getPlayer().getGender()));
        } else if (c.getPlayer().getMapId() == 1020500) //Pirate
        {
            c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/pirate/Scene" + c.getPlayer().getGender()));
        }
    }

    public void goAdventure() {
        lockUI();
        c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/goAdventure/Scene" + c.getPlayer().getGender()));
    }

    public void goLith() {
        lockUI();
        c.announce(MaplePacketCreator.showIntro("Effect/Direction3.img/goLith/Scene" + c.getPlayer().getGender()));
    }

    public void explorerQuest(short questid, String questName) {
        MapleQuest quest = MapleQuest.getInstance(questid);
        if (!isQuestStarted(questid)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus q = getPlayer().getQuest(quest);
        if (!q.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(q.getMedalProgress());
        int infoex = quest.getInfoEx();
        getPlayer().announce(MaplePacketCreator.questProgress(quest.getInfoNumber(), status));
        StringBuilder smp = new StringBuilder();
        StringBuilder etm = new StringBuilder();
        if (q.getMedalProgress() == infoex) {
            etm.append("獲得了 ").append(questName).append(" 勳章!");
            smp.append("你已進入 <").append(questName).append(">").append(rewardstring);
            getPlayer().announce(MaplePacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            getPlayer().announce(MaplePacketCreator.earnTitleMessage(status + "/" + infoex + " 地區探索."));
            etm.append("嘗試為 ").append(questName).append(" 勳章.");
            smp.append("您正在進展著 ").append(questName).append(" 勳章. ").append(status).append("/").append(infoex);
        }
        getPlayer().announce(MaplePacketCreator.earnTitleMessage(etm.toString()));
        showInfoText(smp.toString());
    }

    public void touchTheSky() { //29004
        MapleQuest quest = MapleQuest.getInstance(29004);
        if (!isQuestStarted(29004)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        MapleQuestStatus q = getPlayer().getQuest(quest);
        if (!q.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(q.getMedalProgress());
        getPlayer().announce(MaplePacketCreator.questProgress(quest.getInfoNumber(), status));
        getPlayer().announce(MaplePacketCreator.earnTitleMessage(status + "/5 完成"));
        getPlayer().announce(MaplePacketCreator.earnTitleMessage("The One Who's Touched the Sky title in progress."));
        if (q.getMedalProgress() == quest.getInfoEx()) {
            showInfoText("誰感動了一個天空的" + rewardstring);
            getPlayer().announce(MaplePacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            showInfoText("誰感動了一個天空的勳章進度. " + status + "/5 完成");
        }
    }
}
