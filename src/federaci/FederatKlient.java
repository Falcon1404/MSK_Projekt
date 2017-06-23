package federaci;

import ambasador.KlientAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;

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
                    log(federateName + " dodano klient " + fedamb.federatKlientIDAttributeValue);
                }
                if(fedamb.getCzyTworzycVIP())
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(0, klient);
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

                //Czy skończył robić zakupy
                for (Klient klient : listaKlientow)
                {
                    if(!klient.czyJestWKolejce && !klient.czySkonczylRobicZakupy && !klient.czyJestObslugiwany && !klient.czyZostalObsluzony)
                    {
                        klient.czySkonczylRobicZakupy(currentTime);
                    }
                }

                if(fedamb.getCzyKlientZostalObsluzony())
                {
                    fedamb.setCzyKlientZostalObsluzony(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientZakonczenieObslugiValue)
                        {
                            if(!klient.czyJestWKolejce && klient.czySkonczylRobicZakupy && klient.czyJestObslugiwany && !klient.czyZostalObsluzony)
                            {
                                for(Kasa kasa : listaKas)
                                {
//                                    log("Kasa " + listaKas.get(i).ID + " kolejka" + listaKas.get(i).liczbaKlientowWKolejce);
                                    if(kasa.ID == fedamb.IDKasaZakonczenieObslugiValue)
                                    {
                                        if (kasa.aktualnieObslugiwanyKlient.ID == klient.ID)
                                        {
                                            klient.czyZostalObsluzony = true;
                                            kasa.aktualnieObslugiwanyKlient.zakonczenieObslugi = currentTime;
                                            kasa.aktualnieObslugiwanyKlient.czyZostalObsluzony = true;
                                            log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + kasa.ID
                                                    + " po czasie " +  kasa.aktualnieObslugiwanyKlient.czasObslugi);
                                            kasa.aktualnieObslugiwanyKlient = null;
                                        }
                                    }
                                }
                            }
                        }
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
                                if(!kasa.czyPrzepelniona)
                                {
                                    if(kasa.liczbaKlientowWKolejce < najkrotszaKolejka)
                                    {
                                        IDKasa = kasa.ID;
                                        najkrotszaKolejka = kasa.liczbaKlientowWKolejce;
                                    }
                                }
                            }

                            if(IDKasa > -1 && najkrotszaKolejka < Integer.MAX_VALUE)
                            {
                                fedamb.czasWejsciaDoKolejki = currentTime;
                                klient.nrKasy = IDKasa;
                                dodajKlientaDoKasy(IDKasa, klient);
                                klient.rozpoczecieObslugi = currentTime;
                                sendWejscieDoKolejki(klient.ID, IDKasa);
                                klient.czyJestWKolejce = true;
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
                                log("Kasa " + kasa.ID + " kolejka = " + kasa.liczbaKlientowWKolejce);
                                if(kasa.ID == fedamb.IDKasaRozpoczecieObslugiValue)
                                {
                                    if(kasa.aktualnieObslugiwanyKlient == null)
                                    {
                                        if(kasa.liczbaKlientowWKolejce > 0)
                                        {
                                            if(kasa.kolejkaKlientow.get(0).ID == fedamb.IDKlientRozpoczecieObslugiValue)
                                            {
                                                klient.czyJestObslugiwany = true;
                                                klient.czyZostalObsluzony = false;
                                                klient.czyJestWKolejce = false;
                                                kasa.aktualnieObslugiwanyKlient = kasa.kolejkaKlientow.remove(0);
                                                kasa.liczbaKlientowWKolejce--;
                                                kasa.czyPrzepelniona = false;
                                                log("Klient " + kasa.aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + kasa.ID);
                                            }
                                        }
                                    }
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

