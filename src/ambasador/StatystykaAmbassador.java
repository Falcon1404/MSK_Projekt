package ambasador;

public class StatystykaAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "StatystykaFederatAmbsassor";

    public StatystykaAmbassador(){}

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }
}
