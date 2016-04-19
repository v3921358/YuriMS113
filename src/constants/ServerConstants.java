package constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConstants {

    public static short VERSION = 113;
    public static short PATCH = 1;
    public static String[] WORLD_NAMES = {
        "雪吉拉", "菇菇寶貝", "星光精靈", "緞帶肥肥", "藍寶", "綠水靈", "三眼章魚", "木妖", "火獨眼獸", "蝴蝶精",
        "巴洛古", "海怒斯", "電擊象", "鯨魚號", "皮卡啾", "神獸", "泰勒熊"};

    // 資料庫設定
    public static String DB_HOST;
    public static String DB_PORT;
    public static String DB_NAME;
    public static String DB_USER;
    public static String DB_PASS;

    public static String HOST_IP;
    public static int HOST_PORT;
    public static String CHANNEL_IP;
    public static int CHANNEL_PORT;

    public static boolean JAVA_8;

    public static final boolean PERFECT_PITCH = false;

    static {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("config.ini"));

            //SERVER
            ServerConstants.HOST_IP = p.getProperty("HOST");
            ServerConstants.HOST_PORT = Integer.parseInt(p.getProperty("HOST_PORT"));
            ServerConstants.CHANNEL_IP = p.getProperty("CHANNEL");
            ServerConstants.CHANNEL_PORT = Integer.parseInt(p.getProperty("CHANNEL_PORT"));

            //SQL DATABASE
            ServerConstants.DB_HOST = p.getProperty("DB_HOST");
            ServerConstants.DB_PORT = p.getProperty("DB_PORT");
            ServerConstants.DB_NAME = p.getProperty("DB_NAME");
            ServerConstants.DB_USER = p.getProperty("DB_USER");
            ServerConstants.DB_PASS = p.getProperty("DB_PASS");

            //OTHER
            ServerConstants.JAVA_8 = p.getProperty("JAVA8").equalsIgnoreCase("true");

        } catch (IOException e) {
            System.out.println("Failed to load config.ini.");
            System.err.println(e);
            System.exit(0);
        }
    }
}
