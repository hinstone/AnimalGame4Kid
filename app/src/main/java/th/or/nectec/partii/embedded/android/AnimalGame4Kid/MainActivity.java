package th.or.nectec.partii.embedded.android.AnimalGame4Kid;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import th.or.nectec.partii.embedded.android.AnimalGane4Kid.R;
import th.or.nectec.partii.embedded.android.EmbeddedUtils.ModelUtil;
import th.or.nectec.partii.embedded.android.RecognitionListener;
import th.or.nectec.partii.embedded.android.SpeechRecognizer;


public class MainActivity extends AppCompatActivity implements RecognitionListener, ModelUtil.OnReceiveStatusListener {

    private String[] AnimalNames = {
            "ควาย",
            "วัว",
            "เป็ด",
            "ปลา",
            "กบ",
            "ม้า",
            "สิงโต",
            "แพนด้า",
            "หมู",
            "แกะ",
            "เสือ",
            "เต่า"
    };

    private Integer[] AnimalImages = {
            R.drawable.buffalo,
            R.drawable.cow,
            R.drawable.duck,
            R.drawable.fish,
            R.drawable.frog,
            R.drawable.horse,
            R.drawable.lion,
            R.drawable.panda,
            R.drawable.pig,
            R.drawable.sheep,
            R.drawable.tiger,
            R.drawable.turtle
    };

    private int AnimalIndex = 0;

    private SpeechRecognizer recognizer;
    Context context = null;
    private ImageView img_animal = null;
    private ImageView img_answer = null;
    private LinearLayout lyt_ctl = null;
    private Button btn_new = null;
    private Button btn_recog = null;
    private Button btn_download = null;
    private TextView txt_result = null;
    private boolean isSetupRecognizer = false;
    boolean flag = true;
    private String decodedStr = "";
    ModelUtil mUtil = null;
    String APIKEY = "YOUR Partii2go APIKEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mUtil = new ModelUtil();
        mUtil.setOnReceiveDialogStatus(MainActivity.this);

        img_animal = (ImageView) findViewById(R.id.img_animal);

        lyt_ctl = (LinearLayout) findViewById(R.id.lyt_ctl);
        img_answer = (ImageView) findViewById(R.id.img_answer);
        txt_result = (TextView) findViewById(R.id.txt_result);

        btn_new = (Button) findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int min = 0;
                int max = AnimalNames.length - 1;
                Random r = new Random();
                AnimalIndex = r.nextInt(max - min) + min;

                img_animal.setImageResource(AnimalImages[AnimalIndex]);
                img_answer.setImageResource(R.drawable.none);
            }
        });

        btn_recog = (Button) findViewById(R.id.btn_recog);
        btn_recog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSetupRecognizer) {
                    if (flag) {
                        flag = false;
                        btn_recog.setText("หยุด");
                        recognizer.startListening();
                    } else {
                        flag = true;
                        btn_recog.setText("พูด");
                        recognizer.stop();
                    }
                }
            }
        });

        btn_download = (Button) findViewById(R.id.btn_download);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mUtil.isPermissionGranted(getApplicationContext())) {
                    mUtil.requestPermission(getApplicationContext());
                }
                else if (!mUtil.isGetAssets(getExternalFilesDir(""))) {
                    mUtil.startDownload(context, MainActivity.this, getExternalFilesDir(""), APIKEY);
                }
            }
        });

        if(mUtil.isPermissionGranted(getApplicationContext())) {
            if(mUtil.isSyncDir(getExternalFilesDir("")) && !isSetupRecognizer) {
                setUpRecognizer();
            }
        }
        else {
            mUtil.requestPermission(getApplicationContext());
            lyt_ctl.setVisibility(View.GONE);
            btn_download.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecognizer(){
        Log.d("Recognizer", "Setting recognizer");

        try {
            recognizer = mUtil.getRecognizer(context);
            if (recognizer.getDecoder() == null) {
                finish();
            }
            recognizer.addListener(this);
            isSetupRecognizer = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verfiyAnimal(String result) {
        img_answer.setImageResource(R.drawable.incorrect);
        for (int i = 0; i < AnimalNames.length; i++) {
            if (result.trim().compareTo(AnimalNames[i].trim()) == 0) {
                if (i == AnimalIndex) {
                    img_answer.setImageResource(R.drawable.correct);
                    break;
                }
            }
        }
    }

    // Download models
    private Runnable downloadModel = new Runnable() {
        @Override
        public void run() {
            try {
                // waiting for permission granted
                while (!mUtil.isPermissionGranted(getApplicationContext())) {
                    System.err.println("waiting for permission granted");
                    Thread.sleep(1000);
                }
                if (!mUtil.isGetAssets(getExternalFilesDir(""))) {
                    mUtil.startDownload(context, MainActivity.this, getExternalFilesDir(""), APIKEY);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onProgress(int i) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(String s) {

    }

    @Override
    public void onResult(String s) {
        if (s != null) {
            if (!s.equals(SpeechRecognizer.NO_HYP) && !s.equals(SpeechRecognizer.REQUEST_NEXT)) {
                decodedStr = s + " ";
            }
        }
        txt_result.setText(decodedStr);
        verfiyAnimal(decodedStr);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onReceiveDownloadComplete() {
        Log.d("Recognizer", "DownloadComplete");

        btn_download.setVisibility(View.GONE);
        lyt_ctl.setVisibility(View.VISIBLE);
        setUpRecognizer();
    }

    @Override
    public void onReceiveDownloadFailed() {

    }
}
