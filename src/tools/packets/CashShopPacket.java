/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.SendOpcode;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MTSItemInfo;
import tools.HexTool;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;
import static tools.packets.MaplePacketCreator.addItemInfo;

/**
 *
 * @author Yuri
 */
public class CashShopPacket {

    public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId) {
        addCashItemInformation(mplew, item, accountId, null);
    }

    public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId, String giftMessage) {
        boolean isGift = giftMessage != null;
        boolean isRing = false;
        Equip equip = null;
        if (item.getType() == 1) {
            equip = (Equip) item;
            isRing = equip.getRingId() > -1;
        }
        mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
        if (!isGift) {
            mplew.writeInt(accountId);
            mplew.writeInt(0);
        }
        mplew.writeInt(item.getItemId());
        if (!isGift) {
            mplew.writeInt(item.getSN());
            mplew.writeShort(item.getQuantity());
        }
        mplew.writeAsciiString(item.getGiftFrom(), 15);
        if (isGift) {
            mplew.writeAsciiString(giftMessage, 74);
            return;
        }
        MaplePacketCreator.addExpirationTime(mplew, item.getExpiration());
        mplew.writeLong(0);
    }

    public static void addModCashItemInfo(MaplePacketLittleEndianWriter mplew, CashItemInfo.CashModInfo item) {

        /*
         F3 C2 35 01 
         FF FF 01 00 
        
         0F 0E 10 00 
         01 00 
         5A 7C 15 00 
         00 
         00 
         5A 00 
         00 00 00 00 
         00 00 00 00 
         00 
         02 gender
         01 showup
         02 mark
         00 
         00 00 
         00 00 
         00 00 
         00"
        
         61 48 37 01 
         FF FF 01 00 
        
         F1 E6 0F 00 
         01 00 
         46 00 00 00 
         FF 0C 00 00 
         00 
         00 
         00 00 
         00 00 00 00 FF 02 01 01 FF 00 00 00 00 00 00 00
         */
        int flags = item.flags;
        mplew.writeInt(item.sn);
        mplew.writeInt(flags);
        if ((flags & 0x1) != 0) {
            mplew.writeInt(item.itemid);
        }
        if ((flags & 0x2) != 0) {
            mplew.writeShort(item.count);
        }
        if ((flags & 0x4) != 0) {
            mplew.writeInt(item.discountPrice);
        }
        if ((flags & 0x8) != 0) {
            mplew.write(item.unk_1 - 1);
        }
        if ((flags & 0x10) != 0) {
            mplew.write(item.priority);
        }
        if ((flags & 0x20) != 0) {
            mplew.writeShort(item.period);
        }
        if ((flags & 0x40) != 0) {
            mplew.writeInt(0);
        }
        if ((flags & 0x80) != 0) {
            mplew.writeInt(item.meso);
        }
        if ((flags & 0x100) != 0) {
            mplew.write(item.unk_2 - 1);
        }
        if ((flags & 0x200) != 0) {
            mplew.write(item.gender);
        }
        if ((flags & 0x400) != 0) {
            mplew.write(item.showUp ? 1 : 0);
        }
        if ((flags & 0x800) != 0) {
            mplew.write(item.mark);
        }
        if ((flags & 0x1000) != 0) {
            mplew.write(item.unk_3 - 1);
        }
        if ((flags & 0x2000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x4000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x8000) != 0) {
            mplew.writeShort(0);
        }
        if ((flags & 0x10000) != 0) {
            List<CashItemInfo> pack = CashItemFactory.getInstance().getPackageItems(item.sn);
            if (pack == null) {
                mplew.write(0);
            } else {
                mplew.write(pack.size());
                for (int i = 0; i < pack.size(); i++) {
                    mplew.writeInt(pack.get(i).getSN());
                }
            }
        }
    }

    public static byte[] openCashShop(MapleClient c, boolean mts) throws Exception {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(mts ? SendOpcode.SET_ITC.getValue() : SendOpcode.SET_CASH_SHOP.getValue());

        PacketHelper.addCharacterInfo(mplew, c.getPlayer());

        /*if (!mts) {
         mplew.write(1);
         }*/
        mplew.writeMapleAsciiString(c.getAccountName());
        if (mts) {
            mplew.write(new byte[]{(byte) 0x88, 19, 0, 0, 7, 0, 0, 0, (byte) 0xF4, 1, 0, 0, (byte) 0x18, 0, 0, 0, (byte) 0xA8, 0, 0, 0, (byte) 0x70, (byte) 0xAA, (byte) 0xA7, (byte) 0xC5, (byte) 0x4E, (byte) 0xC1, (byte) 0xCA, 1});
        } else {
            List<CashItemInfo.CashModInfo> cmi = new ArrayList<>(CashItemFactory.getInstance().getAllModInfo());
            Set<Integer> items = CashItemFactory.getInstance().getAllItemSNs();
            Iterator<CashItemInfo.CashModInfo> it = cmi.iterator();

            //mplew.write(items.size());
            //for ( Integer sn : items)
            //mplew.writeInt(sn);
            mplew.writeInt(0); // some info , it'size , decodeBuffer(4*size)

            mplew.writeShort(cmi.size());
            for (int i = 0; i < cmi.size(); i++) {
                addModCashItemInfo(mplew, cmi.get(i));
                //CashModInfo m = it.next();
                //mplew.writeInt(m.sn);
            }

            mplew.write(HexTool.getByteArrayFromHexString("00 00 0A 00 50 10 27 00 00 00 5A 00 00 00 00 00 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 "));
            mplew.write(HexTool.getByteArrayFromHexString("06 00 00 00 31 00 30 00 31 00 00 00 00 00 00 00 05 00 0E 00 05 00 08 06 A0 01 14 00 C8 FE 8D 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 13 00 0A 01 0C 06 06 00 00 00 31 00 30 00 31 00 00 00 00 00 00 00 03 00 16 00 0D 00 0C 06 90 01 14 00 F8 36 8C 06 31 00 00 00 00 00 00 00 03 00 19 00 10 01 0C 06 06 00 00 00 31 00 30 00"));

            //mplew.write(HexTool.getByteArrayFromHexString("06 00 00 00 31 00 30 00 31 00 00 00 00 00 00 00 05 00 0E 00 05 00 08 06 A0 01 14 00 C8 FE 8D 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 13 00 0A 01 0C 06 06 00 00 00 31 00 30 00 31 00 00 00 00 00 00 00 03 00 16 00 0D 00 0C 06 90 01 14 00 F8 36 8C 06 31 00 00 00 00 00 00 00 03 00 19 00 10 01 0C 06 06 00 00 00 31 00 30 00"));
            for (int i = 1; i <= 8; i++) {
                for (int j = 0; j < 2; j++) {
                    mplew.writeInt(i);
                    mplew.writeInt(j);
                    mplew.writeInt(10000007);

                    mplew.writeInt(i);
                    mplew.writeInt(j);
                    mplew.writeInt(10000008);

                    mplew.writeInt(i);
                    mplew.writeInt(j);
                    mplew.writeInt(10000009);

                    mplew.writeInt(i);
                    mplew.writeInt(j);
                    mplew.writeInt(10000010);

                    mplew.writeInt(i);
                    mplew.writeInt(j);
                    mplew.writeInt(10000011);
                }
            }
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(0);
        }
        return mplew.getPacket();
    }

    public static byte[] enableCSUse() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CS_USE.getValue());

        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] showCashInventory(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x46);
        mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

        for (Item item : c.getPlayer().getCashShop().getInventory()) {
            addCashItemInformation(mplew, item, c.getAccID()); // 57 * size
        }

        mplew.writeShort(c.getPlayer().getStorage().getSlots());
        mplew.writeShort(c.getCharacterSlots());

        return mplew.getPacket();
    }

    /*
     CCashShop__NoticeFailReason
     166:	391	已超過工作時間。\r\n休息一下再繼續。
     168:	392	GASH 餘額不足。
     169:	393	未滿14歲的玩家不能\r\n贈送加值道具。
     170:	394	已超過可送禮物的限額。
     171:	4593	或無法送禮到相同的帳號！\r\n請利用該角色登入後購買。
     172:	4594	請確認是否為錯誤的角色名稱！
     173:	4595	此為有性別限制的道具！\r\n請確認收禮人的性別。
     174:	4596	收禮人的保管箱已滿！\r\n無法送出禮物。
     175:	395	請確認是否超過\r\n可以保有的加值道具數量。
     176:	396	請確認對方的伺服器、角色名稱是否正確；\r\n贈送的物品是否有性別限制，\r\n並請確認對方所擁有的加值道具\r\n是否已達上限。
     179:	404	此序號發生異常, 請洽客服人員。
     180:	406	此序號已被使用過！
     181:	406	此序號已被使用過！
     182:	401	只有在Premium 網咖上\r\n可以使用的會員卡。\r\n請在Premium 網咖上使用。
     183:	402	Premium 網咖專用會員卡\r\n已經使用過的會員卡。
     184:	403	Premium 網咖\r\n已經過期的會員卡。
     186:	398	你的性別無法使用這項道具。
     185:	2729	這是NexonCashCoupon號碼！\r\n請上Nexon.com(www.nexon.com)的\r\nMyPage>NexonCash>Menu中登錄Coupon號碼。
     187:	408	此優待券為專用道具。\r\n因此無法贈送。
     188:	3552	此優待券為楓葉點數專用！\r\n無法送禮給其他人。
     189:	634	請確認是否你的\r\n道具欄的空間不夠。
     190:	409	這種物品只在優秀會員網咖\r\n買得到。
     191:	410	戀人道具只能贈送給相同頻道的\r\n不同性別的角色。請確認\r\n你要送出禮物的角色\r\n在同一頻道且性別不同。
     192:	428	請你正確輸入要送禮物的角色名稱。
     193:	2103	UI/UIWindow.img/Memo/BtOpen
     194:	2104	關閉訊息窗，所有訊息都被消除\r\n 你要繼續進行嗎?
     195:	411	喀蝦議蓮 掘衙紫蒂 蟾婁艘蝗棲棻.
     196:	201	楓幣不足。
     197:	382	請確認第二組密碼\r\n再重試。
     198:	2730	渡 瓔擎 議衛嬴檜蠱 褐敘 掘殮濠虜\r\n餌辨檜 陛棟棲棻.
     199:	3076	已經報名
     205:	397	該道具已超過一日購買上限，\r\n無法繼續購買。
     208:	4397	該序號已超過Gash帳號使用上限！r\n詳細內容請參考序號說明！
     210:	5286	未滿７歲的玩家\r\n無法購買此道具。
     211:	5287	未滿７歲的玩家\r\n無法領取禮物。
     212:	3255	此序號不存在。
     213:	407	目前系統繁忙,請於一小時後再試。
     214:	2509	請先至楓之谷官網認證您的遊戲帳號，才能使用購物商場。
     215:	4691	必須要有折價券才能可以買該道具。
     216:	4604	限20級以上才能申請伺服器移民。
     217:	4607	無法移民到相同的伺服器！
     218:	4608	無法移民到最新開放的伺服器！
     219:	4609	若所要移民的伺服器中，已無多餘的角色欄位，\r\n是無法進行伺服器移民！
     220:	4629	無法將角色移民到您所指定的伺服器！
     221:	4850	暱稱或帳號讀取失敗！
     224:	5137	這道具是\r\n無法使用楓點購買。
     225:	5138	不好意思。\r\n請再試一次。
     def:	412	發生不明錯誤！\r\n購物商場使用失敗！
     */
    public static byte[] showCashInventoryFail(MapleClient c, int flag) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x47);
        mplew.write(flag);
        return mplew.getPacket();

    }

    public static byte[] showGifts(List<Pair<Item, String>> gifts) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x48);

        mplew.writeShort(gifts.size());

        for (Pair<Item, String> gift : gifts) {
            addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight()); // 101 * size;
        }

        return mplew.getPacket();
    }

    /*
     CCashShop__NoticeFailReason
     166:	391	已超過工作時間。\r\n休息一下再繼續。
     168:	392	GASH 餘額不足。
     169:	393	未滿14歲的玩家不能\r\n贈送加值道具。
     170:	394	已超過可送禮物的限額。
     171:	4593	或無法送禮到相同的帳號！\r\n請利用該角色登入後購買。
     172:	4594	請確認是否為錯誤的角色名稱！
     173:	4595	此為有性別限制的道具！\r\n請確認收禮人的性別。
     174:	4596	收禮人的保管箱已滿！\r\n無法送出禮物。
     175:	395	請確認是否超過\r\n可以保有的加值道具數量。
     176:	396	請確認對方的伺服器、角色名稱是否正確；\r\n贈送的物品是否有性別限制，\r\n並請確認對方所擁有的加值道具\r\n是否已達上限。
     179:	404	此序號發生異常, 請洽客服人員。
     180:	406	此序號已被使用過！
     181:	406	此序號已被使用過！
     182:	401	只有在Premium 網咖上\r\n可以使用的會員卡。\r\n請在Premium 網咖上使用。
     183:	402	Premium 網咖專用會員卡\r\n已經使用過的會員卡。
     184:	403	Premium 網咖\r\n已經過期的會員卡。
     186:	398	你的性別無法使用這項道具。
     185:	2729	這是NexonCashCoupon號碼！\r\n請上Nexon.com(www.nexon.com)的\r\nMyPage>NexonCash>Menu中登錄Coupon號碼。
     187:	408	此優待券為專用道具。\r\n因此無法贈送。
     188:	3552	此優待券為楓葉點數專用！\r\n無法送禮給其他人。
     189:	634	請確認是否你的\r\n道具欄的空間不夠。
     190:	409	這種物品只在優秀會員網咖\r\n買得到。
     191:	410	戀人道具只能贈送給相同頻道的\r\n不同性別的角色。請確認\r\n你要送出禮物的角色\r\n在同一頻道且性別不同。
     192:	428	請你正確輸入要送禮物的角色名稱。
     193:	2103	UI/UIWindow.img/Memo/BtOpen
     194:	2104	關閉訊息窗，所有訊息都被消除\r\n 你要繼續進行嗎?
     195:	411	喀蝦議蓮 掘衙紫蒂 蟾婁艘蝗棲棻.
     196:	201	楓幣不足。
     197:	382	請確認第二組密碼\r\n再重試。
     198:	2730	渡 瓔擎 議衛嬴檜蠱 褐敘 掘殮濠虜\r\n餌辨檜 陛棟棲棻.
     199:	3076	已經報名
     205:	397	該道具已超過一日購買上限，\r\n無法繼續購買。
     208:	4397	該序號已超過Gash帳號使用上限！r\n詳細內容請參考序號說明！
     210:	5286	未滿７歲的玩家\r\n無法購買此道具。
     211:	5287	未滿７歲的玩家\r\n無法領取禮物。
     212:	3255	此序號不存在。
     213:	407	目前系統繁忙,請於一小時後再試。
     214:	2509	請先至楓之谷官網認證您的遊戲帳號，才能使用購物商場。
     215:	4691	必須要有折價券才能可以買該道具。
     216:	4604	限20級以上才能申請伺服器移民。
     217:	4607	無法移民到相同的伺服器！
     218:	4608	無法移民到最新開放的伺服器！
     219:	4609	若所要移民的伺服器中，已無多餘的角色欄位，\r\n是無法進行伺服器移民！
     220:	4629	無法將角色移民到您所指定的伺服器！
     221:	4850	暱稱或帳號讀取失敗！
     224:	5137	這道具是\r\n無法使用楓點購買。
     225:	5138	不好意思。\r\n請再試一次。
     def:	412	發生不明錯誤！\r\n購物商場使用失敗！
     */
    public static byte[] sendShowGiftFail(MapleClient c, int flag) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x49);
        mplew.write(flag);
        return mplew.getPacket();

    }

    public static byte[] showWishList(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x4A);

        for (int sn : mc.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] sendShowWishListFail(MapleClient c, int flag) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x4B);
        mplew.write(flag);
        return mplew.getPacket();
    }

    public static byte[] setWishList(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x4C);

        for (int sn : mc.getCashShop().getWishList()) {
            mplew.writeInt(sn);
        }

        for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] sendSetWishListFail(MapleClient c, int flag) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x4D);
        mplew.write(flag);
        return mplew.getPacket();
    }

    public static byte[] showBoughtCashItem(Item item, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x4E);
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
    }

    public static byte[] sendShowBoughtCashItemFail(Item item, int flag, int page) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x4F);
        mplew.writeShort(flag);
        if (flag == 194 || flag == 193) {
            mplew.writeInt(page);
        }
        return mplew.getPacket();
    }

    public static byte[] showBoughtCashPackage(List<Item> cashPackage, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x80);
        mplew.write(cashPackage.size());

        for (Item item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }

        mplew.write(1); // 顯示買好了
        return mplew.getPacket();
    }

    public static byte[] sendShowBoughtCashPackageFail(Item item, int flag, int page) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x81);
        mplew.writeShort(flag);
        if (flag == 194 || flag == 193) {
            mplew.writeInt(page);
        }
        return mplew.getPacket();
    }

    public static byte[] sendGift(String to, CashItemInfo item, int gainMaplePoint, boolean isPackage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(isPackage ? 0x82 : 0x55);
        mplew.writeMapleAsciiString(to);
        mplew.writeInt(item.getId());
        mplew.writeShort(item.getCount());
        if (isPackage) {
            mplew.writeShort(gainMaplePoint);
        }

        return mplew.getPacket();
    }

    public static byte[] sendGiftFail(int flag, int page, boolean isPackage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(isPackage ? 0x83 : 0x56);
        mplew.writeShort(flag);
        if (flag == 194 || flag == 193) {
            mplew.writeInt(page);
        }
        return mplew.getPacket();
    }

    public static byte[] showCouponRedeemedItem(List<Item> cashPackage, List<Item> itemPackage, int maplePoints, int mesos, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x50);
        mplew.write(cashPackage.size());

        for (Item item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }

        mplew.writeInt(maplePoints);
        mplew.writeInt(itemPackage.size());
        for (Item item : itemPackage) {
            mplew.writeInt(item.getQuantity());
            mplew.writeInt(item.getItemId());
        }

        mplew.writeInt(mesos);

        return mplew.getPacket();
    }

    public static byte[] showCouponGifted(List<Item> cashPackage, List<Item> itemPackage, int maplePoints, int mesos, String toName, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x52);
        mplew.writeMapleAsciiString(toName);
        mplew.write(cashPackage.size());

        for (Item item : cashPackage) {
            addCashItemInformation(mplew, item, accountId);
        }

        mplew.writeInt(itemPackage.size());
        for (Item item : itemPackage) {
            mplew.writeInt(item.getQuantity());
            mplew.writeInt(item.getItemId());
        }
        return mplew.getPacket();
    }

    public static byte[] sendCouponFail(Item item, int flag, int page) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        mplew.write(0x53);
        mplew.writeShort(flag);
        return mplew.getPacket();
    }

    public static byte[] showCashShopAcc(MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(0x015F);
        mplew.write(1);
        mplew.writeMapleAsciiString(c.getAccountName());
        return mplew.getPacket();
    }

    public static byte[] showBoughtInventorySlots(int type, short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x57);
        mplew.write(type);
        mplew.writeShort(slots);

        //ORG
         //mplew.write(0x60);
        //mplew.write(type);
        //mplew.writeShort(slots);
        
        
        return mplew.getPacket();
    }

    public static byte[] showBoughtStorageSlots(short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x59);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] showBoughtCharacterSlot(short slots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x5B);
        mplew.writeShort(slots);

        return mplew.getPacket();
    }

    public static byte[] takeFromCashInventory(Item item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
        
        mplew.write(0x5F);
        mplew.writeShort(item.getPosition());
        MaplePacketCreator.addItemInfo(mplew, item, true);

        return mplew.getPacket();
    }

    public static byte[] putIntoCashInventory(Item item, int accountId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x61);
        addCashItemInformation(mplew, item, accountId);

        return mplew.getPacket();
    }

    public static byte[] showCash(MapleCharacter mc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.QUERY_CASH_RESULT.getValue());
        mc.getCashShop().updateCash();
        mplew.writeInt(mc.getCashShop().getCash(0));
        mplew.writeInt(mc.getCashShop().getCash(1));
        return mplew.getPacket();
    }

    public static byte[] showBoughtQuestItem(int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

        mplew.write(0x8D);
        mplew.writeInt(1);
        mplew.writeShort(1);
        mplew.write(0x0B);
        mplew.write(0);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /* MTS Packet*/
    public static byte[] sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x15); //operation
        mplew.writeInt(pages * 16); //testing, change to 10 if fails
        mplew.writeInt(items.size()); //number of items
        mplew.writeInt(tab);
        mplew.writeInt(type);
        mplew.writeInt(page);
        mplew.write(1);
        mplew.write(1);
        for (int i = 0; i < items.size(); i++) {
            MTSItemInfo item = items.get(i);
            addItemInfo(mplew, item.getItem(), true);
            mplew.writeInt(item.getID()); //id
            mplew.writeInt(item.getTaxes()); //this + below = price
            mplew.writeInt(item.getPrice()); //price
            mplew.writeInt(0);
            mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
            mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
            mplew.writeMapleAsciiString(item.getSeller()); //char name
            for (int j = 0; j < 28; j++) {
                mplew.write(0);
            }
        }
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showMTSCash(MapleCharacter p) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
        mplew.writeInt(p.getCashShop().getCash(1));
        return mplew.getPacket();
    }

    public static byte[] MTSWantedListingOver(int nx, int items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x3D);
        mplew.writeInt(nx);
        mplew.writeInt(items);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmSell() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x1D);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmBuy() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x33);
        return mplew.getPacket();
    }

    public static byte[] MTSFailBuy() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x34);
        mplew.write(0x42);
        return mplew.getPacket();
    }

    public static byte[] MTSConfirmTransfer(int quantity, int pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x27);
        mplew.writeInt(quantity);
        mplew.writeInt(pos);
        return mplew.getPacket();
    }

    public static byte[] notYetSoldInv(List<MTSItemInfo> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x23);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //this + below = price
                mplew.writeInt(item.getPrice()); //price
                mplew.writeInt(0);
                mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] transferInventory(List<MTSItemInfo> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
        mplew.write(0x21);
        mplew.writeInt(items.size());
        if (!items.isEmpty()) {
            for (MTSItemInfo item : items) {
                addItemInfo(mplew, item.getItem(), true);
                mplew.writeInt(item.getID()); //id
                mplew.writeInt(item.getTaxes()); //taxes
                mplew.writeInt(item.getPrice()); //price
                mplew.writeInt(0);
                mplew.writeLong(PacketUtil.getTime(item.getEndingDate()));
                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                mplew.writeMapleAsciiString(item.getSeller()); //char name
                for (int i = 0; i < 28; i++) {
                    mplew.write(0);
                }
            }
        }
        mplew.write(0xD0 + items.size());
        mplew.write(new byte[]{-1, -1, -1, 0});
        return mplew.getPacket();
    }
}
