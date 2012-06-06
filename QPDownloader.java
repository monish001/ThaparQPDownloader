//File: QPDownloader.java
/*TODO

*/
import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.util.AbstractList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

class CourseInfo/* implements Comparable*/{
	CourseInfo(String na, String li){
		name = (na!=null)?na:"";
		link = (li!=null)?li:"";
//		System.out.print(name + ": " + link + "\n");
	}
	String link;
	String name;
}
class SeasonPage{//Contains info for 1 exam season
	void setCourseInfo(String input){
		Pattern pattern1 = Pattern.compile("http.*pdf");
		Matcher matcher1 = pattern1.matcher(input);
		Pattern pattern2 = Pattern.compile("(>.*)+((\\s)*)?(\\w)*</");
		Matcher matcher2 = pattern2.matcher(input);
		boolean res1=false, res2=false;
		if((res1=matcher1.find()) && (res2=matcher2.find())){
			int start = matcher1.start();
			int end = matcher1.end();
			String course_link = input.substring(start, end);

			start = matcher2.start()+1;
			end = matcher2.end();
			String course_name = input.substring(start, end).trim();
			end = course_name.indexOf('<');
			if(end != -1)
				course_name = course_name.substring(0, end);
//			System.out.println(course_name);
			boolean exists = true;
			while(exists){
				exists = false;
				for(CourseInfo ci: coursesInfo){
					if(course_name.equals(ci.name)){
						exists = true;
						break;
					}
				}
				if(exists){
					course_name += "a";
//					System.out.println("Repeat: "+course_name);
				}
			}
			coursesInfo.add(new CourseInfo(course_name, course_link));
			courses.add(course_name);
		}else
			System.out.println("REGEX ERROR 2: "+res1+" "+ res2+" "+input);
	}
	
	public String toString(){
		return name /*+"-"+ coursesInfo.size()*/;
	}
	SeasonPage(String na, String li){
		name = (na!=null)?na:"";
		link = (li!=null)?li:"";
//		System.out.print(name + ": " + link + "\n");

		//initialise coursesInfo
		String seasonPageHTML = (new DownloadHTML(link)).getHTML();
		
		//http://172.31.19.11/qp/mstsep09/BT011.pdf">BT011</a></span>
		//http://172.31.19.11/qp/esmay09/BH008.pdf" style="text-decoration: underline;"> BH008</
		//http://cl.thapar.edu/qp/EN0105.pdf">EN105</
		//String patternString = "http.+pdf\"([^>])*>((\\s)*)?(([^/\"])*\">)*(\\w)+</";
		String patternString = "http.+pdf\"([^>])*>((\\s)*)?([^/])*(\\w){1,7}</";
		Pattern pattern = Pattern.compile(patternString);
		
		Matcher matcher = pattern.matcher(seasonPageHTML);
//		System.out.println("HTML: "+seasonPageHTML+"\n");
		
		while(matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			String match = seasonPageHTML.substring(start, end);
//			System.out.println("Match: "+match);
			setCourseInfo(match);
		}
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
	}
	static void printCourses(){
		System.out.println(courses.size());
	}
	String link;
	String name;
	ArrayList<CourseInfo> coursesInfo = new ArrayList<CourseInfo>();
	static TreeSet<String> courses = new TreeSet<String>();
}
public class QPDownloader{//downloads links from the html select box and saves each in SeasonPage object
	QPDownloader(){
		//String input = (new DownloadHTML("http://localhost/library_qp.html")).getHTML();
		String input = (new DownloadHTML("http://cl.thapar.edu/library_qp.html")).getHTML();
		if(input.length() == 0)
			input = (new DownloadHTML("http://cl.thapar.edu/library_qp.html")).getHTML();
		String patternString = "<option.+";
		Pattern pattern = Pattern.compile(patternString);
		
		Matcher matcher = pattern.matcher(input);
		while(matcher.find()){
			int start = matcher.start()+14;
			int end = matcher.end();
			String match = input.substring(start, end);
			//for each page full of pdf links, read pdf link and its name in anchor tag.
//			System.out.println("Match: "+match);
			setExamSeasonInfo(match);
		}
//		printExamSeasonInfo();
//		SeasonPage.printCourses();
	}
	String shortenSeasonName(String season_name){
		season_name = season_name.replace("February", "Feb").replace("March", "Mar").replace("September", "Sep").replace("November", "Nov").replace("December", "Dec");
		season_name = season_name.replaceFirst("Mid.*Test", "MST").replaceFirst("Mid.*Examination", "MST");
		season_name = season_name.replaceFirst("End Semester Test", "EST").replaceFirst("End Semester Examination", "EST").replaceFirst("E 2 D Examination", "E 2 D");
		return season_name;
	}
	void setExamSeasonInfo(String input){
		if(!(input.endsWith("</option>")))
			input += "</option>";
		Pattern pattern1 = Pattern.compile("http.*html");
		Matcher matcher1 = pattern1.matcher(input);
		Pattern pattern2 = Pattern.compile(">.*</option>");
		Matcher matcher2 = pattern2.matcher(input);
		if(matcher1.find() && matcher2.find()){
			int start = matcher1.start();
			int end = matcher1.end();
			String season_link = input.substring(start, end);

			start = matcher2.start()+1;
			end = matcher2.end()-9;
			String season_name = shortenSeasonName(input.substring(start, end));
//			System.out.println(season_name +" "+ season_link+"\n");
			seasonPagesInfo.add(new SeasonPage(season_name, season_link));
		}else
			System.out.println("REGEX ERROR\n");
	}
/*	void printExamSeasonInfo(){
		Iterator season=seasonPagesInfo.iterator();
		while(season.hasNext())
		{
			SeasonPage s = (SeasonPage)(season.next());
			System.out.println(s.name);
			System.out.println(s.link);
			System.out.println("");
		}
	}*/
	public static void main(String[] args){
		QPDownloader examSeasons = new QPDownloader();
	}
	ArrayList<SeasonPage> seasonPagesInfo = new ArrayList<SeasonPage>();
}
class DownloadHTML{
	DownloadHTML(String web_link){
		if((web_link.substring(7,10)).equals("172"))
			web_link = "http://cl.thapar.edu/" + web_link.substring(20);
		for(int i=0; i<=5; i++){
			System.out.println("DownloadHTML: "+web_link+" i="+i);
			try{
				// Here we give the URL for the Crawler
				URL url = new URL(web_link);

				strbuf = new StringBuffer();
				SocketChannel channel = null;
				channel = SocketChannel.open();
				InetSocketAddress socketAddress = new InetSocketAddress(url.getHost(), 80);
				channel.connect(socketAddress);

				Charset charset = Charset.forName("ISO-8859-1");
				CharsetDecoder decoder = charset.newDecoder();
				CharsetEncoder encoder = charset.newEncoder();
				String request = "GET " + url.getFile() + " \r\n\r\n";
				ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
				CharBuffer charBuffer = CharBuffer.allocate(1024);
				channel.write(encoder.encode(CharBuffer.wrap(request)));
					
				while ((channel.read(buffer)) != -1) {
//					System.out.print("w");
					buffer.flip();
					decoder.decode(buffer, charBuffer, false);
					charBuffer.flip();
					strbuf.append(charBuffer);
					buffer.clear();
					charBuffer.clear();
				}

				
				if(strbuf.toString().length() != 0){
//					System.out.println("DownloadHTML: Got it in turn "+i+". "+web_link+" "+strbuf.toString().length());
					break;
				}
				if(i==5){
					System.out.println("DownloadHTML: SHIT, NOT WORKING "+web_link);
				}
			}

			catch(IOException e)
			{
				System.out.println("DownloadHTML: "+web_link+" "+e);
				break;
			}
		}
	}
	String getHTML()
	{
		return strbuf.toString();
	}
	private StringBuffer strbuf;
}
