package basilica2.agents.events;

import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;

/**
 * MAITriggerEvent
 */
public class MAITriggerEvent extends Event {
    
    private String triggerName = null;

    
    public MAITriggerEvent(Component source, String triggerName) {
        super(source);
        this.triggerName = triggerName; 
        System.err.println("MAITriggerEvent, constructor - MAITriggerEvent created: triggerName: " + triggerName);
        Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"MAITriggerEvent, constructor - MAITriggerEvent created: triggerName: " + triggerName);
    }
    
    public String getTriggerName() {
        System.err.println("MAITriggerEvent, getTriggerName: " + triggerName);
        Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"MAITriggerEvent, getTriggerName: " + triggerName);
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
        System.err.println("MAITriggerEvent, setTriggerName: " + triggerName);
        Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"MAITriggerEvent, setTriggerName: " + triggerName);
    }
    
    @Override
    public String getName() {
        return "MAI_TRIGGER_" + triggerName;
    }

    @Override
    public String toString() {
        return "MAITriggerEvent{name=" + getName() + "}";
    }
}
