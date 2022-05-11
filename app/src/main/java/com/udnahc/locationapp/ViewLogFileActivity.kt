package com.udnahc.locationapp

import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.Companion.DEFAULT_BEHAVIOR
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.udnahc.locationapp.location.TransitionRecognitionReceiver.Companion.clearTransition
import com.udnahc.locationapp.util.RunnableAsync
import com.udnahc.locationmanager.Plog
import java.io.*

class ViewLogFileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_log_file)
        val textView = findViewById<TextView>(R.id.log_file)
        val fileName = intent.getStringExtra("fileName") ?: return
        val path = File("$filesDir/logs/")
        val file = File(path, fileName)
        val entireFile = StringBuilder()
        RunnableAsync({
            val br: BufferedReader
            try {
                br = BufferedReader(FileReader(file))
                var line: String?
                try {
                    while (br.readLine().also { line = it } != null) {
                        entireFile.append(line).append("\n")
                    }
                } catch (e: IOException) { // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }, { textView.text = entireFile }, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            MaterialDialog(this@ViewLogFileActivity, DEFAULT_BEHAVIOR)
                    .title(null, "Delete all log files")
                    .positiveButton(text = "Ok") {
                        val files = path.listFiles()
                        if (files != null) {
                            for (file1 in files) {
                                file1.delete()
                            }
                        }
                        clearTransition(this@ViewLogFileActivity)
                    }
                    .negativeButton(text = "Cancel")
                    .show()
        }
    }
}
