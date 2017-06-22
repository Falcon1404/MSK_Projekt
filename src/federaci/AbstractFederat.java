package federaci;

import ambasador.AbstractAmbassador;
import hla.rti.*;
import hla.rti.jlc.EncodingHelpers;
import hla.rti.jlc.RtiFactoryFactory;
import model.Dane;
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
    public static final String FOM_PATH = "src/fom/sklep.xml";
    public static final String federationName = AbstractAmbassador.FEDERATION_NAME;
    public static final String READY_TO_RUN = AbstractAmbassador.READY_TO_RUN;

    private static final String federateName = "AbstractFederat";

    public ArrayList<Klient> listaKlientow = new ArrayList<>();
    public ArrayList<Kasa> listaKas = new ArrayList<>();
    private Random rand = new Random();
    public final double timeStep = 10.0;

    public RTIambassador rtiamb;
    public AbstractAmbassador fedamb;

    protected static void log(String message)
    {
        System.out.println(federateName + ": " + message);
    }

    public byte[] generateTag()
    {
        return ("" + System.currentTimeMillis()).getBytes();
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
        fedamb.isAnnounced = false;
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

    protected void disableTimePolicy() throws RTIexception
    {
        this.rtiamb.disableTimeRegulation();
        this.rtiamb.disableTimeConstrained();
    }

    public void subscribeKasa() throws RTIexception
    {
        fedamb.federatKasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.federatKasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.federatKasaInteractionHandle);

        rtiamb.subscribeInteractionClass(fedamb.federatKasaInteractionHandle);
    }

    public void publishKasa() throws RTIexception
    {
        fedamb.federatKasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.federatKasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.federatKasaInteractionHandle);

        rtiamb.publishInteractionClass(fedamb.federatKasaInteractionHandle);
    }

    public void subscribeKlient() throws RTIexception
    {
        fedamb.federatKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);

        fedamb.federatKlientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.federatKlientInteractionHandle);

        rtiamb.subscribeInteractionClass(fedamb.federatKlientInteractionHandle);
    }

    public void publishKlient() throws RTIexception
    {
        fedamb.federatKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);

        fedamb.federatKlientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.federatKlientInteractionHandle);

        rtiamb.publishInteractionClass(fedamb.federatKlientInteractionHandle);
    }

    public void publishWejscieDoKolejki() throws RTIexception
    {
        fedamb.wejscieDoKolejkiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_WEJSCIE_DO_KOLEJKI);
        fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.wejscieDoKolejkiInteractionHandle);
        fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.wejscieDoKolejkiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.wejscieDoKolejkiInteractionHandle);
    }

    public void subscribeWejscieDoKolejki() throws RTIexception
    {
        fedamb.wejscieDoKolejkiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_WEJSCIE_DO_KOLEJKI);
        fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.wejscieDoKolejkiInteractionHandle);
        fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.wejscieDoKolejkiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.wejscieDoKolejkiInteractionHandle);
    }

    public void publishRozpocznijObsluge() throws RTIexception
    {
        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void subscribeRozpocznijObsluge() throws RTIexception
    {
        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void publishZakonczObsluge() throws RTIexception
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.zakonczenieObslugiInteractionHandle);
    }

    public void subscribeZakonczObsluge() throws RTIexception
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.zakonczenieObslugiInteractionHandle);
    }

    public void publishOtworzKase() throws RTIexception
    {
        fedamb.otworzKaseInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_OTWORZ_KASE);
        rtiamb.publishInteractionClass(fedamb.otworzKaseInteractionHandle);
    }

    public void subscribeOtworzKase() throws RTIexception
    {
        fedamb.otworzKaseInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_OTWORZ_KASE);
        rtiamb.subscribeInteractionClass(fedamb.otworzKaseInteractionHandle);
    }

    public void publishStartSymulacji() throws RTIexception
    {
        fedamb.startSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_START_SYMULACJI);
        rtiamb.publishInteractionClass(fedamb.startSymulacjiHandle);
    }

    public void subscribeStartSymulacji() throws RTIexception
    {
        fedamb.startSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_START_SYMULACJI);
        rtiamb.subscribeInteractionClass(fedamb.startSymulacjiHandle);
    }

    public void publishStopSymulacji() throws RTIexception
    {
        fedamb.stopSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_STOP_SYMULACJI);
        rtiamb.publishInteractionClass(fedamb.stopSymulacjiHandle);
    }

    public void subscribeStopSymulacji() throws RTIexception
    {
        fedamb.stopSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_STOP_SYMULACJI);
        rtiamb.subscribeInteractionClass(fedamb.stopSymulacjiHandle);
    }

    public LogicalTime convertTime(double time)
    {
        return (LogicalTime) new DoubleTime(time);
    }

    private LogicalTimeInterval convertInterval(double time)
    {
        return (LogicalTimeInterval) new DoubleTimeInterval(time);
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

    private double getLbts()
    {
        return fedamb.federateTime + fedamb.federateLookahead;
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

    public void aktualizacjaKlienta(int ID, int iloscTowarow, boolean czyVIP, int nrKasy)
    {
        if(listaKlientow.size() > 0)
        {
            for(int i = 0; i < listaKlientow.size(); i++)
            {
                if(listaKlientow.get(i).ID == ID)
                {
                    listaKlientow.get(i).iloscTowarow = iloscTowarow;
                    listaKlientow.get(i).czyVIP = czyVIP;
                    listaKlientow.get(i).nrKasy = nrKasy;
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

    public void sendNowyKlientInteraction(Klient klient) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.federatKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);
        fedamb.federatKlientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.federatKlientInteractionHandle);
        fedamb.federatKlientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.federatKlientInteractionHandle);

        byte[] ID = EncodingHelpers.encodeInt(klient.ID);
        byte[] czasUtworzenia = EncodingHelpers.encodeDouble(klient.czasUtworzeniaKlienta);
        byte[] czasZakonczeniaZakupow = EncodingHelpers.encodeDouble(klient.czasZakoczeniaZakupow);
        byte[] IloscGotowki = EncodingHelpers.encodeDouble(klient.iloscGotowki);
        byte[] IloscTowarow = EncodingHelpers.encodeInt(klient.iloscTowarow);
        byte[] czyVIP = EncodingHelpers.encodeBoolean(klient.czyVIP);

        parameters.add(fedamb.federatKlientIDAttributeHandle, ID);
        parameters.add(fedamb.federatKlientCzasUtworzeniaAttributeHandle, czasUtworzenia);
        parameters.add(fedamb.federatKlientCzasZakonczeniaZakupowHandle, czasZakonczeniaZakupow);
        parameters.add(fedamb.federatKlientIloscGotowkiAttributeHandle, IloscGotowki);
        parameters.add(fedamb.federatKlientIloscTowarowAttributeHandle, IloscTowarow);
        parameters.add(fedamb.federatKlientCzyVIPAttributeHandle, czyVIP);

        rtiamb.sendInteraction(fedamb.federatKlientInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log(federateName + " wyslano klienta " + klient.ID);
    }

    public int getIDKlient()
    {
        int ID = 0;
        if(listaKlientow.size() > 0)
        {
            ID = listaKlientow.get(0).ID;
            for(int i = 1; i < listaKlientow.size(); i++)
            {
                if(ID < listaKlientow.get(i).ID)
                {
                    ID = listaKlientow.get(i).ID;
                }
            }
            ID += 1;
        }
        return ID;
    }

    public Klient generateAndAddKlient() throws RTIexception
    {
        int ID = getIDKlient();
        double czasUtworzeniaKlienta = fedamb.getFederateTime();
        double czasZakonczeniaZakupow = rand.nextDouble()*(800.0 - 400.0) + 400.0 + czasUtworzeniaKlienta;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt()*(6-1)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;
        }
        Klient klient = new Klient(ID, czasUtworzeniaKlienta, czasZakonczeniaZakupow, iloscTowarow, iloscGotowki);
        listaKlientow.add(klient);
        log("Dodano klienta " + ID);
        return klient;
    }

    public Klient generateAndAddKlientVIP() throws RTIexception
    {
        int ID = getIDKlient();
        double czasUtworzeniaKlienta = fedamb.getFederateTime();
        double czasZakonczeniaZakupow = rand.nextDouble()*(800.0 - 400.0) + 400.0 + czasUtworzeniaKlienta;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt()*(6-1)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;
        }
        Klient klient = new Klient(ID, czasUtworzeniaKlienta, czasZakonczeniaZakupow, iloscTowarow, iloscGotowki, true);
        listaKlientow.add(klient);
        log("Dodano klienta VIP " + ID);
        return klient;
    }

    public void sendNowaKasaInteraction(Kasa kasa) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.federatKasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.federatKasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.federatKasaInteractionHandle);
        fedamb.federatKasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.federatKasaInteractionHandle);

        byte[] ID = EncodingHelpers.encodeInt(kasa.ID);
        byte[] liczbaKlientowWKolejce = EncodingHelpers.encodeInt(kasa.liczbaKlientowWKolejce);
        byte[] czyPrzepelniona = EncodingHelpers.encodeBoolean(kasa.czyPrzepelniona);

        parameters.add(fedamb.federatKasaIDAttributeHandle, ID);
        parameters.add(fedamb.federatKasaLiczbaKlientowWKolejceAttributeHandle, liczbaKlientowWKolejce);
        parameters.add(fedamb.federatKasaCzyPrzepelnionaAttributeHandle, czyPrzepelniona);

        rtiamb.sendInteraction(fedamb.federatKasaInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log(federateName + " wyslano kase " + kasa.ID);
    }

    public int getIDKasa()
    {
        int ID = 0;
        if(listaKas.size() > 0)
        {
            ID = listaKas.get(0).ID;
            for(int i = 1; i < listaKas.size(); i++)
            {
                if(ID < listaKas.get(i).ID)
                {
                    ID = listaKas.get(i).ID;
                }
            }
            ID += 1;
        }
        return ID;
    }

    public Kasa generateAndAddKasa()
    {
        int ID = getIDKasa();
        Kasa kasa = new Kasa(ID, 0, false);
        log("Dodano kase " + ID);
        return kasa;
    }

}
