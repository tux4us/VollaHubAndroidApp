package com.volla.hub

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.volla.hub.databinding.ActivityDeviceReportBinding
import java.io.File
import java.io.FileOutputStream
import java.util.*

class DeviceReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceReportBinding
    private val selectedImages = mutableListOf<Uri>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImages.addAll(uris)
        updateImageCount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Geräte-Report"

        displayDeviceSpecs()

        binding.btnAddPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnShare.setOnClickListener {
            shareReport()
        }

        binding.btnSavePdf.setOnClickListener {
            saveAsPdf()
        }

        binding.btnCopySpecs.setOnClickListener {
            copySpecsToClipboard()
        }
    }

    private fun copySpecsToClipboard() {
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Volla Device Specs", binding.tvDeviceSpecs.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Spezifikationen in die Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
    }

    private fun displayDeviceSpecs() {
        val specs = StringBuilder()
        specs.append("HERSTELLER: ${Build.MANUFACTURER}\n")
        specs.append("MODELL: ${Build.MODEL}\n")
        specs.append("GERÄT: ${Build.DEVICE}\n")
        specs.append("BOARD: ${Build.BOARD}\n")
        specs.append("HARDWARE: ${Build.HARDWARE}\n")
        specs.append("ANDROID VERSION: ${Build.VERSION.RELEASE}\n")
        specs.append("SDK VERSION: ${Build.VERSION.SDK_INT}\n")
        specs.append("BUILD ID: ${Build.ID}\n")
        specs.append("FINGERPRINT: ${Build.FINGERPRINT}\n")
        
        binding.tvDeviceSpecs.text = specs.toString()
    }

    private fun updateImageCount() {
        binding.tvPhotoCount.text = "${selectedImages.size} Fotos ausgewählt"
    }

    private fun getFullReportText(): String {
        val title = binding.etNoteTitle.text.toString()
        val note = binding.etNoteContent.text.toString()
        val specs = binding.tvDeviceSpecs.text.toString()

        return "VOLLA GERÄTE REPORT\n\n" +
                "TITEL: $title\n\n" +
                "NOTIZ:\n$note\n\n" +
                "SYSTEM-SPEZIFIKATIONEN:\n$specs"
    }

    private fun shareReport() {
        val reportText = getFullReportText()
        
        if (selectedImages.isEmpty()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Volla Support Report: ${binding.etNoteTitle.text}")
                putExtra(Intent.EXTRA_TEXT, reportText)
            }
            startActivity(Intent.createChooser(intent, "Report teilen via..."))
        } else {
            // Telegram und andere Apps brauchen oft ein klares 'image/*' 
            // oder 'message/rfc822' bei Text+Bild Mix.
            // Wir nutzen 'image/*' als Basis, da Bilder vorhanden sind.
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*" 
                putExtra(Intent.EXTRA_SUBJECT, "Volla Support Report: ${binding.etNoteTitle.text}")
                // WICHTIG: Manche Apps erwarten den Text in EXTRA_STREAM als Datei, 
                // aber die meisten nutzen EXTRA_TEXT als Caption für Bilder.
                putExtra(Intent.EXTRA_TEXT, reportText)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedImages))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Explizit den Typ auf mixed setzen für bessere Kompatibilität
            intent.type = "*/*"
            startActivity(Intent.createChooser(intent, "Report teilen via..."))
        }
    }

    private fun saveAsPdf() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val textPaint = Paint().apply {
            textSize = 12f
        }
        
        // --- SEITE 1: TEXT ---
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 50f
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Volla Geräte Report", 50f, y, paint)
        
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        val lines = getFullReportText().split("\n")
        for (line in lines) {
            if (y > 800) {
                pdfDocument.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText(line, 50f, y, paint)
            y += 20f
        }
        pdfDocument.finishPage(page)

        // --- WEITERE SEITEN: BILDER ---
        for (uri in selectedImages) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Bild skalieren, um auf die Seite zu passen (mit Rand)
                    val maxWidth = 495f // 50f Rand links/rechts
                    val maxHeight = 742f // 50f Rand oben/unten
                    
                    val scale = Math.min(maxWidth / bitmap.width, maxHeight / bitmap.height)
                    val drawWidth = bitmap.width * scale
                    val drawHeight = bitmap.height * scale
                    
                    val left = (595f - drawWidth) / 2
                    val top = (842f - drawHeight) / 2

                    canvas.drawBitmap(bitmap, null, android.graphics.RectF(left, top, left + drawWidth, top + drawHeight), null)
                    
                    // Bildunterschrift (Optional)
                    canvas.drawText("Anhang Bild ${pdfDocument.pages.size}", 50f, 810f, textPaint)
                    
                    pdfDocument.finishPage(page)
                    bitmap.recycle() // Speicher freigeben
                }
            } catch (e: Exception) {
                android.util.Log.e("DeviceReport", "Fehler beim Hinzufügen von Bild zu PDF: ${e.message}")
            }
        }

        val fileName = "VollaReport_${System.currentTimeMillis()}.pdf"
        
        try {
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Volla")
            if (!documentsDir.exists()) documentsDir.mkdirs()
            
            val file = File(documentsDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF gespeichert in: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Fehler beim Speichern der PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}