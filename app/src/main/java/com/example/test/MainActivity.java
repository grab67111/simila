package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.test.R.id.shareVK;


public class MainActivity extends AppCompatActivity {

    // ебучие глобальные переменны Терехова
    private SharedPreferences sPref;
    // радиогруппа для реализации выбора
    RadioGroup rG;
    ScrollView sv;
    LinearLayout ll;
    // ебучие радиобаттоны
    RadioButton vkB, ytB, yaB, alB;
    ImageButton vkI, ytI, yaI;
    // переменная выбора, по умолчанию - 4, затем подгружается из памяти для каждого след открытия
    Integer i = 4;

    String newURL = null;
    String[] Track;

    // загрузка сохраненного выбора в локальный файл
    public void load() {
        if (sPref.contains("i"))
            i = sPref.getInt("i", 4);
        else i = 4;
    }

    // сохранение выбора для дальнейшего открытия через него по умолчанию
    public void save() {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("i", i);
        ed.commit();
    }

    // установка выбора в соответствии с выбором, сохраненным в файле
    public void setButtonState() {
        if (i == 1) {
            vkB.setChecked(true);
        } else if (i == 2) {
            ytB.setChecked(true);
        } else if (i == 3) {
            yaB.setChecked(true);
        } else {
            alB.setChecked(true);
        }
    }

    @Override
    // при остановке приложения сохраняем выбор
    protected void onStop() {
        super.onStop();
        save();
    }

    @Override
    // при возрврате к приложению подгружаем выбор
    // хз, надо ли, по идее он и так останется
    protected void onResume() {
        super.onResume();
        load();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPref = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

        // загружаем выбор
        load();

        // получаем интент, вызвавший нас
        final Intent intent = getIntent();
        String url = intent.getDataString();

        // открываем для передачи ссылки
        if (intent.getClipData()!=null) {
            // получаем URL из ссылки регуляркой
            String str = String.valueOf(intent.getClipData());
            Pattern p = Pattern.compile("http.*");
            Matcher m = p.matcher(str);
            String url1 = "";
            while(m.find()){
                url1 = m.group().substring(0,(m.group().length()-3));
            }

            // парсим
            Document html = null;
            DownloadTask downloadTask = new DownloadTask();
            try {
                // спарсить
                html = downloadTask.execute(url1).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // получить данные о треке и генерируем новый юрл
            makeArtist(html, url1);
            if (i == 3)  newURL = MakeYandexUrl(Track);
            if (i == 2)  newURL = MakeYoutubeUrl(Track);
            if (i == 1)  newURL = MakeVkUrl(Track);

            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_SEND);
            intent2.setType("text/plain");
            intent2.putExtra(Intent.EXTRA_TEXT, newURL);
            startActivity(Intent.createChooser(intent2, "Share"));
            this.finish();
        }
        // если мы открываем само приложение, а не при получении URL
        if (url == null) {
            setContentView(R.layout.activity_main);

            sv = (ScrollView) findViewById(R.id.sv);
            ll = (LinearLayout) sv.findViewById(R.id.ll);
            rG = (RadioGroup) ll.findViewById(R.id.rG);
            vkB = (RadioButton) rG.findViewById(R.id.vk);
            ytB = (RadioButton) rG.findViewById(R.id.yt);
            yaB = (RadioButton) rG.findViewById(R.id.ya);
            alB = (RadioButton) rG.findViewById(R.id.al);

            // создание радиогруппы и привязка к ней радиобатонов
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
            // устанавливаем выбор в соотвествии с загруженным
            setButtonState();
        }
        // если мы открыли по ссылке
        // вылетает при открытии ВК
        else {
            Document html = null;
            DownloadTask downloadTask = new DownloadTask();
            try {
                // спарсить
                html = downloadTask.execute(url).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // получить данные о треке
            makeArtist(html, url);
            // выполнить новую ссылку
            useUrl();
        }
    }
    // конец onCreate

    // Функция: получаем исполнителя + название с помощью соответсвующих методов
    void makeArtist(Document html, String url) {
        if (url.contains("yandex.ru/album")) Track = ArtistFromYandex(html);
        else if (url.contains("deezer")) Track = ArtistFromDeezer(html);
        else if (url.contains("apple")) Track = ArtistFromApple(html);
        else if (url.contains("yandex.ru/search")) Track = ArtistFromYSearch(html);
    }

    // Функция: открываем новый url способом по умолчанию
    void useUrl() {
        if (i == 3) {// Яндекс
            newURL = MakeYandexUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 2) { //Ютуб
            newURL = MakeYoutubeUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 1) { //ВК
            newURL = MakeVkUrl(Track);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
            startActivity(browserIntent);
            this.finish();
        }
        if (i == 4) { //Предложить выбор
            // Сделано через ебучий показ другого активити, где просто нет кнопки с выбором...
            // Нужно просто было установить Visible = gone
            // Терехов V(0_0)v
            setContentView(R.layout.activity_ch);
            sv = (ScrollView) findViewById(R.id.sv);
            ll = (LinearLayout) sv.findViewById(R.id.ll);
            vkI = (ImageButton) ll.findViewById(R.id.vk);
            ytI = (ImageButton) ll.findViewById(R.id.yt);
            yaI = (ImageButton) ll.findViewById(R.id.ya);
        }
    }

    // если при выборе мы
    //ткнули ВК
    public void vkClick(View view) {
        newURL = MakeVkUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }

    //ткнули ютуб
    public void ytClick(View view) {
        newURL = MakeYoutubeUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }

    //ткнули Яндекс
    public void yaClick(View view) {
        newURL = MakeYandexUrl(Track);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }


    // получить из прямой ссылки Яндекса
    public String[] ArtistFromYandex(Document html) {
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '—') {
            songName += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != '.') {
            Artist += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получить из Дизера
    public String[] ArtistFromDeezer(Document html) {
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '—') {
            Artist += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != '—') {
            songName += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получить из Эпла
    public String[] ArtistFromApple(Document html) {
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;

        while (data[i] != '«') {
            i++;
        }
        i++;
        while (data[i] != '»') {
            Artist += data[i];
            i++;
        }
        i += 3;
        while (data[i] != ')') {
            songName += data[i];
            i++;
        }

        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получение из поисковой ссылки Яндекса
    public String[] ArtistFromYSearch(Document html) {
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '-') {
            Artist += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != ':') {
            songName += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // Не работает потому что гребаный ютуб всегда открывает свои ссылки сам
    // Не получается отобрать у него его ссылку
    // Для гугла та же история, не отдает свои ссылки
    public String[] ArtistFromYoutube(Document html) {
        String data = html.getElementsByAttribute("ytd-video-primary-info-render").toString();
        String[] Output = new String[2];
        Output[0] = data;
        Output[1] = null;
        return Output;
    }

    //не работает, непонятно откуда получить данные
    public String[] ArtistFromVK(Document html) {
        TextView text = findViewById(shareVK);
        text.setText(html.text());

        String[] Output = new String[2];
        Output[0] = null;
        Output[1] = null;
        return Output;
    }


    // создание ссылки в ВК
    public String MakeVkUrl(String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut = "";
        String songNameOut = "";

        for (int i = 0; i < songName.length; i++) {
            if (songName[i] != ' ') songNameOut += songName[i];
            else songNameOut += "%20";
        }

        for (int i = 0; i < Artist.length; i++) {
            if (Artist[i] != ' ') ArtistOut += Artist[i];
            else ArtistOut += "%20";
        }

        String newURL = "https://vk.com/audio?q=" + ArtistOut + "%20-%20" + songNameOut;
        return newURL;
    }

    // создание ссылки в Ютуб
    public String MakeYoutubeUrl(String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut = "";
        String songNameOut = "";

        for (int i = 0; i < songName.length; i++) {
            if (songName[i] != ' ') songNameOut += songName[i];
            else songNameOut += "+";
        }

        for (int i = 0; i < Artist.length; i++) {
            if (Artist[i] != ' ') ArtistOut += Artist[i];
            else ArtistOut += "+";
        }

        String newURL = "https://www.youtube.com/results?search_query=" + ArtistOut + "+" + songNameOut;
        return newURL;
    }

    // создание ссылки в Яндекс через 2-ой парсинг.
    // Получаем название и артиста, формируем поисковую ссылку Яндекса
    // Затем парсим поисковую ссылку Яндекса и получаем оттуда их внутренний id трека
    public String MakeYandexUrl(String[] Input) {
        {
            char[] songName = Input[1].toCharArray();
            char[] Artist = Input[0].toCharArray();
            String ArtistOut = "";
            String songNameOut = "";

            for (int i = 0; i < songName.length; i++) {
                if (songName[i] != ' ') songNameOut += songName[i];
                else songNameOut += "%20";
            }

            for (int i = 0; i < Artist.length; i++) {
                if (Artist[i] != ' ') ArtistOut += Artist[i];
                else ArtistOut += "%20";
            }

            // формируем поисковую ссылку
            String newURL = "https://music.yandex.ru/search?text=" + ArtistOut + "%20-%20" + songNameOut;
            Document html = null;
            DownloadTask downloadTask = new DownloadTask();
            try {
                html = downloadTask.execute(newURL).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // парсим ID трека
            String str = html.getElementsByAttribute("href").toString();
            int pos = str.indexOf("/album/");
            String Output = "";
            while (str.toCharArray()[pos] != '\"') {
                Output += str.toCharArray()[pos];
                pos++;
            }
            String out = "https://music.yandex.ru" + Output;

            return out;
        }
    }
}

// парсим в потоке с помощью Jsoup'a
class DownloadTask extends AsyncTask<String, Void, Document> {
    @Override
    protected Document doInBackground(String... params) {
        Document html = null;
        try {
            String url = params[0];
            if (url != null) {
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
