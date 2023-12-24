package com.samnn.filemanager

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.samnn.filemanager.databinding.ActivityFileExploreBinding
import com.samnn.filemanager.databinding.CopyDialogBinding
import com.samnn.filemanager.databinding.NewFolderDialogBinding
import com.samnn.filemanager.databinding.RenameDialogBinding
import java.io.File

class FileExploreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileExploreBinding
    private lateinit var adapter: ItemAdapter
    private lateinit var fileList: ArrayList<File>
    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileExploreBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.Appbar)

        path = intent.getStringExtra("path")
        val root = path?.let { File(it) }
        fileList = arrayListOf<File>()
        if (root != null) {
            root.listFiles()?.forEach {
                fileList.add(it)
            }
        }

        binding.fileList.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(applicationContext, fileList)
        binding.fileList.adapter = adapter
        binding.directory.text = path

        if (root != null) {
            binding.Appbar.title = root.name
        }

        if (path != Environment.getExternalStorageDirectory().path) {
            binding.Appbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
            binding.Appbar.setNavigationOnClickListener {
                Log.d("Exit", "exit")
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v("Clicked", item.toString())
        return when (item.itemId) {
            R.id.add_doc -> {
                val dialogBinding = NewFolderDialogBinding.inflate(layoutInflater)
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Create New Document").setView(dialogBinding.root).setPositiveButton("" +
                        "Create") {
                        dialog, _ -> run {
                    val newPath = "$path/${dialogBinding.folderName.text}.txt"
                    val file = File(newPath)
                    if (file.exists()) {
                        Toast.makeText(this, "Document exists", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if (file.createNewFile()) {
                            fileList.add(file)
                            adapter.notifyItemInserted(fileList.size - 1)
                            val intent = Intent(this, TextViewActivity::class.java)
                            intent.putExtra("path", file.path)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        else {
                            Toast.makeText(this, "Failed to create folder ${dialogBinding.folderName.text} ", Toast.LENGTH_SHORT).show()
                        }
                        dialog.cancel()
                    }
                }
                }.setNegativeButton("Cancel") {dialog, _ -> dialog.cancel()}
                val dialog = dialogBuilder.create()
                dialog.show()
                true
            }
            R.id.add_folder -> {
                val dialogBinding = NewFolderDialogBinding.inflate(layoutInflater)
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Create New Folder").setView(dialogBinding.root).setPositiveButton("" +
                        "Create") {
                        dialog, _ -> run {
                    val newPath = "$path/${dialogBinding.folderName.text}"
                    Log.v("New Folder", newPath)
                    val folder = File(newPath)
                    if (folder.exists()) {
                        Toast.makeText(this, "Folder exists", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if (folder.mkdir()) {
                            fileList.add(folder)
                            adapter.notifyItemInserted(fileList.size - 1)
                            Toast.makeText(this, "Folder ${dialogBinding.folderName.text} created", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this, "Failed to create folder ${dialogBinding.folderName.text} ", Toast.LENGTH_SHORT).show()
                        }
                        dialog.cancel()
                    }
                }
                }.setNegativeButton("Cancel") {dialog, _ -> dialog.cancel()}
                val dialog = dialogBuilder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.rename -> {
                val dialogBinding = RenameDialogBinding.inflate(layoutInflater)
                val dialogBuilder = AlertDialog.Builder(this)
                val file = fileList[item.groupId]
                dialogBinding.newName.setText(file.name)
                dialogBuilder.setTitle("Rename").setView(dialogBinding.root).setPositiveButton("" +
                        "OK") {
                        dialog, _ -> run {
                    var newName = dialogBinding.newName.text.toString()
                    if (newName.contains('.')) {
                        if (newName.substring(newName.lastIndexOf('.') + 1) == file.extension) {
                            newName = newName.substring(0, newName.lastIndexOf('.'))
                        }
                    }
                    val newPath = "$path/$newName.${file.extension}"
                    adapter.renameItem(item.groupId, newPath)
                }
                }.setNegativeButton("Cancel") {dialog, _ -> dialog.cancel()}
                val dialog = dialogBuilder.create()
                dialog.show()
                true
            }

            R.id.delete -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Delete ${fileList[item.groupId].name}")
                alertDialogBuilder.setPositiveButton("Delete") {
                        dialog, _ ->
                    run {
                        adapter.deleteItem(item.groupId)
                        dialog.cancel()
                    }
                }
                alertDialogBuilder.setNegativeButton("Cancel") {dialog, _ -> dialog.cancel()}
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
                true
            }

            R.id.copy -> {
                val dialogBinding = CopyDialogBinding.inflate(layoutInflater)
                val dialogBuilder = AlertDialog.Builder(this)
                val file = fileList[item.groupId]
                val listAllFolder = arrayListOf<String>()
                var newLocale: String? = null
                Environment.getExternalStorageDirectory().walkTopDown().forEach {
                    if (it.isDirectory) listAllFolder.add(it.path)
                }
                val spinnerAdapter = ArrayAdapter(this, R.layout.spinner, listAllFolder)
                dialogBinding.newLocate.adapter = spinnerAdapter
                dialogBinding.newLocate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        newLocale = listAllFolder[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
                dialogBuilder.setTitle("Copy to").setView(dialogBinding.root).setPositiveButton("" +
                        "OK") {
                        dialog, _ -> run {
                    if (newLocale != null && newLocale != path) {
                        adapter.copyFile(item.groupId, newLocale!!)

                    }
                    else Toast.makeText(this, "Cannot copied!", Toast.LENGTH_SHORT).show()
                }
                }.setNegativeButton("Cancel") {dialog, _ -> dialog.cancel()}
                val dialog = dialogBuilder.create()
                dialog.show()
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }


}