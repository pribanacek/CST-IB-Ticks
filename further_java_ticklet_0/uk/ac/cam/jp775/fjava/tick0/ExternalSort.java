package uk.ac.cam.jp775.fjava.tick0;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ExternalSort {

    public static final int BYTES_PER_INT = 4;

    public static long estimateAvailableMemory() {
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();
        long presumableFreeMemory = r.maxMemory() - allocatedMemory;
        return presumableFreeMemory;
    }

	public static void sort(String filenameA, String filenameB) throws IOException {
        RandomAccessFile fileA1 = new RandomAccessFile(filenameA,"rw");
        RandomAccessFile fileA2 = new RandomAccessFile(filenameA,"rw");
        RandomAccessFile fileB1 = new RandomAccessFile(filenameB,"rw");
        RandomAccessFile fileB2 = new RandomAccessFile(filenameB,"rw");

        int bufferSize = (int) (estimateAvailableMemory() / 6L);
        int blockSize = bufferSize / BYTES_PER_INT; //size of sorted chunks we're merging (measured in number of ints)
        long fileLength = fileA1.length();

        preSort(fileA1, fileB1, blockSize);

        RandomAccessFile fileIn1 = fileB1;
        RandomAccessFile fileIn2 = fileB2;
        RandomAccessFile fileOut1 = fileA1;
        RandomAccessFile fileOut2 = fileA2;

        while (blockSize * BYTES_PER_INT < fileLength) {
            DataInputStream inStream1 = new DataInputStream(new BufferedInputStream(
                                                new FileInputStream(fileIn1.getFD()), bufferSize));
            DataInputStream inStream2 = new DataInputStream(new BufferedInputStream(
                                                new FileInputStream(fileIn2.getFD()), bufferSize));
            DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(
                                                new FileOutputStream(fileOut1.getFD()), bufferSize * 2));

            fileIn1.seek(0);
            fileIn2.seek(0);
            fileOut1.seek(0);
            fileOut2.seek(0);

            mergeStreams(inStream1, inStream2, outStream, blockSize, fileLength);

            //swap file A and B references
            RandomAccessFile temp1 = fileIn1;
            fileIn1 = fileOut1;
            fileOut1 = temp1;

            RandomAccessFile temp2 = fileIn2;
            fileIn2 = fileOut2;
            fileOut2 = temp2;

            blockSize *= 2;
        }

        if (fileIn1 == fileB1) { //we ended up in fileB, so copy everything back to A
            copy(fileB1, fileA1);
        }
	}

	public static void mergeStreams(DataInputStream streamAin, DataInputStream streamBin,
                                    DataOutputStream streamOut, int blockSize, long fileLength) throws IOException {

        int numberOfBlocks = (int) Math.ceil((double) fileLength / ((double) BYTES_PER_INT * blockSize));

        streamBin.skipBytes(blockSize * BYTES_PER_INT);

        for (int i = 0; i < numberOfBlocks; i += 2) {

            int posA = 0;
            int posB = 0;

            boolean streamAEmpty = false;
            boolean streamBEmpty = false;

            int a = 0;
            int b = 0;
            if ((i * blockSize + posA) * BYTES_PER_INT < fileLength && posA < blockSize) {
                a = streamAin.readInt();
                posA++;
            } else {
                streamAEmpty = true;
            }
            if (((i + 1) * blockSize + posB) * BYTES_PER_INT < fileLength && posB < blockSize) {
                b = streamBin.readInt();
                posB++;
            } else {
                streamBEmpty = true;
            }

            while (!(streamAEmpty && streamBEmpty)) {
                if (!streamAEmpty && !streamBEmpty) {
                    if (a < b) {
                        streamOut.writeInt(a);
                        if (posA < blockSize && (i * blockSize + posA) * BYTES_PER_INT < fileLength) {
                            a = streamAin.readInt();
                            posA++;
                        } else {
                            streamAEmpty = true;
                        }
                    } else if (a > b) {
                        streamOut.writeInt(b);
                        if (posB < blockSize && ((i + 1) * blockSize + posB) * BYTES_PER_INT < fileLength) {
                            b = streamBin.readInt();
                            posB++;
                        } else {
                            streamBEmpty = true;
                        }
                    } else {
                        streamOut.writeInt(a);
                        streamOut.writeInt(b);
                        if (posA < blockSize && (i * blockSize + posA) * BYTES_PER_INT < fileLength) {
                            a = streamAin.readInt();
                            posA++;
                        } else {
                            streamAEmpty = true;
                        }
                        if (posB < blockSize && ((i + 1) * blockSize + posB) * BYTES_PER_INT < fileLength) {
                            b = streamBin.readInt();
                            posB++;
                        } else {
                            streamBEmpty = true;
                        }
                    }

                } else {
                    if (streamAEmpty) {
                        streamOut.writeInt(b);
                        if (posB < blockSize && ((i + 1) * blockSize + posB) * BYTES_PER_INT < fileLength) {
                            b = streamBin.readInt();
                            posB++;
                        } else {
                            streamBEmpty = true;
                        }
                    } else {
                        streamOut.writeInt(a);
                        if (posA < blockSize && (i * blockSize + posA) * BYTES_PER_INT < fileLength) {
                            a = streamAin.readInt();
                            posA++;
                        } else {
                            streamAEmpty = true;
                        }
                    }
                }
            }

            streamOut.flush();

            streamAin.skipBytes(blockSize * BYTES_PER_INT);
            streamBin.skipBytes(blockSize * BYTES_PER_INT);
        }

    }

    public static void copy(RandomAccessFile fileIn, RandomAccessFile fileOut) throws IOException {
        DataInputStream inStream = new DataInputStream(new BufferedInputStream(
                                            new FileInputStream((fileIn.getFD()))));
        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(
                                            new FileOutputStream(fileOut.getFD())));

        fileIn.seek(0);
        fileOut.seek(0);

        for (int i = 0; i < fileIn.length() / BYTES_PER_INT; i++) {
            outStream.writeInt(inStream.readInt());
        }
        outStream.flush();
    }

	public static void preSort(RandomAccessFile fileIn, RandomAccessFile fileOut, int blockSize) throws IOException {

        fileIn.seek(0);
        fileOut.seek(0);

        DataInputStream inStream = new DataInputStream(new BufferedInputStream(
                                            new FileInputStream(fileIn.getFD()), blockSize * BYTES_PER_INT));
        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(
                                            new FileOutputStream(fileOut.getFD()), blockSize * BYTES_PER_INT));

        int numberOfBlocks = (int) Math.ceil((double) fileIn.length() / ((double) BYTES_PER_INT * blockSize));

        for (int i = 0; i < numberOfBlocks; i++) {
            int[] array = readIntArrayFromStream(inStream, blockSize);
	        Arrays.sort(array);
	        writeIntArrayToStream(outStream, array);
	        outStream.flush();
        }
    }

    public static int[] readIntArrayFromStream(DataInputStream stream, int length) throws IOException {
        int[] array = new int[length];
        int arrayLength = array.length;

        for (int i = 0; i < array.length; i++) {
            try {
                array[i] = stream.readInt();
            } catch (EOFException e) {
                //end of file is reached before array is filled
                arrayLength = i;
                break;
            }
        }

        if (arrayLength < array.length) {
            //array is shortened to only have the data read
            array = Arrays.copyOfRange(array, 0, arrayLength);
        }
        return array;
    }

    public static void writeIntArrayToStream(DataOutputStream stream, int[] array) throws IOException {
        for (int i = 0; i < array.length; i++) {
            stream.writeInt(array[i]);
        }
        stream.flush();
    }

	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for(byte v : md.digest())
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String file1 = args[0];
        String file2 = args[1];

        try {
            sort(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("The checksum is: "+checkSum(file1));
        System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");
	}
}
