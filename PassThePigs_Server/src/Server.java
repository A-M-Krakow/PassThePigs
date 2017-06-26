import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

        while (maxIloscGraczy < 2 || maxIloscGraczy>5)                  // dopóki ilość graczy nie będzie z zakresu 1 do 5
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
    int maxIloscGraczy;
    int numerWKolejce;

    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika

    public Uczestnik(Socket socket, int maxIloscGraczy) { // konstruktor obsługi nowego połączenia)
        this.socket = socket;
        this.maxIloscGraczy = maxIloscGraczy;
    }


    private void wyslijDoWszystkich(String tekst) { // metoda wysyłająca dane do wszytkich obecnych
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            synchronized(uczestnicy) { // synchronizowane działanie (może być robione tylko przez jeden wątek na raz)
           //     if (uczestnik != this)  // jeżeli gra nie jest bieżącą grą
                    uczestnik.out.println("<" + nick + ">" + tekst); //do wszystkich innych wysyłamy napisany tekst
            }
        }
    }

    private void users() { // metoda wyświetlająca informacje na temat użytkowników
        out.print("Witaj " + nick + ", aktualnie grają: ");
        for (Uczestnik uczestnik : uczestnicy) { // dla wszystkich uczestników z listy uczestników
            synchronized(uczestnicy) { // synchronizowane działanie
                if (uczestnik != this) // jeżeli uczestnik nie jest bieżącym uczestnikiem
                    out.print(uczestnik.nick + " "); // wyświetlamy nick tego uczestnika.
            }
        }
        out.println();
    }

    public void run() {
        String linia; // deklaracja napisu wpisanego przez użytkownika

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego

            if (uczestnicy.size() < maxIloscGraczy) {
                synchronized (uczestnicy) { //synchronizowane działanie
                    uczestnicy.add(this); // dodanie bieżącego uczestnika do listy uczestników
                    if (uczestnicy.size() == 1) aktualnyUczestnik = this;
                }

                out.println("Połączony z serwerem. Komenda /end kończy połączenie.");   // to wysyłamy do klienta
                out.println("Podaj swój nick: ");                                       // to też
                nick = in.readLine();                                                   // odbieramy od klienta nick
                System.out.println("Do gry dołączył: " + nick);                         // to wyświetlamy na serwerze
                System.out.println("Połączonych klientów: " + uczestnicy.size());      // to też
                wyslijDoWszystkich("Pojawił się w grze");                         // wysyłamy do wszystkich
                users();                                                                // wyświetlamy klientowi info o użytkownikach

                while (!(linia = in.readLine()).equalsIgnoreCase("/q")) {
                    if (aktualnyUczestnik == this) {
                        if (!(linia.equalsIgnoreCase("/r"))) {
                            wyslijDoWszystkich(linia);
                        } else {
                            if (!(uczestnicy.indexOf(this) + 1 == uczestnicy.size())) {
                                aktualnyUczestnik = uczestnicy.get(uczestnicy.indexOf(this) + 1);
                            } else aktualnyUczestnik = uczestnicy.get(0);
                        }
                    }
                }

                wyslijDoWszystkich("Opuścił grę");                              // jeżeli wpisał /q to opuszcza grę
                System.out.println("Grę opuścił: " + nick);
            }
            else {
                out.println("Serwer jest pełny! Komenda /q kończy połączenie.");   // to wysyłamy do klienta
                while (!(linia = in.readLine()).equalsIgnoreCase("/q")) { // czekamy aż użytkownik wpisze /q
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
            }finally {
                synchronized(uczestnicy) {
                    uczestnicy.removeElement(this); // usunięcie bieżącego uczestnika z listy uczestników
                }
            }
        }
    }
}
