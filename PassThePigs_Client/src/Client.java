import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Client extends JFrame implements CzatProtokol {

    private static final String MESSAGE_PREFIX = "MESSAGE:";
    //GUI
    private JButton rzucaj, rezygnuj, polacz, rozlacz;
    private JPanel panelDol;
    private JPanel panelGora;
    private JTextField host, port, wiadomosc;
    private JTextArea komunikaty;
    private JList zalogowani;
    private DefaultListModel listaZalogowanych;

    //Klient
    private String nazwaSerwera = "localhost";
    private int numerPortu = 23;
    private boolean polaczony = false;
    private boolean zalogowany = false;
    private Klient watekKlienta;

    public Client(){
        super("Klient");
        setSize(600, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panelDol = new JPanel(new FlowLayout());
        panelGora = new JPanel(new FlowLayout());
        komunikaty = new JTextArea();
        komunikaty.setLineWrap(true);
        komunikaty.setEditable(false);

        wiadomosc = new JTextField();
        rzucaj = new JButton("Rzucaj");
        rezygnuj = new JButton("Rezygnuj");
        host = new JTextField(nazwaSerwera ,12);
        port = new JTextField((new Integer(numerPortu)).toString() ,8);
        polacz = new JButton("Połącz");
        rozlacz = new JButton("Rozłącz");
        rozlacz.setEnabled(false);

        listaZalogowanych = new DefaultListModel();
        zalogowani = new JList(listaZalogowanych);
        zalogowani.setFixedCellWidth(200);

        ObslugaZdarzen obsluga = new ObslugaZdarzen();

        rzucaj.addActionListener(obsluga);
        rezygnuj.addActionListener(obsluga);
        polacz.addActionListener(obsluga);
        rozlacz.addActionListener(obsluga);

        panelDol.add(rzucaj);
        panelDol.add(rezygnuj);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                rozlacz.doClick();
                setVisible(false);
                System.exit(0);
            }
        });

        panelGora.add(new JLabel("Serwer: "));
        panelGora.add(host);
        panelGora.add(new JLabel("Port: "));
        panelGora.add(port);
        panelGora.add(polacz);
        panelGora.add(rozlacz);

        add(panelGora, BorderLayout.NORTH);
        add(panelDol, BorderLayout.SOUTH);
        add(new JScrollPane(zalogowani), BorderLayout.EAST);
        add(new JScrollPane(komunikaty), BorderLayout.CENTER);
        setVisible(true);

    }

    private class ObslugaZdarzen implements ActionListener, CzatProtokol {

        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("Połącz")) {
                obsluzKomunikat(MESSAGE_PREFIX + "Łączę z: " + nazwaSerwera + " na porcie: " + numerPortu + "...");
                polacz.setEnabled(false);
                rozlacz.setEnabled(true);
                host.setEnabled(false);
                port.setEnabled(false);
                watekKlienta = new Klient();
                watekKlienta.start();
            }
            if (e.getActionCommand().equals("Rozłącz")){
                if (polaczony && zalogowany) watekKlienta.wyslij(QUIT_COMMAND);
                polaczony = false;
                rozlacz.setEnabled(false);
                polacz.setEnabled(true);
                host.setEnabled(true);
                port.setEnabled(true);
                obsluzKomunikat(MESSAGE_PREFIX + "ROZŁĄCZONO!");
            }

            if (e.getActionCommand().equals("Rzucaj")) {
                watekKlienta.wyslij("");
            }
            if (e.getActionCommand().equals("Rezygnuj")) {
                watekKlienta.wyslij(RESIGN_COMMAND);
            }

        }

    }

    private class Klient extends Thread implements CzatProtokol{
        private Socket socket;
        private BufferedReader wejscie;
        private PrintWriter wyjscie;


        public void run(){

            try {
                socket = new Socket(host.getText(), new Integer(port.getText()));
                polaczony = true;

                wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                wyjscie = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                String lancuch = null;
                String nick = JOptionPane.showInputDialog(null, "Podaj nick: ");
                if (nick!=null) {
                    zalogowany = true;
                    wyslij(NICK_COMMAND + nick);
                }
                else rozlacz.doClick();

                while(polaczony &&  (lancuch = wejscie.readLine()) != null){
                    obsluzKomunikat(lancuch);
                }

            }

            catch (UnknownHostException e) {
                obsluzKomunikat(MESSAGE_PREFIX + "Błąd połączenia!");
                rozlacz.doClick();
                polaczony = false;

            }
            catch (IOException e) {
            }finally {
                try {
                    if (polaczony) {
                        wejscie.close(); // zamknięcie wejścia
                        wyjscie.close(); // zamknięcie wyjścia
                        socket.close(); // zamknięcie socketu
                    }
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }
        }

        public void wyslij(String tekst) {
            wyjscie.println(tekst);
            wiadomosc.setText("");
        }
    }

    private void obsluzKomunikat (String tekst){

        if (tekst.startsWith(MESSAGE_PREFIX)) {
            komunikaty.append(tekst.substring(MESSAGE_PREFIX.length()) + "\n");
            komunikaty.setCaretPosition(komunikaty.getDocument().getLength());
        }
        else if (tekst.startsWith(RESULT_COMMAND)) {
            tekst = tekst.substring(RESULT_COMMAND.length());
            String[] wyniki = tekst.split("-");
            obsluzKomunikat(MESSAGE_PREFIX +"\nFigurka 1 spadła na: " + wyniki[0] );
            obsluzKomunikat(MESSAGE_PREFIX +"Figurka 2 spadła na: " + wyniki[1]);
        }
        else if(tekst.equals(YOUR_TURN_COMMAND)){
            //Aktualizacja listy
            obsluzKomunikat(MESSAGE_PREFIX +"\nTWOJA KOLEJ!\n");
        }

        else if(tekst.equals(TURN_LOST_COMMAND)){
            //Aktualizacja listy
            obsluzKomunikat(MESSAGE_PREFIX +"\nZerowanie puktów w turze!\n");
        }
        else if (tekst.startsWith(GOT_POINTS_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX +"\nZdobyłeś " +  tekst.substring(GOT_POINTS_COMMAND.length()) + "  punktów\n");
        }

        else if (tekst.startsWith(ELSE_POINTS_COMMAND)) {
            tekst = tekst.substring(ELSE_POINTS_COMMAND.length());
            String[] punkty = tekst.split("-");
            obsluzKomunikat(MESSAGE_PREFIX +"\n" +  punkty[0] + " +" + punkty[1] + " punktów" );
        }
        else if (tekst.startsWith(TURN_POINTS_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX + "\nPunkty w tej turze: " +  tekst.substring(TURN_POINTS_COMMAND.length()) + "\n");
        }
        else if(tekst.equals(TOUCHING_COMMAND)){
            //Aktualizacja listy
            obsluzKomunikat(MESSAGE_PREFIX +"\nFigurki stykają się!");
            obsluzKomunikat(MESSAGE_PREFIX + "Zerowanie wszystkich punktów!\n");
        }

        else if (tekst.startsWith(THROWS_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX + "\nTeraz rzuca: " +  tekst.substring(THROWS_COMMAND.length()) + "\n");
        }

        else if (tekst.startsWith(END_TURN_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX + "\n***Koniec tury gracza: " +  tekst.substring(END_TURN_COMMAND.length()) + "*** \n");
        }

        else if(tekst.startsWith(USERS_LIST_COMMAND)){
            //Aktualizacja listy
            StringTokenizer gracze = new StringTokenizer(tekst.substring(USERS_LIST_COMMAND.length()), ",");
            listaZalogowanych.clear();
            int iloscOsob = gracze.countTokens();
            for(int i = 0; i < iloscOsob; i++) {
                listaZalogowanych.add(i, gracze.nextToken());
            }
        }

        else if(tekst.equals(YOU_WON_COMMAND)){
            //Aktualizacja listy
            JOptionPane.showMessageDialog(null,  "WYGRAŁEŚ!");
            rozlacz.doClick();
        }

        else if(tekst.equals(YOU_LOST_COMMAND)){
            //Aktualizacja listy
            JOptionPane.showMessageDialog(null, "PRZEGRAŁEŚ!");
            rozlacz.doClick();
        }

        else if (tekst.startsWith(JOINED_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX +"\nDo gry dołączył: " +  tekst.substring(JOINED_COMMAND.length()) + "\n");
        }

        else if(tekst.equals(CONNECTED_COMMAND)){
            obsluzKomunikat(MESSAGE_PREFIX +"\nPołączony z serwerem\n");
        }
        else if (tekst.startsWith(WAIT_COMMAND)) {
            obsluzKomunikat(MESSAGE_PREFIX + "Czekaj na podłączenie wszystkich graczy!");
        }
    }

    public static void main (String[] args) {
        new Client();
    }
}