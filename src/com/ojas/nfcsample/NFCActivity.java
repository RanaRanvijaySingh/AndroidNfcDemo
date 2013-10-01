package com.ojas.nfcsample;

/**
 *  	NdefMessage is the most generic way of exchanging NFC data. You can still define your own non-NDEF data format 
 *  but that is beyond the scope of this tutorial. To illustrate how data is parsed and handled by the tag dispatch system, 
 *  we only focus on the simple plain text type in our example. For other data types, you should check into 
 *  the official site for the complete listing.
 *  
 *  	NfcAdapter is used to check the device NFC support. We use the option enableForegroundDispatch to indicate the 
 *  tag dispatch is handled when the app is running on the foreground. The foreground dispatch system allows an activity 
 *  to intercept an intent and claim priority over other activities that handle the same intent.
 *  
 *  	In onNewIntent(), we try to parse all NDEF messages and their records. Since there are several different data types, 
 *  the example only tries to parse the pain text type as defined inNdefRecord.RTD_TEXT.
 */

import java.util.Arrays;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

public class NFCActivity extends Activity {

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mNFCTechLists;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);
		
		mTextView = (TextView)findViewById(R.id.tv);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
        if (mNfcAdapter != null) {
            mTextView.setText("Read an NFC tag");
        } else {
            mTextView.setText("This phone is not NFC enabled.");
        }
 
        // create an intent with tag data and deliver to this activity
        mPendingIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
        // set an intent filter for all MIME data
        IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefIntent.addDataType("*/*");
            mIntentFilters = new IntentFilter[] { ndefIntent };
        } catch (Exception e) {
            Log.e("TagDispatch", e.toString());
        }
 
        mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };
    
	}

	@Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 
        String strAction = action + "\n\n" + tag.toString();
 
        // parse through all NDEF messages and their records and pick text type only
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            try {
                for (int i = 0; i < data.length; i++) {
                    NdefRecord [] recs = ((NdefMessage)data[i]).getRecords();
                    for (int j = 0; j < recs.length; j++) {
                        if (recs[j].getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                            Arrays.equals(recs[j].getType(), NdefRecord.RTD_TEXT)) {
                            byte[] payload = recs[j].getPayload();
                            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                            int langCodeLen = payload[0] & 0077;
 
                            strAction += ("\n\nNdefMessage[" + i + "], NdefRecord[" + j + "]:\n\"" +
                                 new String(payload, langCodeLen + 1, payload.length - langCodeLen - 1,
                                 textEncoding) + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }
        }
 
        Log.d("NFCDEMO", " MESSAGE >>>> "+ strAction);
        mTextView.setText(strAction);
    }
 
    @Override
    public void onResume() {
        super.onResume();
 
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);
    }
 
    @Override
    public void onPause() {
        super.onPause();
 
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}
