package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import static com.example.test.R.id.shareVK;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences sPref;
    RadioGroup  rG;
    ScrollView sv;
    LinearLayout ll;
    RadioButton vkB, ytB, yaB, alB;
    ImageButton vkI, ytI, yaI;
    Integer i = 4;

    String newURL = null;
    String[] Track;

    public void load() {
        if(sPref.contains("i"))
            i = sPref.getInt("i", 4);
        else i = 4;
    }

    public void save() {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("i", i);
        ed.commit();
    }

    public void setButtonState() {
        if(i == 1)
        {
            vkB.setChecked(true);
        }else if(i == 2)
        {
            ytB.setChecked(true);
        }else if(i == 3)
        {
            yaB.setChecked(true);
        }else{
            alB.setChecked(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        save();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

        load();

        final Intent intent = getIntent();
        String url = intent.getDataString();

        //че делать
        /*setContentView(R.layout.activity_main);
        TextView share = findViewById(shareVK);
        share.setVisibility(View.VISIBLE);
        share.setText("Shared");*/

        if(url == null) {
            setContentView(R.layout.activity_main);

            sv = (ScrollView)findViewById(R.id.sv);
            ll = (LinearLayout)sv.findViewById(R.id.ll);
            rG = (RadioGroup)ll.findViewById(R.id.rG);
            vkB = (RadioButton)rG.findViewById(R.id.vk);
            ytB = (RadioButton)rG.findViewById(R.id.yt);
            yaB = (RadioButton)rG.findViewById(R.id.ya);
            alB = (RadioButton)rG.findViewById(R.id.al);

            rG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.vk: {
                            i = 1;
                            setButtonState();
                            break;
                        }
                        case R.id.yt: {
                            i = 2;
                            setButtonState();
                            break;
                        }
                        case R.id.ya: {
                            i = 3;
                            setButtonState();
                            break;
                        }
                        case R.id.al: {
                            i = 4;
                            setButtonState();
                            break;
                        }
                        default:
                            break;
                    }
                }
            });
            setButtonState();
        }
        else {
            if(!url.contains("vk.com/audio")){
                Document html = null;
                DownloadTask downloadTask = new DownloadTask();
                try {
                    html = downloadTask.execute(url).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                makeArtist(html,url);
                useUrl();
            }
            else {
                Track = new String[2];
                Document html = null;
                DownloadTask downloadTask = new DownloadTask();
                try {
                    html = downloadTask.execute(url).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Track = ArtistFromVK(html);

                useUrl();
            }
        }
    }

    void makeArtist(Document html,String url){
        if (url.contains("yandex.ru/album")) Track = ArtistFromYandex(html);
        else if (url.contains("deezer")) Track = ArtistFromDeezer(html);
        else if (url.contains("apple")) Track = ArtistFromApple(html);
        else if (url.contains("yandex.ru/search")) Track = ArtistFromYSearch(html);
    }

    void useUrl (){
        if (i == 3) {
            newURL = MakeYandexUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 2) {
            newURL = MakeYoutubeUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 1) {
            newURL = MakeVkUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 4) {
            setContentView(R.layout.activity_ch);
            sv = (ScrollView)findViewById(R.id.sv);
            ll = (LinearLayout)sv.findViewById(R.id.ll);
            vkI = (ImageButton)ll.findViewById(R.id.vk);
            ytI = (ImageButton)ll.findViewById(R.id.yt);
            yaI = (ImageButton)ll.findViewById(R.id.ya);
        }
    }

    public void vkClick(View view) {
        newURL = MakeVkUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }

    public void ytClick(View view) {
        newURL = MakeYoutubeUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }

    public void yaClick(View view) {
        newURL = MakeYandexUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }


    public String[] ArtistFromYandex (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i=0;
        while (data[i]!='—'){
            songName += data[i];
            i++;
        }
        i=i+2;
        while (data[i]!='.'){
            Artist+=data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0]=Artist;
        Output[1]=songName;
        return Output;
    }

    //не работает
    public String[] ArtistFromVK (Document html){
        TextView text = findViewById(shareVK);
        text.setText(html.text());

        String[] Output = new String[2];
        Output[0]=null;
        Output[1]=null;
        return Output;
    }

    public String[] ArtistFromDeezer (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i=0;
        while (data[i]!='—'){
            Artist += data[i];
            i++;
        }
        i=i+2;
        while (data[i]!='—'){
            songName+=data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0]=Artist;
        Output[1]=songName;
        return Output;
    }

    public String[] ArtistFromApple (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i=0;

        while (data[i]!='«'){
            i++;
        }
        i++;
        while (data[i]!='»'){
            Artist+=data[i];
            i++;
        }
        i+=3;
        while (data[i]!=')'){
            songName+=data[i];
            i++;
        }

        String[] Output = new String[2];
        Output[0]=Artist;
        Output[1]=songName;
        return Output;
    }

    public  String[] ArtistFromYSearch (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i=0;
        while (data[i]!='-'){
            Artist += data[i];
            i++;
        }
        i=i+2;
        while (data[i]!=':'){
            songName+=data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0]=Artist;
        Output[1]=songName;
        return Output;
    }

    //Не работает
    public String[] ArtistFromYoutube (Document html){
        String data = html.getElementsByAttribute("ytd-video-primary-info-render").toString();
        String[] Output = new String[2];
        Output[0]= data;
        Output[1]= null;
        return Output;
    }


    public String MakeVkUrl (String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut ="";
        String songNameOut ="";

        for(int i=0;i<songName.length;i++){
            if (songName[i]!=' ') songNameOut+=songName[i];
            else songNameOut+="%20";
        }

        for(int i=0;i<Artist.length;i++){
            if (Artist[i]!=' ') ArtistOut+=Artist[i];
            else ArtistOut+="%20";
        }

        String newURL = "https://vk.com/audio?q=" + ArtistOut + "%20-%20"+ songNameOut;
        return newURL;
    }

    public String MakeYoutubeUrl (String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut ="";
        String songNameOut ="";

        for(int i=0;i<songName.length;i++){
            if (songName[i]!=' ') songNameOut+=songName[i];
            else songNameOut+="+";
        }

        for(int i=0;i<Artist.length;i++){
            if (Artist[i]!=' ') ArtistOut+=Artist[i];
            else ArtistOut+="+";
        }

        String newURL = "https://www.youtube.com/results?search_query=" + ArtistOut + "+"+ songNameOut;
        return newURL;
    }

    public String MakeYandexUrl (String[] Input){
        {
            char[] songName = Input[1].toCharArray();
            char[] Artist = Input[0].toCharArray();
            String ArtistOut ="";
            String songNameOut ="";

            for(int i=0;i<songName.length;i++){
                if (songName[i]!=' ') songNameOut+=songName[i];
                else songNameOut+="%20";
            }

            for(int i=0;i<Artist.length;i++){
                if (Artist[i]!=' ') ArtistOut+=Artist[i];
                else ArtistOut+="%20";
            }

            String newURL = "https://music.yandex.ru/search?text=" + ArtistOut + "%20-%20"+ songNameOut;
            Document html = null;
            DownloadTask downloadTask = new DownloadTask();
            try {
                html = downloadTask.execute(newURL).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String str = html.getElementsByAttribute("href").toString();
            int pos = str.indexOf("/album/");
            String Output = "";
            while(str.toCharArray()[pos]!='\"'){
                Output+=str.toCharArray()[pos];
                pos++;
            }
            String out = "https://music.yandex.ru"+Output;

            return out;
        }
    }
}

class DownloadTask extends AsyncTask<String,Void, Document> {
    @Override
    protected Document doInBackground(String... params)
    {
        Document html = null;
        try {
            String url = params[0];
            if(url!=null) {
                try {
                    html = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return html;
    }
}
