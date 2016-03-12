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

importPackage(Packages.tools);
importPackage(Packages.client);

status = -1;
var sel;

function start() {
    if((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()){
        cm.sendNext("你的等級低於20或者超過30,很抱歉你不能參加此活動!!");
        cm.dispose();
        return;
    }
    if(cm.getPlayer().getMapId() % 10 == 1)
        cm.sendSimple("你需要什麼嗎??\r\n#b#L0# 給我 #t2270002# 和 #t2100067#.#l\r\n#L1# 我應該做什麼??#l\r\n#L2# 我要離開這裡..#l");
    else
        cm.sendSimple(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) == cm.getPlayer().getName() ? "你想開始比賽了嗎??#b\r\n#b#L3# 準備好了!!#l\r\n#L1# 我想踢除小白.#l\r\n#L2# 我要離開這裡.#l" : "你想要什麼?#b\r\n#L2# 我要離開這裡.#l");
}

function action(mode, type, selection){
    status++;
    if(mode != 1){
        if(mode == 0 && type == 0)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if(cm.getPlayer().getMapId() % 10 == 1){
        if (status == 0){
            if (sel == undefined)
                sel = selection;
            if(sel == 0){
                if(cm.haveItem(2270002))
                    cm.sendNext("你已經有了 #b#t2270002##k.");
                else if(cm.canHold(2270002) && cm.canHold(2100067)){
                    if(cm.haveItem(2100067))
                        cm.removeAll(2100067);
                    cm.gainItem(2270002, 32);
                    cm.gainItem(2100067, 5);
                    cm.sendNext("現在你已經得到 #b#t2270002##k 準備開始戰鬥!!!");
                }else
                    cm.sendNext("請確認你的消耗攔是否滿了...");
                cm.dispose();
            }else if(sel == 1)
                cm.sendNext("你這個新手。需要我詳細解釋嗎.");
            else
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) == cm.getPlayer().getName())
                    cm.sendYesNo("你確定你要離開？你是競技場的領導者，所以，如果你離開，整個戰鬥競技場將關閉.");
                else
                    cm.sendYesNo("你確定要離開這裡嗎??");
        } else if (status == 1){
            if(type == 1){
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) != cm.getPlayer().getName()){
                    cm.warp(980010020);
                    cm.dispose();
                    return;
                }else{
                    cm.getPlayer().removeAriantRoom((cm.getPlayer().getMapId() / 100) % 10);
                    cm.mapMessage(6, cm.getPlayer().getName() + " 已經離開了賽場，所以競技場將立即關閉。");
                    cm.warpMap(980010020);
		            cm.dispose();
					return;
                }
            }
            cm.sendNextPrev("它是很簡單的 實際上. 你會拿到這 #b#t2270002##k 道具, 你的使命就是要消除怪物數量 然後用 #b#t2270002##k 吸收能量.");
    }else if (status == 2)
        cm.sendNextPrev("這很簡單。如果你吸收怪物的力量 #b#t2270002##k, 那麼你會有 #b#t4031868##k, 你必須防止別人吸收才能贏得勝利!!");
    else if (status == 3)
        cm.sendNextPrev("但有個規則. #r你不能使用寵物這一點#k 明白嗎??~!");
        else if (status == 4)
            cm.dispose();
    }else{
        var nextchar = cm.getMap(cm.getPlayer().getMapId()).getCharacters().iterator();
        if(status == 0){
            if (sel == undefined)
                sel = selection;
            if(sel == 1)
                if(cm.getPlayerCount(cm.getPlayer().getMapId()) > 1){
                    var text = "你想要把哪個小白給踢了???"; //Not GMS like text
                    var name;
                    for(var i = 0; nextchar.hasNext(); i++){
                        name = nextchar.next().getName();
                        if(!cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1).equals(name))
                            text += "\r\n#b#L" + i + "#" + name + "#l";
                    }
                    cm.sendSimple(text);
                }else{
                    cm.sendNext("目前房間沒有人可以踢.");
                    cm.dispose();
                }
            else if(sel == 2){
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) == cm.getPlayer().getName())
                    cm.sendYesNo("你確定你要離開？你是競技場的領導者，所以，如果你離開，整個戰鬥競技場將關閉.");
                else
                    cm.sendYesNo("你確定要離開這裡嗎??"); //No GMS like.
            }else if(sel == 3)
                if(cm.getPlayerCount(cm.getPlayer().getMapId()) > 1)
                    cm.sendYesNo("房間內所有的設置，並沒有其他的玩家可能加入這場戰鬥競技場。你想現在開始遊戲?");
                else{
                    cm.sendNext("你需要2人才可以開始比賽!!!");
                    cm.dispose();
                }
        }else if (status == 1){
            if(sel == 1){
                for(var i = 0; nextchar.hasNext(); i++)
                    if(i == selection){
                        nextchar.next().changeMap(cm.getMap(980010000));
                        break;
                    }else
                        nextchar.next();
                cm.sendNext("這個玩家已經被踢了."); //Not GMS like
            }else if(sel == 2){
                if(cm.getPlayer().getAriantRoomLeaderName(((cm.getPlayer().getMapId() / 100) % 10) - 1) != cm.getPlayer().getName())
                    cm.warp(980010000);
                else{
                    cm.getPlayer().removeAriantRoom((cm.getPlayer().getMapId() / 100) % 10);
                    cm.mapMessage(6, cm.getPlayer().getName() + " 已經離開了賽場，所以競技場將立即關閉。");
                    cm.warpMap(980010000);
                }
            }else{
                cm.warpMap(cm.getPlayer().getMapId() + 1);
				cm.dispose();
            //}
            //cm.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(cm.getPlayer().getName(), 0, true));
            }
            cm.dispose();
        }
    }
}
