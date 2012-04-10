package distributedLanguage;

public abstract class RoCoRole extends Entity {

    public static final RoCoRole NONE = null;

    @Override public abstract boolean isPrimaryRole();
 
    public RoCoRole(RoCoProgram program) { super(program); }
}
