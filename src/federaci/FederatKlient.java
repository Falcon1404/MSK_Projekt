package federaci;

import ambasador.Ambasador;
import hla.rti.*;
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
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        createFederation();
        createAndJoinFederation();
        joinFederation(federateName);
        registerSyncPoint();
        waitForUser();
        achieveSyncPoint();
        enableTimePolicy();
        publishAndSubscribe();
    }

    protected void createAndJoinFederation() throws RTIexception
    {
        fedamb = new Ambasador(this);
        while(fedamb.running)
        {
            if (fedamb.czyTworzycKlienta)
            {
                //Nowy klient
                generateAndAddKlient();
                fedamb.czyTworzycKlienta = false;
            }
            if(fedamb.czyTworzycKase)
            {
                //nowa kasa
                fedamb.czyTworzycKase = false;
            }
            if(fedamb.czyAktualizowacKase)
            {
                //aktualizacja wybranej kasy
                aktualizacjaKasy(fedamb.IDAktualizowanejKasy, fedamb.dlugoscKolejki, fedamb.czyPrzepelniona);
            }
            //tworzenie klienta co jakiś czas
            if(rand.nextDouble() <= 0.2)
            {
                generateAndAddKlientVIP();
                fedamb.czyTworzycKlienta = false;
            }
            else
            {
                generateAndAddKlient();
                fedamb.czyTworzycKlienta = false;
            }

            //Aktualizacja robienia zakupów
            for(int i = 0; i < listaKlientow.size(); i++)
            {
                listaKlientow.get(i).czySkonczylRobicZakupy(fedamb.getFederateTime());
            }

            //Wybór kolejki
            for(int i = 0; i < listaKlientow.size(); i++)
            {
                if(listaKlientow.get(i).czySkonczylRobicZakupy)
                {
                    int najkrotszaKolejka = -1;
                    int IDKasa = -1;
                    if(listaKas.size() > 0)
                    {
                        for(int j = 0; j < listaKas.size(); j++)
                        {
                            if(!listaKas.get(j).czyPrzepelniona)
                            {
                                if(listaKas.get(j).liczbaKlientowWKolejce <= najkrotszaKolejka)
                                {
                                    najkrotszaKolejka = listaKas.get(j).liczbaKlientowWKolejce;
                                    IDKasa = listaKas.get(j).ID;
                                }
                            }
                        }
                    }
                    listaKlientow.get(i).nrKasy = IDKasa;
                    dodajKlientaDoKasy(IDKasa, listaKlientow.get(i));
                }
            }
            advanceTime(timeStep);
        }
    }

    public void generateAndAddKlient()
    {
        double federateTime = fedamb.getFederateTime();
        double czasRobieniaZakupow = rand.nextDouble()*(800.0 - 400.0) + 400.0 + federateTime;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt()*(6-1)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;
        }
        listaKlientow.add(new Klient(czasRobieniaZakupow, iloscTowarow, iloscGotowki));
    }

    public void generateAndAddKlientVIP()
    {
        double federateTime = fedamb.getFederateTime();
        double czasRobieniaZakupow = rand.nextDouble()*(800.0 - 400.0) + 400.0 + federateTime;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt()*(6-1)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;
        }
        listaKlientow.add(new Klient(czasRobieniaZakupow, iloscTowarow, iloscGotowki, true));
    }

    private void publishAndSubscribe()
    {
        try
        {
            publishKlient();
            subscribeKasa();

            publishWejscieDoKolejki();
            publishRozpocznijObsluge();
            publishZakonczObsluge();
        }
        catch(NameNotFound | FederateNotExecutionMember | RTIinternalError | InteractionClassNotDefined | SaveInProgress | ConcurrentAccessAttempted | RestoreInProgress | FederateLoggingServiceCalls | OwnershipAcquisitionPending | ObjectClassNotDefined | AttributeNotDefined e)
        {
            e.printStackTrace();
        }
    }
}

