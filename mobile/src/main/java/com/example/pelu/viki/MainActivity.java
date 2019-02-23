package com.example.pelu.viki;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.net.Uri;
import android.os.AsyncTask;
import java.util.ArrayList;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.content.ContentResolver;
import android.database.Cursor;


public class MainActivity extends AppCompatActivity implements edu.cmu.pocketsphinx.RecognitionListener, TextToSpeech.OnInitListener, MediaPlayerControl {


    private SpeechRecognizer recognizer;
    private static final String KEYPHRASE = "hola viki";
    private static final String MENU_SEARCH = "accede al menú";
    private static final String KWS_SEARCH = "hola";
    private static final String NOMBRE_VIKI = "viki";
    private static final String LISTASPOTY = "loquillo";

    private TextToSpeech textToSpeech;
    private MediaPlayer mediaPlayer;
    ArrayList<String> listItems = new ArrayList<String>();

    /*
    variable de instancia mediacontroller para el control de las canciones
     */
    private MusicController controller;
    private ArrayList<Song> songList;
    private ArrayList<Song> Dance;

    private boolean paused=false, playbackPaused=false;
    private boolean maximizado;
    private boolean directorio;

    private String carpeta;

    public boolean conectado;




    //debemos almacenar para mostrar en la lista, el nombre de la play list que esté en la config de usuario
    //TODO: los datos mejor en base de datos ¿?? //
    String tituloPlayList[] = {"FIREHOUSE", "BBKING"};
    String Playlist[] = {"playlist:1X7lWdHqmPajeBECHtSVD3","playlist:0nSQ0Uaw66rJ3vZy9MUomB"};
    String Canciones[] = {};
    String Albumes[] = {"LOQUILLO","BBKING","LA LEYENDA DE LA MANCHA","GARY MOORE","STAYOZ","TOM JONES","WARCRY","DEBLER"};
    String Artistas[] = {};

    ArrayAdapter<String> adapter;
    ListView listaDispo;
    Button playlist, canciones,album, artista;
    TableLayout spotifyTabla;
    MenuItem MusicaPlay, MusicaNext, MusicaStop,ocupado, libre;
    View PanelMusica;


    private boolean dance = false;
    private boolean spoty_playLists = false;
    private boolean Spotypanel = false;
    private boolean listaRepord =  false;
    private boolean albumes =  false;
    private boolean artistas =  false;
    private boolean estadoVoz = false;
    public boolean whatsapp = true;


    MailJob mail;

    /*variables dictado por voz */
    TextView grabar;
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;
    private int colorFondoLista = Color.parseColor("#973b89c7");
    String telefonos;
    String textos;



    //TODO: NOTA--> BUSCAR PALABRA CLAVE EN LA REPRODUCCION DE LA MUSICA // -->

    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);
        setContentView(R.layout.activity_main);

        if(whatsapp == true){
            System.out.println("dentro !!!!!!");
            openWhatsApp();
        }

                /*
         lista canciones
                */
        songList = new ArrayList<Song>();
        Dance = new ArrayList<Song>();



        MusicaNext = (MenuItem) findViewById(R.id.action_shuffle);
        MusicaStop = (MenuItem) findViewById(R.id.action_end);
        ocupado = (MenuItem) findViewById(R.id.estadoVikiOff);
        libre = (MenuItem) findViewById(R.id.estadoVikiOn);

        listaDispo = (ListView) findViewById(R.id.lista);
        playlist = (Button) findViewById(R.id.button8);
        canciones = (Button) findViewById(R.id.button7);
        album = (Button) findViewById(R.id.button10);
        artista = (Button) findViewById(R.id.button9);
        spotifyTabla = (TableLayout) findViewById(R.id.TablaSpoty);
        PanelMusica = (View) findViewById(R.id.view);


        //listaDispo.setBackgroundColor(255);
        listaDispo.setVisibility(View.VISIBLE);

        spotifyTabla.setVisibility(View.INVISIBLE);

        textToSpeech = new TextToSpeech(this, this);

        runRecognizerSetup();

        listaDispo.setVisibility(View.INVISIBLE);



                                    /*
        Prueba de acciones del boton-- whatsapp y encendido remoto
                                    */
        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                System.out.println("boton presionado");
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            wakeup("85.56.134.181", "309C238975FF");

                        } catch (Exception e) {

                            showAlert();
                        }

                    }
                }).start();

                                                                /*
        todo: llamamos a metodos de prueba, como inicir el repriductor, cambiar de activity para dictar y enviar por whatsupp


                /*
                Lista de telefonos
                */
                whatsapp = true;
                Intent telefonos = new Intent(getApplicationContext(), ListaTelefonos.class);
                startActivity(telefonos);
                recognizer.stop();
                finish();

                /*
                google maps

                Intent telefonos = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(telefonos);
                */
            }

        });

        final Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                textToSpeech.speak("abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                spotifyTabla.setVisibility(View.VISIBLE);
                listaDispo.setVisibility(View.INVISIBLE);
                Spotypanel = true;

            }

        });


        final Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                creaMusica();
            }

        });


        final Button button8 = findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

        listaRepord = true;  // para saber si la url pertenece a playlist..> para el método "abrespoty" //
        spoty_playLists = true;
        dance = false;
        creaMusica();

        recognizer.stop();
        recognizer.startListening(MENU_SEARCH);


        spoty_playLists = true;
        creaMusica();


            }

        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    controller.setVisibility(View.INVISIBLE);
                    recognizer.stop();
                    recognizer.startListening(MENU_SEARCH);


                }

             listaDispo.setVisibility(View.INVISIBLE);


               invalidateOptionsMenu();


                break;
            case R.id.action_end:
               // stopService(playIntent);
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                }
                mediaPlayer = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


                                        /*
Llamada al archivo xml que contien el menu.superior.. si no dará error
                                        */
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        menu.clear();


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);


         System.out.println("ESTAMOS DENTRO DEL MENÚ !!!");

        MenuItem off = menu.findItem(R.id.estadoVikiOff);
        MenuItem on = menu.findItem(R.id.estadoVikiOn);
        off.setVisible(false);
        on.setVisible(false);

        /*
        Retardo para las imágenes dle menú principal
         */

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                MenuItem off = menu.findItem(R.id.estadoVikiOff);
                MenuItem on = menu.findItem(R.id.estadoVikiOn);


                if(estadoVoz == true){
                    System.out.println("ESTAMOS DENTRO DEL MENÚ !!!");

                    on.setVisible(true);
                    off.setVisible(false);
                }

                else if(estadoVoz == false){
                    on.setVisible(false);
                    off.setVisible(true);
                }

            }
        }, 500);

        return true;
    }

                            /*
    Abre spoty con la url de la lista que le pasemos y del tipo que sea
                            */

    public void abrespoty(String url){

                                /*
        TODO: La lista de reproduccion se obtiene, sacando el enlace de spotyfy web. Así podremos agregar las listas que queramos.
                                 */

        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);

        if(listaRepord = true){
            intent.setData(Uri.parse("spotify:user:mentzal:" + url));
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try{

            MainActivity.this.startActivity(intent);
        }catch (Exception e){

            showAlert();
        }

    }

            /*
    Diálogo de alerta
            */

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ERROR")
                .setMessage("Error en la acción")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }



    public void creaMusica() {



       // todo: crear uno por cada carpeta o directorio que queramos reproducir pasandolo como
       // todo: parámetro. O sacar todas las carpetas.
        /*
        Oredna las cancions alfabéticamente
         */
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

                                    /*
        nueva instancia adapter de las cancioonesy las asociamos a la lista

                                    */

        SongAdapter songAdt = new SongAdapter(this, songList);
        listaDispo.setAdapter(songAdt);


        final String Cancion;
        //listaDispo.setBackgroundColor(colorFondoLista);
        final String PATH_TO_FILE = "/sdcard/Music/";



        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems) {

            /*
   ESTILO TEXTVIEW DE LA LISTA
           */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                View view = super.getView(position, convertView, parent);

                TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);

                ListItemShow.setTextColor(Color.parseColor("#FFFFFF99"));


                return view;
            }

        };


            try {
                File f = new File(Environment.getExternalStorageDirectory() + "/Music");
                File[] files = f.listFiles();
                adapter.clear();

                for (int i = 0; i < files.length; i++)

                {
                    //Sacamos del array files un fichero
                    File file = files[i];

                    //Si es directorio...
                    if (file.isDirectory())

                        adapter.add(file.getName() + "/");
                        directorio = true;

                }

            } catch (Exception e) {

                showAlert();
                System.out.println("Primera lista");
            }


            listaDispo.setAdapter(adapter);
            listaDispo.setVisibility(View.VISIBLE); //hace invisible o visible la lista //



        // Accion para realizar al pulsar sobre un item de la lista
        listaDispo.setOnItemClickListener(new AdapterView.OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


              final String Cancion;


              //listaDispo.setBackgroundColor(colorFondoLista);


            if(directorio == true){

                 directorio = false;
                 carpeta = adapter.getItem(i).toString();

                try {
                    File f = new File(Environment.getExternalStorageDirectory() + "/Music/" + adapter.getItem(i));
                    File[] files = f.listFiles();
                    adapter.clear();

                    for (int j = 0; j < files.length; j++)

                    {
                        //Sacamos del array files un fichero
                        File file = files[j];

                        //Si es directorio...
                        if (file.isDirectory())

                            adapter.add(file.getName() + "/");


                            //Si es fichero...
                        else
                            adapter.add(file.getName());

                    }

                } catch (Exception e) {

                    showAlert();
                    System.out.println("Segunda lista");
                }

            }
                            /*
            Reproducimos la cancion seleccionada
                             */

            else if(directorio == false){


                if(mediaPlayer != null){

                    mediaPlayer.stop();
                }

                System.out.println("REPRODUCIMOS LA CANCION");
                try{
                    Cancion =PATH_TO_FILE +carpeta + adapter.getItem(i);
                    System.out.println(Cancion);

                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(Cancion);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                    //

                    recognizer.stop();
                    estadoVoz = false;
                    invalidateOptionsMenu(); //resetea el menu para cambiar estado del icono del micro.

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {


                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.stop();
                            Random rand = new Random();
                            int n = rand.nextInt(adapter.getCount());
                            String Cancion = PATH_TO_FILE +carpeta + adapter.getItem(n);

                            try {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(Cancion);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            mediaPlayer.start();
                            recognizer.stop();
                            estadoVoz = false;
                        }
                    });

                }catch (Exception e){

                    showAlert();
                }

                /*
                Si el controlador está lleno lo borramos y abrimos otro
                para que no se acumulen
                 */

                if(controller != null){

                    controller.setVisibility(View.INVISIBLE);
                }

                setController();

            }

              listaDispo.setAdapter(adapter);
              listaDispo.setVisibility(View.VISIBLE); //hace invisible o visible la lista //

          }
      });

    }

    //play next
    private void playNext(){

        recognizer.stop();
        estadoVoz = false;
        mediaPlayer.stop();
        invalidateOptionsMenu();

       final String PATH_TO_FILE = "/sdcard/Music/";

        Random rand = new Random();
        int n = rand.nextInt(adapter.getCount());
        String Cancion = PATH_TO_FILE +carpeta + adapter.getItem(n);


        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(Cancion);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    //play previous
    private void playPrev(){


        //  recognizer.stop();
        //  musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }


@Override
protected void onPause() {


   super.onPause();
    invalidateOptionsMenu();
    paused=true;

}

    @Override
    protected void onResume(){

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

        super.onResume();
        invalidateOptionsMenu();
        if(paused){
            //setController();
            paused=false;
        }

    }


//todo: abre el nabegador chrome //
void open(Activity activity, String url) {
   Uri uri = Uri.parse("googlechrome://navigate?url=" + url);
   Intent i = new Intent(Intent.ACTION_VIEW, uri);
   if (i.resolveActivity(activity.getPackageManager()) == null) {
       i.setData(Uri.parse(url));
   }
   activity.startActivity(i);
}

private void runRecognizerSetup() {
   // Recognizer initialization is a time-consuming and it involves IO,
   // so we execute it in async task
   new AsyncTask<Void, Void, Exception>() {
       @Override
       protected Exception doInBackground(Void... params) {
           try {
               estadoVoz = true;
               Assets assets = new Assets(MainActivity.this);
               File assetDir = assets.syncAssets();
               setupRecognizer(assetDir);
           } catch (IOException e) {
               return e;
           }
           return null;
       }

       @Override
       protected void onPostExecute(Exception result) {
           if (result != null) {
               System.out.println(result.getMessage() + "asincrono");
           } else {
               switchSearch(KWS_SEARCH);
           }
       }
   }.execute();
}


private void setupRecognizer(File assetsDir) throws IOException {

   recognizer = SpeechRecognizerSetup.defaultSetup()
           .setAcousticModel(new File(assetsDir, "es-ptm"))
           .setDictionary(new File(assetsDir, "es.dict"))
           //.setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
           .setKeywordThreshold(Float.valueOf("1.8"))
           // Disable this line if you don't want recognizer to save raw
           // audio files to app's storage
           // .setRawLogDir(assetsDir)
           .getRecognizer();
   recognizer.addListener(this);
   // Create keyword-activation search.
   recognizer.addKeyphraseSearch(KWS_SEARCH,KEYPHRASE );

   //todo: utilizamos el archivo de gramática para nvegar por él si queremos //
   // Create your custom grammar-based search

   File menuGrammar = new File(assetsDir, "menu.gram");
   recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
  // recognizer.addGrammarSearch(LISTASPOTY, menuGrammar);

}

@Override
public void onBeginningOfSpeech() {

}


@Override
protected void onDestroy() {
   super.onDestroy();

   if (recognizer != null) {
       recognizer.cancel();
       recognizer.shutdown();
   }
}

@Override
public void onEndOfSpeech() {


   if (!recognizer.getSearchName().equals(KWS_SEARCH))
       switchSearch(KWS_SEARCH);

}

@Override
public void onPartialResult(Hypothesis hypothesis) {

   textToSpeech.setLanguage(Locale.UK);
   Locale locSpanish = new Locale("spa", "ES");
   textToSpeech.setLanguage(locSpanish);


   if (hypothesis == null)
       return;

   String text = hypothesis.getHypstr();


   if (text.equals(KEYPHRASE)) {
       recognizer.stop();
       estadoVoz = false;
       switchSearch(MENU_SEARCH);
   } else

   {
       recognizer.stop();
       recognizer.startListening(MENU_SEARCH);
       System.out.println(hypothesis.getHypstr() + " Hypotesis Onpartial");

   }

}
                                    /*
                            ENCENDIDO REMOTO
                                    */

    private static byte[] getMacBytes(String mac) throws IllegalArgumentException {
        Log.d("GetMacBytes", "method started");
// TODO Auto-generated method stub
        byte[] bytes = new byte[6];
        try {
            String hex;
            for (int i = 0; i < 6; i++) {
                hex = mac.substring(i * 2, i * 2 + 2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);
                Log.d("GetMacbytes", "calculated");
                Log.d("GetMacBytes (bytes)", new String(bytes));
            }
        } catch (NumberFormatException e) {
            Log.e("GetMacBytes", "error");
        }
        return bytes;
    }

    public static void wakeup(String broadcastIP, String mac) {
        Log.d("wakeup", "method started");
        if (mac == null) {
            Log.d("Mac error at wakeup", "mac = null");
            return;
        }

        try {
            byte[] macBytes = getMacBytes(mac);
            Log.d("wakeup (bytes)", new String(macBytes));
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            Log.d("wakeup", "calculating completed, sending...");
            InetAddress address = InetAddress.getByName(broadcastIP);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            Log.d("wakeup", "Magic Packet sent");


        } catch (Exception e) {
            Log.e("wakeup", "error");
        }

    }
             /*
    FIN ENCENDIDO REMOTO
             */


    @Override
    public void onResult(Hypothesis hypothesis) {

         boolean listalokillo = false;

        if (hypothesis != null) {


            System.out.println(hypothesis.getHypstr() + "hipotesis");



            if (hypothesis.getHypstr().equals("encender ordenador")) {
                textToSpeech.speak("encendiendo", TextToSpeech.QUEUE_FLUSH, null, null);

                new Thread(new Runnable() {
                    public void run() {
                        wakeup("192.168.1.112", "309C238975FF");
                    }
                }).start();

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);

            } else if (hypothesis.getHypstr().equals("abre whatsapp")) {

                textToSpeech.speak("Abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                Intent telefonos = new Intent(getApplicationContext(), ListaTelefonos.class);
                startActivity(telefonos);

                recognizer.stop();
                estadoVoz = false;
                finish();
               // recognizer.startListening(MENU_SEARCH);

            } else if (hypothesis.getHypstr().equals("abre spotify")) {

                textToSpeech.speak("abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                spotifyTabla.setVisibility(View.VISIBLE);
                listaDispo.setVisibility(View.INVISIBLE);
                Spotypanel = true;

            }

            else if(hypothesis.getHypstr().equals("abre playlist") && Spotypanel == true){

                listaRepord = true;  // para saber si la url pertenece a playlist..> para el método "abrespoty" //
                spoty_playLists = true;
                dance = false;
                creaMusica();

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);


                spoty_playLists = true;
                creaMusica();

            }

             else if (hypothesis.getHypstr().equals("abre música")) {

                                /*
                  reproduce una cancion selecionada
                                 */
                dance = false;
                creaMusica();

                // musica = true;
                textToSpeech.speak("Abriendo música", TextToSpeech.QUEUE_FLUSH, null, null);
                recognizer.stop();
                estadoVoz = false;

            }

             else if (hypothesis.getHypstr().equals("cierra música")) {
                dance = false;
                controller.hide();
                estadoVoz = false;
                listaDispo.setVisibility(View.INVISIBLE);
                recognizer.startListening(MENU_SEARCH);
            }

            //todo: hacer lista dinámica...par amostrar unas canciones u otras ... sacar del oncreate //
            else if (hypothesis.getHypstr().equals("música dance")) {
                dance = true;
                // mediaPlayer.stop();
                creaMusica();
                recognizer.stop();


                //  lista.setVisibility(View.INVISIBLE);
                recognizer.startListening(MENU_SEARCH);


            }

            else if(hypothesis.getHypstr().equals("hola viki")){
                textToSpeech.speak("hola", TextToSpeech.QUEUE_FLUSH, null, null);
                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            }
            else if(hypothesis.getHypstr().equals("hola viki")&& maximizado == true){

                Intent principal = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(principal);
                System.out.println("ESTAMOS DENTRO !!!!!!!!!");

            }



            else if(hypothesis.getHypstr().equals("manda correo")){
                textToSpeech.speak("Enviando correo", TextToSpeech.QUEUE_FLUSH, null, null);

                 /*
        ENVIO DE CORREO DE PRUEBA
        En la cuenta de correo de gmail, permitir acceso a aplicaciones no seguras.
                */
                  new MailJob("belaklord@gmail.com", "A968908054").execute(
                  new MailJob.Mail("belaklord@gmail.com", "belaklord@gmail.com", "subjeto", "contenido") );

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
            }

            else if(hypothesis.getHypstr().equals("enviar correo")){

            }

                                     /*
            Reinicia la aplicacion maximizandola si está minimizada
                                    */

            else if(hypothesis.getHypstr().equals("mostrar")){

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.pelu.viki");
                startActivity(launchIntent);

              super.finish();

            }
        }
    }

    @Override
    public void onError(Exception e) {

        System.out.println(e.getMessage());

    }

    @Override
    public void onTimeout() {


        switchSearch(KWS_SEARCH);
    }


    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH)) {
            estadoVoz = true;
            recognizer.startListening(searchName);
        } else {
            estadoVoz = true;
            // textToSpeech.speak("no se ha accedido al menú", TextToSpeech.QUEUE_FLUSH, null,null);

            recognizer.startListening(searchName, 10000);

        }

    }

    /*
    todo: metodos de control de lista de reproduccion
     */

    @Override
    public void onInit(int status) {

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void start() {

        mediaPlayer.start();
        recognizer.stop();
        estadoVoz = false;
        invalidateOptionsMenu();
    }

    @Override
    public void pause() {
       // playbackPaused=true;

        mediaPlayer.pause();
        invalidateOptionsMenu();
        estadoVoz= true;
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                recognizer.stop();
                recognizer.startListening(KWS_SEARCH);


            }
        }, 1000);

    }


    @Override
    public int getDuration() {

        return mediaPlayer.getDuration();

    }

    @Override
    public int getCurrentPosition() {
       return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);

    }

    @Override
    public boolean isPlaying() {



        return mediaPlayer.isPlaying();

    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

       /*
       Aplicacion del controlador de canciones
        */
    private void setController(){

        controller = new MusicController(this);

            //todo: acciones de los botones del controlador
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        /*
        Aplicamos y asociamos el control a la lista de canciones
         */
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.view));
        controller.setEnabled(true);
        controller.show();
    }


    /*
       Abre whatsapp y envía mensaje al numero indicado
                            */
    public void openWhatsApp(){
        try {

            telefonos = (String) getIntent().getExtras().getSerializable("numero");
            textos = (String) getIntent().getExtras().getSerializable("texto");

            String text = textos;// Replace with your message.

            String toNumber = telefonos; // Replace with mobile phone number without +Sign or leading zeros.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(telefonos.startsWith("+34")){
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));
            }
            else{
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=+34"+toNumber +"&text="+text));
            }


            startActivity(intent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        whatsapp = false;
    }

}

