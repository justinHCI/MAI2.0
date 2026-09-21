package basilica2.mai.operation;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import basilica2.agents.operation.BaseAgentOperation;
import basilica2.agents.operation.BaseAgentUI;
import basilica2.agents.operation.ConditionAgentUI;



public class MAIAgentOperation extends BaseAgentOperation
{
    public static void main(final String[] args) 
    {
        initializeSystemProperties("system.properties");
        final MAIAgentOperation thisOperation = new MAIAgentOperation();
        thisOperation.startOperation();
        thisOperation.launchAgent("testroom", false);
        // Keep the process alive
        try { Thread.currentThread().join(); } catch (InterruptedException e) {}
    }
} 


// dummy comment
/*
public class MAIAgentOperation extends BaseAgentOperation
{
    public static void main(final String[] args) 
    {
        initializeSystemProperties("system.properties");
        
        java.awt.EventQueue.invokeLater(new Runnable() 
        {

            @Override
            public void run() 
            {
            	MAIAgentOperation thisOperation = new MAIAgentOperation();
            	
            	
                BaseAgentUI thisUI = new ConditionAgentUI(thisOperation, "Test1");
                //thisUI.setLocation(windowLoc);
                thisOperation.setUI(thisUI);
                thisOperation.startOperation();
                thisUI.operationStarted();
                
                
                thisOperation.processArgs(args);
            }
        });
    }


}
*/