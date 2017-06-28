import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by anna on 09.05.2017.
 */
public class Server {
    private static ServerSocket server; // deklaracja zmiennej przechowującej socket na którym nasłuchuje serwer
    private static final  int PORT = 22; // definicja portu, na którym nasłuchuje serwer

    public static void main(String args[]){
        int maxIloscGraczy = 0;    // definicja zmiennej przechowującej ilość graczy na początkowe 0

        while (maxIloscGraczy < 2 || maxIloscGraczy>5)                  // dopóki ilość graczy nie będzie z zakresu 2 do 5
        {
            System.out.print("Podaj ilość graczy (od 2 do 5):");  // serwer prosi o jej wprowadzenie
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextInt()) maxIloscGraczy = sc.nextInt();        //dopisywanie liczby do zmiennej maxIloscGraczy jeśli jest liczbą całkowitą
        }
        try
        {
            server = new ServerSocket(PORT); // definicja socketu serwera na odpowiednim porcie
            System.out.println("Serwer gry uruchomiony na porcie: " + PORT);

            while (true){
                Socket socket = server.accept(); // włączenie akceptowania nowego połączenia
                InetAddress addr = socket.getInetAddress(); // definicja zmiennej przechowującej adres połączonego klienta
                System.out.println("Połączenie z adresu: "+ addr.getHostName() + " [" + addr.getHostAddress() + "]");
                new Uczestnik(socket, maxIloscGraczy).start(); // uruchomienie obsługi gry dla uczestnika
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Uczestnik extends Thread {
    static Vector<Uczestnik> uczestnicy = new Vector<Uczestnik>(); // definicja zmiennej przechowującej wszystkich uczestnikow;
    static int polaczeniGracze = 0;  //definicja zmiennej przechowującej ilość podłączonych uczestników
    static Uczestnik aktualnyUczestnik;
    static int maxIloscGraczy;
    static int ktoZacznie = 0;
    boolean pierwszy = false;
    int wszystkiePunkty = 0;
    int punktyWTurze = 0;
    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika

    public Uczestnik(Socket socket, int maxIloscGraczy) { // konstruktor obsługi nowego połączenia)
        this.socket = socket;
        if (this.maxIloscGraczy == 0)
        {
        this.maxIloscGraczy = maxIloscGraczy; }
    }

    class Rzut {
        int punktywRzucie;
        boolean stykajaSie = false;
        String [] ulozeniaFigurek = new String[2];

        String[] mozliweUlozenia = {
                "ucho",
                "ryjek",
                "nogi",
                "plecy",
                "prawy bok",
                "lewy bok"
        };

        public Rzut() {

                punktywRzucie =0;
                int losowanie;
                for (int i=0; i<ulozeniaFigurek.length; i++) {
                losowanie = (int) (Math.random() * 100);
                if (losowanie >= 0 && losowanie < 5) ulozeniaFigurek[i] = mozliweUlozenia[0];
                else if (losowanie >= 5 && losowanie < 15) ulozeniaFigurek[i] = mozliweUlozenia[1];
                 else if (losowanie >= 15 && losowanie < 30) ulozeniaFigurek[i] = mozliweUlozenia[2];
                  else if (losowanie >= 30 && losowanie < 50) ulozeniaFigurek[i] = mozliweUlozenia[3];
                   else if (losowanie >= 50 && losowanie < 75) ulozeniaFigurek[i] = mozliweUlozenia[4];
                    else if (losowanie >= 75 && losowanie < 100) ulozeniaFigurek[i] = mozliweUlozenia[5];
            }

                losowanie = (int) (Math.random() * 100);
                if (losowanie >= 0 && losowanie < 10) stykajaSie = true;

        }

         public void podajUlozenie() {
            wyslijDoWszystkich("Figurka 1 spadła na: " + ulozeniaFigurek[0]);
            wyslijDoWszystkich("Figurka 2 spadła na: " + ulozeniaFigurek[1]);
         }

         public int podliczPunkty() {

             if (!stykajaSie) {
                if ( (ulozeniaFigurek[0].equals(mozliweUlozenia[4]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[5]))) || (ulozeniaFigurek[0].equals(mozliweUlozenia[5]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[4])))) {
                    wyslijDoWszystkich("Zerowanie punktów w turze!");
                    punktywRzucie=-1;
                    punktyWTurze=0;
                }
                else {
                    if (ulozeniaFigurek[0].equals(ulozeniaFigurek[1]))
                        {
                        if(ulozeniaFigurek[0].equals(mozliweUlozenia[0])) punktywRzucie=60;
                         else if(ulozeniaFigurek[0].equals(mozliweUlozenia[1])) punktywRzucie=40;
                          else if(ulozeniaFigurek[0].equals(mozliweUlozenia[2])) punktywRzucie=20;
                           else if(ulozeniaFigurek[0].equals(mozliweUlozenia[3])) punktywRzucie=20;
                            else if(ulozeniaFigurek[0].equals(mozliweUlozenia[4])) punktywRzucie=1;
                             else if(ulozeniaFigurek[0].equals(mozliweUlozenia[5])) punktywRzucie=1;
                    }
                    else {
                        for (int i=0; i<ulozeniaFigurek.length; i++) {
                            if (ulozeniaFigurek[i].equals(mozliweUlozenia[0])) punktywRzucie+=15;
                             else if (ulozeniaFigurek[i].equals(mozliweUlozenia[1])) punktywRzucie+=10;
                              else if (ulozeniaFigurek[i].equals(mozliweUlozenia[2])) punktywRzucie+=5;
                               else if (ulozeniaFigurek[i].equals(mozliweUlozenia[3])) punktywRzucie+=5;

                        }
                    }
                    out.println("\nZdobyłeś "  + punktywRzucie + " punktów!");
                    punktyWTurze+=punktywRzucie;
                    out.println("\nPunkty w tej turze:" + punktyWTurze);
                    out.println("[enter] - rzucaj");
                    out.println("/p - wszystkie punkty");
                    out.println("/r - rezygnuj");
                    out.println("/q - wyjdź\n");
                    wyslijDoInnych(aktualnyUczestnik, nick + " +"  + punktywRzucie + " punktów !");

                }


            } else {
                wyslijDoWszystkich("Figurki stykają się!");
                wyslijDoWszystkich("Zerowanie wszystkich punktów!!");
                punktywRzucie=-1;
                punktyWTurze = 0;
                wszystkiePunkty = 0;
            }
             return punktywRzucie;
         }




        }


    private void wyslijDoJednego(Uczestnik uczestnik, String text)
    {
        uczestnik.out.println(text);
    }

    private synchronized void wyslijDoWszystkich(String tekst) { // metoda wysyłająca dane do wszytkich uczestników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
                    uczestnik.out.println(tekst); //do wszystkich  wysyłamy  tekst
        }
    }

    private synchronized  void wyslijDoInnych(Uczestnik zWyjatkiem, String tekst) { // metoda wysyłająca dane do pozostałych uczetników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
                if (uczestnik != zWyjatkiem)  // jeżeli uczestnik nie jest wykluczonym uczestnikiem
                uczestnik.out.println(tekst); //do wszystkich innych wysyłamy  tekst

        }
    }

    private void graj() {

        Rzut rzut = new Rzut();
        rzut.podajUlozenie();
        int punkty =  rzut.podliczPunkty();
        if (punkty == -1) zakonczTure();
    }

    private void pokazPunkty() {
        out.println("\n*****************");
        out.println("TABELA PUNKTÓW:");

        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników

            out.println(uczestnik.nick + ": " + uczestnik.wszystkiePunkty + "pkt" );
        }
        out.println("*****************");
    }

    private synchronized void dolaczDoGry(){

            uczestnicy.add(this); // dodanie bieżącego uczestnika do listy uczestników

        System.out.println("Do gry dołączył: " + nick);                         // to wyświetlamy na serwerze
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);      // to też
        wyslijDoInnych(this, "Do gry dołączył: " + nick);                         // wysyłamy do wszystkich
    }

    private synchronized void opuscGre(){
        wyslijDoWszystkich("Grę opuścił: " + nick);                              // jeżeli wpisał /q to opuszcza grę
        if (this == aktualnyUczestnik) {
            zakonczTure();
        }
            uczestnicy.removeElement(this);
            maxIloscGraczy--;
        System.out.println("Grę opuścił: " + nick);
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);

    }

    private synchronized void zakonczTure() {

            wyslijDoWszystkich("*** koniec tury ***");
            if ((uczestnicy.indexOf(this) + 1 == uczestnicy.size())) {
                ustawAktualnego(0);
            } else {
                ustawAktualnego(uczestnicy.indexOf(this) + 1);
            }
            wszystkiePunkty+=punktyWTurze;



    }

    private int losujKtoZacznie()
    {
        int ktoZacznie = (int)(Math.random() * maxIloscGraczy);
        return ktoZacznie;
    }

    private synchronized  void ustawAktualnego(int numerAktualnego) {
        aktualnyUczestnik = uczestnicy.get(numerAktualnego) ;
        wyslijDoJednego(uczestnicy.get(numerAktualnego), "\n\nTWOJA KOLEJ!\n\n");
        wyslijDoJednego(uczestnicy.get(numerAktualnego), "[enter] - rzucaj");
        wyslijDoJednego(uczestnicy.get(numerAktualnego),"/r - rezygnuj");
        wyslijDoInnych(aktualnyUczestnik, "\n\nTeraz rzuca: " + aktualnyUczestnik.nick);
        wyslijDoWszystkich("/p - punkty");
        wyslijDoWszystkich("/q - wyjscie");
        aktualnyUczestnik = uczestnicy.get(numerAktualnego) ;
    }



    public void run() {
        String linia; // deklaracja napisu wpisanego przez użytkownika

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego
            out.println("Połączony z serwerem. Komenda /q kończy połączenie.");   // to wysyłamy do klienta
            out.println("Podaj swój nick: ");                                       // to też
            nick = in.readLine();                                                   // odbieramy od klienta nick

            if (uczestnicy.size() < maxIloscGraczy) {
                dolaczDoGry();
                if (uczestnicy.size() == 1)
                {
                  pierwszy = true;
                }

                if (uczestnicy.size() != maxIloscGraczy) {
                    out.println("Czekaj na dołączenie wszystkich graczy!");
                    while (uczestnicy.size() != maxIloscGraczy) {
                    }
                }

                if (pierwszy)
                {
                    ktoZacznie = losujKtoZacznie();
                    ustawAktualnego(ktoZacznie);
                }

                    while ((linia = in.readLine()) != null) {
                        if (linia.equalsIgnoreCase("/q")) {
                            break;
                        }
                         else if (linia.equalsIgnoreCase("/p")) pokazPunkty();
                          else if (aktualnyUczestnik == this) {
                            if ((!linia.equalsIgnoreCase("/r"))) graj();
                              else zakonczTure();

                          }
                }
                    opuscGre();
            }
            else {
                out.println("Serwer jest pełny! Komenda /q kończy połączenie.");   // to wysyłamy do klienta
                while ((linia = in.readLine())!= null) {
                    if (linia.equalsIgnoreCase("/q")) break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                in.close(); // zamknięcie wejścia
                out.close(); // zamknięcie wyjścia
                socket.close(); // zamknięcie socketu
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
