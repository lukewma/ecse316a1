import java.util.Random;
import java.nio.ByteBuffer;

public class Request {

	private String dom;
	private QType type;

	public Request(String domain, QType type){
		this.type = type;
		this.dom = domain;
	}

	public byte[] getReq(){
		int qLen = getqLen();
		ByteBuffer req = ByteBuffer.allocate(12 + 5 + qLen);
		req.put(createReqH());
		req.put(createQH(qLen));
        return req.array();
	}

	private byte[] createReqH(){
		//as per lab docs, 12 byte offset
		ByteBuffer h = ByteBuffer.allocate(12);
		byte[] randomID = new byte[2]; 
		new Random().nextBytes(randomID);
		h.put(randomID);
		h.put((byte)0x01);
		h.put((byte)0x00);
		h.put((byte)0x00);
		h.put((byte)0x01);
		
		//lines 3, 4, and 5 will be all 0s, which is what we want
		return h.array();
	}
	

	private int getqLen(){
		String[] items = dom.split("\\.");
		int bLen = 0;
		for(int i=0; i < items.length; i ++){
			//1 bLen = 1 char
			//www.mcgill.ca = 3, w, w, w, 6, m, c, g, i, l, l, 2, c, a = 14 bLen
			bLen += items[i].length() + 1;
		}
		return bLen;
	}

	private byte[] createQH(int qLen){
		ByteBuffer q = ByteBuffer.allocate(qLen+5);
		
		//first calculate how many bytes we need so we know the size of the array
		String[] items = dom.split("\\.");
		for(int i=0; i < items.length; i ++){
			q.put((byte) items[i].length());
			for (int j = 0; j < items[i].length(); j++){
				q.put((byte) ((int) items[i].charAt(j)));
				
			}
		}

		q.put((byte) 0x00);

		//Add Query Type
		q.put(hxStr2BytArr("000" + hexByQt(type)));
		q.put((byte) 0x00);
		//Add Query Class - always  0x0001 for internet addresses
		q.put((byte) 0x0001);

		return q.array();
	}
	
	private char hexByQt(QType type){
		if (type == QType.A) {
			return '1';
		} else if (type == QType.NS) {
			return '2';
		} else {
			return 'F';
		}
	}

	private static byte[] hxStr2BytArr(String s) {
		int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
