package net;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;

//import com.charlygao.crypt.Crypt;


public class Network {
	//private static final String Auth = "eWa25vrE";
	//private static final String Key = "2DbcAh3G";
	private static final String UserAgent = "Million/1.0.1 (iPhone; iPhone5,2; 6.0.1)";
	//private static final String UserAgent = "Million/1.0.1 (aries; aries; 4.1.1) Xiaomi/aries/aries:4.1.1/JRO03L/3.9.6:user/release-keys";
	private DefaultHttpClient client;
	
	public Network() {
		client = new DefaultHttpClient();
		HttpParams hp = client.getParams();
		//hp.setParameter("http.socket.timeout", 0x7530);
		//hp.setParameter("http.connection.timeout", 0x7530);
		}
	
	private List<NameValuePair> RequestProcess(List<NameValuePair> source, boolean UseDefaultKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
		Iterator<NameValuePair> i  = source.iterator();
		while(i.hasNext()) {
			NameValuePair n = i.next();
			result.add(new BasicNameValuePair(n.getName(),Crypto.Encrypt2Base64WithKey(n.getValue())));
			}
		return result;
	}
	
	public byte[] ConnectToServer(String url, List<NameValuePair> content, boolean UseDefaultKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClientProtocolException, IOException {
		
		List<NameValuePair> post = RequestProcess(content,true);
		
		HttpPost hp = new HttpPost(url);
		hp.setHeader("Accept", "*/*");
		hp.setHeader("User-Agent", UserAgent);
		hp.setHeader("Accept-Language", "en-us");
		hp.setHeader("Accept-Encoding", "gzip");
		//hp.setHeader("Accept-Encoding", "gzip, deflate");
		hp.setEntity(new UrlEncodedFormEntity(post,"UTF-8"));
		
		//AuthScope as = new AuthScope(hp.getURI().getHost(),hp.getURI().getPort());
		//CredentialsProvider cp = client.getCredentialsProvider();
		//UsernamePasswordCredentials upc = new UsernamePasswordCredentials(Auth,Key);
		//cp.setCredentials(as, upc);
		byte[] b = client.execute(hp,new HttpResponseHandler());
		//return Crypto.DecryptWithKey(b);
		
		// end */
		if (b != null) {
			if (url.contains("gp_verify_receipt?")) {
				// need to be decoded
				return null;
			}
			try {
				if (UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b);
				}
			} catch (Exception ex) {
				if (!UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b);
				}
			}
		} 
		return null;
		}
}
	
	

