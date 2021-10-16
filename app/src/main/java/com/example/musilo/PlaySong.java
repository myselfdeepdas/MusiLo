package com.example.musilo;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PlaySong extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateSeekBarTask);
        mediaPlayer.stop();
        mediaPlayer.release();
    }
    TextView textView,txtSongStart, txtSongEnd;
    ImageView play, previous, next ,forward, backward, setLoop, setShuffle;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    int position;
    SeekBar seekBar;
    boolean looping, shuffling;
    private Handler mHandler = new Handler();
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home)
//        {
//            onBackPressed();
//
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private Runnable mUpdateSeekBarTask = new Runnable() {
        public void run() {
            String duration = createTime(mediaPlayer.getDuration());
            String currentTime = createTime(mediaPlayer.getCurrentPosition());
            txtSongStart.setText(currentTime);
            txtSongEnd.setText(duration);
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        txtSongStart=findViewById(R.id.txtSongStart);
        txtSongEnd=findViewById(R.id.txtSongEnd);
        backward=findViewById(R.id.backward);
        forward=findViewById(R.id.forward);
        setLoop=findViewById(R.id.loop);
        setShuffle=findViewById(R.id.shuffle);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        position = intent.getIntExtra("position", 0);

        playSong(songs.get(position));

        seekBar.setOnSeekBarChangeListener(this);

        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.white),PorterDuff.Mode.SRC_IN);

        final int delay = 1000;

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    play.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                }
                else{
                    play.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                }

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position!=0){
                    position = position - 1;
                }
                else{
                    position = songs.size() - 1;
                }
                playSong(songs.get(position));
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position!=songs.size()-1){
                    position = position + 1;
                }
                else{
                    position = 0;
                }
                playSong(songs.get(position));
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }

            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }

            }
        });

        setLoop.setOnClickListener(v -> {
            looping = !looping;
            mediaPlayer.setLooping(looping);
            setLoop.setImageResource(looping ? R.drawable.loop_on : R.drawable.loop);
        });

        setShuffle.setOnClickListener(v -> {
            shuffling = !shuffling;
            setShuffle.setImageResource(shuffling ? R.drawable.shuffle_on : R.drawable.shuffle);
        });

    }

    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time = time+min+":";
        if(sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }

    private void playSong(File song) {
        Uri uri = Uri.parse(song.toString());

        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(shuffling) {
                        playRandomSong();
                    } else {
                        next.performClick();
                    }
                }
            });
        } else {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(this, uri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String name = song.getName().replace(".mp3", "");
        textView.setText(name);
        textView.setSelected(true);

        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        mHandler.postDelayed(mUpdateSeekBarTask, 1000);
    }

    private void playRandomSong() {
        Random rand = new Random();
        File song = songs.get(rand.nextInt(songs.size()));
        playSong(song);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateSeekBarTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateSeekBarTask);
        mediaPlayer.seekTo(seekBar.getProgress());
        mHandler.postDelayed(mUpdateSeekBarTask, 500);
    }
}
