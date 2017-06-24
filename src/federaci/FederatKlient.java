package federaci;

import ambasador.KlientAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;
import model.MyInteraction;

import java.util.Random;


public class FederatKlient extends AbstractFederat
{
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
        federateName = "FederatKlient";
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
            double currentTime = fedamb.getFederateTime();

            for(MyInteraction myInteraction : fedamb.listaInterakcji)
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

                if (myInteraction.interactionClass == fedamb.rozpoczecieObslugiInteractionHandle)
                {
                    fedamb.obsluzRozpoczecieObslugi(myInteraction.theInteraction, myInteraction.theTime);
                }

                if (myInteraction.interactionClass == fedamb.zakonczenieObslugiInteractionHandle)
                {
                    fedamb.obsluzZakonczenieObslugi(myInteraction.theInteraction, myInteraction.theTime);
                }


                if(fedamb.getCzyTworzycKlienta())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, fedamb.klientCzasUtworzeniaAttributeValue,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycKlienta(false);
                    log(federateName + " dodano klient " + fedamb.klientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, fedamb.klientCzasUtworzeniaAttributeValue,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(0, klient);
                    fedamb.setCzyTworzycVIP(false);
                    log(federateName + " dodano klient VIP " + fedamb.klientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycKase())
                {
                    Kasa kasa = new Kasa(fedamb.kasaIDAttributeValue, fedamb.kasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.kasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.setCzyTworzycKase(false);
                    log(federateName + " dodano kase " + fedamb.kasaIDAttributeValue);
                }

                //Wychodzi z kasy
                if(fedamb.getCzyKlientZostalObsluzony())
                {
                    fedamb.setCzyKlientZostalObsluzony(false);
                    for (int i = listaKlientow.size() - 1; i >= 0; i--)
                    {
                        Klient klient = listaKlientow.get(i);
                        if(klient.ID == fedamb.IDKlientZakonczenieObslugiValue)
                        {
                            for(Kasa kasa : listaKas)
                            {
                                if(kasa.ID == fedamb.IDKasaZakonczenieObslugiValue && kasa.aktualnieObslugiwanyKlient != null)
                                {
                                    klient.czyZostalObsluzony = true;
                                    kasa.aktualnieObslugiwanyKlient.zakonczenieObslugi = currentTime;
                                    kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony = true;
                                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                                    log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + kasa.ID
                                            + " po czasie " +  kasa.aktualnieObslugiwanyKlient.czasObslugi
                                            + " dlugosc kolejki " + kasa.getLiczbaKlientowWKolejce());

                                    kasa.aktualnieObslugiwanyKlient = null;
                                    kasa.czyPrzepelniona = false;
                                }
                            }
                            listaKlientow.remove(i);
                        }
                    }
                }

                //Obsługa klienta
                if(fedamb.getCzyKlientJestObslugiwany())
                {
                    fedamb.setCzyKlientJestObslugiwany(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientRozpoczecieObslugiValue)
                        {
                            for(Kasa kasa : listaKas)
                            {
                                log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                                if(kasa.ID == fedamb.IDKasaRozpoczecieObslugiValue)
                                {
                                    klient.czyJestObslugiwany = true;
                                    klient.czyZostalObsluzony = false;
                                    klient.czyJestWKolejce = false;
                                    kasa.aktualnieObslugiwanyKlient = kasa.kolejkaKlientow.remove(0);
                                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                                    log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                                    kasa.czyPrzepelniona = false;
                                    log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + kasa.ID);
                                }
                            }
                        }
                    }
                }

                if(fedamb.getCzyStopSymulacji())
                {
                    System.out.println("Amb: Odebrano Stop Interaction.");
                    fedamb.running = false;
                }
            }
            fedamb.listaInterakcji.clear();

            for(Kasa kasa : listaKas)
            {
                kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                if(kasa.getLiczbaKlientowWKolejce() < kasa.MAX_LICZBA_KLIENTOW)
                {
                    kasa.czyPrzepelniona = false;
                }
            }

            if(fedamb.getCzyStartSymulacji())
            {
                //Czy skończył robić zakupy
                for (Klient klient : listaKlientow)
                {
                    if(!klient.czyJestWKolejce && !klient.czySkonczylRobicZakupy && !klient.czyJestObslugiwany && !klient.czyZostalObsluzony)
                    {
                        klient.czySkonczylRobicZakupy(currentTime);
                    }
                }
                //Wybor kasy
                for (Klient klient : listaKlientow)
                {
                    if(!klient.czyJestWKolejce && klient.czySkonczylRobicZakupy && !klient.czyJestObslugiwany && !klient.czyZostalObsluzony)
                    {
                        if(listaKas.size() > 0)
                        {
                            int IDKasa = -1;
                            int najkrotszaKolejka = Integer.MAX_VALUE;

                            for(Kasa kasa : listaKas)
                            {
                                kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                                if(kasa.getLiczbaKlientowWKolejce() < kasa.MAX_LICZBA_KLIENTOW)
                                {
                                    kasa.czyPrzepelniona = false;
                                }
                                if(!kasa.czyPrzepelniona)
                                {
                                    if(kasa.getLiczbaKlientowWKolejce() < najkrotszaKolejka)
                                    {
                                        IDKasa = kasa.ID;
                                        najkrotszaKolejka = kasa.getLiczbaKlientowWKolejce();
                                    }
                                }
                            }

                            if(IDKasa > -1 && najkrotszaKolejka < Integer.MAX_VALUE)
                            {
                                log("Klient " + klient.ID + " wszedl do kolejki kasy " + IDKasa);
                                klient.wejscieDoKolejki = currentTime;
                                klient.nrKasy = IDKasa;
                                dodajKlientaDoKasy(IDKasa, klient);
                                klient.rozpoczecieObslugi = currentTime;
                                sendWejscieDoKolejki(klient.ID, IDKasa);
                                klient.czyJestWKolejce = true;
                            }
                        }
                    }
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
            publishWejscieDoKolejki();
            subscribeRozpocznijObsluge();
            subscribeZakonczObsluge();
        }
        catch(RTIexception e)
        {
            log(e.getMessage());
        }
    }

}

