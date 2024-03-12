package com.example.bubu

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts.Intents.UI
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class dashboard : AppCompatActivity() {
    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)



        val email:String = FirebaseAuth.getInstance().currentUser?.email.toString()
        val UID:String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val username:String = FirebaseAuth.getInstance().currentUser?.displayName.toString()

        findViewById<TextView>(R.id.email).setText(email)
        findViewById<TextView>(R.id.UID).setText(UID)
        findViewById<TextView>(R.id.username).setText(username)

        checkGoogleFitPermissions()

    }

    private fun checkGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.d(TAG,"Dikkat hai")
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit()
            }
        }
    }

    private fun accessGoogleFit() {
        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val totalSteps = if (dataSet.isEmpty) 0 else dataSet.dataPoints.first().getValue(
                    Field.FIELD_STEPS).asInt()
                findViewById<TextView>(R.id.stepCount).text = "Steps: $totalSteps"
            }
            .addOnFailureListener { e ->
                findViewById<TextView>(R.id.stepCount).text = e.toString()
            }
    }

    companion object {
        private const val REQUEST_OAUTH_REQUEST_CODE = 1
    }
}