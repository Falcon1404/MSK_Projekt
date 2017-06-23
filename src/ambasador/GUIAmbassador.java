package ambasador;

import hla.rti.EventRetractionHandle;
import hla.rti.LogicalTime;
import hla.rti.ReceivedInteraction;
import hla.rti.jlc.EncodingHelpers;

public class GUIAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "GUIFederatAmbsassor";

    public GUIAmbassador(){}

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }

}
