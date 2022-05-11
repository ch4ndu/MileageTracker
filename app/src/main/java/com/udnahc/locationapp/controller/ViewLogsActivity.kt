package com.udnahc.locationapp.controller

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.udnahc.locationapp.R
import com.udnahc.locationapp.ViewLogFileActivity
import com.udnahc.locationapp.location.TransitionRecognitionReceiver
import java.io.File
import java.util.*

class ViewLogsActivity : UtilActivity() {
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_logs)
        recyclerView = findViewById(R.id.logsRecycler)
        setToolbarWithBack()
        title = "Log Files"
        val path = File("$filesDir/logs/")
        val files = path.listFiles()
        val fileList: MutableList<File> = ArrayList()
        if (files != null) {
            fileList.addAll(listOf(*files))
        }
        fileList.sortWith(Comparator { left, right -> right.name.compareTo(left.name) })
        val adapter = LogsAdapter(fileList)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_logs, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_logs) {
            MaterialDialog(this, MaterialDialog.DEFAULT_BEHAVIOR)
                .title(null, "Delete all log files")
                .positiveButton(text = "Ok") {
                    val path = File("$filesDir/logs/")
                    val files = path.listFiles()
                    if (files != null) {
                        for (file1 in files) {
                            file1.delete()
                        }
                    }
                    TransitionRecognitionReceiver.clearTransition(this)
                }
                .negativeButton(text = "Cancel")
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class LogsAdapter internal constructor(private val fileList: List<File>) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.header_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = fileList[position].toString()
            holder.textView.text = file.substring(file.lastIndexOf("/") + 1)
        }

        override fun getItemCount(): Int {
            return fileList.size
        }

        internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.header_title)

            init {
                textView.setOnClickListener { v ->
                    val intent = Intent(v.context, ViewLogFileActivity::class.java)
                    val file = fileList[adapterPosition].toString()
                    intent.putExtra("fileName", file.substring(file.lastIndexOf("/") + 1))
                    v.context.startActivity(intent)
                }
            }
        }

    }
}
