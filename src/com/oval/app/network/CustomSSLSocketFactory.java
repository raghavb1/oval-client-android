package com.oval.app.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;

import javax.net.ssl.*;

/**
 * Bypasser SSLFactory implementation which is designed to ignore all the SSL
 * certificate. Note: Please only use this for development testing (for self
 * signed in certificate). Adding this to the public application is a serious
 * blunder.
 * 
 * @author Abhishek Aggarwal
 */
public class CustomSSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
	private SSLSocketFactory sslFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

	public static KeyStore getKeyStoreForCertificate(InputStream certPath) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// From
			// https://www.washington.edu/itconnect/security/ca/load-der.crt
			InputStream caInput = new BufferedInputStream(certPath);
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
			} finally {
				caInput.close();
			}

			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);
			return keyStore;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public CustomSSLSocketFactory(KeyStore keyStore)
			throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(null);
		if (keyStore == null) {

			try {
				SSLContext context = SSLContext.getInstance("TLS");

				// Create a trust manager that does not validate certificate
				// chains and simply
				// accept all type of certificates
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[] {};
					}

					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}
				} };

				// Initialize the socket factory
				context.init(null, trustAllCerts, new SecureRandom());
				sslFactory = context.getSocketFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			sslFactory = context.getSocketFactory();

		}
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
			throws IOException, UnknownHostException {
		return sslFactory.createSocket(socket, host, port, autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslFactory.createSocket();
	}
}
