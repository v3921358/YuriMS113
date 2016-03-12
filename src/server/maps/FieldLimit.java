package server.maps;

public enum FieldLimit {

    Jump(0x01),
    MovementsSkills(0x02),
    SummoningBag(0x04),
    MysticDoor(0x08),
    ChannelSwitch(0x10),
    RegularExploss(0x20),
    VipRock(0x40),
    Minigames(0x80),
    NoClue1(0x100), // APQ and a couple quest maps have this
    Mount(0x200),
    PotionUse(0x400), // Monster carnival?
    //NoClue3(0x800), // Monster carnival?
    PotionUse2(0x1000),
    Event(0x2000),
    //Unused(0x4000),
    PEet(0x8000), // Ariant colosseum-related?
    //NoClue6(0x10000), // No notes
    Event2(0x10000),
    DropDown(0x20000);
    //NoClue7(0x40000); // Seems to .. disable Rush if 0x2 is set
    private long i;

    private FieldLimit(long i) {
        this.i = i;
    }

    public long getValue() {
        return i;
    }

    public boolean check(int fieldlimit) {
        return (fieldlimit & i) == i;
    }
}
