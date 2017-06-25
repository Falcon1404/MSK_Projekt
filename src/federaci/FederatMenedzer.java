package federaci;


import ambasador.MenedzerAmbassador;
import hla.rti.RTIexception;
import model.Kasa;
import model.Klient;
import model.MyInteraction;

import java.util.Collections;
import java.util.Random;

public class FederatMenedzer extends AbstractFederat
{
    private Random rand = new Random();
    private boolean czyWyslacKase = true;

    public static void main(String[] args)
    {
        try
        {
            new FederatMenedzer().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        federateName = "FederatMenedzer";
        fedamb = new MenedzerAmbassador();
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
                if (myInteraction.interactionClass == fedamb.rozpoczecieObslugiInteractionHandle)
                {
                    fedamb.obsluzRozpoczecieObslugi(myInteraction.theInteraction, myInteraction.theTime);
                }
                if (myInteraction.interactionClass == fedamb.zakonczenieObslugiInteractionHandle)
                {
                    fedamb.obsluzZakonczenieObslugi(myInteraction.theInteraction, myInteraction.theTime);
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
                if (fedamb.getCzyTworzycKase())
                {
                    czyWyslacKase = true;
                    Kasa kasa = new Kasa(fedamb.kasaIDAttributeValue, fedamb.kasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.kasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log("Dodano kase " + fedamb.kasaIDAttributeValue);
                }

                //Wychodzi z kasy
                if(fedamb.getCzyKlientZostalObsluzony())
                {
                    fedamb.setCzyKlientZostalObsluzony(false);
                    for(Kasa kasa : listaKas)
                    {
                        if(kasa.ID == fedamb.IDKasaZakonczenieObslugiValue)
                        {
                            kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce()-1);
                            kasa.czyPrzepelniona = false;
                        }
                    }
                }

                //Obsługa klienta
                if(fedamb.getCzyKlientJestObslugiwany())
                {
                    fedamb.setCzyKlientJestObslugiwany(false);
                    for(Kasa kasa : listaKas)
                    {
//                        log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                        if(kasa.ID == fedamb.IDKasaRozpoczecieObslugiValue)
                        {
                            //kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce()-1);
                            //log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                            //kasa.czyPrzepelniona = false;
                            //log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + kasa.ID);
                        }
                    }
                }

                //Wejście do kolejki
                if (fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Kasa kasa : listaKas)
                    {
                        if (kasa.ID == fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue)
                        {
                            kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce()+1);
                            kasa.czyPrzepelniona = kasa.getLiczbaKlientowWKolejce() >= kasa.MAX_LICZBA_KLIENTOW;
                        }
                    }
                }

                boolean menagoPrzepelniona = true;
                for (Kasa kasa : listaKas)
                {
                    if(!kasa.czyPrzepelniona)
                    {
                        menagoPrzepelniona = false;
                    }
                }

                if(menagoPrzepelniona && czyWyslacKase)
                {
                    log("Kasy sa przepelnione");
                    sendOtworzKaseInteraction();
                    czyWyslacKase = false;
                }
            }


            if(fedamb.getCzyStopSymulacji())
            {
                System.out.println("Odebrano: Stop Interaction.");
                fedamb.running = false;
            }
            fedamb.listaInterakcji.clear();
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
            subscribeWejscieDoKolejki();
            subscribeRozpocznijObsluge();
            subscribeZakonczObsluge();
            publishOtworzKase();
        }
        catch(RTIexception e)
        {
            log(e.getMessage());
        }
    }
}
