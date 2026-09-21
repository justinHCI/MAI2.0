package basilica2.mai.listeners;

import basilica2.agents.components.InputCoordinator;
import basilica2.agents.listeners.plan.PlanExecutor;
import basilica2.agents.listeners.plan.Step;
import basilica2.agents.listeners.plan.StepHandler;
import basilica2.util.TimeoutAdapter;
import basilica2.util.Timer;
import edu.cmu.cs.lti.basilica2.core.Agent;
import edu.cmu.cs.lti.project911.utils.log.Logger;


public class ConditionalListenStepHandler implements StepHandler
{

	private CognitiveListener cognitivelistener = null;
	private MetacognitiveListener metacognitivelistener = null;
	private BehavioralListener behaviorallistener = null;
	private SocialListener sociallistener = null;
	private AffectiveListener affectivelistener = null;
	
	@Override
	public void execute(Step currentStep, final PlanExecutor overmind, InputCoordinator source)
	{
		Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"starting listen step...");
		Agent a = overmind.getAgent();
		if(cognitivelistener == null)
		{
			cognitivelistener = new CognitiveListener(a);
			Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"Constructing cognitive listener");
		}

		if(metacognitivelistener == null)
		{
			metacognitivelistener = new MetacognitiveListener(a);
			Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"Constructing metacognitive listener");
		}

		if (behaviorallistener == null)
		{
			behaviorallistener = new BehavioralListener(a);
		} 

		if (sociallistener == null)
		{
			sociallistener = new SocialListener(a);
			Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"Constructing social listener");
		}

		if (affectivelistener == null)
		{
			affectivelistener = new AffectiveListener(a);
			Logger.commonLog(getClass().getSimpleName(),Logger.LOG_NORMAL,"Constructing affective listener");
		}
		
		
		//revoicer.setDelegate(new EndStepOnStopListening(overmind, currentStep.name));
        
		//overmind.addHelper(cognitivelistener);
        //overmind.addHelper(metacognitivelistener);
        //overmind.addHelper(behaviorallistener);
        //overmind.addHelper(sociallistener);
        //overmind.addHelper(affectivelistener);

		if(currentStep.attributes.containsKey("duration"))
			new Timer(Long.parseLong(currentStep.attributes.get("duration")), null, new TimeoutAdapter()
			{
				@Override
				public void timedOut(String id)
				{
					overmind.stepDone();
				}
				
			}).start();
	}

}
