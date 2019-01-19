package com.example.pelu.viki;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import android.content.ContentResolver;
import android.database.Cursor;


public class MainActivity extends AppCompatActivity implements edu.cmu.pocketsphinx.RecognitionListener, TextToSpeech.OnInitListener, MediaPlayerControl {


    private SpeechRecognizer recognizer;
    private static final String KEYPHRASE = "hola";
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


    //service
    private MusicService musicSrv;
    private Intent playIntent;
    //binding
    private boolean musicBound=false;

    //activity and playback pause flags
    private boolean paused=false, playbackPaused=false;
    private boolean maximizado;



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
    MenuItem MusicaPlay, MusicaNext, MusicaStop;
    View PanelMusica;


    private boolean dance = false;
    private boolean spoty_playLists = false;
    private boolean Spotypanel = false;
    private boolean listaRepord =  false;
    private boolean albumes =  false;
    private boolean artistas =  false;


    MailJob mail;

    /*variables dictado por voz */
    TextView grabar;
    private static final int RECOGNIZE_SPEECH_ACTIVITY = 1;

    private int colorFondoLista = Color.parseColor("#973b89c7");


    //TODO: NOTA--> BUSCAR PALABRA CLAVE EN LA REPRODUCCION DE LA MUSICA // -->

    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);
        setContentView(R.layout.activity_main);


                /*
         lista canciones
                */
        songList = new ArrayList<Song>();
        Dance = new ArrayList<Song>();

        MusicaNext = (MenuItem) findViewById(R.id.action_shuffle);
        MusicaStop = (MenuItem) findViewById(R.id.action_end);
        listaDispo = (ListView) findViewById(R.id.lista);
        playlist = (Button) findViewById(R.id.button8);
        canciones = (Button) findViewById(R.id.button7);
        album = (Button) findViewById(R.id.button10);
        artista = (Button) findViewById(R.id.button9);
        spotifyTabla = (TableLayout) findViewById(R.id.TablaSpoty);
        PanelMusica = (View) findViewById(R.id.view);


        listaDispo.setBackgroundColor(255);
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
                                                                */
                Intent telefonos = new Intent(getApplicationContext(), ListaTelefonos.class);
                startActivity(telefonos);
                recognizer.stop();
                finish();

               // creaMusica();
               // recognizer.stop();

            }

        });

    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    //start and bind the service when the activity starts
    @Override
    protected void onStart() {

        super.onStart();

        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }


    //user song select
    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        recognizer.stop();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Obtencion de los datos de las canciones
     */

    public void getSongList(Uri direccion, ArrayList<Song> arraycanciones) {

        arraycanciones.clear();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = direccion;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                arraycanciones.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }

    }


                                        /*
Llamada al archivo xml que contien el menu.superior.. si no dará error
                                        */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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

                            /*
        Aplicams al vista del control de conciones
                             */
        setController();


        if (dance == true && spoty_playLists == false) {

            getSongList(android.provider.MediaStore.Audio.Media.getContentUriForPath("/storage/emulated/0/Music"),songList);

        }

        else if (dance == false && spoty_playLists == false) {


            getSongList(android.provider.MediaStore.Audio.Media.getContentUriForPath("/storage/emulated/0/Music"),songList);
        }



        else if(spoty_playLists == true){

            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {


                    View view = super.getView(position, convertView, parent);

                    TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);

                    ListItemShow.setTextColor(Color.parseColor("#FFFFFF"));


                    return view;
                }
            };
                 try{


                    adapter.clear();
                    for(int i = 0; i<Playlist.length; i++){

                        adapter.add(tituloPlayList[i].toString());
                        adapter.add(Playlist[i].toString());


                    }
                    spotifyTabla.setVisibility(View.INVISIBLE);
                }catch (Exception e){

                    // showAlert();
                }

            listaDispo.setAdapter(adapter);

            // Accion para realizar al pulsar sobre un item de la lista
            listaDispo.setOnItemClickListener(  new AdapterView.OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    System.out.println(adapter.getItem(i));

                    if (spoty_playLists == true) {

                        abrespoty(adapter.getItem(i));
                    }
                }
            });

            }

            listaDispo.setVisibility(View.VISIBLE);

            recognizer.stop();
            recognizer.startListening(MENU_SEARCH);

    }

@Override
protected void onPause() {
   super.onPause();
    paused=true;

}


    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }



public void apagarViki() {
   textToSpeech.speak("apagando", TextToSpeech.QUEUE_FLUSH, null, null);
   recognizer.stop();

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
   recognizer.addKeyphraseSearch(KWS_SEARCH, NOMBRE_VIKI);

   //todo: utilizamos el archivo de gramática para nvegar por él si queremos //
   // Create your custom grammar-based search

   File menuGrammar = new File(assetsDir, "menu.gram");
   recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
   recognizer.addGrammarSearch(LISTASPOTY, menuGrammar);


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
                /*
            } else if (hypothesis.getHypstr().equals("abre navegador")) {
                // abre el navegador chorme //
                open(MainActivity.this, "http://www.google.com");
                textToSpeech.speak("abriendo navegador", TextToSpeech.QUEUE_FLUSH, null, null);

                recognizer.stop();
                recognizer.startListening(MENU_SEARCH);
                */
            } else if (hypothesis.getHypstr().equals("abre whatsapp")) {

                textToSpeech.speak("Abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                Intent telefonos = new Intent(getApplicationContext(), ListaTelefonos.class);
                startActivity(telefonos);

                recognizer.stop();
                finish();
               // recognizer.startListening(MENU_SEARCH);

            } else if (hypothesis.getHypstr().equals("abre spotify")) {

                textToSpeech.speak("abriendo", TextToSpeech.QUEUE_FLUSH, null, null);
                spotifyTabla.setVisibility(View.VISIBLE);
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

            }

             else if (hypothesis.getHypstr().equals("cierra música")) {
                dance = false;
                controller.hide();
                recognizer.stop();
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
            /*
            else if (hypothesis.getHypstr().equals("a dormir")) {

                apagarViki();
            }
            */

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
            recognizer.startListening(searchName);
        } else {
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

        //controller.hide();
                        /*
        Aplicacion en segundo plano (minimizada)
                        */
        maximizado = true; //intentamos traerla al frente pero no funciona //
        System.out.println("Estamos fuerisima!!!!!!!!!!!!!!!!!!");
        super.onStop();



        /*
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        */
    }

    @Override
    public void start() {
        musicSrv.go();
        recognizer.stop();

    }

    @Override
    public void pause() {

        playbackPaused=true;
        musicSrv.pausePlayer();
        recognizer.startListening(MENU_SEARCH);
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)

        return musicSrv.isPng();
        return false;
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
        return 0;
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
    }


    //play next
    private void playNext(){
        musicSrv.playNext();
        recognizer.stop();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        recognizer.stop();
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

}
