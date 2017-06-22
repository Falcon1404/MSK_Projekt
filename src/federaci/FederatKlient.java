package federaci;

import ambasador.KlientAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;

import java.util.Random;


public class FederatKlient extends AbstractFederat
{
    private static final String federateName = "FederatKlient";
    private Random rand = new Random();

    public static void main(String[] args)
    {
        try
        {
            new FederatKlient().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        fedamb = new KlientAmbassador();
        createFederation();

        joinFederation(federateName);
        registerSyncPoint();
        waitForUser();
        achieveSyncPoint();
        enableTimePolicy();
        publishAndSubscribe();
        run();
    }

    public void run() throws RTIexception
    {
        while(fedamb.running)
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
                    log(federateName + " dodano klient " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycVIP(false);
                    log(federateName + " dodano klient VIP " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycKase())
                {
                    Kasa kasa = new Kasa(fedamb.federatKasaIDAttributeValue, fedamb.federatKasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.federatKasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log(federateName + " dodano kase " + fedamb.federatKasaIDAttributeValue);
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


    private void publishAndSubscribe() throws RTIexception
    {
        try
        {
            subscribeStartSymulacji();
            subscribeStopSymulacji();
            subscribeKlient();
            subscribeKasa();
        }
        catch(RTIexception e)
        {
            log(e.getMessage());
        }
    }

}

