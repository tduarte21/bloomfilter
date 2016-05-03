import java.util.Scanner;
import java.lang.Math;
import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
//import com.google.common.collect.*;

public class TAP_TP2_BloomFilter{

	private static final int N_HASHES = 5;
	//private static final String[] cities = {"Porto", "Aveiro", "Coimbra", "Lisboa", "Algarve", "Braga"};
	private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final int RANDOM_STRING_LENGTH = 40;

	public static void main(String[] args){

		System.out.println("\nStarting...");
		Scanner scan = new Scanner(System.in);

		int input;

		do{	
			System.out.println();
			System.out.println(" ---                   Bloom Filters                   --- ");
			System.out.println();
			System.out.println("  Please choose one option:");
			System.out.println("   1) Cities list (file)");
			System.out.println("   2) Random strings (K=3) (N=8000)");
			System.out.println("   3) Random strings (K=1 to 15) (N=8000)");
			System.out.println("   4) Random strings (K=1 to 15) (N=1000,4000,8000,16000) ");
			System.out.println("   0) Exit");
			System.out.println(" --------------------------------------------------------- ");

			System.out.print("Option: ");
			
			input = scan.nextInt();	

			if (input==1) {

				System.out.println("\n - Loading cities file - ");
				byte[] file = loadFile("cities-portugal.csv");
				String[] cities = new String(file).split("\n");
				HashSet<String> strSet = new HashSet<String>();
				for(String city: cities){
					strSet.add(city);
				}
				//File Source: http://www.downloadexcelfiles.com/wo_en/download-excel-file-list-cities-portugal#.Vx9LwzeDGko
				System.out.println("\nInitializing Bloom Filter... (K=5)");
				System.out.print("Adding half of the cities to Boom Filter...");
				
				int halfCities = strSet.size()/2;

				String[] citiesToAdd = new String[halfCities];
				String[] citiesNotToAdd = new String[halfCities];
				int[] truePositiveCount = new int[halfCities];
				int[] falsePositiveCount = new int[halfCities];

				Iterator it = strSet.iterator();

				int count=0; //Add half of the cities to BitSet and array
				while(count<halfCities){
					String str = (String)it.next();
					citiesToAdd[count] = str;
					count++;
				}
				count=0; //Remaining half to array
				while(it.hasNext()){
					citiesNotToAdd[count] = (String)it.next();
					count++;
				}		
				
				for (int i=1; i<9; i++) {
					
					int bfSize = strSet.size()*i;
					BloomFilter bf = new BloomFilter(bfSize, N_HASHES);
					
					for(String str : citiesToAdd){
						byte[] cityBytes = str.getBytes();
						bf.add(cityBytes);
					}
					
					for (int j=0; j<citiesToAdd.length; j++) {
						String str = citiesToAdd[j];
						byte[] cityBytes = str.getBytes();
						if (bf.contains(cityBytes)) {
							truePositiveCount[i-1]++;
						}
					}

					for (int j=0; j<citiesNotToAdd.length; j++) {
						String str = citiesNotToAdd[j];
						byte[] cityBytes = str.getBytes();
						if (bf.contains(cityBytes)) {
							falsePositiveCount[i-1]++;
						}
					}
				}

				System.out.println(" Done!");

				System.out.println("\n - True positives -");
				for (int i=1; i<9; i++) {
					int bfSize = strSet.size()*i;
					double error = truePositiveCount[i-1]/(double)citiesToAdd.length;
					System.out.printf("Bloom Filter Size = %4d -> %3d/%d | %5.2f%%\n", bfSize, truePositiveCount[i-1], citiesToAdd.length, error*100);
				}

				System.out.println("\n - False positives -");
				for (int i=1; i<9; i++) {
					int bfSize = strSet.size()*i;
					double error = falsePositiveCount[i-1]/(double)citiesNotToAdd.length;
					System.out.printf("Bloom Filter Size = %4d -> %3d/%d | %5.2f%%\n", bfSize, falsePositiveCount[i-1], citiesNotToAdd.length, error*100);
				}		
			}
				
			/*}
			else if(input==2){

				System.out.print("Word: ");
				String newElement = scan.next();
				bf.add(newElement.getBytes());
				
			}
			else if(input==3){

				System.out.print("Word: ");
				String element = scan.next();
				if (bf.contains(element.getBytes())) {
					System.out.println("Set CONTAINS the word " + new String(element.getBytes()) + ".");
				}else {
					System.out.println("Set does NOT contain the word " + new String(element.getBytes()) + ".");
				}

				for(String city : cities){
					if (bf.contains(city.getBytes())) {
					System.out.println("Set CONTAINS the word " + city);
				}else {
					System.out.println("Set does NOT contain the word " + city);
				}
				}
			}*/
			else if(input==2){

				int addSize=10000;
				int testSize=10000;
				int bfSize=addSize*8;

				System.out.println("\n - True positives -");

				int result = testBloomFilter(addSize,bfSize,3);
				double error = result/(double)addSize;
				System.out.printf("K=%02d -> %5d/%d | %5.2f%%\n", 3, result, addSize, error*100);

				System.out.println("\n - False positives -");
				result = testBloomFilter(addSize,testSize,bfSize,3);
				error = result/(double)testSize;
				System.out.printf("K=%02d -> %5d/%d | %5.2f%%\n", 3, result, testSize, error*100);
				

			}
			else if(input==3){

				int addSize=1000;
				int testSize=10000;
				int bfSize=addSize*8;

				System.out.println("\n - False positives -");

				for (int k=1; k<=15; k++){
					int result = testBloomFilter(addSize,testSize,bfSize,k);
					double error = result/(double)testSize;
					System.out.printf("K=%02d -> %5d/%d | %5.2f%%\n", k, result, testSize, error*100);
				}
			}
			else if(input==4){

				int[] bfSizeArray = {1000,4000,8000,16000}; 

				for (int i=0; i<bfSizeArray.length; i++) {

					int addSize=1000;
					int testSize=10000;
					int bfSize=bfSizeArray[i];

					System.out.println("\n - False positives with N=" + bfSize + " -");

					for (int k=1; k<=15; k++){
						int result = testBloomFilter(addSize,testSize,bfSize,k);
						double error = result/(double)testSize;
						System.out.printf("K=%02d -> %5d/%d | %5.2f%%\n", k, result, testSize, error*100);
					}
				}
			}
		}while(input!=0);
	}

	private static byte[] loadFile(String fileName){

		System.out.print("Loading file: " + fileName + "...");
		byte fileByteArray[] = null;
		
		try{
			int fileLength = (int) new File(fileName).length();
		    MappedByteBuffer file = new FileInputStream(fileName).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, fileLength);
		    int i = 0;
			fileByteArray = new byte[fileLength];
			file.get(fileByteArray, 0, fileByteArray.length);
			//System.out.println(fileLength + " chars loaded to memory! ");
		}
		catch (IOException ex){
			System.out.println("ERROR: " + ex);
		}
		System.out.println("Loaded!");
		
		return fileByteArray;
	}

	private static String generateRandomString(){
		Random rand = new Random();
		StringBuilder sb = new StringBuilder();
		int randInt = 0;

		for (int i=0; i<RANDOM_STRING_LENGTH; i++) {
			randInt = rand.nextInt(CHAR_LIST.length());
			char ch = CHAR_LIST.charAt(randInt);
			sb.append(ch);
		}

		return sb.toString();
	}

	private static String[] genStringArray(int size){
		String[] randStrs = new String[size];

		for (int i=0; i<size; i++) {
			randStrs[i] = generateRandomString();
		}

		return randStrs;
	}


	private static int testBloomFilter(int addSize, int testSize, int bfSize, int k){

		String[] strToAdd = genStringArray(addSize);
		String[] strToTest = genStringArray(testSize);
		BloomFilter bf = new BloomFilter(bfSize, k);
		int count = 0;
		for (String str : strToAdd) {
			bf.add(str.getBytes());
		}
		for (String str : strToTest) {
			byte[] cityBytes = str.getBytes();
			if (bf.contains(cityBytes))
				count++;
		}
		return count;
	}

	private static int testBloomFilter(int addSize, int bfSize, int k){

		String[] strToAdd = genStringArray(addSize);
		BloomFilter bf = new BloomFilter(bfSize, k);
		int count = 0;
		for (String str : strToAdd) {
			bf.add(str.getBytes());
		}
		for (String str : strToAdd) {
			byte[] cityBytes = str.getBytes();
			if (bf.contains(cityBytes))
				count++;
		}
		return count;
	}

}

class BloomFilter{

	private int bitSetSize;
	private int k;
	private BitSet bitset;
	private int elementCount;
	private static final int[] primesList = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};

	//eSize - Element size
	//int k - Number of Hash functions used
	public BloomFilter(int size, int k){
		this.bitSetSize = size;
		this.bitset = new BitSet(bitSetSize);
		this.k = k;
		this.elementCount = 0;
	}

	public void add(byte[] element){
		for (int i=0; i<k; i++) {
			int hash = MurmurHash.hash(element, primesList[i]);
			setBitSet(hash);
		}
		elementCount++;
	}

	private void setBitSet(int hash){
		bitset.set(getIndex(hash), true);
	}

	private int getIndex(int hash) {
		return Math.abs(hash % bitSetSize);
	}

	public int getElementCount(){
		return elementCount;
	}
	public boolean contains(byte[] element){
		for (int i=0; i<k; i++) {
			int hash = MurmurHash.hash(element,primesList[i]);
			if(!bitset.get(getIndex(hash))) {
				return false;
			}
		}
		return true;
	}
}

/**
 * This is a very fast, non-cryptographic hash suitable for general hash-based
 * lookup.  See http://murmurhash.googlepages.com/ for more details.
 * 
 * <p>The C version of MurmurHash 2.0 found at that site was ported
 * to Java by Andrzej Bialecki (ab at getopt org).</p>
 */
class MurmurHash {
  	public static int hash(byte[] data, int seed) {
	    int m = 0x5bd1e995;
	    int r = 24;
	    int h = seed ^ data.length;
	    int len = data.length;
	    int len_4 = len >> 2;
	    for (int i = 0; i < len_4; i++) {
	      int i_4 = i << 2;
	      int k = data[i_4 + 3];
	      k = k << 8;
	      k = k | (data[i_4 + 2] & 0xff);
	      k = k << 8;
	      k = k | (data[i_4 + 1] & 0xff);
	      k = k << 8;
	      k = k | (data[i_4 + 0] & 0xff);
	      k *= m;
	      k ^= k >>> r;
	      k *= m;
	      h *= m;
	      h ^= k;
	    }
	    int len_m = len_4 << 2;
	    int left = len - len_m;
	    if (left != 0) {
	      if (left >= 3) {
	        h ^= (int) data[len - 3] << 16;
	      }
	      if (left >= 2) {
	        h ^= (int) data[len - 2] << 8;
	      }
	      if (left >= 1) {
	        h ^= (int) data[len - 1];
	      }
	      h *= m;
	    }
	    h ^= h >>> 13;
	    h *= m;
	    h ^= h >>> 15;
	    return h;
  	}
}