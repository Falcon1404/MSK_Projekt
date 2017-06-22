package ambasador;

public class GUIAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "GUIFederatAmbsassor";

    public GUIAmbassador(){}

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }
}
