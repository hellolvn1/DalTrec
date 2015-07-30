/**
 * Twitter Tools
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.twittertools.stream;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;
import org.apache.log4j.varia.LevelRangeFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import twitter4j.RawStreamListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.google.gson.Gson;

public final class GatherStatusStream {
	private static String GOOGLE_IMAGE = "https://images.google.com/searchbyimage?site=search&image_url=";
	private static int cnt = 0;

	public static String urlRegex = "http+://[\\S]+|https+://[\\S]+";
	public static Pattern urlPattern = Pattern.compile(urlRegex);
	private static Logger logger = Logger.getLogger(GatherStatusStream.class);
	private static Map<UserProfile, List<Tweet>> selectedTweets = new HashMap<UserProfile, List<Tweet>>();
	private static List<UserProfile> lsProfiles = new ArrayList<UserProfile>();
	@SuppressWarnings("unused")
	private static final String MINUTE_ROLL = ".%d{yyyy-MM-dd-HH-mm}.gz";
	private static final String HOUR_ROLL = ".%d{yyyy-MM-dd-HH}.gz";
	private static final String SCENARIO_B = "yyyyMMdd"; //"Fri Mar 29 11:03:41 +0000 2013"; 
	private static final Map<String, String> mapWeights = new HashMap<String, String>();
	public static void main(String[] args) throws TwitterException {
		// get a list of stop word
		String stopWordFile = PropertyUtils.getStopwordPath();
		File stopword = new File(stopWordFile);
		List<String> lsWeight = null;
		
		try {
			lsWeight = FileUtils.readLines(stopword);
			for (String line: lsWeight){				
				String [] arr = line.split("\\s+");
				String code = arr[0];
				mapWeights.put(code.toLowerCase(), line.toLowerCase());				
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (Profile pro: Profile.values()){
				try {
					//
					System.out.println("Processing profile:" + pro.getCode());
					URL urlRest = new URL(
							"http://localhost:8080/dexter-webapp/api/rest/annotate");
					String query = "text=" + pro.getTitle().replaceAll("[^ A-Za-z]+", "") + "&n=50&wn=false&debug=false&format=text&min-conf=0.5";

					// make connection
					URLConnection urlc = urlRest.openConnection();

					// use post mode
					urlc.setDoOutput(true);
					urlc.setAllowUserInteraction(false);

					// send query
					PrintStream ps;
					ps = new PrintStream(urlc.getOutputStream());
					ps.print(query);
					ps.close();

					// get result
					BufferedReader br = new BufferedReader(
							new InputStreamReader(urlc.getInputStream()));
					String l = null;
							
					String title = "", description= "", details= "";
					
					while ((l = br.readLine()) != null) {
						//System.out.println(l);
						// get document
						JSONParser parser = new JSONParser();
						Object obj = parser.parse(l);						 
						JSONObject jsonObject = (JSONObject) obj;
						JSONArray msg = (JSONArray) jsonObject.get("spots");
						
						for (Object obje: msg){
							JSONObject jObj = (JSONObject) obje;
							//increase entities with Wikipedia linking
							
							String entity = jObj.get("mention").toString().toLowerCase();
							title += entity + " ";
						}					
					}
					
					URL urlRest1 = new URL(
							"http://localhost:8080/dexter-webapp/api/rest/annotate");
					String query1 = "text=" + pro.getDescription().replaceAll("[^ A-Za-z]+", "") + "&n=50&wn=false&debug=false&format=text&min-conf=0.5";

					// make connection
					URLConnection urlc1 = urlRest1.openConnection();
					

					// make connection

					// use post mode
					urlc1.setDoOutput(true);
					urlc1.setAllowUserInteraction(false);

					// send query
					PrintStream ps1;
					ps1 = new PrintStream(urlc1.getOutputStream());
					ps1.print(query1);
					ps1.close();

					// get result
					br = new BufferedReader(
							new InputStreamReader(urlc1.getInputStream()));
					l = null;												
					
					while ((l = br.readLine()) != null) {
						//System.out.println(l);
						// get document
						JSONParser parser = new JSONParser();
						Object obj = parser.parse(l);						 
						JSONObject jsonObject = (JSONObject) obj;
						JSONArray msg = (JSONArray) jsonObject.get("spots");
						
						for (Object obje: msg){
							JSONObject jObj = (JSONObject) obje;
							//increase entities with Wikipedia linking
							
							String entity = jObj.get("mention").toString().toLowerCase();
							description += entity + " ";
						}					
					}
					
					URL urlRest2 = new URL(
							"http://localhost:8080/dexter-webapp/api/rest/annotate");
					String query2 = "text=" + pro.getDetails().replaceAll("[^ A-Za-z]+", "") + "&n=50&wn=false&debug=false&format=text&min-conf=0.5";

					// make connection
					URLConnection urlc2 = urlRest2.openConnection();
					

					// make connection

					// use post mode
					urlc2.setDoOutput(true);
					urlc2.setAllowUserInteraction(false);

					// send query
					PrintStream ps2;
					ps2 = new PrintStream(urlc2.getOutputStream());
					ps2.print(query2);
					ps2.close();

					// get result
					br = new BufferedReader(
							new InputStreamReader(urlc2.getInputStream()));
					l = null;												
					
					while ((l = br.readLine()) != null) {
						//System.out.println(l);
						// get document
						JSONParser parser = new JSONParser();
						Object obj = parser.parse(l);						 
						JSONObject jsonObject = (JSONObject) obj;
						JSONArray msg = (JSONArray) jsonObject.get("spots");
						
						for (Object obje: msg){
							JSONObject jObj = (JSONObject) obje;
							//increase entities with Wikipedia linking
							
							String entity = jObj.get("mention").toString().toLowerCase();
							details += entity + " ";
						}					
					}
					
					UserProfile uProfile = new UserProfile(pro.getCode().toUpperCase(), title.toLowerCase(), description.toLowerCase(), details.toLowerCase());
					lsProfiles.add(uProfile);					
					br.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
		PatternLayout layoutStandard = new PatternLayout();
		layoutStandard.setConversionPattern("[%p] %d %c %M - %m%n");

		PatternLayout layoutSimple = new PatternLayout();
		layoutSimple.setConversionPattern("%m%n");

		// Filter for the statuses: we only want INFO messages
		LevelRangeFilter filter = new LevelRangeFilter();
		filter.setLevelMax(Level.INFO);
		filter.setLevelMin(Level.INFO);
		filter.setAcceptOnMatch(true);
		filter.activateOptions();

		TRECUtils.getProcessor();
		TRECUtils.getSim();

		TimeBasedRollingPolicy statusesRollingPolicy = new TimeBasedRollingPolicy();
		statusesRollingPolicy.setFileNamePattern("statuses.log" + HOUR_ROLL);
		statusesRollingPolicy.activateOptions();

		RollingFileAppender statusesAppender = new RollingFileAppender();
		statusesAppender.setRollingPolicy(statusesRollingPolicy);
		statusesAppender.addFilter(filter);
		statusesAppender.setLayout(layoutSimple);
		statusesAppender.activateOptions();

		TimeBasedRollingPolicy warningsRollingPolicy = new TimeBasedRollingPolicy();
		warningsRollingPolicy.setFileNamePattern("warnings.log" + HOUR_ROLL);
		warningsRollingPolicy.activateOptions();

		RollingFileAppender warningsAppender = new RollingFileAppender();
		warningsAppender.setRollingPolicy(statusesRollingPolicy);
		warningsAppender.setThreshold(Level.WARN);
		warningsAppender.setLayout(layoutStandard);
		warningsAppender.activateOptions();

		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setThreshold(Level.WARN);
		consoleAppender.setLayout(layoutStandard);
		consoleAppender.activateOptions();

		// configures the root logger
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		rootLogger.removeAllAppenders();
		rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(statusesAppender);
		rootLogger.addAppender(warningsAppender);

		// creates a custom logger and log messages

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		RawStreamListener rawListener = new RawStreamListener() {

			@Override
			public void onMessage(String rawString) {
				if (rawString.contains("\"delete\"")) {
					return;
				}
				cnt++;
				Gson gson = new Gson();
				Tweet tweet = gson.fromJson(rawString, Tweet.class);
				String orgTweet = tweet.getText();
				if (!tweet.getLang().equals("en")
						|| (tweet.getRetweetedStatus() != null)) {
					return;
				}
				logger.info(tweet.getIdStr() + "-" + tweet.getText());
				if (cnt % 1000 == 0) {
					System.out.println(cnt + " messages received.");
				}

				try {
					URL urlRest = new URL(
							"http://localhost:8080/dexter-webapp/api/rest/annotate");
					String query = "text=" + tweet.getText().replaceAll("[^ bA-Za-z]+", "") + "&n=50&wn=false&debug=false&format=text&min-conf=0.5";

					// make connection
					URLConnection urlc = urlRest.openConnection();

					// use post mode
					urlc.setDoOutput(true);
					urlc.setAllowUserInteraction(false);

					// send query
					PrintStream ps;
					ps = new PrintStream(urlc.getOutputStream());
					ps.print(query);
					ps.close();

					// get result
					BufferedReader br = new BufferedReader(
							new InputStreamReader(urlc.getInputStream()));
					String l = null;
					String strTweet = "";
					while ((l = br.readLine()) != null) {
						//System.out.println(l);
						// get document
						JSONParser parser = new JSONParser();
						Object obj = parser.parse(l);						 
						JSONObject jsonObject = (JSONObject) obj;
						JSONArray msg = (JSONArray) jsonObject.get("spots");
						for (Object obje: msg){
							JSONObject jObj = (JSONObject) obje;
							//increase entities with Wikipedia linking
							String entity = jObj.get("mention").toString().toLowerCase();
							strTweet += entity + " ";							
						}
					}
					tweet.setText(strTweet.toLowerCase());
					br.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!tweet.getText().isEmpty())
					automaticMobileAgorithm(tweet, orgTweet);
					automaticDailyAgorithm(tweet, orgTweet);
					manualDailyAgorithm(tweet, orgTweet);
					manualMobileAgorithm(tweet, orgTweet);
			}

			@Override
			public void onException(Exception ex) {
				logger.warn(ex);
			}

		};

		twitterStream.addListener(rawListener);
		twitterStream.sample();
	}

	public static String getLongUrl(String shortUrl)
			throws MalformedURLException, IOException {
		String result = shortUrl;
		String header;
		do {
			URL url = new URL(result);
			HttpURLConnection.setFollowRedirects(false);
			URLConnection conn = url.openConnection();
			header = conn.getHeaderField(null);
			String location = conn.getHeaderField("location");
			if (location != null) {
				result = location;
			}
		} while (header.contains("301"));

		return result;
	}

	public static String automaticMobileAgorithm(Tweet tweet, String urlContent) {
		// loop through enum
		UserProfile maxPro = lsProfiles.get(0);
		Double max = 0.0;
		for (UserProfile pro : lsProfiles) {
			// high cosine and GTM similarity mean high duplicated content
			try {
				Double sim1 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getTitle());
				//if tweet does not match with profile title --> ignore
				if (sim1 <= 0.7){
					continue;
				}
				Double sim2 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDescription());
				Double sim3 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDetails());
				double sim = Math.max(sim1, Math.max(sim2, sim3));
				if (sim > 1.0){
					continue;
				}
				if (sim > max) {
					maxPro = pro;
					max = sim;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		if (max >= TRECUtils.DEFAULT_GTM_SCORE) {
			if (!selectedTweets.containsKey(maxPro.getCode())) {
				List<Tweet> tweets = new ArrayList<Tweet>();
				tweets.add(tweet);
				selectedTweets.put(maxPro, tweets);
				logger.info(maxPro.getCode() + "-" + tweet.getIdStr() + "-"
						+ tweet.getCreatedAt());
				
				long epoch = new Date().getTime()/1000;					
				// write tweets to files for clustering
				try {
					FileUtils.writeStringToFile(
					new File(PropertyUtils.getTweetFolder() + "//" + "scenarioA_A.txt"), maxPro.getCode() + " " + tweet.getIdStr() + " " + epoch + " DALTRECAA1" + System.getProperty("line.separator"), true);				
					System.out.println("Tweet found: " + tweet.getText());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			} else {
				List<Tweet> tweets = selectedTweets.get(maxPro);
				boolean flag = true;
				// compare with current tweets
				for (Tweet t : tweets) {
					if (TRECUtils.isTwoTweetsContentTheSame(t, tweet)) {
						flag = false;
					}
				}
				if (flag) {
					tweets.add(tweet);
					selectedTweets.put(maxPro, tweets);
					
					long epoch = new Date().getTime()/1000;					
					// write tweets to files for clustering
					try {
						FileUtils.writeStringToFile(
						new File(PropertyUtils.getTweetFolder() + "//" + "scenarioA_A.txt"), maxPro.getCode() + " " + tweet.getIdStr() + " " + epoch + " DALTRECAA1" + System.getProperty("line.separator"), true);				
						System.out.println("Tweet found: " + tweet.getText());
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			}

			
		}

		return "";
	}
	
	public static String automaticDailyAgorithm(Tweet tweet, String urlContent) {
		// loop through enum
		UserProfile maxPro = lsProfiles.get(0);
		Double max = 0.0;
		for (UserProfile pro : lsProfiles) {
			// high cosine and GTM similarity mean high duplicated content
			try {
				Double sim1 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getTitle());
				//if tweet does not match with profile title --> ignore
				
				if (sim1 <= 0.65){
					continue;
				}
				Double sim2 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDescription());
				Double sim3 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDetails());
				double sim = Math.max(sim1, Math.max(sim2, sim3));
				if (sim > 1.0){
					continue;
				}
				if (sim > max) {
					maxPro = pro;
					max = sim;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		if (max >= TRECUtils.DEFAULT_GTM_SCORE - 0.1) {
			if (!selectedTweets.containsKey(maxPro.getCode())) {
				List<Tweet> tweets = new ArrayList<Tweet>();
				tweets.add(tweet);
				selectedTweets.put(maxPro, tweets);				
				// write tweets to files for clustering
				try {
					FileUtils.writeStringToFile(
							new File(PropertyUtils.getTweetFolder() + "//" + "scenarioA_B.txt"),  new SimpleDateFormat(SCENARIO_B).format(new Date()) + " " + maxPro.getCode() + " " + "Q0 " + tweet.getIdStr() + " " + "1" + " 2 " + "DALTRECAB1" + System.getProperty("line.separator"), true);				
					System.out.println("Tweet found: " + tweet.getText());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			} else {
				List<Tweet> tweets = selectedTweets.get(maxPro);
				boolean flag = true;
				// compare with current tweets
				for (Tweet t : tweets) {
					if (TRECUtils.isTwoTweetsContentTheSame(t, tweet)) {
						flag = false;
					}
				}
				if (flag) {
						tweets.add(tweet);
						selectedTweets.put(maxPro, tweets);				
						// write tweets to files for clustering
						try {
							FileUtils.writeStringToFile(
							new File(PropertyUtils.getTweetFolder() + "//" + "scenarioA_B.txt"),  new SimpleDateFormat(SCENARIO_B).format(new Date()) + " " + maxPro.getCode() + " " + "Q0" + tweet.getIdStr() + " " + "1" + " 2 " + "DALTRECAB1" + System.getProperty("line.separator"), true);				
							System.out.println("Tweet found: " + tweet.getText());
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
				}
			}

			
		}

		return "";
	}
	
	public static String manualMobileAgorithm(Tweet tweet, String urlContent) {
		// loop through enum
		UserProfile maxPro = lsProfiles.get(0);
		Double max = 0.0;
		tweet.setText(urlContent);
		for (UserProfile pro : lsProfiles) {
			// high cosine and GTM similarity mean high duplicated content
			try {
				String strWeight = mapWeights.get(pro.getCode().toLowerCase());
				String strTeet = tweet.getText().toLowerCase();
				String proTitle = pro.getTitle().toLowerCase();
				if (strWeight != null){
					String[] arr = strWeight.split("\\s+");
					for (int i = 1; i < arr.length; i++ ){
						String word = arr[i].split(":")[0];
						if (word.contains("_")){
							word = word.replace("_", " ");
						}
						String duplicatedWord = word;
						Integer value = Integer.valueOf(arr[i].split(":")[1]);
						if (tweet.getText().contains(word) && pro.getTitle().contains(word)){
							for (int j = 1; j < value;j++){
								duplicatedWord += " " + word;
							}
							strTeet = tweet.getText().replace(word, duplicatedWord); 
							proTitle = pro.getTitle().replace(word, duplicatedWord);
						}
					}
				}
				
				
				Double sim1 = TRECUtils.docDocComparison(strTeet,
						proTitle);
				//if tweet does not match with profile title --> ignore
				if (sim1 <= 0.7){
					continue;
				}
				Double sim2 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDescription());
				Double sim3 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDetails());
				double sim = Math.max(sim1, Math.max(sim2, sim3));
				if (sim > 1.0){
					continue;
				}
				if (sim > max) {
					maxPro = pro;
					max = sim;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		if (max >= TRECUtils.DEFAULT_GTM_SCORE) {
			if (!selectedTweets.containsKey(maxPro.getCode())) {
				List<Tweet> tweets = new ArrayList<Tweet>();
				tweets.add(tweet);
				selectedTweets.put(maxPro, tweets);
				logger.info(maxPro.getCode() + "-" + tweet.getId() + "-"
						+ tweet.getCreatedAt());
				
				long epoch = new Date().getTime()/1000;					
				// write tweets to files for clustering
				try {
					FileUtils.writeStringToFile(
					new File(PropertyUtils.getTweetFolder() + "//" + "scenarioM_A.txt"), maxPro.getCode() + " " + tweet.getIdStr() + " " + epoch + " DALTRECMA1" + System.getProperty("line.separator"), true);				
					System.out.println("Tweet found: " + tweet.getText());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			} else {
				List<Tweet> tweets = selectedTweets.get(maxPro);
				boolean flag = true;
				// compare with current tweets
				for (Tweet t : tweets) {
					if (TRECUtils.isTwoTweetsContentTheSame(t, tweet)) {
						flag = false;
					}
				}
				if (flag) {
					tweets.add(tweet);
					selectedTweets.put(maxPro, tweets);
					
					long epoch = new Date().getTime()/1000;					
					// write tweets to files for clustering
					try {
						FileUtils.writeStringToFile(
						new File(PropertyUtils.getTweetFolder() + "//" + "scenarioM_A.txt"), maxPro.getCode() + " " + tweet.getIdStr() + " " + epoch + " DALTRECMA1" + System.getProperty("line.separator"), true);				
						System.out.println("Tweet found: " + tweet.getText());
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			}

			
		}

		return "";
	}
	
	public static String manualDailyAgorithm(Tweet tweet, String urlContent) {
		// loop through enum
		UserProfile maxPro = lsProfiles.get(0);
		Double max = 0.0;
		for (UserProfile pro : lsProfiles) {
			// high cosine and GTM similarity mean high duplicated content
			try {
				String strWeight = mapWeights.get(pro.getCode().toLowerCase());
				String strTeet = tweet.getText();
				String proTitle = pro.getTitle();
				if (strWeight != null){
					String[] arr = strWeight.split("\\s+");
					for (int i = 1; i < arr.length; i++ ){
						String word = arr[i].split(":")[0];
						if (word.contains("_")){
							word = word.replace("_", " ");
						}
						
						String duplicatedWord = word;
						Integer value = Integer.valueOf(arr[i].split(":")[1]);
						if (tweet.getText().toLowerCase().contains(word) && pro.getTitle().toLowerCase().contains(word)){
							for (int j = 1; j < value;j++){
								duplicatedWord += " " + word;
							}
							strTeet = tweet.getText().replace(word, duplicatedWord); 
							proTitle = pro.getTitle().replace(word, duplicatedWord);
						}
					}
				}
				
				Double sim1 = TRECUtils.docDocComparison(strTeet,
						proTitle);
				//if tweet does not match with profile title --> ignore
				if (sim1 <= 0.65){
					continue;
				}
				Double sim2 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDescription());
				Double sim3 = TRECUtils.docDocComparison(tweet.getText(),
						pro.getDetails());
				double sim = Math.max(sim1, Math.max(sim2, sim3));
				if (sim > 1.0){
					continue;
				}
				if (sim > max) {
					maxPro = pro;
					max = sim;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

		if (max >= TRECUtils.DEFAULT_GTM_SCORE - 0.1) {
			if (!selectedTweets.containsKey(maxPro.getCode())) {
				List<Tweet> tweets = new ArrayList<Tweet>();
				tweets.add(tweet);
				selectedTweets.put(maxPro, tweets);					
				// write tweets to files for clustering
				try {
					FileUtils.writeStringToFile(
							new File(PropertyUtils.getTweetFolder() + "//" + "scenarioM_B.txt"),  new SimpleDateFormat(SCENARIO_B).format(new Date()) + " " + maxPro.getCode() + " " + "Q0 " + tweet.getIdStr() + " " + "1" + " 2 " + "DALTRECMB1" + System.getProperty("line.separator"), true);				
					System.out.println("Tweet found: " + tweet.getText());
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			} else {
				List<Tweet> tweets = selectedTweets.get(maxPro);
				boolean flag = true;
				// compare with current tweets
				for (Tweet t : tweets) {
					if (TRECUtils.isTwoTweetsContentTheSame(t, tweet)) {
						flag = false;
					}
				}
				if (flag) {
						tweets.add(tweet);
						selectedTweets.put(maxPro, tweets);
						// write tweets to files for clustering
						try {
							FileUtils.writeStringToFile(
							new File(PropertyUtils.getTweetFolder() + "//" + "scenarioM_B.txt"),  new SimpleDateFormat(SCENARIO_B).format(new Date()) + " " + maxPro.getCode() + " " + "Q0" + tweet.getIdStr() + " " + "1" + " 2 " + "DALTRECMB1" + System.getProperty("line.separator"), true);				
							System.out.println("Tweet found: " + tweet.getText());
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
				}
			}

			
		}

		return "";
	}
}
