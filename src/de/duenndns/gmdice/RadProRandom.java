package de.duenndns.gmdice;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.util.Random;

public class RadProRandom extends Random {
	private UsbSerialPort port;
	public RadProRandom(UsbSerialPort _port) {
		port = _port;
	}

	// store the bytes received from USB until one line is complete
	StringBuffer lineBuffer = new StringBuffer(512);
	public String readUsbLine() throws IOException {
		while (true) {
			// first check if we already have a full line in the buffer
			int crlf = lineBuffer.indexOf("\r\n");
			if (crlf >= 0) {
				String line = lineBuffer.substring(0, crlf);
				lineBuffer.delete(0, crlf+2);
				return line;
			}
			// read more bytes, convert to UTF-8, append to lineBuffer
			byte[] buffer = new byte[64];
			int len = port.read(buffer, 200);
			String ascii = new String(buffer, 0, len, "UTF-8");
			lineBuffer.append(ascii);
		}
	}

	// store random bits received from USB for later use
	StringBuffer randomBits = new StringBuffer(32);
	@Override
	protected int next(int bits) {
		try {
			int got_bits = 0;
			int bit_data = 0;
			while (true) {
				while (randomBits.length() > 0) {
					bit_data = bit_data * 16 + Character.digit(randomBits.charAt(0), 16);
					got_bits += 4;
					if (got_bits >= bits)
						return bit_data;
					randomBits.deleteCharAt(0);
				}
				port.write("GET randomData\r\n".getBytes(), 2000);
				String response = readUsbLine();
				Log.d("RadProRandom: ", response);
				if (response.startsWith("OK ")) {
					randomBits.append(response.substring(3));
				} else {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
