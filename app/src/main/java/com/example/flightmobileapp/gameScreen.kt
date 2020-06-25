package com.example.flightmobileapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.Gravity
import android.widget.SeekBar
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.zerokol.views.joystickView.JoystickView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game_screen.*
import kotlinx.coroutines.delay
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class gameScreen : AppCompatActivity() {

    //variables
    private var aileron: Double = 0.0
    private var rudder: Double = 0.0
    private var elevator: Double = 0.0
    private var throttle: Double = 0.0
    private var prevAileron: Double = 0.0
    private var prevRudder: Double = 0.0
    private var prevElevator: Double = 0.0
    private var prevThrottle: Double = 0.0

    // THROTTLE: (0, 1), AILERON: (-1,1), ELEVATOR: (-1,1), RUDDER: (-1,1)
    /*private var angleTextView: TextView? = null
    private var powerTextView: TextView? = null
    private var directionTextView: TextView? = null*/
    private var joystick: JoystickView? = null
    private var urlAddress: String? = null
    private var imageFromSimulator: ImageView? = null
    private var rudderSeekBar: SeekBar? = null
    private var throttleSeekBar: SeekBar? = null
    private var rudderTextView: TextView? = null
    private var throttleTextView: TextView? = null
    private var stop = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)
        urlAddress = intent.getStringExtra(EXTRA_MESSAGE)
        if (urlAddress == null) {
            finish()
        }
        initViewElements(savedInstanceState)


        joystick?.setOnJoystickMoveListener(object : JoystickView.OnJoystickMoveListener {
            override fun onValueChanged(angle: Int, power: Int, direction: Int) {
                determineAileronAndElevator(angle, power)
                if (changedAtLeastInOnePercent()) {
                    sendWheelsData();
                }
            }
        }, 100)


        //For rudder seek bar
        rudderSeekBar?.max = 200;
        val rudderChanged: SeekBar.OnSeekBarChangeListener =
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    rudder = (progress.toDouble() - 100.0) / 100.0;
                    rudderTextView?.text = rudder.toString()
                    if (changedAtLeastInOnePercent()) {
                        sendWheelsData();
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    rudderTextView?.text = rudder.toString()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    rudderTextView?.text = rudder.toString()
                }
            }
        rudderSeekBar?.setOnSeekBarChangeListener(rudderChanged)

        //For throttle seek bar
        val throttleChanged: SeekBar.OnSeekBarChangeListener =
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    throttle = progress.toDouble() / 100.0;
                    throttleTextView?.text = throttle.toString()
                    if (changedAtLeastInOnePercent()) {
                        sendWheelsData();
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    throttleTextView?.text = throttle.toString()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    throttleTextView?.text = throttle.toString()
                }
            }
        throttleSeekBar?.setOnSeekBarChangeListener(throttleChanged)
    }

    //!!!!!!!!!!!!!!!!!!!!!!! Stopped here - don't let me to put breakpoint !!!!!!!!!!!!!!!!!!!!!
    private fun changedAtLeastInOnePercent(): Boolean {

//        println("=============================================")
//        println("=============================================")
//        println("prevAileron=$prevAileron ; aileron=$aileron")
//        println("prevElevator=$prevElevator ; elevator=$elevator")
//        println("prevRudder=$prevRudder ; rudder=$rudder")
//        println("prevThrottle=$prevThrottle ; throttle=$throttle")
//        println("=============================================")

        val offset = 0.01
        return (abs(aileron - prevAileron) >= offset
                || abs(rudder - prevRudder) >= offset
                || abs(throttle - prevThrottle) >= offset
                || abs(elevator - prevElevator) >= offset)

//        return ((aileron > upperLimit * prevAileron) || (aileron < lowerLimit * prevAileron)
//                || (rudder > upperLimit * prevRudder) || (rudder < lowerLimit * prevRudder)
//                || (throttle > upperLimit * prevThrottle) || (throttle < lowerLimit * prevThrottle)
//                || (elevator > upperLimit * prevElevator) || (elevator < lowerLimit * prevElevator))
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("rudderTextView", rudderTextView?.text.toString())
        savedInstanceState.putInt("rudderSeekBarProgress", rudderSeekBar?.progress!!)
        savedInstanceState.putString("throttleTextView", throttleTextView?.text.toString())
        savedInstanceState.putInt("throttleSeekBarProgress", throttleSeekBar?.progress!!)
    }

    //init view elements (according to screen mode)
    private fun initViewElements(savedInstanceState: Bundle?) {

        imageFromSimulator = findViewById(R.id.imageSimulatorLand) as? ImageView
        if (imageFromSimulator == null) {
            imageFromSimulator = findViewById(R.id.imageSimulator) as? ImageView
        }

        rudderSeekBar = findViewById(R.id.rudderSeekBar)
        if (rudderSeekBar == null) {
            rudderSeekBar = findViewById(R.id.rudderSeekBar_land)
        }

        throttleSeekBar = findViewById(R.id.throttleSeekBar)
        if (throttleSeekBar == null) {
            throttleSeekBar = findViewById(R.id.throttleSeekBar_land)
        }

        rudderTextView = findViewById(R.id.rudderTextView)
        if (rudderTextView == null) {
            rudderTextView = findViewById(R.id.rudderTextView_land)
        }

        throttleTextView = findViewById(R.id.throttleTextView)
        if (throttleTextView == null) {
            throttleTextView = findViewById(R.id.throttleTextView_land)
        }

        joystick = findViewById(R.id.joystickView) as? JoystickView
        if (joystick == null) {
            joystick = findViewById(R.id.joystickView_land)
        }

        initViewElementsValues(savedInstanceState)
    }

    private fun initViewElementsValues(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            //init to saved values
            rudderTextView?.text = savedInstanceState.getString("rudderTextView")
            rudderSeekBar?.progress = savedInstanceState.getInt("rudderSeekBarProgress")
            throttleTextView?.text = savedInstanceState.getString("throttleTextView")
            throttleSeekBar?.progress = savedInstanceState.getInt("throttleSeekBarProgress")

        } else {
            //init all to default values
            rudderTextView?.text = "0.0"
            rudderSeekBar?.progress = 100
            throttleTextView?.text = "0.0"
            throttleSeekBar?.progress = 0
        }
    }

    private fun determineAileronAndElevator(angle: Int, power: Int) {

        val alpha: Int;
        when (angle) {
            in 0..90 -> {
                alpha = angle
                aileron = (sin(Math.toRadians(alpha.toDouble())) * power) / 100.0;
                elevator = (cos(Math.toRadians(alpha.toDouble())) * power) / 100.0;
            }
            in 91..180 -> {
                alpha = angle - 90
                aileron = (cos(Math.toRadians(alpha.toDouble())) * power) / 100.0;
                elevator =
                    ((sin(Math.toRadians(alpha.toDouble())) * power) * (-1.0 / 100.0));
            }
            in -180..-90 -> {
                alpha = angle + 180
                aileron = (sin(Math.toRadians(alpha.toDouble())) * power) * (-1.0 / 100.0);
                elevator = (cos(Math.toRadians(alpha.toDouble())) * power) * (-1.0 / 100.0);
            }
            in -90..0 -> {
                alpha = angle * (-1)
                aileron = (sin(Math.toRadians(alpha.toDouble())) * power) * (-1.0 / 100.0)
                elevator = (cos(Math.toRadians(alpha.toDouble())) * power) / 100.0;
            }
        }

    }

    private fun getImage(url: String?) {
        if (url == null) {
            return
        }
        try {
            val retrofit = Retrofit.Builder().baseUrl(url).build();

            //Defining the api for sending by the request
            val api = retrofit.create(Api::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                //Sending the request
                while (!stop) {
                    delay(250)
                    api.getScreenShot().enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.message() == "OK") {
                                //println("Successfully got screenshot")
                                showImage(response)
                            } else {
                                //todo write "Error connecting"
                                println("Failed to get screenshot - on response")

                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            //todo write "Error connecting"
                            println("Failed to get screenshot - on failure")
                        }
                    })
                }
            }
        } catch (e: Exception) {
            //todo write "Error connecting"
            println("Failed to get screenshot - invalid url")
        }
    }


    override fun onPause() {
        super.onPause()
        stop = true
    }

    override fun onResume() {
        super.onResume()
        stop = false
        getImage(urlAddress)
    }


    private fun showImage(response: Response<ResponseBody>) {
        try {
            val imageStream = response.body()?.byteStream()
            val theImage = BitmapFactory.decodeStream(imageStream)
            /*val bytes = response.body()?.bytes()
            val theImage = bytes?.size?.let { BitmapFactory.decodeByteArray(bytes, 0, it) }*/
            //println("the image is:##########################$theImage")
            runOnUiThread {
                imageFromSimulator?.setImageBitmap(theImage)
            }
        } catch (e: Exception) {
            println(e.message)
        }

    }

    fun sendWheelsData() {
        val json =
            "{\"aileron\": $aileron,\n \"rudder\": $rudder,\n \"elevator\": $elevator,\n \"throttle\": $throttle\n}"
        val rb: RequestBody = RequestBody.create(MediaType.parse("application/json"), json)
        val gson = GsonBuilder().setLenient().create();

        //Create the retrofit instance to issue with the network requests:
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(urlAddress.toString())
                .addConverterFactory(GsonConverterFactory.create(gson)).build();


            //Defining the api for sending by the request
            val api = retrofit.create(Api::class.java)

            //Sending the request
            api.postJoystickData(rb).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    println("Successfully posted joystick data to controller");
                    prevAileron = aileron;
                    prevElevator = elevator;
                    prevRudder = rudder;
                    prevThrottle = throttle;
                    println(json)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("Failed to post joystick data to controller");
                }
            })
        } catch (e: Exception) {
            toastMessage("Error occurred, check the URL and try again");
        }
    }

    private fun toastMessage(messageText: String) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, messageText, duration)
        toast.setGravity(Gravity.BOTTOM or Gravity.START, 100, 250)
        toast.show()
    }

    /*
    * This function made to deal with the case in which the prev value is 0, so when checking
    * if value changed in 1 percent when comparing this to prev value it always returns false
    *
    * */
//    private fun initPrev(current: Double): Double {
//        if(current == 0.0){
//            return 0.01
//        }
//        return current
//    }
}