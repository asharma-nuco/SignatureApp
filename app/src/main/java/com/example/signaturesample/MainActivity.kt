package com.example.signaturesample
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    val TAG = "MainAct"
    val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Can be used to listen to certain events on the Signature Pad, to track when the
        //user started writing, and finished writing.
        signature_pad.setOnSignedListener (object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
            }

            override fun onClear() {
            }

            override fun onSigned() {
            }

        })

        //When the user clicks on "Done" button, we have to save the file to the storage.
        //Check if we have permission to store the file, if yes, store it. Else ask for the permission.
        btDone.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

            saveFileIfGranted(permission)
        }

        //Clear the pad when the user clicks on "Clear" button
        btClear.setOnClickListener {
            signature_pad.clear()
        }
    }

    //Ask for permission
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
    }

    //Capture results of storage permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Please provide permission to save image.")
                }
            }
        }
    }

    //Save the file if we have permission, otherwise ask for permission.
    private fun saveFileIfGranted(isGranted: Int)
    {
        if (isGranted != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please provide permission.", Toast.LENGTH_SHORT).show()
            makeRequest()
        } else {
            val rootPath = Environment.getExternalStorageDirectory().absolutePath + "/signFiles"
            val myDir = File(rootPath)
            if(!myDir.exists()){
                myDir.mkdirs()
            }

            val fileName : String = "image_${System.currentTimeMillis()}.jpg"

            val file = File(myDir, fileName)
            file.parentFile?.mkdir()
            file.createNewFile()

            //Extract the signature from the pad and save it as a bitmap
            val bitmap = signature_pad.signatureBitmap
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, bos)

            val bitmapData = bos.toByteArray()

            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()

            Toast.makeText(this, "File saved at ${file.path}", Toast.LENGTH_SHORT).show()
        }
    }
}