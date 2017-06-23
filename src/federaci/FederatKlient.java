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
                if(fedamb.getCzyKlientJestObslugiwany())
                {
                    fedamb.setCzyKlientJestObslugiwany(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientRozpoczecieObslugiValue)
                        {
                            for(int i = 0; i < listaKas.size(); i++)
                            {
                                if(listaKas.get(i).ID == fedamb.IDKasaRozpoczecieObslugiValue)
                                {
                                    if(listaKas.get(i).aktualnieObslugiwanyKlient == null)
                                    {
                                        if(listaKas.get(i).liczbaKlientowWKolejce > 0)
                                        {
                                            if(listaKas.get(i).kolejkaKlientow.get(0).ID == fedamb.IDKlientRozpoczecieObslugiValue)
                                            {
                                                listaKas.get(i).aktualnieObslugiwanyKlient = listaKas.get(i).kolejkaKlientow.remove(0);
                                                listaKas.get(i).liczbaKlientowWKolejce--;
                                                log("Klient " + listaKas.get(i).aktualnieObslugiwanyKlient.ID + " jest obslugiwany w kasie " + listaKas.get(i).ID);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //Czy skończył robić zakupy
                for (Klient klient : listaKlientow)
                {
                    klient.czySkonczylRobicZakupy(currentTime);
                }

                //Czy może wyjsc z kasy
                for (Klient klient : listaKlientow)
                {
                    if(klient.ID == fedamb.IDKlientZakonczenieObslugiValue)
                    {
                        if(klient.czySkonczylRobicZakupy && !klient.czyJestObslugiwany && klient.czyZostalObsluzony)
                        {
                            for(int i = 0; i < listaKas.size(); i++)
                            {
                                if(listaKas.get(i).ID == fedamb.IDKasaZakonczenieObslugiValue)
                                {
                                    if (listaKas.get(i).aktualnieObslugiwanyKlient.ID == klient.ID)
                                    {
                                        listaKas.get(i).aktualnieObslugiwanyKlient.zakonczenieObslugi = currentTime;
                                        listaKas.get(i).aktualnieObslugiwanyKlient.czyZostalObsluzony = true;
                                        sendZakonczenieObslugi(listaKas.get(i).aktualnieObslugiwanyKlient.ID, listaKas.get(i).ID);
                                        log("Klient " + listaKas.get(i).aktualnieObslugiwanyKlient.ID + " zostal obsluzony w kasie " + listaKas.get(i).ID
                                                + " po czasie " +  listaKas.get(i).aktualnieObslugiwanyKlient.czasObslugi);
                                        listaKas.get(i).aktualnieObslugiwanyKlient = null;
                                    }
                                }
                            }
                        }
                    }
                }

                //Czy może wejść do kasy
                for (Klient klient : listaKlientow)
                {
                    if(klient.czySkonczylRobicZakupy && !klient.czyJestWKolejce && !klient.czyJestObslugiwany)
                    {
                        if(listaKas.size() > 0)
                        {
                            int IDKasa = -1;
                            int najkrotszaKolejka = Integer.MAX_VALUE;

                            for(int i = 0; i < listaKas.size(); i++)
                            {
                                if(!listaKas.get(i).czyPrzepelniona)
                                {
                                    if(listaKas.get(i).liczbaKlientowWKolejce < najkrotszaKolejka)
                                    {
                                        IDKasa = listaKas.get(i).ID;
                                        najkrotszaKolejka = listaKas.get(i).liczbaKlientowWKolejce;
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

