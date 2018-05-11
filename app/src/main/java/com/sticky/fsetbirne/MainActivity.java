package com.sticky.fsetbirne;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity
{
    String ip = "192.168.4.1";
    String[] modes;
    String httpRequest;

    int RGB_MAX = 255;
    //Segments
    int BULB_START = 0;
    int BULB_END = 600;
    int OUTER_START = BULB_START;
    int OUTER_END = 199;
    int THREAD_START = 400;
    int THREAD_END = BULB_END;
    int INNER_START = 200;
    int INNER_END = 399;
    //Programs



    int segmentStart[] = {BULB_START,INNER_START,THREAD_START};
    int segmentEnd[] = {OUTER_END,INNER_END,BULB_END};


    int brightness = 100,
            mode = 1,
            led_Start = BULB_START,
            led_End = BULB_END,
            r = RGB_MAX,
            g = RGB_MAX,
            b = RGB_MAX,
            hue = 100,
            delay = 200;

    boolean connected;
    Button changeButton, executeButton;
    NumberPicker delayPicker, brightnessPicker;
    EditText pickRed, pickGreen,pickBlue;
    Spinner modeMenu, startSegmentMenu, endSegmentMenu;
    ImageView statusLight;
    RequestQueue requestQueue;
    StringRequest stringRequest;
    ArrayAdapter<CharSequence> modeAdapter, startSegmentAdapter, endSegmentAdapter;
    Toast dbug;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        checkConnection();
        /* TODO Make it work
        *
        * */
        // ---Number Pickers---
        //Delay
        delayPicker = (NumberPicker) findViewById(R.id.picker_delay);
        delayPicker.setMinValue(0);
        delayPicker.setMaxValue(1000);

        //Brightness
        brightnessPicker = (NumberPicker) findViewById(R.id.picker_brightness);
        brightnessPicker.setMaxValue(100);
        brightnessPicker.setMinValue(0);

        //---EditText---
        //Red
        pickRed = (EditText) findViewById(R.id.input_R);
        pickRed.setInputType(InputType.TYPE_CLASS_NUMBER);
        pickRed.setText("255");
        //Green
        pickGreen = (EditText) findViewById(R.id.input_G);
        pickGreen.setInputType(InputType.TYPE_CLASS_NUMBER);
        pickGreen.setText("255");

        //Blue
        pickBlue = (EditText) findViewById(R.id.input_B);
        pickBlue.setInputType(InputType.TYPE_CLASS_NUMBER);
        pickBlue.setText("255");
        //---Spinner---
        //Mode
        modeMenu = (Spinner) findViewById(R.id.spinner_mode);
        modeAdapter = ArrayAdapter.createFromResource(this,R.array.modes,android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeMenu.setAdapter(modeAdapter);
        //OnItemSelect
        modeMenu.setOnItemSelectedListener
                (
                new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                mode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                mode = 1;
            }
        }
                );

        //Segment Start
        startSegmentMenu = (Spinner) findViewById(R.id.picker_startLED);
        startSegmentAdapter = ArrayAdapter.createFromResource(this, R.array.segments, android.R.layout.simple_spinner_item);
        startSegmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startSegmentMenu.setAdapter(startSegmentAdapter);
        //OnItemSelect
        startSegmentMenu.setOnItemSelectedListener
                (
                new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                led_Start = segmentStart[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                led_Start = BULB_START;
            }
        }
                );

        //Segment End
        endSegmentMenu = (Spinner) findViewById(R.id.picker_endLED);
        endSegmentAdapter = ArrayAdapter.createFromResource(this, R.array.segments, android.R.layout.simple_spinner_item);
        endSegmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endSegmentMenu.setAdapter(endSegmentAdapter);
        //OnItemSelect
        endSegmentMenu.setOnItemSelectedListener
                (
                new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                led_End = segmentEnd[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                led_End = BULB_END;
            }
        }
                );
        //---ImageView---
        statusLight = (ImageView) findViewById(R.id.icon_connection);
        statusLight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkConnection();
            }
        });

        //---Button---
        changeButton = (Button) findViewById(R.id.button_send);
        changeButton.setOnClickListener
                (
                new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                brightness = brightnessPicker.getValue();
                r = Integer.parseInt(pickRed.getText().toString());
                g = Integer.parseInt(pickGreen.getText().toString());
                b = Integer.parseInt(pickBlue.getText().toString());
                float hsv[] = new float[3];
                Color.RGBToHSV(r,g,b,hsv);
                hue = Math.round(hsv[0]);
                delay = delayPicker.getValue();
                dbug = Toast.makeText(v.getContext(),"brightness = " + brightness + " \nhue = " + hue + " \nmode = " + mode + " \nStart LED = " + led_Start + " \nEnd LED = " + led_End,Toast.LENGTH_SHORT);
                dbug.show();
            }
        }
                );

        executeButton = (Button) findViewById(R.id.button_execute);
        executeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stringRequest = new StringRequest(Request.Method.POST, ip + "/settings"
                        , new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if (response.equals("ok"))
                        {
                            statusLight.setBackgroundResource(R.color.color_ok);
                        }
                        else
                        {
                            statusLight.setBackgroundResource(R.color.color_error);
                        }
                    }
                }
                        , new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        statusLight.setBackgroundResource(R.color.color_error);
                    }
                }
                )
                {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("program", ""+ mode);
                        MyData.put("brightness","" + brightness);
                        MyData.put("start", "" + led_Start);
                        MyData.put("ending", "" + led_End);
                        MyData.put( "hue" , "" + hue);
                        return MyData;
                    }
                };
                requestQueue.add(stringRequest);
            }
        });
    }

    public void checkConnection()
    {
        stringRequest = new StringRequest(Request.Method.GET, ip + "/status"
                , new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                if (response.equals("ok"))
                {
                    statusLight.setBackgroundResource(R.color.color_ok);
                    connected = true;
                }
                else
                {
                    statusLight.setBackgroundResource(R.color.color_error);
                    connected = false;
                }
            }
        }
                , new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                statusLight.setBackgroundResource(R.color.color_error);
            }
        }
        );
        dbug = Toast.makeText(this, "Request sent",Toast.LENGTH_LONG);
        dbug.show();

        requestQueue.add(stringRequest);
    }
}
