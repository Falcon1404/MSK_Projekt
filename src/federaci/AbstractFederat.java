package federaci;

import ambasador.Ambasador;
import hla.rti.*;
import hla.rti.jlc.RtiFactoryFactory;
import model.Kasa;
import model.Klient;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.*;

public abstract class AbstractFederat
{
    public static final String ID = "ID";
    public static final String ILOSC_TOWAROW = "iloscTowarow";
    public static final String CZY_VIP = "czyVIP";
    public static final String NR_KASY = "nrKasy";
    public static final String POZYCJA_w_KOLEJCE = "pozycjaWKolejce";

    public static final String LICZBA_KLIENTOW_w_KOLEJCE = "liczbaKlientowWKolejce";
    public static final String CZY_PRZEPELNIONA = "czyPrzepelniona";

    public static final String ID_KLIENT = "IDKlient";
    public static final String ID_KASA = "IDKasa";

    public static final String HLA_KLIENT = "HLAobjectRoot.Klient";
    public static final String HLA_KASA = "HLAobjectRoot.Kasa";

    public static final String HLA_ROZPOCZECIE_OBSLUGI = "HLAinteractionRoot.RozpoczecieObslugi";
    public static final String HLA_ZAKONCZENIE_OBSLUGI = "HLAinteractionRoot.ZakonczenieObslugi";
    public static final String HLA_WEJSCIE_DO_KOLEJKI = "HLAinteractionRoot.WejscieDoKolejki";
    public static final String HLA_OTWORZ_KASE = "HLAinteractionRoot.OtworzKase";

    public static final String FOM_PATH = "src/fom/sklep.xml";
    public static final String federationName = Ambasador.FEDERATION_NAME;
    public static final String READY_TO_RUN = Ambasador.READY_TO_RUN;
    //Mapa obiektów i ich klas
    protected HashMap<Integer, Integer> objectAndClassHandleMap = new HashMap<>();
    public ArrayList<Klient> listaKlientow = new ArrayList<>();
    public ArrayList<Kasa> listaKas = new ArrayList<>();
    public final double timeStep = 10.0;

    public RTIambassador rtiamb;
    public Ambasador fedamb;

    private void log(String message)
    {
        System.out.println("FederatKlient: " + message);
    }

    protected void createFederation()
    {
        try
        {
            rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
        }
        catch (RTIinternalError rtIinternalError)
        {
            rtIinternalError.printStackTrace();
        }

        try
        {
            File fom = new File(FOM_PATH);
            rtiamb.createFederationExecution(fedamb.FEDERATION_NAME, fom.toURI().toURL());
            log("Created Federation");
        }
        catch (FederationExecutionAlreadyExists exists)
        {
            log("Didn't create federation, it already existed");
        }
        catch (MalformedURLException urle)
        {
            log("Exception processing fom: " + urle.getMessage());
        }
        catch (ConcurrentAccessAttempted | RTIinternalError | CouldNotOpenFED | ErrorReadingFED concurrentAccessAttempted) {
            concurrentAccessAttempted.printStackTrace();
        }
    }

    protected void joinFederation(String federateName)
    {
        try
        {
            rtiamb.joinFederationExecution(federateName, federationName, fedamb);
        }
        catch (FederateAlreadyExecutionMember | FederationExecutionDoesNotExist | SaveInProgress | RTIinternalError | RestoreInProgress | ConcurrentAccessAttempted federateAlreadyExecutionMember)
        {
            federateAlreadyExecutionMember.printStackTrace();
        }
        log("Joined Federation as " + federateName);
    }

    protected void registerSyncPoint()
    {
        try
        {
            rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        }
        catch (FederateNotExecutionMember | SaveInProgress | RTIinternalError | RestoreInProgress | ConcurrentAccessAttempted federateNotExecutionMember)
        {
            federateNotExecutionMember.printStackTrace();
        }

        while (!fedamb.isAnnounced)
        {
            try
            {
                rtiamb.tick();
            }
            catch (RTIinternalError | ConcurrentAccessAttempted rtIinternalError)
            {
                rtIinternalError.printStackTrace();
            }
        }
    }

    public void waitForUser()
    {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try
        {
            reader.readLine();
        }
        catch(Exception e)
        {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void achieveSyncPoint()
    {
        try
        {
            rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        }
        catch (SynchronizationLabelNotAnnounced | ConcurrentAccessAttempted | RTIinternalError | RestoreInProgress | SaveInProgress | FederateNotExecutionMember synchronizationLabelNotAnnounced)
        {
            synchronizationLabelNotAnnounced.printStackTrace();
        }
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (!fedamb.isReadyToRun)
        {
            try
            {
                rtiamb.tick();
            }
            catch (RTIinternalError | ConcurrentAccessAttempted rtIinternalError)
            {
                rtIinternalError.printStackTrace();
            }
        }
    }

    protected void enableTimePolicy() throws RTIexception
    {
        LogicalTime currentTime = convertTime(fedamb.federateTime);
        LogicalTimeInterval lookahead = convertInterval(fedamb.federateLookahead);

        try
        {
            this.rtiamb.enableTimeRegulation(currentTime, lookahead);
            while (!fedamb.isRegulating)
            {
                rtiamb.tick();
            }
        }
        catch (TimeRegulationAlreadyEnabled | EnableTimeRegulationPending | TimeAdvanceAlreadyInProgress | InvalidLookahead | InvalidFederationTime | FederateNotExecutionMember | SaveInProgress | RTIinternalError | RestoreInProgress | ConcurrentAccessAttempted timeRegulationAlreadyEnabled)
        {
            timeRegulationAlreadyEnabled.printStackTrace();
        }

        try
        {
            this.rtiamb.enableTimeConstrained();
            while (!fedamb.isConstrained)
            {
                rtiamb.tick();
            }
        }
        catch (TimeConstrainedAlreadyEnabled | EnableTimeConstrainedPending | FederateNotExecutionMember | TimeAdvanceAlreadyInProgress | RestoreInProgress | SaveInProgress | ConcurrentAccessAttempted | RTIinternalError timeConstrainedAlreadyEnabled)
        {
            timeConstrainedAlreadyEnabled.printStackTrace();
        }
    }


    public void subscribeKasa() throws NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, AttributeNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        /*HashMap<String, Class<?>> parametersAndTypes = new HashMap<>();
        parametersAndTypes.put(ID, Integer.class);
        parametersAndTypes.put(LICZBA_KLIENTOW_w_KOLEJCE, Integer.class);
        parametersAndTypes.put(CZY_PRZEPELNIONA, Boolean.class);
        fedamb.federatKasaClassHandle = setFomObject(rtiamb.getObjectClassHandle(HLA_KASA), parametersAndTypes);*/

        fedamb.federatKasaClassHandle = rtiamb.getObjectClassHandle(HLA_KASA);
        fedamb.federatKasaIDAttributeHandle = rtiamb.getAttributeHandle(ID, fedamb.federatKasaClassHandle);
        fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getAttributeHandle(LICZBA_KLIENTOW_w_KOLEJCE, fedamb.federatKasaClassHandle);
        fedamb.federatKasaCzyPrzepelnionaAttributeHandle = rtiamb.getAttributeHandle(CZY_PRZEPELNIONA, fedamb.federatKasaClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(fedamb.federatKasaIDAttributeHandle);
        attributes.add(fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle);
        attributes.add(fedamb.federatKasaCzyPrzepelnionaAttributeHandle);

        rtiamb.subscribeObjectClassAttributes(fedamb.federatKasaClassHandle, attributes);
    }

    public void subscribeKlient() throws NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, AttributeNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.federatKlientClassHandle = rtiamb.getObjectClassHandle(HLA_KLIENT);
        fedamb.federatKlientIDAttributeHandle = rtiamb.getAttributeHandle(ID, fedamb.federatKlientClassHandle);
        fedamb.federatKlientIloscTowarowAttributeHandle = rtiamb.getAttributeHandle(ILOSC_TOWAROW, fedamb.federatKlientClassHandle);
        fedamb.federatKlientCzyVIPAttributeHandle = rtiamb.getAttributeHandle(CZY_VIP, fedamb.federatKlientClassHandle);
        fedamb.federatKlientNrKasyAttributeHandle = rtiamb.getAttributeHandle(NR_KASY, fedamb.federatKlientClassHandle);
        fedamb.federatKlientPozycjaWKolejceAttributeHandle = rtiamb.getAttributeHandle(POZYCJA_w_KOLEJCE, fedamb.federatKlientClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(fedamb.federatKlientIDAttributeHandle);
        attributes.add(fedamb.federatKlientIloscTowarowAttributeHandle);
        attributes.add(fedamb.federatKlientCzyVIPAttributeHandle);
        attributes.add(fedamb.federatKlientNrKasyAttributeHandle);
        attributes.add(fedamb.federatKlientPozycjaWKolejceAttributeHandle);

        rtiamb.subscribeObjectClassAttributes(fedamb.federatKlientClassHandle, attributes);
    }

    public void publishKasa() throws NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, AttributeNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.federatKasaClassHandle = rtiamb.getObjectClassHandle(HLA_KASA);
        fedamb.federatKasaIDAttributeHandle = rtiamb.getAttributeHandle(ID, fedamb.federatKasaClassHandle);
        fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getAttributeHandle(LICZBA_KLIENTOW_w_KOLEJCE, fedamb.federatKasaClassHandle);
        fedamb.federatKasaCzyPrzepelnionaAttributeHandle = rtiamb.getAttributeHandle(CZY_PRZEPELNIONA, fedamb.federatKasaClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(fedamb.federatKasaIDAttributeHandle);
        attributes.add(fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle);
        attributes.add(fedamb.federatKasaCzyPrzepelnionaAttributeHandle);

        rtiamb.publishObjectClass(fedamb.federatKasaClassHandle, attributes);
    }

    public void publishKlient() throws NameNotFound, FederateNotExecutionMember, RTIinternalError, ObjectClassNotDefined, ConcurrentAccessAttempted, AttributeNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        /*HashMap<String, Class<?>> parametersAndTypes = new HashMap<>();
        parametersAndTypes.put(ID, Integer.class);
        parametersAndTypes.put(ILOSC_TOWAROW, Integer.class);
        parametersAndTypes.put(NR_KASY, Integer.class);
        parametersAndTypes.put(POZYCJA_w_KOLEJCE, Integer.class);
        parametersAndTypes.put(CZY_VIP, Boolean.class);
        fedamb.federatKlientClassHandle = setFomObject(rtiamb.getObjectClassHandle(HLA_KLIENT), parametersAndTypes);*/

        fedamb.federatKlientClassHandle = rtiamb.getObjectClassHandle(HLA_KLIENT);
        fedamb.federatKlientIDAttributeHandle = rtiamb.getAttributeHandle(ID, fedamb.federatKlientClassHandle);
        fedamb.federatKlientIloscTowarowAttributeHandle = rtiamb.getAttributeHandle(ILOSC_TOWAROW, fedamb.federatKlientClassHandle);
        fedamb.federatKlientCzyVIPAttributeHandle = rtiamb.getAttributeHandle(CZY_VIP, fedamb.federatKlientClassHandle);
        fedamb.federatKlientNrKasyAttributeHandle = rtiamb.getAttributeHandle(NR_KASY, fedamb.federatKlientClassHandle);
        fedamb.federatKlientPozycjaWKolejceAttributeHandle = rtiamb.getAttributeHandle(POZYCJA_w_KOLEJCE, fedamb.federatKlientClassHandle);

        AttributeHandleSet attributes = RtiFactoryFactory.getRtiFactory().createAttributeHandleSet();
        attributes.add(fedamb.federatKlientIDAttributeHandle);
        attributes.add(fedamb.federatKlientIloscTowarowAttributeHandle);
        attributes.add(fedamb.federatKlientCzyVIPAttributeHandle);
        attributes.add(fedamb.federatKlientNrKasyAttributeHandle);
        attributes.add(fedamb.federatKlientPozycjaWKolejceAttributeHandle);

        rtiamb.publishObjectClass(fedamb.federatKlientClassHandle, attributes);
    }

    public void publishWejscieDoKolejki() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.wejscieDoKolejkiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_WEJSCIE_DO_KOLEJKI);
        fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.wejscieDoKolejkiInteractionHandle);
        fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(ID_KASA, fedamb.wejscieDoKolejkiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.wejscieDoKolejkiInteractionHandle);
    }

    public void subscribeWejscieDoKolejki() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.wejscieDoKolejkiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_WEJSCIE_DO_KOLEJKI);
        fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.wejscieDoKolejkiInteractionHandle);
        fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(ID_KASA, fedamb.wejscieDoKolejkiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.wejscieDoKolejkiInteractionHandle);
    }

    public void publishRozpocznijObsluge() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void subscribeRozpocznijObsluge() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void publishZakonczObsluge() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.zakonczenieObslugiInteractionHandle);
    }

    public void subscribeZakonczObsluge() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.zakonczenieObslugiInteractionHandle);
    }

    public void publishOtworzKase() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.otworzKaseInteractionHandle = rtiamb.getInteractionClassHandle(HLA_OTWORZ_KASE);
        rtiamb.publishInteractionClass(fedamb.otworzKaseInteractionHandle);
    }

    public void subscribeOtworzKase() throws NameNotFound, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, FederateLoggingServiceCalls, ConcurrentAccessAttempted, InteractionClassNotDefined, RestoreInProgress, SaveInProgress, OwnershipAcquisitionPending
    {
        fedamb.otworzKaseInteractionHandle = rtiamb.getInteractionClassHandle(HLA_OTWORZ_KASE);
        rtiamb.subscribeInteractionClass(fedamb.otworzKaseInteractionHandle);
    }

    private LogicalTime convertTime(double time)
    {
        return new DoubleTime(time);
    }

    private LogicalTimeInterval convertInterval(double time)
    {
        return new DoubleTimeInterval(time);
    }

    public void advanceTime(double timeToAdvance)
    {
        fedamb.isAdvancing = true;
        LogicalTime newTime = convertTime(fedamb.federateTime + timeToAdvance);
        try
        {
            rtiamb.timeAdvanceRequest(newTime);
            while (fedamb.isAdvancing)
            {
                rtiamb.tick();
            }
        }
        catch (InvalidFederationTime | FederationTimeAlreadyPassed | EnableTimeRegulationPending | TimeAdvanceAlreadyInProgress | FederateNotExecutionMember | EnableTimeConstrainedPending | RestoreInProgress | SaveInProgress | ConcurrentAccessAttempted | RTIinternalError invalidFederationTime)
        {
            invalidFederationTime.printStackTrace();
        }
    }

    public void aktualizacjaKasy(int ID, int liczbaKlientowWKolejce, boolean czyPrzepelniona)
    {
        if(listaKas.size() > 0)
        {
            for(int i = 0; i < listaKas.size(); i++)
            {
                if(listaKas.get(i).ID == ID)
                {
                    listaKas.get(i).liczbaKlientowWKolejce = liczbaKlientowWKolejce;
                    listaKas.get(i).czyPrzepelniona = czyPrzepelniona;
                }
            }
        }
    }

    public void dodajKlientaDoKasy(int ID, Klient klient)
    {
        if(listaKas.size() > 0)
        {
            for(int i = 0; i < listaKas.size(); i++)
            {
                if(listaKas.get(i).ID == ID)
                {
                    listaKas.get(i).liczbaKlientowWKolejce++;
                    listaKas.get(i).addKlient(klient);
                    if(listaKas.get(i).liczbaKlientowWKolejce >= listaKas.get(i).MAX_LICZBA_KLIENTOW)
                    {
                        listaKas.get(i).czyPrzepelniona = true;
                    }
                }
            }
        }
    }

    public void aktualizujCzasRozpoczeciaObslugi(int ID, double czasRozpoczeciaObslugi)
    {
        for(int i = 0; i < listaKlientow.size(); i++)
        {
            if(listaKlientow.get(i).ID == ID)
            {
                listaKlientow.get(i).rozpoczecieObslugi = czasRozpoczeciaObslugi;
            }
        }
    }

    public void aktualizujCzasZakonczeniaObslugi(int ID, double czasZakonczeniaObslugi)
    {
        for(int i = 0; i < listaKlientow.size(); i++)
        {
            if(listaKlientow.get(i).ID == ID)
            {
                listaKlientow.get(i).zakonczenieObslugi = czasZakonczeniaObslugi;
                listaKlientow.get(i).czyZostalObsluzony = true;
            }
        }
    }

    public void aktualizujCzasWejsciaDoKolejki(int ID, double czasWejsciaDoKolejki)
    {
        for(int i = 0; i < listaKlientow.size(); i++)
        {
            if(listaKlientow.get(i).ID == ID)
            {
                listaKlientow.get(i).wejscieDoKolejki = czasWejsciaDoKolejki;
                listaKlientow.get(i).czySkonczylRobicZakupy = true;
            }
        }
    }

    public void usunKlientaZKasy(int IDKlient, int IDKasa)
    {
        for(int i = 0; i < listaKlientow.size(); i++)
        {
            if(listaKlientow.get(i).ID == IDKlient)
            {
                listaKlientow.remove(i);
            }
        }
        for(int i = 0; i < listaKas.size(); i++)
        {
            if(listaKas.get(i).ID == IDKasa)
            {
                listaKas.get(i).liczbaKlientowWKolejce--;
                for(int j = 0; j < listaKas.get(i).kolejkaKlientow.size(); j++)
                {
                    if(listaKas.get(i).kolejkaKlientow.get(j).ID == IDKlient)
                    {
                        listaKas.get(i).kolejkaKlientow.remove(j);
                    }
                }
            }
        }
    }
}
