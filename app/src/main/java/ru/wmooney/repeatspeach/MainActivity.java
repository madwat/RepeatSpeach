package ru.wmooney.repeatspeach;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnInitListener {
    private TextView EnteredText;
	//переменная для проверки поддержки распознавания голоса на пользовательском устройстве
	private static final int VR_REQUEST = 999;
    //переменная для проверки данных движка TTS на пользовательском устройстве
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech repeatTTS; 
    //ListView для отображения текста
	private ListView wordList;
	
	//Логирование
	private final String LOG_TAG = "SpeechRepeatActivity";
	
    /** Подготовка речи к обработке и повтору */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	//Вызов суперкласса
        super.onCreate(savedInstanceState);

        setContentView(ru.wmooney.repeatspeach.R.layout.activity_main);

        //получаем ссылку на кнопку
        Button speechBtn = (Button) findViewById(ru.wmooney.repeatspeach.R.id.speech_btn);
        //получаем ссылку на word list
        wordList = (ListView) findViewById(ru.wmooney.repeatspeach.R.id.word_list);
        
        //выясняем, поддерживается ли распознавание речи
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities
        		(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) {
        	//если распознавание речи поддерживается - определяем нажатия кнопок
            speechBtn.setOnClickListener(this);
            //подготовить TTS для повтора слов
            Intent checkTTSIntent = new Intent();  
            //проверить данные TTS
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
            //начать проверку Intent - получить результат в onActivityResult
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE); 
        }
        else 
        {
        	//
            //если распознавание речи не поддерживается, отключяем кнопку  и выводим сообщение
            speechBtn.setEnabled(false);
            Toast.makeText(this, "\n" +  "К сожалению, распознавание речи не поддерживается!", Toast.LENGTH_LONG).show();
        }

            // TextView wordView = (TextView)viewlist;


        wordList.setOnItemClickListener(new OnItemClickListener() {
        	
        	//click listener for items within list
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {

            	TextView wordView = (TextView)view;
                // получить выбранное слово
            	String wordChosen = (String) wordView.getText();
                //вывод для отладки
            	Log.v(LOG_TAG, "chosen: "+wordChosen);
                //произнести слово, используя TTS
            	repeatTTS.speak("Вы сказали: "+wordChosen, TextToSpeech.QUEUE_FLUSH, null);
            	//вывод сообщения
            	Toast.makeText(MainActivity.this, "Вы сказали: "+wordChosen, Toast.LENGTH_SHORT).show();//**alter for your Activity name***
            }
        });
    }
    
    /**
     *
     * Вызывается, когда пользователь нажимает кнопку
     */
    public void onClick(View v) {
        if (v.getId() == ru.wmooney.repeatspeach.R.id.speech_btn) {
        	//listen for results
            listenToSpeech();
        }
    }
    
    /**
     * Слушать ввод речи пользователя
     */
    private void listenToSpeech() {
    	//начать распознавание речи
    	Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    	listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
    	//сообщение для отображения во время прослушивания
    	listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите!");

    	listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
    	//сколько вернуть результатов
    	listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

    	//начать слушать
        startActivityForResult(listenIntent, VR_REQUEST);
    }


    /**
     * onActivityResults обрабатывает:
     *  - получение результатов распознавания речи при прослушивании
     *  - получение результата проверки данных TTS
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //проверка результата распознавания речи
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK) 
        {
        	//сохранение возвращенный список слов как ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //установка полученного списока для отображения в ListView с помощью ArrayAdapter
            String Result;
            Result = suggestedWords.get(0);
            wordList.setAdapter(new ArrayAdapter<String> (this, ru.wmooney.repeatspeach.R.layout.word, suggestedWords));



            repeatTTS.speak(Result, TextToSpeech.QUEUE_FLUSH, null);

        }
        
        //
        if (requestCode == MY_DATA_CHECK_CODE) 
        {  
	        //если есть данные - создать экземпляр TTS
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)  
	        	repeatTTS = new TextToSpeech(this, this);  
	        //нет данных - предложить пользователю установить их
	        else 
	        {  
	        	//загрузка TTS в Play Маркете
	        	Intent installTTSIntent = new Intent();  
	        	installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);  
	        	startActivity(installTTSIntent);  
	        }  
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /**
     * onInit срабатывает при инициализации TTS
     */
    public void onInit(int initStatus) { 
    	//в случае успеха установить локаль
    	 if (initStatus == TextToSpeech.SUCCESS)   
    	  repeatTTS.setLanguage(Locale.getDefault());//***выбрать локаль здесь***
          repeatTTS.setSpeechRate(2f); // скорость
          repeatTTS.setPitch(0.6f); // тембр
    	   
    }
}
