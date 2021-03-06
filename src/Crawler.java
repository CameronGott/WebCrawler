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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.io.FileWriter;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Crawler {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.println("Press 'c' for crawl mode:");
		char userMode = input.nextLine().toCharArray()[0];
		if(userMode == 'c')
		{
			//input.close
			System.out.println("Enter a URL: ");
			String url = input.nextLine();
			
			crawler(url);
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
			
			for(String s : getSubURLs(urlString))
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
	
	public static ArrayList<String> getSubURLs(String urlString)
	{
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
			connection.connect();
			BufferedReader r  = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
			
			String line = r.readLine();
			int current = 0;
			while(line != null)
			{
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
 * Slept for one hour tonight. Not a very productive day today. 
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
