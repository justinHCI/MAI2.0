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



public class MetacognitiveListener implements BasilicaPreProcessor {


	// Configure RollingWindow history size + purge interval

	protected static final int HISTORY_WINDOW = 300; // define history window
	
    //private static final int TRIGGER_THRESHOLD = 3;
    private static final String TRIGGER_NAME = "METACOGNITIVE";
    //private static final double PRIORITY = 2.0; // Priority level
	
	// test comment for eclipse

	public MetacognitiveListener(Agent a) {
		RollingWindow.sharedWindow().setWindowSize(HISTORY_WINDOW, 2);
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
		
		
		// this was made according to the logs, but could it also work as "!me.hasAnnotations("DOM", "CON")
//		if (!me.hasAnnotations("DOM") || (!me.hasAnnotations("COO")))
	//		return;
		
//		if (!me.hasAnnotations("DOM") || (!me.hasAnnotations("COO")))
//			return;
		if (!me.hasAnnotations("COO_L"))
			return;
		
		// Add to rolling window
		//RollingWindow.sharedWindow().addEvent(me, "DOM+COO");
		RollingWindow.sharedWindow().addEvent(me, "COO_L");
		// if DOM+COO has been identified more than 3 times in the last 5 minutes
		Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL, "Metacognitive Event added");
		
		
		MAITriggerEvent MAITriggerEvent = new MAITriggerEvent(source, TRIGGER_NAME);
      	System.err.println("MetacognitiveListener, execute - MAITriggerEvent created");
      	Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"MetacognitiveListener, execute - MAITriggerEvent created");
		source.pushProposal(PriorityEvent.makeBlackoutEvent("macro", "MAITriggerEvent", MAITriggerEvent, OutputCoordinator.HIGH_PRIORITY, 5.0, 2));
		//returns a count of events occurring in the last secondsAgo seconds matching ALL keys
		
		//if (RollingWindow.sharedWindow().countAnyEvents(HISTORY_WINDOW, "DOM+COO") >= 3)
		if (RollingWindow.sharedWindow().countAnyEvents(HISTORY_WINDOW, "COO_L") >= 3)
		{
			// Then propose a cognitive trigger
			 Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL,
                "TRIGGER FIRED: METACOGNITIVE");
            
            MessageEvent triggerMsg = new MessageEvent(source, "MAI_LISTENER", TRIGGER_NAME, "METACOGNITIVE");
            
            // add an annotation to the message for "metacognitive"
            triggerMsg.addAnnotation("METACOGNITIVE", Arrays.asList("METACOGNITIVE"));
            System.out.println("Metacognitive trigger msg that is sent to MAI Actor: " + triggerMsg.toString());
			for (int i=0; i<triggerMsg.getAllAnnotations().length; i++) {
	    		System.out.println("Extracting metacognitive trigger msg annotations: " + triggerMsg.getAllAnnotations()[i]);
	    	}
			
            source.addPreprocessedEvent(triggerMsg);
			
			
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
