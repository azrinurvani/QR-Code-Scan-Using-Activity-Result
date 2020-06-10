package com.mobile.azrinurvani.qrcodescannerusingactiviyresults

import android.Manifest
import android.R.attr.name
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_scan_page.*
import kotlinx.android.synthetic.main.alert_failed.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class ScanPageActivity : AppCompatActivity() {
    private val requestPermissionCode = 1111

    var barcodeDetector : BarcodeDetector? = null
    var cameraSource : CameraSource? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_page)

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this,barcodeDetector)
            .setRequestedPreviewSize(641,481)
            .setAutoFocusEnabled(true)
            .build()

        cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource?.stop()
            }

            override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
                if (ActivityCompat.checkSelfPermission(
                        this@ScanPageActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this@ScanPageActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        requestPermissionCode
                    )
                    return
                }
                try{
                    cameraSource?.start(cameraSurfaceView.holder)
                }catch (e : Exception){
                    Log.v("ADX","ScanPageActivity.surfaceCreated exception : ${e.localizedMessage}" )
                }
            }

        })

        barcodeDetector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                if ((detections!= null) && (detections.detectedItems != null)){

                    val qrDetect : SparseArray<Barcode> = detections?.detectedItems
                    if (qrDetect?.size()!=null){

                        val code = qrDetect?.valueAt(0)
//                            txtResult.text = code?.displayValue
                        val resultScan = code?.displayValue

                        val p = Pattern.compile(
                            "[^a-z0-9 ]",
                            Pattern.CASE_INSENSITIVE
                        )
                        val m: Matcher = p.matcher(resultScan)
                        val containsSymbol = m.find()

                        val space = Pattern.compile("\\s+")
                        val matcherSpace = space.matcher(resultScan.toString())
                        val containsSpace = matcherSpace.find()

                        if (containsSpace==true||containsSymbol==true) {
                            alertValueContainsSymbol()
                            Log.v("ADX","Contains Symbol and Space")
                        }

                        barcodeDetector?.release()
                        val index= intent.getIntExtra("index",0)
                        val returnIntent = Intent()
                        returnIntent.putExtra("value",resultScan)
                        Log.d("ADX","ScanPage value : "+resultScan + " Index : "+resultScan)
                        setResult(index,returnIntent)
                        finish()
//                        }
                    }
                }

            }

        })

    }

    private fun alertValueContainsSymbol(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_failed, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(mDialogView)

        val alertDialog = builder.show()
        alertDialog.setCanceledOnTouchOutside(false)

        mDialogView.btnDissmisAlert.setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(applicationContext,MainActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            requestPermissionCode -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }

                    try {
                        cameraSource?.start(cameraSurfaceView.holder)
                    } catch (e: Exception) {
                        Log.e("ADX","RequestPermissionsResult Error : ${e.localizedMessage}")
                    }
                }
            }
        }
    }
}
