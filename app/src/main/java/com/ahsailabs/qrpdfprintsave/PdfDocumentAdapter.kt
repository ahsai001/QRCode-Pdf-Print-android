package com.ahsailabs.qrpdfprintsave

import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.*


/**
 * Created by ahmad s on 26/02/22.
 */
class PdfDocumentAdapter(private val pathName: String) : PrintDocumentAdapter() {
    override fun onLayout(
        printAttributes: PrintAttributes,
        printAttributes1: PrintAttributes,
        cancellationSignal: CancellationSignal,
        layoutResultCallback: LayoutResultCallback,
        bundle: Bundle?
    ) {
        if (cancellationSignal.isCanceled) {
            layoutResultCallback.onLayoutCancelled()
        } else {
            val builder = PrintDocumentInfo.Builder("file name")
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            layoutResultCallback.onLayoutFinished(
                builder.build(),
                printAttributes1 != printAttributes
            )
        }
    }

    override fun onWrite(
        pageRanges: Array<PageRange?>?,
        parcelFileDescriptor: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        writeResultCallback: WriteResultCallback
    ) {
        var input: InputStream? = null
        var out: OutputStream? = null
        try {
            val file = File(pathName)
            input = FileInputStream(file)
            out = FileOutputStream(parcelFileDescriptor.fileDescriptor)
            val buf = ByteArray(16384)
            var size: Int
            while (input.read(buf).also { size = it } >= 0 && !cancellationSignal.isCanceled) {
                out.write(buf, 0, size)
            }
            if (cancellationSignal.isCanceled) {
                writeResultCallback.onWriteCancelled()
            } else {
                writeResultCallback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (e: Exception) {
            writeResultCallback.onWriteFailed(e.message)
            e.printStackTrace()
        } finally {
            try {
                input?.close()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}