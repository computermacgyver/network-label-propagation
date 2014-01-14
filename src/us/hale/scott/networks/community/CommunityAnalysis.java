package us.hale.scott.networks.community;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author Scott A. Hale, http://www.scotthale.net/
 * 
 * After community detection has completed, this class performs some basic statistical calculations
 * about the communities' sizes and compositions and saves these to a CSV file. 
 *
 */
public class CommunityAnalysis {
	
	private ArrayList<Community> communityList;
	
	
	/**
	 * Reads in a communities file
	 * 
	 * @param numCommunities The number of communities in the file
	 * @param fileMembership The filename of the file containing community memberships.
	 * 			This should be the numeric id of the node and the numeric id of the community 
	 * 			that node belongs to separated by whitespace.
	 * 			This can be produced by CleanLabels.java, by LabelPropagation.java, or another process. 
	 * @param fileLanguages The filename of a file containing the most-used language of each node.
	 * 			This should be the two-letter iso code of the most-used language 
	 * 			for each node one per line in the same order as the nodes are in the fileMembership.
	 * @throws IOException
	 */
	public void readIt(int numCommunities, String fileMembership, String fileLanguages) throws IOException {
		
		communityList = new ArrayList<Community>(numCommunities);
		for (int i=0; i<numCommunities; i++) {
			communityList.add(new Community());
		}
				
		
		BufferedReader brMembership = new BufferedReader(new FileReader(fileMembership));
		BufferedReader brLanguages = new BufferedReader(new FileReader(fileLanguages));
		
		String membershipLine = brMembership.readLine();
		String langLine = brLanguages.readLine();
		while (membershipLine!=null) {
			String[] parts = membershipLine.split(" ");
			
			int nodeId = Integer.valueOf(parts[0]);
			int label = Integer.valueOf(parts[1])-1;
			
			communityList.get(label).nodeList.add(new LanguageNode(nodeId,label,langLine));
			
			membershipLine=brMembership.readLine();
			langLine = brLanguages.readLine();
		}
		
		System.out.println("Memberships loaded from file.");

		brMembership.close();
		brLanguages.close();
		
	}
	
	
	/**
	 * Calculates several statistics about each community. The readIt method must be called before this method.
	 */
	public void calcStats() {
	
		HashMap<String,Integer> langCounts = new HashMap<String,Integer>();
		for (int i=0; i<communityList.size(); i++) {
			Community comm = communityList.get(i);
			langCounts.clear();
			
			for (int j=0; j<comm.getSize(); j++) {
				
				LanguageNode node = comm.nodeList.get(j);
				String lang = node.getLanguage();
				Integer count = langCounts.get(lang);
				if (count==null) {
					langCounts.put(lang,Integer.valueOf(1));
				} else {
					langCounts.put(lang,Integer.valueOf(count.intValue()+1));
				}
					
			}
			
			//Now get stats
			comm.setNumLangs(langCounts.size());
			int majLangCount=0;
			String majLang=null;
			for (String lang : langCounts.keySet()) {
				int count = langCounts.get(lang).intValue();
				if (count>majLangCount) {
					majLangCount=count;
					majLang=lang;
				}
			}
			
			comm.setMajLang(majLang);
			comm.setMajLangCount(majLangCount);
		}
	}
	
	/**
	 * 
	 * Writes a comma-separated values file to file. The calcStats() method must be called before this method.
	 * 
	 * There is one line per community, and each line has the following fields:
	 * * label: numeric label of the community
	 * * size: number of notes in the community
	 * * numLangs: number of most-used languages in the community
	 * * majLang: The iso code of the most-most-used language
	 * * majLangCount: The number of users in the community whose  most-used language is majLang
	 * 
	 * @param file File to write data to 
	 * @throws IOException
	 */
	public void writeCommunities(String file) throws IOException {
		FileOutputStream fso = new FileOutputStream(file);
		OutputStreamWriter fileWriter = new OutputStreamWriter(fso,Charset.forName("UTF-8"));
		Community c;
		fileWriter.write("label,size,numLangs,majLang,majLangCount\n");
		for (int i=0; i<communityList.size(); i++) {
			c=communityList.get(i);
			fileWriter.write(i + "," + c.getSize() + "," + c.getNumLangs() + "," + c.getMajLang() + "," + c.getMajLangCount() + "\n");
		}
		fileWriter.close();
		fso.close();
	}
	

	public static void main(String[] args) throws IOException {
		CommunityAnalysis ca = new CommunityAnalysis();
		ca.readIt(20253,//Number of Communities 
				"memberships_smart.txt", //Community membership as saved by CleanLabels.java 
				"langclean.txt"); //Most-used language per node 
		ca.calcStats();
		ca.writeCommunities("community_info.csv");

	}
	
	
	private class Community {
		
		public ArrayList<LanguageNode> nodeList = new ArrayList<LanguageNode>();
		
		
		private int numLangs;
		private String majLang;
		private int majLangCount;
		
		public int getSize() {
			return nodeList.size();
		}

		public int getNumLangs() {
			return numLangs;
		}

		public void setNumLangs(int numLangs) {
			this.numLangs = numLangs;
		}

		public String getMajLang() {
			return majLang;
		}

		public void setMajLang(String majLang) {
			this.majLang = majLang;
		}

		public int getMajLangCount() {
			return majLangCount;
		}

		public void setMajLangCount(int majLangCount) {
			this.majLangCount = majLangCount;
		}
		
		
		
		
	}
	
	private class LanguageNode extends Node {
		public LanguageNode(int id, int label) {
			super(id, label);
		}
		
		public LanguageNode(int id, int label, String lang) {
			this(id,label);
			language=lang;
		}
		
		private String language;
		public String getLanguage(){
			return language;
		}
		public void setLangauge(String lang) {
			language=lang;
		}
	}

}
