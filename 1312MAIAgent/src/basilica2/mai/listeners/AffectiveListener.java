package basilica2.mai.listeners;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
 
import edu.cmu.cs.lti.basilica2.core.Event;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.components.OutputCoordinator;
import basilica2.agents.events.MAITriggerEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.priority.PriorityEvent;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.agents.components.StateMemory;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.project911.utils.log.Logger;
import basilica2.agents.data.RollingWindow;
 
public class AffectiveListener implements BasilicaPreProcessor {
 
	// Configure RollingWindow history size + purge interval
	protected static final int HISTORY_WINDOW = 300; // define history window
	
    private static final String TRIGGER_NAME = "AFFECTIVE";

	private static long lastFiredTime = 0;
	private static final long LISTENER_COOLDOWN_MS = 180000; // 3 minutes
 
	public AffectiveListener(Agent a) {
		RollingWindow.sharedWindow().setWindowSize(HISTORY_WINDOW, 2);
		RollingWindow.sharedWindow().purge(0); //Clears stale
	}
 
	/**
	 * Preprocess an incoming event, by modifying this event or creating a new event in response. 
	 * All original and new events will be passed by the InputCoordinator to the second-stage Reactors ("BasilicaListener" instances).
	 */
	@Override
	public void preProcessEvent(InputCoordinator source, Event event)
	{
		if (!(event instanceof MessageEvent)) {
            return;
        }
		MessageEvent me = (MessageEvent)event;
		
		// No change needed here - NE_L check is correct
		if (!me.hasAnnotations("NE_L"))
			return;
		
		// Add to rolling window
		RollingWindow.sharedWindow().addEvent(event, "NE_L");
 
		// CHANGE: Only fire trigger when NE_L has occurred 3+ times in 5 minutes
		if (RollingWindow.sharedWindow().countAnyEvents(HISTORY_WINDOW, "NE_L") >= 3)
		{

			// Hard cooldown check - don't fire again for 3 minutes
			long now = System.currentTimeMillis();
			if (now - lastFiredTime < LISTENER_COOLDOWN_MS) {
				Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL, "COGNITIVE cooldown active, skipping");
				return;
			}
			lastFiredTime = now;

			Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL, "TRIGGER FIRED: AFFECTIVE");
 
			// CHANGE: MAITriggerEvent is now inside the count check, not outside it
			// MAITriggerEvent MAITriggerEvent = new MAITriggerEvent(source, TRIGGER_NAME);
			//System.err.println("AffectiveListener, execute - MAITriggerEvent created");
			// Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL, "AffectiveListener, execute - MAITriggerEvent created");
			// source.pushProposal(PriorityEvent.makeBlackoutEvent("macro", "MAITriggerEvent", MAITriggerEvent, OutputCoordinator.HIGH_PRIORITY, 5.0, 2));
 
            MessageEvent triggerMsg = new MessageEvent(source, "MAI_LISTENER", TRIGGER_NAME, "AFFECTIVE");
			triggerMsg.addAnnotation("AFFECTIVE", Arrays.asList("AFFECTIVE"));
			System.out.println("Affective trigger msg that is sent to MAI Actor: " + triggerMsg.toString());
			for (int i=0; i<triggerMsg.getAllAnnotations().length; i++) {
	    		System.out.println("Extracting affective trigger msg annotations: " + triggerMsg.getAllAnnotations()[i]);
	    	}
			source.addPreprocessedEvent(triggerMsg);
			RollingWindow.sharedWindow().purge(0); // Reset window after firing
		}
	}
	
	/**
	 * @return the classes of events that this Preprocessor cares about
	 */
	@Override
	public Class[] getPreprocessorEventClasses()
	{
		//only MessageEvents will be delivered to this watcher.
		return new Class[]{MessageEvent.class};
	}
}