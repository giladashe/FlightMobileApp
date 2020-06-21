package com.example.flightmobileapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.zerokol.views.joystickView.JoystickView
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.cos
import kotlin.math.sin

private var aileron: Double = 0.0
private var rudder: Double = 0.0
private var elevator: Double = 0.0
private var throttle: Double = 0.0
// THROTTLE: (0, 1), AILERON: (-1,1), ELEVATOR: (-1,1), RUDDER: (-1,1)


class gameScreen : AppCompatActivity() {

    //variables
    private var angleTextView: TextView? = null
    private var powerTextView: TextView? = null
    private var directionTextView: TextView? = null
    private var joystick: JoystickView? = null

    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        //views
        angleTextView = findViewById(R.id.angleTextView) as? TextView
        powerTextView = findViewById(R.id.powerTextView) as? TextView
        directionTextView = findViewById(R.id.directionTextView) as? TextView
        //Referencing also other views
        joystick = findViewById(R.id.joystickView) as? JoystickView

        //connection
        val url = URL("http://10.0.2.2:52686")
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

        joystick?.setOnJoystickMoveListener(object : JoystickView.OnJoystickMoveListener {
            override fun onValueChanged(angle: Int, power: Int, direction: Int) {
                angleTextView!!.text = " " + (angle).toString() + "°"
                powerTextView!!.text = " " + (power).toString() + "%"
                val alpha:Int;

                if (angle in 0..90) {
                    alpha = angle
                    aileron = (sin(Math.toRadians(alpha.toDouble())) * power)/100.0;
                    elevator = (cos(Math.toRadians(alpha.toDouble())) * power)/100.0;
                } //V

                else if (angle in 91..180) {
                    alpha = angle - 90
                    aileron = (cos(Math.toRadians(alpha.toDouble())) * power)/100.0 ;
                    elevator = ((sin(Math.toRadians(alpha.toDouble())) * power)*(-1.0/100.0));
                } //V

                else if (angle in -180..-90) {
                    alpha = angle + 180
                    aileron = (sin(Math.toRadians(alpha.toDouble())) * power)*(-1.0/100.0);
                    elevator = (cos(Math.toRadians(alpha.toDouble())) * power)*(-1.0/100.0);
                } //V

                else if (angle in -90..0) {
                    alpha = angle * (-1)
                    aileron = (sin(Math.toRadians(alpha.toDouble())) * power)*(-1.0/100.0)
                    elevator = (cos(Math.toRadians(alpha.toDouble())) * power)/100.0;
                } //V

                println()
                println("\nx is:$aileron")
                println("y is:$elevator\n")
                        // sin($angle) * $power =
                        //cos($angle) * $power = :"
                println()
                /*println(" " + (angle).toString() + "°")
                println(" " + (power).toString() + "%")*/
                when (direction) {
                    JoystickView.FRONT -> directionTextView!!.text = R.string.front_lab.toString()
                    JoystickView.FRONT_RIGHT -> directionTextView!!.text =
                        R.string.front_right_lab.toString()
                    JoystickView.RIGHT -> directionTextView!!.text = R.string.right_lab.toString()
                    JoystickView.RIGHT_BOTTOM -> directionTextView!!.text =
                        R.string.right_bottom_lab.toString()
                    JoystickView.BOTTOM -> directionTextView!!.text = R.string.bottom_lab.toString()
                    JoystickView.BOTTOM_LEFT -> directionTextView!!.text =
                        R.string.bottom_left_lab.toString()
                    JoystickView.LEFT -> directionTextView!!.text = R.string.left_lab.toString()
                    JoystickView.LEFT_FRONT -> directionTextView!!.text =
                        R.string.left_front_lab.toString()
                    else -> directionTextView!!.text = R.string.center_lab.toString()
                }
                sendJoystickData();
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL)
    }
}


fun sendJoystickData() {

    val json: String =
        "{\"aileron\": $aileron,\n \"rudder\": $rudder,\n \"elevator\": $elevator,\n \"throttle\": $throttle\n}"
    val rb: RequestBody = RequestBody.create(MediaType.parse("application/json"), json)
    val gson = GsonBuilder().setLenient().create();

    //Create the retrofit instance to issue with the network requests:
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:52238/") //todo: change that will enable dynamic ip and port
        .addConverterFactory(GsonConverterFactory.create(gson)).build();

    //Defining the api for sending by the request
    val api = retrofit.create(Api::class.java)

    //Sending the request
    val body = api.postJoystickData(rb).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            println("Successfully posted joystick data to controller");
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            println("Failed to post joystick data to controller");
        }
    })

}