/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.packets;

import client.MapleStat;
import java.util.Collections;
import java.util.List;
import tools.Pair;

/**
 *
 * @author flyx
 */
public class PacketUtil {

    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
    public final static long FT_UT_OFFSET = 116444592000000000L; // EDT
    public final static long DEFAULT_TIME = 150842304000000000L;//00 80 05 BB 46 E6 17 02
    public final static long ZERO_TIME = 94354848000000000L;//00 40 E0 FD 3B 37 4F 01
    public final static long PERMANENT = 150841440000000000L; // 00 C0 9B 90 7D E5 17 02

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1) {
            return DEFAULT_TIME;//high number ll
        } else if (realTimestamp == -2) {
            return ZERO_TIME;
        } else if (realTimestamp == -3) {
            return PERMANENT;
        }
        return realTimestamp * 10000 + FT_UT_OFFSET;
    }
}
