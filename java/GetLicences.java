import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetLicences {
	private String user;
	private String password;
	private String apilocation;

	public GetLicences(String user, String password, String apilocation) {
		this.user = user;
		this.password = password;
		this.apilocation = apilocation;
	}

	public String getMembreInfo(String id) {
		return getFFVV(apilocation+id);
	}

	public String getLicenceInfo(String id) {
		String tmp = getFFVV(apilocation+id+"/players");
		String [] licences = tmp.split("}}");
		int selectedIndex = 0;
		String maxDate = "";
		for (int i = 0; i < licences.length; i++) {
			String licence = licences [i];
			int index = licence.indexOf("\"starting_at\":");
			if (index >-1) {
				index += "\"starting_at\":".length();
				String date = licence.substring(index,index+10);
				if (date.compareTo(maxDate)>0) {
					maxDate= date;
					selectedIndex = i;
				}
			}
		}
		//System.out.println(licences [selectedIndex]);
		tmp = licences [selectedIndex];
		String licenceNumber = "";
		String valide = "";
		int index = tmp.indexOf("\"licence_number\":\"");
		if (index >-1) {
			index += "\"licence_number\":\"".length();
			licenceNumber = tmp.substring(index,index+10);
		}
		index = tmp.indexOf("\"ending_at\":\"");
		if (index >-1) {
			index += "\"ending_at\":\"".length();
			valide = tmp.substring(index,index+10);
		}
		String result = this.getMembreInfo(id);
		if (result!=null) {
			result = result.substring(0,1) +
					"\"licencenum\":\""+licenceNumber+"\",\"licencedateval\":\""+valide+"\"," + result.substring(1);
		}
		return result;
	}

	public String getFFVV(String request) {
		String result = null;
		try {
			URL url = new URL(request);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setRequestMethod("GET");
			httpCon.setRequestProperty("Content-Type", "application/json");
			httpCon.setConnectTimeout(5000);
			httpCon.setReadTimeout(5000);
			//here you use the WsseToken class to get wsse headers
			WsseToken token = new WsseToken(user, password);
			httpCon.setRequestProperty(WsseToken.HEADER_AUTHORIZATION,
					token.getAuthorizationHeader());
			httpCon.setRequestProperty(WsseToken.HEADER_WSSE, token.getWsseHeader());
			httpCon.connect();
			System.out.println(httpCon.getResponseCode());
			// Buffer the result into a string
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(httpCon.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			result = (sb.toString());
		}
		catch (Exception e) {
			result=null;
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		String id = "4335";
		GetLicences ffvv = new GetLicences("myWsseLogin","myWssePassword","http://api.licences.ffvv.stadline.com/persons/");
		String tmp = ffvv.getLicenceInfo(id);
		System.out.println(tmp);
	}
}
