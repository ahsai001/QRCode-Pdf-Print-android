package com.ahsailabs.qrpdfprintsave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ahsailabs.qrpdfprintsave.databinding.ActivityMainBinding
import android.widget.Toast

import com.journeyapps.barcodescanner.ScanContract

import com.journeyapps.barcodescanner.ScanOptions

import com.journeyapps.barcodescanner.ScanIntentResult
import android.content.pm.PackageManager

import android.Manifest.permission.READ_EXTERNAL_STORAGE

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.graphics.*

import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat

import android.os.Environment

import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 10021

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Scan qrcode of product")
            options.setCameraId(0) // Use a specific camera of the device
            options.captureActivity = ScanCapturePortraitActivity::class.java
            options.setBeepEnabled(true)
            options.setBarcodeImageEnabled(false)
            options.setOrientationLocked(false)
            barcodeLauncher.launch(options)
        }

        binding.btnSave.setOnClickListener {
            if (checkStoragePermission()) {
                generatePDF(binding.etGoodId.text.toString(), binding.tvInfo.text.toString(),binding.spType.selectedItem.toString(), binding.etPrice.text.toString())
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                requestStoragePermission()
            }
        }

        binding.etGoodId.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    //getDetail(binding.etGoodId.text.toString())
                    return true
                }
                return false
            }
        })
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()

            binding.etGoodId.setText(result.contents)
        }
    }

    private fun generatePDF(productId: String, productName: String, paymentType: String, price: String) {
        val pdfDocument = PdfDocument()

        val imagePaint = Paint()
        val textPaint = Paint()

        val firstPage = PageInfo.Builder(792,1120, 1).create()

        val myPage = pdfDocument.startPage(firstPage)
        val canvas: Canvas = myPage.canvas

        val bmp = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false)
        canvas.drawBitmap(scaledbmp, 56f, 40f, imagePaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 15f
        textPaint.color = ContextCompat.getColor(this, R.color.purple_500)
        canvas.drawText("Minimarket Lubang Buaya 2", 209f, 80f, textPaint)
        canvas.drawText("Jakarta Timur, 13810", 209f, 100f, textPaint)
        canvas.drawText("Senyum itu sedekah", 209f, 120f, textPaint)

        textPaint.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(this, R.color.purple_500)
        textPaint.textSize = 15f
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawText("This is a product id of your good you bought :", 396f, 560f, textPaint)
        canvas.drawText("1. $productId $productName : Rp $price", 396f, 580f, textPaint)

        canvas.drawText("Silakan lakukan pembayar ke $paymentType dengan akun 981923986931", 396f, 620f, textPaint)


        pdfDocument.finishPage(myPage)

        val folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
        val fileName = "invoice-ahmad.pdf"
        val file = File(folder, fileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(
                this@MainActivity,
                "Document pdf siap $folder/invoice-ahmad.pdf",
                Toast.LENGTH_SHORT
            ).show()
            //openPdf(file)
            printPdf("$folder/$fileName")

        } catch (e: IOException) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    private fun printPdf(pathPdf: String) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        try{
            val printAdapter = PdfDocumentAdapter(pathPdf);
            printManager.print("Invoice", printAdapter, PrintAttributes.Builder().build());
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun openPdf(file: File) {
        val path: Uri = Uri.fromFile(file)
        val objIntent = Intent(Intent.ACTION_VIEW)
        objIntent.setDataAndType(path, "application/pdf")
        objIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(objIntent)

    }

    private fun checkStoragePermission(): Boolean {
        val writePermission = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    generatePDF(binding.etGoodId.text.toString(), binding.tvInfo.text.toString(),binding.spType.selectedItem.toString(), binding.etPrice.text.toString())
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}