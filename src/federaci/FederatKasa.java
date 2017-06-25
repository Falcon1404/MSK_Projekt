package federaci;

import ambasador.KasaAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;
import model.MyInteraction;

import java.util.Collections;
import java.util.Random;

public class FederatKasa extends AbstractFederat
{
    private Random rand = new Random();

    public static void main(String[] args)
    {
        try
        {
            new FederatKasa().runFederate();
            log("Wystartowal.");
        }
        catch (Exception e)
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
            double currentTime = fedamb.getFederateTime();
            Collections.sort(fedamb.listaInterakcji,  new MyInteraction.MyInteractionComparator());
            for (MyInteraction myInteraction : fedamb.listaInterakcji)
            {
                if (myInteraction.interactionClass == fedamb.startSymulacjiHandle)
                {
                    fedamb.obsluzStartSymulacji(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (myInteraction.interactionClass == fedamb.stopSymulacjiHandle)
                {
                    fedamb.obsluzStopSymulacji(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (myInteraction.interactionClass == fedamb.nowyKlientInteractionHandle)
                {
                    fedamb.obsluzNowyKlientInteractionHandle(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (myInteraction.interactionClass == fedamb.kasaInteractionHandle)
                {
                    fedamb.obsluzNowaKasa(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (myInteraction.interactionClass == fedamb.wejscieDoKolejkiInteractionHandle)
                {
                    fedamb.obsluzWejscieDoKolejki(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (fedamb.getCzyTworzycKlienta())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, fedamb.klientCzasUtworzeniaAttributeValue,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycKlienta(false);
                    log("Dodano klient " + fedamb.klientIDAttributeValue);
                }
                if (fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, fedamb.klientCzasUtworzeniaAttributeValue,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(0, klient);
                    fedamb.setCzyTworzycVIP(false);
                    log("Dodano klient VIP " + fedamb.klientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycKase())
                {
                    Kasa kasa = new Kasa(fedamb.kasaIDAttributeValue, fedamb.kasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.kasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log(federateName + " dodano kase " + fedamb.kasaIDAttributeValue);
                }


                //Wejście do kolejki
                if (fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Klient klient : listaKlientow)
                    {
                        if (klient.ID == fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue)
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


                if (fedamb.getCzyStopSymulacji())
                {
                    System.out.println("Odebrano Stop Interaction.");
                    fedamb.running = false;
                }
            }
            fedamb.listaInterakcji.clear();

            if (fedamb.getCzyStartSymulacji())
            {
                //Obsluga klientów
                for (Kasa kasa : listaKas)
                {
                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                    if(kasa.getLiczbaKlientowWKolejce() < kasa.MAX_LICZBA_KLIENTOW)
                    {
                        kasa.czyPrzepelniona = false;
                    }

                    if (kasa.aktualnieObslugiwanyKlient != null)
                    {
                        if (kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony(currentTime))
                        {
                            for (Klient klient : listaKlientow)
                            {
                                if (klient.ID == kasa.aktualnieObslugiwanyKlient.ID)
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

                //Pobieranie klientów do obsługi
                for (Kasa kasa : listaKas)
                {
                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                    if(kasa.getLiczbaKlientowWKolejce() < kasa.MAX_LICZBA_KLIENTOW)
                    {
                        kasa.czyPrzepelniona = false;
                    }

                    if (kasa.aktualnieObslugiwanyKlient == null)
                    {
                        if (kasa.getLiczbaKlientowWKolejce() > 0)
                        {
                            kasa.kolejkaKlientow.get(0).rozpoczecieObslugi = currentTime;
                            kasa.aktualnieObslugiwanyKlient = kasa.kolejkaKlientow.remove(0);
                            kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                            kasa.czyPrzepelniona = false;

                            for (Klient klient : listaKlientow)
                            {
                                if (klient.ID == kasa.aktualnieObslugiwanyKlient.ID)
                                {
                                    klient.czyJestWKolejce = false;
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
        catch (RTIexception e)
        {
            e.printStackTrace();
        }
    }

}
