package net.schnorbus.basti.homeview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.DataPoint;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import net.schnorbus.basti.homeview.util.API_Keys;
import net.schnorbus.basti.homeview.util.CustomDigitalClock;

import org.junit.Test;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dalvik.annotation.TestTarget;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static junit.framework.Assert.assertEquals;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private CustomDigitalClock clock;
    private TextView tv;
    private TextView tvicon;
    private TextView tv_update;
    private TextView tv_dayofweek;
    private TextView tv_date;
    private ImageView image;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        clock = (CustomDigitalClock) findViewById(R.id.digitalClock1);
        tv = (TextView) findViewById(R.id.weatherresponse);
        tvicon = (TextView) findViewById(R.id.weathericon);
        image = (ImageView) findViewById(R.id.weatherimage);
        tv_update=(TextView)findViewById(R.id.update_timestamp);
        tv_date=(TextView)findViewById(R.id.date);
        tv_dayofweek=(TextView)findViewById(R.id.dayofweek);

        ForecastApi.create(API_Keys.forecast_io);
        //updateWeatherData();
        handler.post(runnableCode);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private void updateWeatherData() {
        final RequestBuilder weather = new RequestBuilder();
        Request request = new Request();
        request.setLat("49.4135");
        request.setLng("8.7081");
        request.setUnits(Request.Units.CA);
        request.setLanguage(Request.Language.PIG_LATIN);
//        request.addExcludeBlock(Request.Block.CURRENTLY);
        //tv.setText("Weather requested");

        Context context = getApplicationContext();
        CharSequence text = "Weather update requested";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                tvicon.setText(weatherResponse.getCurrently().getIcon());
                updateWeatherUi(weatherResponse.getCurrently());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Tag", "Error while calling: " + error.getUrl() + error.getMessage());
                //update with mock data
                updateWeatherUi(null);
            }
        });
    }

    public Bitmap getImageByName(String nameOfTheDrawable, Activity a){
        Drawable drawFromPath;
        int path = a.getResources().getIdentifier(nameOfTheDrawable, "drawable", "net.schnorbus.basti.homeview");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap source = BitmapFactory.decodeResource(a.getResources(), path, options);

        return source;
    }

    private final String[] dayofweek = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    private void updateWeatherUi(DataPoint weather) {

        double temperature = weather==null ? 18.5 : weather.getTemperature();
        String iconname = weather==null ? "clear-night" : weather.getIcon();
        String summary = weather==null ? "Cloudy until tomorrow morning" : weather.getSummary();

        DecimalFormat devFmt = new DecimalFormat("##°");
        tv.setText(devFmt.format(temperature));
        tvicon.setText(summary);
        SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        tv_update.setText("Update: "+s.format(new Date()));
        String weekDay;
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.GERMAN);

        Calendar calendar = Calendar.getInstance();
        tv_dayofweek.setText(dayFormat.format(calendar.getTime()));

        s=new SimpleDateFormat("dd. MMMM");
        tv_date.setText(s.format(new Date()));
        Bitmap b=getImageByName(createWeatherImgName("and", iconname, 128), this);
        if (b==null) {
            Log.d("Iconname", "Not found for icon: " + iconname);
        }
        else
            image.setImageBitmap(b);
    }

    @NonNull
    private String createWeatherImgName(String iconname, int size) {
        return createWeatherImgName("", iconname, size);
    }

    private String createWeatherImgName(String prefix, String iconname, int size) {
        StringBuilder sb = new StringBuilder(prefix);

        if (sb.toString().length()>0)
            sb.append("_");

        sb.append(iconname)
            .append("_")
            .append(size);

        return sb.toString().replace('-', '_');
    }

    @Test
    public void testWeatherImgFilename() {
      assertEquals("little_rain_128", createWeatherImgName("little-rain", 128));
    }

    @Test
    public void testWeatherImgFilenameWithPrefix() {
        assertEquals("w10_partly_cloudy_36", createWeatherImgName("w10", "partly-cloudy", 36));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);

        handler.removeCallbacks(runnableCode);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    Handler handler=new Handler();

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            updateWeatherData();
            Log.d("Handlers", "Called on main thread");

            handler.postDelayed(runnableCode, 120000);
        }
    };
}
