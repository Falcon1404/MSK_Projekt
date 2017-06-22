package federaci;

import ambasador.KlientAmbassador;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import model.Dane;
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
        //Generowanie klientów początkowych
//        for (int i = 0; i < Dane.LICZBA_POCZATKOWYCH_KLIENTOW; i++)
//        {
//            generateAndAddKlient();
//        }
//
//        for (int i = 0; i < Dane.LICZBA_POCZATKOWYCH_VIP; i++)
//        {
//            generateAndAddKlientVIP();
//        }

        //Tworzenie federacji
        createFederation();

        //Dołączanie do federacji

        joinFederation(federateName);

        //Pierwszy punkt synchronizacji
        registerSyncPoint();

        //Poczekaj na użytkownika
        waitForUser();

        //Zgłoś osiągnięcie punktu synchronizacji
        achieveSyncPoint();

        publishAndSubscribe();

        ////////////////////////////
        // Main loop			  //
        ////////////////////////////

        //Enable time policy
        enableTimePolicy();

        while(fedamb.running)
        {
            if(fedamb.czyStartSymulacji)
            {
                if(fedamb.czyTworzycKlienta)
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.czyTworzycKlienta = false;
                }
                if(fedamb.czyTworzycVIP)
                {
                    Klient klient = new Klient(fedamb.federatKlientIDAttributeValue, fedamb.federatKlientCzasUtworzeniaAttributeValue,
                            fedamb.federatKlientCzasZakonczeniaZakupowValue, fedamb.federatKlientIloscTowarowAttributeValue,
                            fedamb.federatKlientIloscGotowkiAttributeValue, fedamb.federatKlientCzyVIPAttributeValue);
                    listaKlientow.add(klient);
                    fedamb.czyTworzycVIP = false;
                }
                if(fedamb.czyTworzycKase)
                {
                    Kasa kasa = new Kasa(fedamb.federatKasaIDAttributeValue, fedamb.federatKasaLiczbaKlientowWKolejceAttributeValue,
                            fedamb.federatKasaCzyPrzepelnionaAttributeValue);
                    listaKas.add(kasa);
                    fedamb.czyTworzycKase = false;
                }

                if(fedamb.czyStopSymulacji)
                {
                    System.out.println("Amb: Odebrano Stop Interaction.");
                }
            }

//            if (fedamb.czyTworzycKlienta)
//            {
//                //Nowy klient
//                generateAndAddKlient();
//                fedamb.czyTworzycKlienta = false;
//            }
//            if(fedamb.czyTworzycKase)
//            {
//                //nowa kasa
//                fedamb.czyTworzycKase = false;
//            }
//            if(fedamb.czyAktualizowacKase)
//            {
//                //aktualizacja wybranej kasy
//                aktualizacjaKasy(fedamb.IDAktualizowanejKasy, fedamb.dlugoscKolejki, fedamb.czyPrzepelniona);
//            }
//
//            //tworzenie klienta co jakiś czas
//            if(rand.nextDouble() <= 0.2)
//            {
//                generateAndAddKlientVIP();
//                fedamb.czyTworzycKlienta = false;
//            }
//            else
//            {
//                generateAndAddKlient();
//                fedamb.czyTworzycKlienta = false;
//            }
//
//            //Aktualizacja robienia zakupów
//            for(int i = 0; i < listaKlientow.size(); i++)
//            {
//                listaKlientow.get(i).czySkonczylRobicZakupy(fedamb.getFederateTime());
//            }

            //Wybór kolejki
//            for(int i = 0; i < listaKlientow.size(); i++)
//            {
//                if(listaKlientow.get(i).czySkonczylRobicZakupy)
//                {
//                    int najkrotszaKolejka = -1;
//                    int IDKasa = -1;
//                    if(listaKas.size() > 0)
//                    {
//                        for(int j = 0; j < listaKas.size(); j++)
//                        {
//                            if(!listaKas.get(j).czyPrzepelniona)
//                            {
//                                if(listaKas.get(j).liczbaKlientowWKolejce <= najkrotszaKolejka)
//                                {
//                                    najkrotszaKolejka = listaKas.get(j).liczbaKlientowWKolejce;
//                                    IDKasa = listaKas.get(j).ID;
//                                    //TODO sendInteraction
//                                }
//                            }
//                            else
//                            {
//                                czyJakasKasaJestPrzepelniona = true;
//                                //TODO sendInteraction do Menago
//                            }
//                        }
//                    }
//                    listaKlientow.get(i).nrKasy = IDKasa;
//                    dodajKlientaDoKasy(IDKasa, listaKlientow.get(i));
//                }
//            }
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

//            publishWejscieDoKolejki();
//            publishRozpocznijObsluge();
//            publishZakonczObsluge();
        }
        catch(RTIexception e)
        {
            log(e.getMessage());
        }
    }

}

