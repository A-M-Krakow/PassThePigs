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
    private static final  int PORT = 23; // definicja portu, na którym nasłuchuje serwer

    public static void main(String args[]){
        int maxIloscGraczy = 0;    // definicja zmiennej przechowującej ilość graczy na początkowe 0


        while (maxIloscGraczy < 2 || maxIloscGraczy>5)                  // dopóki ilość graczy nie będzie z zakresu 2 do 5
        {
            System.out.print("Podaj ilość graczy (od 2 do 5):");  // serwer prosi o jej wprowadzenie
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextInt()) maxIloscGraczy = sc.nextInt();        //dopisywanie liczby do zmiennej maxIloscGraczy jeśli jest liczbą całkowitą
        }
        Gra gra = new Gra(maxIloscGraczy);
        try
        {
            server = new ServerSocket(PORT); // definicja socketu serwera na odpowiednim porcie
            System.out.println("Serwer gry uruchomiony na porcie: " + PORT);

            while (true){
                Socket socket = server.accept(); // włączenie akceptowania nowego połączenia
                InetAddress addr = socket.getInetAddress(); // definicja zmiennej przechowującej adres połączonego klienta
                System.out.println("Połączenie z adresu: "+ addr.getHostName() + " [" + addr.getHostAddress() + "]");
                new Uczestnik(socket, gra).start(); // uruchomienie obsługi gry dla uczestnika
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Gra {
    private Vector<Uczestnik> uczestnicy = new Vector<>(); // definicja zmiennej przechowującej wszystkich uczestnikow gry
    private Uczestnik aktualny; // deklaracja uczestnika, który gra w danym momencie
    private Uczestnik pierwszyWKolejce; //deklaracja uczestnika, który rozpoczyna kolejkę
    private int poczatkowaMaxIloscGraczy; //deklaracja maksymalnej ilości graczy na początku gry
    private int maxIloscGraczy; //deklaracja maksymalnej ilości graczy, która będzie aktualizowana jeżeli gracze opuszczą grę
    private boolean czyJestZwyciezca = false;

    public Gra(int maxIloscGraczy)
    {
        this.maxIloscGraczy = poczatkowaMaxIloscGraczy = maxIloscGraczy;
    }

    /* metoda dodająca uczestnika do danej gry */
    public synchronized void dodajUczestnika(Uczestnik uczestnik){
        uczestnicy.add(uczestnik);
        System.out.println("Do gry dołączył: " + "<" + uczestnik.podajNick() + ">");                         // to wyświetlamy na serwerze
        System.out.println("Połączonych graczy: " + this.podajIloscGraczy() + "/" + maxIloscGraczy);      // to też
        wyslijDoInnych(aktualny, "Do gry dołączył: " + "<" + uczestnik.podajNick() + ">");                         // wysyłamy do wszystkich
    }

    /* Metoda zwracająca ilość graczy w grze */
     public int podajIloscGraczy() {
        return uczestnicy.size();
    }

    /* Metoda zwracająca maksymalną ilość graczy w grze */

    public int podajMaxIloscGraczy() {
        return maxIloscGraczy;
    }


    /* Metoda zwracająca uczestnika, który aktualnie gra */
    public synchronized Uczestnik podajAktualnego() {
        return aktualny;
    }

    /* Metoda sprawdzająca, czy jest już zwycięzca gry */
    public void szukajZwyciezcy()
    {
        for (Uczestnik uczestnik : uczestnicy) {
            if (uczestnik.podajWszystkiePunkty() > 99) {
                this.czyJestZwyciezca = true;
            }
        }
    }

    /*Metoda zwracająca info czy jest zwycięzca */
    public boolean wskazCzyJestZwyciezca()
    {
        return czyJestZwyciezca;
    }

    /* Metoda przekazująca kolejkę do następnego uczestnika */
    public synchronized  void przekazKolejke(Uczestnik nowyAktualny) {
        this.aktualny = nowyAktualny ;
        /*aktualny gracz zmienia się na następnego gracza */

        if(aktualny==pierwszyWKolejce) szukajZwyciezcy();
        /* jeżeli aktualny gracz jest był pierwszy w kolejce, sprawdzamy, czy nie wyłonił się zwycięzca */

        if (!wskazCzyJestZwyciezca()) {
            /* jeżeli nie wyłonił się zwycięzca */
            wyslijDoJednego(this.aktualny, "\n\nTWOJA KOLEJ!");
            wyslijDoJednego(this.aktualny, "[enter] - rzucaj");
            wyslijDoJednego(this.aktualny, "/r - rezygnuj");
            wyslijDoInnych(this.aktualny, "\n\nTeraz rzuca: " + "<" + this.aktualny.podajNick() + ">");
            wyslijDoWszystkich("/p - punkty");
            wyslijDoWszystkich("/q - wyjscie");
        }
        else {
            /* jeżeli wyłonił się zwycięzca */
            wyslijDoWszystkich( "\n\nGRA ZAKOŃCZONA!");
            wyslijDoWszystkich(  "\n*****************");
            wyslijDoWszystkich( "TABELA PUNKTÓW:");

            for (Uczestnik gracz : uczestnicy) {
                /*wyświetlamy punkty każdego gracza z listy graczy */

                wyslijDoWszystkich(  "<"+ gracz.podajNick() + ">" +  ": " + gracz.podajWszystkiePunkty() + "pkt" );
            }
            wyslijDoWszystkich( "*****************");

        }
    }

    /* Metoda zakańczająca turę gracza */
    public synchronized void zakonczTure() {
        wyslijDoWszystkich("*** koniec tury gracza <" + aktualny.podajNick() + "> ***");
                    aktualny.przyznajPunkty();
                    /*dopisujemy graczowi punkty, które zdobył w turze */

                    if ((uczestnicy.indexOf(aktualny) + 1 == uczestnicy.size())) {
                        /*jeżeli aktualny gracz jest ostatni w kolejce */
                        przekazKolejke(uczestnicy.get(0));
                        /*przekazuje kolejkę pierwszemu w kolejce graczowi */
                    }
                    else {
                        przekazKolejke(uczestnicy.get(uczestnicy.indexOf(aktualny) + 1));
                        /*w przeciwnym wypadku przekazuje kolejkę graczowi, który jest w kolejce po nim */
                    }

            }


    /*Metoda usuwająca gracza z gry */
    public synchronized void opuscGre(Uczestnik uczestnik){


        if(pierwszyWKolejce == uczestnik && podajIloscGraczy()>1)
        /*jeżeli opuszczający był pierwszym w kolejce i jest więcej niż jeden gracz*/
        {

            if (uczestnicy.indexOf(uczestnik)+1 == podajIloscGraczy())
                /*jeżeli opuszczający był ostatni na liście */
                ustawPierwszego(uczestnicy.get(0));
                /*pierwszym w kolejce zostaje pierwszy na liście */
                else ustawPierwszego(uczestnicy.get(uczestnicy.indexOf(uczestnik) +1));
                 /* w przeciwnym wypadku, pierwszym w kolejce zostaje następny na liście */
        }

        if (uczestnik == aktualny) {
            zakonczTure();
        }
        uczestnicy.removeElement(uczestnik);

        maxIloscGraczy--;
        System.out.println("Grę opuścił: " + "<" + uczestnik.podajNick() + ">");
        System.out.println("Połączonych graczy: " + uczestnicy.size()+ "/" + maxIloscGraczy);
        if (maxIloscGraczy ==0) {
            maxIloscGraczy = poczatkowaMaxIloscGraczy;
            czyJestZwyciezca = false;
        }
    }

    /* Metoda wysyłająca wiadomość do wszystkich graczy w grze */
    public synchronized void wyslijDoWszystkich(String wiadomosc) { // metoda wysyłająca dane do wszytkich uczestników
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            uczestnik.wyslijWiadomosc(wiadomosc);
        }
    }

    /*Metoda wysyłająca wiadomość do jednego gracza w grze */
    public void wyslijDoJednego(Uczestnik uczestnik, String wiadomosc)    {
        uczestnik.wyslijWiadomosc(wiadomosc);
    }

    /* Metoda wysyłająca wiadomość do wszystkich graczy poza podanym */
    public synchronized  void wyslijDoInnych(Uczestnik zWyjatkiem, String wiadomosc) {
        for (Uczestnik uczestnik : uczestnicy) { // dla uczestników z listy uczestników
            if (uczestnik != zWyjatkiem)  // jeżeli uczestnik nie jest wykluczonym uczestnikiem
                uczestnik.wyslijWiadomosc(wiadomosc); //do wszystkich innych wysyłamy  wiadomosc

        }
    }

    /* Metoda rozpoczynająca turę gracza */
    public void graj() {
        Rzut rzut = new Rzut(this);
        rzut.podajUlozenie();
        int punkty =  rzut.podliczPunkty();
        if (punkty == -1) this.zakonczTure();
    }

    /*Metoda wyśtlająca podanemu graczowi tabelę z punktami */
    public void pokazPunkty(Uczestnik uczestnik) {
        wyslijDoJednego(uczestnik, "\n*****************");
        wyslijDoJednego(uczestnik, "TABELA PUNKTÓW:");

        for (Uczestnik gracz : uczestnicy) { // dla uczestników z listy uczestników

            wyslijDoJednego(uczestnik, "<"+ gracz.podajNick() + ">" +  ": " + gracz.podajWszystkiePunkty() + "pkt" );
        }
        wyslijDoJednego(uczestnik, "*****************");
    }

    /*Metoda wyświetlająca zwycięzcę gry */


    public void ustawPierwszego(Uczestnik uczestnik)
    {
        this.pierwszyWKolejce = uczestnik;
    }

    public void ustawMaxIloscGraczy(int maxIloscGraczy) {
        this.maxIloscGraczy = maxIloscGraczy;
    }

    public Vector<Uczestnik> podajListeUczestnikow()
    {
        return uczestnicy;
    }

}

class Rzut {
    Gra gra;
    int punktywRzucie = 0;
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

    public Rzut(Gra gra) {

        this.gra = gra;

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

    /* Metoda wyświetlająca ułożenie rzuconych figurek */
    public void podajUlozenie() {
        gra.wyslijDoWszystkich("Figurka 1 spadła na: " + ulozeniaFigurek[0]);
        gra.wyslijDoWszystkich("Figurka 2 spadła na: " + ulozeniaFigurek[1]);
    }

    /* Metoda zwracająca ilość zdobytych w rzucie punktów*/
    public int podliczPunkty() {

        if (!stykajaSie) {
            if ( (ulozeniaFigurek[0].equals(mozliweUlozenia[4]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[5]))) || (ulozeniaFigurek[0].equals(mozliweUlozenia[5]) && (ulozeniaFigurek[1].equals(mozliweUlozenia[4])))) {
                gra.wyslijDoWszystkich("Zerowanie punktów w turze!\n\n");
                punktywRzucie=-1;
                gra.podajAktualnego().ustawPunktyWTurze(0);
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
                gra.wyslijDoJednego(gra.podajAktualnego(), "\nZdobyłeś "  + punktywRzucie + " punktów!");
                gra.podajAktualnego().ustawPunktyWTurze(gra.podajAktualnego().podajPunktyWTurze()+punktywRzucie);
                gra.wyslijDoJednego(gra.podajAktualnego(), "\nPunkty w tej turze:" + gra.podajAktualnego().podajPunktyWTurze());
                gra.wyslijDoJednego(gra.podajAktualnego(), "[enter] - rzucaj");
                gra.wyslijDoJednego(gra.podajAktualnego(), "/p - wszystkie punkty");
                gra.wyslijDoJednego(gra.podajAktualnego(), "/r - rezygnuj");
                gra.wyslijDoJednego(gra.podajAktualnego(), "/q - wyjdź\n");
                gra.wyslijDoInnych(gra.podajAktualnego(), "<" + gra.podajAktualnego().podajNick() + "> +"  + punktywRzucie + " punktów !");

            }
        } else {
            gra.wyslijDoWszystkich("Figurki stykają się!");
            gra.wyslijDoWszystkich("Zerowanie wszystkich punktów!!\n\n");
            punktywRzucie=-1;
            gra.podajAktualnego().ustawPunktyWTurze(0);
            gra.podajAktualnego().ustawWszystkiePunkty(0);
        }
        return punktywRzucie;
    }

}

class Uczestnik extends Thread {
    private Gra gra;
    private String linia; // deklaracja napisu wpisanego przez użytkownika
    private int wszystkiePunkty = 0;
    private int punktyWTurze = 0;
    private Socket socket; // deklaracja socketu dla połączenia z uczestnikiem
    private BufferedReader in; // deklaracja strumienia danych otrzymanych od uczestnika
    private PrintWriter out; // deklaracja strumienia danych wysyłanych do uczestnika
    private String nick; // deklaracja nazwy uczestnika

    public Uczestnik(Socket socket, Gra gra) { // konstruktor obsługi nowego połączenia)
        this.gra = gra;
        this.socket = socket;
    }

    /* Metoda dodająca punkty z tury do wszystkich punktów */
    public void przyznajPunkty()
    {
        this.wszystkiePunkty+=this.punktyWTurze;
        this.punktyWTurze = 0;
    }

    /*Metoda wyświetlająca tekst temu użytkownikowi */
    public void wyslijWiadomosc(String wiadomosc)
    {
        this.out.println(wiadomosc);
    }

    /* Metoda zwracająca nick użytkownika */
    public String podajNick() {
        return nick;
    }

    /* Metoda zwracająca wszystkie punkty uczestnika */
    public int podajWszystkiePunkty()    {
        return wszystkiePunkty;
    }

    /*Metoda zwracająca punkty uczestnika w aktualnej turze */
    public int podajPunktyWTurze()    {
        return punktyWTurze;
    }

    /*Metoda aktualizująca wszystkie punkty uczestnika*/
    public void ustawWszystkiePunkty(int punkty)    {
        wszystkiePunkty = punkty;
    }

    /*Metoda aktualizująca punkty uczestnika w aktualnej turze */
    public void ustawPunktyWTurze(int punkty)    {
         punktyWTurze = punkty;
    }


    public void run() {

        int wylosowany = 0;
        boolean poczatek = true;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // definicja strumienia wejściowego
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // definicja strumienia wyjściowego
            this.wyslijWiadomosc("Połączony z serwerem. Komenda /q kończy połączenie.");
            this.wyslijWiadomosc("Podaj swój nick: ");

            nick = in.readLine(); // pobranie nicku od użytkownika

            if (gra.podajIloscGraczy() < gra.podajMaxIloscGraczy()) {
                /* jeżeli w grze są jeszcze wolne miejsca */
                gra.dodajUczestnika(this);

                if (gra.podajIloscGraczy() != 1) {
                    /* jeżeli nie gracz jest pierwszym podłączonym */
                    poczatek = false;
                }

                if (poczatek) {
                    /* jeżeli gra dopiero się zaczęła */
                    wylosowany = (int) (Math.random() * gra.podajMaxIloscGraczy() - 1);
                    //losujemy numer gracza, który zacznie grę
                }


                if (gra.podajIloscGraczy() != gra.podajMaxIloscGraczy()) {
                    /* jeżeli wszyscy gracze nie dołączyli */
                    out.println("Czekaj na dołączenie wszystkich graczy!");

                    while (gra.podajIloscGraczy() != gra.podajMaxIloscGraczy()) {
                    /* czekamy aż wszyscy dołączą */}
                }

                if (poczatek) {
                    /*jeżeli gra dopiero się zaczęła */
                    gra.ustawPierwszego(gra.podajListeUczestnikow().get(wylosowany))
                    /*ustawiamy, kto będzie pierwszy w kolejce (wcześniej wylosowany numer*/;
                    gra.przekazKolejke(gra.podajListeUczestnikow().get(wylosowany));
                    /*przekazujemy  kolejkę do pierwszego w kolejce */
                }

                while (((linia = in.readLine()) != null) && (!gra.wskazCzyJestZwyciezca())) {
                    /* dopóki istnieje możliwość odebrania tekstu od gracza (połączenie)
                        oraz dopóki nie wyłonił się zwycięzca gry */
                    if ((linia.equalsIgnoreCase("/q")) || (gra.wskazCzyJestZwyciezca())) {
                        /* jeżeli gracz wpisze /q albo zaistnieje zwycięzca */
                        break; // przechodzimy do opuszczania gry
                    } else if (linia.equalsIgnoreCase("/p")) gra.pokazPunkty(this);
                    /* jeżeli gracz wpisze /p, wyświetli mu się tabela z punktami */
                    else if (gra.podajAktualnego() == this) {
                        /*jeżeli gracz jest aktualnym graczem */
                        if ((!linia.equalsIgnoreCase("/r"))) gra.graj();
                        /* jeżeli gracz nie wpisze /r to rozpoczynamy granie */
                        else gra.zakonczTure();
                        /*jeżeli wpisał /r to kończymy jego turę */
                    }
                } gra.opuscGre(this);    /*nie ma tekstu wpisanego od uczestnika (brak połączenia)
                                                    lub wyłonił się zwycięzca - opuszczamy grę */
        }
            else {
                out.println("Serwer jest pełny! Komenda /q kończy połączenie ");   /* gra nie miała wolnych miejsc, gracz nie został wpuszczony */
                while ((linia = in.readLine())!= null) {
                    if (linia.equalsIgnoreCase("/q")) break;
                    /*jeżeli gracz wpisze /q zakończy grę */
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
