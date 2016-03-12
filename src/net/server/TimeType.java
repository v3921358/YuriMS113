/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server;

/**
 *
 * @author flyx
 */
public enum TimeType {

    HOUR1(0x1),
    HOUR2(0x2),
    HOUR3(0x4),
    HOUR4(0x8),
    HOUR5(0x10),
    HOUR6(0x20),
    HOUR7(0x40),
    HOUR8(0x80),
    HOUR9(0x100),
    HOUR10(0x200),
    HOUR11(0x400),
    HOUR12(0x800),
    HOUR13(0x1000),
    HOUR14(0x2000),
    HOUR15(0x4000),
    HOUR16(0x8000),
    HOUR17(0x10000),
    HOUR18(0x20000),
    HOUR19(0x40000),
    HOUR20(0x80000),
    HOUR21(0x100000),
    HOUR22(0x200000),
    HOUR23(0x400000),
    HOUR0(0x800000),
    MIDNIGHT(HOUR0.getValue() | HOUR1.getValue() | HOUR2.getValue() | HOUR3.getValue() | HOUR4.getValue() | HOUR5.getValue()),
    MORNING(HOUR6.getValue() | HOUR7.getValue() | HOUR8.getValue() | HOUR9.getValue() | HOUR10.getValue() | HOUR11.getValue()),
    AFTERNOON(HOUR12.getValue() | HOUR13.getValue() | HOUR14.getValue() | HOUR16.getValue() | HOUR17.getValue() | HOUR17.getValue()),
    NIGHT(HOUR18.getValue() | HOUR19.getValue() | HOUR20.getValue() | HOUR21.getValue() | HOUR22.getValue() | HOUR23.getValue());

    private int value = 0;

    private TimeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
