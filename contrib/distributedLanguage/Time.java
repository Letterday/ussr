package distributedLanguage;

public class Time {

    private long time;

    public Time(long t) {
        this.time = t;
    }
    
    public long difference(Time updateTime) {
        return updateTime.time - time;
    }

}
