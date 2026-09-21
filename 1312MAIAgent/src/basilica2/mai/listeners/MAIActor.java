package basilica2.mai.listeners;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import basilica2.agents.components.InputCoordinator;
import basilica2.agents.components.OutputCoordinator;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.priority.PriorityEvent;
import basilica2.agents.listeners.BasilicaListener;


/* 
 * 	1. Receives triggers from multiple listeners
	2. Stores them in a queue
	3. Waits 2 seconds (decision window)
	4. Removes expired triggers
	5. Applies cooldown
	6. Selects highest priority trigger
	7. Sends it to OutputCoordinator
 * 
 * */

public class MAIActor implements BasilicaListener
{

	// Queue storing trigger events arriving within the decision window
	ConcurrentLinkedQueue<MessageEvent> q = new ConcurrentLinkedQueue<>();

	// Tracks last time each trigger type was fired (for cooldown)
	private Map<String,Long> lastFireTime = new HashMap<>();

	// Cooldown period: once a trigger fires, it cannot fire again for 3 minutes
	private static final long COOLDOWN_MS = 180000;
	// Global last fire time - blocks ALL triggers after any one fires
	//private long globalLastFireTime = 0L;

	// Time window to wait and collect multiple competing triggers
	private static final long DECISION_WAIT_MS = 2000;

	// Any trigger older than this will be ignored when deciding
	private static final long TRIGGER_EXPIRE_MS = 5000;

	// Ensures only one decision timer runs at a time
	private boolean timerRunning = false;

	// Scheduler used to delay decision-making
	private ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor();

	// Priority ranking (lower number = higher priority)
	private static final Map<String,Integer> PRIORITY = new HashMap<String,Integer>() {{
    	put("METACOGNITIVE",1);
		put("COGNITIVE",2);
		put("BEHAVIORAL",3);
   		put("AFFECTIVE",4);
		put("SOCIAL",5);
	}};

	// Map trigger types to audio categories (used elsewhere in system)
	private static final Map<String, String> TRIGGER_TO_MOVE = new HashMap<String, String>() {{
		put("METACOGNITIVE", "metacognitive");
		put("COGNITIVE", "cognitive");
		put("BEHAVIORAL", "behavioral");
		put("AFFECTIVE", "socio_emotional");
		put("SOCIAL", "shared_perspective");
	}};

	// Constructor: initialize cooldown tracking
	public MAIActor(Agent a) {
    	for(String k : PRIORITY.keySet())
			lastFireTime.put(k, 0L);
	}

	public MAIActor(Agent a, String n, String pf)
	{
	    this(a);
	}

	public MAIActor() {
	    for(String k : PRIORITY.keySet())
			lastFireTime.put(k, 0L);
	}

	@Override
	public void processEvent(InputCoordinator source, Event event)
	{
		// Only process MessageEvents
		if(!(event instanceof MessageEvent)) return;

    	MessageEvent me = (MessageEvent) event;
    	
    	// Debug: print all annotations on the message
    	for (int i=0; i<me.getAllAnnotations().length; i++) {
    		System.out.println("Extracting trigger type from message event: " + me.getAllAnnotations()[i]);
    	}

    	// Extract trigger type from annotations (assumes first annotation is the trigger)
		String triggerType = me.getAllAnnotations()[0];

		// Ignore messages that are not valid trigger types
		if(!PRIORITY.containsKey(triggerType))
			return;

    	System.out.println("MAIActor received trigger: " + triggerType);

    	// Add event to queue (to compete with other triggers)
    	q.add(me);

    	// If this is the first trigger in the queue, start decision timer
    	if (!timerRunning) {

    		timerRunning = true;

    		// After DECISION_WAIT_MS, decide which trigger to fire
    		scheduler.schedule(() -> decideTrigger(source),
    				DECISION_WAIT_MS,
    				TimeUnit.MILLISECONDS);
    	}
	}

	// Called after the decision window expires
	private void decideTrigger(InputCoordinator source)
	{
		long now = System.currentTimeMillis();

		MessageEvent best = null;
		int bestPriority = Integer.MAX_VALUE;

		// Iterate through all queued triggers
		for(MessageEvent e : q)
		{
			// Skip triggers that are too old
			if(now - e.getTimestamp() > TRIGGER_EXPIRE_MS)
				continue;

			// Extract trigger type (NOTE: currently using text here)
			String triggerType = e.getAllAnnotations()[0];

			// Get priority of this trigger
			int p = PRIORITY.getOrDefault(triggerType, Integer.MAX_VALUE);

			// Get last time this trigger type fired
			long lastFire = lastFireTime.getOrDefault(triggerType, 0L);

			// Enforce cooldown (skip if fired too recently)
			if(now - lastFire < COOLDOWN_MS)
				continue;

			// Select highest priority trigger (lowest number wins)
			if(p < bestPriority)
			{
				bestPriority = p;
				best = e;
			}
		}

		// If a valid trigger was found, fire it
		if(best != null)
		{
			fireTrigger(source, best);
		}

		// Clear queue for next cycle
		q.clear();
		timerRunning = false;
	}

	// Sends the selected trigger forward to the OutputCoordinator
/*	private void fireTrigger(InputCoordinator source, MessageEvent e)
	{
		String triggerType = e.getAllAnnotations()[0];

		System.out.println("MAIActor firing trigger: " + triggerType);

		// Update cooldown timestamp
		lastFireTime.put(triggerType, System.currentTimeMillis());
		globalLastFireTime = System.currentTimeMillis();

		// Send event with high priority to OutputCoordinator
		source.pushProposal(
			PriorityEvent.makeBlackoutEvent(
				"macro",
				"Final trigger event",
				e,
				OutputCoordinator.HIGH_PRIORITY,
				5.0,
				2
			)
		);
	}
*/
	private void fireTrigger(InputCoordinator source, MessageEvent e)
	{
		String triggerType = e.getAllAnnotations()[0];

		System.out.println("MAIActor firing trigger: " + triggerType);

		// Update cooldown timestamps
		lastFireTime.put(triggerType, System.currentTimeMillis());
		//globalLastFireTime = System.currentTimeMillis();

		// Flush queue immediately
		q.clear();
		timerRunning = false;

		// Send event with high priority to OutputCoordinator
		source.pushProposal(
			PriorityEvent.makeBlackoutEvent(
				"macro",
				"Final trigger event",
				e,
				OutputCoordinator.HIGH_PRIORITY,
				5.0,
				2
			)
		);
	}
	
	
	
	@Override
	public Class[] getListenerEventClasses()
	{
		// This listener reacts to MessageEvents only
		return new Class[]{MessageEvent.class};
	}

}
