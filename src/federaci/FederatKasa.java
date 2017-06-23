package federaci;

import ambasador.KasaAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;

import java.util.Random;

public class FederatKasa extends AbstractFederat
{
//    private static final String federateName = "FederatKasa";
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
        federateName = "FederatKasa";
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
                double currentTime = fedamb.getFederateTime();
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
                    log("Dodano klient VIP " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycKase())
                {
                    Kasa kasa = new Kasa(fedamb.federatKasaIDAttributeValue, fedamb.federatKasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.federatKasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log("Dodano kase " + fedamb.federatKasaIDAttributeValue);
                }

                //Wejście do kolejki
                if(fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue)
                        {
                            dodajKlientaDoKasy(fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue, klient);
                            fedamb.czasWejsciaDoKolejki = currentTime;
                            log("Klient " + fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue + " wszedl do kolejki w kasie "
                                    + fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
                        }
                    }
                }

                //Pobieranie klientów do obsługi
                for(int i = 0; i < listaKas.size(); i++)
                {
                    if(listaKas.get(i).aktualnieObslugiwanyKlient == null)
                    {
                        if(listaKas.get(i).liczbaKlientowWKolejce > 0)
                        {
                            listaKas.get(i).kolejkaKlientow.get(0).rozpoczecieObslugi = currentTime;
                            listaKas.get(i).aktualnieObslugiwanyKlient = listaKas.get(i).kolejkaKlientow.remove(0);
                            listaKas.get(i).liczbaKlientowWKolejce--;
                            sendRozpoczecieObslugi(listaKas.get(i).aktualnieObslugiwanyKlient.ID, listaKas.get(i).ID);
                            log("Klient " + listaKas.get(i).aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + listaKas.get(i).ID);
                        }
                    }
                }

                //Obsluga klientów
                for(int i = 0; i < listaKas.size(); i++)
                {
                    if (listaKas.get(i).aktualnieObslugiwanyKlient != null)
                    {
                        if(listaKas.get(i).aktualnieObslugiwanyKlient.czyZostalObsluzony(currentTime))
                        {
                            listaKas.get(i).aktualnieObslugiwanyKlient.zakonczenieObslugi = currentTime;
                            listaKas.get(i).aktualnieObslugiwanyKlient.czyZostalObsluzony = true;
                            sendZakonczenieObslugi(listaKas.get(i).aktualnieObslugiwanyKlient.ID, listaKas.get(i).ID);
                            log("Klient " + listaKas.get(i).aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + listaKas.get(i).ID);
                            listaKas.get(i).aktualnieObslugiwanyKlient = null;
                        }
                    }
                }

                if(fedamb.getCzyStopSymulacji())
                {
                    System.out.println("Amb: Odebrano Stop Interaction.");
                    fedamb.running = false;
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
            subscribeWejscieDoKolejki();
            publishRozpocznijObsluge();
            publishZakonczObsluge();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }

}
