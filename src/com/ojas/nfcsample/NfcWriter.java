package com.ojas.nfcsample;

import java.io.IOException;
import java.nio.charset.Charset;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

public class NfcWriter {
	
	public static boolean supportedTechs(String[] techs) {  
		boolean ultralight=false;  
		boolean nfcA=false;  
		boolean ndef=false;  
		for(String tech:techs) {  
			if(tech.equals("android.nfc.tech.MifareUltralight")) {  
				ultralight=true;  
			}else if(tech.equals("android.nfc.tech.NfcA")) {   
				nfcA=true;  
			} else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {  
				ndef=true;  
			}  
		}  
		if(ultralight && nfcA && ndef) {  
			return true;  
		} else {  
			return false;  
		}  
	} 

	public WriteResponse writeTag(NdefMessage message, Tag tag) {  
		int size = message.toByteArray().length;  
		String mess = "";  
		try {  
			Ndef ndef = Ndef.get(tag);  
			if (ndef != null) {  
				ndef.connect();  
				if (!ndef.isWritable()) {  
					return new WriteResponse(0,"Tag is read-only");  
				}  
				if (ndef.getMaxSize() < size) {  
					mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size  
							+ " bytes.";  
					return new WriteResponse(0,mess);  
				}  
				ndef.writeNdefMessage(message);  
				
				//if(writeProtect) ndef.makeReadOnly();
				
				mess = "Wrote message to pre-formatted tag.";  
				return new WriteResponse(1,mess);  
			} else {  
				NdefFormatable format = NdefFormatable.get(tag);  
				if (format != null) {  
					try {  
						format.connect();  
						format.format(message);  
						mess = "Formatted tag and wrote message";  
						return new WriteResponse(1,mess);  
					} catch (IOException e) {  
						mess = "Failed to format tag.";  
						return new WriteResponse(0,mess);  
					}  
				} else {  
					mess = "Tag doesn't support NDEF.";  
					return new WriteResponse(0,mess);  
				}  
			}  
		} catch (Exception e) {  
			mess = "Failed to write tag";  
			return new WriteResponse(0,mess);  
		}  
	} 
	
	private class WriteResponse {  
		int status;  
		String message;  
		WriteResponse(int Status, String Message) {  
			this.status = Status;  
			this.message = Message;  
		}  
		public int getStatus() {  
			return status;  
		}  
		public String getMessage() {  
			return message;  
		}  
	} 
	
	public static NdefMessage getTagAsNdef(String message) {  
		boolean addAAR = false;  
		String uniqueId = message; // "smartwhere.com/nfc.html";      
		byte[] uriField = uniqueId.getBytes(Charset.forName("US-ASCII"));  
		byte[] payload = new byte[uriField.length + 1];       //add 1 for the URI Prefix  
		payload[0] = 0x01;                        //prefixes http://www. to the URI  
		System.arraycopy(uriField, 0, payload, 1, uriField.length); //appends URI to payload  
		NdefRecord rtdUriRecord = new NdefRecord(  
				NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);  
		if(addAAR) {  
			// note: returns AAR for different app (nfcreadtag)  
			return new NdefMessage(new NdefRecord[] {  
					rtdUriRecord, NdefRecord.createApplicationRecord("com.ojas.nfcsample")  
			});   
		} else {  
			return new NdefMessage(new NdefRecord[] {  
					rtdUriRecord});  
		}  
	} 
	
}
