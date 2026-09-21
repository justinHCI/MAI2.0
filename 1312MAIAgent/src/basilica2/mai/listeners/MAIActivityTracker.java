/*
 *  Copyright (c), 2009 Carnegie Mellon University.
 *  All rights reserved.
 *  
 *  Use in source and binary forms, with or without modifications, are permitted
 *  provided that that following conditions are met:
 *  
 *  1. Source code must retain the above copyright notice, this list of
 *  conditions and the following disclaimer.
 *  
 *  2. Binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or
 *  other materials provided with the distribution.
 *  
 *  Permission to redistribute source and binary forms, with or without
 *  modifications, for any purpose must be obtained from the authors.
 *  Contact Rohit Kumar (rohitk@cs.cmu.edu) for such permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND
 *  ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 *  NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package basilica2.mai.listeners;

import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.basilica2.core.Component;
import edu.cmu.cs.lti.basilica2.core.Event;
import edu.cmu.cs.lti.project911.utils.log.Logger;
import edu.cmu.cs.lti.project911.utils.time.TimeoutReceiver;
import edu.cmu.cs.lti.project911.utils.time.Timer;
import basilica2.agents.components.InputCoordinator;
import basilica2.agents.components.StateMemory;
import basilica2.agents.data.State;
// import basilica2.social.events.DormantGroupEvent;
import basilica2.social.events.DormantStudentEvent;
import basilica2.agents.events.LaunchEvent;
import basilica2.agents.events.MessageEvent;
import basilica2.agents.events.PresenceEvent;
import basilica2.agents.listeners.BasilicaAdapter;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class MAIActivityTracker extends BasilicaAdapter implements TimeoutReceiver {

    private Map<String, Integer> messageCounts = new HashMap<>();
    private int totalMessages = 0;

    private InputCoordinator source;

    private double activityPulseMinutes = 1; // check every 3 minutes
    private int groupActivityThreshold = 0;  // same idea as original
    private boolean isTracking = false;

    public MAIActivityTracker(Agent a) {
        super(a);
    }

    // 🚀 Start tracking when session launches
    private void startTracking() {
        State s = StateMemory.getSharedState(agent);
        String[] sids = s.getStudentIds();

        messageCounts.clear();
        for (String sid : sids) {
            messageCounts.put(sid, 0);
        }

        totalMessages = 0;
        isTracking = true;

        Timer t = new Timer(activityPulseMinutes * 60, this);
        t.start();
    }

    private void handleLaunchEvent(LaunchEvent le) {
        startTracking();
    }

    private void handleMessageEvent(MessageEvent me) {
        if (!isTracking) {
            startTracking();
        }

        String from = me.getFrom();

        // ensure student exists
        if (!messageCounts.containsKey(from)) {
            messageCounts.put(from, 0);
        }

        messageCounts.put(from, messageCounts.get(from) + 1);
        totalMessages++;
    }

    @Override
    public void timedOut(String id) {
        if (messageCounts.isEmpty()) return;

        String[] participants = messageCounts.keySet().toArray(new String[0]);

        int maxCount = Integer.MIN_VALUE;
        int minCount = Integer.MAX_VALUE;
        String minStudent = null;

        for (String student : participants) {
            int count = messageCounts.get(student);

            if (count > maxCount) {
                maxCount = count;
            }

            if (count < minCount) {
                minCount = count;
                minStudent = student;
            }
        }

        // 🧠 Relative dormancy condition
        if (totalMessages > groupActivityThreshold && minStudent != null) {

            if ((minCount * 2) < maxCount) {
                DormantStudentEvent dse = new DormantStudentEvent(source, minStudent);
                source.queueNewEvent(dse);
            }
        }

        // 🔄 reset for next cycle
        startTracking();
    }

    @Override
    public void preProcessEvent(InputCoordinator source, Event event) {
        this.source = source;

        if (event instanceof LaunchEvent) {
            handleLaunchEvent((LaunchEvent) event);
        } else if (event instanceof MessageEvent) {
            handleMessageEvent((MessageEvent) event);
        }
    }

    @Override
    public void processEvent(InputCoordinator source, Event event) {
        // not used
    }

    @Override
    public Class[] getPreprocessorEventClasses() {
        return new Class[]{MessageEvent.class, LaunchEvent.class};
    }

    @Override
    public Class[] getListenerEventClasses() {
        return new Class[]{};
    }

	@Override
	public void log(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}
}






/*
public class MAIActivityTracker extends BasilicaAdapter
{
	 private long dormantThreshold = 60*1000;   // I'd want this to be 5*60*1000 (5mins) - this is empirical, we'll see
    private HashMap<String,Long> lastSpoke = new HashMap<>();

    public MAIActivityTracker(Agent a) {
        super(a);
        // maybe read threshold from properties
    }

    @Override
    public void preProcessEvent(InputCoordinator source, Event event) {
        if (event instanceof MessageEvent) {
            MessageEvent me = (MessageEvent)event;
            lastSpoke.put(me.getFrom(), System.currentTimeMillis());
        } else if (event instanceof PresenceEvent) {
            // update presence if needed
        }
    }

	@Override
    public void processEvent(InputCoordinator source, Event event) {
        // not used
    }

    @Override
    public Class[] getListenerEventClasses() {
        return new Class[] { MessageEvent.class, PresenceEvent.class };
    }


    public boolean anyoneDormant() {
        long now = System.currentTimeMillis();
        for(Long ts : lastSpoke.values()) {
            if (now - ts > dormantThreshold) return true;
        }
        return false;
    }

	@Override
	public Class[] getPreprocessorEventClasses()
	{
		return new Class[] { MessageEvent.class, LaunchEvent.class };
	}
}

*/
