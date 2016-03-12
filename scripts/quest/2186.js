/*
    Author: BubblesDev 0.75
    Quest: Abel Glasses Quest
*/

function end(mode, type, selection){
    if(!qm.isQuestCompleted(2186))
        if(qm.haveItem(4031853)){
            qm.gainItem(4031853, -1);
            qm.sendNext("任務完成.");
            qm.forceCompleteQuest();
        }else if(qm.haveItem(4031854) || qm.haveItem(4031855)){ //When I figure out how to make a completance with just a pickup xD
            if(qm.haveItem(4031854))
                qm.gainItem(4031854, -1);
            else
                qm.gainItem(4031855, -1);
            qm.sendNext("很抱歉這不是我的眼鏡.");
        }
        else
            qm.c.getPlayer().dropMessage(1, "未知的錯誤.....");
    qm.dispose();
}