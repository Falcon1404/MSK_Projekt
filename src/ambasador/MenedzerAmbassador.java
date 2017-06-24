package ambasador;

public class MenedzerAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "MenedzerFederatAmbsassor";

    public MenedzerAmbassador(){}

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }
}
