package client;

public class MapleFamilyEntry {

    private int familyId;
    private int rank, reputation, totalReputation, todaysRep, totalJuniors, juniors, chrid;
    private String familyName;

    public int getId() {
        return familyId;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getChrId() {
        return chrid;
    }

    public void setChrId(int chrid) {
        this.chrid = chrid;
    }

    public int getReputation() {
        return reputation;
    }

    public int getTodaysRep() {
        return todaysRep;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void setTodaysRep(int today) {
        this.todaysRep = today;
    }

    public void gainReputation(int gain) {
        this.reputation += gain;
        this.totalReputation += gain;
    }

    public int getTotalJuniors() {
        return totalJuniors;
    }

    public void setTotalJuniors(int totalJuniors) {
        this.totalJuniors = totalJuniors;
    }

    public int getJuniors() {
        return juniors;
    }

    public void setJuniors(int juniors) {
        this.juniors = juniors;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getTotalReputation() {
        return totalReputation;
    }

    public void setTotalReputation(int totalReputation) {
        this.totalReputation = totalReputation;
    }
}
