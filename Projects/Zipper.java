import java.util.zip.*;
import java.util.jar.*;
import java.io.*;
/*took code from http://www.oracle.com/technetwork/articles/java/compress-1565076.html, 
http://www.programcreek.com/2011/03/java-write-to-a-file-code-example/, and Goodrich 5.1.4
got help from http://stackoverflow.com/questions/7409953/writing-a-long-primitive-type-to-a-file-in-java*/
public class Zipper
{
	static final int BUFFER = 2048;
	CheckedOutputStream checksum;
	ZipOutputStream out;
	BufferedInputStream origin = null;
	PrintWriter reporter;
	int pathAdjuster;
	byte data[] = new byte[BUFFER];
	String[] filePaths;
	public static void main(String[] args) {
		try{
			File toBeZipped = new File(args[0]);
			File destination = new File(toBeZipped, "out" + args[1]);
			Zipper thing = new Zipper(destination.getPath(), new File(toBeZipped, "report.txt"));
			thing.recursiveZip(toBeZipped, args[2]);
			thing.finish(new File(toBeZipped, "crc.txt"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	* makes a new zipper object with a given destination and report file.
	*@param destination the file path of the destination zip file
	*@param reportFile The file to store reports in.
	*/
	Zipper(String destination, File reportFile)
		throws IOException
	{
		if(reportFile.getParentFile().getParent()  != null)
			pathAdjuster = reportFile.getParentFile().getParent().length() + 1;
		else
			pathAdjuster = 0;
		filePaths = new String[2];
		filePaths[1] = reportFile.getPath();
		filePaths[0] = destination;
		FileOutputStream output = new FileOutputStream(reportFile);
		reporter = new PrintWriter(output, true);
		if(destination.endsWith(".zip")){
			prepareZipOutputStream(destination);
		}
		else if (destination.endsWith(".jar")) {
			prepareJarOutputStream(destination);
		}
		else{
			throw new IOException("Please choose .jar or .zip file type");
		}
	}
	/**
	*recursivly goes through the file system and zips every file it finds 
	*@param file the file to be evaluated
	*@param report the file type to report
	*/
	public void recursiveZip(File file, String report)
		throws IOException
	{
		if(file.isDirectory()){
			for (String childname : file.list()) {
				File child = new File(file, childname);
				recursiveZip(child, report);
			}
		}
		else{
			if(file.getPath().endsWith(report)){
				reporter.println(file.getPath().substring(pathAdjuster));
			}
			compressFile(file);
		}
	}
	/**
	*	prepares the zip output stream.
	*@param destination the destination for the zip file
	*/
	private void prepareZipOutputStream(String destination)
		throws IOException
	{
		BufferedInputStream origin = null;
		FileOutputStream dest = new 
		  FileOutputStream(destination);
		checksum = new 
		  CheckedOutputStream(dest, new CRC32());
		out = new 
		  ZipOutputStream(new 
		    BufferedOutputStream(checksum));
		//out.setMethod(ZipOutputStream.DEFLATED);
	}
	/**
	* prepares the jar output stream.
	*@param destination the destination for the jar file
	*/

	private void prepareJarOutputStream(String destination)
			throws IOException
	{
		BufferedInputStream origin = null;
		FileOutputStream dest = new 
		  FileOutputStream(destination);
		checksum = new 
		  CheckedOutputStream(dest, new CRC32());
		out = new 
		  JarOutputStream(new 
		    BufferedOutputStream(checksum));
		//out.setMethod(ZipOutputStream.DEFLATED);
	}
	/**
	* compresses a single file and adds it to the main zip file 
	*
	*@param file the file to compress and add
	*/
	private void compressFile(File file)
		throws IOException
	{
		//does not add if it is the zip archive itself or the reporting file
		if(file.getPath().equals(filePaths[0]) || file.getPath().equals(filePaths[1]))
			return;
		FileInputStream fi = new 
		  FileInputStream(file);
		this.origin = new 
		  BufferedInputStream(fi, BUFFER);
		ZipEntry entry = new ZipEntry(file.getPath());
		this.out.putNextEntry(entry);
		int count;
		while((count = this.origin.read(data, 0, 
		  BUFFER)) != -1) {
		   this.out.write(data, 0, count);
		}
		this.origin.close();
	}
	/**
	* closes the reporting files and creates the checksum file
	*@param checksumFile the destination of the checksum file
	*/
	public void finish(File checksumFile)
		throws IOException
	{
		this.out.close();
		this.reporter.close();
		FileWriter fw = new FileWriter(checksumFile);
		fw.write(checksum.getChecksum().getValue() + "\n");
		fw.close();
	}
}
