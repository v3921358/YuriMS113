/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 *
 * @author Administrator
 */
public class NxcodeCreate {

    private boolean validcode = false;

    public static void main(String args[]) {
        Console con = System.console();

        Scanner scn = new Scanner(System.in);
        System.out.println("--------商城序號產生器--------");
        int i = 0;
        while (true) {
            try {
                System.out.println("請選擇要產生的種類, 1--點數, 2--物品");
                i = Integer.parseInt(scn.next());
                System.out.println();
                break;
            } catch (Exception e) {
                //System.err.println(e);
            }
        }

        switch (i) {
            case 1:
                creat_Cash(scn);
                break;
            case 2:
                creat_Item(scn);
                break;
        }

        //getNXCodeValid();
    }

    public static void creat_Cash(Scanner scn) {
        int n = 0;
        String s = "";
        while (true) {
            System.out.println("Serial Number Quantity?  (max 5)");
            s = scn.next();
            if (isDigi(s)) {
                n = Integer.parseInt(s);
                if (n < 1 || n > 5) {
                    continue;
                }
                break;
            }

        }

        int i = 1;
        int cash = 0;
        String code;

        while (i <= n) {
            int type = 0;
            System.out.println("Please select type, type 0--nxCredit, type 1--nxPrepaid");
            s = scn.next();
            if (isDigi(s)) {
                type = Integer.parseInt(s);
                if (type != 0 && type != 1) {
                    continue;
                }
            }

            System.out.println("Serial Number " + i + " Cash amount ?");
            s = scn.next();
            if (isDigi(s)) {
                cash = Integer.parseInt(s);
                code = createCode(20);
                if (upload_data(code, type, cash)) {
                    System.out.println();
                    System.out.println(code + "　　CASH：" + cash);
                    i++;
                }
            }
        }
    }

    public static boolean upload_data(String code, int type, int item) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO nxcode (code,valid, type, item) VALUES (?,1,?,?)");
            ps.setString(1, code);
            ps.setInt(2, type);
            ps.setInt(3, item);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public static String createCode(int num) {
        int n = 0, l = 0, u = 0, i = 0;
        String t = "";
        Double m = Math.floor(num / 4);
        while (true) {
            int r = randint(1, 4);
            if (r == 1 && (n < m || i > 0)) {
                n++;
                t += (char) randint(48, 57);
            }
            if (r == 2 && (l < m || i > 0)) {
                l++;
                t += (char) randint(97, 122);
            }
            if (r == 3 && (u < m || i > 0)) {
                u++;
                t += (char) randint(65, 90);
            }
            if (n + l + u >= m * 3) {
                i++;
            }
            if (n + l + u >= num) {
                break;
            }
        }

        return t;
    }

    public static int randint(int min, int max) {
        Double num = Math.floor(Math.random() * (max - min + 1) + min);
        return num.intValue();
    }

    public static void creat_Item(Scanner scn) {
        int n = 0;
        String s = "";
        while (true) {
            System.out.println("Serial Number Quantity?  (max 5)");
            s = scn.next();
            if (isDigi(s)) {
                n = Integer.parseInt(s);
                if (n < 1 || n > 5) {
                    continue;
                }
                break;
            }

        }

        int i = 1;
        int cash = 0;
        String code;

        while (i <= n) {
            System.out.println();
            System.out.println("Serial Number " + i + " Item ID ?");
            s = scn.next();
            if (isDigi(s)) {
                cash = Integer.parseInt(s);
                code = createCode(20);
                if (upload_data(code, 2, cash)) {
                    System.out.println(code + "　　ITEM ID：" + cash);
                    i++;
                }
            }
        }

    }

    public static boolean getNXCodeValid(String code) {
        boolean inSQL = false;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `code` FROM nxcode");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("code").equals(code)) {
                    inSQL = true;
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return inSQL;
    }

    public static boolean isDigi(String s) {
        try {
            int a = Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
