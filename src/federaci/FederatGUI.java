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
import java.util.Collections;
import java.util.Comparator;

public class FederatGUI extends AbstractFederat
{
    //GUI
    private JFrame frame;
    private JButton start;
    private JButton stop;
    private JButton nowyKlientZwykly;
    private JButton nowy10KlientZwykly;
    private JButton nowyVIP;
    private JButton nowaKasa;
    private JLabel klientText;
    private JLabel symulacjaText;
    private JLabel kasaText;
    private JLabel sredniCzasZakupowText;
    private JLabel sredniCzasZakupowValue;
    private JLabel sredniCzasPobytuWKolejceText;
    private JLabel sredniCzasPobytuWKolejceValue;
    private JLabel sredniCzasCzasObslugiText;
    private JLabel sredniCzasObslugiValue;
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


        sredniCzasZakupowText = new JLabel("Średni czas robienia zakupów: ");
        sredniCzasZakupowText.setVisible(true);
        sredniCzasZakupowText.setSize(300, 30);
        sredniCzasZakupowText.setLocation(130, 30);
        sredniCzasZakupowText.setFont(new Font("Calibri", Font.BOLD, 20));
        panel.add(sredniCzasZakupowText);

        sredniCzasZakupowValue = new JLabel("0.0");
        sredniCzasZakupowValue.setVisible(true);
        sredniCzasZakupowValue.setSize(100, 30);
        sredniCzasZakupowValue.setLocation(395, 30);
        sredniCzasZakupowValue.setFont(new Font("Calibri", Font.PLAIN, 20));
        panel.add(sredniCzasZakupowValue);


        sredniCzasPobytuWKolejceText = new JLabel("Średni czas pobytu w kolejce: ");
        sredniCzasPobytuWKolejceText.setVisible(true);
        sredniCzasPobytuWKolejceText.setSize(300, 30);
        sredniCzasPobytuWKolejceText.setLocation(640, 30);
        sredniCzasPobytuWKolejceText.setFont(new Font("Calibri", Font.BOLD, 20));
        panel.add(sredniCzasPobytuWKolejceText);

        sredniCzasPobytuWKolejceValue = new JLabel("0.0");
        sredniCzasPobytuWKolejceValue.setVisible(true);
        sredniCzasPobytuWKolejceValue.setSize(100, 30);
        sredniCzasPobytuWKolejceValue.setLocation(900, 30);
        sredniCzasPobytuWKolejceValue.setFont(new Font("Calibri", Font.PLAIN, 20));
        panel.add(sredniCzasPobytuWKolejceValue);


        sredniCzasCzasObslugiText = new JLabel("Średni czas obsługi w kasie: ");
        sredniCzasCzasObslugiText.setVisible(true);
        sredniCzasCzasObslugiText.setSize(300, 30);
        sredniCzasCzasObslugiText.setLocation(1160, 30);
        sredniCzasCzasObslugiText.setFont(new Font("Calibri", Font.BOLD, 20));
        panel.add(sredniCzasCzasObslugiText);

        sredniCzasObslugiValue = new JLabel("0.0");
        sredniCzasObslugiValue.setVisible(true);
        sredniCzasObslugiValue.setSize(100, 30);
        sredniCzasObslugiValue.setLocation(1400, 30);
        sredniCzasObslugiValue.setFont(new Font("Calibri", Font.PLAIN, 20));
        panel.add(sredniCzasObslugiValue);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        klientText = new JLabel("KLIENT");
        klientText.setVisible(true);
        klientText.setSize(100, 30);
        klientText.setLocation(790, 240);
        klientText.setFont(new Font("Calibri", Font.BOLD, 19));
        panel.add(klientText);

        nowyKlientZwykly = new JButton("Zwykly");
        nowyKlientZwykly.setEnabled(false);
        nowyKlientZwykly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlientow(false);
                fedamb.setCzyTworzycKlienta(true);
                fedamb.setCzyTworzycVIP(false);
            }
        });
        panel.add(nowyKlientZwykly);
        nowyKlientZwykly.setSize(100, 30);
        nowyKlientZwykly.setLocation(770, 270);

        nowy10KlientZwykly = new JButton(Dane.LICZBA_KLIENTOW + "x Zwykly");
        nowy10KlientZwykly.setEnabled(false);
        nowy10KlientZwykly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlientow(true);
                fedamb.setCzyTworzycKlienta(false);
                fedamb.setCzyTworzycVIP(false);
            }
        });
        panel.add(nowy10KlientZwykly);
        nowy10KlientZwykly.setSize(100, 30);
        nowy10KlientZwykly.setLocation(770, 310);


        nowyVIP = new JButton("VIP");
        nowyVIP.setEnabled(false);
        nowyVIP.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fedamb.setCzyTworzycKlientow(false);
                fedamb.setCzyTworzycKlienta(false);
                fedamb.setCzyTworzycVIP(true);
            }
        });
        panel.add(nowyVIP);
        nowyVIP.setSize(100, 30);
        nowyVIP.setLocation(770, 350);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        kasaText = new JLabel("KASA");
        kasaText.setVisible(true);
        kasaText.setSize(100, 30);
        kasaText.setLocation(800, 410);
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
        nowaKasa.setLocation(770, 440);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        symulacjaText = new JLabel("SYMULACJA");
        symulacjaText.setVisible(true);
        symulacjaText.setSize(100, 30);
        symulacjaText.setLocation(775, 110);
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
                nowy10KlientZwykly.setEnabled(true);
                nowyVIP.setEnabled(true);
                nowaKasa.setEnabled(true);
                start.setEnabled(false);
                stop.setEnabled(true);
            }
        });
        panel.add(start);
        start.setSize(100, 30);
        start.setLocation(770, 150);

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
        stop.setLocation(770, 190);

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
            Collections.sort(fedamb.listaInterakcji, new MyInteraction.MyInteractionComparator());
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
                if(myInteraction.interactionClass == fedamb.sredniCzasZakupowHandle)
                {
                    fedamb.obsluzSredniCzasZakupow(myInteraction.theInteraction, myInteraction.theTime);
                }
                if(myInteraction.interactionClass == fedamb.sredniCzasWKolejceHandle)
                {
                    fedamb.obsluzSredniCzasWKolejce(myInteraction.theInteraction, myInteraction.theTime);
                }
                if(myInteraction.interactionClass == fedamb.sredniCzasObslugiHandle)
                {
                    fedamb.obsluzSredniCzasObslugi(myInteraction.theInteraction, myInteraction.theTime);
                }

                if(fedamb.getCzyKlientWszedlDoKolejki())
                {
                    fedamb.setCzyKlientWszedlDoKolejki(false);
                    for (Klient klient : listaKlientow)
                    {
                        if(klient.ID == fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue)
                        {
                            log("Klient " + fedamb.IDKlientWejscieDoKolejkiInteractionAttributeValue + " wszedl do kolejki w kasie " +
                                    fedamb.IDKasaWejscieDoKolejkiInteractionAttributeValue);
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

                if(fedamb.getCzySredniCzasZakupow())
                {
                    fedamb.setCzySredniCzasZakupow(false);
                    sredniCzasZakupowValue.setText("" + fedamb.czasZakupowValue);
                }
                if(fedamb.getCzySredniCzasWKolejce())
                {
                    fedamb.setCzySredniCzasZakupow(false);
                    sredniCzasPobytuWKolejceValue.setText("" + fedamb.czasWKolejceValue);
                }
                if(fedamb.getCzySredniCzasObslugi())
                {
                    fedamb.setCzySredniCzasZakupow(false);
                    sredniCzasObslugiValue.setText("" + fedamb.czasObslugiValue);
                }
            }
            fedamb.listaInterakcji.clear();



            if(fedamb.getCzyStartSymulacji())
            {
                fedamb.setCzyStartSymulacji(false);
                sendStartInteraction();
                fedamb.running = true;
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
            if(fedamb.getCzyTworzycKlientow())
            {
                fedamb.setCzyTworzycKlientow(false);
                for(int i = 0; i < Dane.LICZBA_KLIENTOW; i++)
                {
                    Klient klient = generateAndAddKlient();
                    sendNowyKlientInteraction(klient);
                    stan.dodajKlientNaTerenSklepu(klient.ID);
                }
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
                log("Dodano kase " + kasa.ID + " na polecenie Menadzera.");
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
            subscribeSredniCzasZakupow();
            subscribeSredniCzasWKolejce();
            subscribeSredniCzasObslugi();
        }
        catch(RTIexception e)
        {
            e.printStackTrace();
        }
    }
}
