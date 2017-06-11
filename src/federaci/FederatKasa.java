package federaci;


import ambasador.Ambasador;
import hla.rti.*;

import java.util.Random;

public class FederatKasa extends AbstractFederat
{
    private static final String federateName = "FederatKasa";
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

            //Pobieranie klientów do obsługi
            for(int i = 0; i < listaKas.size(); i++)
            {
                if(listaKas.get(i).aktualnieObslugiwanyKlient == null)
                {
                    if(listaKas.get(i).kolejkaKlientow.size() > 0)
                    {
                        listaKas.get(i).aktualnieObslugiwanyKlient = listaKas.get(i).kolejkaKlientow.get(0);
                    }
                }
            }

            //Obsługiwanie klientów
            for(int i = 0; i < listaKas.size(); i++)
            {
                if(listaKas.get(i).aktualnieObslugiwanyKlient != null)
                {
                    boolean czyZostalObsluzony = listaKas.get(i).aktualnieObslugiwanyKlient.czyZostalObsluzony(fedamb.getFederateTime());
                    if(czyZostalObsluzony == true)
                    {
                        usunKlientaZKasy(listaKas.get(i).aktualnieObslugiwanyKlient.ID, listaKas.get(i).ID);
                        listaKas.get(i).aktualnieObslugiwanyKlient = null;
                    }
                }
            }
        }
    }

    private void publishAndSubscribe()
    {
        try
        {
            publishKasa();
            subscribeKlient();

            subscribeOtworzKase();
        }
        catch(NameNotFound | FederateNotExecutionMember | RTIinternalError | InteractionClassNotDefined | SaveInProgress | ConcurrentAccessAttempted | RestoreInProgress | FederateLoggingServiceCalls | OwnershipAcquisitionPending | ObjectClassNotDefined | AttributeNotDefined e)
        {
            e.printStackTrace();
        }
    }

}
