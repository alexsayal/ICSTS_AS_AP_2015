package main;

public class BufferTwo {

	private static byte[] buff;
	private static int sizeBuffer;
	private static int beginIndex;
	private static int endIndex;
	private static int writeIndex;

	public BufferTwo(int size) {
		sizeBuffer = size;
		buff = new byte[size];
		beginIndex = 0;
		endIndex = 0;
		writeIndex = 0;
	}

	/**
	 * Add bytes to Buffer , Define read begin 
	 * @param array
	 */	
	public void addBytes(byte[] array){
		for(int i=0;i<array.length;i++){
			buff[(writeIndex)%sizeBuffer] = array[i];
			writeIndex+=1;
		}	
	}

	/**
	 * Finds the first 1B in the buffer
	 */
	public void findbegin(){
		for(int i=0;i<buff.length-1;i++){
			if(buff[i] == (byte)0x1B && buff[i+1] != (byte)0xFF){
				beginIndex = i;
				break;
			}
		}	
	}


	/**
	 * Check if message is complete: Has got at least the number of bytes defined in comp
	 * @return boolean complete
	 */
	public boolean checkMessage(){

		boolean complete = true;

		int comp = CMSInterface.byte2toInt(buff[beginIndex+2], buff[beginIndex+1]);

		if(((writeIndex+sizeBuffer-beginIndex)%sizeBuffer) < comp+1){
			complete = false;
		}
		else{
			endIndex = beginIndex + comp; //endIndex e "virtual"

			for(int ii=beginIndex ; ii<endIndex; ii++){
				int ii_real = ii % sizeBuffer;
				if(buff[ii_real] == (byte)0x1B && buff[ii_real+1]==(byte)0xFF){ //If the message contains 1BFF, it might not be complete
					complete = false;
					break;
				}			
			}

			if((writeIndex+sizeBuffer-endIndex)%sizeBuffer > 0 && !complete){ //Check if the buffer has more bytes to read.
				for(int j=endIndex+1 ; j<(writeIndex+sizeBuffer-endIndex)%sizeBuffer-1 ;j++){ 
					int j_real = j % sizeBuffer;
					if(buff[j_real]==(byte)0x1B && buff[j_real+1]!=(byte)0xFF){ //If a start 0x1B is found, then the message ends before that.
						endIndex = j_real - 1 + sizeBuffer;
						complete = true;
						break;
					}
				}
			}
		}

		return complete;
	}

	public static byte[] getBuff() {
		return buff;
	}

	public static void setBuff(byte[] buff) {
		BufferTwo.buff = buff;
	}

	/**
	 * Export message in byte array and delete it from buffer.
	 * @return byte[] message
	 */
	public byte[] exportMessage(){
		byte[] message = new byte[ endIndex + 1 - beginIndex];
				
		for(int ii=beginIndex; ii<=endIndex;ii++){
			message[ii-beginIndex] = buff[ii%sizeBuffer];
		}
		
		beginIndex = endIndex+1;
		
		return message;
	}


}