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

    public static String federateName = "AbstractFederat";

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
        fedamb.kasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.kasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.kasaInteractionHandle);
        fedamb.kasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.kasaInteractionHandle);
        fedamb.kasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.kasaInteractionHandle);

        rtiamb.subscribeInteractionClass(fedamb.kasaInteractionHandle);
    }

    public void publishKasa() throws RTIexception
    {
        fedamb.kasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.kasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.kasaInteractionHandle);
        fedamb.kasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.kasaInteractionHandle);
        fedamb.kasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.kasaInteractionHandle);

        rtiamb.publishInteractionClass(fedamb.kasaInteractionHandle);
    }

    public void subscribeKlient() throws RTIexception
    {
        fedamb.nowyKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);

        fedamb.klientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.nowyKlientInteractionHandle);

        rtiamb.subscribeInteractionClass(fedamb.nowyKlientInteractionHandle);
    }

    public void publishKlient() throws RTIexception
    {
        fedamb.nowyKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);

        fedamb.klientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.nowyKlientInteractionHandle);

        rtiamb.publishInteractionClass(fedamb.nowyKlientInteractionHandle);
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
        fedamb.IDKasaRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void subscribeRozpocznijObsluge() throws RTIexception
    {
        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        fedamb.IDKasaRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.rozpoczecieObslugiInteractionHandle);
        rtiamb.subscribeInteractionClass(fedamb.rozpoczecieObslugiInteractionHandle);
    }

    public void publishZakonczObsluge() throws RTIexception
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        fedamb.IDKasaZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.zakonczenieObslugiInteractionHandle);
        rtiamb.publishInteractionClass(fedamb.zakonczenieObslugiInteractionHandle);
    }

    public void subscribeZakonczObsluge() throws RTIexception
    {
        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        fedamb.IDKasaZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.zakonczenieObslugiInteractionHandle);
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
                    listaKas.get(i).setLiczbaKlientowWKolejce(liczbaKlientowWKolejce);
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
            for(Kasa kasa : listaKas)
            {
                if(kasa.ID == ID)
                {
                    kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce()+1);
                    kasa.addKlient(klient);
                    if(kasa.getLiczbaKlientowWKolejce() >= kasa.MAX_LICZBA_KLIENTOW)
                    {
                        kasa.czyPrzepelniona = true;
                        kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce());
                    }
                    else
                    {
                        kasa.czyPrzepelniona = false;
                        kasa.setLiczbaKlientowWKolejce(kasa.getLiczbaKlientowWKolejce());
                    }
                }
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
                listaKas.get(i).setLiczbaKlientowWKolejce(listaKas.get(i).getLiczbaKlientowWKolejce()-1);
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

    public void sendRozpoczecieObslugi(int IDKlient, int IDKasa) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.rozpoczecieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ROZPOCZECIE_OBSLUGI);
        fedamb.IDKlientRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.rozpoczecieObslugiInteractionHandle);
        fedamb.IDKasaRozpoczecieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.rozpoczecieObslugiInteractionHandle);

        byte[] IDKlientValue = EncodingHelpers.encodeInt(IDKlient);
        byte[] IDKasaValue = EncodingHelpers.encodeInt(IDKasa);
        parameters.add(fedamb.IDKlientRozpoczecieObslugiHandle, IDKlientValue);
        parameters.add(fedamb.IDKasaRozpoczecieObslugiHandle, IDKasaValue);
        rtiamb.sendInteraction(fedamb.rozpoczecieObslugiInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

//        log("Klient " + IDKlient + " jest obslugiwany w kasie " + IDKasa);
    }

    public void sendZakonczenieObslugi(int IDKlient, int IDKasa) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.zakonczenieObslugiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_ZAKONCZENIE_OBSLUGI);
        fedamb.IDKlientZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.zakonczenieObslugiInteractionHandle);
        fedamb.IDKasaZakonczenieObslugiHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.zakonczenieObslugiInteractionHandle);

        byte[] IDKlientValue = EncodingHelpers.encodeInt(IDKlient);
        byte[] IDKasaValue = EncodingHelpers.encodeInt(IDKasa);
        parameters.add(fedamb.IDKlientZakonczenieObslugiHandle, IDKlientValue);
        parameters.add(fedamb.IDKasaZakonczenieObslugiHandle, IDKasaValue);
        rtiamb.sendInteraction(fedamb.zakonczenieObslugiInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

//        log("Klient " + IDKlient + " zostal obslzony w kasie " + IDKasa);
    }

    public void sendWejscieDoKolejki(int IDKlient, int IDKasa) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.wejscieDoKolejkiInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_WEJSCIE_DO_KOLEJKI);
        fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KLIENT, fedamb.wejscieDoKolejkiInteractionHandle);
        fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle = rtiamb.getParameterHandle(Dane.ID_KASA, fedamb.wejscieDoKolejkiInteractionHandle);

        byte[] IDKlientValue = EncodingHelpers.encodeInt(IDKlient);
        byte[] IDKasaValue = EncodingHelpers.encodeInt(IDKasa);

        parameters.add(fedamb.IDKlientWejscieDoKolejkiInteractionAttributeHandle, IDKlientValue);
        parameters.add(fedamb.IDKasaWejscieDoKolejkiInteractionAttributeHandle, IDKasaValue);

        rtiamb.sendInteraction(fedamb.wejscieDoKolejkiInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

//        log("Klient " + IDKlient + " wszedl do kasy " + IDKasa);
    }

    public void sendNowyKlientInteraction(Klient klient) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.nowyKlientInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWY_KLIENT);
        fedamb.klientIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasUtworzeniaAttributeHandle = rtiamb.getParameterHandle(Dane.CZAS_UTWORZENIA, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzasZakonczeniaZakupowHandle = rtiamb.getParameterHandle(Dane.CZAS_ZAKONCZENIA_ZAKUPOW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscGotowkiAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_GOTOWKI, fedamb.nowyKlientInteractionHandle);
        fedamb.klientIloscTowarowAttributeHandle = rtiamb.getParameterHandle(Dane.ILOSC_TOWAROW, fedamb.nowyKlientInteractionHandle);
        fedamb.klientCzyVIPAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_VIP, fedamb.nowyKlientInteractionHandle);

        byte[] ID = EncodingHelpers.encodeInt(klient.ID);
        byte[] czasUtworzenia = EncodingHelpers.encodeDouble(klient.czasUtworzeniaKlienta);
        byte[] czasZakonczeniaZakupow = EncodingHelpers.encodeDouble(klient.czasZakoczeniaZakupow);
        byte[] IloscGotowki = EncodingHelpers.encodeDouble(klient.iloscGotowki);
        byte[] IloscTowarow = EncodingHelpers.encodeInt(klient.iloscTowarow);
        byte[] czyVIP = EncodingHelpers.encodeBoolean(klient.czyVIP);

        parameters.add(fedamb.klientIDAttributeHandle, ID);
        parameters.add(fedamb.klientCzasUtworzeniaAttributeHandle, czasUtworzenia);
        parameters.add(fedamb.klientCzasZakonczeniaZakupowHandle, czasZakonczeniaZakupow);
        parameters.add(fedamb.klientIloscGotowkiAttributeHandle, IloscGotowki);
        parameters.add(fedamb.klientIloscTowarowAttributeHandle, IloscTowarow);
        parameters.add(fedamb.klientCzyVIPAttributeHandle, czyVIP);

        rtiamb.sendInteraction(fedamb.nowyKlientInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        if(klient.czyVIP)
        {
            log("Stworzono klienta VIP " + klient.ID);
        }
        else
        {
            log("Stworzono klienta " + klient.ID);
        }
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
//        double czasZakonczeniaZakupow = rand.nextDouble()*(600.0 - 200.0) + 200.0 + czasUtworzeniaKlienta;
        double czasZakonczeniaZakupow = 600.0 + czasUtworzeniaKlienta;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt(6)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;

        }
        Klient klient = new Klient(ID, czasUtworzeniaKlienta, czasZakonczeniaZakupow, iloscTowarow, iloscGotowki);
        listaKlientow.add(klient);
//        log("Dodano klienta " + ID + " " + czasUtworzeniaKlienta + " " + czasZakonczeniaZakupow + " " + iloscTowarow + " " + iloscGotowki);
        return klient;
    }

    public Klient generateAndAddKlientVIP() throws RTIexception
    {
        int ID = getIDKlient();
        double czasUtworzeniaKlienta = fedamb.getFederateTime();
        //        double czasZakonczeniaZakupow = rand.nextDouble()*(600.0 - 200.0) + 200.0 + czasUtworzeniaKlienta;
        double czasZakonczeniaZakupow = 600.0 + czasUtworzeniaKlienta;
        double iloscGotowki = 0.0;
        int iloscTowarow = rand.nextInt(6)+1;
        for(int i = 0; i < iloscTowarow; i++)
        {
            iloscGotowki += rand.nextDouble()*(200.0 - 5.0) + 5.0;
        }
        Klient klient = new Klient(ID, czasUtworzeniaKlienta, czasZakonczeniaZakupow, iloscTowarow, iloscGotowki, true);
        listaKlientow.add(0, klient);
//        log("Dodano klienta " + ID + " " + czasUtworzeniaKlienta + " " + czasZakonczeniaZakupow + " " + iloscTowarow + " " + iloscGotowki);
        return klient;
    }

    public void sendNowaKasaInteraction(Kasa kasa) throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();

        fedamb.kasaInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_NOWA_KASA);

        fedamb.kasaIDAttributeHandle = rtiamb.getParameterHandle(Dane.ID, fedamb.kasaInteractionHandle);
        fedamb.kasaLiczbaKlientowWKolejceAttributeHandle = rtiamb.getParameterHandle(Dane.LICZBA_KLIENTOW_w_KOLEJCE, fedamb.kasaInteractionHandle);
        fedamb.kasaCzyPrzepelnionaAttributeHandle = rtiamb.getParameterHandle(Dane.CZY_PRZEPELNIONA, fedamb.kasaInteractionHandle);

        byte[] ID = EncodingHelpers.encodeInt(kasa.ID);
        byte[] liczbaKlientowWKolejce = EncodingHelpers.encodeInt(kasa.getLiczbaKlientowWKolejce());
        byte[] czyPrzepelniona = EncodingHelpers.encodeBoolean(kasa.czyPrzepelniona);

        parameters.add(fedamb.kasaIDAttributeHandle, ID);
        parameters.add(fedamb.kasaLiczbaKlientowWKolejceAttributeHandle, liczbaKlientowWKolejce);
        parameters.add(fedamb.kasaCzyPrzepelnionaAttributeHandle, czyPrzepelniona);

        rtiamb.sendInteraction(fedamb.kasaInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log("Wyslano kase " + kasa.ID);
    }

    public void sendOtworzKaseInteraction() throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        fedamb.otworzKaseInteractionHandle = rtiamb.getInteractionClassHandle(Dane.HLA_OTWORZ_KASE);
        rtiamb.sendInteraction(fedamb.otworzKaseInteractionHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));
//        log("Menedzer otworzyl nowa kase.");
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
        listaKas.add(kasa);
        log("Dodano kase " + ID);
        return kasa;
    }

}
