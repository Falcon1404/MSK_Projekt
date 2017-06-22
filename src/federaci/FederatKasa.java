package federaci;

import ambasador.KasaAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;

import java.util.Random;

public class FederatKasa extends AbstractFederat
{
    private static final String federateName = "FederatKasa";
    private Random rand = new Random();

    public static void main(String[] args)
    {
        try
        {
            new FederatKasa().runFederate();
            log("Wystartowal." );
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        fedamb = new KasaAmbassador();
        createFederation();

        joinFederation(federateName);
        registerSyncPoint();
        waitForUser();
        achieveSyncPoint();
        enableTimePolicy();
        publishAndSubscribe();
        run();
    }

    protected void run() throws RTIexception
    {
        while (fedamb.running)
        {
            if(fedamb.getCzyStartSymulacji())
            {
                if(fedamb.getCzyTworzycKlienta())
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycKlienta(false);
                    log("16. " + federateName + " dodano klient " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycVIP(false);
                    log("17. " + federateName + " dodano klient VIP " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycKase())
                {
                    Kasa kasa = new Kasa(fedamb.federatKasaIDAttributeValue, fedamb.federatKasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.federatKasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log("18. " + federateName + " dodano kase " + fedamb.federatKasaIDAttributeValue);
                }

                if(fedamb.getCzyStopSymulacji())
                {
                    System.out.println("Amb: Odebrano Stop Interaction.");
                }
            }
            advanceTime(timeStep);
        }
        disableTimePolicy();
    }

    private void publishAndSubscribe()
    {
        try
        {
            subscribeStartSymulacji();
            subscribeStopSymulacji();
            subscribeKasa();
            subscribeKlient();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }

}
