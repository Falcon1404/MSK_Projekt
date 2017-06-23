package model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stan
{
    private JTextArea area;

    public Stan(JTextArea area)
    {
        this.area = area;
    }

    private String string = "";
    public List<String> historia = new ArrayList<>();
    public List<Integer> terenSklepu = new ArrayList<>();
    public Map<Integer, Integer> listaKas = new HashMap<>();
    public Map<Integer, List<Integer>> kolejkiDoKas = new HashMap<>();
    private int nrKasy;

    public void clearString()
    {
        string="";
    }
    public void appendToString(String str)
    {
        string +=str;
    }

    public void addToTextArea()
    {
        String str = toString();
        area.setText(str);
        historia.add(str);
    }

    public void dodajKlientNaTerenSklepu(int IDKlient)
    {
        terenSklepu.add(IDKlient);
        addToTextArea();
    }

    public void dodajKasa(int IDKasa)
    {
        listaKas.put(IDKasa, -1);
        kolejkiDoKas.put(IDKasa, new ArrayList<>());
        addToTextArea();
    }

    public void dodajKlientaDoKolejki(int IDKlient, int IDKasa)
    {
        kolejkiDoKas.get(IDKasa).add(IDKlient);
        addToTextArea();
    }

    public void dodajKlientaVIPDoKolejki(int IDKlient, int IDKasa)
    {
        kolejkiDoKas.get(IDKasa).add(0, IDKlient);
        addToTextArea();
    }

    public void usunKlientaZKolejki(int IDKlient, int IDKasa)
    {
        kolejkiDoKas.get(IDKasa).remove((Object)IDKlient);
        addToTextArea();
    }

    public void usunKlientaZKasy(int nrKlienta)
    {
        for (Integer integer : kolejkiDoKas.keySet())
        {
            kolejkiDoKas.get(integer).remove((Object)nrKlienta);
        }
        for (Integer integer : listaKas.keySet())
        {
            if(listaKas.get(integer) == nrKlienta)
            {
                listaKas.put(integer, -1);
            }
        }
        addToTextArea();
    }

    public void setNrKasy(int nrKasy)
    {
        this.nrKasy = nrKasy;
        addToTextArea();
    }

    public void usunKlienta(int theObject)
    {
        listaKas.forEach((integer, integer2) -> {
            if (integer2 == theObject)
            {
                setNrKasy(integer);
            }
        });
        listaKas.put(nrKasy, -1);
        addToTextArea();
    }

    public void usunKlientaZTerenuSklepu(Integer id)
    {
        terenSklepu.remove(id);
        addToTextArea();
    }

    public void klientJestObslugiwany(int nrKlienta, int nrKasy)
    {
        listaKas.put(nrKasy, nrKlienta);
        addToTextArea();
    }

    public void usunKase(int nrKasy)
    {
        listaKas.remove(nrKasy);
        addToTextArea();
    }

    public String toString()
    {
        clearString();
        appendToString("Wejscie:");
        for (Integer integer : terenSklepu)
        {
            appendToString("/"+integer+"/  ");
        }
        appendToString("\n");

        listaKas.forEach((integer, integer2) -> {
            if(integer!=0)
            {
                appendToString("" + integer + ":");
                try
                {
                    for (Integer integer4 : kolejkiDoKas.get(integer))
                    {
                        appendToString("/" + integer4 + "/  ");
                    }
                }
                catch (Exception e)
                {
                }
            }
            else
            {
            }
            appendToString("\n");
        });
        return string;
    }
}
