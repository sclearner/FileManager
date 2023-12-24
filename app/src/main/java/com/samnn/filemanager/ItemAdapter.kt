package com.samnn.filemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.samnn.filemanager.databinding.ItemShowBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class ItemAdapter(private val context: Context, private val items: ArrayList<File>) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemShowBinding.inflate(inflater, parent, false)
        return when (viewType) {
            KindOfType.DIRECTORY ->  DirectoryHolder(itemBinding, context, inflater)
            KindOfType.FILE -> FileHolder(itemBinding, context, inflater)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val file = items[position]
        return if (file.isDirectory) KindOfType.DIRECTORY
        else KindOfType.FILE
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, context)
    }


    fun renameItem(position: Int, newName: String) {
        val file = items[position]
        val oldName = file.name
        Log.v("Old", file.absolutePath)
        Log.v("Old", newName)
        val newFile = File(newName)
        val result = file.renameTo(newFile)
        if (result) {
            items[position] = newFile
            Toast.makeText(context, "Item renamed from $oldName to ${file.name}", Toast.LENGTH_SHORT).show()
            notifyItemChanged(position)
        }
        else {
            Toast.makeText(context, "Item failed to rename", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteItem(position: Int) {
        val file = items[position]
        if (file.delete()) {
            items.remove(file)
            notifyItemRemoved(position)
            Toast.makeText(context, "${file.name} successfully deleted", Toast.LENGTH_SHORT)
                .show()
        } else Toast.makeText(context, "Cannot delete", Toast.LENGTH_LONG).show()
    }

    fun copyFile(position: Int, newLocale: String) {
        val file = items[position]
        if (file.copyTo(File("$newLocale/${file.name}")).exists()) {
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        }
        else Toast.makeText(context, "Cannot copy!", Toast.LENGTH_SHORT).show()
    }


    abstract class ViewHolder(val itemBinding: ItemShowBinding, val context: Context, val inflater: LayoutInflater) :
        RecyclerView.ViewHolder(itemBinding.root) {
            abstract fun bind(item: File, context: Context)
        }

    class DirectoryHolder(itemBinding: ItemShowBinding, context: Context, inflater: LayoutInflater) :
        ViewHolder(itemBinding, context, inflater), OnCreateContextMenuListener{
        @SuppressLint("UsableSpace")
        override fun bind(item: File, context: Context) {
            itemBinding.itemType.setImageResource(R.drawable.folder)
            itemBinding.itemName.text = item.name
            itemBinding.itemModifiedDate.text = ""

            itemBinding.item.setOnClickListener {
                goToFileManager(item)
            }

            itemBinding.item.setOnCreateContextMenuListener(this)
        }

        private fun goToFileManager(item: File) {
            val intent = Intent(context, FileExploreActivity::class.java)
            val path = item.absolutePath
            intent.putExtra("path", path)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(this.adapterPosition, R.id.rename, 0, "Rename")
            menu?.add(this.adapterPosition, R.id.delete, 1, "Delete")
        }
    }

    class FileHolder(itemBinding: ItemShowBinding, context: Context, inflater: LayoutInflater) :
        ViewHolder(itemBinding, context, inflater), OnCreateContextMenuListener {
        @SuppressLint("UsableSpace")
        override fun bind(item: File, context: Context) {
            itemBinding.itemType.setImageResource(
                if (item.isDirectory) R.drawable.folder
                else R.drawable.document
            )
            itemBinding.itemName.text = item.name
            itemBinding.itemModifiedDate.text = "Modifided: " + SimpleDateFormat.getDateTimeInstance().format(Date(item.lastModified()))

            itemBinding.item.setOnClickListener {
                if (item.isDocument) {
                    openDoc(item)
                } else if (item.isImage) {
                    openImage(item)
                } else {
                    Toast.makeText(context, "Wait", Toast.LENGTH_SHORT).show()
                }
            }

            itemBinding.item.setOnCreateContextMenuListener(this)
        }

        private val File.isDocument: Boolean
            get() = extension in listOf<String>("txt")

        private val File.isImage: Boolean
            get() = extension in listOf<String>("bmp", "jpg", "jpeg", "png", "gif", "ico", "tiff")

        private fun openDoc(file: File) {
            val intent = Intent(context, TextViewActivity::class.java)
            val path = file.absolutePath
            intent.putExtra("path", path)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        private fun openImage(item: File) {
            val intent = Intent(context, MediaViewActivity::class.java)
            val path = item.absolutePath
            intent.putExtra("path", path)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu?.add(this.adapterPosition, R.id.copy, 0, "Copy to...")
            menu?.add(this.adapterPosition, R.id.rename, 1, "Rename")
            menu?.add(this.adapterPosition, R.id.delete, 2, "Delete")
        }
    }

}