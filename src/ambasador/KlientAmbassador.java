package ambasador;

public class KlientAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "KlientFederatAmbsassor";

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }

    public KlientAmbassador(){}



}
