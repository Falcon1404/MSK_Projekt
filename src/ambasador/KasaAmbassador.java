package ambasador;

public class KasaAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "KasaFederatAmbsassor";

    public KasaAmbassador(){}

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }

}
