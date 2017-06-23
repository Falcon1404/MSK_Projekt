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
                    listaKlientow.add(0, klient);
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
                            klient.czySkonczylRobicZakupy = true;
                            klient.czyJestWKolejce = true;
                            dodajKlientaDoKasy(fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue, klient);
                            klient.wejscieDoKolejki = currentTime;
                            log("Klient " + fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue + " wszedl do kolejki w kasie "
                                    + fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
                        }
                    }
                }

                //Pobieranie klientów do obsługi
                for(Kasa kasa : listaKas)
                {
                    if(kasa.aktualnieObslugiwanyKlient == null)
                    {
                        if(kasa.liczbaKlientowWKolejce > 0)
                        {
                            kasa.kolejkaKlientow.get(0).rozpoczecieObslugi = currentTime;
                            kasa.aktualnieObslugiwanyKlient = kasa.kolejkaKlientow.remove(0);
                            kasa.aktualnieObslugiwanyKlient.czyJestObslugiwany = true;
                            kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony = false;
                            kasa.liczbaKlientowWKolejce--;
                            kasa.czyPrzepelniona = false;

                            for(Klient klient : listaKlientow)
                            {
                                if(klient.ID == kasa.aktualnieObslugiwanyKlient.ID)
                                {
                                    klient.czyJestObslugiwany = true;
                                    klient.czyZostalObsluzony = false;
                                    klient.rozpoczecieObslugi = currentTime;
                                }
                            }
                            sendRozpoczecieObslugi(kasa.aktualnieObslugiwanyKlient.ID, kasa.ID);
                            log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + kasa.ID);
                        }
                    }
                }

                //Obsluga klientów
                for(Kasa kasa : listaKas)
                {
                    if (kasa.aktualnieObslugiwanyKlient != null)
                    {
                        if(kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony(currentTime))
                        {
                            for(Klient klient : listaKlientow)
                            {
                                if(klient.ID == kasa.aktualnieObslugiwanyKlient.ID)
                                {
                                    klient.czyJestWKolejce = false;
                                    klient.czyJestObslugiwany = false;
                                    klient.czyZostalObsluzony = true;
                                    klient.zakonczenieObslugi = currentTime;
                                }
                            }
                            kasa.aktualnieObslugiwanyKlient.czyJestObslugiwany = false;
                            kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony = true;
                            kasa.aktualnieObslugiwanyKlient.czyJestWKolejce = false;
                            kasa.aktualnieObslugiwanyKlient.zakonczenieObslugi = currentTime;
                            sendZakonczenieObslugi(kasa.aktualnieObslugiwanyKlient.ID, kasa.ID);
                            log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + kasa.ID);
                            kasa.aktualnieObslugiwanyKlient = null;
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
