package federaci;

import ambasador.StatystykaAmbassador;
import hla.rti.RTIexception;
import model.Kasa;
import model.Klient;
import model.MyInteraction;

import java.util.Collections;

public class FederatStatystyka extends AbstractFederat
{
    private double lacznyCzasZakupow = 0.0;
    private double sredniCzasZakupow = 0.0;
    private double oldSredniCzasZakupow = 0.0;
    private int liczbaKlientowRobiacychZakupy = 0;

    private double lacznyCzasPobytuWKolejce = 0.0;
    private double sredniCzasPobytuWKolejce = 0.0;
    private double oldSredniCzasPobytuWKolejce = 0.0;
    private int liczbaKlientowPobytuWKolejce = 0;

    private double lacznyCzasObslugi = 0.0;
    private double sredniCzasObslugi = 0.0;
    private double oldSredniCzasObslugi = 0.0;
    private int liczbaKlientowCzasObslugi = 0;

    protected static void log(String message)
    {
        System.out.println(federateName + ": " + message);
    }

    public static void main(String[] args)
    {
        try
        {
            new FederatStatystyka().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public void runFederate() throws RTIexception
    {
        federateName = "FederatStatystyka";
        fedamb = new StatystykaAmbassador();
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
        while (fedamb.running)
        {
            double currentTime = fedamb.getFederateTime();
            Collections.sort(fedamb.listaInterakcji,  new MyInteraction.MyInteractionComparator());
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

                if(fedamb.getCzyTworzycKlienta())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, currentTime,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.setCzyTworzycKlienta(false);
                    log(federateName + " dodano klient " + fedamb.klientIDAttributeValue + " t = " + currentTime);
                }
                if(fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.klientIDAttributeValue, currentTime,
                            fedamb.klientCzasZakonczeniaZakupowValue, fedamb.klientIloscTowarowAttributeValue,
                            fedamb.klientIloscGotowkiAttributeValue, fedamb.klientCzyVIPAttributeValue);
                    listaKlientow.add(0, klient);
                    fedamb.setCzyTworzycVIP(false);
                    log(federateName + " dodano klient " + fedamb.klientIDAttributeValue + " t = " + currentTime);
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
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientZakonczenieObslugiValue)
                        {
                            for(Kasa kasa : listaKas)
                            {
                                if(kasa.ID == fedamb.IDKasaZakonczenieObslugiValue && kasa.aktualnieObslugiwanyKlient != null)
                                {
                                    klient.czyZostalObsluzony = true;
                                    klient.zakonczenieObslugi = currentTime;
                                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
                                    log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + kasa.ID + " t = " + currentTime);
//                                    log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + kasa.ID
//                                            + " po czasie " +  kasa.aktualnieObslugiwanyKlient.czasObslugi
//                                            + " dlugosc kolejki " + kasa.getLiczbaKlientowWKolejce());
                                    kasa.aktualnieObslugiwanyKlient = null;
                                    kasa.czyPrzepelniona = false;
                                }
                            }
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
//                                log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                                if(kasa.ID == fedamb.IDKasaRozpoczecieObslugiValue)
                                {
                                    klient.rozpoczecieObslugi = currentTime;
                                    klient.czyJestObslugiwany = true;
                                    kasa.aktualnieObslugiwanyKlient = kasa.kolejkaKlientow.remove(0);
                                    kasa.setLiczbaKlientowWKolejce(kasa.kolejkaKlientow.size());
//                                    log("Kasa " + kasa.ID + " kolejka = " + kasa.getLiczbaKlientowWKolejce());
                                    kasa.czyPrzepelniona = false;
                                    log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + kasa.ID + " t = " + currentTime);
                                }
                            }
                        }
                    }
                }
                //Wejście do kolejki
                if (fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Klient klient : listaKlientow)
                    {
                        if (klient.ID == fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue)
                        {
                            klient.czyJestWKolejce = true;
                            klient.czySkonczylRobicZakupy = true;
                            dodajKlientaDoKasy(fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue, klient);
                            klient.wejscieDoKolejki = currentTime;
                            log("Klient " + fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue + " wszedl do kolejki w kasie "
                                    + fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue + " t = " + currentTime);
                        }
                    }
                }


                //sredni czas obslugi
                liczbaKlientowCzasObslugi = 0;
                lacznyCzasObslugi = 0.0;
                //Sredni czas w kolejce
                liczbaKlientowPobytuWKolejce = 0;
                lacznyCzasPobytuWKolejce = 0.0;
                //Sredni czas robienia zakupow
                liczbaKlientowRobiacychZakupy = 0;
                lacznyCzasZakupow = 0.0;

                for(Klient klient : listaKlientow)
                {
                    if(klient.czySkonczylRobicZakupy)
                    {
                        liczbaKlientowRobiacychZakupy++;
                        lacznyCzasZakupow += (klient.czasZakoczeniaZakupow - klient.czasUtworzeniaKlienta);
                    }
                    if(liczbaKlientowRobiacychZakupy > 0)
                    {
                        sredniCzasZakupow = lacznyCzasZakupow / (double) liczbaKlientowRobiacychZakupy;
                        if(oldSredniCzasZakupow >= 0 && sredniCzasZakupow > 0)
                        {
                            if(oldSredniCzasZakupow != sredniCzasZakupow)
                            {
                                sendSredniCzasZakupow((int) sredniCzasZakupow);
                                log("Sredni czas zakupow = " + sredniCzasZakupow);
                                oldSredniCzasZakupow = sredniCzasZakupow;
                            }
                        }
                    }

                    if(klient.czyZostalObsluzony)
                    {
                        liczbaKlientowCzasObslugi++;
                        lacznyCzasObslugi += (klient.zakonczenieObslugi - klient.rozpoczecieObslugi);

                        liczbaKlientowPobytuWKolejce++;
                        lacznyCzasPobytuWKolejce += ((klient.rozpoczecieObslugi - klient.wejscieDoKolejki));
//                                + (klient.czasZakoczeniaZakupow - klient.czasUtworzeniaKlienta));
                    }
                    if(liczbaKlientowCzasObslugi > 0)
                    {
                        sredniCzasObslugi = lacznyCzasObslugi / (double) liczbaKlientowCzasObslugi;
                        if(oldSredniCzasObslugi >= 0 && sredniCzasObslugi > 0)
                        {
                            if(oldSredniCzasObslugi != sredniCzasObslugi)
                            {
                                sendSredniCzasObslugi((int) sredniCzasObslugi);
                                log("Sredni czas obslugi = " + sredniCzasObslugi);
                                oldSredniCzasObslugi = sredniCzasObslugi;
                            }
                        }
                    }
                    if(liczbaKlientowPobytuWKolejce > 0)
                    {
                        sredniCzasPobytuWKolejce = lacznyCzasPobytuWKolejce / (double) liczbaKlientowPobytuWKolejce;
                        if(oldSredniCzasPobytuWKolejce >= 0 && sredniCzasPobytuWKolejce > 0)
                        {
                            if(oldSredniCzasPobytuWKolejce != sredniCzasPobytuWKolejce)
                            {
                                sendSredniCzasWKolejce((int) sredniCzasPobytuWKolejce);
                                log("Sredni czas pobytu w kolejce = " + sredniCzasPobytuWKolejce);
                                oldSredniCzasPobytuWKolejce = sredniCzasPobytuWKolejce;
                            }
                        }
                    }
                }

                if(fedamb.getCzyStopSymulacji())
                {
                    System.out.println("Odebrano Stop Interaction.");
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
            publishSredniCzasZakupow();
            publishSredniCzasWKolejce();
            publishSredniCzasObslugi();
        }
        catch(RTIexception e)
        {
            log(e.getMessage());
        }
    }
}
