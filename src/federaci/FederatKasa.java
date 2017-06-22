package federaci;


import ambasador.AbstractAmbassador;
import ambasador.KasaAmbassador;
import hla.rti.*;
import model.Kasa;
import model.Klient;

import java.util.Random;

public class FederatKasa extends AbstractFederat
{
    private static final String federateName = "FederatKasa";
    private Random rand = new Random();

    public static void main(String[] args)
    {
        try
        {
            new FederatKasa().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
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
            advanceTime(timeStep);
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
//            //Pobieranie klientów do obsługi
//            for(int i = 0; i < listaKas.size(); i++)
//            {
//                if(listaKas.get(i).aktualnieObslugiwanyKlient == null)
//                {
//                    if(listaKas.get(i).kolejkaKlientow.size() > 0)
//                    {
//                        listaKas.get(i).aktualnieObslugiwanyKlient = listaKas.get(i).kolejkaKlientow.get(0);
//                    }
//                }
//            }
//
//            //Obsługiwanie klientów
//            for(int i = 0; i < listaKas.size(); i++)
//            {
//                if(listaKas.get(i).aktualnieObslugiwanyKlient != null)
//                {
//                    boolean czyZostalObsluzony = listaKas.get(i).aktualnieObslugiwanyKlient.czyZostalObsluzony(fedamb.getFederateTime());
//                    if(czyZostalObsluzony)
//                    {
//                        usunKlientaZKasy(listaKas.get(i).aktualnieObslugiwanyKlient.ID, listaKas.get(i).ID);
//                        listaKas.get(i).aktualnieObslugiwanyKlient = null;
//                    }
//                }
//            }
//        }
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

//            subscribeOtworzKase();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }

}
