package model;

import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import org.portico.impl.hla13.types.DoubleTime;

import java.util.Comparator;

public class MyInteraction
{
    public int interactionClass;
    public ReceivedInteraction theInteraction;
    public LogicalTime theTime;
    Double time;


    public MyInteraction(int interactionClass, ReceivedInteraction theInteraction, LogicalTime theTime)
    {
        this.interactionClass = interactionClass;
        this.theInteraction = theInteraction;
        this.theTime = theTime;
        this.time = this.convertTime(theTime);
    }

    protected double convertTime(LogicalTime logicalTime)
    {
        return ((DoubleTime) logicalTime).getTime();
    }

    public static class MyInteractionComparator implements Comparator<MyInteraction>
    {
        public int compare(MyInteraction i1, MyInteraction i2)
        {
            return i1.time.compareTo(i2.time);
        }
    }

}
