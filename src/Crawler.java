/*
 * Basic web crawler
 * 
 * Initial version pulled from Introduction to Java Programming by Y. Daniel Liang
 * 02/27/2021
 * 
 * and developed by Cameron Gott.
 * 
 * 
 * ******* FEATURES *******
 * -abort requests / parsing longer than X amount of time
 * -scrape more information from each site (Jsoup will help here)
 * -tree structure search - crawl subsequently discovered URLs
 * -clean up duplicate URLs in traversedURLs file
 * -setup command line argument support
 * -multi-threading support for crawling
 * -multi-thread file clean-up
 * -use Jsoup to parse HTML, start building a list of keywords
 * -store history of searched URLs in file
 * -store history of searched URLs in database
 * -check input URL against stored history
 * -store URL results in a database
 * -notify user of waiting URL requests greater than 3 seconds
 * -notify users of request statuses as they happen
 * -allow user to interrupt long crawl sessions 
 * 		(maybe multithreading to read keyboard input?)
 * Start filtering out www.w3 domains (maybe use regex on url string before
 *     making the request?)
 * 
 * 
 *  ******* FINISHED FEATURES *******
 *  -store URL results in a file
 *  -crawl https links in addition to http
 *  -add header to https request to make it actually return successfully
 *  -replaced Scanner with BufferedReader
 * 
 * ******* CHANGE LOG *******
 * 03/05/2021
 * -started building user mode selection (crawl versus cleanup versus index etc)
 * -building file mode for crawler (reading URLs to crawl from file storage)
 * 
 * 03/03/2021
 * -added Jsoup to project
 * -program now writes traversed URLs to file
 * -added support for parsing https links
 * -replaced Scanner with BufferedReader
 * -initial GitHub commit
 * 
 * 02/27/2021
 * -added termination message
 * -added code to print the number of URLs traversed
 */
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.io.FileWriter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Crawler {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.println("Press 'c' for manual URL crawl mode; f for file URL crawl mode: ");
		char userMode = input.nextLine().toCharArray()[0];
		if(userMode == 'c')
		{
			//input.close
			System.out.println("Enter a URL: ");
			String url = input.nextLine();
			
			crawler(url);
		}
		else if(userMode == 'f')
		{
			fileCrawler();
		}
		
		

	}
	
	public static void crawler(String startingURL)
	{
		ArrayList<String> listOfPendingURLs = new ArrayList<>();
		ArrayList<String> listOfTraversedURLs = new ArrayList<>();
		
		listOfPendingURLs.add(startingURL);
		while(!listOfPendingURLs.isEmpty() && listOfTraversedURLs.size() <= 100)
		{
			String urlString = listOfPendingURLs.remove(0);
			if(!listOfTraversedURLs.contains(urlString))
			{
				listOfTraversedURLs.add(urlString);
				System.out.println("Crawl: " + urlString);
			}
			if(urlString.contains("akamai.com"))
			{
				System.out.println("Parsing akamai url. Beginning bug hunt.\nBuilding arraylist of subURLs...");
			}
			
			ArrayList<String> subURLsFromPage = getSubURLs(urlString);
			if(urlString.contains("akamai.com"))
			{
				System.out.println("ArrayList of subURLs from akamai built successfully. Continuing...");
			}
			for(String s : subURLsFromPage)
			{
				if(!listOfTraversedURLs.contains(s))
				{
					listOfPendingURLs.add(s);
				}
			}
		}
		System.out.println("Finished crawling.");
		System.out.println("Traversed " + listOfTraversedURLs.size() + "URLs.");
		System.out.println("Storing traversed URLs to file.");
		writeTraversedURLsToFile(listOfTraversedURLs);
	}
	
	public static void fileCrawler()
	{
		ArrayList<String> listOfPendingURLs = new ArrayList<>();
		ArrayList<String> listOfTraversedURLs = new ArrayList<>();
		try
		{
			File urlsToCrawl = new File("traversedFiles.txt");
			BufferedReader reader = new BufferedReader(new FileReader(urlsToCrawl));
			
			//listOfPendingURLs.add(startingURL);
			String currentLine;
			try
			{
				while((currentLine = reader.readLine()) != null)
				{
					listOfPendingURLs.add(currentLine);
				}
			}
			catch(IOException e)
			{
				System.out.println("An error ocurred in fileCrawler() function");
				e.printStackTrace();
			}
			
			
			while(!listOfPendingURLs.isEmpty() && listOfTraversedURLs.size() <= 1000)
			{
				String urlString = listOfPendingURLs.remove(0);
				if(!listOfTraversedURLs.contains(urlString))
				{
					listOfTraversedURLs.add(urlString);
					System.out.println("Crawl: " + urlString);
				}
				
				for(String s : getSubURLs(urlString))
				{
					
					if(!listOfTraversedURLs.contains(s))
					{
						listOfPendingURLs.add(s);
					}
				}
			}
			System.out.println("Finished crawling file URLs.");
			System.out.println("Traversed " + listOfTraversedURLs.size() + "URLs.");
			System.out.println("Storing traversed URLs to file.");
			writeTraversedURLsToFile(listOfTraversedURLs);
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found. Oops you dun goofed.");
		}
		
		
	}
	
	public static ArrayList<String> getSubURLs(String urlString)
	{
		if(urlString.contains("akamai.com"))
		{
			System.out.println("entering getSubURLs");
		}
		ArrayList<String> list = new ArrayList<>();
		
		try
		{
			java.net.URL url = new java.net.URL(urlString);
			//Scanner input = new Scanner(url.openStream());
			/*
			 * The code block below implements the added behavior
			 * to make HTTPS requests return successfully. We do this by
			 * playing with the header we send so our program appears to be a user
			 * on a normal web browser. We do this because without setting the header
			 * request properties, we get lots of errors and no HTML :(
			 */
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			try
			{
				/*
				 * lots of connection attempts are taking a long time
				 * and the long attempts usually correlate to non-interesting
				 * URLs (like resources, images, cloud-based crap)
				 * so we will set a timeout value to move on if its taking
				 * too long to connect.
				 */
				connection.setConnectTimeout(5000);
				connection.connect();
				
			}
			catch(SocketTimeoutException e)
			{
				System.out.println("Socket Timeout Exception on last URL :(");
			}
			catch(IOException e)
			{
				System.out.println("IOException thrown :(");
			}
			if(urlString.contains("akamai.com"))
			{
				System.out.println("creating BufferedReader...");
			}
			
			BufferedReader r  = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
			if(urlString.contains("akamai.com"))
			{
				System.out.println("BufferedReader created successfully. Reading first line...");
			}
			
			String line = r.readLine();
			int current = 0;
			while(line != null)
			{
				if(urlString.contains("akamai.com"))
				{
					System.out.println("entering main parse while loop...");
				}
				//String line = input.nextLine();
				// **************** to do ************
				//add https link support
				
				//first parse the document for http links.
				//Fun fact: This was the sole original parse loop. 
				current = line.indexOf("http:", current);
				while(current > 0)
				{
					int endIndex = line.indexOf("\"", current);
					if(endIndex > 0)
					{
						list.add(line.substring(current, endIndex));
						current = line.indexOf("http:", endIndex);
					}
					else
					{
						current = -1;
					}
				} //end while
				//Now parse the line for https strings.
				current = 0;
				current = line.indexOf("https:", current);
				while(current > 0)
				{
					int endIndex = line.indexOf("\"", current);
					if(endIndex > 0)
					{
						list.add(line.substring(current, endIndex));
						current = line.indexOf("https:", endIndex);
					}
					else
					{
						current = -1;
					}
				} //end while
				if(urlString.contains("akamai.com"))
				{
					System.out.println("exiting loop iteration. Reading next line...");
				}
				line = r.readLine();
			}//end while
		}
		
		
		catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
		return list;
	}
	
	public static void writeTraversedURLsToFile(ArrayList<String> urls)
	{
		//Create the URL storage file / confirm it exists already
		try
		{
			File traversedURLsFile = new File("traversedFiles.txt");
			if(traversedURLsFile.createNewFile())
			{
				System.out.println("File created successfully.");
			}
			else
			{
				System.out.println("File already exists.");
			}
		}
		catch(IOException e)
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		//write contents of ArrayList that stores urls to the file
		try
		{
			FileWriter fileWriter = new FileWriter("traversedFiles.txt");
			for(String url : urls)
			{
				//We use a temp String because
				//I tried building the url + newline string
				//inside of the write() function call, which ended up writing 
				//just the urls without a newline. That was ugly to read, and
				//would be a bitch to parse later.
				//EDIT:
				//might have just been Notepad refusing to display the damn newlines. 
				//assholes.
				String temp = url + "\n";
				fileWriter.write(temp);
			}
			fileWriter.close();
			System.out.println("URLs successfully written to file.");
		}
		catch(IOException e)
		{
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
	}

}
/*
 * ******* NOTES *******
 * 03/05/2021
 * Slept for one hour tonight (last night). Not a very productive day today. 
 * Nvm, 30 minutes later and 2 White Claws has me writing a lot of code.
 * Time will tell if this code is of any use whatsoever.
 * 
 * Implemented connection timeout exception catching
 * for those pesky URLs that take forever to connect and end up
 * hanging up the crawler. Works pretty well! Crawl time improved by 500%.
 * 
 * So I'm running into a specific problem with an akamai URL hanging up.
 * Exceptions for URLConnection aren't getting thrown. Task Manager
 * says my CPU usage is ~3% so I'm probably not stuck in some infinite 
 * parsing loop. Last line of console output I receive is Crawl: {akamai URL}
 * so I'm starting the bug hunt there.
 * I don't know where exactly in my code the hang-up is, so finding that
 * problematic line is the first step.
 * 
 * Ok, so previously I was calling getSubURLs inside of a for-each loop to build
 * the ArrayList<String> of subURLs. That was a bitch to test, so I pulled out the
 * list building one line above the parsing for-each loop. About to test it now
 * to see what the console spits out (yes I know I should be a big boy and use
 * the debugger right now. But it's 11:15pm and I'm sleepy.)
 * 
 * So something interesting happened. Ive been testing on https://news.ycombinator.com
 * and they added a new link. My program started to hang up on some SEC page but recovered 
 * after maybe 10 seconds. Reaches the same problematic akamai url, only for the program to
 * actually reach the point of building our ArrayList<String> from the returned page.
 * 
 * I guess there's a chance the building of the list is taking a long ass time
 * without throwing up my CPU usage. I could confirm this by setting system
 * timers and printing our their values, researching any known bottlenecks / slow behavior 
 * of building ArrayList<String> from large inputs, etc etc. Might just let it 
 * run for 10-20 minutes and see if it completes.
 * 
 * This is the point where I'm really regretting not using
 * the debugger, cause I assume I could monitor the status of the arraylist build process
 * through increasing memory usage, size of list, etc etc. Maybe if this run
 * doesn't complete soon, I'll put on my big boy debugger pants.
 * 
 * No dice. Added some more good-ole console print statements and
 * found the hangup where we create the BufferedReader. Wonder if the file
 * size is too big for it to handle. Can implement a size check before creating? Maybe?
 * 
 * 03/04/2021
 * Next feature is going to be parsing HTML for keywords.
 * We'll use Jsoup to grap HTML, turn it into STring, then use one of
 *  many libraries to parse String text. 
 *  
 * Apache Lucene looks promising. 
 * https://lucene.apache.org/
 * 
 * Relevant SO post
 * https://stackoverflow.com/questions/30992731/finding-the-count-of-a-keyword-in-html-using-jsoup
 * 
 * Architecture:
 * On first thought, the easiest way to plug this into the existing
 * program is to insert the scraping code around our link-parsing code.
 * Optionally, if we want to provide the user a choice of whether to scrape while
 * crawling we can set up a command line argument to decide whether or not to 
 * scrape while crawling.
 * 
 * This would be useful in cases where we want to crawl really fast / efficiently
 * and don't want to be slowed down by any scraping / keyword hunting / indexing.
 * 
 * 03/03/2021
 * So today I got https links to parse correctly. That was fun. Not too difficult.
 * But I noticed a lot of errors when making requests to https links on news.ycombinator.com
 * I was getting constant error 403 http response codes. Suspecting some shenanigans, I manually
 * combed through the error log, copied the links, and put them into Google Chrome.
 * 
 * Alas, the sites work fine. 
 * So what is going on that makes the site servers happy to serve my request when submitted
 * through Chrome, but not happy to serve my Java program requests?
 * 
 * HTTP Status Code 403: Forbidden
 * So the server sees my request but is telling my program to fuck off.
 * Fair enough. 
 * How to bypass?
 * Pretend to be Chrome?
 * Some sort of ettiquette that I am missing?
 * 
 * OK, so I did some searching and there is infact a way to emulate being a 
 * browser. I need to modify the header I'm sending to appear as a "user agent".
 * This StackOverFlow link explains the solution:
 * 
 * https://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
 * 
 * Also, there is something called LiveHttpHeaders that allows me to see
 * the headers my browser sends out to servers. Kinda cool.
 * 
 * HTTPS uses SSL to connect to an endpoint. This is more complicated than
 * the HTTP protocol because there is some handshaking between me and the endpoint,
 * so that's why the HTTP version of this crawler was able
 * to be so simple and use so little code. 
 * 
 * Added project to GitHub. 
 * Git commands so I don't forget for the tenth time.
 * Pull up Git CMD (if I'm on my Windows 7 machine)
 * 
 * git add (any new files I have)
 * git commit -m "commit message"
 * git push -u origin master
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
