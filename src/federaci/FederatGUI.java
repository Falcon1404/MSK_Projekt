package federaci;


import ambasador.GUIAmbassador;
import hla.rti.RTIexception;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RtiFactoryFactory;
import model.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FederatGUI extends AbstractFederat
{
    //GUI
    private JFrame frame;
    private JButton start;
    private JButton stop;
    private JButton nowyKlientZwykly;
    private JButton nowyVIP;
    private JButton nowaKasa;
    private JLabel klientText;
    private JLabel symulacjaText;
    private JLabel kasaText;
    private static JTextArea textArea;
    private JScrollPane scrollPane;
    private JTextArea textArea2;
    private Stan stan;

    protected static void log(String message)
    {
        textArea.append(message + "\n");
        System.out.println(federateName + ": " + message);
    }

    private void createWindow()
    {
        frame = new JFrame("I6B2S4 Joanna Bednarko i Joanna Koszela - Metody i techniki symulacji komputerowej");
        JPanel panel = new JPanel();

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        klientText = new JLabel("KLIENT");
        klientText.setVisible(true);
        klientText.setSize(100, 30);
        klientText.setLocation(790, 270);
        klientText.setFont(new Font("Calibri", Font.BOLD, 19));
        panel.add(klientText);

        nowyKlientZwykly = new JButton("Zwykly");
        nowyKlientZwykly.setEnabled(false);
        nowyKlientZwykly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(true);
                fedamb.setCzyTworzycVIP(false);
            }
        });
        panel.add(nowyKlientZwykly);
        nowyKlientZwykly.setSize(100, 30);
        nowyKlientZwykly.setLocation(770, 300);


        nowyVIP = new JButton("VIP");
        nowyVIP.setEnabled(false);
        nowyVIP.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlienta(false);
                fedamb.setCzyTworzycVIP(true);
            }
        });
        panel.add(nowyVIP);
        nowyVIP.setSize(100, 30);
        nowyVIP.setLocation(770, 340);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        kasaText = new JLabel("KASA");
        kasaText.setVisible(true);
        kasaText.setSize(100, 30);
        kasaText.setLocation(800, 400);
        kasaText.setFont(new Font("Calibri", Font.BOLD, 19));
        panel.add(kasaText);

        nowaKasa = new JButton("Nowa Kasa");
        nowaKasa.setEnabled(false);
        nowaKasa.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKase(true);
            }
        });
        panel.add(nowaKasa);
        nowaKasa.setSize(100, 30);
        nowaKasa.setLocation(770, 430);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        symulacjaText = new JLabel("SYMULACJA");
        symulacjaText.setVisible(true);
        symulacjaText.setSize(100, 30);
        symulacjaText.setLocation(775, 100);
        symulacjaText.setFont(new Font("Calibri", Font.BOLD, 19));
        panel.add(symulacjaText);

        start = new JButton("START");
        start.setEnabled(true);
        start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyStartSymulacji(true);
                nowyKlientZwykly.setEnabled(true);
                nowyVIP.setEnabled(true);
                nowaKasa.setEnabled(true);
                start.setEnabled(false);
                stop.setEnabled(true);
            }
        });
        panel.add(start);
        start.setSize(100, 30);
        start.setLocation(770, 140);

        stop = new JButton("STOP");
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyStopSymulacji(true);
                nowyKlientZwykly.setEnabled(false);
                nowyVIP.setEnabled(false);
                nowaKasa.setEnabled(false);
                start.setEnabled(true);
                stop.setEnabled(false);
            }
        });
        panel.add(stop);
        stop.setSize(100, 30);
        stop.setLocation(770, 180);

        textArea = new JTextArea();
        textArea.setEnabled(true);
        textArea.setEditable(false);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);
        scrollPane.setBounds(30, 65, 475, 180);
        panel.add(scrollPane);
        scrollPane.setSize(700, 400);
        scrollPane.setLocation(50, 100);
        panel.add(scrollPane);

        textArea2 = new JTextArea();
        textArea2.setEnabled(true);
        textArea2.setLineWrap(true);
        textArea2.setEditable(false);
        stan = new Stan(textArea2);
        JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setViewportView(textArea2);
        scrollPane2.setBounds(30, 65, 475, 180);
        panel.add(scrollPane2);
        DefaultCaret caret2 = (DefaultCaret) textArea2.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane2.setSize(700, 400);
        scrollPane2.setLocation(900, 100);

        frame.add(panel);
        frame.setContentPane(panel);
        panel.setLayout(null);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(1650, 600);
        frame.setMaximumSize( new Dimension(1650, 600));
        frame.setMinimumSize( new Dimension(1650, 600));
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

    }

    public static void main(String[] args)
    {
        try
        {
            new FederatGUI().runFederate();
            log("Wystartowal " + federateName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runFederate() throws RTIexception
    {
        createWindow();
        federateName = "FederatGUI";
        fedamb = new GUIAmbassador();
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

            for(MyInteraction myInteraction : fedamb.listaInterakcji)
            {
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
                if (myInteraction.interactionClass == fedamb.otworzKaseInteractionHandle)
                {
                    fedamb.obsluzOtoworzKase(myInteraction.theInteraction, myInteraction.theTime);
                }

                if(fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue)
                        {
                            log("Klient " + fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue + " wszedl do kolejki w kasie " + fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
                            stan.usunKlientaZTerenuSklepu(fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue);
                            if(klient.czyVIP)
                            {
                                stan.dodajKlientaVIPDoKolejki(fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue, fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
                            }
                            else
                            {
                                stan.dodajKlientaDoKolejki(fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue, fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
                            }
                        }
                    }
                }
                if(fedamb.getCzyKlientJestObslugiwany())
                {
                    fedamb.setCzyKlientJestObslugiwany(false);
                    for (Klient klient : listaKlientow)
                    {
                        if (klient.ID == fedamb.IDKlientRozpoczecieObslugiValue)
                        {
                            log("Klient " + fedamb.IDKlientRozpoczecieObslugiValue + " jest obslugiwany w kasie " + fedamb.IDKasaRozpoczecieObslugiValue);
                            stan.usunKlientaZKolejki(fedamb.IDKlientRozpoczecieObslugiValue, fedamb.IDKasaRozpoczecieObslugiValue);
                            stan.klientJestObslugiwany(fedamb.IDKlientRozpoczecieObslugiValue, fedamb.IDKasaRozpoczecieObslugiValue);
                        }
                    }
                }
                if(fedamb.getCzyKlientZostalObsluzony())
                {
                    fedamb.setCzyKlientZostalObsluzony(false);
                    for (Klient klient : listaKlientow)
                    {
                        if (klient.ID == fedamb.IDKlientZakonczenieObslugiValue)
                        {
                            log("Klient " + fedamb.IDKlientZakonczenieObslugiValue + " zostal obsluzony w kasie " + fedamb.IDKasaZakonczenieObslugiValue);
                            stan.usunKlientaZKasy(fedamb.IDKlientZakonczenieObslugiValue);
                            stan.usunKlienta(fedamb.IDKlientZakonczenieObslugiValue);
                        }
                    }
                }
            }
            fedamb.listaInterakcji.clear();

            if(fedamb.getCzyStartSymulacji())
            {
                fedamb.setCzyStartSymulacji(false);
                sendStartInteraction();
            }
            if(fedamb.getCzyStopSymulacji())
            {
                fedamb.setCzyStopSymulacji(false);
                sendStopInteraction();
                fedamb.running = false;
            }
            if(fedamb.getCzyTworzycKlienta())
            {
                fedamb.setCzyTworzycKlienta(false);
                Klient klient = generateAndAddKlient();
                sendNowyKlientInteraction(klient);
                stan.dodajKlientNaTerenSklepu(klient.ID);
            }
            if(fedamb.getCzyTworzycVIP())
            {
                fedamb.setCzyTworzycVIP(false);
                Klient klient = generateAndAddKlientVIP();
                sendNowyKlientInteraction(klient);
                stan.dodajKlientNaTerenSklepu(klient.ID);
            }
            if(fedamb.getCzyTworzycKase())
            {
                fedamb.setCzyTworzycKase(false);
                Kasa kasa = generateAndAddKasa();
                sendNowaKasaInteraction(kasa);
                stan.dodajKasa(kasa.ID);
            }
            if(fedamb.getCzyOtworzycKase())
            {
                Kasa kasa = generateAndAddKasa();
                sendNowaKasaInteraction(kasa);
                stan.dodajKasa(kasa.ID);
                fedamb.setCzyOtworzycKase(false);
                log(federateName + " dodano kase " + fedamb.kasaIDAttributeValue + " na polecenie Menadzera.");
            }
            advanceTime(timeStep);
        }
        disableTimePolicy();
    }

    private void sendStartInteraction() throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        fedamb.startSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_START_SYMULACJI);
        rtiamb.sendInteraction(fedamb.startSymulacjiHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log("Wysłano polecenie START.");
    }

    private void sendStopInteraction() throws RTIexception
    {
        SuppliedParameters parameters = RtiFactoryFactory.getRtiFactory().createSuppliedParameters();
        fedamb.stopSymulacjiHandle = rtiamb.getInteractionClassHandle(Dane.HLA_STOP_SYMULACJI);
        rtiamb.sendInteraction(fedamb.stopSymulacjiHandle, parameters, "tag".getBytes(), convertTime(fedamb.getFederateTime() + 1.0));

        log("Wysłano polecenie STOP.");
    }

    private void publishAndSubscribe()
    {
        try
        {
            publishStartSymulacji();
            publishStopSymulacji();
            publishKlient();
            publishKasa();
            subscribeWejscieDoKolejki();
            subscribeRozpocznijObsluge();
            subscribeZakonczObsluge();
            subscribeOtworzKase();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }
}
