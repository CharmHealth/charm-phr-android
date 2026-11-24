package com.charm.phr.mphr.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.charm.phr.mphr.R
import com.rajat.pdfviewer.PdfRendererView
import java.io.File

object PdfUtil {

    fun initPdfViewer(
        pdfView: PdfRendererView,
        file: File,
        context: Context,
        onError: (() -> Unit)? = null
    ): Boolean {
        return try {
            pdfView.initWithFile(file)
            true
        } catch (e: SecurityException) {
            handlePasswordProtectedPdf(context, file)
            onError?.invoke()
            false
        } catch (e: Exception) {
            Log.d("PdfUtil", "Error loading PDF: ${e.message}", e)
            showPdfErrorDialog(context, context.getString(R.string.error_loading_pdf))
            onError?.invoke()
            false
        }
    }

    /**
     * Checks if a PDF viewer app is installed on the device
     */
    private fun isPdfAppInstalled(context: Context, fileUri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val packageManager = context.packageManager
        return intent.resolveActivity(packageManager) != null
    }

    /**
     * Handles password-protected PDF by asking user if they want to open externally
     * Shows dialog, and if user confirms, opens in external app and closes activity when they return
     */
    private fun handlePasswordProtectedPdf(context: Context, file: File) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                context.getString(R.string.content_provider_authority),
                file
            )

            val hasPdfApp = isPdfAppInstalled(context, fileUri)

            if (hasPdfApp) {
                // Show confirmation dialog before opening externally
                AlertDialog.Builder(context)
                    .setTitle(R.string.password_protected_pdf_title)
                    .setMessage(R.string.password_protected_pdf_message_with_app)
                    .setPositiveButton(R.string.open_externally) { dialog, _ ->
                        dialog.dismiss()
                        // Open in external app
                        openPdfExternally(context, fileUri)

                        // Close the activity so when user comes back, they see the previous screen
                        if (context is Activity) {
                            context.finish()
                        }
                    }
                    .setNegativeButton(R.string.cancel_str) { dialog, _ ->
                        dialog.dismiss()
                        // User cancelled - close the activity
                        if (context is Activity) {
                            context.finish()
                        }
                    }
                    .setOnCancelListener {
                        // Dialog dismissed - close the activity
                        if (context is Activity) {
                            context.finish()
                        }
                    }
                    .setCancelable(true)
                    .show()
            } else {
                // Show message that no PDF app is installed
                AlertDialog.Builder(context)
                    .setTitle(R.string.password_protected_pdf_title)
                    .setMessage(R.string.password_protected_pdf_message_no_app)
                    .setPositiveButton(R.string.ok_str) { dialog, _ ->
                        dialog.dismiss()
                        // Close the activity
                        if (context is Activity) {
                            context.finish()
                        }
                    }
                    .setOnCancelListener {
                        // Dialog dismissed - close the activity
                        if (context is Activity) {
                            context.finish()
                        }
                    }
                    .setCancelable(true)
                    .show()
            }
        } catch (e: Exception) {
            Log.d("PdfUtil", "Error handling password-protected PDF: ${e.message}", e)
            UIUtil.showToast(context, R.string.password_protected_pdf_error, android.widget.Toast.LENGTH_SHORT)

            // Close the activity on error
            if (context is Activity) {
                context.finish()
            }
        }
    }

    /**
     * Opens PDF in an external app
     */
    private fun openPdfExternally(context: Context, fileUri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_pdf_with)))
        } catch (e: Exception) {
            Log.d("PdfUtil", "Error opening PDF externally: ${e.message}", e)
            UIUtil.showToast(context, R.string.no_pdf_app_available, android.widget.Toast.LENGTH_SHORT)
        }
    }

    /**
     * Shows a generic error dialog for PDF loading issues
     */
    private fun showPdfErrorDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle(R.string.error_loading_pdf_title)
            .setMessage(message)
            .setPositiveButton(R.string.ok_str) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}
