import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Base64;
import java.util.TimeZone;

public class WsseToken {

	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String HEADER_WSSE = "X-WSSE";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	//in our case, User is an entity (just a POJO) persisted into sqlite database
	private String user;
	private String password;
	private String nonce;
	private String createdAt;
	private String digest;

	public WsseToken(String user, String password) {
		//we need the user object because we need his username
		this.user = user;
		this.password = password;
		this.createdAt = generateTimestamp();
		this.nonce = generateNonce();
		//System.out.println(this.nonce);
		this.digest = generateDigest();
	}

	private String generateNonce() {
		SecureRandom random = new SecureRandom();
		byte[] chaine = new byte[20];
		random.nextBytes(chaine);
		return bytesToHex(chaine);
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','b','d','e','f'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private String generateTimestamp() {
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		return sdf.format(new Date());
	}

	private String generateDigest() {
		String retdigest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			StringBuilder sb = new StringBuilder();
			sb.append(this.nonce);
			sb.append(this.createdAt);
			sb.append(this.password);
			byte sha[] = md.digest(sb.toString().getBytes());
			retdigest = Base64.getEncoder().encodeToString(sha);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return retdigest;
	}

	public String getWsseHeader() {
		StringBuilder header = new StringBuilder();
		header.append("UsernameToken Username=\"");
		header.append(this.user);
		header.append("\", PasswordDigest=\"");
		header.append(this.digest);
		header.append("\", Nonce=\"");
		header.append(this.nonce);
		header.append("\", Created=\"");
		header.append(this.createdAt);
		header.append("\"");
		return header.toString();
	}

	public String getAuthorizationHeader() {
		return "WSSE profile=\"UsernameToken\"";
	}
}