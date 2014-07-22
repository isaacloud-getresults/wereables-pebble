package pl.sointeractive.isaacloud;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Helper class for downloading the IsaaCloud certificate.
 *
 * @author Mateusz Renes
 */
class SSLCertificateFactory {

    /**
     * Get a current certificate from page.
     *
     * @param port - port the server operates on
     * @param host - address of the server
     * @return Certificate to be added to keystore.
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.net.UnknownHostException
     * @throws java.io.IOException
     * @throws java.security.KeyManagementException
     * @throws java.security.cert.CertificateException
     */
    public static Certificate getCertificate(int port, String host)
            throws NoSuchAlgorithmException, UnknownHostException, IOException,
            KeyManagementException, CertificateException {

        // create custom trust manager to ignore trust paths
        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{trm}, null);
        SSLSocketFactory factory = sc.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        SSLSession session = socket.getSession();
        Certificate[] servercerts = session
                .getPeerCertificates();

        String certStr = "-----BEGIN CERTIFICATE-----\n"
                + Base64.encodeToString(servercerts[0].getEncoded(), Base64.DEFAULT)
                + "\n-----END CERTIFICATE-----\n";

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new ByteArrayInputStream(certStr.getBytes());

        Certificate ca = cf.generateCertificate(caInput);

        socket.close();

        return ca;
    }
}