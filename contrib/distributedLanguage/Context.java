package distributedLanguage;

import java.util.Map;

import distributedLanguage.ContextManager.ContextInformation;

public class Context {
    private Map<ModuleID, ContextInformation> context;
    
    public Context(Map<ModuleID, ContextInformation> context) {
        this.context = context;
    }
    
    public ContextInformation get(ModuleID id) {
        return context.get(id);
    }
}
