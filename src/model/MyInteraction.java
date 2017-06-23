package model;

import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;

public class MyInteraction
{
    public int interactionClass;
    public ReceivedInteraction theInteraction;
    public LogicalTime theTime;


    public MyInteraction(int interactionClass, ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        this.interactionClass = interactionClass;
        this.theInteraction = theInteraction;
        this.theTime = theTime;
    }
}
