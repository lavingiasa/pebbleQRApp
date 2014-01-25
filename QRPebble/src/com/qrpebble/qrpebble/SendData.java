package com.qrpebble.qrpebble;

import android.net.Uri;
import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.EnumMap;
import java.util.UUID;

public class SendData extends Activity {

	private static final UUID QR_UUID = UUID.fromString(("107f875b-50cf-4bf7-a90c-0caebea8bcb2"));
	private static final int QR_DATA = 0;
	private static final int LAST_ITEM = 1;
	
	TextView textLocation;
	Button sendStuff;
	EditText editableText;
	ImageView mImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sendStuff = (Button)findViewById(R.id.sendText);
		textLocation = (TextView)findViewById(R.id.whereTheTextGoes);
		editableText = (EditText)findViewById(R.id.editText1);
		mImageView = (ImageView)findViewById(R.id.mImageView);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_data, menu);
		return true;
	}
	
	public void sendTheStuff(View v)
	{
		EditText test = (EditText)findViewById(R.id.editText1);
		TextView textBox = (TextView)findViewById(R.id.whereTheTextGoes);
		
		String value = test.getText().toString();
		//textBox.setText(value);
		
		try {
			
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
			startActivityForResult(intent, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "ERROR:" + e, 1).show();

		}
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		TextView textBox = (TextView)findViewById(R.id.whereTheTextGoes);
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				//tvStatus.setText(intent.getStringExtra("SCAN_RESULT_FORMAT"));
//				Log.w("myApp", intent.getStringExtra("SCAN_RESULT_BYTES" + " "));
				textBox.setText(intent.getStringExtra("SCAN_RESULT"));
//				generateQRCode(intent.getStringExtra("SCAN_RESULT"));
				encode(intent.getStringExtra("SCAN_RESULT"));
				
		
			} else if (resultCode == RESULT_CANCELED) {
				//tvStatus.setText("Press a button to start a scan.");
				textBox.setText("Scan cancelled.");
			}
		}
	}

	private void generateQRCode(String data) {
	    com.google.zxing.Writer writer = new QRCodeWriter();
	    mImageView = (ImageView)findViewById(R.id.mImageView);
	    String finaldata =Uri.encode(data, "ISO-8859-1");
	    int qr_size = 105;
	    Bitmap mBitmap = null;
	    try {
	        BitMatrix bm = writer.encode(finaldata,BarcodeFormat.QR_CODE, qr_size, qr_size);
	        mBitmap = Bitmap.createBitmap(qr_size, qr_size, Config.ARGB_8888);
	        for (int i = 0; i < qr_size; i++) {
	            for (int j = 0; j < qr_size; j++) {
	                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
	            }
	        }
	        
	        Log.w("myApp", "got through the bitmap");
	    } catch (WriterException e) {
	        e.printStackTrace();
	    }
	    if (mBitmap != null) {
	        mImageView.setImageBitmap(mBitmap);
	        
		
	    }
	}
	
	private void encode(String uniqueID) {
        // TODO Auto-generated method stub
         BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;
         
            int width0 = 150;
            int height0 = 150;

            int colorBack = 0xFF000000;
            int colorFront = 0xFFFFFFFF;

            QRCodeWriter writer = new QRCodeWriter();
            try
            {
                EnumMap<EncodeHintType, Object> hint = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
                hint.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                BitMatrix bitMatrix = writer.encode(uniqueID, barcodeFormat, width0, height0, hint);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++)
                {
                    int offset = y * width;
                    for (int x = 0; x < width; x++)
                    {

                        pixels[offset + x] = bitMatrix.get(x, y) ? colorBack : colorFront;
                    }
                }

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                ImageView imageview = (ImageView)findViewById(R.id.mImageView);
                imageview.setImageBitmap(bitmap);
                
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                int numTimes = byteArray.length/32;
                int remainder = byteArray.length%32;
                
                for(int i = 0; i < numTimes; i++)
                {
		        	PebbleDictionary whileData = new PebbleDictionary();
		        	byte[] oneUnit = new byte[32];
		        	for(int j = 0 ; j < 32; j ++)
		        	{
		        		oneUnit[j] = byteArray[32*i+j];
		        	
		        	}
		        	whileData.addBytes(QR_DATA, oneUnit);
		        	whileData.addUint8(LAST_ITEM, (byte)0);
					PebbleKit.sendDataToPebble(getApplicationContext(), QR_UUID, whileData);
					
					 synchronized(Thread.currentThread()) { //added
						 try {
						            Thread.currentThread().wait(3000);
						        } catch (InterruptedException e) {
						            Log.e("Attractivometer","Main Thread interrupted while waiting");
						            e.printStackTrace();
						        }
						 }

                }
                
                PebbleDictionary lastData = new PebbleDictionary();
                byte[] oneUnit = new byte[remainder];
	        	
                for(int j = 0 ; j < remainder; j ++)
	        	{
	        		oneUnit[j] = byteArray[32*numTimes+j];
	        	}
                
                lastData.addBytes(QR_DATA, oneUnit);
                lastData.addUint8(LAST_ITEM, (byte)1);
				PebbleKit.sendDataToPebble(getApplicationContext(), QR_UUID, lastData);
				
                
				Log.w("myApp", Integer.toString(byteArray.length));
				Log.w("myApp", byteArray.toString());
				
				Toast.makeText(getApplicationContext(), Integer.toString(byteArray.length), 
						   Toast.LENGTH_LONG).show();

            } catch (WriterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
	
}
