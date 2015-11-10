package de.spas.kissthefrog;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int FROG_ID = 212121;
    private int points;
    private int round;
    private int countdown;
    private int highscore;
    private Random rnd = new Random();
    private Handler handler = new Handler();
    private Typeface ttf;
    private ImageView frog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ttf = Typeface.createFromAsset(getAssets(), "JandaManateeSolid.ttf");
        ((TextView)findViewById(R.id.countdown)).setTypeface(ttf);
        ((TextView)findViewById(R.id.round)).setTypeface(ttf);
        ((TextView)findViewById(R.id.points)).setTypeface(ttf);
        ((TextView)findViewById(R.id.highscore)).setTypeface(ttf);
        ((TextView)findViewById(R.id.help)).setTypeface(ttf);
        findViewById(R.id.help).setOnClickListener(this);
        showStartFragment();
    }

    private void loadHighscore() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        highscore = sp.getInt("highscore", 0);
    }

    private void saveHighscore(int points) {
        highscore=points;
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt("highscore", highscore);
        e.commit();
    }


    private void newGame() {
        points=0;
        round=1;
        initRound();
    }

    private void initRound() {
        countdown=10;
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        WimmelView wv = new WimmelView(this);
        container.addView(wv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        wv.setImageCount(8*(10+round));
        frog = new ImageView(this);
        frog.setId(FROG_ID);
        frog.setImageResource(R.drawable.frog);
        frog.setScaleType(ImageView.ScaleType.CENTER);
        float scale = getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Math.round(64*scale),Math.round(61*scale));
        lp.gravity = Gravity.TOP + Gravity.LEFT;
        lp.leftMargin = rnd.nextInt(container.getWidth()-64);
        lp.topMargin = rnd.nextInt(container.getHeight()-61);
        frog.setOnClickListener(this);
        container.addView(frog, lp);
        update();
        handler.postDelayed(runnable,1000-round*50);
    }

    private void update() {
        fillTextView(R.id.points, Integer.toString(points)+" ");
        loadHighscore();
        fillTextView(R.id.highscore, Integer.toString(highscore));
        fillTextView(R.id.round, " "+Integer.toString(round));
        fillTextView(R.id.countdown, Integer.toString(countdown*1000)+" ");
    }

    private void fillTextView(int id, String text) {
        TextView tv = (TextView) findViewById(id);
        tv.setText(text);
    }

    private void showStartFragment() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.fragment_start, null));
        container.findViewById(R.id.start).setOnClickListener(this);
        ((TextView)findViewById(R.id.title)).setTypeface(ttf);
        ((TextView)findViewById(R.id.start)).setTypeface(ttf);
    }

    private void showGameOverFragment() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.addView( getLayoutInflater().inflate(R.layout.fragment_gameover, null) );
        container.findViewById(R.id.play_again).setOnClickListener(this);
        ((TextView)findViewById(R.id.title)).setTypeface(ttf);
        ((TextView)findViewById(R.id.play_again)).setTypeface(ttf);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.start) {
            startGame();
        } else if(view.getId()==R.id.play_again) {
            showStartFragment();
        } else if(view.getId()==FROG_ID) {
            kissFrog();
        } else if(view.getId()==R.id.help) {
            showTutorial();
        }
    }

    private void kissFrog() {
        handler.removeCallbacks(runnable);
        showToast(R.string.kissed);
        points += countdown*1000;
        round++;
        initRound();
    }

    private void showToast(int stringResId) {
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        TextView textView = new TextView(this);
        textView.setText(stringResId);
        textView.setTextColor(getResources().getColor(R.color.points));
        textView.setTextSize(48f);
        textView.setTypeface(ttf);
        toast.setView(textView);
        toast.show();
    }

    private void showTutorial() {
        final Dialog dialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_tutorial);
        ((TextView)(dialog.findViewById(R.id.text))).setTypeface(ttf);
        ((TextView)(dialog.findViewById(R.id.start))).setTypeface(ttf);
        dialog.findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startGame();
            }
        });
        dialog.show();
    }

    private void startGame() {
        newGame();
    }

    private void countdown() {
        countdown--;
        update();
        if(countdown<=0) {
            frog.setOnClickListener(null);
            if(points>highscore) {
                saveHighscore(points);
                update();
            }
            showGameOverFragment();
        } else {
            handler.postDelayed(runnable, 1000 - round * 50);
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countdown();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}


