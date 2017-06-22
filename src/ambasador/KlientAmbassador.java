package ambasador;


public class KlientAmbassador extends AbstractAmbassador
{
    public static final String AMBASSADOR_NAME = "KlientFederatAmbsassor";

    protected void log(String message)
    {
        System.out.println(AMBASSADOR_NAME + ": " + message);
    }

    public KlientAmbassador(){}

//        if(interactionClass == wejscieDoKolejkiInteractionHandle)
//        {
//            for(int i = 0; i < theInteraction.size(); i++)
//            {
//                try
//                {
//                    byte[] value = theInteraction.getValue(i);
//                    if(theInteraction.getParameterHandle(i) == IDKlientWejscieDoKolejkiInteractionAttributeHandle)
//                    {
//                        IDKlientWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
//                    }
//                    if(theInteraction.getParameterHandle(i) == IDKasaWejscieDoKolejkiInteractionAttributeHandle)
//                    {
//                        IDKasaWejscieDoKolejkiInteractionAttributeValue = EncodingHelpers.decodeInt(value);
//                    }
//                }
//                catch(Exception e)
//                {
//                    log(e.getMessage());
//                }
//            }
//            czasWejsciaDoKolejki = convertTime(theTime);
//            //federat.aktualizujCzasWejsciaDoKolejki(IDKlientWejscieDoKolejkiInteractionAttributeValue, czasWejsciaDoKolejki);
//        }
//        if(interactionClass == rozpoczecieObslugiInteractionHandle)
//        {
//            try
//            {
//                byte[] value = theInteraction.getValue(0);
//                IDKlientRozpoczecieObslugiValue = EncodingHelpers.decodeInt(value);
//                czasRozpoczeciaObslugi = convertTime(theTime);
//                federat.aktualizujCzasRozpoczeciaObslugi(IDKlientRozpoczecieObslugiValue, czasRozpoczeciaObslugi);
//            }
//            catch (Exception e)
//            {
//                log(e.getMessage());
//            }
//        }
//        if(interactionClass == zakonczenieObslugiInteractionHandle)
//        {
//            try
//            {
//                byte[] value = theInteraction.getValue(0);
//                IDKlientZakonczenieObslugiValue = EncodingHelpers.decodeInt(value);
//                czasZakonczeniaObslugi = convertTime(theTime);
//                federat.aktualizujCzasZakonczeniaObslugi(IDKlientZakonczenieObslugiValue, czasZakonczeniaObslugi);
//            }
//            catch (Exception e)
//            {
//                log(e.getMessage());
//            }
//        }
//        if(interactionClass == otworzKaseInteractionHandle)
//        {
//            czyTworzycKase = true;
//        }

}
