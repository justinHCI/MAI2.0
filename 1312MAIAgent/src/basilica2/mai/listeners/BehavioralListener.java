package basilica2.mai.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.lti.basilica2.core.Event;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.events.MAITriggerEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.PresenceEvent;
import basilica2.agents.listeners.BasilicaPreProcessor;
import basilica2.mai.listeners.MAIActivityTracker;
import basilica2.agents.listeners.BasilicaAdapter;
import edu.cmu.cs.lti.project911.utils.time.TimeoutReceiver;
import basilica2.agents.components.StateMemory;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.project911.utils.log.Logger;
import basilica2.social.events.DormantStudentEvent;

import basilica2.agents.data.RollingWindow;

//Dormant students and dormant groups are tracked by ActivityTracker.java,
// which is one of a suite of listeners under SocialAgent. 
// It creates DormantStudentEvent and DormantGroupEvent, 
// to which actors or other listeners can respond. SocialAgent also has a lot of other interesting code under src/
// , including some that react to dormancy events, some that compute and deal with strategy, 
// and some interesting feature detectors under basilica2.social.utilities. 


public class BehavioralListener implements BasilicaPreProcessor {

    private static final String TRIGGER_NAME = "BEHAVIORAL";

    public BehavioralListener(Agent a) {
	RollingWindow.sharedWindow().purge(0); // Clears stale
        // nothing needed here now
    }

    @Override
    public void preProcessEvent(InputCoordinator source, Event event) {

        // 🎯 Only react to dormancy events
        if (!(event instanceof DormantStudentEvent)) {
            return;
        }

        DormantStudentEvent dse = (DormantStudentEvent) event;
        String student = dse.getStudentName();

        Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL,
                "Behavioral trigger – student dormant: " + student);

        // 🔥 Create trigger event
        MessageEvent triggerMsg = new MessageEvent(
                source,
                "MAI_LISTENER",
                TRIGGER_NAME,
                "BEHAVIORAL"
        );

        triggerMsg.addAnnotation("BEHAVIORAL_TRIGGER", Arrays.asList("BEHAVIORAL"));
        triggerMsg.addAnnotation("STUDENT", Arrays.asList(student));

        source.addPreprocessedEvent(triggerMsg);
    }

    @Override
    public Class[] getPreprocessorEventClasses() {
        return new Class[]{DormantStudentEvent.class};
    }
}


//public class BehavioralListener implements BasilicaPreProcessor {

/*
	// Configure RollingWindow history size + purge interval

	//protected static final int HISTORY_WINDOW = 300; // define history window
    private static final String TRIGGER_NAME = "BEHAVIORAL";
	private MAIActivityTracker at;

	public BehavioralListener(Agent a) {
		//RollingWindow.sharedWindow().setWindowSize(HISTORY_WINDOW, 2);
		at = new MAIActivityTracker(a);
	}
	
	// we need the behavioral listener to take info from the presence watcher and calculate messages per student


	@Override
	public void preProcessEvent(InputCoordinator source, Event event)
	{
		// is this needed? idk
		if (!(event instanceof MessageEvent)) {
            return;
        }
		MessageEvent me = (MessageEvent)event;

		// forward event to activity tracker so it updates its internal counts
        at.preProcessEvent(source, event);
        
        // now ask it whether any student just went dormant
        if (at.anyoneDormant()) {
			Logger.commonLog(getClass().getSimpleName(), Logger.LOG_NORMAL,
                    "behavioural trigger – somebody has gone idle");

			// trigger a MAITriggerEvent with the name "BEHAVIORAL" and the type "DORMANCY"
			MessageEvent triggerMsg = new MessageEvent(source, "MAI_LISTENER", TRIGGER_NAME, "BEHAVIORAL_TRIGGER");
            
			triggerMsg.addAnnotation("AFFECTIVE_TRIGGER", Arrays.asList("BEHAVIORAL"));
			source.addPreprocessedEvent(triggerMsg);
		}

        //// or get list of dormant students and trigger.
		
	}

	*/
	
	
//	/**
//	 * @return the classes of events that this Preprocessor cares about
//	 */
//	@Override
//	public Class[] getPreprocessorEventClasses()
//	{
//		//MessageEvents and PresenceEvents will be delivered to this watcher.
//		return new Class[]{MessageEvent.class, PresenceEvent.class};
//	}

//}
